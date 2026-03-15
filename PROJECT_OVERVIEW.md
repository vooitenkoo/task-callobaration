# Обзор проекта — вспомнить за пару минут

Краткая шпаргалка: что это за проект, как он устроен и как с ним работать.

---

## Что это за проект

**Task Collaboration** — бэкенд веб-приложения для совместной работы над задачами:

- **Пользователи:** регистрация, вход по логину/паролю, OAuth2 (Google, GitHub), JWT.
- **Проекты и участники:** проекты, роли участников (ProjectMember).
- **Задачи:** задачи в рамках проектов.
- **Чат:** сообщения по проектам (REST + WebSocket).
- **Профили:** профиль пользователя, аватар в MinIO.

Стек: **Spring Boot 3.5**, Java 17, PostgreSQL, Redis, Flyway, MinIO, Spring Security (JWT + OAuth2), WebSocket, SpringDoc OpenAPI.

---

## Структура кода (пакеты)

| Пакет | Назначение |
|-------|------------|
| `domain.model` | Сущности JPA: User, Profile, Project, Task, Message, ProjectMember |
| `domain.repository` | JPA-репозитории (Spring Data) |
| `domain.service` | Сервисы с бизнес-логикой (User, Project, Task, Message, Profile, JwtService) |
| `application.dto` | DTO для API (запросы/ответы) |
| `application.mapper` | Маппинг Entity ↔ DTO |
| `infrastructure.controller` | REST-контроллеры (Auth, OAuth2, Project, Task, Message, Profile) |
| `infrastructure.security` | SecurityConfig, JWT-фильтр, CORS |
| `infrastructure.oauth2` | OAuth2 UserService, Success/Failure handlers |
| `infrastructure.websocket` | Обработчик WebSocket-чата |
| `infrastructure.config` | OpenAPI, Async, настройки приложения |
| `infrastructure.event` | События (например, UserRegistered) |

Логика разделена: домен в `domain`, слой приложения в `application`, всё внешнее (HTTP, security, WebSocket) в `infrastructure`.

---

## Как запустить

1. **Инфраструктура:**  
   `docker-compose up -d`  
   Поднимаются PostgreSQL (5432), Redis (6379), MinIO (9000, консоль 9001).

2. **Приложение:**  
   `./gradlew bootRun` (или `gradlew.bat bootRun` на Windows).  
   Порт: **8084**.

3. **Проверка:**  
   - API: http://localhost:8084/swagger-ui.html  
   - Health: http://localhost:8084/actuator/health  

Миграции Flyway выполняются при старте из `src/main/resources/templates/db/migration/` (V1–V22).

---

## Конфиг и переменные окружения

- **БД:** в `application.properties` — `localhost:5432`, БД `task_platform`, пользователь `postgres`. Для прода — лучше через переменные (`SPRING_DATASOURCE_URL` и т.д.).
- **Redis:** `localhost:6379` (можно переопределить через `SPRING_DATA_REDIS_HOST`).
- **MinIO:** URL, ключи и bucket в `application.properties`; в прод — через переменные.
- **JWT:** `jwt.secret`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration`.
- **OAuth2:** `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET` (или в конфиге).

---

## Что можно добавить/потренировать

- **Новый сервис:** по аналогии с `TaskService`/`ProjectService` — доменный сервис + контроллер + DTO + при необходимости миграция Flyway.
- **Docker:** образ приложения (Dockerfile), `docker-compose` с сервисом app — см. `docs/DOCKER_AND_PRODUCTION.md`.
- **Прод:** профиль `prod`, переменные окружения, отключение `show-sql`, настройка CORS и секретов.
- **Тесты:** больше unit/интеграционных тестов; E2E уже есть заготовка с Selenium (`SeleniumDemoTest`, `SELENIUM_README.md`).
- **Новые технологии:** например, Spring Boot 3.2+ Virtual Threads, Testcontainers для тестов, отдельный фронт на React/Vue.

---

## Уровень проекта

Подходит для **Middle** или сильного **Junior**: Spring Boot 3, Security, JWT, OAuth2, WebSocket, Redis, Flyway, MinIO, слоистая архитектура, REST, OpenAPI. Хорошая база, чтобы вспомнить практики и постепенно добавлять новые фичи и инфраструктуру.
