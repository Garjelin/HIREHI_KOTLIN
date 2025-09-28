# Используем официальный образ Gradle с JDK 17
FROM gradle:8.5-jdk17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем все файлы проекта
COPY . .

# Собираем приложение
RUN ./gradlew shadowJar --no-daemon

# Создаем финальный образ
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный shadow JAR файл
COPY --from=build /app/build/libs/*-all.jar app.jar

# Открываем порт (Render автоматически установит PORT)
EXPOSE 10000

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]