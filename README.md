# Retail Order System with PostgreSQL

This project is a Spring Boot application that provides a RESTful API for managing retail orders, using PostgreSQL as the database.

## Features

* Create, retrieve, update, and delete orders.
* Uses PostgreSQL for data persistence.
* Spring Boot Actuator for monitoring and management.
* Comprehensive test suite including unit and integration tests.
* Docker support for easy setup and deployment.
* OpenAPI 3 (Swagger) documentation for API endpoints.

## Technologies Used

* Java 17
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Flyway
* Maven
* JUnit
* Testcontainers (for integration tests)
* Docker

## Getting Started

### Prerequisites

* Java 17 or higher
* Maven 3.6.0 or higher
* Docker 
* PostgreSQL (if you want to run the application outside Docker)

### Installation

1.  **Clone the repository:**

```bash
    git clone https://github.com/j2eeexpert2015/testcontainers-with-spring-boot-postgres.git
    cd testcontainers-with-spring-boot-postgres
```

2.  **Build the application:**

```bash
    mvn clean install
```

### Running the Application

#### With Docker

1.  **Ensure PostgreSQL is running and accessible.**
3.  **Run the Spring Boot application:**

```bash
docker-compose build
docker-compose up
mvn spring-boot:run
```
####  Access Swagger UI

```bash
http://localhost:8080/swagger-ui/index.html
```

#### With TestContainer

1.  Ensure Docker is installed and running 
2.  Build and run the Docker Compose setup

```bash
mvn spring-boot:run
```
The Testcontainer will start the PostgreSQL database. The Spring Boot application will also attempt to connect to it (you may need to build the Spring Boot application separately or include it in your Docker setup).

### Running Tests

```bash
mvn test
