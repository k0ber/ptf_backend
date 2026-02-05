# Patifiner Backend 🚀

![Tests](https://github.com/k0ber/ptf_backend/actions/workflows/test_on_push.yaml/badge.svg)
[![Allure Report](https://img.shields.io/badge/Allure%20Report-deployed-yellowgreen)](https://k0ber.github.io/ptf_backend/)

Бэкенд-часть социальной платформы **Patifiner** — приложения для поиска друзей и единомышленников.

Frontend часть проекта: [ptf_frontend](https://github.com/k0ber/ptf_frontend)

---

## 🌐 Статус и Деплой
Сервер развернут и доступен для проверки:
- **API Health Check:** [https://api.patifiner.ru/check](https://api.patifiner.ru/check)
- **CI/CD:** GitHub Actions настроены на прогон тестов (JDK 21) и автоматическую публикацию Allure-отчетов.
- **Инфраструктура:** Docker Compose + Traefik (автоматический SSL через Let's Encrypt).

---

## 🛠 Технологический стек
- **Kotlin 2.3.0**
- **JDK**
- **Ktor 3.4**
- **Koin (DI)**
- **Exposed**
- **Docker** & **Traefik**
- **Allure** (Test Reporting)

---

## 🚀 Локальная разработка

### 1. Запуск базы данных
Самый быстрый способ поднять PostgreSQL нужной версии с настроенной БД:

**macOS / Linux (Bash):**
```bash
docker run --name ptf-db -e POSTGRES_DB=ptf_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:16.9-alpine
```

**Windows (PowerShell):**
```powershell
docker run --name ptf-db -e POSTGRES_DB=ptf_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:16.9-alpine
```

### 2. Конфигурация
Убедитесь, что параметры подключения в `app/src/main/resources/application-dev.conf` соответствуют данным выше (порт 5432, user: postgres, password: password).

### 3. Запуск приложения
Приложение требует указания пути к конфигу:
```bash
./gradlew :app:run --args="-config=app/src/main/resources/application-dev.conf"
```
*В IntelliJ IDEA добавьте этот флаг в Program Arguments.*

---

## 🧪 Тестирование и отчеты

### Автоматические тесты
```bash
./gradlew test
```

### Allure-отчеты
Для генерации и просмотра отчетов локально:
```bash
./gradlew allureReport
./gradlew allureServe
```

### Postman
В корне проекта лежит `ptf.postman_collection.json`. Импортируйте его в Postman для тестирования API.

---

## 🏗 Структура проекта
- **`:app`** — точка входа и конфигурация.
- **`:core`** — база: авторизация, БД (Exposed + JSONB), общие утилиты.
- **`:features`** — бизнес-логика (User, Events, Topics, Search, Geo и др.).
- **`build-logic`** — общие Gradle-плагины (Composite Build).

---

## 📜 Лицензия
Copyright (c) 2026 Nikita Polyakov. All rights reserved.
Использование разрешено только в образовательных целях. Коммерческое копирование запрещено.
