# 🚀 Быстрое развертывание на Render.com

## ⚡ Быстрый старт (5 минут)

### 1. Подготовка репозитория
```bash
git add .
git commit -m "Ready for Render deployment"
git push origin main
```

### 2. Создание сервиса на Render
1. Откройте [dashboard.render.com](https://dashboard.render.com)
2. Нажмите "New +" → "Web Service"
3. Подключите GitHub репозиторий `HIREHI_KOTLIN`
4. Настройки:
   - **Name**: `hirehi-kotlin`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `./Dockerfile`
   - **Plan**: `Free`
5. Нажмите "Create Web Service"

### 3. Ожидание развертывания
- ⏱️ **Время сборки**: 5-10 минут
- 📊 **Мониторинг**: Render Dashboard → Logs
- ✅ **Готово**: URL вида `https://hirehi-kotlin.onrender.com`

## 🔧 Проверка работоспособности

### Основные эндпоинты:
- **Главная**: `https://your-app.onrender.com/`
- **API статус**: `https://your-app.onrender.com/api/status`
- **JSON данные**: `https://your-app.onrender.com/api/jobs`
- **Обновление**: `POST https://your-app.onrender.com/api/refresh`

### Ожидаемый результат:
- ✅ 105 общих вакансий
- ✅ 16 отфильтрованных (Kotlin/Android)
- ✅ 0 в архиве
- ✅ Кнопка "Обновить данные" работает

## 🐛 Если что-то не работает

### Проблема: Build failed
**Решение**: Проверьте логи сборки в Render Dashboard

### Проблема: Service not responding
**Решение**: Убедитесь что PORT=10000 в переменных окружения

### Проблема: Out of memory
**Решение**: Добавьте `JAVA_OPTS=-Xmx512m -Xms256m`

## 📈 После успешного развертывания

1. **Добавьте custom domain** (опционально)
2. **Настройте мониторинг** в Render Dashboard
3. **Перейдите на платный план** для production использования

---

**🎉 Готово! Ваше приложение HireHi работает в облаке!**
