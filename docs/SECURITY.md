# Безопасность: аутентификация и авторизация от А до Я

Как в этом проекте устроены вход, проверка прав и защита API. После прочтения будет понятно, работает ли безопасность и соответствует ли она ожиданиям.

---

## 1. Общая схема

- **Аутентификация** — «кто ты?»: вход по паролю или через OAuth2 (Google/GitHub), в результате выдаётся JWT.
- **Авторизация** — «что тебе можно?»: проверка прав на уровне URL (кто может вызывать эндпоинт) и на уровне бизнес-логики (например, только участник проекта может видеть задачи).

В приложении:

1. Для **REST API** используется **JWT** в заголовке `Authorization: Bearer <token>`. Сессий нет (stateless).
2. **OAuth2** используется только для **входа**: пользователь перенаправляется на Google/GitHub, после успешного входа наш бэкенд создаёт/обновляет пользователя и **тоже выдаёт JWT**. Дальше все запросы идут с этим JWT так же, как после обычного логина.
3. **Проверка прав** в коде: в сервисах явно проверяется членство в проекте и роль (OWNER/ADMIN/MEMBER) через `ProjectMemberRepository`; на контроллерах при необходимости — `@PreAuthorize("isAuthenticated()")`.

---

## 2. Цепочка обработки запроса (кто за что отвечает)

Любой HTTP-запрос проходит примерно так:

```
Входящий запрос
  → CORS (CorsFilter) — разрешённые origin/methods/headers
  → SecurityFilterChain (SecurityConfig)
       → Разрешить ли путь без авторизации? (permitAll)
       → Если нет — JwtAuthenticationFilter
            → Есть заголовок Authorization: Bearer <token>?
            → Валиден ли токен? (JwtTokenProvider)
            → Достать email из токена → загрузить User (CustomUserDetailsService)
            → Положить Authentication в SecurityContextHolder
       → Проверить: для остальных путей требуется authenticated()
  → Controller → Service → ...
```

Итого: если путь не в `permitAll`, то без валидного JWT запрос не дойдёт до контроллера — Spring вернёт 401.

---

## 3. Что объявлено «без проверки» (permitAll)

В `SecurityConfig.securityFilterChain()` без авторизации доступны:

| Путь | Назначение |
|------|------------|
| `OPTIONS /**` | CORS preflight |
| `/api/auth/**` | Регистрация, логин (получение JWT) |
| `/api/oauth2/**` | Информация о провайдерах OAuth2, URL для входа |
| `/oauth2/**`, `/login/oauth2/**` | Редиректы OAuth2 (в т.ч. callback от Google/GitHub) |
| `/swagger-ui/**`, `/v3/api-docs/**`, … | Документация API |
| `/ws/**`, `/topic/**`, `/queue/**`, `/app/**` | WebSocket (здесь нет проверки JWT в конфиге; при необходимости проверять вручную) |

Все остальные запросы (`anyRequest().authenticated()`) требуют успешной аутентификации (в контексте должен быть установлен `Authentication`).

---

## 4. JWT: как устроен и как проверяется

### 4.1 Где создаётся токен

- **Логин (email + пароль):** `AuthController.login` → `AuthService.login` → проверка пароля через `PasswordEncoder`, затем `JwtTokenProvider.generateAccessToken(email)` и `generateRefreshToken(email)`.
- **OAuth2:** после входа через Google/GitHub срабатывает `OAuth2AuthenticationSuccessHandler` → там вызывается `JwtService.generateAccessToken(user)` и `generateRefreshToken(user)` (JwtService оборачивает JwtTokenProvider). Токены возвращаются в JSON в теле ответа и дополнительно в заголовках Set-Cookie.

В токене в `subject` (claim `sub`) хранится **email** пользователя. Роль в JWT не зашита — при каждом запросе пользователь подгружается из БД и права берутся из `User.role` и из членства в проектах.

### 4.2 Где хранятся секрет и срок жизни

- В `application.properties`: `jwt.secret`, `jwt.access-token-expiration` (мс), `jwt.refresh-token-expiration` (мс).
- `JwtTokenProvider` при старте строит ключ подписи из `jwt.secret` (HMAC-SHA). Важно: секрет должен быть достаточно длинным и не попадать в репозиторий в прод.

### 4.3 Как токен проверяется при запросе

1. `JwtAuthenticationFilter` (выполняется до `UsernamePasswordAuthenticationFilter`):
   - Читает заголовок `Authorization`, если нет префикса `Bearer ` — передаёт запрос дальше без установки контекста (запрос к защищённому пути тогда завершится 401).
   - Берёт строку после `Bearer ` как токен.
   - Вызывает `JwtTokenProvider.validateToken(token)` (подпись + срок действия).
   - Если невалиден — логирует и передаёт запрос дальше (401 на защищённом пути).
   - Если валиден — извлекает email: `JwtTokenProvider.getEmailFromToken(token)`.
   - Загружает пользователя: `UserDetailsService.loadUserByUsername(email)` → у нас это `CustomUserDetailsService`, который по email находит `User` и возвращает `CustomUserDetails(user)`.
   - Создаёт `UsernamePasswordAuthenticationToken(userDetails, null, authorities)` и кладёт в `SecurityContextHolder.getContext().setAuthentication(auth)`.
2. Дальше по цепочке Spring Security видит, что `Authentication` есть, и считает запрос аутентифицированным.

Итого: **аутентификация по JWT работает**: без токена или с невалидным токеном доступ к защищённым эндпоинтам запрещён; с валидным токеном в контексте оказывается пользователь из БД с актуальными ролями.

---

## 5. UserDetails и роли

- **CustomUserDetails** реализует `UserDetails`, хранит внутри `User`, возвращает:
  - `getUsername()` = email (для Spring Security это «имя пользователя»);
  - `getAuthorities()` = один элемент `ROLE_USER` или `ROLE_ADMIN` (из `User.getRole()`);
  - `isAccountNonLocked()` = `!user.isBlocked()`.
- **CustomUserDetailsService** реализует `UserDetailsService`: по email ищет пользователя в БД и возвращает `CustomUserDetails(user)`.

В контроллерах текущий пользователь берётся так: `@AuthenticationPrincipal CustomUserDetails userDetails`, дальше `userDetails.getUser().getId()` и т.д. Роль глобальная (`User.role`) используется, если добавить, например, `@PreAuthorize("hasRole('ADMIN')")`. Сейчас на эндпоинтах в основном только `@PreAuthorize("isAuthenticated()")`, а проверки «может ли этот пользователь править этот проект/задачу» делаются в сервисах через `ProjectMemberRepository`.

---

## 6. OAuth2: полный поток

1. Пользователь переходит по ссылке (или редирект с фронта) на наш URL, например: `/oauth2/authorization/google` или `/oauth2/authorization/github`.
2. Spring Security перенаправляет на страницу входа Google/GitHub (authorization request с `client_id`, `redirect_uri`, `scope`).
3. Пользователь вводит логин/пароль у провайдера и разрешает доступ.
4. Провайдер перенаправляет браузер на наш callback URL: `/login/oauth2/code/google` или `/login/oauth2/code/github` с параметром `code`.
5. Spring Security обменивает `code` на access token у провайдера, затем запрашивает данные пользователя (userinfo).
6. Вызывается наш **CustomOAuth2UserService.loadUser()**:
   - По email из атрибутов OAuth2 ищется пользователь в БД.
   - Если найден — обновляются имя, провайдер, providerId, аватар и т.д.
   - Если не найден — создаётся новый пользователь (OAuth2-only, без пароля).
   - В контекст Spring возвращается **CustomOAuth2UserPrincipal(user, attributes)** (реализует OAuth2User, отдаёт роли из `User.role`).
7. Срабатывает **OAuth2AuthenticationSuccessHandler**:
   - Из `Authentication.getPrincipal()` достаётся `CustomOAuth2UserPrincipal` и из него `User`.
   - Генерируются access и refresh JWT через `JwtService`.
   - В ответ записывается JSON с токенами и данными пользователя; дополнительно выставляются cookies с токенами.

Ошибки обрабатывает **OAuth2AuthenticationFailureHandler**: возвращается 401 и JSON с сообщением об ошибке.

Итого: **OAuth2 работает как способ входа**; после входа приложение не различает «вошёл по паролю» и «вошёл через Google» — везде один и тот же JWT и одна и та же модель `User`.

---

## 7. Авторизация на уровне API и бизнес-логики

- **Уровень URL:** в конфиге задано только «всё, что не permitAll, требует authenticated()». То есть отдельно по ролям (USER/ADMIN) эндпоинты не разведены — везде достаточно быть просто залогиненным.
- **Уровень операций:** в сервисах явно проверяется:
  - членство в проекте (`ProjectMemberRepository.findByUserIdAndProjectId`);
  - роль в проекте (OWNER может удалять задачу, OWNER/ADMIN — создавать/редактировать и т.д.).

При нарушении выбрасывается, например, `AccessDeniedException`, что приводит к 403. То есть **авторизация «кто что может делать с каким проектом/задачей» реализована в коде сервисов**, а не только в конфиге Security.

---

## 8. Пароли

- При регистрации пароль хэшируется через **BCrypt** (Strength 8) в `AuthService.register` и сохраняется в `User.password`. Обычный текст нигде не хранится.
- При логине проверка через `PasswordEncoder.matches(loginDto.getPassword(), user.getPassword())`. OAuth2-пользователи могут не иметь пароля (поле null) — для них вход только через OAuth2.

---

## 9. CORS

В `SecurityConfig` задаётся `CorsConfigurationSource`: разрешены origin’ы `http://localhost:3000` и `http://localhost:8084`, методы GET/POST/PUT/DELETE/OPTIONS и т.д., заголовки `Authorization`, `Content-Type` и др. Для продакшена нужно заменить список origin на реальный домен фронта.

---

## 10. Что работает корректно

- Stateless JWT: доступ по токену в заголовке, без сессий на сервере.
- Логин и регистрация с хэшированием паролей.
- OAuth2 Google/GitHub как способ входа с выдачей того же JWT.
- Проверка прав в сервисах по проектам и ролям участников.
- Публичные пути явно перечислены, остальное только для аутентифицированных.

---

## 11. Что улучшить или добавить (кратко)

| Тема | Сейчас | Рекомендация |
|------|--------|--------------|
| **Refresh token** | Нет эндпоинта обмена refresh на новую пару | Добавить `POST /api/auth/refresh`, проверять refresh, выдавать новые токены; хранить или помечать использованные refresh (например, в Redis). |
| **Logout** | Нет | При logout помещать access (или refresh) в blacklist в Redis до истечения срока или отзывать refresh. |
| **Срок access token** | Задаётся в конфиге (например 15 мин) | Оставить коротким; продлевать через refresh. |
| **JWT secret** | В properties | В проде — только из переменных окружения или секрет-менеджера, длинный ключ (например 256 бит для HS256). |
| **WebSocket** | В конфиге permitAll для /ws, /app, … | Если нужна защита — передавать токен при установке соединения и проверять его в обработчике. |
| **Rate limiting** | Нет | Для логина и регистрации ограничить число запросов (Bucket4j, Resilience4j или фильтр). |
| **OAuth2 callback для SPA** | Success handler пишет JSON в ответ | Для SPA удобно редиректить на фронт с токенами в query/fragment или через промежуточную страницу, которая передаёт их в родительское окно. |
| **Роли на эндпоинтах** | Только isAuthenticated() | При появлении админских операций добавить, например, `@PreAuthorize("hasRole('ADMIN')")`. |

---

## 12. Краткая шпаргалка «кто где»

| Класс/компонент | Роль |
|-----------------|------|
| **SecurityConfig** | Цепочка фильтров, permitAll-пути, oauth2Login, CORS, PasswordEncoder. |
| **JwtAuthenticationFilter** | Читает Bearer token, валидирует, загружает User, ставит Authentication в контекст. |
| **JwtTokenProvider** | Генерация access/refresh, парсинг, проверка подписи и срока. |
| **JwtService** | Обёртка над JwtTokenProvider для удобства в сервисах. |
| **CustomUserDetailsService** | Загрузка User по email → CustomUserDetails (для фильтра и Spring Security). |
| **CustomUserDetails** | UserDetails с User внутри, authorities из User.role. |
| **AuthService** | Регистрация (хэш пароля), логин (проверка пароля + выдача JWT). |
| **CustomOAuth2UserService** | После OAuth2: найти/создать User, вернуть CustomOAuth2UserPrincipal. |
| **OAuth2AuthenticationSuccessHandler** | После успешного OAuth2: выдать JWT и вернуть JSON (и cookies). |
| **OAuth2AuthenticationFailureHandler** | При ошибке OAuth2: 401 + JSON. |

Если что-то перестанет работать (например, 401 на защищённом эндпоинте с валидным токеном), удобно пройти по цепочке: заголовок Authorization → JwtAuthenticationFilter → JwtTokenProvider → CustomUserDetailsService → контекст. По этому документу можно быстро вспомнить, как у тебя устроена безопасность и что проверить первым делом.
