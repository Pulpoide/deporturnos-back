# DeporTurnos Backend

Este es el repositorio del backend para la aplicación **DeporTurnos**, una plataforma para la gestión intergal de canchas deportivas.
Esta aplicación está construida con Spring Boot y proporciona una solución completa para la administración de reservas, gestión de usuarios y más.

## Características

- **Gestión de canchas:** Permite gestionar reservas de canchas de fútbol y pádel.
- **Autenticación y autorización:** Seguridad con JWT para proteger los endpoints.
- **Documentación OpenAPI:** Documentación automática de la API con Springdoc OpenAPI.
- **Envío de correos electónicos:** Soporte para notificaciones por correo electrónico.

## Tecnologías Utilizadas

- **Spring Boot:** Framework para aplicaciones Java.
- **Spring Data JPA:** Para la persistencia de datos en base de datos.
- **Thymeleaf:** Motor de plantillas para la generación de vistas.
- **Spring Security:** Para la autenticación y autorización.
- **Spring Mail:** Para el envío de correos electrónicos.
- **JWT (JSON Web Tokens):** Para la seguridad y autenticación.
- **PostgreSQL:** Base de datos relacional utilizada.
- **Springdoc OpenAPI:** Documentación de la API.

## Estructura del Proyecto

```bash 
deporturnos
├── src
│   ├── main 
│   │   ├── java
│   │   │   ├── configuration
│   │   │   ├── controller
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── model
│   │   │   ├── repository
│   │   │   ├── security
│   │   │   ├── service
│   │   │   └── DeporturnosApplication.java
│   │   ├── resources  
│   │   │   └── application.properties 
├── build.gradle
└── settings.gradle
```

## Instalación y Uso

```bash
git clone https://github.com/Pulpoide/deporturnos-back
cd deporturnos
./gradlew bootRun
```
## Autor
[**Joaquin D. Olivero**](https://github.com/Pulpoide)
[LinkedIn](https://www.linkedin.com/in/JoaquinOlivero)
