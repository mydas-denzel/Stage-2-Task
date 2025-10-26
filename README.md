# Country Currency Exchange API 🌍

## Overview
This is a robust Spring Boot application that provides a RESTful API for querying country-specific data, including capital, region, population, currency, and an estimated GDP. It dynamically integrates with external APIs to fetch the latest geographical information and currency exchange rates, persisting this data using Spring Data JPA with an H2 database. The API is designed for reliability and provides endpoints for data retrieval, management, and real-time status updates.

## Features
- **Spring Boot 3.x**: Leverages the latest Spring Boot features for rapid, production-ready application development.
- **RESTful API Design**: Implements clean, intuitive REST endpoints for efficient data interaction.
- **External API Integration**: Seamlessly fetches country details from `restcountries.com` and currency exchange rates from `open.er-api.com`.
- **H2 Database**: Utilizes an embedded H2 database for efficient data storage and retrieval, configurable for file-based persistence.
- **Spring Data JPA**: Simplifies data access and persistence operations through powerful ORM capabilities.
- **Dynamic Data Refresh**: Provides an endpoint to refresh all country and currency exchange data from external sources.
- **Calculated Metrics**: Dynamically computes an estimated GDP for each country based on population and exchange rates.
- **Image Generation**: Generates a visual summary (PNG image) of key country statistics, including top countries by estimated GDP.
- **Lombok**: Reduces boilerplate code for Java POJOs (Plain Old Java Objects).
- **Maven Wrapper**: Ensures consistent build environments across different machines.

## Getting Started
To get this project up and running on your local machine, follow these steps.

### Prerequisites
Make sure you have the following installed:
*   ✅ **Java Development Kit (JDK) 21** or higher
*   ✅ **Apache Maven 3.6** or higher (or use the included Maven Wrapper)

### Installation
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/mydas-denzel/Stage-2-Task.git
    cd Stage-2-Task
    ```

2.  **Build the Project**:
    Use the Maven Wrapper to build the project, which will also download necessary dependencies.
    ```bash
    ./mvnw clean install
    ```

3.  **Run the Application**:
    Start the Spring Boot application using the Maven Wrapper.
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on `http://localhost:8080` by default.

### Environment Variables
The application uses properties from `src/main/resources/application.properties`. For production or flexible deployments, these can be overridden via environment variables.

| Variable Name             | Description                                          | Default Value                 | Example               |
| :------------------------ | :--------------------------------------------------- | :---------------------------- | :-------------------- |
| `SPRING_DATASOURCE_URL`   | JDBC URL for the H2 database.                        | `jdbc:h2:file:./data/countries` | `jdbc:h2:mem:testdb`  |
| `SPRING_DATASOURCE_USERNAME` | Username for the database connection.               | `sa`                          | `dbuser`              |
| `SPRING_DATASOURCE_PASSWORD` | Password for the database connection.               | (empty)                       | `dbpassword`          |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL generation strategy.                  | `update`                      | `none`                |

Example usage (on Linux/macOS):
```bash
export SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
export SPRING_DATASOURCE_USERNAME=myuser
export SPRING_DATASOURCE_PASSWORD=mypassword
./mvnw spring-boot:run
```
For Windows (cmd):
```cmd
set SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
set SPRING_DATASOURCE_USERNAME=myuser
set SPRING_DATASOURCE_PASSWORD=mypassword
mvnw spring-boot:run
```

## API Documentation
The API provides comprehensive endpoints to interact with country and currency data.

### Base URL
`http://localhost:8080`

### Endpoints

#### `POST /countries/refresh`
Refreshes the country and currency data by fetching the latest information from external APIs (`restcountries.com`, `open.er-api.com`) and updates the local database. This also triggers the generation of the summary image.

**Request**:
```
No request body required.
```

**Response**:
```json
{
  "message": "Countries refreshed successfully"
}
```

**Errors**:
- `503 Service Unavailable`: Occurs if external data sources are unreachable or return malformed data.
  ```json
  {
    "error": "External data source unavailable",
    "details": "Failed to fetch external API data: IOException - No country data received"
  }
  ```

#### `GET /countries`
Retrieves a list of countries, with optional filtering by region or currency, and sorting by estimated GDP.

**Request Parameters**:
- `region` (Optional, String): Filter countries by a specific region (e.g., `Africa`, `Europe`).
- `currency` (Optional, String): Filter countries by a specific currency code (e.g., `USD`, `EUR`).
- `sort` (Optional, String): Sorts the results. Currently supports `gdp_desc` for sorting by estimated GDP in descending order.

**Response**:
```json
[
  {
    "id": 1,
    "name": "United States",
    "capital": "Washington D.C.",
    "region": "Americas",
    "population": 331002651,
    "currencyCode": "USD",
    "exchangeRate": 1.0,
    "estimatedGdp": 331002651000.00,
    "flagUrl": "https://restcountries.com/data/usa.svg",
    "lastRefreshedAt": "2023-10-27T10:30:00"
  },
  {
    "id": 2,
    "name": "Canada",
    "capital": "Ottawa",
    "region": "Americas",
    "population": 38005238,
    "currencyCode": "CAD",
    "exchangeRate": 1.35,
    "estimatedGdp": 28151920000.00,
    "flagUrl": "https://restcountries.com/data/can.svg",
    "lastRefreshedAt": "2023-10-27T10:30:00"
  }
]
```

**Errors**:
- No specific error responses are defined beyond standard HTTP status codes for invalid requests.

#### `GET /countries/{name}`
Retrieves detailed information for a specific country by its name.

**Request Path Variable**:
- `name` (String, Required): The full name of the country (case-insensitive).

**Response**:
```json
{
  "id": 1,
  "name": "United States",
  "capital": "Washington D.C.",
  "region": "Americas",
  "population": 331002651,
  "currencyCode": "USD",
  "exchangeRate": 1.0,
  "estimatedGdp": 331002651000.00,
  "flagUrl": "https://restcountries.com/data/usa.svg",
  "lastRefreshedAt": "2023-10-27T10:30:00"
}
```

**Errors**:
- `404 Not Found`: If no country with the specified name is found.
  ```json
  {
    "error": "Country not found"
  }
  ```

#### `DELETE /countries/{name}`
Deletes a country from the database by its name.

**Request Path Variable**:
- `name` (String, Required): The full name of the country to delete (case-insensitive).

**Response**:
```json
{
  "message": "Deleted successfully"
}
```

**Errors**:
- No specific error responses are defined. If the country does not exist, the response will still be `200 OK` as no operation is needed.

#### `GET /countries/image`
Retrieves a PNG image summarizing key country statistics, including the top 5 countries by estimated GDP. This image is generated and cached after a successful `/countries/refresh` call.

**Request**:
```
No request body required.
```

**Response**:
- `200 OK`: Returns an `image/png` stream.

**Errors**:
- `404 Not Found`: If the summary image has not yet been generated (e.g., `/countries/refresh` hasn't been called or failed).
  ```json
  {
    "error": "Summary image not found"
  }
  ```

#### `GET /countries/status`
Provides status information about the country data in the database.

**Request**:
```
No request body required.
```

**Response**:
```json
{
  "total_countries": 193,
  "last_refreshed_at": "2023-10-27T10:30:00"
}
```
*Note: `last_refreshed_at` reflects the most recent refresh time of any country entry in the database.*

**Errors**:
- No specific error responses are defined.

#### `GET /status`
Provides a simple application heartbeat with the total number of countries currently stored.

**Request**:
```
No request body required.
```

**Response**:
```json
{
  "total_countries": 193,
  "last_refreshed_at": "2023-10-27T10:30:00"
}
```
*Note: `last_refreshed_at` in this endpoint is a placeholder and reflects the current server timestamp, not the actual data refresh time.*

**Errors**:
- No specific error responses are defined.

## Technologies Used
This project is built using a modern stack of technologies:

| Technology         | Version    | Description                                       | Link                                                       |
| :----------------- | :--------- | :------------------------------------------------ | :--------------------------------------------------------- |
| **Java**           | 21         | Core programming language.                        | [Official Website](https://www.java.com/)                  |
| **Spring Boot**    | 3.5.6      | Framework for building robust, stand-alone, production-grade Spring applications. | [Spring Boot](https://spring.io/projects/spring-boot)      |
| **Spring Data JPA**| Included   | Provides repository abstraction for simplified data access. | [Spring Data JPA](https://spring.io/projects/spring-data-jpa) |
| **H2 Database**    | Included   | Lightweight, in-memory/file-based relational database. | [H2 Database](https://h2database.com/html/main.html)       |
| **Lombok**         | Included   | Pluggable annotation processors for reducing boilerplate code. | [Project Lombok](https://projectlombok.org/)           |
| **Maven**          | 3.6+       | Build automation tool.                            | [Apache Maven](https://maven.apache.org/)                  |
| **RestTemplate**   | Included   | Spring's synchronous client for consuming RESTful services. | [Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#web-client) |

## Author Info
Developed with passion for clean code and efficient solutions.

*   **LinkedIn**: [Denzel Okungbowa](https://linkedin.com/in/denzel-okungbowa)
*   **Twitter**: [samael.exe](https://x.com/KiddMydas)

---
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.java.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)](https://github.com/mydas-denzel/Stage-2-Task/actions)

[![Readme was generated by Dokugen](https://img.shields.io/badge/Readme%20was%20generated%20by-Dokugen-brightgreen)](https://www.npmjs.com/package/dokugen)