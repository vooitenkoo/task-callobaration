# Как всё устроено — карта проекта

Когда смотришь на код и «разбегаются глаза», иди по этой карте: с чего начать и как части связаны.

---

## 1. Точка входа

**Один класс:** `TaskCollaborationApplication.java` — метод `main` запускает `SpringApplication.run(...)`.  
Spring подхватывает все `@Configuration`, бины, контроллеры из пакетов ниже `com.example.task_collaboration`.

---

## 2. Запрос приходит в приложение — кто за что отвечает

```
HTTP-запрос
    → Security (SecurityConfig) — кто может зайти (JWT / OAuth2)
    → Controller (REST) — разбирает путь, вызывает Service
    → Service (domain) — бизнес-логика, работа с Repository
    → Repository (JPA) — чтение/запись в БД
```

- **Контроллеры** только: принять запрос, вызвать сервис, вернуть DTO. Логики минимум.
- **Сервисы** — вся логика: проверки прав, создание/обновление сущностей, вызов других сервисов.
- **Репозитории** — доступ к таблицам. Обычно только интерфейсы `JpaRepository<Entity, Id>`.

---

## 3. Аутентификация — как понимать «кто залогинен»

- **JWT:** клиент шлёт заголовок `Authorization: Bearer <token>`. Цепочка: `JwtAuthenticationFilter` → проверка токена → в контекст кладётся `Authentication` с пользователем.
- **OAuth2:** пользователь идёт на `/oauth2/authorization/google` (или github) → редирект на провайдер → callback → `OAuth2AuthenticationSuccessHandler` создаёт/находит юзера и выдаёт JWT.
- В контроллере текущий пользователь: `@AuthenticationPrincipal CustomUserDetails userDetails`, дальше `userDetails.getUser().getId()`.

Полезно один раз пройти: `SecurityConfig` → какие URL публичные, какие требуют auth → `JwtAuthenticationFilter` → как из токена достаётся User.

---

## 4. Основные сущности и где они живут

| Сущность    | Таблица       | Смысл |
|------------|---------------|--------|
| User       | users         | Пользователь (email, пароль или OAuth2). |
| Profile    | profiles      | Профиль (аватар, имя и т.д.), связь 1–1 с User. |
| Project    | projects      | Проект. |
| ProjectMember | project_members | Участник проекта (роль: OWNER, ADMIN, MEMBER). |
| Task       | tasks         | Задача в проекте (createdBy, assignee, status, deadline). |
| Message    | messages      | Сообщение в чате (по проекту/задаче). |
| Activity   | activities    | Лог действий (кто что сделал по задаче). |
| File       | files         | Файл, привязанный к задаче. |

Связи: Project → ProjectMember → User; Project → Task; Task → File, Activity; чат через Message.

---

## 5. Типичный поток: «создать задачу»

1. `POST /api/tasks` → `TaskController.createTask(...)`.
2. Из `userDetails` достаётся текущий User.
3. `TaskService.createTask(currentUser, dto)`:
   - проверка, что пользователь — участник проекта и роль OWNER/ADMIN;
   - маппинг DTO → Task (TaskMapper);
   - установка project, createdBy, при необходимости assignee, загрузка файла в MinIO и создание File;
   - `taskRepository.save(task)`.
4. Возврат `TaskResponseDTO` клиенту.

Аналогично смотреть: обновление задачи (`updateTask`), удаление (`deleteTask`), получение списка — всё в `TaskService` + `TaskController`.

---

## 6. Где что искать в коде

- **REST API:** `infrastructure.controller` — по путям `/api/...`.
- **Права доступа:** `SecurityConfig`, плюс в сервисах проверки через `ProjectMemberRepository` (роль пользователя в проекте).
- **БД:** миграции в `src/main/resources/templates/db/migration/` (Flyway), сущности в `domain.model`, репозитории в `domain.repository`.
- **Конфиг:** `application.properties`; для прода — переменные окружения или отдельный профиль.

---

## 7. Что поднимается при старте

- **PostgreSQL** — основные данные (пользователи, проекты, задачи, сообщения, активности и т.д.).
- **Redis** — кэш/сессии, если используются (в коде может быть через Spring Data Redis).
- **MinIO** — хранение файлов (аватары, вложения к задачам).
- **Приложение** — читает конфиг, подключается к БД, применяет Flyway, поднимает HTTP и WebSocket.

Если что-то «не поднимается» — смотреть: порты (5432, 6379, 9000, 8084), переменные окружения, логи приложения и `docker-compose` (если инфра в Docker).

---

## 8. Как не тупить при правках

1. Выбери одну фичу (например «создание задачи» или «чат»).
2. Найди контроллер по пути в Swagger или по `@RequestMapping`.
3. От контроллера пройди в сервис — там вся логика.
4. От сервиса — в репозитории и сущности.
5. Конфиг и безопасность — в `infrastructure.config` и `infrastructure.security`.

Сначала «прочитай» один поток от HTTP до БД, потом уже меняй или добавляй новое по той же схеме.
