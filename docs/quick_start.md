# ⚡ Quick Start & Feature Verification Guide

Simple, quick procedures for starting tools and verifying features work after implementation.

---

## 📌 Schema v2.0 Updates (March 27, 2026)

**Important Changes Affecting Verification Procedures:**

This guide was created for the initial schema. The project has been updated to **Schema v2.0** with these changes affecting quick start:

| Entity | Change | Verification Impact |
|--------|--------|---------------------|
| **OrderEntity** | `status` (String) → `orderStatus` (FK to OrderStatusEntity) | Verify `order_status_id` column (FK), not `status` (String) |
| **EmployeeEntity** | Added `user_id` (OneToOne FK) and `address_id` (ManyToOne FK) | Both fields required in DDL |
| **RestaurantEntity** | Added `address_id` (unique FK) and `priceRange` field | Verify new columns in DDL |
| **RestaurantEntity** | Renamed `owner_id` → `user_id` column | Updated column name in database |
| **CustomerEntity** | Added `address_id` (ManyToOne FK, required) | Verify new FK column exists |
| **ProductEntity** | Renamed `price` → `cost` | Verify column renamed in DDL |
| **OrderStatusEntity** | Added `name` field with values (PENDING, CONFIRMED, etc.) | Lookup table now populated in DataSeeder |

**When Verifying Tables in DBeaver:**
- All FK columns use `_id` suffix (e.g., `order_status_id`, `user_id`, `address_id`)
- Verify constraints: FOREIGN KEY, UNIQUE, NOT NULL per spec
- See [SCHEMA_COMPLIANCE.md](../../SCHEMA_COMPLIANCE.md) for complete changes

When running `mvn spring-boot:run`, DataSeeder will automatically populate **OrderStatusEntity** with (PENDING, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED).

---

## 🚀 Startup Procedures

### 1️⃣ Start DBeaver (Database Viewer)

**What:** Visual database inspection tool to verify tables and data

**Steps:**
```
1. Open DBeaver application
2. Look for "MySQL" connection on left sidebar
3. If not found:
   - Click "+" → New Database Connection
   - Type: MySQL
   - Hostname: localhost
   - Port: 3306
   - Database: rocketfood
   - Username: root
   - Password: AvaPop628_
   - Click Test Connection
   - Click Finish
4. Expand "MySQL" → "rocketfood" → "Tables"
5. You're ready to inspect tables and data
```

**Quick Test:**
```
Right-click "Tables" → Refresh
Should see tables appear as you implement features
```

---

### 2️⃣ Start Spring Boot Application

**What:** Runs the Java application with auto-create database tables

**Terminal Steps:**
```bash
# Step 1: Navigate to project
cd /home/avaspop/Projects/RocketDelivery

# Step 2: Clean build
mvn clean compile

# Step 3: Start application
mvn spring-boot:run

# Step 4: Wait for success message
# Look for green text: "Started RocketFoodApplication in X.XXX seconds"

# Success = Tables will auto-create in database!
```

**You'll see in terminal:**
```
...
2024-03-25 10:30:15.123 INFO 12345 --- [ main] c.r.rocketFood.RocketFoodApplication : Started RocketFoodApplication in 2.726 seconds
```

**To Stop:** Press `Ctrl+C` in terminal

---

## ✅ Quick Feature Verification (After Implementation)

### Checklist for Each Feature

After you implement a feature (e.g., Users Schema):

**🔧 Step 1: Compile**
```bash
mvn clean compile
# Should complete WITHOUT errors
# If errors → Fix code before proceeding
```

**🚀 Step 2: Start Application**
```bash
mvn spring-boot:run
# Wait for "Started RocketFoodApplication" message
# If fails → Check MySQL is running, credentials correct
```

**🔍 Step 3: Verify Table in DBeaver**
```
1. In DBeaver: Right-click "Tables" → Refresh
2. Look for new table (e.g., "users")
3. Right-click table → View Table → DDL
4. Verify columns exist:
   - id, email, firstName, lastName, phoneNumber, createdAt, updatedAt
5. Check column types and constraints
```

**📝 Step 4: Verify Data**
```
In DBeaver SQL Editor:
SELECT * FROM users;

Should show:
- Table structure
- Any test data you created
- NO errors
```

**🧪 Step 5: Run Tests**
```bash
# In new terminal (keep Spring Boot running):
mvn test

# Should see:
# Tests run: X
# Failures: 0
# Skipped: 0
# BUILD SUCCESS
```

---

## 📋 Simple Verification Checklist

Use this after implementing each feature:

```
✅ Code Compiles
   Command: mvn clean compile
   Expected: BUILD SUCCESS

✅ Application Starts
   Command: mvn spring-boot:run
   Expected: "Started RocketFoodApplication in X.XXX seconds"

✅ Table Exists
   Check in DBeaver: Tables → [TableName]
   Expected: Table visible in DBeaver

✅ Columns Correct
   Right-click table → View Table → DDL
   Expected: All columns from spec present

✅ Constraints Applied
   Expected: UNIQUE, NOT NULL, PRIMARY KEY shown in DDL

✅ Tests Pass
   Command: mvn test
   Expected: 0 failures, BUILD SUCCESS

✅ Data Persists
   Query in DBeaver: SELECT * FROM [tablename];
   Expected: Rows inserted and retrieved successfully
```

---

## 🔌 Startup Sequence (Full Process)

**Do this at the start of each work session:**

### Minute 1: Tools Ready
```
1. Open DBeaver (already connected to MySQL)
2. Open VS Code (already have project open)
3. Open Terminal in VS Code
```

### Minute 2: Verify Prerequisites
```bash
# In VS Code Terminal:
java -version
# Should show: openjdk version "17.x"

mysql -u root -p -e "SELECT 1"
# Should prompt for password, then return "1"
```

### Minute 3: Start Spring Boot
```bash
# In VS Code Terminal:
cd /home/avaspop/Projects/RocketDelivery
mvn clean compile
mvn spring-boot:run

# Wait for "Started RocketFoodApplication" message
# Keep this terminal open (do NOT close it!)
```

### Minute 4: Verify Application is Ready
```bash
# In DBeaver:
1. Right-click "rocketfood" database → Refresh
2. Expand Tables
3. Should see existing tables (if any)
4. Application is ready for testing!
```

**Now you can implement features!**

---

## 🧪 Testing a Single Feature (Example: Users Schema)

**Scenario: You just implemented UserEntity, UserRepository, UserService**

### Quick Verification (5 minutes)

**Terminal 1: Spring Boot Running** ✅ (from startup above)

**Terminal 2: Run Tests**
```bash
# Open new terminal
mvn test -Dtest=UserServiceTest

# Should see:
# Tests run: 3
# Failures: 0
# BUILD SUCCESS
```

**DBeaver: Check Table**
```
1. Right-click "Tables" → Refresh
2. Look for "users" table
3. Right-click "users" → View Table
4. Should show data:
   | id | email | firstName | lastName | phoneNumber | createdAt | updatedAt |
```

**Query Table**
```sql
-- In DBeaver SQL Editor:
SELECT COUNT(*) as user_count FROM users;

-- Should return:
-- user_count: 1 (or however many you created in tests)
```

**✅ Feature Works!** → Ready to commit

---

## 🐛 Quick Troubleshooting

### Application Won't Start
```
Error: "Connection refused"

Fix:
1. Make sure MySQL is running
2. Check credentials in application.properties
3. Restart MySQL service
```

### Compilation Errors
```
Error: "Cannot find symbol"

Fix:
1. mvn clean
2. mvn compile
3. Check syntax in your Java file
```

### Table Not Showing
```
Application runs but table missing

Fix:
1. In DBeaver: Right-click → Refresh
2. Check @Entity annotation on class
3. Check application.properties: spring.jpa.hibernate.ddl-auto=update
4. Restart Spring Boot: Ctrl+C then mvn spring-boot:run
```

### Test Fails
```
Error: "Test failed"

Fix:
1. Check test code for typos
2. Ensure database is clean (no old test data)
3. Run: mvn test -Dtest=YourTestClass -X (verbose)
```

---

## 📱 Commands Cheat Sheet

### Essential Commands

```bash
# 🚀 START APPLICATION
mvn spring-boot:run

# 🧮 COMPILE
mvn clean compile

# 🧪 RUN TESTS
mvn test

# 🧪 RUN ONE TEST CLASS
mvn test -Dtest=UserServiceTest

# 🧪 RUN ONE TEST METHOD
mvn test -Dtest=UserServiceTest#testCreateUser

# 🛑 CREATE BRANCH
git checkout -b feature/users-schema

# 📤 COMMIT CHANGES
git add .
git commit -m "feat: implement users schema"

# 🔀 MERGE TO DEV
git checkout dev
git merge feature/users-schema
```

### Database Queries

```bash
# 📊 CHECK TABLES
mysql -u root -p -e "USE rocketfood; SHOW TABLES;"

# 📋 VIEW TABLE STRUCTURE
mysql -u root -p -e "USE rocketfood; DESC users;"

# 📥 VIEW TABLE DATA
mysql -u root -p -e "USE rocketfood; SELECT * FROM users;"

# 🔢 COUNT ROWS
mysql -u root -p -e "USE rocketfood; SELECT COUNT(*) FROM users;"

# 🗑️ CLEAR TABLE DATA
mysql -u root -p -e "USE rocketfood; TRUNCATE TABLE users;"
```

---

## 🎯 Standard Work Session Flow

**Each time you work on a feature:**

### 1. Start (5 min)
```bash
cd /home/avaspop/Projects/RocketDelivery
mvn spring-boot:run
# Wait for startup message
# Keep open - do NOT close
```

### 2. Implement (varies)
```
- Create entity class
- Add repository
- Add service
- Write tests
- Verify compilation: mvn clean compile
```

### 3. Test (10 min)
```bash
# In new terminal:
mvn test

# In DBeaver:
- Refresh tables
- View table structure
- Query data
- Verify constraints
```

### 4. Commit (5 min)
```bash
git add .
git commit -m "feat: implement [feature-name]"
git push origin feature/[feature-name]
```

### 5. Stop (1 min)
```bash
# Close Spring Boot: Ctrl+C in terminal
# Keep DBeaver & VS Code open for next feature
```

---

## 📌 Key Points to Remember

✅ **Always have Spring Boot running** when testing features
- Started via: `mvn spring-boot:run`
- Tables auto-create via Hibernate
- Keep terminal visible to see logs

✅ **Always refresh DBeaver** after starting application
- Right-click "Tables" → Refresh
- New tables should appear

✅ **Always run mvn clean compile** before Spring Boot
- Catches errors early
- Ensures all classes compiled

✅ **Always run tests before committing**
- `mvn test` must pass
- Ensures feature works

✅ **Use DBeaver to verify**
- Check table exists
- Check columns match spec
- Check data persists
- Check constraints enforced

---

## ⏱️ Time Estimates

| Task | Time |
|------|------|
| Startup (Maven + Spring Boot + DBeaver) | 3-5 min |
| Compile code | 30 sec |
| Implement entity + service | 10-15 min |
| Write tests | 10-15 min |
| Run tests | 30 sec - 2 min |
| Verify in DBeaver | 2-3 min |
| Commit & push | 2-3 min |
| **Total per feature** | **30-45 min** |

---

## ✨ You're Ready!

Print this page or bookmark it.
Reference it at the start of each feature.

**Next step: Start Feature #1 - Users Schema**

```bash
# 1. Create branch
git checkout -b feature/users-schema

# 2. Start application
mvn spring-boot:run

# 3. Implement entities/services (follow spec)

# 4. Verify
mvn test
# Check DBeaver

# 5. Commit
git add .
git commit -m "feat: implement users schema"
```

**Let's build! 🚀**
