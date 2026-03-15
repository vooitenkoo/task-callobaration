# Task Collaboration

Веб-приложение для совместной работы над задачами: проекты, задачи, чат, профили, OAuth2 (Google/GitHub), JWT, WebSocket, MinIO для аватаров.

---

## Быстрый старт (за пару минут)

### 1. Поднять инфраструктуру (PostgreSQL + Redis + MinIO)

```bash
docker-compose up -d
```

- **PostgreSQL:** `localhost:5432`, БД `task_platform`, логин/пароль `postgres`
- **Redis:** `localhost:6379`
- **MinIO:** `http://localhost:9000`, логин/пароль `minioadmin` (консоль MinIO — для аватаров)

### 2. Запустить приложение

```bash
# Windows
gradlew.bat bootRun

# Linux/macOS
./gradlew bootRun
```

Приложение: **http://localhost:8084**

### 3. Полезные URL

| Что | URL |
|-----|-----|
| API (Swagger UI) | http://localhost:8084/swagger-ui.html |
| OpenAPI JSON | http://localhost:8084/v3/api-docs |
| Health | http://localhost:8084/actuator/health |
| MinIO Console | http://localhost:9001 (логин `minioadmin` / `minioadmin`) |

### 4. OAuth2 (по желанию)

Для входа через Google/GitHub задайте переменные окружения:

- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`

Либо укажите их в `application.properties` (не коммитьте секреты).

---

## Стек технологий

| Категория | Технологии |
|-----------|-------------|
| Backend | **Spring Boot 3.5**, Java 17 |
| Web | Spring Web, Spring Security, WebSocket |
| БД | PostgreSQL 16, JPA/Hibernate, **Flyway** (миграции) |
| Кэш/сессии | Redis |
| Аутентификация | JWT (jjwt), OAuth2 Client (Google, GitHub) |
| Файлы | MinIO (S3-совместимое хранилище, аватары) |
| Документация API | SpringDoc OpenAPI (Swagger UI) |
| Сборка | Gradle 8.x |

---

## Архитектура проекта

```
src/main/java/com/example/task_collaboration/
├── TaskCollaborationApplication.java   # Точка входа
├── domain/                             # Доменный слой
│   ├── model/                          # Сущности (User, Project, Task, Message, Profile, ProjectMember)
│   ├── repository/                     # JPA-репозитории
│   └── service/                        # Бизнес-логика (User, Task, Project, Message, Profile, JWT)
├── application/                        # Прикладной слой
│   ├── dto/                            # DTO запросов/ответов
│   └── mapper/                         # Маппинг Entity ↔ DTO
└── infrastructure/                     # Инфраструктура
    ├── config/                         # OpenAPI, Async, JWT filter
    ├── controller/                     # REST: Auth, OAuth2, Project, Task, Message, Profile
    ├── security/                       # SecurityConfig, CORS
    ├── oauth2/                         # OAuth2 UserService, Success/Failure handlers
    ├── websocket/                      # WebSocket-чат
    ├── event/                          # События (UserRegistered)
    └── exсeption/                      # Обработка ошибок
```

**Паттерны:** слоистая архитектура (domain / application / infrastructure), Stateless JWT, Flyway для версионирования БД.

---

## Запуск через Docker (всё в контейнерах)

Сборка и запуск приложения вместе с инфраструктурой:

```bash
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d
```

Подробнее: раздел [Docker](#docker) ниже и файл `docs/DOCKER_AND_PRODUCTION.md`.

---

## Конфигурация

Основной файл: `src/main/resources/application.properties`.

Важные параметры:

- Порт: `server.port=8084`
- БД: `spring.datasource.*`, Flyway: `spring.flyway.locations=classpath:templates/db/migration`
- Redis: `spring.data.redis.host/port`
- MinIO: `minio.url`, `minio.access-key`, `minio.secret-key`, `minio.bucket-name`
- JWT: `jwt.secret`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration`

Для продакшена используйте переменные окружения или `application-prod.properties` (см. `docs/DOCKER_AND_PRODUCTION.md`).

---

## Тесты

- Юнит/интеграционные: `./gradlew test` (JUnit 5, Mockito, Spring Boot Test, Spring Security Test).
- **Для полного прогона** нужна поднятая инфраструктура: `docker-compose up -d` (иначе падает тест загрузки контекста из‑за Flyway/PostgreSQL).
- E2E с браузером: `SeleniumDemoTest` — нужен Chrome; для CI обычно headless (уже настроен в тестах).

Документация по Selenium в проекте: `SELENIUM_README.md`.

---

## Документация

| Файл | Назначение |
|------|------------|
| [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) | Краткий обзор: что за проект, уровень, как быстро вспомнить |
| [docs/HOW_IT_WORKS.md](docs/HOW_IT_WORKS.md) | Карта проекта: как устроен код, поток запроса, где что искать |
| [docs/DEVELOPMENT_PLAN.md](docs/DEVELOPMENT_PLAN.md) | Полный план разработки: видение, требования, функционал бэкенда, этапы, стек, AI |
| [docs/SECURITY.md](docs/SECURITY.md) | Аутентификация и авторизация от А до Я: JWT, OAuth2, как работает и что улучшить |
| [docs/DEVOPS_AND_WORKFLOW.md](docs/DEVOPS_AND_WORKFLOW.md) | Git, Docker, настройки, БД и миграции |
| [docs/DOCKER_AND_PRODUCTION.md](docs/DOCKER_AND_PRODUCTION.md) | Docker, сборка образа, прод, переменные окружения |

---

## Уровень проекта (оценка для Java-разработчика)

- **Уровень:** Middle (2–4 года опыта) или сильный Junior.
- **Что затронуто:** Spring Boot 3, Security, JWT, OAuth2, WebSocket, Redis, Flyway, MinIO, слоистая архитектура, REST API, OpenAPI.
- **Что можно добавить для роста:** тесты (больше покрытия), Dockerfile и полноценный docker-compose под прод, CI/CD (GitHub Actions), отдельный фронт (React/Vue) или доработать текущий, мониторинг (Prometheus/Grafana), логирование (структурированные логи).

---

## Лицензия

Учебный/демо-проект.
