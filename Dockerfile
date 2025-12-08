# Использование OpenJDK для сборки (с JDK)
FROM openjdk:21-jdk-slim AS build

# Установка рабочей директории
WORKDIR /app

# Копирование файлов проекта и сборка (предполагаем, что вы используете Gradle)
# Если у вас Kotlin/JVM, используйте команду сборки вашего проекта (например, ./gradlew build)
COPY . .
RUN chmod +x ./gradlew
# Сборка приложения в JAR-файл
RUN ./gradlew clean build --no-daemon

# --- Финальный образ (Runtime) ---
FROM openjdk:21-jre-slim

# Создание рабочей директории
WORKDIR /app

# Копирование скомпилированного JAR-файла из этапа 'build'
# Путь может меняться в зависимости от вашей Gradle-конфигурации
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Установка порта, который будет слушать Ktor
EXPOSE 8080

# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]