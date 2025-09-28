# Clean Architecture - HireHi Kotlin Project

## Структура проекта

```
src/main/kotlin/com/hirehi/
├── Main.kt                           # Точка входа приложения
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

### 3. **Presentation Layer** (Main.kt)
- **Точка входа**: `main()` функция
- **Dependency Injection**: Создает экземпляры и связывает зависимости
- **Веб-сервер**: Ktor для обслуживания HTML страниц

## Поток данных

```
Main.kt
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

- `HireHiScraperStandalone.kt` - заменен на Clean Architecture
- `JobLocalDataSource.kt` - не использовался
- `MockScraper.kt` - не использовался
- `JobView.kt` - не использовался
- `SimpleJobView.kt` - не использовался
- `JobController.kt` - не использовался
- `RefreshJobsUseCase.kt` - не использовался

## Результат

✅ **16 отфильтрованных вакансий** получаются корректно  
✅ **Веб-сервер** работает на порту 10000  
✅ **HTML страница** генерируется с правильными ссылками  
✅ **Clean Architecture** соблюдена  
✅ **Код** очищен от неиспользуемых файлов
