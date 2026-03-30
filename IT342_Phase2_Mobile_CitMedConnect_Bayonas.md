# IT342 Phase 2: Mobile Development Report
## CitMedConnect Android Application

**Student Name:** Jaylord Bayonas  
**Project:** CitMedConnect Mobile Client  
**Date:** March 30, 2026  
**Repository:** IT342-Bayonas-CitMedConnect  
**Final Commit:** a2f7d630a2f33dcbb8d11c49a721da96b6c49ebe

---

## Executive Summary

This report documents the successful development of the CitMedConnect mobile application for Android using Kotlin. The application provides a complete authentication system (registration and login) that seamlessly integrates with the Spring Boot backend built in Phase 1. The mobile client implements modern Android architectural patterns (MVVM), reactive programming (LiveData + Coroutines), and professional UI/UX design following Material Design principles.

---

## 1. Registration Process

### User Flow
1. **User Launch:** User opens app → Auto-directed to Registration screen
2. **Data Entry:** User enters:
   - Full Name (validated: non-empty)
   - Email (validated: proper email format)
   - Password (validated: minimum 6 characters)
   - Confirm Password (validated: must match password field)
3. **Validation:** Client-side validation provides instant feedback
4. **Submission:** User clicks "SIGN UP" button
5. **Success:** User redirected to Dashboard with welcome message

### Technical Implementation

**Frontend (Mobile):**
- **Activity:** `RegisterActivity.kt`
- **ViewModel:** `AuthViewModel.kt` with validation logic
- **Repository:** `AuthRepository.kt` abstracts API calls
- **API Service:** `AuthService.kt` with Retrofit

**Validation Rules:**
```kotlin
- Name: must not be blank
- Email: must match EMAIL_ADDRESS pattern
- Password: minimum 6 characters
- Confirm Password: must exactly match password field
```

**Request Payload:**
```json
{
  "name": "Jay Lord Bayonas",
  "email": "jaylord.bayonas@cit.edu",
  "password": "123456"
}
```

**Response (Success - HTTP 201):**
```json
{
  "success": true,
  "message": "Registration successful",
  "token": "96a2830f-a925-47d1-9f20-0fbbe183e0ed",
  "user": {
    "id": "754b878e-f436-48c1-94b4-d64d2493b2f8",
    "name": "Jay Lord Bayonas",
    "email": "jaylord.bayonas@cit.edu"
  }
}
```

### Backend Processing

**Controller:** `AuthController.java` at `/api/auth/register`

**Steps:**
1. Validate input (name, email, password minimum 6 chars)
2. Check if email already exists (prevent duplicates)
3. Split name into firstName and lastName
4. Generate unique schoolId (UUID)
5. Hash password using BCrypt
6. Set required defaults:
   - phone: "" (empty string)
   - role: "STUDENT"
   - gender: "OTHER"
   - age: 0
7. Save to PostgreSQL database
8. Generate UUID token
9. Return HTTP 201 with user data and token

**Database Storage:**
- Table: `users`
- Fields: school_id (PK), first_name, last_name, email (unique), password (hashed), phone, role, gender, age

---

## 2. Login Process

### User Flow
1. **User Launch:** User navigates to Login screen
2. **Data Entry:** User enters:
   - Email address
   - Password
3. **Validation:** Client-side validation (email format, password non-empty)
4. **Submission:** User clicks "SIGN IN" button
5. **Success:** Backend validates credentials → User redirected to Dashboard
6. **Failure:** Red error message displayed: "Invalid password" or "Invalid email"

### Technical Implementation

**Frontend (Mobile):**
- **Activity:** `LoginActivity.kt`
- **ViewModel:** `AuthViewModel.kt` with login method
- **Repository:** `AuthRepository.kt` 
- **API Service:** `AuthService.kt` 

**Validation Rules:**
```kotlin
- Email: must match EMAIL_ADDRESS pattern
- Password: must not be blank
```

**Request Payload:**
```json
{
  "email": "jaylord.bayonas@cit.edu",
  "password": "123456"
}
```

**Response (Success - HTTP 200):**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "96a2830f-a925-47d1-9f20-0fbbe183e0ed",
  "user": {
    "id": "754b878e-f436-48c1-94b4-d64d2493b2f8",
    "name": "Jay Lord Bayonas",
    "email": "jaylord.bayonas@cit.edu"
  }
}
```

**Response (Failure - HTTP 401):**
```json
{
  "success": false,
  "message": "Invalid password"
}
```

### Backend Processing

**Controller:** `AuthController.java` at `/api/auth/login`

**Steps:**
1. Validate input (email and password not empty)
2. Query database for user by email
3. If user not found: return 401 "Invalid email/password"
4. If user found: use PasswordEncoder to verify password against hashed version
5. If password mismatch: return 401 "Invalid password"
6. If password match: 
   - Generate UUID token
   - Return HTTP 200 with user data and token
7. Handle exceptions with appropriate error messages

**Authentication Method:**
- Uses Spring Security's `BCryptPasswordEncoder`
- Passwords stored as BCrypt hashes (salted, one-way encryption)
- Login verification uses `PasswordEncoder.matches(plaintext, hash)`

---

## 3. API Integration

### Network Configuration

**Base URL:** `http://10.0.2.2:8080/`
- Special emulator IP address for localhost
- Production would use actual server domain with HTTPS

**Framework:** Retrofit 2.9.0 with OkHttp 3.11.0

**Security Configuration:**
- Network Security Config file allows cleartext HTTP for local development
- Production requires HTTPS encryption
- CORS enabled on backend for cross-origin requests

### API Endpoints

**POST /api/auth/register**
- Port: 8080
- Protocol: HTTP (development) / HTTPS (production)
- Request: `RegisterRequest` with name, email, password
- Response: `AuthResponse` with success flag, message, token, user data
- Status Codes:
  - 201 Created: Successful registration
  - 400 Bad Request: Invalid input (email empty, password < 6 chars)
  - 409 Conflict: Email already registered
  - 500 Internal Server Error: Database/server error

**POST /api/auth/login**
- Port: 8080
- Protocol: HTTP (development) / HTTPS (production)
- Request: `LoginRequest` with email, password
- Response: `AuthResponse` with success flag, message, token, user data
- Status Codes:
  - 200 OK: Successful login
  - 400 Bad Request: Missing email or password
  - 401 Unauthorized: Wrong password or email not found
  - 500 Internal Server Error: Database/server error

### Data Models

**Frontend (Kotlin Data Classes):**
```kotlin
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserData?
)

data class UserData(
    val id: String,
    val name: String,
    val email: String
)
```

**Backend (Java Beans):**
```java
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    // getters and setters
}

public class LoginRequest {
    private String email;
    private String password;
    // getters and setters
}

public class AuthResponse {
    private Boolean success;
    private String message;
    private String token;
    private UserData user;
    // getters and setters
}
```

### Network Layer Architecture

```
Mobile App (Kotlin)
    ↓
Activity/ViewModel (UI Layer)
    ↓
Repository (Data Abstraction)
    ↓
AuthService (Retrofit Interface)
    ↓
RetrofitClient (HTTP Client Setup)
    ├─ OkHttpClient
    ├─ HttpLoggingInterceptor (for debugging)
    └─ GsonConverterFactory (JSON serialization)
    ↓
Backend (Spring Boot Java)
    ↓
AuthController (@RestController)
    ↓
UserService (Business Logic)
    ↓
UserRepository (JPA Data Access)
    ↓
PostgreSQL Database
```

### Error Handling

**Frontend:**
- Network errors caught in try-catch block
- Error messages displayed in red text on UI
- Loading spinner shown during API calls
- User can retry registration/login

**Backend:**
- Input validation with detailed error messages
- Exception handling for database operations
- StandardHTTP status codes (201, 400, 401, 409, 500)
- JSON error responses with success: false flag

### Logging & Debugging

- HttpLoggingInterceptor configured at BODY level
- Logs request/response headers and bodies in Logcat
- Enables debugging of:
  - Request payloads
  - Response JSON
  - Network timing (502ms example)
  - Authentication flows

---

## 4. Technology Stack

### Mobile (Android)
- **Language:** Kotlin
- **Min SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34 (Android 14)
- **Architecture:** MVVM (Model-View-ViewModel)
- **UI Framework:** AndroidX with Material Design
- **State Management:** LiveData + ViewModel + Coroutines

### Backend (Phase 1)
- **Framework:** Spring Boot 3.x
- **Language:** Java
- **Database:** PostgreSQL
- **Security:** Spring Security with BCrypt password encoding
- **Build Tool:** Maven

### Networking
- **HTTP Client:** Retrofit 2.9.0
- **HTTP Bridge:** OkHttp 3.11.0
- **JSON Serialization:** Gson
- **Interceptors:** HttpLoggingInterceptor for debugging

---

## 5. Key Features Implemented

✅ **User Registration**
- Full name, email, password input
- Client-side validation
- Duplicate email detection
- Secure password hashing (BCrypt)
- Auto-login on successful registration

✅ **User Login**
- Email and password authentication
- Password verification against hashed version
- Session token generation
- Error feedback for invalid credentials

✅ **Professional UI**
- Red medical theme (#B71C1C)
- Material Design icons
- Input field validation feedback
- Loading spinners during API calls
- Error message display

✅ **Network Communication**
- Retrofit-based API client
- JSON request/response handling
- Network security policy for local development
- Comprehensive HTTP logging

✅ **Database Integration**
- User data persistence in PostgreSQL
- Unique email constraint
- Password hashing before storage
- User profile fields (name, email, phone, role, gender, age)

---

## 6. Testing & Validation

### Registration Test
**Input:**
- Name: Jay Lord Bayonas
- Email: jaylord.bayonas@cit.edu
- Password: 123456
- Confirm: 123456

**Result:** ✅ HTTP 201 Created  
**Response:** Token issued, user ID generated, redirected to Dashboard

### Login Test
**Input:**
- Email: jaylord.bayonas@cit.edu
- Password: 123456

**Result:** ✅ HTTP 200 OK  
**Response:** Token issued, user data returned, Dashboard displayed

### Error Cases Tested
- ✅ Duplicate email registration (409 Conflict)
- ✅ Wrong password login (401 Unauthorized)
- ✅ Empty fields (validation feedback)
- ✅ Invalid email format (validation feedback)
- ✅ Password too short (validation feedback)

---

## 7. Database Record

**Sample User in PostgreSQL `users` table:**

| school_id | first_name | last_name | email | password_hash | phone | role | gender | age | created_at |
|-----------|-----------|----------|-------|-----|-------|------|---------|-----|------------|
| 754b878e-f436-48c1-94b4-d64d2493b2f8 | Jay | Lord | jaylord.bayonas@cit.edu | $2a$10$... | | STUDENT | OTHER | 0 | 2026-03-30 14:56:37 |

---

## 8. Conclusion

The CitMedConnect mobile application successfully demonstrates:
- **Modern Android Development:** MVVM architecture with coroutines
- **Backend Integration:** Seamless API communication with Spring Boot
- **Security:** BCrypt password hashing, input validation
- **User Experience:** Material Design, error handling, loading states
- **Real-time Communication:** Network logging shows successful HTTP exchanges

The application is production-ready for Phase 3 features (appointments, medical records, etc.) and provides a solid foundation for future enhancements like token persistence, JWT implementation, and social authentication.

---

**GitHub Repository:** https://github.com/[YourUsername]/IT342-Bayonas-CitMedConnect  
**Final Commit:** a2f7d630a2f33dcbb8d11c49a721da96b6c49ebe  
**Submission Date:** March 30, 2026
