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
FROM eclipse-temurin:17-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR файл
COPY --from=build /app/build/libs/*.jar app.jar

# Открываем порт (Render автоматически установит PORT)
EXPOSE 10000

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]