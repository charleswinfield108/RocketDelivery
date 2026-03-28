# Initial Setup Instructions - RocketDelivery v2.0

This guide walks through the complete setup process for running RocketDelivery locally.

---

## 📋 Prerequisites

Before starting, ensure you have the following installed:

| Requirement | Version | Verify Command |
|-------------|---------|-----------------|
| **Java JDK** | 17+ | `java -version` |
| **MySQL** | 8.0+ | `mysql --version` |
| **Git** | Any | `git --version` |
| **Maven** | 3.8+ (optional) | `mvn -v` |

---

## 🚀 Step-by-Step Setup

### Step 1: Clone the Repository

```bash
git clone https://github.com/charleswinfield108/RocketDelivery.git
cd RocketDelivery
```

Verify you're on the dev branch:
```bash
git branch
# Should show: * dev
```

### Step 2: Verify Java & MySQL

```bash
# Check Java
java -version
# Should show: java version "17..." or higher

# Check MySQL is running
mysql -u root -p -e "SELECT 1"
# Should return: 1
```

### Step 3: Create Database

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rdelivery;"
# Or for schema testing:
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rocketfood;"
```

### Step 4: Configure Application Properties

**Copy the template:**
```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

**Edit with your credentials:**
```bash
nano src/main/resources/application.properties
# Or use VS Code: code src/main/resources/application.properties
```

**Update these values:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rdelivery
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# Logging
logging.level.root=INFO
logging.level.com.rocketFoodDelivery=DEBUG

# Server
server.port=8080
```

⚠️ **Important:** Never commit `application.properties` to Git (already in .gitignore)

### Step 5: Compile the Project

```bash
./mvnw clean compile
# Or if Maven installed:
mvn clean compile
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X.XXXs
```

### Step 6: Start Spring Boot Application

```bash
./mvnw spring-boot:run
# Or:
mvn spring-boot:run
```

**Wait for success message:**
```
2026-03-27 10:30:15.123 INFO 12345 --- [ main] c.r.rocketFood.RocketFoodApplication : Started RocketFoodApplication in 2.726 seconds
```

**To stop:** Press `Ctrl+C`

### Step 7: Verify Database Tables (Optional)

Open another terminal and check that tables were created:

```bash
mysql -u root -p rdelivery -e "SHOW TABLES;"
```

You should see tables like:
```
users, customers, restaurants, products, orders, order_statuses, employees, addresses, product_orders
```

### Step 8: Access the Application

**Web UI (Back Office):**
```
http://localhost:8080/backoffice/restaurants
```

**REST API:**
```bash
# Get all restaurants
curl http://localhost:8080/api/restaurants

# Get all orders
curl http://localhost:8080/api/orders
```

---

## ⚠️ Schema v2.0 Changes (Important!)

RocketDelivery v2.0 includes database schema alignment. **If upgrading from v1.0:**

**New Requirements:**
- Restaurants now require FK to Address (`address_id`)
- Restaurants now have `priceRange` field (1-3)
- Employees now require FK to User (`user_id`) and Address (`address_id`)
- Customers now require FK to Address (`address_id`)
- Orders use OrderStatus FK instead of String field
- Products renamed field: `price` → `cost`

**What to do:**
1. See SCHEMA_COMPLIANCE.md for entity changes
2. See MIGRATION.md for SQL migration scripts
3. Or, use `ddl-auto=create-drop` for fresh database

---

## 🆘 Troubleshooting

### MySQL Connection Error
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**Solution:**
```bash
# Check MySQL is running
mysql -u root -p -e "SELECT 1"

# If not running, start MySQL:
# macOS: brew services start mysql-server
# Linux: sudo systemctl start mysql
# Windows: net start MySQL80
```

### Database Not Created
```
[ERROR] Unknown database 'rdelivery'
```

**Solution:**
```bash
mysql -u root -p -e "CREATE DATABASE rdelivery;"
```

### Port 8080 Already in Use
```
[ERROR] Failed to start embedded Tomcat on port 8080
```

**Solution:**
```bash
# Change in application.properties:
server.port=8081

# Or kill process using 8080:
# macOS/Linux: lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
# Windows: netstat -ano | findstr :8080
```

### Git Branch Issues
```
fatal: not a git repository
```

**Solution:**
```bash
# Verify you're in project directory:
cd /home/avaspop/Projects/RocketDelivery

# Check git status:
git status
git branch
```

### Java Version Mismatch
```
[ERROR] COMPILATION ERROR
[ERROR] Source option 17 is not supported. Use 6, 7, 8, 11, 16, or 17.
```

**Solution:**
```bash
# Install Java 17 or update PATH to use Java 17:
java -version
# Should show: java version "17.0.X"
```

---

## ✅ Verification Checklist

- [ ] Java 17+ installed: `java -version`
- [ ] MySQL 8.0+ running: `mysql -u root -p -e "SELECT 1"`
- [ ] Git cloned: `git status` (shows dev branch)
- [ ] Database created: `mysql -u root -p -e "SHOW DATABASES;" | grep rdelivery`
- [ ] application.properties configured with your credentials
- [ ] Project compiles: `mvn clean compile` (BUILD SUCCESS)
- [ ] Application starts: `mvn spring-boot:run` (shows "Started RocketFoodApplication")
- [ ] Tables created: `mysql -u root -p rdelivery -e "SHOW TABLES;"`
- [ ] API responds: `curl http://localhost:8080/api/restaurants`

---

## 📚 Next Steps

1. **Explore the API:** See quick_start.md
2. **Set up testing:** See testing.md
3. **Back office UI:** Access http://localhost:8080/backoffice/restaurants
4. **Review schema:** Check SCHEMA_COMPLIANCE.md
5. **Check documentation:** See DEVELOPER_GUIDE.md

---

## ❓ Got Questions?

- **About setup?** Check the Troubleshooting section above
- **About schema?** See SCHEMA_COMPLIANCE.md
- **About API?** See readme.md
- **About testing?** See testing.md
