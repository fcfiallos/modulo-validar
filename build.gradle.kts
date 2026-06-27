plugins {
    id("java")
    id("io.quarkus") version "3.35.2"
    id("io.freefair.lombok") version "9.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Usamos la plataforma oficial vinculada al plugin automáticamente
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:3.35.2"))

    // 1. CORE DE QUARKUS Y API REST
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jsonb")
    // 2. PERSISTENCIA
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-postgresql")

    // 3. CLIENTE HTTP
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-rest-client-jsonb")

    // 4. SEGURIDAD FORENSE
    implementation("org.mindrot:jbcrypt:0.4")
//    implementation("io.quarkus:quarkus-rest:3.37.0")
//    implementation("io.quarkus:quarkus-hibernate-orm:3.37.0")
//    implementation("io.quarkus:quarkus-rest-jackson:3.37.0")

    // 5. TESTING
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "com.tesis.identity"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}