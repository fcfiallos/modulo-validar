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
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")

    // 4. SEGURIDAD FORENSE
    implementation("org.mindrot:jbcrypt:0.4")

    // Bean Validation (Jakarta Validation / Hibernate Validator)
    implementation("io.quarkus:quarkus-hibernate-validator")

    //5. key de azure
    implementation("com.azure:azure-security-keyvault-keys:4.10.6")
    implementation("com.azure:azure-identity:1.13.0")

    // 6. Autenticación basada en tokens (JWT) — solo verificación; la firma
    //    se hace a mano contra Azure Key Vault, ver JwtTokenIssuer.
    implementation("io.quarkus:quarkus-smallrye-jwt")

}

group = "com.tesis.identity"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
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