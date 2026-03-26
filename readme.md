# 🚀 RocketDelivery - Food Delivery Platform

> A comprehensive, production-ready food delivery platform built with **Spring Boot and MySQL**, featuring a complete REST API, professional error handling, and extensive documentation.

---

## 📋 Table of Contents

1. [Project Title & Description](#-rocketdelivery---food-delivery-platform)
2. [Tech Stack](#-tech-stack)
3. [Project Structure](#-project-structure)
4. [Installation & Setup Instructions](#-installation--setup-instructions)
5. [Environment Variables](#-environment-variables)
6. [API Documentation](#-api-documentation)
7. [Author](#-author)
8. [Additional Resources](#-additional-resources)
9. [Module 11 Questions](#-module-11-questions)

---

## 🛠 Tech Stack

| Category | Technology | Version/Notes |
|----------|-----------|---------------|
| **Language** | Java | 17+ |
| **Framework** | Spring Boot | Latest LTS |
| **ORM** | Spring Data JPA / Hibernate | Auto-configured |
| **Database** | MySQL | 5.7+ / 8.0+ |
| **Build Tool** | Maven | 3.6+ (included: mvnw) |
| **Server** | Embedded Tomcat | Via Spring Boot |
| **Template Engine** | Thymeleaf | For web UI |
| **Validation** | Jakarta Validation API | Bean validation |
| **Logging** | SLF4J / Logback | Default Spring Boot logging |
| **Testing** | JUnit 5, Mockito | In pom.xml |
| **Utilities** | Lombok, Annotation Processing | Reduce boilerplate code |

### Key Dependencies:
- ✅ Spring Boot Starter Web (REST & MVC)
- ✅ Spring Boot Starter Data JPA (Database persistence)
- ✅ MySQL Connector/J (Database driver)
- ✅ Spring Boot Starter Thymeleaf (Server-side templating)
- ✅ Lombok (Code generation)
- ✅ Jakarta Validation & Persistence APIs

---

## 📁 Project Structure

```
RocketDelivery/
├── src/
│   ├── main/
│   │   ├── java/com/rocketFoodDelivery/rocketFood/
│   │   │   ├── controller/              # REST & MVC Controllers
│   │   │   │   ├── RestaurantController.java         # Web UI
│   │   │   │   └── RestaurantRestController.java     # REST API (40+ endpoints)
│   │   │   ├── models/                  # JPA Entity Classes
│   │   │   │   ├── RestaurantEntity, UserEntity, CustomerEntity
│   │   │   │   ├── OrderEntity, EmployeeEntity, AddressEntity
│   │   │   │   ├── OrderStatusEntity, ProductEntity, ProductOrderEntity
│   │   │   ├── repository/              # Data Access Layer
│   │   │   │   └── 8 Spring Data repositories
│   │   │   ├── service/                 # Business Logic
│   │   │   │   └── 8 business services
│   │   │   ├── RocketFoodApplication.java
│   │   │   └── DataSeeder.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── application.properties.example
│   │   │   └── templates/
│   │   │       ├── backoffice/restaurants/
│   │   │       └── error/               # Professional error pages
│   └── test/java/                       # Unit & integration tests
├── docs/                                # Documentation
├── pom.xml                              # Maven dependencies
├── mvnw / mvnw.cmd                      # Maven wrapper
└── README.md                            # This file
```

### Folder Purpose Summary:

| Folder | Purpose |
|--------|---------|
| **controller/** | HTTP request handlers (REST & web endpoints) |
| **models/** | JPA Entity classes (database table mappings) |
| **repository/** | Data access layer (Spring Data repositories) |
| **service/** | Business logic layer (service classes) |
| **templates/** | Thymeleaf HTML templates (UI & error pages) |

---

## 📦 Installation & Setup Instructions

### Prerequisites

Ensure you have the following installed:

- **Java 17+** → Check: `java -version`
- **MySQL 5.7 or 8.0+** → Check: `mysql --version`
- **Maven 3.6+** → Check: `mvn --version` (or use included `mvnw`)

### Step 1️⃣: Clone the Repository

```bash
git clone https://github.com/charleswinfield108/RocketDelivery.git
cd RocketDelivery
```

### Step 2️⃣: Set Up Database

```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE rdelivery;
EXIT;
```

### Step 3️⃣: Configure Application Properties

```bash
# Copy configuration template
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit with your database credentials
nano src/main/resources/application.properties
```

Update these properties:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rdelivery
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

### Step 4️⃣: Run the Application

**Option A: Using Maven Wrapper (Recommended)**
```bash
./mvnw spring-boot:run
```

**Option B: Using VS Code**
- Open `RocketFoodApplication.java`
- Click the "Run Java" button (arrow icon, top-right)

**Option C: Build & Run JAR**
```bash
./mvnw clean install
java -jar target/rocketFood-1.0.0.jar
```

### Step 5️⃣: Verify Installation

```bash
# Test API endpoint
curl http://localhost:8080/api/restaurants

# Test web UI
curl http://localhost:8080/backoffice/restaurants
```

✅ **Success:** Application runs on `http://localhost:8080`

---

## 🔧 Environment Variables

Create an `.env` file or update `application.properties` with:

| Variable | Purpose | Default | Example |
|----------|---------|---------|---------|
| `spring.datasource.url` | MySQL connection URL | — | `jdbc:mysql://localhost:3306/rdelivery` |
| `spring.datasource.username` | MySQL username | `root` | `root` |
| `spring.datasource.password` | MySQL password | — | `yourpassword` |
| `spring.jpa.hibernate.ddl-auto` | Schema management | `update` | `validate`, `create`, `create-drop` |
| `server.port` | Application port | `8080` | `8080` |
| `logging.level.root` | Root logging level | `INFO` | `DEBUG`, `INFO`, `WARN` |

### Complete Configuration Example:

```properties
# ===== DATABASE =====
spring.datasource.url=jdbc:mysql://localhost:3306/rdelivery
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===== JPA/HIBERNATE =====
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# ===== APPLICATION =====
spring.application.name=RocketDelivery
server.port=8080

# ===== LOGGING =====
logging.level.root=INFO
logging.level.com.rocketFoodDelivery=DEBUG
```

---

## 📡 API Documentation

### Base URL
```
http://localhost:8080/api
```

### REST API Overview

The unified `RestaurantRestController` provides **40+ endpoints** covering 8 entity types with full CRUD operations.

### Supported Entities & Endpoints

| Entity | Base Path | CRUD Operations |
|--------|-----------|-----------------|
| **Restaurants** | `/api/restaurants` | GET, POST, PUT, DELETE |
| **Users** | `/api/users` | GET, POST, PUT, DELETE |
| **Customers** | `/api/customers` | GET, POST, PUT, DELETE |
| **Addresses** | `/api/addresses` | GET, POST, PUT, DELETE |
| **Orders** | `/api/orders` | GET, POST, PUT, DELETE |
| **Employees** | `/api/employees` | GET, POST, PUT, DELETE |
| **OrderStatuses** | `/api/order-statuses` | GET, POST, PUT, DELETE |
| **ProductOrders** | `/api/product-orders` | GET, POST, PUT, DELETE |

### HTTP Methods & Response Codes

| Method | Operation | Success Status | Example |
|--------|-----------|---|---------|
| `GET /api/restaurants` | List all (paginated) | `200 OK` | Fetch 10 restaurants per page |
| `GET /api/restaurants/{id}` | Get single | `200 OK` | Fetch restaurant with ID 5 |
| `POST /api/restaurants` | Create | `201 Created` | Create new restaurant |
| `PUT /api/restaurants/{id}` | Update | `200 OK` | Update restaurant details |
| `DELETE /api/restaurants/{id}` | Delete | `204 No Content` | Remove restaurant |

### Error Responses

All errors return JSON:
```json
{
  "error": "Restaurant not found",
  "timestamp": 1711353600000
}
```

| Status | Meaning | When Returned |
|--------|---------|---------------|
| **400** | Bad Request | Invalid input/validation fails |
| **403** | Forbidden | Authorization error |
| **404** | Not Found | Resource doesn't exist |
| **500** | Server Error | Unexpected server error |

### Example API Requests

**List Restaurants:**
```bash
curl -X GET "http://localhost:8080/api/restaurants?page=0&size=10"
```

**Get Specific Restaurant:**
```bash
curl -X GET "http://localhost:8080/api/restaurants/1"
```

**Create Restaurant:**
```bash
curl -X POST "http://localhost:8080/api/restaurants" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tony'\''s Pizza",
    "email": "tony@pizza.com",
    "phoneNumber": "555-0123",
    "city": "San Francisco",
    "country": "USA",
    "openingTime": "10:00",
    "closingTime": "22:00",
    "owner": { "id": 1 }
  }'
```

**Update Restaurant:**
```bash
curl -X PUT "http://localhost:8080/api/restaurants/1" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Tony'\''s Pizza Palace", ... }'
```

**Delete Restaurant:**
```bash
curl -X DELETE "http://localhost:8080/api/restaurants/1"
```

### Pagination Example

```bash
curl "http://localhost:8080/api/restaurants?page=0&size=5&sort=name,asc"
```

Response includes:
```json
{
  "content": [ ... 5 items ... ],
  "totalElements": 25,
  "totalPages": 5,
  "currentPageNumber": 0,
  "pageSize": 5,
  "isLast": false
}
```

### Authorization

- 🔐 Some endpoints require owner/user ID parameters for authorization
- 📝 Example: `PUT /api/restaurants/{id}` requires `owner_id`
- 💡 See controller code for specific authorization requirements

---

## 👨‍💻 Author

**Charles Winfield**
- GitHub: [@charleswinfield108](https://github.com/charleswinfield108)
- Repository: [RocketDelivery](https://github.com/charleswinfield108/RocketDelivery)
- Platform: GitHub
- Contact: Available via repository



---

## 📚 Additional Resources

### Database Concepts
- **SQL Fundamentals** - Queries, joins, and relationships
- **Primary & Foreign Keys** - Data integrity enforcement
- **Entity Relationships** - 1:1, 1:N, M:N patterns
- **Database Design** - Normalization and schema design

### Project Documentation
- **Back Office Guide** → [docs/back_office_implementation_report.md](docs/back_office_implementation_report.md)
- **Testing Guide** → [docs/docs/testing.md](docs/docs/testing.md)
- **Quick Start** → [docs/QUICK_START.md](docs/QUICK_START.md)
- **AI Features** → [docs/ai/features/](docs/ai/features/)

### Development Guides
- **Test-Driven Development (TDD)** - Write tests first, then code
- **Spring Boot Best Practices** - Framework conventions
- **REST API Design** - HTTP methods, status codes, pagination
- **Entity Relationships** - Database modeling patterns

---

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| **MySQL Connection Error** | Verify MySQL is running: `mysql --version`, check credentials |
| **Port 8080 Already in Use** | Change `server.port` in properties or kill process: `lsof -i :8080` |
| **Build Fails** | Run `./mvnw clean install -DskipTests` |
| **Database Not Created** | Manually create: `CREATE DATABASE rdelivery;` |
| **Lombok Annotations Not Working** | Enable annotation processing in IDE settings |
| **Tests Failing** | Check MySQL is running, database exists, credentials are correct |

---

## ❓ Module 11 Questions

Brief overview of questions answered in Module 11, covering project setup, architecture, and REST API implementation.

For detailed answers and additional information, see [readme.modulequestions.md](readme.modulequestions.md)

---

## 📄 License & Status

- **Status:** ✅ Production Ready
- **Last Updated:** March 2026
- **Java Version:** 17+
- **Spring Boot:** Latest LTS
- **License:** Educational & Commercial Use

---

**For support or questions, check the logs and verify configuration in `application.properties`**

