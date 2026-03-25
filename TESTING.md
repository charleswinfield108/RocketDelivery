# 🧪 RocketDelivery Testing Documentation

Comprehensive guide for testing the RocketDelivery Spring Boot application as features are built incrementally using feature branches.

---

## Table of Contents

1. [Testing Environment Setup](#testing-environment-setup)
2. [Prerequisites & Tools](#prerequisites--tools)
3. [Testing Approach](#testing-approach)
4. [Unit Testing](#unit-testing)
5. [Integration Testing](#integration-testing)
6. [Manual Testing](#manual-testing)
7. [Feature Testing Checklist](#feature-testing-checklist)
8. [Common Commands](#common-commands)
9. [Troubleshooting](#troubleshooting)

---

## Testing Environment Setup

### Local Development Environment

**Recommended Setup:**
```
┌─────────────────────────────────────────────┐
│         Development Workstation             │
├─────────────────────────────────────────────┤
│  Java 17 JDK                                │
│  Maven 3.8+                                 │
│  MySQL 8.0+ (running on localhost:3306)    │
│  DBeaver or MySQL Workbench                │
│  VS Code with Java Extension Pack           │
│  Postman (optional, for API testing)        │
│  Git (version control)                      │
└─────────────────────────────────────────────┘
       ↓
   Spring Boot Application
   (localhost:8080)
       ↓
   MySQL Database
   (rocketfood)
```

### Database Setup

```bash
# Login to MySQL
mysql -u root -p

# Create database (if not exist)
CREATE DATABASE rocketfood;
USE rocketfood;

# Verify empty state
SHOW TABLES;
-- Should return empty set initially

# Exit
EXIT;
```

### Application Properties Configuration

**File:** `src/main/resources/application.properties`

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/rocketfood
spring.datasource.username=root
spring.datasource.password=AvaPop628_
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.root=INFO
logging.level.com.rocketFoodDelivery=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Key Setting:** `spring.jpa.hibernate.ddl-auto=update`
- Auto-creates/updates tables based on entity definitions
- Essential for feature-by-feature development

---

## Prerequisites & Tools

### Required Tools

| Tool | Version | Purpose | Verify Command |
|------|---------|---------|-----------------|
| **Java JDK** | 17+ | Compile & run application | `java -version` |
| **Maven** | 3.8+ | Build tool | `mvn -v` |
| **MySQL** | 8.0+ | Database engine | `mysql -V` |
| **Git** | Any | Version control | `git --version` |

### Development Tools

| Tool | Purpose | Why Needed |
|------|---------|-----------|
| **DBeaver** | Database management & visualization | Verify table creation, inspect data, check constraints |
| **VS Code** | IDE with Java support | Write & debug code |
| **Java Extension Pack** | VS Code extension | Java language support, debugging |
| **Postman** | API testing (later phases) | Test REST endpoints |
| **MySQL Workbench** | Alternative to DBeaver | Query editor, schema visualization |

### Verification Checklist

```bash
# Run this before each testing session
✅ Java 17 installed
java -version

✅ Maven installed
mvn -v

✅ MySQL server running
mysql -u root -p -e "SELECT 1"

✅ Database exists
mysql -u root -p -e "SHOW DATABASES;" | grep rocketfood

✅ application.properties configured
cat src/main/resources/application.properties | grep spring.datasource

✅ No existing test data (clean state for new feature)
mysql -u root -p -e "USE rocketfood; SHOW TABLES;"

✅ Git on dev branch
git branch
# Should show: * dev
```

---

## Testing Approach

### Three-Level Testing Strategy

```
┌──────────────────────────────────────────────┐
│  UNIT TESTS                                  │
│  (Entity validation, service logic)          │
│  Tools: JUnit 5, Mockito                     │
│  Run: mvn test                               │
│  Coverage: Individual classes in isolation   │
└──────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────┐
│  INTEGRATION TESTS                           │
│  (Entity ↔ Repository ↔ Database)           │
│  Tools: Spring Boot Test, TestContainers     │
│  Run: mvn verify                             │
│  Coverage: Spring context, database CRUD    │
└──────────────────────────────────────────────┘
              ↓
┌──────────────────────────────────────────────┐
│  MANUAL TESTS                                │
│  (Application startup, UI verification)      │
│  Tools: DBeaver, Postman, Browser           │
│  Run: ./mvnw spring-boot:run                |
│  Coverage: Full application flow             │
└──────────────────────────────────────────────┘
```

### Feature Development Cycle

```
1. Create Feature Branch
   └─> git checkout -b feature/users-schema

2. Implement Entity Class
   └─> UserEntity.java with JPA annotations

3. Compile & Verify
   └─> mvn clean compile
   └─> Check for compilation errors

4. Start Spring Boot
   └─> mvn spring-boot:run
   └─> Wait for startup message

5. Verify Database
   └─> Open DBeaver
   └─> Check table creation
   └─> Verify columns & constraints

6. Implement Repository & Service
   └─> UserRepository.java
   └─> UserService.java

7. Write Unit Tests
   └─> UserEntityTest.java
   └─> UserServiceTest.java

8. Run Tests
   └─> mvn test
   └─> Verify all tests pass

9. Manual Testing
   └─> Test service methods
   └─> Verify data in database

10. Commit & Prepare Merge
    └─> git add .
    └─> git commit -m "feat: implement users schema"
    └─> git push origin feature/users-schema

11. Merge to Dev
    └─> git checkout dev
    └─> git merge feature/users-schema
```

---

## Unit Testing

### Unit Test Framework Setup

**Dependencies in pom.xml (Already Included):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Includes:
- JUnit 5 (Jupiter)
- Mockito
- AssertJ
- Spring Boot Test

### Example Unit Test: UserEntity

**File:** `src/test/java/com/rocketFoodDelivery/rocketFood/models/UserEntityTest.java`

```java
package com.rocketFoodDelivery.rocketFood.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {
    
    private UserEntity user;
    
    @BeforeEach
    void setUp() {
        user = new UserEntity();
    }
    
    @Test
    void testUserCreation() {
        user.setEmail("john@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("5551234567");
        
        assertEquals("john@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
    }
    
    @Test
    void testEmailValidation() {
        user.setEmail("invalid-email");
        // Jakarta validation will catch this
        assertNotNull(user.getEmail());
    }
    
    @Test
    void testTimestampGeneration() {
        assertNull(user.getCreatedAt()); // Set by Hibernate on insert
        assertNull(user.getUpdatedAt());
    }
}
```

### Example Unit Test: UserService

**File:** `src/test/java/com/rocketFoodDelivery/rocketFood/service/UserServiceTest.java`

```java
package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    
    private UserService userService;
    
    @Mock
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }
    
    @Test
    void testCreateUser() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("5551234567");
        
        when(userRepository.save(user)).thenReturn(user);
        
        UserEntity savedUser = userService.createUser(user);
        
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    void testFindUserById() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("john@example.com");
        
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        
        UserEntity foundUser = userService.findUserById(1L);
        
        assertNotNull(foundUser);
        assertEquals("john@example.com", foundUser.getEmail());
    }
}
```

### Running Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run specific test method
mvn test -Dtest=UserServiceTest#testCreateUser

# Run with coverage report
mvn test jacoco:report
# View report: target/site/jacoco/index.html
```

---

## Integration Testing

### Integration Test: Entity with Database

**File:** `src/test/java/com/rocketFoodDelivery/rocketFood/repository/UserRepositoryTest.java`

```java
package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testSaveAndRetrieveUser() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("5551234567");
        
        // Act
        UserEntity savedUser = userRepository.save(user);
        Optional<UserEntity> retrievedUser = userRepository.findById(savedUser.getId());
        
        // Assert
        assertTrue(retrievedUser.isPresent());
        assertEquals("test@example.com", retrievedUser.get().getEmail());
        assertEquals("Test", retrievedUser.get().getFirstName());
    }
    
    @Test
    void testUniqueEmailConstraint() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setEmail("unique@example.com");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setPhoneNumber("5551111111");
        userRepository.save(user1);
        
        UserEntity user2 = new UserEntity();
        user2.setEmail("unique@example.com"); // Same email
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setPhoneNumber("5552222222");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            userRepository.flush(); // Force constraint check
        });
    }
    
    @Test
    void testFindAllUsers() {
        // Arrange
        UserEntity user1 = new UserEntity();
        user1.setEmail("user1@example.com");
        user1.setFirstName("User");
        user1.setLastName("One");
        user1.setPhoneNumber("5551111111");
        
        UserEntity user2 = new UserEntity();
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setPhoneNumber("5552222222");
        
        userRepository.save(user1);
        userRepository.save(user2);
        
        // Act
        var users = userRepository.findAll();
        
        // Assert
        assertTrue(users.size() >= 2);
    }
}
```

### Running Integration Tests

```bash
# Run all tests (includes integration)
mvn test

# Run only integration tests
mvn verify

# Run specific integration test
mvn test -Dtest=UserRepositoryTest

# Run with Spring Boot context (slower but tests full app)
mvn test -Dtest="**/*IT.java"
```

---

## Manual Testing

### Starting the Application

```bash
# Navigate to project directory
cd /home/avaspop/Projects/RocketDelivery

# Clean and compile
mvn clean compile

# Start Spring Boot
mvn spring-boot:run

# Expected output:
# ...
# 2024-03-25 10:30:15.123 INFO 12345 --- [ main] c.r.rocketFood.RocketFoodApplication : Started RocketFoodApplication in 2.726 seconds (process running for 2.963)
# 2024-03-25 10:30:15.456 INFO 12345 --- [ main] org.hibernate.dialect.Dialect : HHH000400: Using dialect: org.hibernate.dialect.MySQL8Dialect
```

### Verifying Database State

**Open DBeaver:**

1. Right-click on MySQL connection → **Refresh**
2. Expand `rocketfood` database
3. Expand `Tables` folder
4. Should see auto-created tables based on entities

**View Table Structure:**
```
Right-click table (e.g., users) → View Table → DDL
```

**Query Table Data:**
```sql
-- In DBeaver SQL Editor:
USE rocketfood;
SELECT * FROM users;
```

### Checking Application Logs

**In VS Code Terminal (where mvn spring-boot:run runs):**

Look for:
- ✅ Entity mapping messages: `HHH000412: Hibernate initialization`
- ✅ SQL statements (if show-sql=true): `INSERT INTO users...`
- ✅ No error stacktraces

### Testing Entity Persistence

**Test Scenario: Create User via Service**

```bash
# 1. Start application (mvn spring-boot:run)
# 2. In another terminal, run:

# Create a quick test script or use Spring Boot shell:
java -jar target/rocketFood-1.0.0.jar

# Or add a temporary test method in a controller
```

**Alternative: Use Spring Boot Actuator Shell (if enabled)**

```bash
# Add to pom.xml:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

# In application.properties:
management.endpoints.web.exposure.include=*
```

Then access: `http://localhost:8080/actuator/health`

---

## Feature Testing Checklist

### For Each Feature Branch

Use this checklist to verify each feature is complete and tested:

#### Pre-Implementation
- [ ] Branch created: `git checkout -b feature/users-schema`
- [ ] Feature spec reviewed: `AI_FEATURE_USERS_SCHEMA.md`
- [ ] Database is clean: `mysql -e "USE rocketfood; SHOW TABLES;"`

#### Entity Implementation
- [ ] Entity class created with all fields
- [ ] @Entity, @Table annotations applied
- [ ] @Id and @GeneratedValue on primary key
- [ ] @Column annotations with constraints
- [ ] Lombok annotations (@Data, @NoArgsConstructor, @AllArgsConstructor)
- [ ] Jakarta validation annotations (@NotNull, @Email, @Size, etc.)

#### Compilation & Database
- [ ] Code compiles without errors: `mvn clean compile`
- [ ] Spring Boot starts: `mvn spring-boot:run`
- [ ] No SQL errors in console output
- [ ] Table created in DBeaver
- [ ] Column names match specification
- [ ] Data types correct (BIGINT, VARCHAR, TIME, etc.)
- [ ] Constraints applied (UNIQUE, NOT NULL, PRIMARY KEY)

#### Repository Layer
- [ ] Repository interface created extending JpaRepository
- [ ] Compiles without errors
- [ ] Basic CRUD methods available

#### Service Layer
- [ ] Service class created with @Service annotation
- [ ] Constructor injection of repository
- [ ] CRUD methods implemented:
  - [ ] Create/Save
  - [ ] Read/FindById
  - [ ] FindAll
  - [ ] Update
  - [ ] Delete
- [ ] Validation logic implemented
- [ ] Exception handling added

#### Unit Tests
- [ ] Entity tests written
- [ ] Service tests written (with Mockito)
- [ ] Repository tests written (@DataJpaTest)
- [ ] All tests pass: `mvn test`
- [ ] Test coverage > 80%

#### Integration Testing
- [ ] Data persists to database correctly (DBeaver verification)
- [ ] UNIQUE constraints enforced
- [ ] NOT NULL constraints enforced
- [ ] Timestamps auto-populated (if applicable)
- [ ] Foreign key relationships work (later features)
- [ ] No orphaned data after operations

#### Git & Cleanup
- [ ] Feature branch code committed
- [ ] Commit message follows convention: `feat: implement {feature-name}`
- [ ] All tests pass on feature branch
- [ ] Ready to merge to dev

#### Final Checklist
- [ ] Feature works in isolation
- [ ] Ready for next feature build
- [ ] Documentation updated if needed

---

## Common Commands

### Git Commands

```bash
# Create feature branch
git checkout -b feature/users-schema

# View current branch
git branch

# Switch to branch
git checkout feature/users-schema

# Check branch status
git status

# Stage changes
git add .

# Commit changes
git commit -m "feat: implement users schema with entity, repository, service, tests"

# Push to remote
git push origin feature/users-schema

# Switch to dev
git checkout dev

# Merge feature into dev
git merge feature/users-schema

# Delete local feature branch
git branch -d feature/users-schema
```

### Maven Commands

```bash
# Clean & compile
mvn clean compile

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=UserServiceTest

# Start Spring Boot application
mvn spring-boot:run

# Build JAR
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# View dependency tree
mvn dependency:tree

# Check plugin versions
mvn -version
```

### MySQL Commands

```bash
# Login
mysql -u root -p

# Show databases
SHOW DATABASES;

# Use specific database
USE rocketfood;

# Show all tables
SHOW TABLES;

# Describe table structure
DESC users;

# View table data
SELECT * FROM users;

# Count records
SELECT COUNT(*) FROM users;

# Drop table (be careful!)
DROP TABLE users;

# Exit MySQL
EXIT;
```

### Database Verification

```bash
# Quick verification script
mysql -u root -p rocketfood -e "
SELECT 'Tables:' as '';
SHOW TABLES;
SELECT '' as '';
SELECT 'Users Table Structure:' as '';
DESC users;
SELECT '' as '';
SELECT 'Sample Data:' as '';
SELECT * FROM users LIMIT 5;
"
```

---

## Troubleshooting

### Common Issues & Solutions

#### Issue: Test Compilation Fails

**Error:** `Cannot find symbol...`

**Solution:**
```bash
# Clean Maven cache
mvn clean

# Rebuild
mvn compile

# Check that all dependencies are in pom.xml
```

#### Issue: Application Won't Start

**Error:** `Connection refused` or `Access denied`

**Solution:**
```bash
# Verify MySQL is running
mysql -u root -p -e "SELECT 1"

# Check application.properties credentials
cat src/main/resources/application.properties

# Verify database exists
mysql -u root -p -e "SHOW DATABASES;" | grep rocketfood

# Try creating database manually
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS rocketfood;"
```

#### Issue: Table Not Created

**Error:** No tables appear in DBeaver

**Solution:**
```bash
# Check application.properties:
# Should have: spring.jpa.hibernate.ddl-auto=update

# Check Spring Boot console output for Hibernate logs
# Look for: "HHH000412: Hibernate initialization"

# Verify entity has @Entity annotation
# Check logs for SQL errors

# Manually refresh DBeaver: Right-click connection → Refresh

# Check DBeaver connection:
# Tools → Database → New Database Connection → MySQL
```

#### Issue: Unique Constraint Violated

**Error:** `Duplicate entry 'example@test.com' for key 'email_UNIQUE'`

**Solution:**
```bash
# Clear data from table
mysql -u root -p -e "USE rocketfood; TRUNCATE TABLE users;"

# Or delete specific record
mysql -u root -p -e "USE rocketfood; DELETE FROM users WHERE email='example@test.com';"

# Restart application
mvn spring-boot:run
```

#### Issue: Tests Fail with Database Issues

**Error:** `Database is locked` or `Connection reset`

**Solution:**
```bash
# Stop running Spring Boot instance (press Ctrl+C)

# Wait 5 seconds

# Restart
mvn spring-boot:run

# Or explicitly restart MySQL
# On Linux/Mac: brew restart mysql-server
# On Windows: net restart MySQL80
```

#### Issue: Port 8080 Already in Use

**Error:** `Failed to start embedded Tomcat on port 8080`

**Solution:**
```bash
# Change port in application.properties
echo "server.port=8081" >> src/main/resources/application.properties

# Or kill process using port 8080
# On Linux/Mac: lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill
# On Windows: netstat -ano | findstr :8080
```

#### Issue: Git Branch Issues

**Error:** `Branch already exists` or `Cannot checkout`

**Solution:**
```bash
# See all branches
git branch -a

# Check current branch
git branch

# Force checkout (discard local changes)
git checkout -f feature/users-schema

# Delete and recreate branch
git branch -D feature/users-schema
git checkout -b feature/users-schema
```

---

## Testing Checklist Summary

Before each feature merge to `dev`:

```bash
✅ Code compiles:
   mvn clean compile

✅ All tests pass:
   mvn test

✅ Application starts:
   mvn spring-boot:run
   (Wait for "Started RocketFoodApplication" message)

✅ Database tables created (verify in DBeaver)

✅ Entity constraints enforced

✅ Service methods tested

✅ Data persists correctly (query in DBeaver)

✅ No console errors or warnings

✅ Git branch clean and committed

✅ Ready to merge to dev
```

---

## Next Steps

1. **Create Feature Branch:** `git checkout -b feature/users-schema`
2. **Follow Testing Checklist:** For each item as you implement
3. **Run Tests:** `mvn test` after each component
4. **Verify Database:** Check DBeaver after Spring Boot starts
5. **Commit & Push:** When all tests pass
6. **Merge to Dev:** When feature is complete

**Happy testing! 🚀**
