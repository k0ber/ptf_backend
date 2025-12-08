# ЭТАП 1: СБОРКА (BUILDER)
FROM gradle:jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew

# 1. Запускаем сборку и выполняем таску installDist
RUN ./gradlew :app:installDist --no-daemon --info

# ---

# ЭТАП 2: ФИНАЛЬНЫЙ ОБРАЗ (RUNTIME)
# Используем легкий JRE на Alpine Linux
FROM eclipse-temurin:17-jre-alpine
WORKDIR /opt/app

# 2. Копируем ВСЕ файлы, необходимые для запуска, из папки install/app
# Эта папка содержит lib/, bin/ и ваш Plain JAR
COPY --from=builder /app/app/build/install/app .

# 3. Устанавливаем права на запуск скрипта (Launcher Script)
# Исполняемый скрипт находится в ./bin/app
RUN chmod +x bin/app

# Установка порта
EXPOSE 8080

# 4. Команда для запуска приложения (используем Launcher Script)
# Запуск через скрипт гарантирует правильную настройку Classpath
ENTRYPOINT ["/opt/app/bin/app"]