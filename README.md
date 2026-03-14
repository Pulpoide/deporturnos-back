# DeporTurnos Backend

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-6DB33F?logo=springboot&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white) ![k6](https://img.shields.io/badge/k6-Testing-7D64FF?logo=k6&logoColor=white)

Welcome to the backend repository for **DeporTurnos**, a comprehensive platform for the integral management of sports centers.
Built with Spring Boot, this application provides a robust solution for managing users, courts, turns, and reservations.

## Features

- **Court Management:** Manage reservations for Football and Padel courts.
- **Authentication & Authorization:** Secure endpoints using JWT (JSON Web Tokens).
- **OpenAPI Documentation:** Automatic API documentation with Springdoc OpenAPI.
- **Email Notifications:** Support for email notifications (e.g., reservation confirmations).
- **Optimized Performance:** Capable of handling high concurrency with asynchronous processing.

## Tech Stack

- **Spring Boot:** Java application framework.
- **Spring Data JPA:** Database persistence.
- **Spring Security & JWT:** Authentication and authorization.
- **Spring Mail:** Email notification services.
- **Thymeleaf:** Template engine (for email templates).
- **PostgreSQL:** Relational database.
- **Springdoc OpenAPI:** API documentation.
- **Docker & k6:** Containerized load and performance testing.

## Project Structure

~~~bash
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
├── k6/                # Load Testing Scripts
├── docker-compose.k6.yml
├── build.gradle
└── settings.gradle
~~~

## Installation and Usage

1. **Clone the repository:**
   ~~~bash
   git clone [https://github.com/Pulpoide/deporturnos-back](https://github.com/Pulpoide/deporturnos-back)
   cd deporturnos
   ~~~

2. **Run the application:**
   ~~~bash
   ./gradlew bootRun
   ~~~

3. **Access API Documentation:**
   Once the server is running, you can explore the endpoints via Swagger UI:
   - URL: `http://localhost:8080/swagger-ui.html`

### Environment Variables

You need to configure the following environment variables (e.g., in `application.properties` or your IDE run configuration):

* `SPRING_DATASOURCE_URL`: PostgreSQL database URL.
* `SPRING_DATASOURCE_USERNAME`: Database username.
* `SPRING_DATASOURCE_PASSWORD`: Database password.
* `JWT_SECRET_KEY`: Secret key for generating and validating JWT tokens.
* `APP_PASSWORD`: App password for the email account used to send notifications.

## Performance Testing (k6)

This project includes a professional load testing suite using [k6](https://k6.io/), containerized with Docker for easy execution. These tests evaluate the system's resilience and stability under high traffic.

### Prerequisites
* Docker & Docker Compose installed.
* The backend application must be running locally (default: `http://localhost:8080`).

### Running Tests

We provide two main test scenarios:

**1. Stress Test**
Simulates a gradual increase in load to test the system's endurance.
* **Goal:** Verify stability under sustained load (20 VUs for 4 minutes).
* **Command:**
    ~~~bash
    docker compose -f docker-compose.k6.yml run --rm k6-stress
    ~~~

**2. Spike Test**
Simulates a sudden, extreme burst of traffic.
* **Goal:** Verify system recovery and stability during peaks (0 to 50 VUs in 20s).
* **Command:**
    ~~~bash
    docker compose -f docker-compose.k6.yml run --rm k6-spike
    ~~~

The JSON reports will be generated in `k6/reports/{test_type}/json/results.json`.

---

## Author

**Joaquin D. Olivero** Software Engineer | AI Engineer

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/JoaquinOlivero)

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Pulpoide)
