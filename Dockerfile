# Используем официальный образ Gradle с JDK 17
FROM gradle:8.5-jdk17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта
COPY . .

# Собираем приложение
RUN ./gradlew build --no-daemon

# Создаем финальный образ с JRE
FROM openjdk:17-jre-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл
COPY --from=build /app/build/libs/*.jar app.jar

# Открываем порт (Render.com ожидает порт 10000)
EXPOSE 10000

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
