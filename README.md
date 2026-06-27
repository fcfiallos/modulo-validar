# Módulo de Identidad 
## 1. Misión del Módulo

Establecer la **"Raíz de Confianza" (Root of Trust)**. En informática forense, ninguna evidencia (firma o certificado) tiene validez si no se puede vincular de forma irrefutable con una identidad civil verificada. Este módulo asegura que solo autores reales y validados por el Estado puedan entrar al sistema.

## 2. Arquitectura de Software

* **Patrón:** Arquitectura Hexagonal (Puertos y Adaptadores).
* **Estilo:** Microservicios Cloud-Native.
* **Framework:** Quarkus 3.35+ (Optimizado para alto rendimiento y bajo consumo).
* **Principios:** Cumplimiento de los **12-Factor App** (especialmente Factor III: Configuración y Factor IV: Backing ...
## 3. El Flujo de Registro Forense (Los 4 Pilares)

1. **Validación Externa (Integridad):** Consumo de una API externa (Python/Azure) que emula el Registro Civil. El sistema no "cree" en lo que el usuario escribe, lo "verifica".
2. **Seguridad Criptográfica (Confidencialidad):** Uso de BCrypt con factor de costo 12. Las contraseñas no se guardan; se genera un hash irreversible que protege la identidad incluso ante una filtración de la base de datos.
3. **Persistencia Robusta:** Uso de PostgreSQL con identidades basadas en UUID para evitar ataques de enumeración de usuarios.
4. **Optimización Reactiva:** Implementación de la estrategia `@Blocking` para manejar operaciones de red y base de datos sin congelar el núcleo del sistema (Event Loop).

## 4. Componentes Clave para Recordar

| Componente | Tecnología | Función Forense |
| :--- | :--- | :--- |
| **IdentityClient** | MicroProfile Rest Client | Interoperabilidad con el microservicio legal de identidad. |
| **AuthService** | CDI Bean (Application Layer) | Orquestador de la lógica de negocio y seguridad. |
| **UserEntity** | Hibernate Panache | Mapeo de la "Tabla Rosa" (Usuarios) con integridad de datos. |
| **BCrypt** | JBCrypt | Garantiza el No-Repudio del acceso mediante hashing fuerte. |

---
# Microservicio de Identidad
## 1. Estructura: Arquitectura Hexagonal
Esta organización garantiza la independencia de la lógica de negocio frente a la infraestructura (Base de Datos y APIs externas).
```
module-a-identity/
├── src/main/java/com/tesis/identity/
│   ├── application/
│   │   └── AuthService.java            <-- Lógica de Registro y Login (Casos de Uso)
│   ├── domain/
│   │   └── models/
│   │       └── User.java               <-- Modelo de dominio (Java 25 Record)
│   └── infrastructure/
│       ├── client/
│       │   └── IdentityClient.java     <-- Cliente para la API de Python (Azure)
│       ├── persistence/
│       │   └── UserEntity.java         <-- Mapeo de Tabla "usuarios" (PostgreSQL)
│       └── rest/
│           └── UserResource.java       <-- Endpoint expuesto (API Controller)
├── src/main/resources/
│   └── application.properties          <-- Configuración (Factor III - 12 Factor App)
└── build.gradle.kts                    <-- Gestión de dependencias (Lombok, Quarkus, BCrypt)
```

## 2. Protocolo de Registro 

Este es el verbo que el Frontend (o Postman) consume para crear un nuevo autor en el sistema.

* **Método:** POST
* **URL:** `http://localhost:8080/usuarios/registro`
* **Content-Type:** `application/json`

**Cuerpo de la Petición (JSON Payload):**

```json
{
  "cedula": "9900000001",
  "nombres": "ANA",
  "apellidos": "PEREZ",
  "correo": "ana@ejemplo.com",
  "nombreArtistico": "AnaVanguardia",
  "passwordHash": "PasswordSeguro123"
}
```
**Nota de Seguridad:** El campo `passwordHash` en este nivel lleva la contraseña en texto plano. El sistema la procesa con **BCrypt (Costo 12)** antes de que toque la base de datos.

## 3. Protocolo de Validación 

Este es el recurso externo que asegura la integridad de la identidad mediante la conexión con el simulador del Registro Civil.

* **Método:** POST
* **URL:** `https://azurewebsites.net`
* **Content-Type:** `application/json`

**Cuerpo de la Petición (JSON-B):**
```json
{
  "cedula": "9900000001",
  "name": "ANA",
  "surname": "PEREZ"
}
```

**Respuesta del Recurso:**

* **Éxito:** `1` (String) → El proceso de registro continúa.
* **Fallo:** `0` (String) → Se lanza una excepción y el registro se cancela.



