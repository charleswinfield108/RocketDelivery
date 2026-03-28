# Database Migration Guide - Schema v2.0 Alignment

**Version:** 2.0  
**Date:** March 27, 2026  
**Status:** Ready for Implementation  

This guide provides step-by-step instructions for migrating from the previous database schema to the new schema-aligned version.

---

## ⚠️ Important Notes Before Migration

1. **Backup Your Database**: Always backup your MySQL database before running migrations
   ```bash
   mysqldump -u root -p rdelivery > backup_before_migration.sql
   ```

2. **No Data Loss**: These migrations preserve existing data while adding new columns and constraints

3. **Test First**: Run migrations on a test database first before production

4. **Backward Compatibility**: The application continues to work during and after migration due to compatibility methods

---

## 🔄 Migration Steps

### Step 1: Order Statuses Reference Data

First, ensure all order statuses exist in the database:

```sql
-- Create reference data for order statuses
INSERT INTO order_statuses (status_code, name, status_name, description, display_order, is_active, created_at)
VALUES 
  ('PENDING', 'Pending', 'Pending', 'Order received, awaiting confirmation', 1, true, NOW()),
  ('CONFIRMED', 'Confirmed', 'Confirmed', 'Restaurant confirmed the order', 2, true, NOW()),
  ('PREPARING', 'Preparing', 'Preparing', 'Restaurant is preparing the order', 3, true, NOW()),
  ('READY', 'Ready', 'Ready', 'Order is ready for delivery or pickup', 4, true, NOW()),
  ('OUT_FOR_DELIVERY', 'Out for Delivery', 'Out for Delivery', 'Driver has picked up and is en route', 5, true, NOW()),
  ('DELIVERED', 'Delivered', 'Delivered', 'Order successfully delivered to customer', 6, true, NOW()),
  ('CANCELLED', 'Cancelled', 'Cancelled', 'Order was cancelled by customer or restaurant', 7, true, NOW());
```

**Note:** If table doesn't exist, Hibernate will create it automatically on first run.

---

### Step 2: Update Employees Table

Add new required columns for employee entity compliance:

```sql
-- Add user_id (OneToOne relationship to users)
ALTER TABLE employees 
  ADD COLUMN user_id BIGINT UNIQUE AFTER id,
  ADD FOREIGN KEY fk_employee_user (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  ADD INDEX idx_user_id (user_id);

-- Add address_id (ManyToOne relationship to addresses)
ALTER TABLE employees 
  ADD COLUMN address_id BIGINT NOT NULL AFTER user_id,
  ADD FOREIGN KEY fk_employee_address (address_id) REFERENCES addresses(id) ON DELETE RESTRICT,
  ADD INDEX idx_address_id (address_id);

-- Make restaurant_id optional (was required)
ALTER TABLE employees MODIFY COLUMN restaurant_id BIGINT NULL;
```

**Data Migration (if employees exist):**
```sql
-- For each existing employee, assign a user from users table
-- This assumes employees were created for specific users

-- Example: If you have a mapping or can infer from context
UPDATE employees e
SET e.user_id = (SELECT u.id FROM users u WHERE u.email = CONCAT('employee_', e.id, '@restaurant.com') LIMIT 1)
WHERE e.user_id IS NULL;

-- Assign a default address from addresses table
UPDATE employees e
SET e.address_id = (SELECT a.id FROM addresses a LIMIT 1)
WHERE e.address_id IS NULL;
```

---

### Step 3: Update Restaurants Table

Add new columns and update foreign key naming:

```sql
-- Add price_range field
ALTER TABLE restaurants 
  ADD COLUMN price_range INT NOT NULL DEFAULT 1 AFTER name,
  ADD CHECK (price_range BETWEEN 1 AND 3),
  ADD INDEX idx_price_range (price_range);

-- Add address_id (unique ManyToOne relationship to addresses)
ALTER TABLE restaurants 
  ADD COLUMN address_id BIGINT UNIQUE AFTER owner_id,
  ADD FOREIGN KEY fk_restaurant_address (address_id) REFERENCES addresses(id) ON DELETE RESTRICT,
  ADD INDEX idx_address_id (address_id);

-- Rename owner_id to user_id (schema compliance)
ALTER TABLE restaurants 
  CHANGE COLUMN owner_id user_id BIGINT NOT NULL,
  ADD FOREIGN KEY fk_restaurant_user (user_id) REFERENCES users(id) ON DELETE RESTRICT;

-- Update the foreign key name if needed
ALTER TABLE restaurants DROP FOREIGN KEY restaurants_ibfk_1;
```

**Data Migration (if restaurants exist):**
```sql
-- Assign addresses to restaurants
-- Option 1: Use existing embedded address to find/create corresponding address record
-- This assumes you want to link restaurants to specific address records

UPDATE restaurants r
SET r.address_id = (SELECT a.id FROM addresses a WHERE a.street LIKE CONCAT('%', r.street, '%') LIMIT 1)
WHERE r.address_id IS NULL AND EXISTS(SELECT 1 FROM addresses a WHERE a.street LIKE CONCAT('%', r.street, '%'));

-- Option 2: If no matching address found, create one from embedded fields
-- (This would require custom script outside SQL)

-- Ensure price_range is set (default is already 1)
UPDATE restaurants SET price_range = 2 WHERE price_range IS NULL;
```

---

### Step 4: Update Orders Table

Migrate from String status to OrderStatus foreign key:

```sql
-- Add order_status_id (ManyToOne FK) column
ALTER TABLE orders 
  ADD COLUMN order_status_id BIGINT NOT NULL AFTER order_number,
  ADD FOREIGN KEY fk_order_status (order_status_id) REFERENCES order_statuses(id) ON DELETE RESTRICT,
  ADD INDEX idx_order_status_id (order_status_id);

-- Add restaurant_rating column
ALTER TABLE orders 
  ADD COLUMN restaurant_rating INT AFTER status,
  ADD CHECK (restaurant_rating BETWEEN 1 AND 5 OR restaurant_rating IS NULL);

-- Populate order_status_id from existing status column
UPDATE orders o
SET o.order_status_id = (
  SELECT os.id FROM order_statuses os 
  WHERE os.status_code = UPPER(TRIM(o.status))
  LIMIT 1
)
WHERE o.order_status_id IS NULL;

-- Verify migration (should show 0 rows)
SELECT COUNT(*) as unmapped_orders FROM orders WHERE order_status_id IS NULL;
```

**Important:** If there are unmapped statuses (count > 0), check them:
```sql
SELECT DISTINCT status FROM orders WHERE order_status_id IS NULL;
```

And insert missing order statuses or fix the data.

---

### Step 5: Update OrderStatuses Table

Add the 'name' field required by schema:

```sql
-- Add name field (user-friendly status name)
ALTER TABLE order_statuses 
  ADD COLUMN name VARCHAR(100) NOT NULL UNIQUE AFTER id,
  ADD INDEX idx_name (name);

-- Populate name from status_name if not already set
UPDATE order_statuses SET name = status_name WHERE name IS NULL OR name = '';
```

---

### Step 6: Update Customers Table

Add address requirement:

```sql
-- Add address_id (ManyToOne relationship to addresses)
ALTER TABLE customers 
  ADD COLUMN address_id BIGINT NOT NULL AFTER user_id,
  ADD FOREIGN KEY fk_customer_address (address_id) REFERENCES addresses(id) ON DELETE RESTRICT,
  ADD INDEX idx_address_id (address_id);
```

**Data Migration (if customers exist):**
```sql
-- Assign default address from addresses table
UPDATE customers c
SET c.address_id = (SELECT a.id FROM addresses a LIMIT 1)
WHERE c.address_id IS NULL;

-- Or assign specific addresses if you have a mapping
UPDATE customers c
SET c.address_id = (SELECT a.id FROM addresses a WHERE a.user_id = c.user_id LIMIT 1)
WHERE c.address_id IS NULL AND EXISTS(SELECT 1 FROM addresses a WHERE a.user_id = c.user_id);
```

---

### Step 7: Update Products Table

Rename price column to cost:

```sql
-- Rename price to cost
ALTER TABLE products RENAME COLUMN price TO cost;

-- Verify the change
DESC products;  -- Should show 'cost' column, not 'price'
```

---

## ✅ Verification Checklist

After running migrations, verify everything:

```sql
-- 1. Check all new columns exist
DESC employees;      -- Should have user_id, address_id
DESC restaurants;    -- Should have address_id, price_range, user_id (renamed)
DESC orders;         -- Should have order_status_id, restaurant_rating
DESC order_statuses; -- Should have name column
DESC customers;      -- Should have address_id
DESC products;       -- Should have cost (not price)

-- 2. Verify foreign key constraints
SELECT CONSTRAINT_NAME, TABLE_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME IN ('employees', 'restaurants', 'orders', 'customers')
  AND COLUMN_NAME IN ('user_id', 'address_id', 'order_status_id')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- 3. Check for unmapped records (should all be 0)
SELECT 'employees missing user_id' as check_type, COUNT(*) as count FROM employees WHERE user_id IS NULL
UNION ALL
SELECT 'employees missing address_id', COUNT(*) FROM employees WHERE address_id IS NULL
UNION ALL
SELECT 'restaurants missing address_id', COUNT(*) FROM restaurants WHERE address_id IS NULL
UNION ALL
SELECT 'restaurants missing price_range', COUNT(*) FROM restaurants WHERE price_range IS NULL
UNION ALL
SELECT 'orders missing order_status_id', COUNT(*) FROM orders WHERE order_status_id IS NULL
UNION ALL
SELECT 'customers missing address_id', COUNT(*) FROM customers WHERE address_id IS NULL;

-- 4. Sample data verification
SELECT * FROM restaurants LIMIT 1;  -- Check user_id, address_id, price_range present
SELECT * FROM orders LIMIT 1;       -- Check order_status_id, restaurant_rating present
SELECT * FROM products LIMIT 1;     -- Check 'cost' column exists
```

---

## 🚀 Application Startup After Migration

Once migrations are complete:

```bash
# 1. Stop the application if running
# Press Ctrl+C in the terminal where it's running

# 2. Rebuild and restart
mvn clean install
mvn spring-boot:run

# 3. Monitor console output
# Should see successful startup with "Started RocketFoodApplication"
# Watch for any Hibernate validation errors

# 4. Test with API
curl http://localhost:8080/api/restaurants
curl http://localhost:8080/api/orders
curl http://localhost:8080/api/order-statuses
```

---

## 🔄 Rollback Procedure

If you need to rollback the migration:

```bash
# 1. Restore from backup
mysql -u root -p rdelivery < backup_before_migration.sql

# 2. Rebuild application code to match database schema
mvn clean install
mvn spring-boot:run
```

---

## 🐛 Troubleshooting

### Issue: Foreign Key Constraint Violation

**Problem:** Migration fails with foreign key error
```
ERROR 1452: Cannot add or update a child row
```

**Solution:**
1. Identify which records have missing references
2. Either create the referenced records or delete the orphaned records
3. Re-run the migration

### Issue: Duplicate Entry for Unique Column

**Problem:** Migration adds unique constraint but duplicate data exists
```
ERROR 1131: Duplicate entry
```

**Solution:**
1. Find duplicates: `SELECT name, COUNT(*) FROM order_statuses GROUP BY name HAVING COUNT(*) > 1;`
2. Remove duplicates or merge them
3. Re-run the migration

### Issue: Column Already Exists

**Problem:** Running migration twice causes "column already exists" error

**Solution:**
```sql
-- Check if column exists before adding
ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_status_id BIGINT;
```

### Issue: Application Won't Start After Migration

**Problem:**
```
Caused by: org.hibernate.tool.schema.spi.SchemaManagementException: 
Schema validation: missing column [order_status_id]
```

**Solution:**
1. Make sure all migration SQL ran successfully
2. Verify with: `DESC orders;` (should show order_status_id)
3. If column missing, run the SQL manually in MySQL
4. Restart application with `mvn spring-boot:run`

---

## 📝 Notes for Developers

1. **Column Naming Convention**: Database uses snake_case (order_status_id), Java uses camelCase (orderStatus)
2. **BigDecimal for Money**: Products.cost uses BigDecimal, not int, for monetary precision
3. **Lazy Loading**: Foreign key relationships use FetchType.LAZY to improve query performance
4. **Cascade Options**: Most FKs use ON DELETE RESTRICT to prevent cascading deletes
5. **Indexes**: Added on new FK columns for query performance optimization

---

## Questions?

- See [SCHEMA_COMPLIANCE.md](SCHEMA_COMPLIANCE.md) for entity-by-entity changes
- See [readme.md](readme.md) for database schema overview
- Check Java entity definitions in `src/main/java/com/rocketFoodDelivery/rocketFood/models/`
