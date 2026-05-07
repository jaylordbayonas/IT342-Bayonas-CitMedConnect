# CitMedConnect - Comprehensive Architecture & Structure Analysis

**Analysis Date**: May 7, 2026  
**Project Status**: Phase 3 Complete (Web + OAuth2)  
**Version**: 3.5.11 (Spring Boot), 18.3.1 (React), 34 (Android SDK)

---

## 1. PROJECT OVERVIEW

### Technology Stack
- **Backend**: Spring Boot 3.5.11, Java 17, PostgreSQL (Supabase)
- **Frontend**: React 18.3.1, Vite 6.0.5, Axios 1.14.0
- **Mobile**: Kotlin, Android SDK 34, Retrofit 2.9.0
- **State Management**: React Context API (web), ViewModel (mobile)
- **Authentication**: GitHub OAuth2, JWT/Session-based

### Key Features Implemented
1. **Authentication** - User registration, login, GitHub OAuth2
2. **Appointments** - Scheduling, time slot management, rescheduling
3. **Medical Records** - Patient medical history and records
4. **Notifications** - System alerts and notifications
5. **User Management** - Profile management, role-based access
6. **Time Slot Management** - Staff availability and slot booking

### Repository Information
- **Git Status**: Main branch is stable, no merge conflicts
- **Phases Completed**: 3 (Auth, Mobile, Web)
- **Documentation**: Phase 2 Mobile report exists; gaps in functional specs

---

## 2. BACKEND ARCHITECTURE (Spring Boot)

### Current Architecture Pattern
**Type**: Traditional Layered Architecture (not vertical slice)  
**Organization**: By Technical Layers

```
edu.cit.bayonas.citmedconnect/
├── CitmedconnectApplication.java (Main entry point)
├── config/
│   └── SecurityConfig.java (Spring Security + OAuth2)
├── controller/ (REST endpoints)
├── service/ (Business logic)
├── repository/ (Data access)
├── entity/ (JPA entities)
├── dto/ (Data transfer objects)
└── mapper/ (Entity-DTO mapping)
```

### Backend File Count Summary

| Layer | Component | Count | Files |
|-------|-----------|-------|-------|
| **Controllers** | REST Endpoints | 7 | AppointmentController, AuthController, MedicalRecordController, NotificationController, OAuth2Controller, TimeSlotController, UserController |
| **Services** | Business Logic | 6 | AppointmentService, AuthService*, MedicalRecordService, NotificationService, OAuth2Service, TimeSlotService, UserService |
| **Repositories** | Data Access | 5 | AppointmentRepository, MedicalRecordRepository, NotificationRepository, TimeSlotRepository, UserRepository |
| **Entities** | Domain Models | 5 | AppointmentEntity, MedicalRecordEntity, NotificationEntity, TimeSlot, UserEntity |
| **DTOs** | Data Transfer | 9 | AppointmentDTO, AuthResponse, GitHubOAuth2UserInfo, LoginRequest, MedicalRecordDTO, OAuth2AuthResponse, RegisterRequest, TimeSlotDTO, UserDTO |
| **Mappers** | Transformation | 2 | MedicalRecordMapper, UserMapper |
| **Config** | Configuration | 1 | SecurityConfig |
| **Tests** | Test Classes | 1 | CitmedconnectApplicationTests (stub only) |
| **TOTAL** | - | **36 files** | |

### Controllers & API Endpoints

#### 1. **AuthController** `/api/auth`
```
POST   /api/auth/register         - User registration
POST   /api/auth/login            - User login
GET    /api/auth/me               - Get current user
POST   /api/auth/logout           - Logout (optional)
POST   /api/auth/refresh-token    - Refresh JWT
```

#### 2. **AppointmentController** `/api/appointments`
```
POST   /api/appointments/                    - Create appointment
GET    /api/appointments/staff/all           - Get all appointments (staff view)
GET    /api/appointments/student/my-appointments  - Get user's appointments
GET    /api/appointments/{id}                - Get appointment details
PUT    /api/appointments/{id}                - Update appointment
DELETE /api/appointments/{id}                - Cancel appointment
POST   /api/appointments/{id}/reschedule    - Reschedule appointment
POST   /api/appointments/{id}/confirm       - Confirm appointment
POST   /api/appointments/{id}/complete      - Mark as completed
```

#### 3. **MedicalRecordController** `/api/medical-records`
```
POST   /api/medical-records/                - Create medical record
GET    /api/medical-records/user/{userId}   - Get user's medical records
GET    /api/medical-records/{id}            - Get record details
PUT    /api/medical-records/{id}            - Update record
DELETE /api/medical-records/{id}            - Delete record
GET    /api/medical-records/search          - Search records
```

#### 4. **NotificationController** `/api/notifications`
```
GET    /api/notifications/                  - Get user notifications
GET    /api/notifications/{id}              - Get notification details
PUT    /api/notifications/{id}/read         - Mark as read
POST   /api/notifications/send              - Send notification
DELETE /api/notifications/{id}              - Delete notification
```

#### 5. **UserController** `/api/users`
```
GET    /api/users/                          - List all users
GET    /api/users/{schoolId}                - Get user by school ID
PUT    /api/users/{schoolId}                - Update user profile
DELETE /api/users/{schoolId}                - Delete user
POST   /api/users/search                    - Search users by criteria
```

#### 6. **TimeSlotController** `/api/timeslots`
```
POST   /api/timeslots/                      - Create time slot
GET    /api/timeslots/                      - List all time slots
GET    /api/timeslots/{id}                  - Get slot details
PUT    /api/timeslots/{id}                  - Update time slot
DELETE /api/timeslots/{id}                  - Delete time slot
GET    /api/timeslots/available             - Get available slots
GET    /api/timeslots/staff/{staffId}       - Get staff's time slots
POST   /api/timeslots/{id}/book             - Book a time slot
```

#### 7. **OAuth2Controller** `/api/oauth2`
```
POST   /api/oauth2/github/callback          - GitHub OAuth callback handler
GET    /api/oauth2/user-info                - Get OAuth user info
POST   /api/oauth2/logout                   - OAuth logout
```

### Backend Services Structure

| Service | Responsibilities | Dependencies |
|---------|-----------------|--------------|
| **AppointmentService** | Appointment CRUD, scheduling logic, status management | AppointmentRepo, UserRepo, TimeSlotRepo, NotificationService |
| **UserService** | User registration, authentication, profile management | UserRepository, OAuth2Service |
| **MedicalRecordService** | Medical records CRUD, patient history | MedicalRecordRepo, UserRepo |
| **NotificationService** | Notification creation, delivery, read status | NotificationRepository, UserRepository |
| **TimeSlotService** | Time slot management, availability checking | TimeSlotRepository, UserRepository |
| **OAuth2Service** | GitHub OAuth integration, token exchange | UserRepository |

### Backend Entity Relationships

```
UserEntity (1)
  ├─ (1:N) → AppointmentEntity
  ├─ (1:N) → MedicalRecordEntity
  ├─ (1:N) → NotificationEntity
  └─ (1:N) → TimeSlot

AppointmentEntity (1)
  ├─ (N:1) → UserEntity (student)
  ├─ (N:1) → UserEntity (staff)
  ├─ (N:1) → TimeSlot
  └─ (1:1) → MedicalRecordEntity

TimeSlot (1)
  ├─ (1:N) → AppointmentEntity
  └─ (N:1) → UserEntity (staff)

MedicalRecordEntity (1)
  ├─ (N:1) → UserEntity (patient)
  ├─ (N:1) → AppointmentEntity
  └─ (N:1) → UserEntity (staff/doctor)

NotificationEntity (1)
  └─ (N:1) → UserEntity
```

### Backend Configuration

**Database**: PostgreSQL (Supabase)
- Connection Pool: HikariCP (max 5 connections - free tier)
- DDL Strategy: `update` (auto-create/update tables)
- Dialect: PostgreSQL
- Transaction Isolation: Connection pooling at transaction mode (port 6543)

**Security**:
- OAuth2 Provider: GitHub
- Client ID: `Ov23liMMOX0oCIllvWnK`
- Authorized Redirect URIs:
  - `http://localhost:3000/oauth2/callback/github` (React)
  - `http://localhost:5173/oauth2/callback/github` (Vite)

**Dependencies**:
- Spring Data JPA (ORM)
- Spring Security (Auth/OAuth2)
- Spring Web (REST)
- Spring Validation (Bean validation)
- PostgreSQL Driver
- Lombok (optional, for entity generation)

---

## 3. WEB FRONTEND ARCHITECTURE (React + Vite)

### Current Architecture Pattern
**Type**: Feature-Based Organization (partial)  
**State Management**: Context API + Custom Hooks  
**Build Tool**: Vite 6.0.5

```
web/src/
├── App.jsx (Main app, routing)
├── main.jsx (Entry point)
├── index.css (Global styles)
├── components/
│   └── common/
│       ├── layout/
│       │   ├── Layout.jsx
│       │   └── Layout.css
│       ├── UI Components (15 components)
│       └── index.js, index.jsx
├── pages/ (8 page components)
├── services/ (9 service files)
├── context/ (5 context providers)
├── hooks/ (6 custom hooks)
├── types/ (index.js, methods.js)
└── utils/ (3 utility files)
```

### Web Frontend File Count Summary

| Category | Subcategory | Count | Items |
|----------|------------|-------|-------|
| **Pages** | Page Components | 8 | Appointments, Calendar, Dashboard, Landing, MedicalRecords, Notifications, OAuth2Callback, Profile |
| **Components** | Common UI | 15 | Alert, Badge, Button, Card, EmptyState, Input, Modal, NotificationBadge, Select, Textarea, ToastNotification, etc. |
| **Services** | API/Business | 9 | api-endpoints, api, appointment-service, auth-helper, data-transformer, github-oauth-service, medicalRecordsService, notificationService, userService |
| **Context** | State Management | 5 | AppointmentContext, AuditLogContext, AuthContext, MedicalRecordsContext, NotificationContext |
| **Hooks** | Custom Hooks | 6 | useAppointments, useAuth, useMedicalRecords, useNotifications, useNotificationSender, useUsers |
| **Types** | Data Models | 2 | index.js (schemas), methods.js |
| **Utils** | Utilities | 3 | constants, dateHelpers, validation |
| **TOTAL** | - | **48+ files** | |

### Web Pages (Routes)

| Route | Component | Feature | Status |
|-------|-----------|---------|--------|
| `/` | Landing.jsx | Public landing page, OAuth2 login | ✅ Implemented |
| `/dashboard` | Dashboard.jsx | User dashboard, overview | ✅ Implemented |
| `/appointments` | Appointments.jsx | Appointment management | ✅ Implemented |
| `/calendar` | Calendar.jsx | Calendar view | ✅ Implemented |
| `/medical-records` | MedicalRecords.jsx | Medical records view | ✅ Implemented |
| `/notifications` | Notifications.jsx | Notification center | ✅ Implemented |
| `/profile` | Profile.jsx | User profile, settings | ✅ Implemented |
| `/oauth2/callback/github` | OAuth2Callback.jsx | OAuth2 callback handler | ✅ Implemented |

### Web Components Structure

#### Common UI Components (15)
```
Button.jsx          - Primary action button with variants
Card.jsx            - Container component for content
Modal.jsx           - Dialog/modal overlays
Input.jsx           - Text input with validation
Select.jsx          - Dropdown select component
Textarea.jsx        - Multi-line text input
Badge.jsx           - Status/label badge
Alert.jsx           - Alert/warning messages
EmptyState.jsx      - Empty state placeholder
NotificationBadge.jsx - Notification counter
ToastNotification.jsx - Toast notifications
Layout.jsx          - Main layout wrapper with navigation
```

#### Page Components (8)
- Landing.jsx (Public, OAuth2 integration)
- Dashboard.jsx (Protected, overview)
- Appointments.jsx (Appointment CRUD, scheduling)
- Calendar.jsx (Calendar view)
- MedicalRecords.jsx (Medical records display)
- Notifications.jsx (Notification center)
- Profile.jsx (User profile, settings)
- OAuth2Callback.jsx (OAuth2 callback handler)

### Web Services (9)

| Service | Purpose | API Calls |
|---------|---------|-----------|
| **api-endpoints.js** | Endpoint configuration | Route definitions |
| **api.js** | Main API client | (Empty - needs implementation) |
| **appointment-service.js** | Appointment API calls | GET, POST, PUT, DELETE appointments |
| **auth-helper.js** | Auth utilities | Token management, local storage |
| **data-transformer.js** | Data transformation | Format conversion |
| **github-oauth-service.js** | GitHub OAuth | OAuth flow handling |
| **medicalRecordsService.js** | Medical records API | CRUD operations |
| **notificationService.js** | Notification API | Get, create notifications |
| **userService.js** | User API | Get, update user profile |

### Web Context Providers (5)

| Context | Purpose | Provided State/Methods |
|---------|---------|----------------------|
| **AuthContext** | Authentication state | `user`, `login()`, `logout()`, `loading`, `isAuthenticated` |
| **AppointmentContext** | Appointments state | `appointments`, `createAppointment()`, `updateAppointment()` |
| **MedicalRecordsContext** | Medical records state | `records`, `addRecord()`, `updateRecord()` |
| **NotificationContext** | Notifications state | `notifications`, `addNotification()`, `markAsRead()` |
| **AuditLogContext** | Audit logging | `logs`, `addLog()` (for compliance/tracking) |

### Web Custom Hooks (6)

| Hook | Purpose | Returns |
|------|---------|---------|
| **useAuth()** | Auth state management | `user`, `login()`, `logout()`, `isAuthenticated` |
| **useAppointments()** | Appointments state | `appointments`, `loading`, `error`, `create()`, `update()` |
| **useMedicalRecords()** | Medical records state | `records`, `loading`, `fetch()`, `create()` |
| **useNotifications()** | Notifications state | `notifications`, `unreadCount`, `markAsRead()` |
| **useNotificationSender()** | Send notifications | `sendNotification()` |
| **useUsers()** | Users state | `users`, `searchUsers()`, `getUser()` |

### Web Utilities & Types

**Types** (Entity Schemas in `types/index.js`):
- UserSchema
- AppointmentsSchema
- Time_SlotSchema
- Medical_RecordsSchema
- NotificationsSchema
- AuditLogSchema

**Utils**:
- `constants.js` - App constants, role types
- `dateHelpers.js` - Date formatting, parsing
- `validation.js` - Form/field validation

---

## 4. MOBILE APPLICATION ARCHITECTURE (Android/Kotlin)

### Current Architecture Pattern
**Type**: MVVM (Model-View-ViewModel)  
**Networking**: Retrofit 2.9.0 + OkHttp  
**Build Target**: Android SDK 34, Min SDK 26 (Android 8)

```
mobile/app/src/main/java/edu/cit/bayonas/citmedconnect/
├── MainActivity.kt (Main activity)
├── ui/
│   ├── DashboardActivity.kt
│   ├── LoginActivity.kt
│   ├── RegisterActivity.kt
│   └── viewmodel/
│       └── AuthViewModel.kt
├── data/
│   ├── api/
│   │   ├── AuthService.kt
│   │   └── RetrofitClient.kt
│   ├── model/
│   │   └── AuthModels.kt
│   └── repository/
│       └── AuthRepository.kt
└── (Other components - TBD)
```

### Mobile File Count Summary

| Layer | Component | Count | Files |
|-------|-----------|-------|-------|
| **Activities** | UI Activities | 3 | MainActivity, LoginActivity, RegisterActivity, DashboardActivity |
| **ViewModels** | State Management | 1 | AuthViewModel |
| **API Services** | Network | 2 | AuthService, RetrofitClient |
| **Models** | Data Models | 1 | AuthModels |
| **Repositories** | Data Access | 1 | AuthRepository |
| **TOTAL** | - | **8+ files** | |

### Mobile Architecture Components

#### Activities
```
MainActivity.kt          - Entry point, navigation hub
LoginActivity.kt        - User login screen
RegisterActivity.kt     - User registration screen
DashboardActivity.kt    - Main dashboard after login
```

#### ViewModels
```
AuthViewModel.kt        - Auth state management (username, password, auth state)
                        - Methods: login(), register(), validateCredentials()
```

#### Network Layer
```
RetrofitClient.kt       - Retrofit instance configuration
                        - Base URL: http://localhost:8080/api
                        - Interceptors: Logging, Auth headers
                        
AuthService.kt          - API interface definitions
                        - login(): Observable<AuthResponse>
                        - register(): Observable<AuthResponse>
```

#### Data Models
```
AuthModels.kt           - LoginRequest, RegisterRequest, AuthResponse
                        - User model representation
```

#### Repository Pattern
```
AuthRepository.kt       - Data access abstraction
                        - login(credentials) → calls AuthService.login()
                        - register(user) → calls AuthService.register()
                        - Handles error transformation
```

### Mobile API Integration

**Base URL**: `http://localhost:8080/api`

**Endpoints Used**:
```
POST   /api/auth/login       - User login
POST   /api/auth/register    - User registration
GET    /api/auth/me          - Get current user info
POST   /api/auth/logout      - Logout
```

**HTTP Client Configuration**:
- Retrofit 2.9.0
- OkHttp 4.11.0 with Logging Interceptor
- Gson converter for JSON serialization
- Connection timeout: 30 seconds
- Read timeout: 30 seconds

### Mobile Dependencies

**AndroidX**:
- core: 1.12.0
- core-ktx: 1.12.0
- appcompat: 1.6.1
- constraintlayout: 2.1.4
- material: 1.11.0

**Networking**:
- Retrofit: 2.9.0
- OkHttp: 4.11.0
- Gson converter: 2.9.0

**Lifecycle**:
- ViewModel, LiveData (for MVVM)
- Kotlin Coroutines (for async operations)

---

## 5. FEATURE BOUNDARIES & DEPENDENCIES

### Feature 1: Authentication (Cross-Platform)

**Scope**: User registration, login, OAuth2, session management

**Backend**:
- AuthController (`/api/auth/*`)
- AuthResponse, LoginRequest, RegisterRequest DTOs
- OAuth2Service, OAuth2Controller, OAuth2 Config
- GitHubOAuth2UserInfo
- UserService (for user creation/lookup)

**Web Frontend**:
- AuthContext + useAuth hook
- Landing.jsx (OAuth2 login button)
- OAuth2Callback.jsx (callback handler)
- github-oauth-service.js
- Protected routes (ProtectedRoute component)

**Mobile**:
- LoginActivity, RegisterActivity
- AuthViewModel
- AuthService, AuthRepository
- AuthModels

**Dependencies**:
- All other features depend on Auth (Auth → Appointments, Auth → MedicalRecords)
- Standalone feature in mobile (can work offline for form)

---

### Feature 2: Appointments (Cross-Platform)

**Scope**: Schedule appointments, view slots, reschedule, cancel, confirm

**Backend**:
- AppointmentController (`/api/appointments/*`)
- AppointmentEntity, TimeSlot, AppointmentDTO, TimeSlotDTO
- AppointmentService, TimeSlotService
- AppointmentRepository, TimeSlotRepository
- NotificationService (for appointment notifications)

**Web Frontend**:
- Appointments.jsx, Calendar.jsx (pages)
- AppointmentContext + useAppointments hook
- appointment-service.js
- TimeSlotController integration

**Mobile**:
- AppointmentActivity (TBD)
- AppointmentViewModel (TBD)
- AppointmentService, AppointmentRepository (TBD)

**Dependencies**:
- Depends on: Auth (for user identification), UserService (staff lookup)
- Called by: Notifications (appointment reminders), MedicalRecords (appointment linking)

**API Endpoints** (13 total):
```
POST, GET /api/appointments/
POST /api/appointments/reschedule/{id}
POST /api/appointments/confirm/{id}
POST /api/appointments/complete/{id}
GET /api/appointments/staff/all
GET /api/appointments/student/my-appointments
POST, GET /api/timeslots/
GET /api/timeslots/available
GET /api/timeslots/staff/{staffId}
```

---

### Feature 3: Medical Records (Cross-Platform)

**Scope**: Create, view, update medical records and vital signs

**Backend**:
- MedicalRecordController (`/api/medical-records/*`)
- MedicalRecordEntity, MedicalRecordDTO
- MedicalRecordService, MedicalRecordMapper
- MedicalRecordRepository

**Web Frontend**:
- MedicalRecords.jsx (page)
- MedicalRecordsContext + useMedicalRecords hook
- medicalRecordsService.js

**Mobile**:
- MedicalRecordsActivity (TBD)
- MedicalRecordsViewModel (TBD)
- MedicalRecordsService, MedicalRecordsRepository (TBD)

**Dependencies**:
- Depends on: Auth (for user identification), Appointments (linked records)
- Independent from: Notifications, other features

**Data Model**:
```
Diagnosis, Treatment, Prescriptions
Vital Signs: Blood Pressure, Heart Rate, Temperature, Weight
Medical History
```

---

### Feature 4: Notifications (Cross-Platform)

**Scope**: Send, receive, mark as read, notification center

**Backend**:
- NotificationController (`/api/notifications/*`)
- NotificationEntity, NotificationDTO
- NotificationService
- NotificationRepository

**Web Frontend**:
- Notifications.jsx (page)
- NotificationContext + useNotifications hook
- NotificationBadge component
- notificationService.js

**Mobile**:
- NotificationsActivity (TBD)
- NotificationService (TBD)

**Dependencies**:
- Depends on: Auth (user identification)
- Called by: AppointmentService (appointment notifications), UserService (admin alerts)
- Semi-independent (can be enhanced with push notifications)

**Notification Types**:
- Appointment reminders
- Appointment confirmations
- Booking confirmations
- System alerts
- Admin broadcasts

---

### Feature 5: User Management (Cross-Platform)

**Scope**: User profiles, role management, search functionality

**Backend**:
- UserController (`/api/users/*`)
- UserEntity, UserDTO, UserMapper
- UserService
- UserRepository

**Web Frontend**:
- Profile.jsx (page)
- useUsers hook
- userService.js

**Mobile**:
- ProfileActivity (TBD)
- ProfileViewModel (TBD)

**Dependencies**:
- Depends on: Auth (initial user setup)
- Used by: All other features (user lookup, role checking)
- Critical service for access control

---

### Dependency Graph

```
                    ┌─────────────────────────────────┐
                    │     AUTHENTICATION (Auth)       │
                    │  - Login/Register/OAuth2         │
                    └────────┬────────────────────────┘
                             │
                ┌────────────┼────────────┐
                │            │            │
        ┌───────▼──────┐  ┌──▼────────┐  ┌──▼──────────┐
        │ APPOINTMENTS │  │   USERS   │  │NOTIFICATIONS│
        │  - Schedule  │  │ - Profiles│  │  - Reminders│
        │  - Reschedule│  │ - Search  │  │  - Alerts   │
        └───────┬──────┘  └──┬────────┘  └──┬──────────┘
                │            │             │
                └────────────┼─────────────┘
                             │
                    ┌────────▼──────────┐
                    │ MEDICAL RECORDS   │
                    │  - Diagnosis      │
                    │  - Treatment      │
                    │  - Vital Signs    │
                    └───────────────────┘

Legend:
→ Depends on (must complete first)
```

---

## 6. FILE COUNT SUMMARY & METRICS

### By Platform

| Platform | Backend | Web | Mobile | Total |
|----------|---------|-----|--------|-------|
| **Controllers/Activities** | 7 | 8 | 4 | 19 |
| **Services** | 6 | 9 | 2 | 17 |
| **Repositories/Data** | 5 | 1 | 1 | 7 |
| **Entities/Models** | 5 | 0 | 1 | 6 |
| **DTOs/State** | 9 | 5 | 0 | 14 |
| **ViewModels** | 0 | 0 | 1 | 1 |
| **Configuration** | 1 | 3 | 1 | 5 |
| **Tests** | 1 | 0 | 0 | 1 |
| **TOTAL** | **36** | **48+** | **10** | **94+** |

### By Type

| Type | Count | Status |
|------|-------|--------|
| **Java Classes** | 36 | ✅ Implemented |
| **React Components** | 23+ | ✅ Implemented |
| **Kotlin Classes** | 8+ | ✅ Partially Implemented |
| **Configuration Files** | 5 | ✅ Implemented |
| **Tests** | 1 | ⚠️ Stubs only |
| **Documentation** | 2 | ⚠️ Partial |

---

## 7. CURRENT VS PROPOSED ARCHITECTURE

### Current Organization

**Backend**: Layered by Technical Concerns
```
controller/
service/
repository/
entity/
dto/
```

**Issues**:
- Features scattered across layers
- Auth logic mixed with user management
- Appointment CRUD and scheduling logic together
- Hard to understand feature boundaries
- Difficult to test features in isolation

---

### Proposed: Vertical Slice Architecture

**Backend**: Organized by Feature/Domain
```
src/main/java/edu/cit/bayonas/citmedconnect/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── appointments/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── medicalrecords/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── notifications/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
├── users/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
└── common/
    ├── config/
    ├── exception/
    ├── util/
    └── security/
```

**Benefits**:
- ✅ Each feature is self-contained
- ✅ Easy to identify feature dependencies
- ✅ Simplified testing per feature
- ✅ Easier to add/remove features
- ✅ Clear ownership boundaries
- ✅ Better for microservices migration

---

**Web Frontend**: Already Feature-Based (Good!)
```
src/
├── features/
│   ├── auth/
│   │   ├── pages/
│   │   ├── services/
│   │   └── hooks/
│   ├── appointments/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── context/
│   │   └── hooks/
│   ├── medicalrecords/
│   ├── notifications/
│   └── users/
├── common/
│   ├── components/
│   ├── hooks/
│   ├── utils/
│   └── types/
└── shared/
    ├── services/
    └── config/
```

---

**Mobile**: MVVM is Good Foundation
```
src/main/java/edu/cit/bayonas/citmedconnect/
├── features/
│   ├── auth/
│   │   ├── ui/
│   │   ├── viewmodel/
│   │   ├── repository/
│   │   └── service/
│   ├── appointments/
│   ├── medicalrecords/
│   ├── notifications/
│   └── users/
└── common/
    ├── api/
    ├── config/
    ├── util/
    └── model/
```

---

## 8. CROSS-CUTTING CONCERNS

### Shared Across All Platforms

#### 1. **Authentication & Authorization**
- Backend: Spring Security, OAuth2
- Web: Context API, JWT tokens
- Mobile: SharedPreferences, authentication state

#### 2. **Error Handling**
- Backend: Exception handlers (missing)
- Web: Try-catch in services, error states
- Mobile: Error handling in repositories (missing)

#### 3. **Logging & Audit**
- Backend: Spring logging (minimal)
- Web: AuditLogContext (implemented but not fully used)
- Mobile: Logcat logging

#### 4. **Data Validation**
- Backend: Bean Validation (@Valid annotations)
- Web: validation.js utility
- Mobile: Manual validation (TBD)

#### 5. **API Communication**
- Backend: REST endpoints
- Web: Axios calls through services
- Mobile: Retrofit HTTP calls

### Missing/Incomplete Cross-Cutting Concerns

| Concern | Status | Impact |
|---------|--------|--------|
| Global Exception Handling | ⚠️ Backend incomplete | Poor error user experience |
| Request/Response Logging | ⚠️ Minimal | Hard to debug |
| Rate Limiting | ❌ Missing | No protection from abuse |
| Caching Strategy | ❌ Missing | Unnecessary API calls |
| Circuit Breaker | ❌ Missing | No resilience |
| Input Sanitization | ⚠️ Partial | Security concern |
| CORS Configuration | ✅ Implemented | Endpoints accessible |
| Database Transactions | ⚠️ Partial | Data consistency issues |

---

## 9. ARCHITECTURAL PATTERNS IDENTIFIED

### Backend Patterns

1. **Layered Architecture** (current) → **Vertical Slice** (proposed)
2. **Repository Pattern** ✅ (data abstraction)
3. **Service/Business Layer** ✅ (logic isolation)
4. **DTO Pattern** ✅ (data transfer)
5. **Mapper Pattern** ✅ (partial - only 2 entities)
6. **Spring Security Configuration** ✅ (config-based)

### Web Frontend Patterns

1. **Component-Based Architecture** ✅
2. **Context API for State** ✅
3. **Custom Hooks** ✅
4. **Service Layer** ✅ (for API calls)
5. **Protected Routes** ✅ (auth protection)
6. **Layout Pattern** ✅

### Mobile Patterns

1. **MVVM (Model-View-ViewModel)** ✅
2. **Repository Pattern** ✅
3. **Dependency Injection** ⚠️ (manual, not using Hilt)
4. **Retrofit for HTTP** ✅

---

## 10. CURRENT GAPS & ISSUES

### Critical (Blocking)

| Issue | Platform | Impact | Fix Effort |
|-------|----------|--------|-----------|
| No unit tests | All | Cannot verify features work | High |
| Missing test dependencies | All | Cannot add tests | Medium |
| No exception handling | Backend | Unclear error responses | Medium |
| Incomplete API error responses | Backend, Web | Poor UX on errors | Medium |

### Major (Should Fix)

| Issue | Platform | Impact | Fix Effort |
|-------|----------|--------|-----------|
| Layered not vertical slice | Backend | Hard to refactor later | High |
| Limited DTOs/Mappers | Backend | Data transformation incomplete | Medium |
| No pagination | Backend, Web | Performance issues with large data | Medium |
| No caching | Web, Mobile | Unnecessary API calls | Medium |
| Incomplete OAuth2 flow | Web | Limited OAuth2 features | Medium |
| No input validation feedback | Web | Poor form UX | Low |

### Minor (Nice to Have)

| Issue | Platform | Impact | Fix Effort |
|-------|----------|--------|-----------|
| Limited logging | All | Hard to debug | Low |
| No analytics | Web | Can't track usage | Low |
| No offline support | Mobile | Needs connectivity | High |
| No push notifications | Mobile | Limited engagement | High |

---

## 11. DEPENDENCY ANALYSIS

### Backend Service Dependencies (Wiring)

```
AppointmentController
  ├─ AppointmentService
  │   ├─ AppointmentRepository
  │   ├─ UserRepository
  │   ├─ TimeSlotRepository
  │   └─ NotificationService (circular dependency risk!)
  │       └─ NotificationRepository

AuthController
  ├─ UserService
  │   └─ UserRepository
  └─ OAuth2Service
      └─ UserRepository

MedicalRecordController
  ├─ MedicalRecordService
  │   └─ MedicalRecordRepository

NotificationController
  ├─ NotificationService
  │   └─ NotificationRepository

UserController
  └─ UserService
      └─ UserRepository

TimeSlotController
  ├─ TimeSlotService
  │   └─ TimeSlotRepository
```

**Circular Dependency Alert**:
- AppointmentService → NotificationService → (could call AppointmentService for details)
- **Mitigation**: Use event-driven pattern or separate notification queue

### Frontend Component Dependencies

```
App.jsx (Root)
  ├─ AuthProvider (context)
  ├─ NotificationProvider
  ├─ AuditLogProvider
  ├─ AppointmentProvider
  ├─ ProtectedRoute
  │   └─ Layout
  │       └─ Page Components
  └─ Page Routes

Pages (Protected)
  ├─ Dashboard
  │   ├─ useAuth
  │   └─ useAppointments
  ├─ Appointments
  │   ├─ useAppointments
  │   ├─ useAuth
  │   └─ Calendar
  └─ MedicalRecords
      └─ useMedicalRecords
```

### Data Flow

```
User Input (Component)
  ↓
Service Layer (API call via axios)
  ↓
Backend API (Controller → Service → Repository)
  ↓
Database
  ↓
Response (DTO)
  ↓
Service Layer (Transform data)
  ↓
Context/State Update
  ↓
Component Re-render
```

---

## 12. TESTING GAPS

### Backend Tests

| Layer | Test Type | Coverage | Status |
|-------|-----------|----------|--------|
| Controller | Unit | 0% | ❌ Missing |
| Service | Unit | 0% | ❌ Missing |
| Repository | Integration | 0% | ❌ Missing |
| End-to-End | API | 0% | ❌ Missing |

### Web Tests

| Category | Test Type | Coverage | Status |
|----------|-----------|----------|--------|
| Components | Unit | 0% | ❌ Missing |
| Hooks | Unit | 0% | ❌ Missing |
| Services | Unit | 0% | ❌ Missing |
| Integration | E2E | 0% | ❌ Missing |

### Mobile Tests

| Category | Test Type | Coverage | Status |
|----------|-----------|----------|--------|
| Activities | Unit | 0% | ❌ Missing |
| ViewModels | Unit | 0% | ❌ Missing |
| Repository | Unit | 0% | ❌ Missing |
| Integration | E2E | 0% | ❌ Missing |

**Test Recommendations**:
1. Backend: JUnit 5 + Mockito + Spring Boot Test
2. Web: Vitest or Jest + React Testing Library
3. Mobile: JUnit 4 + Mockito + Espresso

---

## 13. DATA MODEL ANALYSIS

### Domain Entities

#### User
```
- schoolId (PK)
- firstName, lastName
- email (unique)
- phone
- password (hashed)
- role (STUDENT | STAFF | ADMIN)
- gender
- age
- oauthProvider, oauthId (for OAuth)
- createdAt, updatedAt
```

#### Appointment
```
- appointmentId (PK)
- userId (FK → User)
- staffId (FK → User/Staff)
- timeSlotId (FK → TimeSlot)
- status (SCHEDULED | COMPLETED | CANCELLED | RESCHEDULED)
- reason
- symptoms
- notes
- createdAt, updatedAt
```

#### TimeSlot
```
- timeSlotId (PK)
- slotDate
- slotTime
- duration (minutes)
- isAvailable (boolean)
- staffId (FK → User/Staff)
- location
- createdAt, updatedAt
```

#### MedicalRecord
```
- recordId (PK)
- studentId (FK → User)
- appointmentId (FK → Appointment)
- diagnosis
- treatment
- prescriptions (array)
- vitalSigns (BP, HR, Temp, Weight)
- medicalHistory
- staffId (FK → User/Doctor)
- createdAt, updatedAt
```

#### Notification
```
- notificationId (PK)
- userId (FK → User)
- type (SUCCESS | WARNING | INFO | ERROR)
- title, message
- isRead
- relatedEntity (appointment/record ID)
- createdAt
```

### Missing Models

- **Prescription** (should be separate entity)
- **VitalSigns** (should be separate entity or embedded)
- **AuditLog** (for compliance tracking)
- **SystemRole/Permission** (for RBAC)

---

## 14. API SPECIFICATION SUMMARY

### Authentication Endpoints (7)
```
POST   /api/auth/register
POST   /api/auth/login
GET    /api/auth/me
POST   /api/auth/logout
POST   /api/auth/refresh-token
POST   /api/oauth2/github/callback
GET    /api/oauth2/user-info
```

### Appointment Endpoints (13)
```
POST   /api/appointments/
GET    /api/appointments/
GET    /api/appointments/{id}
PUT    /api/appointments/{id}
DELETE /api/appointments/{id}
POST   /api/appointments/{id}/reschedule
POST   /api/appointments/{id}/confirm
POST   /api/appointments/{id}/complete
GET    /api/appointments/staff/all
GET    /api/appointments/student/my-appointments
POST   /api/timeslots/{id}/book
GET    /api/timeslots/available
GET    /api/timeslots/staff/{staffId}
```

### Medical Records Endpoints (6)
```
POST   /api/medical-records/
GET    /api/medical-records/
GET    /api/medical-records/{id}
PUT    /api/medical-records/{id}
DELETE /api/medical-records/{id}
GET    /api/medical-records/user/{userId}
```

### Notification Endpoints (5)
```
GET    /api/notifications/
GET    /api/notifications/{id}
PUT    /api/notifications/{id}/read
POST   /api/notifications/send
DELETE /api/notifications/{id}
```

### User Endpoints (5)
```
GET    /api/users/
GET    /api/users/{schoolId}
PUT    /api/users/{schoolId}
DELETE /api/users/{schoolId}
POST   /api/users/search
```

### TimeSlot Endpoints (7)
```
POST   /api/timeslots/
GET    /api/timeslots/
GET    /api/timeslots/{id}
PUT    /api/timeslots/{id}
DELETE /api/timeslots/{id}
GET    /api/timeslots/available
GET    /api/timeslots/staff/{staffId}
```

**Total Endpoints**: 43 API endpoints across 6 controllers

---

## 15. REFACTORING RECOMMENDATIONS

### Phase 1: Preparation (2-3 hours)
1. Add test dependencies to all platforms
2. Create test infrastructure
3. Document current API behavior

### Phase 2: Backend Refactoring (6-8 hours)
1. Create feature-based package structure
2. Move code to vertical slices
3. Add exception handling layer
4. Add missing DTOs and mappers
5. Implement pagination

### Phase 3: Web Refactoring (4-6 hours)
1. Reorganize into features directory
2. Enhance service layer (implement api.js)
3. Add better error handling
4. Improve state management organization

### Phase 4: Mobile Refactoring (4-6 hours)
1. Expand MVVM structure
2. Add all missing features (appointments, records, etc.)
3. Implement dependency injection (Hilt)
4. Add error handling

### Phase 5: Testing (6-8 hours)
1. Write unit tests for services
2. Write integration tests for APIs
3. Write component tests for UI
4. Set up CI/CD for testing

### Phase 6: Documentation (3-4 hours)
1. Architecture Decision Records (ADRs)
2. API specification (OpenAPI/Swagger)
3. Development guide
4. Deployment guide

---

## 16. SUMMARY TABLE

| Aspect | Current | Proposed | Priority |
|--------|---------|----------|----------|
| **Backend Architecture** | Layered by layer | Vertical slice (feature) | HIGH |
| **Backend Tests** | 1 stub | Full coverage | CRITICAL |
| **Web Architecture** | Partial feature-based | Full feature-based | MEDIUM |
| **Web Tests** | None | Full coverage | HIGH |
| **Mobile Architecture** | MVVM (partial) | Complete MVVM | MEDIUM |
| **Mobile Tests** | None | Basic coverage | MEDIUM |
| **Exception Handling** | Incomplete | Comprehensive | HIGH |
| **API Documentation** | Comments only | OpenAPI/Swagger | MEDIUM |
| **Pagination** | Missing | Implemented | MEDIUM |
| **Caching** | None | Redis/Local cache | LOW |

---

## 17. CONCLUSION

### Current State Assessment

✅ **Strengths**:
- Well-separated platforms (backend, web, mobile)
- Clean layered architecture on backend
- Feature-aware component structure on web
- MVVM pattern established on mobile
- Modern tech stack (Spring Boot 3.5, React 18, Kotlin)
- OAuth2 integration complete
- Database properly configured

❌ **Weaknesses**:
- No testing framework implemented
- Backend organized by layers, not features
- Limited error handling
- Incomplete data mapping
- No pagination support
- Limited logging

### Refactoring Viability: ✅ **YES**

The project is ready for vertical slice refactoring. The codebase is:
- Well-structured and modular
- Manageable size (94+ files across 3 platforms)
- Following consistent patterns
- Using modern frameworks

### Estimated Effort: **18-25 hours**

---

**Document Created**: May 7, 2026  
**Next Steps**: Review this analysis and proceed with Phase 1 preparation
