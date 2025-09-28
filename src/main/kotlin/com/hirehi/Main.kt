package com.hirehi

import com.hirehi.application.HireHiApplication
import com.hirehi.presentation.webserver.WebServer

fun main() {
    // Запускаем приложение для получения данных
    val application = HireHiApplication()
    application.run()
    
    // Запускаем веб-сервер
    val webServer = WebServer()
    webServer.start()
}