# 🗄️ Настройка Supabase для архива вакансий

## Шаг 1: Создание проекта в Supabase

1. Перейдите на [supabase.com](https://supabase.com)
2. Нажмите "Start your project" или "New project"
3. Войдите в аккаунт (или создайте новый)
4. Создайте новый проект:
   - **Name**: `hirehi-archive` (или любое другое имя)
   - **Database Password**: создайте надежный пароль (сохраните его!)
   - **Region**: выберите ближайший к вам регион
   - **Pricing Plan**: Free (бесплатный план)

## Шаг 2: Получение Connection String

1. После создания проекта перейдите в **Settings** → **Database**
2. Найдите секцию **Connection string**
3. Выберите **URI** и скопируйте строку подключения
4. Она выглядит примерно так:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres
   ```

## Шаг 3: Настройка переменных окружения в Render

1. Перейдите в ваш Web Service в Render Dashboard
2. Откройте вкладку **Environment**
3. Добавьте новую переменную:
   - **Key**: `DATABASE_URL`
   - **Value**: вставьте скопированную строку подключения из Supabase

## Шаг 4: Проверка подключения

После деплоя ваше приложение автоматически:
- Подключится к Supabase
- Создаст таблицу `archived_jobs` для хранения архивированных вакансий
- Начнет работать с архивом

## Доступные API endpoints

После настройки будут доступны следующие endpoints:

### Архивирование вакансии
```http
POST /api/archive
Content-Type: application/json

{
  "jobId": "job-id-here",
  "reason": "Причина архивирования (необязательно)"
}
```

### Получение архивированных вакансий
```http
GET /api/archive?limit=100&offset=0
```

### Статистика архива
```http
GET /api/archive/statistics
```

### Удаление из архива
```http
DELETE /api/archive/{jobId}
```

## Тестирование

1. Откройте `https://your-app.onrender.com/archive` для тестирования архивирования
2. Или используйте API endpoints напрямую

## Лимиты бесплатного плана Supabase

- **Storage**: 500 MB
- **Bandwidth**: 2 GB
- **Database size**: 500 MB
- **API requests**: 50,000 в месяц
- **Concurrent connections**: 60

Этого достаточно для архива тысяч вакансий!

## Troubleshooting

### Ошибка подключения к БД
- Проверьте правильность `DATABASE_URL`
- Убедитесь, что пароль в URL правильный
- Проверьте, что проект Supabase активен

### Таблица не создается
- Приложение автоматически создает таблицы при первом запуске
- Проверьте логи в Render Dashboard

### Ошибки в логах
- Откройте Render Dashboard → ваш сервис → Logs
- Ищите сообщения об ошибках подключения к БД
