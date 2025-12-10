# ЭТАП 1: СБОРКА
FROM gradle:jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && \
    ./gradlew :app:installDist --no-daemon --info --stacktrace

# ЭТАП 2: ФИНАЛЬНЫЙ ОБРАЗ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/app

# Копируем собранное приложение
COPY --from=builder /app/app/build/install/app .

# Права и запуск
RUN chmod +x bin/app
EXPOSE 8080
ENTRYPOINT ["/opt/app/bin/app"]