# ЭТАП 1: СБОРКА (BUILDER)
# Используем образ с предустановленным Gradle и JDK 21 для быстрой сборки.
FROM gradle:jdk21 AS builder

# Установка рабочей директории в контейнере
WORKDIR /app

# Копируем все файлы проекта
COPY . .

# Даем права на выполнение Gradle Wrapper
RUN chmod +x gradlew

# Запускаем сборку. Используем shadowJar, который собирает все зависимости в один JAR (Fat JAR).
RUN ./gradlew shadowJar --no-daemon

# ---

# ЭТАП 2: ФИНАЛЬНЫЙ ОБРАЗ (RUNTIME)
# Используем очень легкий образ JRE (Java Runtime Environment) на базе Alpine Linux
# Это уменьшает размер финального образа и повышает безопасность.
FROM eclipse-temurin:21-jre-alpine

# Установка рабочей директории для запуска
WORKDIR /opt/app

# Копируем скомпилированный Fat JAR из этапа 'builder'
# (Вам нужно убедиться, что путь /app/app/build/libs/app-all.jar верен
# для вашего Gradle-проекта с плагином shadowJar).
COPY --from=builder /app/app/build/libs/app-all.jar app.jar

# Установка порта
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]