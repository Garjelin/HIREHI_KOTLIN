# Clean Architecture - HireHi Kotlin Project

## Структура проекта

```
src/main/kotlin/com/hirehi/
├── Main.kt                           # Точка входа приложения
├── application/                      # Application Layer
│   └── HireHiApplication.kt         # Основная логика приложения
├── data/                            # Data Layer
│   ├── remote/
│   │   └── HireHiScraper.kt        # Реализация скрапинга данных
│   └── repository/
│       └── JobRepositoryImpl.kt     # Реализация репозитория
├── domain/                          # Domain Layer
│   ├── model/
│   │   └── Job.kt                  # Модели данных
│   ├── repository/
│   │   ├── JobRepository.kt        # Интерфейс репозитория
│   │   └── JobScraper.kt           # Интерфейс скрапера
│   └── usecase/
│       └── GetJobsUseCase.kt       # Use Case для получения вакансий
└── presentation/                    # Presentation Layer
    ├── service/
    │   └── JobService.kt           # Сервис для управления данными
    ├── view/
    │   └── JobView.kt              # View для генерации HTML
    ├── viewmodel/
    │   └── JobViewModel.kt         # ViewModel (пока не используется)
    └── webserver/
        └── WebServer.kt            # Веб-сервер Ktor
```

## Принципы Clean Architecture

### 1. **Domain Layer** (Внутренний слой)
- **Модели**: `Job`, `JobSearchParams`, `JobResponse`
- **Интерфейсы**: `JobRepository`, `JobScraper`
- **Use Cases**: `GetJobsUseCase`
- **Независим** от внешних фреймворков и библиотек

### 2. **Data Layer** (Внешний слой)
- **HireHiScraper**: Реализует `JobScraper` для получения данных с hirehi.ru
- **JobRepositoryImpl**: Реализует `JobRepository` для управления данными
- **Зависит** от Domain Layer через интерфейсы

### 3. **Application Layer**
- **HireHiApplication**: Основная логика приложения, координация между слоями
- **Управление жизненным циклом**: Запуск и остановка компонентов

### 4. **Presentation Layer**
- **JobService**: Сервис для управления данными и файлами
- **JobView**: Генерация HTML страниц
- **JobViewModel**: ViewModel для будущего использования (MVVM)
- **WebServer**: Веб-сервер Ktor для обслуживания HTML страниц
- **Main.kt**: Точка входа, только запуск приложения

## Поток данных

```
Main.kt
    ↓
HireHiApplication
    ↓
JobService
    ↓
GetJobsUseCase
    ↓
JobRepositoryImpl
    ↓
HireHiScraper
    ↓
hirehi.ru API
```

## Преимущества рефакторинга

1. **Разделение ответственности**: Каждый слой имеет четкую роль
2. **Тестируемость**: Легко мокать интерфейсы для тестов
3. **Расширяемость**: Легко добавить новые источники данных
4. **Независимость**: Domain слой не зависит от внешних библиотек
5. **Чистота кода**: Удалены неиспользуемые файлы и функции

## Удаленные файлы

### Из корня проекта:
- `hirehi_all_jobs.json` - устаревший JSON файл
- `hirehi_filtered_jobs.json` - устаревший JSON файл  
- `hirehi_jobs.json` - устаревший JSON файл
- `jobs_display.html` - устаревший HTML файл
- `last_update.txt` - устаревший файл

### Из кода:
- `HireHiScraperStandalone.kt` - заменен на Clean Architecture
- `JobLocalDataSource.kt` - не использовался
- `MockScraper.kt` - не использовался
- `JobView.kt` (старый) - заменен на новый в presentation/view/
- `SimpleJobView.kt` - не использовался
- `JobController.kt` - не использовался
- `RefreshJobsUseCase.kt` - не использовался

## Результат

✅ **16 отфильтрованных вакансий** получаются корректно  
✅ **Веб-сервер** работает на порту 10000  
✅ **HTML страница** генерируется с правильными ссылками  
✅ **Clean Architecture** полностью соблюдена  
✅ **Код** очищен от неиспользуемых файлов  
✅ **Main.kt** упрощен - только запуск приложения  
✅ **View и ViewModel** слой добавлен  
✅ **Корень проекта** очищен от устаревших файлов  
✅ **Приложение** готово для развертывания на Render.com
