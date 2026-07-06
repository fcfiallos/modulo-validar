# 🛡️ Módulo A: Búnker de Identidad y Custodia Segura

Este documento detalla la arquitectura de blindaje implementada para garantizar la **Raíz de Confianza (Root of Trust)** y la **Custodia de Credenciales** dentro del sistema de certificación forense.

## 1. Justificación de la Arquitectura de Seguridad
En un peritaje informático, la validez de la evidencia depende de la inalterabilidad del vínculo entre el **Autor** y la **Obra**. Este módulo ha sido diseñado para blindar la identidad del autor mediante cuatro capas de protección activa.

### ¿Por qué este trabajo justifica a dos investigadoras?
Mientras el **Módulo B** se centra en el análisis forense de la imagen (el objeto), el **Módulo A** se centra en la seguridad de la infraestructura y el sujeto (la identidad), aplicando estándares bancarios y gubernamentales de custodia.

---

## 2. Capas de Blindaje Implementadas

### Capa 1: Validación Legal (Integridad de Identidad)
*   **Implementación:** Integración con microservicio externo (Python/Azure) que emula al Registro Civil de Ecuador.
*   **Justificación:** El sistema no admite registros basados en "confianza ciega". Cada identidad se contrasta contra el registro oficial para evitar la creación de autores ficticios.

### Capa 2: Atributos Forenses X.509 (Identidad Autónoma)
*   **Implementación:** Uso del **OID 2.5.4.5 (SerialNumber)** para incrustar la cédula y el campo **OU** para el apodo artístico.
*   **Justificación:** La evidencia debe ser autónoma. Al incluir estos datos en el certificado firmado por una CA, la prueba de autoría viaja dentro del archivo y no depende de una base de datos externa para identificar al titular.

### Capa 3: Cifrado de Sobre (Envelope Encryption) - **EL CORAZÓN DEL BLINDAJE**
Debido a que el algoritmo **RSA-4096** tiene límites físicos de tamaño (no puede cifrar archivos .p12 directamente), se implementó una arquitectura híbrida:
1.  **DEK (Data Encryption Key):** Se genera una llave **AES-256-GCM** local en RAM para cifrar los nombres, la cédula y el archivo `.p12`.
2.  **KEK (Key Encryption Key):** Esa llave AES es "envuelta" (cifrada) por la llave maestra **RSA-4096** que reside en **Azure Key Vault**.
3.  **Resultado:** Los datos en PostgreSQL son totalmente ilegibles sin la llave maestra de la nube.

### Capa 4: Gestión de Secretos Cloud-Native (Azure Key Vault / HSM)
*   **Implementación:** El "Sello del Sistema" y la "Llave Maestra de Custodia" no existen en el servidor.
*   **Justificación:** Aislamiento total de secretos. Si el servidor de aplicaciones es comprometido, el atacante no encontrará ninguna llave privada; estas residen en un módulo de seguridad de hardware (HSM) en la nube bajo el estándar **FIPS 140-2**.

### Capa 5: Criptografía Serverless (Escalabilidad de Recursos)
*   **Implementación:** El acto de firma RSA-4096 se delegó a una **Azure Function**.
*   **Justificación:** Las operaciones criptográficas de alta densidad consumen mucha CPU. Al usar Serverless, garantizamos que el sistema principal sea ligero y que la potencia de cómputo escale elásticamente según la demanda.

---

## 3. Resumen Técnico para Defensa

| Amenaza | Medida de Mitigación | Estándar Aplicado |
| :--- | :--- | :--- |
| **Suplantación de Identidad** | Doble validación (Legal + Criptográfica) | X.509 SerialNumber OID |
| **Robo de Base de Datos** | Cifrado en Reposo (Encryption at Rest) | AES-256-GCM (Authenticated) |
| **Ataque de Diccionario** | Hashing Computacionalmente Costoso | BCrypt (Work Factor 12) |
| **Fuga de Secretos** | Aislamiento de llaves institucionales | Azure Key Vault (HSM) |
| **Fuga de RAM (Memory Dumps)** | Purga de material sensible | `char[]` para passwords |

---

## 4. Conceptos Clave para el Tribunal
*   **No Repudio:** El autor no puede negar haber firmado la obra porque solo él posee la clave de su `.p12` custodiado.
*   **Zero-Knowledge Storage:** La plataforma guarda las firmas pero no tiene capacidad de usarlas sin la autorización (contraseña) del usuario y la llave del Vault.
*   **Confidencialidad Persistente:** Cumplimiento técnico con la **LOPDP (Ecuador)** mediante la ofuscación de Datos Personales (PII).

---
*Este blindaje representa el compromiso del proyecto con la integridad forense y la soberanía digital del artista.*

---
Para tus diagramas y tu documento de tesis, es vital que definas con precisión técnica las **cuatro arquitecturas** que convergen en tu proyecto. Cada una cumple una función distinta dentro de la **Informática Forense**.

Aquí tienes la información detallada que necesitas para sustentar y diagramar tu trabajo:

---

# 🏗️ Guía de Arquitecturas para Diagramación Técnica

## 1. Arquitectura Hexagonal (Ports & Adapters)
Es la arquitectura que organiza el **interior** de tu microservicio de Java.

*   **Definición:** Patrón de diseño que desacopla el núcleo lógico (Dominio) de las tecnologías externas (Base de datos, APIs, Nube).
*   **Implementación en tu tesis:**
    *   **Núcleo (Domain):** Tus clases `User` y `AuthService`. Contienen las reglas de negocio (ej: "No registrar si no acepta términos").
    *   **Puertos (Ports):** Las interfaces que definen qué necesitamos (ej: `IdentityClient`, `UserRepository`).
    *   **Adaptadores (Adapters):** Las clases técnicas (ej: `UserEntity` para Postgres, `IdentityHttpAdapter` para Azure, `VaultEncryptionService` para el Vault).
*   **Justificación Forense:** Facilita la **Auditabilidad**. Un perito puede probar la lógica de registro sin necesidad de estar conectado a internet o a la base de datos, garantizando que el proceso de decisión sea transparente y repetible.

## 2. Arquitectura de Microservicios (Distribuida)
Es la forma en que tus componentes se comunican a nivel **global**.

*   **Definición:** Estilo arquitectónico que divide una aplicación en servicios pequeños, independientes y comunicados a través de protocolos ligeros (HTTP/REST).
*   **Implementación en tu tesis:**
    *   **Servicio de Identidad (Tuyo):** Desarrollado en Quarkus (Java).
    *   **Servicio de Validación Legal:** API de Python (Azure) para el Registro Civil.
    *   **Servicio Criptográfico (Tuyo):** Azure Functions (Python) para RSA-4096.
*   **Justificación Forense:** Permite la **Segregación de Funciones**. La responsabilidad de "quién es el usuario" está físicamente separada de "quién firma la obra", lo cual es un estándar en cadenas de custodia profesionales.

## 3. Arquitectura Serverless (FaaS - Function as a Service)
Es la tecnología específica usada para las operaciones criptográficas pesadas.

*   **Definición:** Modelo de computación donde el proveedor de la nube (Azure) ejecuta el código solo cuando es necesario, asignando recursos de forma elástica.
*   **Implementación en tu tesis:** Las Azure Functions para generar y validar el archivo `.p12`.
*   **Justificación Forense:** **Optimización de Recursos y Aislamiento**. Las operaciones de alto costo computacional (RSA-4096/SHA-512) no saturan el servidor principal. Además, el material criptográfico vive en un contenedor efímero que se destruye apenas termina la función, reduciendo la superficie de ataque.

## 4. Arquitectura de Cifrado Híbrido (Envelope Encryption)
Es el diseño de seguridad de tu "Búnker de Datos".

*   **Definición:** Estrategia que combina la velocidad del cifrado simétrico (AES) con la seguridad del cifrado asimétrico (RSA).
*   **Implementación en tu tesis:**
    *   **Llave de Datos (DEK):** Una llave AES-256 generada en RAM para cifrar los nombres y el `.p12`.
    *   **Llave Maestra (KEK):** Una llave RSA-4096 en **Azure Key Vault** que cifra la llave AES.
*   **Justificación Forense:** **Privacidad y Custodia Blindada**. Cumple con la LOPDP de Ecuador. Garantiza que ni el administrador de la nube ni el de la base de datos puedan leer la información personal del artista, asegurando que la evidencia solo pueda ser "iluminada" por el microservicio autorizado.

---

### 💡 Sugerencias para tus diagramas:

1.  **Diagrama Hexagonal:** Dibuja un hexágono central con el `AuthService`. Pon flechas que salgan hacia afuera a "Adaptadores" representados por logos de PostgreSQL, Azure Key Vault y el rayo de Azure Functions.
2.  **Diagrama de Secuencia (El flujo de registro):**
    *   Usuario ➔ Quarkus (Datos).
    *   Quarkus ➔ Python Identity API (¿Existe?).
    *   Quarkus ➔ Azure Key Vault (Pide KEK).
    *   Quarkus ➔ PostgreSQL (Guarda datos cifrados).
3.  **Diagrama de Nube:** Dibuja el límite de "Azure Cloud" y dentro coloca los servicios como piezas interconectadas, resaltando que la **Llave Maestra** está dentro de un **HSM (Hardware Security Module)**.



