# QA Вакансии - HireHi Kotlin

Веб-приложение на Kotlin для автоматического сбора и отображения QA вакансий с сайта hirehi.ru с фильтрацией по ключевым словам Kotlin и Android.

## 🌟 Особенности

- **Чистая архитектура** - разделение на слои (data, domain, presentation)
- **Ktor веб-сервер** - современный асинхронный веб-фреймворк
- **Автоматический парсинг** - сбор вакансий с hirehi.ru
- **Кэширование** - локальное сохранение данных
- **REST API** - готовность к интеграции с Android приложением
- **Docker поддержка** - легкое развертывание на Render.com

## 🚀 Технологии

- **Kotlin** - основной язык программирования
- **Ktor** - веб-фреймворк
- **Ktor HTTP Client** - API запросы
- **Kotlinx.html** - генерация HTML
- **org.json** - парсинг JSON
- **Docker** - контейнеризация
- **Render.com** - облачное развертывание
- **Gradle** - система сборки

## 📋 Требования

- Java 17+
- Gradle 8.5+

## 🛠 Установка и запуск

### Локальный запуск

```bash
# Клонирование репозитория
git clone <repository-url>
cd HIREHI_KOTLIN

# Запуск приложения
./gradlew run
```

Приложение будет доступно по адресу: http://localhost:10000

### Сборка JAR

```bash
./gradlew build
java -jar build/libs/HIREHI_KOTLIN-1.0-SNAPSHOT.jar
```

## 🌐 Развертывание на Render.com

### Подготовка

1. Убедитесь, что в проекте есть `Dockerfile`
2. Настройте переменную окружения `PORT` (по умолчанию 10000)

### Развертывание

1. Зарегистрируйтесь на [Render.com](https://render.com/)
2. Подключите GitHub репозиторий
3. Создайте новый Web Service:
   - **Runtime**: Docker
   - **Build Command**: (оставить пустым)
   - **Start Command**: (оставить пустым)
   - **Environment**: Python 3 (не важно, используется Docker)
4. Выберите Free tier
5. Нажмите "Create Web Service"

### Автоматическое развертывание

При каждом push в main ветку будет происходить автоматический деплой.

## 📁 Структура проекта

```
src/main/kotlin/com/hirehi/
├── data/
│   ├── local/           # Локальное хранение данных
│   ├── remote/          # Парсинг внешних источников
│   └── repository/      # Реализация репозиториев
├── domain/
│   ├── model/           # Модели данных
│   ├── repository/      # Интерфейсы репозиториев
│   └── usecase/         # Бизнес-логика
└── presentation/
    ├── controller/      # Контроллеры (маршруты)
    └── view/           # Представления (HTML)
```

## 🔧 API Endpoints

- `GET /` - Главная страница с вакансиями
- `GET /api/jobs` - JSON список вакансий
- `POST /api/refresh` - Обновление данных
- `GET /api/status` - Статус приложения

## ⚙️ Настройка

### Изменение ключевых слов

В `JobSearchParams` можно изменить ключевые слова для фильтрации:

```kotlin
val params = JobSearchParams(
    keywords = listOf("Kotlin", "Android", "Java")
)
```

### Настройка параметров поиска

```kotlin
val params = JobSearchParams(
    category = "qa",
    format = "удалённо",
    levels = listOf("senior", "middle"),
    subcategory = "auto"
)
```

## 🔄 Обновление данных

Данные обновляются автоматически при:
- Первом запуске приложения
- Истечении кэша (1 час)
- Вызове API `/api/refresh`
- Нажатии кнопки "Обновить данные" на сайте

## 📊 Формат данных

```json
{
  "id": "unique-id",
  "title": "QA Engineer (auto)",
  "company": "Название компании",
  "salary": "от 200 000 ₽",
  "level": "senior",
  "format": "удалённо",
  "url": "https://hirehi.ru/qa/qa-testirovshchik-auto-1234",
  "description": "Описание вакансии",
  "publishedAt": "2024-01-15T10:30:00"
}
```

## 🚀 Планы развития

- [ ] Интеграция с Android приложением
- [ ] Расширенная фильтрация вакансий
- [ ] Уведомления о новых вакансиях
- [ ] Статистика и аналитика
- [ ] Поддержка других сайтов с вакансиями

## 📄 Лицензия

MIT License

## 🤝 Вклад в проект

1. Форкните репозиторий
2. Создайте ветку для новой функции
3. Внесите изменения
4. Создайте Pull Request

## 🌐 Развертывание на Render.com

### Быстрое развертывание
```bash
# 1. Подготовка
git add .
git commit -m "Ready for deployment"
git push origin main

# 2. Создание сервиса на Render
# - Откройте dashboard.render.com
# - New + → Web Service
# - Подключите GitHub репозиторий
# - Environment: Docker
# - Dockerfile Path: Dockerfile
```

### Подробная инструкция
См. [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) для полного руководства по развертыванию.

### Быстрый старт
См. [QUICK_DEPLOY.md](QUICK_DEPLOY.md) для 5-минутного развертывания.

## ⚠️ Важные замечания

- Приложение делает запросы к hirehi.ru с задержками
- Не злоупотребляйте частотой запросов
- Данные кэшируются локально для оптимизации
- Free tier на Render.com имеет ограничения (sleep после 15 мин, 750 часов/месяц)