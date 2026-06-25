// Definición de variables centralizadas para fácil actualización de versiones
val quarkusVersion = "3.36.3" // Última versión estable de Quarkus
val lombokVersion = "8.12.1"   // Última versión del plugin de Lombok para Gradle
val jbcryptVersion = "0.4"

plugins {
    java
    // Core de Quarkus (Utiliza la variable declarada arriba)
    id("io.quarkus") version "3.36.3"

    // Plugin de Lombok en su última versión configurado directamente en el bloque plugins
    id("io.freefair.lombok") version "8.12.1"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // 1. CORE DE QUARKUS Y API REST (Justo lo necesario para el Login/Registro)
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")

    // 2. PERSISTENCIA (Hibernate con Panache es la forma más ligera y eficiente en Quarkus)
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")

    // 3. CLIENTE HTTP (Para llamar a tu API de Python del Registro Civil)
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")

    // 4. SEGURIDAD FORENSE (Lo que pidió tu docente: BCrypt)
    implementation("org.mindrot:jbcrypt:$jbcryptVersion")

    // 5. TESTING (XP - Para validar que el registro funciona antes de entregar)
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "com.tesis.identity"
version = "1.0.0-SNAPSHOT"

// Configuración obligatoria para compilar y usar Java 25
java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

