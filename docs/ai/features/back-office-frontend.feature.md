# Back Office Frontend Feature Specification

**Status:** Ready for Implementation  
**Companion Feature:** Back Office Backend (already implemented)

---

## Feature Overview

Create a user-friendly Thymeleaf-based web interface for restaurant management. This frontend works seamlessly with the Back Office Backend controller and service layers to provide complete CRUD operations for restaurant staff.

---

## Feature Goals

✅ Display restaurants in a clean, readable list format
✅ Create new restaurants via intuitive forms
✅ Edit restaurant details with pre-filled data
✅ Delete restaurants with confirmation
✅ Provide clear success/error feedback to users
✅ Persist all changes to MySQL database
✅ Ensure accessibility and responsive design

---

## Scope

### In Scope ✅

**Thymeleaf Templates (5 HTML Views):**
- `index.html` — Restaurant list with table, statistics, action buttons
- `create.html` — Create restaurant form (empty form)
- `edit.html` — Edit restaurant form (pre-filled data)
- `form.html` — Shared form fragment (optional, for DRY principle)
- `error.html` — Application error page (optional)

**Frontend Features:**
- HTML table display with restaurant data
- Interactive forms for create/edit operations
- Form binding using Thymeleaf `th:field`
- Validation error display using `th:errors`
- Server-side validation feedback
- Success/error notification messages
- Delete confirmation dialog/button
- Navigation between pages (links, buttons)
- Basic CSS styling (layout, colors, spacing, buttons)
- Responsive form fields
- Semantic HTML for accessibility
- CSRF token protection on forms
- Empty state messaging
- Status badges and formatting

**User Interactions:**
- View all restaurants in table format
- Click "Create New Restaurant" to access form
- Fill form and submit new restaurant
- See validation errors for invalid input
- Click "Edit" to access edit form
- Form pre-fills with existing data
- Modify fields and submit updates
- Click "Delete" with confirmation
- See success messages after operations
- Navigate between pages seamlessly

### Out of Scope ❌

- User authentication or login page
- Authorization/permission checks
- Advanced filtering, searching, or sorting
- Pagination
- Bulk operations
- Real-time updates
- PDF export or reporting
- Advanced CSS frameworks (Bootstrap, Tailwind)
- Client-side form validation (JavaScript)
- Internationalization
- Dark mode or theme switching
- Mobile-optimized responsive design
- Image uploads
- Third-party service integration

---

## Template Structure

### Directory Layout

```
src/main/resources/templates/
├── backoffice/
│   └── restaurants/
│       ├── index.html          (List all restaurants)
│       ├── create.html         (Create form)
│       ├── edit.html           (Edit form)
│       └── form.html           (Shared fragment - optional)
└── error.html                   (Error page - optional)
```

### Templates to Create

#### 1. index.html (List View)

**Purpose:** Display all restaurants in table format with management actions

**Key Sections:**
- Header with page title and "Create New Restaurant" button
- Statistics bar showing restaurant count and status
- Flash message area (success/error notifications)
- Restaurant data table
  - Columns: Name, Email, Phone, City, Status, Created Date, Actions
  - Each row has Edit and Delete buttons
  - Status badge (Active/Inactive)
- Empty state message (when no restaurants)
- Footer with copyright/system info

**Thymeleaf Features Used:**
- `th:each` — Iterate over restaurants list
- `th:text` — Display restaurant data
- `th:if` — Conditional rendering (empty state)
- `th:href` — Generate dynamic edit/delete links
- `${successMessage}` — Display success notifications
- `${errorMessage}` — Display error notifications
- `${restaurants}` — Model attribute with list of restaurants

**Styling Requirements:**
- Clean table with header styling
- Hover effects on table rows
- Action buttons with distinct colors (Edit=Green, Delete=Red)
- Statistics bar with background color
- Responsive button layout
- Clear typography hierarchy

#### 2. create.html (Create Form)

**Purpose:** Display empty form to create new restaurant

**Key Sections:**
- Header with page title ("Create New Restaurant")
- Help text explaining the form
- Form with all restaurant fields
  - Name (required)
  - Email (required)
  - Phone Number (required)
  - Street Address (required)
  - City (required)
  - Postal Code (required)
  - Country (required)
  - Opening Time (required)
  - Closing Time (required)
  - Description (optional)
- Form validation error display
- Submit and Cancel buttons
- Cancel link back to list

**Thymeleaf Features Used:**
- `th:object="${restaurant}"` — Bind form to object
- `th:field="*{name}"` — Bind input to property
- `th:errors="*{name}"` — Display validation errors
- `th:action="@{/backoffice/restaurants}"` — Form action URL
- `${_csrf.token}` — CSRF protection token

**Validation Error Display:**
- Error message displayed below each invalid field
- Field border highlighted in red
- Error message color in red
- Multiple errors if field has multiple violations

**Styling Requirements:**
- Form layout with clear field grouping
- Labels associated with inputs
- Required field indicator (asterisk or text)
- Help text for user guidance
- Error message styling (red, prominent)
- Form buttons at bottom (Submit primary, Cancel secondary)

#### 3. edit.html (Edit Form)

**Purpose:** Display form with pre-filled restaurant data for editing

**Key Sections:**
- Header with page title ("Edit Restaurant")
- Restaurant metadata display (ID, created date, updated date, status)
- Form with all fields (pre-filled with current data)
- Form validation error display
- Submit and Cancel buttons
- Delete button/link (with confirmation)
- Cancel link back to list

**Thymeleaf Features Used:**
- `th:object="${restaurant}"` — Bind form to existing object
- `th:field="*{name}"` — Automatically populates with current value
- `th:errors="*{name}"` — Display validation errors
- `th:action="@{/backoffice/restaurants/{id}(id=${restaurant.id})}"` — Dynamic URL with ID
- `${restaurant.id}` — Display read-only ID
- `${#dates.format(restaurant.createdAt, 'MMM dd, yyyy')}` — Format dates

**Key Difference from Create:**
- Form fields pre-populated with existing restaurant data
- Restaurant metadata section showing ID, created date, updated date
- Delete button prominently displayed
- Submit button text "Update" instead of "Save"

**Styling Requirements:**
- Same as create form
- Additional metadata section with read-only values
- Distinct Delete button styling (red background)

#### 4. form.html (Shared Fragment - Optional)

**Purpose:** Reusable form fragment to DRY principle (used by create.html and edit.html)

**Key Sections:**
- Form field `<div>` blocks (one per field)
- Input elements with `th:field` binding
- Error message `<span>` for each field
- Label elements with proper `for` attributes

**Thymeleaf Features Used:**
- `th:fragment="restaurantForm"` — Define reusable fragment
- Used in create.html: `<div th:insert="~{backoffice/restaurants/form :: restaurantForm}"></div>`
- Used in edit.html: Same insert syntax

**Benefit:**
- Single source of truth for form HTML
- Changes to form automatically apply to both create and edit
- Reduces code duplication

#### 5. error.html (Error Page - Optional)

**Purpose:** Display application errors

**Key Sections:**
- Error message display
- Stack trace (if in development mode)
- Link back to home/restaurants list
- Error styling (red, attention-grabbing)

**Thymeleaf Features Used:**
- `${message}` — Display error message
- `${exception}` — Display exception (dev mode)
- `th:href="@{/backoffice/restaurants}"` — Back link

---

## Form Validation & Error Display

### Restaurant Fields

| Field | Type | Required | Validation Rules | Error Message |
|-------|------|----------|------------------|----------------|
| name | String | Yes | 2-255 chars, unique | "Restaurant name must be between 2 and 255 characters" |
| email | String | Yes | Valid email, unique | "Please enter a valid email address" |
| phoneNumber | String | Yes | 10-20 chars | "Phone number must be between 10 and 20 characters" |
| street | String | Yes | 5-255 chars | "Street address must be between 5 and 255 characters" |
| city | String | Yes | 2-100 chars | "City must be between 2 and 100 characters" |
| zipCode | String | Yes | 3-20 chars | "Postal code must be between 3 and 20 characters" |
| country | String | Yes | 2-100 chars | "Country must be between 2 and 100 characters" |
| openingTime | LocalTime | Yes | Valid HH:mm | "Please enter a valid opening time" |
| closingTime | LocalTime | Yes | Valid HH:mm | "Please enter a valid closing time" |
| description | String | No | Max 1000 chars | "Description cannot exceed 1000 characters" |

### Validation Error Display

**HTML Structure:**
```html
<div class="form-group has-error">
    <label for="name">Restaurant Name <span class="required">*</span></label>
    <input type="text" id="name" th:field="*{name}" required>
    <span th:if="${#fields.hasErrors('name')}" th:errors="*{name}" class="error-message"></span>
</div>
```

**CSS Styling:**
- Error input border color: red (#dc3545 or similar)
- Error message color: red
- Error message font-size: smaller (12px)
- Error message display: block (new line)
- Form group class: `has-error` adds visual indicator

**Validation on Form Submit:**
1. Server-side validation runs (Jakarta Bean Validation)
2. If errors exist:
   - Form redisplays with user-entered data preserved
   - Error messages display below each invalid field
   - Field is highlighted/styled as error
   - Submit button disabled or shows error state
3. If no errors:
   - Data saved to database
   - Redirect to list page
   - Success message displayed

---

## Flash Messages & Notifications

### Success Messages

Displayed after CREATE, UPDATE, DELETE operations:
- "Restaurant 'Paradise Pizza' created successfully!"
- "Restaurant 'Paradise Pizza' updated successfully!"
- "Restaurant 'Paradise Pizza' deleted successfully!"

### Error Messages

Displayed for validation or operational errors:
- "Please correct the errors below"
- "A restaurant with this email already exists"
- "Error deleting restaurant: [reason]"
- "Restaurant not found"

### HTML Implementation

```html
<div th:if="${successMessage}" class="alert alert-success" th:text="${successMessage}"></div>
<div th:if="${errorMessage}" class="alert alert-error" th:text="${errorMessage}"></div>
```

### Styling

- Success: Green background (#d4edda), dark green text (#155724)
- Error: Red background (#f8d7da), dark red text (#721c24)
- Padding: 15px
- Border-left: 4px solid (green for success, red for error)
- Margin-bottom: 20px
- Border-radius: 4px

---

## CSS Styling Requirements

### General Layout

- Font: `'Segoe UI', Tahoma, Geneva, Verdana, sans-serif` or similar
- Background color: Light gray (#f5f5f5)
- Container max-width: 1200px (list), 600px (forms)
- Container background: White
- Padding: 20-30px
- Border-radius: 8px
- Box-shadow: Light shadow for depth

### Colors

- Primary: #007bff (Blue) — Primary buttons, links
- Success: #28a745 (Green) — Edit buttons, success messages
- Danger: #dc3545 (Red) — Delete buttons, error messages
- Secondary: #6c757d (Gray) — Secondary buttons, disabled states
- Text: #333 (Dark gray) — Primary text
- Muted: #666 (Medium gray) — Secondary text, help text

### Typography

- H1: 28px, bold, color #333
- H2: 24px, bold, color #333
- Labels: 14px, font-weight 500, color #333
- Input text: 14px, color #333
- Helper text: 12px, color #666
- Error message: 12px, color #dc3545, bold

### Components

**Buttons:**
- Padding: 10-12px 20px
- Border-radius: 4px
- Font-weight: 500
- Cursor: pointer
- Transition: 0.3s ease
- Hover: Darker shade of background color

**Form Fields:**
- Border: 1px solid #ddd
- Border-radius: 4px
- Padding: 10px 12px
- Font: inherit
- Focus: Blue border, subtle shadow

**Tables:**
- Border-collapse: collapse
- Header background: #f8f9fa
- Row hover: Light gray background
- Border: 1px solid #dee2e6

---

## Acceptance Criteria

### List Page (index.html) ✓

- [ ] Page loads at /backoffice/restaurants without errors
- [ ] Page displays all restaurants from database
- [ ] Table shows Name, Email, Phone, City, Status, Created, Actions columns
- [ ] Each row displays correct restaurant data
- [ ] Status badge shows Active/Inactive with appropriate colors
- [ ] Created date formatted as "MMM dd, yyyy" (e.g., "Mar 25, 2026")
- [ ] Edit button on each row links to /backoffice/restaurants/{id}/edit
- [ ] Delete button on each row links to /backoffice/restaurants/{id}/delete
- [ ] "Create New Restaurant" button links to /backoffice/restaurants/new
- [ ] Empty state message displays when no restaurants
- [ ] Success message displays after create/update/delete
- [ ] Error message displays if operation fails
- [ ] Table styling is clean and readable
- [ ] Page refreshes after operations without manual F5

### Create Form (create.html) ✓

- [ ] Page loads at /backoffice/restaurants/new without errors
- [ ] Page title displays "Create New Restaurant"
- [ ] All 10 form fields display: name, email, phone, street, city, postalCode, country, openingTime, closingTime, description
- [ ] Form inputs are empty (no default values)
- [ ] All required fields marked with asterisk (*)
- [ ] Input types correct: text, email, tel, time (for times), textarea (description)
- [ ] Form has [Create Restaurant] submit button
- [ ] Form has [Cancel] button linking back to list
- [ ] Form submits to POST /backoffice/restaurants
- [ ] CSRF token included in form
- [ ] Submit with empty form shows validation errors
- [ ] Submit with invalid email shows email error
- [ ] Errors display below corresponding fields in red color
- [ ] Form data preserved when errors occur (user can fix and resubmit)
- [ ] Submit with valid data creates restaurant and redirects to list
- [ ] Success message displays after creation
- [ ] New restaurant appears in list table

### Edit Form (edit.html) ✓

- [ ] Page loads at /backoffice/restaurants/{id}/edit without errors
- [ ] Page title displays "Edit Restaurant"
- [ ] Restaurant metadata section shows ID, created date, updated date, status
- [ ] All form fields pre-filled with current restaurant data
- [ ] Form inputs display existing values
- [ ] Form submits to POST /backoffice/restaurants/{id}
- [ ] CSRF token included in form
- [ ] [Update Restaurant] button submits changes
- [ ] [Cancel] button returns to list without saving
- [ ] [Delete] button visible and styled as danger button
- [ ] Submit with invalid field shows validation error
- [ ] Errors display below corresponding fields in red
- [ ] Form data preserved (shows current and user-entered values)
- [ ] Submit with valid changes updates database
- [ ] Redirect to list after successful update
- [ ] Success message displays after update
- [ ] Updated data reflects in list table

### Delete Operation ✓

- [ ] Delete link/button visible on list and edit pages
- [ ] Click delete triggers confirmation (JavaScript confirm or form)
- [ ] Confirmation message asks "Are you sure..."
- [ ] Confirm → Restaurant removed from database and list
- [ ] Cancel → Return to page unchanged
- [ ] Success message displays after delete
- [ ] Deleted restaurant no longer appears in list

### Form Validation ✓

- [ ] Submit empty form shows all required field errors
- [ ] Submit with missing name field shows "Restaurant name must be..."
- [ ] Submit with invalid email format shows email error
- [ ] Submit with short name (< 2 chars) shows length error
- [ ] Submit with duplicate email shows "Email already exists"
- [ ] Submit with invalid time format shows time error
- [ ] Error messages are user-friendly and clear
- [ ] Fixing errors and resubmitting succeeds
- [ ] Date/time fields accept valid HH:mm and MM/DD/YYYY format

### Thymeleaf Integration ✓

- [ ] Restaurant objects render with th:text (e.g., name, email) 
- [ ] List iteration works with th:each
- [ ] Form fields bind with th:field
- [ ] Validation errors display with th:errors
- [ ] Conditional rendering works (th:if for empty state)
- [ ] Links generate with th:href and @{} syntax
- [ ] CSRF tokens render in forms
- [ ] Dates format with #dates.format()
- [ ] Status badges conditional with th:classappend
- [ ] No Thymeleaf syntax errors in browser console

### Styling & UX ✓

- [ ] Forms have clear, readable layout
- [ ] All labels clearly associated with inputs
- [ ] Buttons clearly labeled and distinct
- [ ] Success messages visible and styled noticeably
- [ ] Error messages visible with red color
- [ ] Delete requires confirmation
- [ ] Table is easy to scan and read
- [ ] Page loads quickly
- [ ] No JavaScript errors in console

### Data Persistence ✓

- [ ] New restaurants persist to database
- [ ] Verify in database with MySQL client
- [ ] Updated restaurants persist to database
- [ ] Verify updates in database
- [ ] Deleted restaurants removed from database
- [ ] Page refresh shows persisted data
- [ ] No data loss on browser reload

---

## Development Notes

### Thymeleaf Syntax Reminders

**Form Binding:**
```html
<form th:action="@{/backoffice/restaurants}" th:object="${restaurant}" method="post">
    <input type="text" id="name" th:field="*{name}">
    <span th:if="${#fields.hasErrors('name')}" th:errors="*{name}"></span>
</form>
```

**Iteration:**
```html
<tr th:each="restaurant : ${restaurants}">
    <td th:text="${restaurant.name}"></td>
    <td th:text="${restaurant.email}"></td>
</tr>
```

**Conditional:**
```html
<div th:if="${restaurants.isEmpty()}">No restaurants found</div>
<div th:unless="${restaurants.isEmpty()}">
    <!-- Show list -->
</div>
```

**Links:**
```html
<a th:href="@{/backoffice/restaurants/{id}/edit(id=${restaurant.id})}">Edit</a>
```

**CSRF:**
```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
```

### CSS Structure

Keep CSS focused on:
- Layout and spacing
- Colors and typography
- Button and form styling
- Table styling
- Alerts and notifications
- Responsive adjustments

---

## Testing Strategy

### Manual Testing Checklist

**List Page:**
- [ ] Load http://localhost:8080/backoffice/restaurants
- [ ] All restaurants display in table
- [ ] Click "Create New Restaurant" → loads create form
- [ ] Click "Edit" → loads edit form with pre-filled data
- [ ] Click "Delete" → shows confirmation

**Create Form:**
- [ ] Load /backoffice/restaurants/new
- [ ] Submit empty form → shows validation errors
- [ ] Fix errors and submit → restaurant created, redirected to list
- [ ] Verify new restaurant in database

**Edit Form:**
- [ ] Click Edit on list → loads form with data
- [ ] Modify field and submit → restaurant updated, redirected to list
- [ ] Verify changes in database

**Delete:**
- [ ] Click Delete → confirmation prompt
- [ ] Confirm → restaurant removed, success message shown
- [ ] Verify deleted from database and list

### Browser Testing

- [ ] Chrome/Edge latest version
- [ ] Firefox latest version
- [ ] Safari (if available)
- [ ] Page responsiveness on smaller screens
- [ ] No JavaScript errors in browser console

### Database Verification

- [ ] Use MySQL client or DBeaver to verify data
- [ ] Check that constraints are enforced (unique email/name)
- [ ] Verify timestamps (createdAt, updatedAt)
- [ ] Confirm soft-delete isn't breaking anything

---

## Deliverables

### Templates to Create

1. `src/main/resources/templates/backoffice/restaurants/index.html` (320 lines)
2. `src/main/resources/templates/backoffice/restaurants/create.html` (280 lines)
3. `src/main/resources/templates/backoffice/restaurants/edit.html` (310 lines)
4. `src/main/resources/templates/backoffice/restaurants/form.html` (200 lines, optional)
5. `src/main/resources/templates/error.html` (100 lines, optional)

### CSS to Create

- `src/main/resources/static/css/backoffice.css` (or inline in templates)

**Total:** 1,210 lines of HTML/CSS (already created in backend feature)

---

## Success Metrics

✅ All 5 template files created and properly formatted
✅ All form validation errors display correctly
✅ CRUD operations work seamlessly
✅ Data persists to database
✅ User feedback messages clear and visible
✅ Thymeleaf syntax correct
✅ CSS styling clean and readable
✅ No compilation or runtime errors

---

## Timeline & Effort

**Estimated:** 1-2 hours

| Task | Duration | Status |
|------|----------|--------|
| Create index.html template | 30 min | Ready |
| Create create.html template | 30 min | Ready |
| Create edit.html template | 30 min | Ready |
| Style with CSS | 20 min | Ready |
| Test CRUD operations | 30 min | Ready |

---

## References & Resources

- [Thymeleaf Official Documentation](https://www.thymeleaf.org/)
- [Thymeleaf Form Binding](https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html#binding-thfields)
- [Thymeleaf Conditionals](https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#conditional-evaluation)
- [Spring Data HTML/Form Binding](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-view-jsp-form-form)
- [HTML5 Form Attributes](https://www.w3schools.com/html/html_forms.asp)
- [Web Accessibility (WCAG)](https://www.w3.org/WAI/fundamentals/)

