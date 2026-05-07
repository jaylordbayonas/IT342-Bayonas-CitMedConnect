# CitMedConnect - Feature-to-File Mapping & Complete Inventory

**Generated**: May 7, 2026  
**Purpose**: Map each feature to its implementation files across all platforms

---

## 1. AUTHENTICATION FEATURE

### 1.1 Backend Implementation

**Directory**: `backend/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files**:
```
controller/
  └─ AuthController.java              (7 endpoints)
     ├─ POST   /api/auth/register
     ├─ POST   /api/auth/login
     ├─ GET    /api/auth/me
     ├─ POST   /api/auth/logout
     ├─ POST   /api/auth/refresh-token
     └─ Plus OAuth2 endpoints

controller/
  └─ OAuth2Controller.java            (2 endpoints)
     ├─ POST   /api/oauth2/github/callback
     └─ GET    /api/oauth2/user-info

service/
  ├─ UserService.java                 (User CRUD & auth logic)
  │   ├─ registerUser()
  │   ├─ loginUser()
  │   ├─ getUserById()
  │   └─ validatePassword()
  │
  └─ OAuth2Service.java               (GitHub OAuth logic)
      ├─ handleGitHubCallback()
      ├─ exchangeCodeForToken()
      └─ getUserInfoFromGitHub()

repository/
  └─ UserRepository.java              (User data access)
      ├─ findByEmail()
      ├─ findBySchoolId()
      └─ Custom queries

entity/
  └─ UserEntity.java                  (User JPA entity)
      ├─ schoolId (primary key)
      ├─ email (unique)
      ├─ password (hashed)
      ├─ oauthProvider, oauthId
      ├─ role (STUDENT, STAFF, ADMIN)
      └─ firstName, lastName, phone, etc.

dto/
  ├─ LoginRequest.java                (Request DTO)
  ├─ RegisterRequest.java             (Request DTO)
  ├─ AuthResponse.java                (Response DTO)
  ├─ OAuth2AuthResponse.java          (OAuth response DTO)
  ├─ GitHubOAuth2UserInfo.java        (GitHub user info mapping)
  └─ UserDTO.java                     (User data transfer)

mapper/
  └─ UserMapper.java                  (Entity ↔ DTO mapping)

config/
  └─ SecurityConfig.java              (Spring Security config)
      ├─ OAuth2 client registration (GitHub)
      ├─ Security filters
      └─ CORS configuration
```

**Total Backend Auth Files**: 11

---

### 1.2 Web Frontend Implementation

**Directory**: `web/src/`

**Files**:
```
pages/
  └─ Landing.jsx                      (Public landing page)
      ├─ GitHub OAuth2 login button
      ├─ Register/Login forms
      └─ Unauthenticated view

pages/
  └─ OAuth2Callback.jsx               (OAuth callback handler)
      ├─ Handles /oauth2/callback/github
      ├─ Exchanges code for token
      └─ Redirects to dashboard

context/
  └─ AuthContext.jsx                  (Auth state management)
      ├─ user state
      ├─ loading state
      ├─ error state
      ├─ sessionExpiry
      ├─ login() method
      ├─ logout() method
      ├─ register() method
      ├─ refreshToken() method
      └─ useAuth hook

hooks/
  └─ useAuth.jsx                      (Auth hook)
      ├─ Wraps AuthContext
      ├─ Provides useAuth() hook
      └─ Provides AuthProvider

services/
  ├─ auth-helper.js                   (Token management utilities)
  │   ├─ setAuthToken(token)
  │   ├─ getAuthToken()
  │   ├─ removeAuthToken()
  │   └─ isTokenExpired()
  │
  ├─ github-oauth-service.js          (GitHub OAuth flow)
  │   ├─ initiateGitHubLogin()
  │   ├─ handleOAuth2Callback()
  │   └─ exchangeCodeForToken()
  │
  └─ userService.js                   (User API calls)
      ├─ login(email, password)
      ├─ register(userData)
      ├─ getCurrentUser()
      └─ logout()

components/common/layout/
  └─ Layout.jsx                       (Main layout with auth check)
      ├─ Navigation
      ├─ Protected route guard
      └─ User menu

App.jsx                               (Root routing)
  ├─ ProtectedRoute component
  ├─ Public routes (Landing, OAuth2Callback)
  └─ Protected routes

types/
  └─ index.js                         (Auth schemas)
      └─ UserSchema

utils/
  └─ constants.js                     (Auth constants)
      ├─ ROLES
      ├─ AUTH_TOKENS
      └─ API_URLS

css/
  ├─ Landing.css
  ├─ OAuth2Callback.css
  └─ Shared auth styling
```

**Total Web Auth Files**: 14

---

### 1.3 Mobile Implementation

**Directory**: `mobile/app/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files**:
```
ui/
  ├─ LoginActivity.kt                 (Login screen)
  │   ├─ UI layout
  │   ├─ Form handling
  │   └─ Navigation to AuthViewModel
  │
  └─ RegisterActivity.kt              (Registration screen)
      ├─ UI layout
      ├─ Form handling
      └─ Validation

ui/viewmodel/
  └─ AuthViewModel.kt                 (State management)
      ├─ username: LiveData<String>
      ├─ password: LiveData<String>
      ├─ email: LiveData<String>
      ├─ authState: LiveData<AuthState>
      ├─ login()
      ├─ register()
      └─ logout()

data/repository/
  └─ AuthRepository.kt                (Data abstraction)
      ├─ login(credentials)
      ├─ register(user)
      ├─ logout()
      └─ getAuthToken()

data/api/
  ├─ AuthService.kt                   (API interface)
  │   ├─ @POST login()
  │   ├─ @POST register()
  │   └─ @GET me()
  │
  └─ RetrofitClient.kt                (HTTP client setup)
      ├─ Base URL configuration
      ├─ Interceptors (logging, auth)
      └─ Gson converter

data/model/
  └─ AuthModels.kt                    (Data classes)
      ├─ LoginRequest
      ├─ RegisterRequest
      ├─ AuthResponse
      ├─ User
      └─ Token
```

**Total Mobile Auth Files**: 8

---

## 2. APPOINTMENTS FEATURE

### 2.1 Backend Implementation

**Directory**: `backend/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files**:
```
controller/
  ├─ AppointmentController.java       (8 endpoints)
  │   ├─ POST   /api/appointments/
  │   ├─ GET    /api/appointments/
  │   ├─ GET    /api/appointments/{id}
  │   ├─ PUT    /api/appointments/{id}
  │   ├─ DELETE /api/appointments/{id}
  │   ├─ POST   /api/appointments/{id}/reschedule
  │   ├─ POST   /api/appointments/{id}/confirm
  │   └─ POST   /api/appointments/{id}/complete
  │
  └─ TimeSlotController.java          (7 endpoints)
      ├─ POST   /api/timeslots/
      ├─ GET    /api/timeslots/
      ├─ GET    /api/timeslots/{id}
      ├─ PUT    /api/timeslots/{id}
      ├─ DELETE /api/timeslots/{id}
      ├─ GET    /api/timeslots/available
      └─ GET    /api/timeslots/staff/{staffId}

service/
  ├─ AppointmentService.java          (Appointment logic)
  │   ├─ createAppointment()
  │   ├─ updateAppointment()
  │   ├─ cancelAppointment()
  │   ├─ rescheduleAppointment()
  │   ├─ completeAppointment()
  │   ├─ getAppointmentsByUser()
  │   └─ getAppointmentsByStaff()
  │
  └─ TimeSlotService.java             (TimeSlot logic)
      ├─ createTimeSlot()
      ├─ getAvailableSlots()
      ├─ bookTimeSlot()
      ├─ releaseTimeSlot()
      └─ getStaffTimeSlots()

repository/
  ├─ AppointmentRepository.java       (Appointment data access)
  │   ├─ findByUserId()
  │   ├─ findByStaffId()
  │   ├─ findByStatus()
  │   └─ Custom queries
  │
  └─ TimeSlotRepository.java          (TimeSlot data access)
      ├─ findAvailable()
      ├─ findByStaffId()
      └─ Custom queries

entity/
  ├─ AppointmentEntity.java           (Appointment JPA entity)
  │   ├─ appointmentId (primary key)
  │   ├─ userId (foreign key)
  │   ├─ staffId (foreign key)
  │   ├─ timeSlotId (foreign key)
  │   ├─ status (enum)
  │   ├─ reason, symptoms, notes
  │   └─ timestamps
  │
  └─ TimeSlot.java                    (TimeSlot JPA entity)
      ├─ timeSlotId (primary key)
      ├─ slotDate, slotTime
      ├─ duration
      ├─ isAvailable
      ├─ staffId (foreign key)
      ├─ location
      └─ timestamps

dto/
  ├─ AppointmentDTO.java              (Appointment DTO)
  └─ TimeSlotDTO.java                 (TimeSlot DTO)
```

**Total Backend Appointment Files**: 9

---

### 2.2 Web Frontend Implementation

**Directory**: `web/src/`

**Files**:
```
pages/
  ├─ Appointments.jsx                 (Appointments list view)
  │   ├─ Display user's appointments
  │   ├─ Filter by status
  │   ├─ Create new appointment form
  │   └─ Cancel/reschedule options
  │
  ├─ Calendar.jsx                     (Calendar view)
  │   ├─ Display calendar grid
  │   ├─ Show available slots
  │   ├─ Click to book appointment
  │   └─ View booked appointments
  │
  └─ Appointments.css, Calendar.css   (Styling)

context/
  └─ AppointmentContext.jsx           (Appointment state)
      ├─ appointments list
      ├─ selectedAppointment
      ├─ loading, error states
      ├─ addAppointment()
      ├─ updateAppointment()
      ├─ cancelAppointment()
      └─ fetchAppointments()

hooks/
  └─ useAppointments.jsx              (Appointment hook)
      ├─ appointments list
      ├─ loading, error
      ├─ create()
      ├─ update()
      ├─ cancel()
      ├─ reschedule()
      └─ fetch()

services/
  ├─ appointment-service.js           (Appointment API calls)
  │   ├─ getAppointments()
  │   ├─ createAppointment()
  │   ├─ updateAppointment()
  │   ├─ cancelAppointment()
  │   ├─ rescheduleAppointment()
  │   └─ getAppointmentById()
  │
  └─ api-endpoints.js                 (API endpoint config)
      └─ APPOINTMENTS endpoints

types/
  └─ index.js                         (Appointment schemas)
      ├─ AppointmentsSchema
      └─ Time_SlotSchema

components/common/
  ├─ Modal.jsx                        (Appointment form modal)
  ├─ Card.jsx                         (Appointment card)
  ├─ Button.jsx                       (Action buttons)
  └─ Others (reused)
```

**Total Web Appointment Files**: 10

---

### 2.3 Mobile Implementation (TBD - Proposed Structure)

**Directory**: `mobile/app/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files** (Not yet implemented):
```
ui/
  ├─ AppointmentActivity.kt           (Appointments list)
  └─ BookAppointmentActivity.kt       (Booking form)

ui/viewmodel/
  └─ AppointmentViewModel.kt          (State management)

data/repository/
  └─ AppointmentRepository.kt         (Data abstraction)

data/api/
  └─ AppointmentService.kt            (API interface)

data/model/
  └─ AppointmentModels.kt             (Data classes)
```

**Total Mobile Appointment Files**: 6 (proposed)

---

## 3. MEDICAL RECORDS FEATURE

### 3.1 Backend Implementation

**Directory**: `backend/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files**:
```
controller/
  └─ MedicalRecordController.java     (6 endpoints)
      ├─ POST   /api/medical-records/
      ├─ GET    /api/medical-records/
      ├─ GET    /api/medical-records/{id}
      ├─ PUT    /api/medical-records/{id}
      ├─ DELETE /api/medical-records/{id}
      └─ GET    /api/medical-records/user/{userId}

service/
  └─ MedicalRecordService.java        (Medical record logic)
      ├─ createMedicalRecord()
      ├─ updateMedicalRecord()
      ├─ deleteMedicalRecord()
      ├─ getMedicalRecordsByUser()
      ├─ addPrescription()
      └─ recordVitalSigns()

repository/
  └─ MedicalRecordRepository.java     (Medical record data access)
      ├─ findByStudentId()
      ├─ findByAppointmentId()
      ├─ findByStaffId()
      └─ Custom queries

entity/
  └─ MedicalRecordEntity.java         (Medical record JPA entity)
      ├─ recordId (primary key)
      ├─ studentId (foreign key)
      ├─ appointmentId (foreign key)
      ├─ diagnosis
      ├─ treatment
      ├─ prescriptions (JSON/array)
      ├─ vitalSigns (JSON/embedded)
      ├─ medicalHistory
      ├─ staffId (doctor who created)
      └─ timestamps

dto/
  └─ MedicalRecordDTO.java            (Medical record DTO)

mapper/
  └─ MedicalRecordMapper.java         (Entity ↔ DTO mapping)
```

**Total Backend Medical Records Files**: 6

---

### 3.2 Web Frontend Implementation

**Directory**: `web/src/`

**Files**:
```
pages/
  └─ MedicalRecords.jsx               (Medical records view)
      ├─ Display records list
      ├─ Filter by date
      ├─ View record details
      └─ Export record option

pages/
  └─ MedicalRecords.css               (Styling)

context/
  └─ MedicalRecordsContext.jsx        (Medical records state)
      ├─ records list
      ├─ selectedRecord
      ├─ loading, error states
      ├─ fetchRecords()
      └─ createRecord()

hooks/
  └─ useMedicalRecords.js             (Medical records hook)
      ├─ records list
      ├─ loading, error
      ├─ fetch()
      ├─ create()
      └─ getById()

services/
  └─ medicalRecordsService.js         (Medical records API calls)
      ├─ getRecords()
      ├─ getRecordById()
      ├─ createRecord()
      ├─ updateRecord()
      └─ deleteRecord()

types/
  └─ index.js                         (Medical records schemas)
      └─ Medical_RecordsSchema

components/common/
  ├─ Card.jsx                         (Record card)
  ├─ Modal.jsx                        (Record details modal)
  └─ Others (reused)
```

**Total Web Medical Records Files**: 7

---

### 3.3 Mobile Implementation (TBD - Proposed Structure)

**Files** (Not yet implemented):
```
ui/
  ├─ MedicalRecordsActivity.kt        (Records list)
  └─ RecordDetailActivity.kt          (Record view)

ui/viewmodel/
  └─ MedicalRecordsViewModel.kt       (State management)

data/repository/
  └─ MedicalRecordsRepository.kt      (Data abstraction)

data/model/
  └─ MedicalRecordsModels.kt          (Data classes)
```

**Total Mobile Medical Records Files**: 4 (proposed)

---

## 4. NOTIFICATIONS FEATURE

### 4.1 Backend Implementation

**Directory**: `backend/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files**:
```
controller/
  └─ NotificationController.java      (5 endpoints)
      ├─ GET    /api/notifications/
      ├─ GET    /api/notifications/{id}
      ├─ PUT    /api/notifications/{id}/read
      ├─ POST   /api/notifications/send
      └─ DELETE /api/notifications/{id}

service/
  └─ NotificationService.java         (Notification logic)
      ├─ createNotification()
      ├─ sendNotification()
      ├─ markAsRead()
      ├─ getNotificationsByUser()
      ├─ deleteNotification()
      └─ broadcastNotification()

repository/
  └─ NotificationRepository.java      (Notification data access)
      ├─ findByUserId()
      ├─ findUnread()
      └─ Custom queries

entity/
  └─ NotificationEntity.java          (Notification JPA entity)
      ├─ notificationId (primary key)
      ├─ userId (foreign key)
      ├─ type (enum: SUCCESS, WARNING, INFO, ERROR)
      ├─ title, message
      ├─ isRead
      ├─ relatedEntity (appointment/record ID)
      └─ createdAt

dto/
  └─ NotificationDTO.java (implied)  (Notification DTO)
```

**Total Backend Notification Files**: 5

---

### 4.2 Web Frontend Implementation

**Directory**: `web/src/`

**Files**:
```
pages/
  └─ Notifications.jsx                (Notifications center)
      ├─ Display notifications list
      ├─ Filter by type/read status
      ├─ Mark as read
      └─ Delete notification

pages/
  └─ Notifications.css                (Styling)

context/
  └─ NotificationContext.jsx          (Notification state)
      ├─ notifications list
      ├─ unreadCount
      ├─ loading, error states
      ├─ addNotification()
      ├─ markAsRead()
      ├─ removeNotification()
      └─ fetchNotifications()

hooks/
  ├─ useNotifications.js              (Fetch notifications hook)
  │   ├─ notifications list
  │   ├─ loading, error
  │   └─ fetch()
  │
  └─ useNotificationSender.js         (Send notifications hook)
      ├─ sendNotification()
      └─ Broadcast to other users

services/
  └─ notificationService.js           (Notification API calls)
      ├─ getNotifications()
      ├─ markAsRead()
      ├─ deleteNotification()
      └─ sendNotification()

components/common/
  ├─ NotificationBadge.jsx            (Unread count badge)
  ├─ ToastNotification.jsx            (Toast messages)
  ├─ Modal.jsx                        (Notification details)
  └─ Others (reused)

types/
  └─ index.js                         (Notification schemas)
      └─ NotificationsSchema
```

**Total Web Notification Files**: 9

---

### 4.3 Mobile Implementation (TBD - Proposed Structure)

**Files** (Not yet implemented):
```
ui/
  └─ NotificationsActivity.kt         (Notifications list)

ui/viewmodel/
  └─ NotificationViewModel.kt         (State management)

data/repository/
  └─ NotificationRepository.kt        (Data abstraction)

data/model/
  └─ NotificationModels.kt            (Data classes)
```

**Total Mobile Notification Files**: 4 (proposed)

---

## 5. USER MANAGEMENT FEATURE

### 5.1 Backend Implementation

**Directory**: `backend/src/main/java/edu/cit/bayonas/citmedconnect/`

**Files**:
```
controller/
  └─ UserController.java              (5 endpoints)
      ├─ GET    /api/users/
      ├─ GET    /api/users/{schoolId}
      ├─ PUT    /api/users/{schoolId}
      ├─ DELETE /api/users/{schoolId}
      └─ POST   /api/users/search

service/
  └─ UserService.java                 (Already under Auth, reused)

repository/
  └─ UserRepository.java              (Already under Auth, reused)

entity/
  └─ UserEntity.java                  (Already under Auth, reused)

dto/
  └─ UserDTO.java                     (Already under Auth, reused)

mapper/
  └─ UserMapper.java                  (Already under Auth, reused)
```

**Note**: User management heavily overlaps with Auth. Can be consolidated.

**Total Backend User Management Files**: 2-3 (new controllers/endpoints)

---

### 5.2 Web Frontend Implementation

**Directory**: `web/src/`

**Files**:
```
pages/
  └─ Profile.jsx                      (User profile & settings)
      ├─ Display user info
      ├─ Edit profile form
      ├─ Change password
      ├─ Logout button
      └─ Settings

pages/
  └─ Profile.css                      (Styling)

hooks/
  └─ useUsers.jsx                     (User data hook)
      ├─ users list (admin)
      ├─ searchUsers()
      ├─ getUserById()
      └─ updateUser()

services/
  └─ userService.js                   (User API calls)
      ├─ getCurrentUser()
      ├─ getUser(id)
      ├─ getAllUsers()
      ├─ updateUser()
      ├─ deleteUser()
      └─ searchUsers()

types/
  └─ index.js                         (User schemas)
      └─ UserSchema

components/common/
  ├─ Card.jsx                         (User profile card)
  ├─ Input.jsx                        (Form fields)
  └─ Button.jsx                       (Action buttons)
```

**Total Web User Management Files**: 6

---

### 5.3 Mobile Implementation (TBD - Proposed Structure)

**Files** (Not yet implemented):
```
ui/
  └─ ProfileActivity.kt               (User profile view)

ui/viewmodel/
  └─ ProfileViewModel.kt              (State management)

data/repository/
  └─ UserRepository.kt                (Data abstraction)

data/model/
  └─ UserModels.kt                    (Data classes)
```

**Total Mobile User Management Files**: 4 (proposed)

---

## SUMMARY: COMPLETE FILE INVENTORY

### Backend Files Summary

| Feature | Controllers | Services | Repos | Entities | DTOs | Mappers | Total |
|---------|-------------|----------|-------|----------|------|---------|-------|
| Auth | 2 | 2 | 1 | 1 | 5 | 1 | 12 |
| Appointments | 2 | 2 | 2 | 2 | 2 | - | 10 |
| Medical Records | 1 | 1 | 1 | 1 | 1 | 1 | 6 |
| Notifications | 1 | 1 | 1 | 1 | 1* | - | 5 |
| Users | 1 | - | - | - | - | - | 1 |
| Common | - | - | - | - | - | - | 1 (SecurityConfig) |
| **TOTAL** | **7** | **6** | **5** | **5** | **9** | **2** | **34** |

*Implied/not explicit

---

### Web Frontend Files Summary

| Feature | Pages | Context | Hooks | Services | Types | Components | Styling | Total |
|---------|-------|---------|-------|----------|-------|------------|---------|-------|
| Auth | 2 | 1 | 1 | 3 | 1 | 1 | 2 | 11 |
| Appointments | 2 | 1 | 1 | 1 | 2 | 2 | 2 | 11 |
| Medical Records | 1 | 1 | 1 | 1 | 1 | 2 | 1 | 8 |
| Notifications | 1 | 1 | 2 | 1 | 1 | 3 | 1 | 10 |
| Users | 1 | - | 1 | 1 | 1 | 1 | 1 | 6 |
| Common | 1 | - | - | 3 | 1 | 16 | 1 | 22 |
| **TOTAL** | **8** | **4** | **6** | **9** | **7** | **25** | **8** | **67** |

(Includes: 15 common UI components, api-endpoints.js, api.js, auth-helper.js, data-transformer.js)

---

### Mobile Files Summary (Current + Proposed)

| Feature | Activities | ViewModels | Repositories | Services | Models | Total |
|---------|-----------|-----------|--------------|----------|--------|-------|
| Auth | 2 | 1 | 1 | 2 | 1 | 7 |
| Appointments | - | - | - | - | - | 0 (TBD) |
| Medical Records | - | - | - | - | - | 0 (TBD) |
| Notifications | - | - | - | - | - | 0 (TBD) |
| Users | - | - | - | - | - | 0 (TBD) |
| Common | 1 | - | - | - | - | 1 |
| **TOTAL (Implemented)** | **3** | **1** | **1** | **2** | **1** | **8** |
| **TOTAL (With TBD)** | **7** | **5** | **5** | **6** | **5** | **28** |

---

## PROJECT-WIDE STATISTICS

```
┌─────────────────────────────────────────────────────┐
│         CITMEDCONNECT PROJECT METRICS               │
├─────────────────────────────────────────────────────┤
│                                                     │
│  TOTAL FILES:                                       │
│  ├─ Backend:        34 Java files                  │
│  ├─ Web Frontend:   67 React/JS files              │
│  ├─ Mobile:         8 Kotlin files (growing)       │
│  └─ TOTAL:         109 source files                │
│                                                     │
│  BY LAYER:                                          │
│  ├─ Controllers:    7 (REST endpoints)              │
│  ├─ Services:       13 (business logic)             │
│  ├─ Repositories:   6 (data access)                 │
│  ├─ Entities:       5 (domain models)               │
│  ├─ DTOs:           9 (data transfer)               │
│  ├─ ViewModels:     6 (state management)            │
│  ├─ Components:     40 (UI)                         │
│  └─ Other:          18 (config, utils, etc)        │
│                                                     │
│  BY FEATURE:                                        │
│  ├─ Authentication: 23 files (100% complete)        │
│  ├─ Appointments:   29 files (100% backend/web)    │
│  ├─ Medical Rec:    21 files (100% backend/web)    │
│  ├─ Notifications:  20 files (100% backend/web)    │
│  ├─ Users:          11 files (100% backend/web)    │
│  └─ Cross-cutting:  5 files (config, utils)        │
│                                                     │
│  BY PLATFORM:                                       │
│  ├─ Backend:        34 files (100%)                │
│  ├─ Web:            67 files (100%)                │
│  ├─ Mobile:         8 files (PARTIAL)              │
│                                                     │
│  LINES OF CODE (Estimate):                          │
│  ├─ Backend:        ~5,000 LOC (Java)              │
│  ├─ Web:            ~3,500 LOC (JSX/JS)            │
│  ├─ Mobile:         ~800 LOC (Kotlin)              │
│  └─ TOTAL:          ~9,300 LOC                     │
│                                                     │
│  API ENDPOINTS:                                     │
│  ├─ Auth:           7 endpoints                     │
│  ├─ Appointments:   15 endpoints                    │
│  ├─ Medical Rec:    6 endpoints                     │
│  ├─ Notifications:  5 endpoints                     │
│  ├─ Users:          5 endpoints                     │
│  └─ TOTAL:          38 endpoints                    │
│                                                     │
│  TEST COVERAGE:                                     │
│  ├─ Backend:        0% (1 stub)                    │
│  ├─ Web:            0% (no tests)                  │
│  ├─ Mobile:         0% (no tests)                  │
│  └─ TOTAL:          0% ⚠️ CRITICAL GAP             │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## REFACTORING IMPACT ANALYSIS

### If Moving to Vertical Slice Architecture

**Before (Current Layered)**:
```
- Total restructuring needed
- Folder movement: 34 files → new location (backend)
- No web changes (already good)
- Mobile needs expansion
```

**After (Vertical Slices)**:
```
backend/src/.../feature/
├── auth/              (12 files)
├── appointments/      (10 files)
├── medicalrecords/    (6 files)
├── notifications/     (5 files)
├── users/             (1 file)
└── common/            (1+ files)

web/src/
├── features/
│   ├── auth/          (11 files)
│   ├── appointments/  (11 files)
│   ├── medicalrecords/(8 files)
│   ├── notifications/ (10 files)
│   └── users/         (6 files)
└── shared/            (21 files)

mobile/app/src/.../feature/
├── auth/              (7 files)
├── appointments/      (6 files - new)
├── medicalrecords/    (4 files - new)
├── notifications/     (4 files - new)
└── common/            (1 file)
```

**Effort Estimate**:
- Backend restructuring: 2-3 hours
- Web reorganization: 1-2 hours
- Mobile expansion: 3-4 hours
- Testing setup: 2-3 hours
- **Total: 8-12 hours for structure change**

---

## KEY FINDINGS

1. ✅ **Features Are Complete**: All 5 features have backend + web implementation
2. ⚠️ **Mobile Is Incomplete**: Only Auth implemented, others TBD
3. ❌ **No Tests Exist**: 0% coverage across all platforms
4. ⭐ **Backend Is Functional**: 34 files, proper layering, but organized by layer
5. ⭐ **Web Is Well-Organized**: 67 files, feature-based structure, good patterns
6. ⚠️ **Mobile Has Foundation**: MVVM pattern, needs expansion
7. 📊 **API Is Comprehensive**: 38 endpoints, good coverage for features

---

**Analysis Complete**. All files documented and organized.
