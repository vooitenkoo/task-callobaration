# Сборка приложения
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Кэширование зависимостей: копируем только файлы сборки
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle gradle

# Скачиваем зависимости (без сборки кода)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Исходный код и сборка
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Финальный образ для запуска
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Пользователь без root (безопасность)
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
