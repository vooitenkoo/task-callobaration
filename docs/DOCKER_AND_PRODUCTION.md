# Docker и выход в прод

Краткое руководство: как собирать образ приложения, запускать всё в Docker и что учесть для продакшена.

---

## 1. Локальная разработка (инфраструктура в Docker)

Поднять только БД, Redis и MinIO:

```bash
docker-compose up -d
```

Приложение запускаете на хосте:

```bash
./gradlew bootRun
```

Так удобно отлаживать и менять код без пересборки образа.

---

## 2. Полный запуск в Docker (приложение + инфраструктура)

Сборка образа приложения и запуск всех сервисов:

```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

- Первый раз будет долго из‑за сборки JAR и образа.
- Приложение доступно на **http://localhost:8084**.

**Важно:** для сборки в Docker нужен `gradle-wrapper.jar` в `gradle/wrapper/`. Если его нет, выполните один раз локально:

```bash
./gradlew wrapper
```

и закоммитьте `gradle/wrapper/gradle-wrapper.jar`.

---

## 3. Только инфраструктура (как сейчас)

Только PostgreSQL, Redis, MinIO:

```bash
docker-compose up -d
```

Приложение — с хоста (`./gradlew bootRun`). В `application.properties` уже указаны `localhost` для БД, Redis и MinIO.

---

## 4. Переменные окружения для приложения в Docker

В `docker-compose.app.yml` заданы переменные для работы приложения в контейнере:

- `SPRING_DATASOURCE_*` — подключение к PostgreSQL (хост `postgres`).
- `SPRING_DATA_REDIS_HOST=redis`.
- `MINIO_*` — MinIO внутри Docker-сети (хост `minio`).
- `JWT_SECRET` — лучше задать свой в прод (через env или secrets).
- `GOOGLE_*`, `GITHUB_*` — для OAuth2 (по желанию).

В продакшене не храните секреты в файлах композа — используйте секреты Docker/облака или переменные окружения из системы/CI.

---

## 5. Что сделать для продакшена

1. **Профиль `prod`**  
   Создайте `application-prod.properties` (или `application-prod.yml`) и подключайте его через `spring.profiles.active=prod`. В прод-профиле:
   - отключите `spring.jpa.show-sql`,
   - настройте логирование (уровни, формат),
   - при необходимости отключите или ограничьте Swagger.

2. **Секреты**  
   Пароли БД, JWT, OAuth2, MinIO — только из переменных окружения или из секрет-менеджера, не в репозитории.

3. **БД и миграции**  
   Flyway уже включён; в прод используйте отдельную БД и убедитесь, что миграции применяются при деплое (при старте приложения).

4. **CORS**  
   В `SecurityConfig` задайте разрешённые origin для фронта вашего домена, а не `*` в прод.

5. **Health и мониторинг**  
   Actuator уже отдаёт `health`, `info`, `flyway`. В прод можно оставить только `health` и вынести его на отдельный порт/путь при необходимости.

6. **Образ приложения**  
   Текущий Dockerfile многостадийный, на JRE 17 — подходит для прода. Для прод можно добавить:
   - сканирование уязвимостей образа (например, Trivy),
   - непод root уже настроен в Dockerfile.

---

## 6. Полезные команды

| Действие | Команда |
|----------|--------|
| Поднять всё (инфра + app) | `docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d --build` |
| Логи приложения | `docker-compose -f docker-compose.yml -f docker-compose.app.yml logs -f app` |
| Остановить всё | `docker-compose -f docker-compose.yml -f docker-compose.app.yml down` |
| Пересобрать только app | `docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d --build app` |

После изменений в коде пересоберите образ и перезапустите сервис `app`.
