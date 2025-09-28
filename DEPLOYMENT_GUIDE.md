# 🚀 Руководство по развертыванию HireHi на Render.com

## 📋 Обзор

Это руководство поможет вам развернуть Kotlin приложение HireHi на платформе Render.com. Приложение представляет собой веб-сервис для поиска QA вакансий с фильтрацией по ключевым словам Kotlin и Android.

## 🏗️ Архитектура приложения

- **Backend**: Kotlin + Ktor (веб-сервер)
- **Frontend**: HTML + CSS + JavaScript (встроенный в Ktor)
- **Данные**: API hirehi.ru (внешний источник)
- **Порт**: 10000 (настраивается через переменную окружения PORT)

## 📁 Структура проекта

```
HIREHI_KOTLIN/
├── src/main/kotlin/com/hirehi/
│   ├── Main.kt                           # Точка входа
│   ├── application/
│   │   └── HireHiApplication.kt         # Основная логика
│   ├── data/
│   │   ├── remote/HireHiScraper.kt      # Скрапинг данных
│   │   └── repository/JobRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/Job.kt                 # Модели данных
│   │   ├── repository/JobRepository.kt  # Интерфейсы
│   │   └── usecase/GetJobsUseCase.kt    # Use Cases
│   └── presentation/
│       ├── service/JobService.kt        # Сервисы
│       ├── view/JobView.kt              # HTML генерация
│       └── webserver/WebServer.kt       # Ktor сервер
├── build.gradle.kts                     # Gradle конфигурация
├── Dockerfile                          # Docker образ
├── .dockerignore                       # Исключения для Docker
└── render.yaml                         # Конфигурация Render
```

## 🐳 Docker конфигурация

### Dockerfile

```dockerfile
# Используем официальный образ Gradle с JDK 17
FROM gradle:8.5-jdk17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы конфигурации Gradle
COPY build.gradle.kts gradle.properties settings.gradle.kts ./
COPY gradle/ ./gradle/

# Копируем исходный код
COPY src/ ./src/

# Собираем приложение
RUN ./gradlew build --no-daemon

# Создаем финальный образ
FROM openjdk:17-jre-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл
COPY --from=build /app/build/libs/*.jar app.jar

# Открываем порт (Render автоматически установит PORT)
EXPOSE 10000

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### .dockerignore

```
# Gradle
.gradle/
build/
gradle/

# IDE
.idea/
*.iml
.vscode/

# OS
.DS_Store
Thumbs.db

# Logs
*.log

# Temporary files
*.tmp
*.temp

# Local data files
*.json
*.html
```

## ⚙️ Конфигурация Render

### render.yaml

```yaml
services:
  - type: web
    name: hirehi-kotlin
    env: docker
    dockerfilePath: ./Dockerfile
    plan: free
    envVars:
      - key: PORT
        value: 10000
      - key: JAVA_OPTS
        value: "-Xmx512m -Xms256m"
    healthCheckPath: /api/status
    autoDeploy: true
```

## 🚀 Пошаговое развертывание

### Шаг 1: Подготовка репозитория

1. **Убедитесь, что все файлы закоммичены:**
   ```bash
   git add .
   git commit -m "Prepare for Render deployment"
   git push origin main
   ```

2. **Проверьте наличие всех необходимых файлов:**
   - ✅ `Dockerfile`
   - ✅ `.dockerignore`
   - ✅ `render.yaml`
   - ✅ `build.gradle.kts`

### Шаг 2: Создание сервиса на Render

1. **Войдите в Render Dashboard:**
   - Откройте [dashboard.render.com](https://dashboard.render.com)
   - Войдите в аккаунт или зарегистрируйтесь

2. **Создайте новый Web Service:**
   - Нажмите "New +" → "Web Service"
   - Подключите ваш GitHub репозиторий
   - Выберите репозиторий `HIREHI_KOTLIN`

3. **Настройте сервис:**
   - **Name**: `hirehi-kotlin`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Plan**: `Free` (для начала)
   - **Auto-Deploy**: `Yes`

4. **Настройте переменные окружения:**
   - `PORT`: `10000`
   - `JAVA_OPTS`: `-Xmx512m -Xms256m`

### Шаг 3: Развертывание

1. **Нажмите "Create Web Service"**
2. **Дождитесь завершения сборки** (5-10 минут)
3. **Проверьте логи** на наличие ошибок

### Шаг 4: Проверка работоспособности

1. **Откройте URL сервиса** (например: `https://hirehi-kotlin.onrender.com`)
2. **Проверьте основные эндпоинты:**
   - `/` - главная страница с вакансиями
   - `/api/status` - статус API
   - `/api/jobs` - JSON с вакансиями
   - `/api/refresh` - обновление данных (POST)

## 🔧 Настройка и оптимизация

### Переменные окружения

| Переменная | Описание | Значение по умолчанию |
|------------|----------|----------------------|
| `PORT` | Порт сервера | `10000` |
| `JAVA_OPTS` | Опции JVM | `-Xmx512m -Xms256m` |

### Мониторинг

1. **Логи приложения:**
   - Render Dashboard → Your Service → Logs
   - Мониторинг ошибок и производительности

2. **Метрики:**
   - CPU и Memory usage
   - Response time
   - Request count

### Обновления

1. **Автоматическое развертывание:**
   - При push в main ветку
   - Zero-downtime deployment

2. **Ручное развертывание:**
   - Render Dashboard → Manual Deploy

## 🐛 Устранение неполадок

### Частые проблемы

1. **Build fails:**
   ```
   Error: Build failed
   ```
   **Решение:** Проверьте логи сборки, убедитесь что все зависимости корректны

2. **Service not responding:**
   ```
   Error: Service unavailable
   ```
   **Решение:** Проверьте что приложение слушает правильный порт (PORT env var)

3. **Out of memory:**
   ```
   Error: OutOfMemoryError
   ```
   **Решение:** Увеличьте `JAVA_OPTS` или перейдите на платный план

4. **API errors:**
   ```
   Error: Failed to fetch jobs
   ```
   **Решение:** Проверьте доступность hirehi.ru API, добавьте retry логику

### Логи и отладка

1. **Просмотр логов:**
   ```bash
   # В Render Dashboard
   Logs → View Logs
   ```

2. **Локальная отладка:**
   ```bash
   # Запуск с Docker
   docker build -t hirehi-kotlin .
   docker run -p 10000:10000 hirehi-kotlin
   ```

## 📈 Масштабирование

### Free Tier ограничения

- **750 часов/месяц** бесплатного времени
- **Sleep после 15 минут** бездействия
- **512MB RAM** максимум
- **Cold start** при пробуждении

### Переход на платный план

1. **Starter Plan ($7/месяц):**
   - Всегда активен
   - 512MB RAM
   - Custom domains

2. **Standard Plan ($25/месяц):**
   - 1GB RAM
   - Better performance
   - Priority support

## 🔒 Безопасность

### Рекомендации

1. **Environment Variables:**
   - Не храните секреты в коде
   - Используйте Render Environment Variables

2. **API Security:**
   - Rate limiting (если нужно)
   - CORS настройки
   - Input validation

3. **Monitoring:**
   - Логирование ошибок
   - Health checks
   - Performance monitoring

## 📞 Поддержка

### Полезные ссылки

- [Render Documentation](https://render.com/docs)
- [Ktor Documentation](https://ktor.io/docs/)
- [Kotlin Documentation](https://kotlinlang.org/docs/)

### Контакты

- **Render Support**: [help.render.com](https://help.render.com)
- **GitHub Issues**: Создайте issue в репозитории проекта

---

## ✅ Чек-лист развертывания

- [ ] Репозиторий подготовлен и закоммичен
- [ ] Dockerfile создан и протестирован
- [ ] render.yaml настроен
- [ ] Render сервис создан
- [ ] Переменные окружения установлены
- [ ] Первое развертывание успешно
- [ ] Все эндпоинты работают
- [ ] Мониторинг настроен
- [ ] Документация обновлена

**🎉 Поздравляем! Ваше приложение HireHi успешно развернуто на Render.com!**