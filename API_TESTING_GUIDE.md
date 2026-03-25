# RocketDelivery Restaurant API - Testing Guide

This document explains how to test the Restaurant CRUD API using Postman.

## Files Provided

- **RocketDelivery_Restaurant_API.postman_collection.json** - Complete Postman collection with all sample requests

## How to Import the Collection into Postman

### Option 1: Import from File
1. Open Postman
2. Click **Import** button (top left)
3. Select **Upload Files**
4. Choose `RocketDelivery_Restaurant_API.postman_collection.json`
5. Click **Import**

### Option 2: Import from Raw JSON
1. Open Postman
2. Click **Import** button
3. Select **Raw text** tab
4. Copy and paste the contents of the JSON file
5. Click **Continue** then **Import**

## API Base URL
```
http://localhost:8080
```

**Important:** Make sure your Spring Boot application is running before testing!

## Test Sequence (Complete CRUD Test)

### Step 1: Create (POST) - Add Two Restaurants
1. Run **"1. CREATE - Create New Restaurant"**
   - Creates a Burger King restaurant
   - Note the returned `id` value

2. Run **"2. CREATE - Create Another Restaurant"**
   - Creates a Pizza Palace restaurant
   - Note the returned `id` value

**Expected Response:** HTTP 201 Created

---

### Step 2: Read (GET) - Retrieve Restaurants
1. Run **"3. READ - Get All Restaurants (Page 0)"**
   - Should show both restaurants created
   - Check pagination metadata

2. Run **"3. READ - Get Restaurant by ID"**
   - Edit ID in URL to match a created restaurant
   - Should return single restaurant details

**Expected Response:** HTTP 200 OK

---

### Step 3: Update (PUT) - Modify Restaurants
1. Run **"4. UPDATE - Update Restaurant"**
   - Edit ID in URL to match a created restaurant
   - Updates restaurant name, email, address, etc.
   - Note: Owner is preserved automatically

**Expected Response:** HTTP 200 OK with updated data

---

### Step 4: Delete (DELETE) - Remove a Restaurant
1. Run **"5. DELETE - Delete Restaurant"**
   - Edit ID in URL to match one of the restaurants
   - Removes restaurant from database

**Expected Response:** HTTP 204 No Content (no body)

---

### Step 5: Verify (GET) - Confirm Delete
1. Run **"6. VERIFY - Get Deleted Restaurant (Should be 404)"**
   - Use the ID of deleted restaurant
   - Should return 404 error

2. Run **"6. VERIFY - Get All Restaurants After Delete"**
   - Should show one fewer restaurant
   - Verify totalElements count decreased

**Expected Response:** HTTP 404 Not Found (for deleted restaurant)

---

## Testing Error Scenarios

### Test 1: Invalid Data (Validation Error)
- Run: **"ERROR - Create with Invalid Data"**
- Expected: HTTP 400 Bad Request with error message

### Test 2: Update Non-existent Restaurant
- Run: **"ERROR - Update Non-existent Restaurant"**
- Expected: HTTP 404 Not Found

### Test 3: Delete Non-existent Restaurant
- Run: **"ERROR - Delete Non-existent Restaurant"**
- Expected: HTTP 404 Not Found

### Test 4: Get Non-existent Restaurant
- Run: **"3. READ - Get Restaurant Not Found (404)"**
- Expected: HTTP 404 Not Found

---

## Sample Request/Response Examples

### CREATE Request
```json
POST /api/restaurants
Content-Type: application/json

{
  "name": "Burger King",
  "email": "contact@burgerking.com",
  "phoneNumber": "555-1234",
  "street": "789 Fast Food Blvd",
  "city": "Metropolis",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "description": "Fast food restaurant chain",
  "isActive": true
}
```

**Response (HTTP 201 Created):**
```json
{
  "id": 3,
  "name": "Burger King",
  "email": "contact@burgerking.com",
  "phoneNumber": "555-1234",
  "street": "789 Fast Food Blvd",
  "city": "Metropolis",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "description": "Fast food restaurant chain",
  "isActive": true,
  "owner": {
    "id": 1,
    "email": "maria.garcia@rocketfood.com",
    "firstName": "Maria",
    "lastName": "Garcia"
  },
  "createdAt": "2024-03-25T10:30:00",
  "updatedAt": "2024-03-25T10:30:00"
}
```

---

### READ All Request
```json
GET /api/restaurants?page=0&size=10
```

**Response (HTTP 200 OK):**
```json
{
  "content": [
    {
      "id": 3,
      "name": "Burger King",
      "email": "contact@burgerking.com",
      ...
    }
  ],
  "currentPage": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1,
  "isLast": true
}
```

---

### READ Single Request
```json
GET /api/restaurants/3
```

**Response (HTTP 200 OK):**
```json
{
  "id": 3,
  "name": "Burger King",
  "email": "contact@burgerking.com",
  ...
}
```

---

### UPDATE Request
```json
PUT /api/restaurants/3
Content-Type: application/json

{
  "name": "Burger King Premium",
  "email": "premium@burgerking.com",
  "phoneNumber": "555-9999",
  "street": "999 Premium Avenue",
  "city": "New York",
  "state": "NY",
  "zipCode": "10010",
  "country": "USA",
  "description": "Premium fast food restaurant chain",
  "isActive": true
}
```

**Response (HTTP 200 OK):**
```json
{
  "id": 3,
  "name": "Burger King Premium",
  "email": "premium@burgerking.com",
  ...
}
```

---

### DELETE Request
```json
DELETE /api/restaurants/3
```

**Response (HTTP 204 No Content):**
```
(empty body)
```

---

## HTTP Status Codes Reference

| Code | Meaning | When |
|------|---------|------|
| **200** | OK | Successful GET or PUT request |
| **201** | Created | Successful POST request |
| **204** | No Content | Successful DELETE request |
| **400** | Bad Request | Validation error (invalid data) |
| **404** | Not Found | Restaurant doesn't exist |
| **500** | Server Error | Unexpected server error |

---

## Tips for Testing

1. **Copy IDs Carefully** - When updating or deleting, replace the ID in the URL with the actual ID from your created restaurant

2. **Watch the Response** - Always check the HTTP status code and response body to understand what happened

3. **Test Order Matters** - Follow the sequence: Create → Read → Update → Delete → Verify

4. **Pagination** - The GET all endpoint supports `?page=0&size=10` parameters to control pagination

5. **Database State** - Each DELETE operation removes data permanently until you create new records

6. **Validation** - The API validates:
   - Restaurant name cannot be empty or duplicate
   - Email format must be valid
   - Phone number must be 10-20 characters
   - Required fields: name, email, phoneNumber, zipCode, country

---

## Troubleshooting

### "Connection Refused" Error
- Ensure Spring Boot application is running
- Check that port 8080 is not blocked/used
- Start app with: `./mvnw spring-boot:run`

### "404 Not Found" for Existing Restaurant
- Double-check the restaurant ID is correct
- Restaurant may have been deleted or database was reset
- Create a new restaurant and try again

### "400 Bad Request" Validation Error
- Check JSON syntax in request body
- Verify required fields are provided
- See validation rules above

### Empty Response on DELETE
- This is correct! DELETE returns HTTP 204 No Content
- Use GET to verify the restaurant was deleted

---

## Next Steps

After testing the API:
1. Integrate with frontend application
2. Add authentication/authorization headers
3. Create additional controller DTOs for larger payloads
4. Add rate limiting and caching
5. Document API with Swagger/OpenAPI

---

## Additional Resources

- [RocketDelivery Project README](./readme.md)
- [Back Office Implementation Report](./BACK_OFFICE_IMPLEMENTATION_REPORT.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Postman Documentation](https://learning.postman.com/)
