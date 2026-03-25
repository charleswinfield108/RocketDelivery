
# Dependencies
To run this application properly on your local machine, you'll need Java 17 and MySQL installed. All other dependencies are already defined in the pom.xml file.

# Instructions
1. Make sure environment variables are properly set (this should already be the case)
2. Log into your MySQL console
3. Create a database named rdelivery
4. Clone the project
5. Open the project in VS Code
6. Edit your database configuration file (src/main/resources/application.properties)
    1. Edit the username, password and database name as required
7. Execute the main function
    1. Open the RocketFoodApplication.java file and run it (on the top right part of VS Code, you should see an arrow pointing to the right, when you hover over it, you see a “Run Java” tooltip. Click it.
    2.  You should see a line similar to this at the end of the execution: `INFO 24016 --- [ main] c.r.rocketFood.RocketFoodApplication : Started RocketFoodApplication in 2.726 seconds (process running for 2.963)`


    CHECKING FOR UPDATE!!!

---

# Relational Databases & SQL: A Guide for RocketDelivery

This project uses **MySQL**, a relational database system, to manage structured data for the food delivery platform. This guide explains the fundamentals of SQL and relational databases to help understand how data is organized and queried in RocketDelivery.

## What is SQL?

**SQL** (Structured Query Language) is a standardized programming language designed specifically for managing and querying relational databases. SQL allows developers and administrators to communicate with databases using a set of commands that follow a consistent syntax across different database systems.

In the context of RocketDelivery, SQL enables us to:
- **Create** database tables to store business data (restaurants, customers, orders, addresses)
- **Read/Retrieve** data using SELECT queries to fetch restaurants, customer information, or order history
- **Update** existing data when a restaurant updates its opening hours or a customer changes their address
- **Delete** data when records are no longer needed

SQL is declarative, meaning you describe *what* data you want without specifying *how* to retrieve it—the database engine handles the optimization. This makes SQL powerful for both simple and complex data operations.

**Example SQL in RocketDelivery:**
```sql
-- Retrieve all restaurants in a specific city
SELECT * FROM restaurants WHERE city = 'San Francisco';

-- Count total orders from a specific customer
SELECT COUNT(*) FROM orders WHERE customer_id = 5;

-- Update a restaurant's closing time
UPDATE restaurants SET closing_time = '22:00' WHERE id = 1;
```

---

## What is the Main Difference Between SQLite and MySQL?

While both SQLite and MySQL are relational databases that use SQL, they differ significantly in architecture, scalability, and use cases:

| Feature | SQLite | MySQL |
|---------|--------|-------|
| **Architecture** | File-based, serverless | Server-based, client-server model |
| **Setup** | Zero configuration; embedded in application | Requires server installation and configuration |
| **Concurrency** | Limited; not ideal for multiple simultaneous users | Excellent; handles thousands of concurrent connections |
| **Scalability** | Best for small to medium projects (<1GB data) | Enterprise-scale; handles terabytes of data |
| **Performance** | Fast for single-user applications | Optimized for multi-user, high-traffic environments |
| **Use Cases** | Mobile apps, local testing, small desktop apps | Web applications, production servers, e-commerce platforms |
| **Data Access** | Direct file access; limited networking | Remote access via network connections |

**Why RocketDelivery Uses MySQL:**
RocketDelivery is a production food delivery platform that will have:
- Multiple users (customers, employees, restaurant managers) accessing data simultaneously
- High traffic with concurrent orders, restaurant listings, and customer queries
- Scalability requirements as the platform grows
- Remote access needs (different users from different locations)
- Complex relationships between restaurants, orders, customers, and products

SQLite would be insufficient for these requirements because it cannot efficiently handle multiple concurrent users or scale to support thousands of simultaneous orders.

---

## What are Primary and Foreign Keys?

### **Primary Key**

A **Primary Key** is a column (or set of columns) that uniquely identifies each row in a table. Every table should have exactly one primary key. The primary key ensures:
- **Uniqueness:** No two rows can have the same primary key value
- **Non-null:** Every row must have a primary key value
- **Identification:** Each row is uniquely identifiable and can be referenced by other tables

**Example in RocketDelivery - Restaurants Table:**
```sql
CREATE TABLE restaurants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    opening_time TIME NOT NULL,
    closing_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

In this example:
- `id` is the **Primary Key** (AUTO_INCREMENT means it's automatically generated: 1, 2, 3, etc.)
- Each restaurant has a unique ID, so Restaurant ID 5 is always the same restaurant
- Other tables can reference this restaurant using its `id`

### **Foreign Key**

A **Foreign Key** is a column in one table that references the primary key of another table. Foreign keys establish relationships between tables and maintain **referential integrity**, ensuring that:
- You cannot insert a customer order for a restaurant ID that doesn't exist
- When a restaurant is deleted, its related data is handled appropriately
- Data relationships remain consistent

**Example in RocketDelivery - Orders Table (References Restaurants):**
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    delivery_address_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    total_price DECIMAL(10, 2) NOT NULL,
    
    -- Foreign Key constraint
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(id)
);
```

In this example:
- `restaurant_id` is a **Foreign Key** that references `restaurants(id)`
- This ensures an order can only reference a restaurant that actually exists
- `ON DELETE CASCADE` means if a restaurant is deleted, its orders are also deleted (enforcing data consistency)
- `customer_id` links to the `customers` table
- `delivery_address_id` links to the `addresses` table

**Why This Matters:**
Without the foreign key constraint, the database might allow an order to reference a restaurant ID 999 that doesn't exist, corrupting your data. With foreign keys, the database prevents this invalid state.

---

## What are the Different Relationship Types in a Relational Database?

Relational databases use three main relationship types to connect data between tables:

### **1. One-to-One (1:1) Relationship**

In a one-to-one relationship, a single row in Table A is associated with exactly one row in Table B, and vice versa.

**Example in RocketDelivery - User and Profile:**
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL
);

CREATE TABLE user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNIQUE NOT NULL,
    bio TEXT,
    profile_picture_url VARCHAR(500),
    preferences JSON,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Relationship:** Each user has exactly one profile, and each profile belongs to exactly one user. The `UNIQUE` constraint on `user_id` ensures this 1:1 relationship.

---

### **2. One-to-Many (1:N) Relationship**

In a one-to-many relationship, one row in Table A can be associated with multiple rows in Table B, but each row in Table B is associated with only one row in Table A.

**Example in RocketDelivery - Restaurant and Employees:**
```sql
CREATE TABLE restaurants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL
);

CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    restaurant_id BIGINT NOT NULL,
    
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);
```

**Relationship:** Each restaurant can have many employees (10, 50, or 100+), but each employee works at exactly one restaurant.

**Real-World Scenario:**
- Restaurant ID 1 (Mario's Pizza) has employees: John (id 101), Maria (id 102), Antonio (id 103)
- Restaurant ID 2 (Burger Palace) has employees: Sarah (id 104), Tom (id 105)
- If Mario's Pizza closes, all its employees are deleted along with it (`ON DELETE CASCADE`)

---

### **3. Many-to-Many (M:N) Relationship**

In a many-to-many relationship, multiple rows in Table A can be associated with multiple rows in Table B. This relationship requires a **junction table** (also called a join table or throughput table) to connect the two tables.

**Example in RocketDelivery - Orders and Products:**

```sql
-- Orders Table
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'PENDING',
    total_price DECIMAL(10, 2) NOT NULL,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Products Table
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    restaurant_id BIGINT NOT NULL,
    
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

-- Junction Table: Links Orders to Products
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

**Relationship:** 
- One order can contain many products (a single food order might have pizza, salad, and dessert)
- One product can appear in many orders (pizza sold to many different customers)

**Real-World Example:**
- Order #1001 contains: 2x Margherita Pizza (product_id 5), 1x Caesar Salad (product_id 12)
- Order #1002 contains: 1x Margherita Pizza (product_id 5), 2x Garlic Bread (product_id 8)
- Product "Margherita Pizza" (product_id 5) appears in both orders

The junction table `order_items` stores these connections, allowing flexible many-to-many relationships.

---

## Summary Table: Relationship Types

| Type | One-to-One | One-to-Many | Many-to-Many |
|------|-----------|------------|-------------|
| **Definition** | 1 A ↔ 1 B | 1 A → Many B | Many A ↔ Many B |
| **Foreign Key Location** | In either table (with UNIQUE) | In the "many" table | In a junction table |
| **Junction Table** | Not needed | Not needed | **Required** |
| **RocketDelivery Examples** | User ↔ Profile | Restaurant → Employees | Order ↔ Products |
| **Cardinality** | 1:1 | 1:N | M:N |

---

## Conclusion

Understanding these SQL and relational database concepts is crucial for building scalable, maintainable applications like RocketDelivery. By using MySQL with proper primary keys, foreign keys, and relationship types, we ensure:
- ✅ Data integrity and consistency
- ✅ Efficient querying and reporting
- ✅ Prevention of orphaned or invalid data
- ✅ Scalability to support thousands of users and transactions
- ✅ Clear logical structure that mirrors real-world business relationships

---

# Analyzing the RocketDelivery Entity Relationship Diagram (ERD)

The RocketDelivery restaurant management system uses a well-structured relational database with multiple relationship types connecting core business entities. This section analyzes key relationship patterns in the ERD.

## Many-to-One Relationship: Orders → Customers

### **Table Pair:** Orders and Customers

**Relationship:** Many Orders to One Customer

```
Customers (1) ──1:N─→ Orders (Many)
```

### **Why This Relationship?**

A customer can place multiple orders over time, but each individual order belongs to exactly one customer. This is a fundamental e-commerce relationship:

- **Customer 1 (John Doe)** can have:
  - Order #1001 (Placed Monday)
  - Order #1002 (Placed Wednesday)
  - Order #1003 (Placed Friday)
  - ... potentially hundreds of orders

- **Each order** can be traced back to its single originating customer

### **Practical Example:**
```sql
-- John Doe (customer_id = 5) has multiple orders
SELECT * FROM orders WHERE customer_id = 5;

-- Result:
-- id | order_number | customer_id | restaurant_id | order_date            | status
-- 1  | ORD-001      | 5          | 2            | 2024-03-01 10:30:00  | DELIVERED
-- 2  | ORD-045      | 5          | 7            | 2024-03-05 18:45:00  | DELIVERED
-- 3  | ORD-089      | 5          | 2            | 2024-03-10 12:15:00  | PENDING
```

### **Database Implementation:**
The `customer_id` foreign key in the `orders` table enforces this many-to-one relationship:
```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);
```

### **Why It Matters:**
- Enables efficient querying of a customer's order history
- Maintains referential integrity (can't create an order for a non-existent customer)
- Supports analytics (total orders per customer, customer lifetime value, repeat customers)
- Cascading delete ensures data consistency (if a customer is deleted, their orders are handled appropriately)

---

### **Another Many-to-One Example: Employees → Restaurants**

A similar many-to-one relationship exists between Employees and Restaurants:

```
Restaurants (1) ──1:N─→ Employees (Many)
```

Each restaurant can employ many staff members, but each employee works at exactly one restaurant. This handles:
- Multiple pizza chefs at the same restaurant
- Different staff across multiple franchises
- Staff assignment and scheduling per restaurant

---

## Many-to-Many Relationship: Orders ↔ Products (via OrderItems)

### **Tables Involved:** Orders, Products, and OrderItems (Junction Table)

**Relationship:** Many Orders can contain Many Products

```
         ┌─────────────────┐
         │   order_items   │  (Junction Table)
         │   (Line Items)  │
         └─────────────────┘
              /          \
             /            \
        Orders           Products
       (Many) ←─1:N─→ (Many)
            ↓              ↓
      Many orders    Many products
      can have       can appear in
      many items     many orders
```

### **Why This Relationship?**

A single food order can contain multiple products (items), and the same product can appear in many different orders. This is a classic many-to-many relationship requiring a junction table:

**Order Perspective:**
- **Order #1001** (John's order) contains:
  - 2x Margherita Pizza (product_id 5)
  - 1x Caesar Salad (product_id 12)
  - 1x Garlic Bread (product_id 8)

**Product Perspective:**
- **Margherita Pizza** (product_id 5) appears in:
  - Order #1001 (John's order) – 2 units
  - Order #1045 (Sarah's order) – 1 unit
  - Order #2089 (Mike's order) – 3 units

### **Practical Database Example:**
```sql
-- View all items in Order #1001
SELECT 
    oi.id,
    p.name,
    oi.quantity,
    oi.unit_price,
    (oi.quantity * oi.unit_price) as total
FROM order_items oi
JOIN products p ON oi.product_id = p.id
WHERE oi.order_id = 1001;

-- Result:
-- id | name             | quantity | unit_price | total
-- 1  | Margherita Pizza | 2        | 12.99      | 25.98
-- 2  | Caesar Salad     | 1        | 8.99       | 8.99
-- 3  | Garlic Bread     | 1        | 5.99       | 5.99
```

### **Database Implementation:**
The `order_items` junction table bridges Orders and Products:
```sql
CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

### **Why It Matters:**
- **Flexibility:** Each order can have a variable number of items
- **Product Reusability:** Products aren't duplicated; they're referenced from a products catalog
- **Price History:** `unit_price` in `order_items` captures the price at order time (important if prices change later)
- **Order Analytics:** Easy to query "which products are most ordered" or "products in a specific order"
- **Inventory Tracking:** Can analyze which products are frequently ordered

---

### **Another Many-to-Many Example: Customers ↔ Restaurants (Implicit)**

While not explicitly modeled with a junction table, customers and restaurants have an implicit many-to-many relationship through orders:
- A customer can order from multiple restaurants
- Each restaurant can serve many customers
- The `orders` table serves as the connection point

---

## One-to-Many (Inverse) Relationships

While the above examples show many-to-one from the child's perspective, from the parent table perspective, these create one-to-many relationships:

### **Customers → Addresses (One-to-Many)**

```
Customers (1) ──1:N─→ Addresses (Many)
```

**Why:** A customer can have multiple addresses (home, office, vacation home) for delivery or billing purposes, but each address belongs to one customer.

**Practical Example:**
```sql
-- John Doe (customer_id = 5) has multiple addresses
SELECT * FROM addresses WHERE customer_id = 5;

-- Result:
-- id | street                  | city          | postal_code | customer_id
-- 1  | 123 Main St            | San Francisco | 94102       | 5
-- 2  | 456 Office Blvd        | San Francisco | 94105       | 5
-- 3  | 789 Vacation Lane      | Tahoe         | 96150       | 5
```

### **Restaurants → Orders (One-to-Many)**

```
Restaurants (1) ──1:N─→ Orders (Many)
```

**Why:** A restaurant can fulfill many orders from different customers, but each order is prepared by one restaurant.

---

## Summary: ERD Relationship Pattern in RocketDelivery

| Relationship Type | Example | Parent Table | Child Table | Why This Pattern |
|------------------|---------|-------------|-------------|-----------------|
| **Many-to-One** | Orders → Customers | Customers | Orders | One customer places many orders |
| **Many-to-One** | Orders → Restaurants | Restaurants | Orders | One restaurant fulfills many orders |
| **Many-to-One** | Employees → Restaurants | Restaurants | Employees | One restaurant employs many staff |
| **Many-to-One** | Addresses → Customers | Customers | Addresses | One customer has many addresses |
| **Many-to-Many** | Orders ↔ Products | — | order_items | One order has many items; one product sold in many orders |

---

## Key Takeaways from RocketDelivery's ERD

1. **Many-to-One relationships** dominate transactional data (Orders, Employees, Addresses), reflecting how business operations typically organize hierarchically
2. **Many-to-Many relationships** require junction tables to avoid data redundancy and enable flexible associations
3. **Foreign keys** enforce referential integrity, preventing orphaned records and maintaining data quality
4. **Cascading deletes** ensure consistency (e.g., deleting a customer removes their orders automatically)
5. The ERD structure directly maps to the Spring Boot package structure (entities, repositories, services)

---

# Test-Driven Development (TDD) in Java: A Guide for RocketDelivery

## What is Test-Driven Development (TDD)?

**Test-Driven Development (TDD)** is a software development methodology where **tests are written BEFORE the actual implementation code**. Instead of writing code and then testing it afterward, TDD inverts this process:

1. **Write a test** that describes what the code should do
2. **Run the test** (it will fail because the code doesn't exist yet)
3. **Write minimal code** to make the test pass
4. **Refactor** the code to improve quality while keeping tests green

This approach ensures that every line of code has a corresponding test, leading to more reliable, maintainable software.

### **TDD vs. Traditional Development:**

| Aspect | Traditional | TDD |
|--------|-----------|-----|
| **Order** | Code → Test | Test → Code |
| **Test Coverage** | Often incomplete, added afterward | 100% by design |
| **Code Quality** | Varies; depends on discipline | Higher; enforced by tests |
| **Debugging** | Later in development cycle | Caught immediately |
| **Refactoring** | Risky; might break untested code | Safe; tests verify correctness |
| **Documentation** | Separate from code | Tests serve as living documentation |

---

## The TDD Cycle: Red-Green-Refactor

TDD follows a three-phase cycle repeated for each feature:

### **Phase 1: RED 🔴**
Write a test that **fails** because the feature doesn't exist yet.

```java
// Test for RocketDelivery: Calculate order total with tax
@Test
public void calculateOrderTotalWithTax() {
    Order order = new Order(subtotal: 100.0, taxRate: 0.08);
    double total = order.calculateTotal();
    
    assertEquals(108.0, total); // FAILS - method doesn't exist yet
}
```

The test is intentionally failing. This confirms:
- The test is actually testing something
- The feature is truly missing
- You're not accidentally passing without implementation

### **Phase 2: GREEN 🟢**
Write the **minimal code** to make the test pass.

```java
// Minimal implementation - just enough to pass the test
public class Order {
    private double subtotal;
    private double taxRate;
    
    public double calculateTotal() {
        return subtotal * (1 + taxRate); // Makes test pass
    }
}
```

**Characteristics of Green Phase:**
- Code is simple and focused
- No over-engineering or premature optimization
- All tests pass
- The implementation directly addresses the test requirement

### **Phase 3: REFACTOR 🔄**
Improve code quality **without changing behavior**. Tests stay green.

```java
// Refactored: More maintainable, still passes all tests
public class Order {
    private BigDecimal subtotal;
    private double taxRate;
    
    public BigDecimal calculateTotal() {
        BigDecimal taxMultiplier = BigDecimal.valueOf(1 + taxRate);
        return subtotal.multiply(taxMultiplier);
    }
}
```

**Refactoring Goals:**
- Use appropriate data types (BigDecimal for money, not double)
- Improve readability and naming
- Reduce duplication
- Optimize performance
- **Tests continuously verify correctness**

---

## Why TDD Matters for RocketDelivery

A food delivery platform like RocketDelivery has **zero tolerance for bugs** because they directly impact:
- **Customer Trust:** Incorrect order totals or delivery tracking loses trust
- **Revenue:** Payment failures and order miscalculations cost money
- **Reputation:** Delivery errors and wrong food damage brand reputation
- **Data Integrity:** Incorrect database operations can corrupt customer data

### **TDD Benefits for RocketDelivery:**

#### **1. Prevents Critical Business Logic Bugs**
```java
// Example: Order validation
@Test
public void shouldRejectOrderWithInactiveRestaurant() {
    Restaurant restaurant = new Restaurant(isActive: false);
    Order order = new Order(restaurant, items);
    
    assertThrows(IllegalStateException.class, () -> orderService.createOrder(order));
    // TDD ensures this rule is tested and never breaks
}
```

#### **2. Enables Safe Refactoring**
Food delivery is complex with interconnected services. TDD prevents regression when improving code:

```java
// Before refactoring: Complex hairy code that works
public void updateOrderStatus() { /* 50 lines of spaghetti */ }

// After refactoring: Clean, maintainable code
// Tests confirm: Same behavior, better structure
@Test
public void orderStatusTransitionsCorrectly() { /* verified */ }
```

#### **3. Catches Integration Issues Early**
```java
// Test database interactions before production
@Test
public void shouldPersistOrderAndNotifyRestaurant() {
    Order order = orderService.createOrder(request);
    
    assertTrue(orderRepository.findById(order.getId()).isPresent());
    verify(emailService).notifyRestaurant(order.getRestaurantId());
}
```

#### **4. Reduces Customer-Reported Bugs**
- Fewer bugs reach production
- Faster issue resolution when they occur
- Higher-quality order history and delivery tracking

---

## TDD Best Practices for Java

### **1. Test Naming Convention: Describe the Behavior**

```java
// ❌ Poor - Doesn't describe intent
@Test
public void test1() { }

// ✅ Good - Clear, describes expected behavior
@Test
public void shouldCalculateOrderTotalWithDiscount() { }

// ✅ Also good - Arrange-Act-Assert pattern
@Test
public void orderWithDiscount_appliesDiscountCorrectly() { }
```

### **2. Follow Arrange-Act-Assert (AAA) Pattern**

Every test should have three clear sections:

```java
@Test
public void shouldApply10PercentDiscountForLoyalCustomers() {
    // ARRANGE: Set up test data
    Customer loyalCustomer = new Customer(isLoyalty: true);
    Order order = new Order(subtotal: 100.0, customer: loyalCustomer);
    
    // ACT: Perform the action being tested
    double discount = discountService.calculateDiscount(order);
    
    // ASSERT: Verify the result
    assertEquals(10.0, discount);
}
```

### **3. Write One Assertion Per Test (When Possible)**

```java
// ❌ Multiple assertions - harder to isolate failures
@Test
public void testOrderCreation() {
    Order order = new Order(customer, items);
    assertEquals("PENDING", order.getStatus());
    assertEquals(99.99, order.getTotal());
    assertNotNull(order.getCreatedAt());
    assertTrue(order.getItems().size() > 0);
}

// ✅ Separate, focused tests
@Test
public void newOrderHasPendingStatus() {
    Order order = new Order(customer, items);
    assertEquals("PENDING", order.getStatus());
}

@Test
public void newOrderHasCorrectTotal() {
    Order order = new Order(customer, items);
    assertEquals(99.99, order.getTotal());
}
```

### **4. Test Both Success and Failure Cases**

```java
// Success case
@Test
public void shouldCreateOrderSuccessfully() {
    Order order = orderService.createOrder(validRequest);
    assertNotNull(order.getId());
}

// Failure cases
@Test
public void shouldRejectOrderWithoutItems() {
    assertThrows(IllegalArgumentException.class, 
        () -> orderService.createOrder(emptyRequest));
}

@Test
public void shouldRejectOrderWithInvalidAddress() {
    assertThrows(InvalidAddressException.class,
        () -> orderService.createOrder(invalidAddressRequest));
}
```

### **5. Use Meaningful Test Data**

```java
// ❌ Magic numbers - what do they represent?
@Test
public void calculateDeliveryFee() {
    double distance = 5.5;
    double fee = deliveryService.calculateFee(distance);
    assertEquals(12.99, fee);
}

// ✅ Named variables - clear intent
@Test
public void calculateDeliveryFeeFor5MileDelivery() {
    double distance = 5.5; // miles
    double EXPECTED_FEE = 12.99;
    
    double calculatedFee = deliveryService.calculateFee(distance);
    assertEquals(EXPECTED_FEE, calculatedFee);
}
```

### **6. Keep Tests Fast and Isolated**

```java
// ✅ Use mocks to avoid slow database calls
@Test
public void shouldApplyRestaurantDiscountRules() {
    // Mock the database
    RestaurantRepository mockRepo = mock(RestaurantRepository.class);
    when(mockRepo.findById(1L)).thenReturn(
        Optional.of(new Restaurant(discountRule: 0.15))
    );
    
    DiscountService service = new DiscountService(mockRepo);
    double discount = service.calculateDiscount(1L);
    
    assertEquals(0.15, discount);
    // Fast - no database access
}
```

---

## TDD Workflow Example: Order Status Validation

Let's walk through a complete TDD cycle for a RocketDelivery feature:

### **Feature:** Validate that orders can only transition to valid statuses

**Step 1: RED 🔴 - Write Failing Test**
```java
@Test
public void shouldRejectInvalidOrderStatusTransition() {
    Order order = new Order(status: "PENDING");
    
    // PENDING → CANCELLED is valid
    // PENDING → COMPLETED would be invalid (must be CONFIRMED first)
    
    assertThrows(IllegalStateException.class, 
        () -> order.setStatus("COMPLETED"));
}
```

**Step 2: GREEN 🟢 - Write Minimal Code**
```java
public class Order {
    private String status;
    
    public void setStatus(String newStatus) {
        if ("PENDING".equals(status) && "COMPLETED".equals(newStatus)) {
            throw new IllegalStateException("Invalid transition");
        }
        this.status = newStatus;
    }
}
```

**Step 3: REFACTOR 🔄 - Improve Quality**
```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    DELIVERING,
    COMPLETED,
    CANCELLED;
    
    // Define valid transitions
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        PENDING, Set.of(CONFIRMED, CANCELLED),
        CONFIRMED, Set.of(DELIVERING, CANCELLED),
        DELIVERING, Set.of(COMPLETED),
        COMPLETED, Set.of(),
        CANCELLED, Set.of()
    );
    
    public boolean canTransitionTo(OrderStatus next) {
        return VALID_TRANSITIONS.get(this).contains(next);
    }
}

public class Order {
    private OrderStatus status;
    
    public void setStatus(OrderStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + status + " to " + newStatus);
        }
        this.status = newStatus;
    }
}
```

**Additional Test Cases (TDD ensures all are covered):**
```java
@Test
public void shouldAllow_PENDING_to_CONFIRMED() {
    Order order = new Order(status: PENDING);
    order.setStatus(CONFIRMED); // ✅ No exception
}

@Test
public void shouldAllow_PENDING_to_CANCELLED() {
    Order order = new Order(status: PENDING);
    order.setStatus(CANCELLED); // ✅ No exception
}

@Test
public void shouldReject_PENDING_to_COMPLETED() {
    Order order = new Order(status: PENDING);
    assertThrows(IllegalStateException.class, () -> order.setStatus(COMPLETED));
}
```

---

## TDD in the Spring Boot Context (RocketDelivery's Framework)

RocketDelivery uses Spring Boot with JPA, so TDD applies to:

### **Service Layer Testing**
```java
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private RestaurantService restaurantService;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    public void shouldCreateOrderIfRestaurantExists() {
        // Arrange
        Restaurant restaurant = new Restaurant(id: 1L, name: "Mario's Pizza");
        when(restaurantService.getById(1L)).thenReturn(restaurant);
        
        // Act
        Order order = orderService.createOrder(customerId: 5L, items: [...]);
        
        // Assert
        verify(orderRepository).save(order);
        assertEquals(1L, order.getRestaurantId());
    }
}
```

### **Controller Testing**
```java
@WebMvcTest(OrderController.class)
public class OrderControllerTest {
    
    @MockBean
    private OrderService orderService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void shouldReturn200WhenCreatingOrder() throws Exception {
        // Arrange
        Order order = new Order(id: 1L, status: "PENDING");
        when(orderService.createOrder(any())).thenReturn(order);
        
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\": 5}"))
            .andExpect(status().isCreated());
    }
}
```

---

## TDD Benefits Summary

| Benefit | How TDD Delivers It |
|---------|------------------|
| **Bug Prevention** | Every code path has a test; bugs caught before deployment |
| **Documentation** | Tests show exactly how to use each class/method |
| **Design Improvement** | Hard-to-test code forces better architecture |
| **Refactoring Safety** | Tests confirm changes don't break functionality |
| **Faster Development** | Fewer bugs to fix later saves overall time |
| **Team Confidence** | Code reviews backed by comprehensive tests |
| **Regression Prevention** | Future changes verified against existing tests |

---

## Getting Started with TDD in RocketDelivery

1. **Start small:** Apply TDD to new features, not legacy code
2. **Use JUnit 5 and Mockito:** Already in RocketDelivery's pom.xml
3. **Write tests first, code second:** Resist the temptation to code first
4. **Aim for high coverage:** RocketDelivery goals: 80%+ code coverage
5. **Review tests in code reviews:** Test quality = System quality
6. **Run tests frequently:** Continuous feedback loop prevents regressions

TDD transforms RocketDelivery from a system that "seems to work" into one where every order calculation, delivery status, and customer interaction is **mathematically proven correct** through comprehensive testing.

