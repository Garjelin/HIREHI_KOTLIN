plugins {
    kotlin("jvm") version "2.2.0"
    application
    kotlin("plugin.serialization") version "2.2.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.hirehi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-html-builder:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    
    // HTML generation
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")
    
            // Web scraping
            implementation("org.jsoup:jsoup:1.17.2")
            
            // JSON parsing
            implementation("org.json:json:20231013")
    
    // HTTP client
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
}

application {
    mainClass.set("com.hirehi.MainKt")
}

tasks.register<JavaExec>("runScraper") {
    group = "application"
    description = "Run the standalone scraper"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.hirehi.HireHiScraperStandaloneKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}