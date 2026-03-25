# 🤖 AI_FEATURE_BACK_OFFICE_FRONTEND

This document describes the **Back Office Frontend** feature — the Thymeleaf HTML templates, forms, and user interface for restaurant management in the RocketDelivery Back Office system.

---

## Feature Identity

- **Feature Name:** Back Office Frontend
- **Related Area:** Frontend UI / View Layer (Thymeleaf Templates)
- **Companion Feature:** AI_FEATURE_BACK_OFFICE_BACKEND (Controller/Service/Repository layer)

---

## Feature Goal

Create a user-friendly web interface for restaurant staff to manage restaurants through CRUD operations. The interface should:
- Display a list of all restaurants in a clean table format
- Allow staff to create new restaurant records via a form
- Allow staff to update existing restaurant details
- Allow staff to delete restaurants with confirmation
- Provide clear feedback (success/error messages) after operations
- Persist all changes to the MySQL database

---

## Feature Scope

### In Scope (Included)

**Pages/Views (Thymeleaf Templates):**
- Restaurant List Page (`index.html`) — View all restaurants in table format
- Create Restaurant Page (`create.html`) — Form to add new restaurant
- Edit Restaurant Page (`edit.html`) — Form to update restaurant details
- Shared Form Component (`form.html`) — Reusable restaurant form fragment
- Error Page (`error.html`) — Display application errors

**Frontend Features:**
- HTML table with restaurant data display
- Interactive form for creating/editing restaurants
- Form validation feedback (client-side hints + server-side messages)
- Success/error notification banners
- Delete confirmation modal or button
- Navigation between pages (links/buttons)
- Basic CSS styling (grid layout, spacing, buttons, tables)
- Responsive form fields
- Accessibility features (labels, alt text, semantic HTML)

**User Interactions:**
- View list of all restaurants
- Click "New Restaurant" to access create form
- Fill form fields and submit to create restaurant
- Click "Edit" on table row to modify restaurant
- Edit form pre-fills with existing data
- Modify fields and submit to update
- Click "Delete" to remove restaurant
- Confirm deletion before processing
- See success message after create/update/delete
- See error messages for validation failures

**Thymeleaf Integration:**
- Use th:field for two-way form binding
- Use th:errors for validation error display
- Use th:each for restaurant list iteration
- Use th:if for conditional rendering (empty list, error states)
- Use th:text for dynamic content rendering
- Use csrf:token for form security
- Use th:href for dynamic link generation

### Out of Scope (Excluded)

- User authentication/login page
- Authorization/permissions checks (frontend-level)
- Advanced filtering, sorting, or search
- Pagination
- Bulk operations (multi-select, bulk delete)
- Real-time updates or live notifications
- PDF export or reporting
- Advanced styling frameworks (Bootstrap, Tailwind)
- Frontend build process or minification
- Client-side form validation (JavaScript)
- Internationalization/localization
- Dark mode or theme switching
- Mobile-optimized responsive design
- Image uploads for restaurant branding
- Integration with external services

---

## Sub-Requirements (Feature Breakdown)

- **Restaurant List Page** — Display all restaurants in sortable table with action buttons
- **Create Restaurant Form** — Accept new restaurant data with validation feedback
- **Edit Restaurant Form** — Pre-fill and allow modification of existing restaurant data
- **Shared Form Template** — Reusable form fragment for create and edit pages
- **Form Validation Display** — Show server-side and client-side validation errors clearly
- **Success Messages** — Display confirmation after successful create/update/delete
- **Error Messages** — Display user-friendly error messages for failed operations
- **Delete Confirmation** — Ask user to confirm before deleting restaurant
- **Navigation** — Links to move between list, create, edit pages
- **Styling & Layout** — Clean, organized CSS for readability and usability
- **Semantic HTML** — Proper use of HTML tags for accessibility
- **CSRF Protection** — Include CSRF tokens in forms for security
- **Mobile Considerations** — Basic responsive design for smaller screens

---

## User Flow / Logic (High Level)

### Read Flow (List Restaurants)

1. User clicks bookmark or navigates to `/backoffice/restaurants`
2. Browser sends GET request to controller
3. Controller fetches all restaurants from service
4. Thymeleaf renders `index.html` with restaurant data
5. User sees table with all restaurants and action buttons
6. Column headers: Name, Email, Phone, City, Country, Opening Time, Closing Time, Actions
7. Action buttons: [Edit] [Delete] [View Details]

### Create Flow

1. User clicks [New Restaurant] button on list page
2. Browser navigates to `/backoffice/restaurants/new`
3. Controller returns empty form via `create.html`
4. User fills form fields (name, email, phone, address, times, description)
5. User clicks [Save] button
6. Form submits POST request to `/backoffice/restaurants`
7. Server validates data; if invalid, redisplays form with error messages
8. If valid, saves restaurant and redirects to `/backoffice/restaurants` (list page)
9. User sees success message and restaurant appears in table

### Update Flow

1. User clicks [Edit] button on table row
2. Browser navigates to `/backoffice/restaurants/{id}/edit`
3. Controller fetches restaurant by ID and pre-fills form via `edit.html`
4. User modifies fields
5. User clicks [Update] button
6. Form submits POST request to `/backoffice/restaurants/{id}` or PUT
7. Server validates data; if invalid, redisplays form with existing data + error messages
8. If valid, updates restaurant and redirects to list page
9. User sees success message; updated data visible in table

### Delete Flow

1. User clicks [Delete] button on table row
2. Browser shows confirmation dialog (JavaScript confirm or form submission)
3. User confirms deletion
4. Form submits POST request to `/backoffice/restaurants/{id}/delete` or DELETE
5. Server deletes restaurant from database
6. Browser redirects to `/backoffice/restaurants` (list page)
7. User sees success message; restaurant removed from table

---

## Interfaces (Pages/Components/Screens)

### Pages/Templates

#### 1. Restaurant List Page (`templates/backoffice/restaurants/index.html`)

**Purpose:** Display all restaurants in table format

**Key Elements:**
- Page title: "Restaurants Management"
- [New Restaurant] button (link to create form)
- Success/error message area (if applicable)
- Table with columns:
  - Restaurant Name (th:text="${restaurant.name}")
  - Email (th:text="${restaurant.email}")
  - Phone (th:text="${restaurant.phoneNumber}")
  - City (th:text="${restaurant.city}")
  - Country (th:text="${restaurant.country}")
  - Opening Time (th:text="${restaurant.openingTime}")
  - Closing Time (th:text="${restaurant.closingTime}")
  - Actions ([Edit] [Delete] buttons)
- Empty state message if no restaurants exist

**Thymeleaf Snippets:**
```html
<table>
  <thead>
    <tr>
      <th>Name</th>
      <th>Email</th>
      <th>Phone</th>
      <th>City</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="restaurant : ${restaurants}">
      <td th:text="${restaurant.name}"></td>
      <td th:text="${restaurant.email}"></td>
      <td th:text="${restaurant.phoneNumber}"></td>
      <td th:text="${restaurant.city}"></td>
      <td>
        <a th:href="@{/backoffice/restaurants/{id}/edit(id=${restaurant.id})}">Edit</a>
        <a th:href="@{/backoffice/restaurants/{id}/delete(id=${restaurant.id})}">Delete</a>
      </td>
    </tr>
  </tbody>
</table>

<div th:if="${#lists.isEmpty(restaurants)}" class="empty-state">
  <p>No restaurants found. <a th:href="@{/backoffice/restaurants/new}">Create one</a></p>
</div>
```

**Styling:** Clean table with alternating row colors, hover effects on buttons

---

#### 2. Create Restaurant Form Page (`templates/backoffice/restaurants/create.html`)

**Purpose:** Display empty form to create new restaurant

**Key Elements:**
- Page title: "Create New Restaurant"
- Form with all restaurant fields (empty)
- [Save] and [Cancel] buttons
- No pre-filled data
- Link or button back to list page

**Thymeleaf Snippets:**
```html
<h1>Create New Restaurant</h1>
<form th:action="@{/backoffice/restaurants}" method="post" th:object="${restaurant}">
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
  
  <div class="form-group">
    <label for="name">Restaurant Name:</label>
    <input type="text" id="name" th:field="*{name}" placeholder="Enter restaurant name" />
    <span th:errors="*{name}" class="error"></span>
  </div>
  
  <div class="form-group">
    <label for="email">Email:</label>
    <input type="email" id="email" th:field="*{email}" placeholder="Enter email" />
    <span th:errors="*{email}" class="error"></span>
  </div>
  
  <!-- Other fields... -->
  
  <button type="submit" class="btn btn-primary">Save</button>
  <a th:href="@{/backoffice/restaurants}" class="btn btn-secondary">Cancel</a>
</form>
```

**Styling:** Clean form layout with grouped fields, clear labels, error highlighting

---

#### 3. Edit Restaurant Form Page (`templates/backoffice/restaurants/edit.html`)

**Purpose:** Display form with existing restaurant data for editing

**Key Elements:**
- Page title: "Edit Restaurant"
- Form with all restaurant fields (pre-filled with existing data)
- [Update] and [Cancel] buttons
- Data binding to existing restaurant object
- Display restaurant ID (read-only or hidden)

**Thymeleaf Snippets:**
```html
<h1>Edit Restaurant</h1>
<form th:action="@{/backoffice/restaurants/{id}(id=${restaurant.id})}" method="post" th:object="${restaurant}">
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
  
  <div class="form-group">
    <label for="name">Restaurant Name:</label>
    <input type="text" id="name" th:field="*{name}" />
    <span th:errors="*{name}" class="error"></span>
  </div>
  
  <!-- Other fields pre-filled via th:field binding... -->
  
  <button type="submit" class="btn btn-primary">Update</button>
  <a th:href="@{/backoffice/restaurants}" class="btn btn-secondary">Cancel</a>
</form>
```

**Key Difference:** Fields are pre-populated via th:field binding; user modifies and submits

---

#### 4. Shared Form Fragment (`templates/backoffice/restaurants/form.html`) (Optional)

**Purpose:** Reusable form fragment used by both create and edit pages

**Key Elements:**
- Form fragment with all input fields
- Can be included via th:insert in create.html and edit.html
- Reduces code duplication
- Same HTML markup for both create and edit

**Thymeleaf Snippets:**
```html
<div th:fragment="restaurantForm">
  <div class="form-group">
    <label for="name">Restaurant Name:</label>
    <input type="text" id="name" th:field="*{name}" placeholder="Enter restaurant name" />
    <span th:errors="*{name}" class="error"></span>
  </div>
  
  <div class="form-group">
    <label for="email">Email:</label>
    <input type="email" id="email" th:field="*{email}" />
    <span th:errors="*{email}" class="error"></span>
  </div>
  
  <!-- All fields here... -->
</div>
```

---

#### 5. Error Page (`templates/error.html`) (Optional)

**Purpose:** Display application errors

**Key Elements:**
- Error message (th:text="${message}")
- Stack trace (if in development)
- Link back to home/list page
- Styling to indicate error state

**Thymeleaf Snippets:**
```html
<div class="error-container">
  <h1>Oops! An Error Occurred</h1>
  <p th:text="${message}"></p>
  <a th:href="@{/backoffice/restaurants}">Back to Restaurants</a>
</div>
```

---

### Form Fields (All Pages)

**Input Fields to Include:**

```html
<!-- Name -->
<div class="form-group">
  <label for="name">Restaurant Name *</label>
  <input type="text" id="name" th:field="*{name}" required />
  <span th:errors="*{name}" class="error-message"></span>
</div>

<!-- Email -->
<div class="form-group">
  <label for="email">Email *</label>
  <input type="email" id="email" th:field="*{email}" required />
  <span th:errors="*{email}" class="error-message"></span>
</div>

<!-- Phone Number -->
<div class="form-group">
  <label for="phoneNumber">Phone Number *</label>
  <input type="tel" id="phoneNumber" th:field="*{phoneNumber}" required />
  <span th:errors="*{phoneNumber}" class="error-message"></span>
</div>

<!-- Street -->
<div class="form-group">
  <label for="street">Street Address *</label>
  <input type="text" id="street" th:field="*{street}" required />
  <span th:errors="*{street}" class="error-message"></span>
</div>

<!-- City -->
<div class="form-group">
  <label for="city">City *</label>
  <input type="text" id="city" th:field="*{city}" required />
  <span th:errors="*{city}" class="error-message"></span>
</div>

<!-- Postal Code -->
<div class="form-group">
  <label for="postalCode">Postal Code *</label>
  <input type="text" id="postalCode" th:field="*{postalCode}" required />
  <span th:errors="*{postalCode}" class="error-message"></span>
</div>

<!-- Country -->
<div class="form-group">
  <label for="country">Country *</label>
  <input type="text" id="country" th:field="*{country}" required />
  <span th:errors="*{country}" class="error-message"></span>
</div>

<!-- Opening Time -->
<div class="form-group">
  <label for="openingTime">Opening Time *</label>
  <input type="time" id="openingTime" th:field="*{openingTime}" required />
  <span th:errors="*{openingTime}" class="error-message"></span>
</div>

<!-- Closing Time -->
<div class="form-group">
  <label for="closingTime">Closing Time *</label>
  <input type="time" id="closingTime" th:field="*{closingTime}" required />
  <span th:errors="*{closingTime}" class="error-message"></span>
</div>

<!-- Description (Optional) -->
<div class="form-group">
  <label for="description">Description</label>
  <textarea id="description" th:field="*{description}" rows="4"></textarea>
  <span th:errors="*{description}" class="error-message"></span>
</div>
```

---

## Data & Validations

### Form Data Binding

**Thymeleaf th:field Binding:**
- `th:field="*{name}"` → Binds to Restaurant object's `name` property
- Renders both `id`, `name`, and `value` attributes automatically
- Populates with existing value on edit (from Spring model)
- Empty on create

**CSRF Protection:**
```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
```

### Validation Rules

| Field | Type | Size | Required | Notes |
|-------|------|------|----------|-------|
| name | String | 3-255 | Yes | UNIQUE in database |
| email | String | Valid Email | Yes | UNIQUE in database |
| phoneNumber | String | 10-20 | Yes | User-friendly format |
| street | String | 5-255 | Yes | Full street address |
| city | String | 2-100 | Yes | City name |
| postalCode | String | 3-20 | Yes | Flexible format |
| country | String | 2-100 | Yes | Country name |
| openingTime | LocalTime | HH:mm | Yes | Must be valid time |
| closingTime | LocalTime | HH:mm | Yes | Must be valid time |
| description | String | 0-1000 | No | Optional rich description |

### Validation Error Display

**Thymeleaf Error Rendering:**
```html
<span th:errors="*{name}" class="error-message"></span>
```

**Example Error Messages:**
- "Restaurant name must be between 3 and 255 characters"
- "A restaurant with this email already exists"
- "Please enter a valid email address"
- "Phone number must be between 10 and 20 characters"
- "Please select a valid opening time"

### Expected Behavior

**Create Form:**
- Submit empty or incomplete form → Show validation errors
- Submit with existing email → Show "Email already exists" error
- Submit with valid data → Success, redirect to list, show "Restaurant created successfully"

**Edit Form:**
- Submit with valid changes → Success, redirect to list, show "Restaurant updated successfully"
- Submit with email from different restaurant → Show duplicate email error
- Submit with existing email (same restaurant) → Allow (no duplicate)
- Cancel → Return to list without saving

**Delete:**
- Click delete → Show confirmation (modal or inline message)
- Confirm → Remove from table, database, show "Restaurant deleted successfully"
- Cancel/decline → Return to list unchanged

---

## Acceptance Criteria

### Page Layout & Navigation

- [ ] Restaurant list page displays at `/backoffice/restaurants`
- [ ] List page shows table with all restaurants
- [ ] Table has columns: Name, Email, Phone, City, Country, Opening Time, Closing Time, Actions
- [ ] List page has [New Restaurant] button visible
- [ ] [New Restaurant] button links to create form page
- [ ] Create form page displays at `/backoffice/restaurants/new`
- [ ] Edit form page displays at `/backoffice/restaurants/{id}/edit`
- [ ] Edit form URL includes restaurant ID
- [ ] All pages have back-to-list link available
- [ ] Navigation between pages works without errors

### Restaurant List Page (Read)

- [ ] List page loads successfully on app startup
- [ ] All restaurants from database display in table
- [ ] Table rows display correct restaurant data (name, email, city, etc.)
- [ ] Each table row has [Edit] and [Delete] action buttons
- [ ] Empty state message displays when no restaurants exist
- [ ] Table updates immediately after create/update/delete operations
- [ ] No stale or cached data in list
- [ ] Timestamps display in human-readable format (if shown)

### Create Restaurant Form (Create)

- [ ] Create form page loads without errors
- [ ] Form has all 10 input fields visible
- [ ] All required fields marked with asterisk (*)
- [ ] Input types correct: text, email, tel, time, textarea
- [ ] Form has [Save] and [Cancel] buttons
- [ ] Cancel button returns to list without saving
- [ ] Form submission sends POST to `/backoffice/restaurants`
- [ ] CSRF token included in form
- [ ] Form can be submitted successfully with valid data
- [ ] New restaurant saved to database
- [ ] Redirect to list page after successful create
- [ ] Success message displays ("Restaurant created successfully")
- [ ] New restaurant appears in list table

### Edit Restaurant Form (Update)

- [ ] Edit form page loads without errors
- [ ] Form pre-fills with existing restaurant data
- [ ] All fields display current restaurant values
- [ ] Form can be modified
- [ ] Form submission sends POST/PUT to `/backoffice/restaurants/{id}`
- [ ] CSRF token included in form
- [ ] Form can be submitted successfully with modified data
- [ ] Changes saved to database
- [ ] Redirect to list page after successful update
- [ ] Success message displays ("Restaurant updated successfully")
- [ ] Updated data reflects in list table without refresh

### Delete Restaurant (Delete)

- [ ] [Delete] button visible on each table row
- [ ] Delete button action triggers confirmation
- [ ] Confirmation prevents accidental deletion (modal or confirm dialog)
- [ ] Confirmed delete sends DELETE/POST to `/backoffice/restaurants/{id}/delete`
- [ ] Restaurant removed from database
- [ ] Redirect to list page after delete
- [ ] Success message displays ("Restaurant deleted successfully")
- [ ] Deleted restaurant removed from list immediately

### Form Validation & Error Handling

- [ ] Submit empty form shows validation errors
- [ ] Submit incomplete form shows errors for missing required fields
- [ ] Each error displays next to corresponding field
- [ ] Error messages are user-friendly and clear
- [ ] Submit with invalid email format shows email error
- [ ] Submit with email < 3 or > 255 chars shows error
- [ ] Submit with non-time format in time fields shows error
- [ ] Submit with existing email (duplicate) shows error
- [ ] Form redisplays with user-entered data preserved on error
- [ ] Fix errors and resubmit succeeds
- [ ] Success message displays after valid submission

### Data Persistence

- [ ] Create operation persists restaurant to MySQL database
- [ ] Verify new restaurant in database with DBeaver
- [ ] Update operation persists changes to database
- [ ] Verify updated data in database with DBeaver
- [ ] Delete operation removes restaurant from database
- [ ] Verify deletion in database with DBeaver
- [ ] Page refresh shows persisted data (no loss on reload)
- [ ] Database constraints enforced (UNIQUE email/name)

### Thymeleaf Integration

- [ ] All restaurant objects render correctly with th:text
- [ ] List iteration works with th:each
- [ ] Form fields bind correctly with th:field
- [ ] Validation errors display with th:errors
- [ ] Conditional rendering works (th:if for empty state)
- [ ] Links generate correctly with th:href and @{} syntax
- [ ] CSRF token renders correctly in forms
- [ ] No Thymeleaf syntax errors in console

### User Experience

- [ ] Forms are easy to understand with clear labels
- [ ] Buttons are clearly labeled (Save, Update, Delete, Cancel)
- [ ] Success messages confirm operations completed
- [ ] Error messages explain what went wrong
- [ ] Delete requires confirmation before action
- [ ] Form doesn't lose data on validation errors
- [ ] No console JavaScript errors during interactions
- [ ] Page loads quickly without lag
- [ ] All interactions are responsive

### Styling & Layout

- [ ] CSS file exists and loads correctly
- [ ] Table has clear, readable formatting
- [ ] Form fields organized in logical groups
- [ ] Buttons styled distinctly (primary vs secondary)
- [ ] Error messages styled in red or warning color
- [ ] Success messages styled in green or success color
- [ ] Form inputs have appropriate spacing
- [ ] Page has consistent styling across all templates
- [ ] Text is readable (good contrast, font size)

### Browser & Compatibility

- [ ] Pages work in Chrome/Firefox/Safari/Edge
- [ ] Forms submit correctly across browsers
- [ ] Time input fields work in all browsers
- [ ] No browser console errors
- [ ] Thymeleaf renders correctly for all browsers
- [ ] CSRF protection works across browsers

### Security

- [ ] CSRF tokens present in all forms
- [ ] No credentials or sensitive data in HTML source
- [ ] Form validation prevents malicious input
- [ ] SQL injection prevented (via JPA prepared statements)
- [ ] XSS prevention via Thymeleaf autoescaping

---

## Notes for the AI

### Thymeleaf Syntax Reference

```html
<!-- Expression Variables -->
${variable}

<!-- Selection Variables (form binding) -->
*{property}

<!-- Links -->
@{/path/to/resource}
@{/path/{id}/edit(id=${obj.id})}

<!-- Common Attributes -->
th:field="*{name}" <!-- Two-way binding -->
th:text="${value}" <!-- Set text content -->
th:value="${value}" <!-- Set input value -->
th:href="@{/path}" <!-- Set link href -->
th:action="@{/submit/path}" <!-- Set form action -->
th:each="item : ${items}" <!-- Iterate list -->
th:if="${condition}" <!-- Conditional -->
th:errors="*{field}" <!-- Display validation errors -->

<!-- CSRF Token -->
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}" />
```

### Form Fragment Reuse

Instead of duplicating form HTML in create.html and edit.html:

**form.html:**
```html
<div th:fragment="restaurantForm">
  <!-- All form fields here -->
</div>
```

**create.html:**
```html
<form th:action="@{/backoffice/restaurants}" method="post" th:object="${restaurant}">
  <div th:insert="~{backoffice/restaurants/form :: restaurantForm}"></div>
</form>
```

**edit.html:**
```html
<form th:action="@{/backoffice/restaurants/{id}(id=${restaurant.id})}" method="post" th:object="${restaurant}">
  <div th:insert="~{backoffice/restaurants/form :: restaurantForm}"></div>
</form>
```

### CSS Styling Strategy

Keep styling minimal and focused on layout/readability:

```css
/* Form Layout */
.form-group {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
}

/* Error Styling */
.error-message {
  color: #d32f2f;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

/* Button Styling */
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  margin-right: 0.5rem;
}

.btn-primary {
  background-color: #1976d2;
  color: white;
}

.btn-secondary {
  background-color: #757575;
  color: white;
}

/* Table Styling */
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 1rem;
}

table th {
  background-color: #f5f5f5;
  padding: 1rem;
  text-align: left;
  font-weight: bold;
}

table td {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

table tr:hover {
  background-color: #f5f5f5;
}

/* Message Styling */
.success-message {
  background-color: #c8e6c9;
  color: #2e7d32;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}

.error-alert {
  background-color: #ffcdd2;
  color: #c62828;
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}
```

### Delete Confirmation Options

**Option 1: JavaScript confirm() — Simple, no extra HTML**
```html
<a th:href="@{/backoffice/restaurants/{id}/delete(id=${restaurant.id})}" 
   onclick="return confirm('Are you sure you want to delete this restaurant?');">Delete</a>
```

**Option 2: Modal — More polished but requires JavaScript**
```html
<!-- In your CSS/JavaScript -->
<div id="deleteConfirmModal" class="modal">
  <p>Are you sure you want to delete this restaurant?</p>
  <button onclick="confirmDelete()">Confirm</button>
  <button onclick="cancelDelete()">Cancel</button>
</div>
```

**Option 3: Inline Form — No JavaScript required**
```html
<form th:action="@{/backoffice/restaurants/{id}/delete(id=${restaurant.id})}" method="post" style="display:inline;">
  <button type="submit" onclick="return confirm('Delete this restaurant?');">Delete</button>
</form>
```

### Accessibility Best Practices

- Always include explicit `<label>` for form inputs
- Use semantic HTML (`<table>`, `<button>`, `<form>`)
- Include `alt` text for images (if any)
- Use proper heading hierarchy (h1, h2, h3)
- Provide descriptive error messages
- Color shouldn't be the only indicator (use text too)
- Ensure keyboard navigation works
- Use ARIA attributes if needed

### Time Input Considerations

HTML `<input type="time">` support:
- Modern browsers: Full support (Chrome, Firefox, Safari 14.1+, Edge)
- Older browsers: Falls back to text input; users enter HH:mm manually
- Format: Always 24-hour (use `th:field="*{openingTime}"` for LocalTime binding)

**Backend TimeFormatter (if needed in application.properties):**
```properties
spring.jpa.properties.hibernate.format_sql=true
spring.jackson.time-zone=UTC
```

### Form Submission Methods

**POST for create (new record):**
```html
<form th:action="@{/backoffice/restaurants}" method="post">
```

**POST for update (existing record) with method override:**
```html
<form th:action="@{/backoffice/restaurants/{id}(id=${restaurant.id})}" method="post">
  <input type="hidden" name="_method" value="PUT" />
</form>
```

Or **POST to different endpoints:**
```html
<!-- Create -->
<form th:action="@{/backoffice/restaurants}" method="post">

<!-- Update -->
<form th:action="@{/backoffice/restaurants/{id}(id=${restaurant.id})}" method="post">

<!-- Delete -->
<form th:action="@{/backoffice/restaurants/{id}/delete(id=${restaurant.id})}" method="post">
```

---

## Implementation Checklist

### Phase 1: Template Setup

- [ ] Create directory structure: `src/main/resources/templates/backoffice/restaurants/`
- [ ] Create `layout.html` (optional base layout template)
- [ ] Create `index.html` (list page)
- [ ] Create `create.html` (create form)
- [ ] Create `edit.html` (edit form)
- [ ] Create `form.html` fragment (reusable form)
- [ ] Create `error.html` (error page)

### Phase 2: List Page (index.html)

- [ ] Add page title and heading
- [ ] Add [New Restaurant] button
- [ ] Add success/error message area
- [ ] Create HTML table with headers
- [ ] Add th:each loop for restaurant iteration
- [ ] Add th:text bindings for data display
- [ ] Add [Edit] and [Delete] buttons with th:href
- [ ] Add empty state (th:if checking restaurants list)
- [ ] Add basic CSS for table styling

### Phase 3: Create Form (create.html)

- [ ] Add form with th:action="@{/backoffice/restaurants}" method="post"
- [ ] Add th:object="${restaurant}"
- [ ] Add CSRF token field
- [ ] Add all 10 input fields with labels
- [ ] Add th:field bindings for each field
- [ ] Add th:errors for each field
- [ ] Add [Save] and [Cancel] buttons
- [ ] Add CSS for form styling

### Phase 4: Edit Form (edit.html)

- [ ] Copy create.html as starting point
- [ ] Change th:action to @{/backoffice/restaurants/{id}(id=${restaurant.id})}
- [ ] Change title to "Edit Restaurant"
- [ ] Change [Save] button text to [Update]
- [ ] Test with pre-filled data

### Phase 5: Form Fragment (form.html)

- [ ] Move all form field HTML to form.html fragment
- [ ] Define th:fragment="restaurantForm"
- [ ] Update create.html and edit.html to use th:insert

### Phase 6: Styling (CSS)

- [ ] Create `static/css/style.css` (or link in layout)
- [ ] Add form styling (groups, spacing, fonts)
- [ ] Add button styling (primary, secondary colors)
- [ ] Add table styling (rows, headers, hover)
- [ ] Add error/success message styling
- [ ] Add responsive sizing

### Phase 7: Testing

- [ ] Load list page, verify table displays
- [ ] Click [New Restaurant], verify create form loads
- [ ] Submit create form with valid data, verify success
- [ ] Verify new restaurant in database (DBeaver)
- [ ] Verify new restaurant in list (refresh)
- [ ] Click [Edit], verify form pre-fills
- [ ] Modify and submit, verify update in database
- [ ] Click [Delete], confirm deletion
- [ ] Verify deletion in database and list
- [ ] Test form validation (submit invalid data)
- [ ] Verify error messages display

---

## POC-Specific Notes

**Thymeleaf vs Frontend Frameworks:**
- This POC uses **server-side rendering** (Thymeleaf) not frontend frameworks
- All HTML generated on backend; frontend is purely presentational
- Simpler to understand for beginners; fewer moving parts
- Trades some interactivity for simplicity

**Form Handling Approach:**
- Traditional form submission (POST), **not AJAX**
- Page reloads on form submit
- Simpler but less modern than SPA patterns
- Suitable for POC and traditional MVC approach

**Styling Philosophy:**
- **Minimal CSS** to focus on functionality
- Clean, readable layout without heavy frameworks
- Bootstrap optional; use plain CSS for core POC
- Can add styling polish later

**No JavaScript:**
- Minimal JavaScript usage in POC
- Delete confirmation via `confirm()` dialog (no modal)
- Form validation primarily server-side
- Future POC phases can add client-side enhancements

**Database Persistence Verification:**
- After each CRUD operation, verify in DBeaver
- Ensures data truly persists (not just in memory)
- Database connection and entity mapping working
- Critical for proving backend is functioning

**Error Handling:**
- Server-side validation via Spring @Valid
- Validation errors display on same form page
- User-friendly error messages (not stack traces)
- Form data preserved on errors for user convenience

