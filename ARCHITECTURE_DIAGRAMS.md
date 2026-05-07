# CitMedConnect - Architecture Diagrams & Visualizations

## 1. FEATURE DEPENDENCY GRAPH

```
                    ┌─────────────────────────────────┐
                    │     AUTHENTICATION              │
                    │  Controllers: AuthController    │
                    │  Services: UserService,         │
                    │            OAuth2Service        │
                    │  Repos: UserRepository          │
                    │  Entities: UserEntity           │
                    └────────┬─────────────────────────┘
                             │
                ┌────────────┼──────────────────┐
                │            │                  │
        ┌───────▼──────┐  ┌──▼────────┐  ┌─────▼───────────┐
        │ APPOINTMENTS │  │   USERS   │  │ NOTIFICATIONS   │
        │              │  │           │  │                 │
        │ Controllers: │  │ Controllers│  │ Controllers:    │
        │  Appointment │  │  User     │  │  Notification   │
        │  TimeSlot    │  │           │  │                 │
        │              │  │ Services: │  │ Services:       │
        │ Services:    │  │  UserSvc  │  │  Notification   │
        │  Appointment │  │           │  │  Service        │
        │  TimeSlot    │  │ Repos:    │  │                 │
        │              │  │  UserRepo │  │ Repos:          │
        │ Repos:       │  │           │  │  Notification   │
        │  Appointment │  │ Entities: │  │  Repository     │
        │  TimeSlot    │  │  UserEntity│  │                 │
        │              │  │           │  │ Entities:       │
        │ Entities:    │  │ Mappers:  │  │  Notification   │
        │  Appointment │  │  UserMapper│  │  Entity         │
        │  TimeSlot    │  │           │  │                 │
        └───────┬──────┘  └──┬────────┘  └────┬────────────┘
                │            │                 │
                │ (Dependency)                 │ (Calls)
                └────────────┼─────────────────┘
                             │
                    ┌────────▼──────────────┐
                    │  MEDICAL RECORDS      │
                    │                       │
                    │ Controllers:          │
                    │  MedicalRecord        │
                    │                       │
                    │ Services:            │
                    │  MedicalRecord       │
                    │                       │
                    │ Repos:               │
                    │  MedicalRecord       │
                    │                       │
                    │ Entities:            │
                    │  MedicalRecord       │
                    │                       │
                    │ Mappers:             │
                    │  MedicalRecordMapper │
                    └───────────────────────┘

Legend:
→   Depends on / Must complete before
← → Bidirectional calls
```

---

## 2. BACKEND LAYERED ARCHITECTURE (CURRENT)

```
┌─────────────────────────────────────────────────────────┐
│                     REST CONTROLLERS                     │
│  ┌────────────┬──────────┬─────────┬─────────┬─────┬──┐ │
│  │ Auth       │Appoint   │Medical  │Notif    │User │TS│ │
│  │Controller  │Controller│Record   │Control  │Ctrl │Ct│ │
│  │ + OAuth2   │          │         │         │     │rl│ │
│  └────────────┴──────────┴─────────┴─────────┴─────┴──┘ │
└────────────────────────────────────────────────────────┬─┘
                        ▲                                │
                        │                                │
┌───────────────────────┴────────────────────────────────┴──┐
│                    BUSINESS LOGIC LAYER                   │
│  ┌────────────┬──────────┬─────────┬─────────┬─────┬──┐  │
│  │UserService │Appt Svc  │Medical  │Notif    │Auth │TS │  │
│  │            │& TimeSlot│Record   │Service  │Svc  │Svc│  │
│  │            │Service   │Service  │         │     │   │  │
│  └────────────┴──────────┴─────────┴─────────┴─────┴──┘  │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▲
┌───────────────────────┴──────────────────────────────────┐
│               DATA ACCESS LAYER (Repositories)           │
│  ┌────────────┬──────────┬─────────┬─────────┬─────┐   │
│  │UserRepo    │Appt Repo │Medical  │Notif    │TS   │   │
│  │            │& TimeSlot│Record   │Repo     │Repo │   │
│  │            │Repo      │Repo     │         │     │   │
│  └────────────┴──────────┴─────────┴─────────┴─────┘   │
└───────────────────────┬──────────────────────────────────┘
                        │
                        ▲
┌───────────────────────┴──────────────────────────────────┐
│               DATABASE LAYER                             │
│  ┌────────────┬──────────┬─────────┬─────────┬─────┐   │
│  │users       │appt      │medical  │notif    │time │   │
│  │table       │_entity   │_records │_entity  │_slot│   │
│  │            │table     │table    │table    │table│   │
│  └────────────┴──────────┴─────────┴─────────┴─────┘   │
│                                                          │
│            PostgreSQL Database (Supabase)              │
└──────────────────────────────────────────────────────────┘

ISSUE: Features scattered across all layers horizontally
SOLUTION: Reorganize into vertical slices (by feature)
```

---

## 3. PROPOSED VERTICAL SLICE ARCHITECTURE (BACKEND)

```
┌──────────────────────────────────────────────────────────┐
│  edu.cit.bayonas.citmedconnect.feature.{FEATURE_NAME}   │
│  (Each feature is self-contained)                        │
├──────────────────────────────────────────────────────────┤
│
│  ┌──── AUTH FEATURE SLICE ────┐
│  │ auth/                       │
│  │  ├── controller/            │
│  │  │   └── AuthController.java│
│  │  ├── service/               │
│  │  │   ├── AuthService.java   │
│  │  │   ├── UserService.java   │
│  │  │   └── OAuth2Service.java │
│  │  ├── repository/            │
│  │  │   └── UserRepository.java│
│  │  ├── entity/                │
│  │  │   └── UserEntity.java    │
│  │  └── dto/                   │
│  │      ├── LoginRequest.java  │
│  │      └── AuthResponse.java  │
│  └─────────────────────────────┘
│
│  ┌── APPOINTMENTS FEATURE SLICE─┐
│  │ appointments/               │
│  │  ├── controller/            │
│  │  │   ├── Appointment..ert   │
│  │  │   └── TimeSlotCtrl.java  │
│  │  ├── service/               │
│  │  │   ├── AppointmentService │
│  │  │   └── TimeSlotService    │
│  │  ├── repository/            │
│  │  │   ├── Appointment..pository
│  │  │   └── TimeSlotRep..      │
│  │  ├── entity/                │
│  │  │   ├── AppointmentEntity  │
│  │  │   └── TimeSlot.java      │
│  │  └── dto/                   │
│  │      ├── AppointmentDTO     │
│  │      └── TimeSlotDTO        │
│  └─────────────────────────────┘
│
│  [MEDICAL RECORDS, NOTIFICATIONS, USERS similarly]
│
│  ┌──── COMMON / SHARED ────┐
│  │ common/                 │
│  │  ├── config/            │
│  │  │   └── Security..     │
│  │  ├── exception/         │
│  │  │   └── GlobalExc..    │
│  │  ├── util/              │
│  │  └── mapper/            │
│  │      └── (Shared mappers)
│  └─────────────────────────┘
│
└──────────────────────────────────────────────────────────┘

BENEFIT: Each feature is independent, testable unit
```

---

## 4. FRONTEND STATE MANAGEMENT (React + Context API)

```
                        App Root
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    AuthProvider      NotificationProvider  AuditLogProvider
        │                  │                  │
        ├─ user           ├─ notifications   ├─ logs
        ├─ loading        ├─ unreadCount     └─ actions
        ├─ error          ├─ addNotif()
        ├─ login()        └─ markAsRead()
        ├─ logout()
        └─ refresh()

Protected Routes
        │
    ┌───┴────┬─────────────┬──────────┬───────────┬────────┐
    │        │             │          │           │        │
Dashboard Appointments  Calendar Medical   Notifications Profile
    │        │             │       Records      │        │
    │    AppointmentProvider                    │        │
    │    ├─ appointments                        │        │
    │    ├─ loading                             │        │
    │    ├─ create()                            │        │
    │    ├─ update()                            │        │
    │    └─ cancel()                            │        │
    │                                           │        │
    │                                       MedicalRecordsContext
    │                                       ├─ records
    │                                       ├─ fetch()
    │                                       └─ create()

Services Layer (Axios-based):
├─ api-endpoints.js (URL config)
├─ appointment-service.js
├─ medical-records-service.js
├─ notification-service.js
├─ user-service.js
└─ auth-helper.js (token management)
```

---

## 5. MOBILE MVVM ARCHITECTURE

```
┌─────────────────────────────────────────────────────────┐
│                  UI LAYER (Activities)                   │
│  ┌────────────┬──────────┬──────────┬──────────────┐   │
│  │ Main       │Login     │Register  │Dashboard     │   │
│  │ Activity   │Activity  │Activity  │Activity      │   │
│  │            │          │          │              │   │
│  │ (TBD)      │ (TBD)    │ (TBD)    │ (TBD)        │   │
│  └────────────┴──────────┴──────────┴──────────────┘   │
└──────────────────────────────────────┬──────────────────┘
                                       │
                          Observes & Notifies
                                       │
┌──────────────────────────────────────┴──────────────────┐
│               VIEWMODEL LAYER                           │
│  ┌─────────────────────────────────────────────────┐   │
│  │ AuthViewModel                                   │   │
│  │ ├─ username: LiveData<String>                   │   │
│  │ ├─ password: LiveData<String>                   │   │
│  │ ├─ authState: LiveData<AuthState>               │   │
│  │ ├─ login(credentials)                           │   │
│  │ ├─ register(user)                               │   │
│  │ ├─ logout()                                     │   │
│  │ └─ validateCredentials()                        │   │
│  └─────────────────────────────────────────────────┘   │
│  (Additional VMs: Appointment, Medical, etc. - TBD)    │
└──────────────────────────────────────┬──────────────────┘
                                       │
                        Calls Repository
                                       │
┌──────────────────────────────────────┴──────────────────┐
│              REPOSITORY LAYER                           │
│  ┌─────────────────────────────────────────────────┐   │
│  │ AuthRepository                                  │   │
│  │ ├─ login(credentials)                           │   │
│  │ ├─ register(user)                               │   │
│  │ ├─ logout()                                     │   │
│  │ └─ getAuthToken()                               │   │
│  └─────────────────────────────────────────────────┘   │
│  (Appointment, Medical, etc. - TBD)                    │
└──────────────────────────────────────┬──────────────────┘
                                       │
                        Calls API Services
                                       │
┌──────────────────────────────────────┴──────────────────┐
│              NETWORK LAYER (Services)                   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ RetrofitClient.kt                               │   │
│  │ ├─ baseUrl = http://localhost:8080/api          │   │
│  │ └─ interceptors: [Logging, Auth]                │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │ AuthService.kt                                  │   │
│  │ ├─ login(credentials): Observable<AuthResp>     │   │
│  │ ├─ register(user): Observable<AuthResp>         │   │
│  │ └─ logout(): Observable<Boolean>                │   │
│  └─────────────────────────────────────────────────┘   │
└──────────────────────────────────────┬──────────────────┘
                                       │
                         Makes HTTP Calls
                                       │
                    ┌─────────────────────────────┐
                    │    Backend API Server       │
                    │  http://localhost:8080/api │
                    └─────────────────────────────┘
```

---

## 6. DATA FLOW DIAGRAM

### Request Flow (Happy Path)

```
User Action (Click, Form Submit)
        │
        ▼
Component State Update
        │
        ▼
Call Service Method
(appointment-service.js)
        │
        ▼
Axios HTTP Request
        │
        ▼
Backend Controller
(AppointmentController)
        │
        ▼
Spring Security Check
(Is user authenticated?)
        │
        ├─ NO ──► Return 401 Unauthorized
        │
        ▼ YES
Service Layer
(AppointmentService.createAppointment)
        │
        ▼
Business Logic
(Validate timeSlot, check user, etc.)
        │
        ▼
Repository Call
(appointmentRepository.save)
        │
        ▼
Database INSERT/UPDATE
        │
        ▼
Response DTO (AppointmentDTO)
        │
        ▼
Service Layer Response
        │
        ▼
Controller Response
        │
        ▼
Axios Response Handler
        │
        ├─ Success ──► Parse DTO
        │              │
        │              ▼
        │         Update State (Context/Hook)
        │              │
        │              ▼
        │         Component Re-render
        │              │
        │              ▼
        │         UI Update
        │
        └─ Error ──► Parse Error Response
                      │
                      ▼
                 Show Error Toast/Modal
                      │
                      ▼
                 Log to AuditLog
```

---

## 7. SERVICE LAYER DEPENDENCIES

```
Controller Layer
    │
    └─ AppointmentController
        │
        ├─ Calls → AppointmentService
        │           │
        │           ├─ Dependency: AppointmentRepository
        │           ├─ Dependency: UserRepository
        │           ├─ Dependency: TimeSlotRepository
        │           └─ Dependency: NotificationService
        │                           │
        │                           └─ Dependency: NotificationRepository
        │
        ├─ Calls → TimeSlotService
        │           │
        │           ├─ Dependency: TimeSlotRepository
        │           └─ Dependency: UserRepository
        │
        ├─ Uses → SecurityContext
        │   (Get current user from Spring Security)
        │
        └─ Returns → DTO (AppointmentDTO)

        
Authorization Flow:
1. Request comes with JWT token (in header)
2. SecurityConfig validates token
3. Extract user identity from token
4. Set in SecurityContextHolder
5. Controller accesses via 
   SecurityContextHolder.getContext().getAuthentication()
6. UserRepository.findById(username)
7. Check user role (STUDENT vs STAFF)
8. Perform operation or deny
```

---

## 8. FEATURE COUNT BY MODULE

### Backend Services

```
┌─────────────────┬────────┬──────────┬──────────┬────────┐
│ Feature         │ Entity │ Repo     │ Service  │ DTO    │
├─────────────────┼────────┼──────────┼──────────┼────────┤
│ Auth            │ 1 (U)  │ 1        │ 2 (U+O)  │ 3      │
│ Appointments    │ 2 (A+T)│ 2        │ 2 (A+T)  │ 2      │
│ Medical Records │ 1      │ 1        │ 1        │ 1      │
│ Notifications   │ 1      │ 1        │ 1        │ 1      │
│ Users           │ 1      │ 1        │ 1        │ 1      │
├─────────────────┼────────┼──────────┼──────────┼────────┤
│ TOTAL           │ 7      │ 6        │ 7        │ 8      │
└─────────────────┴────────┴──────────┴──────────┴────────┘

(U=User, O=OAuth2, A=Appointment, T=TimeSlot)
```

### Frontend Components

```
┌─────────────────┬───────────┬──────────┬────────┬────────┐
│ Feature         │ Pages     │ Services │ Hooks  │ Context│
├─────────────────┼───────────┼──────────┼────────┼────────┤
│ Auth            │ 1 (Landing)│ 2       │ 1      │ 1      │
│ Appointments    │ 2 (Appt+Cal)│ 1      │ 1      │ 1      │
│ Medical Records │ 1         │ 1        │ 1      │ 1      │
│ Notifications   │ 1         │ 1        │ 1      │ 1      │
│ Users/Profile   │ 1         │ 1        │ 1      │ -      │
├─────────────────┼───────────┼──────────┼────────┼────────┤
│ TOTAL           │ 8         │ 9        │ 6      │ 5      │
└─────────────────┴───────────┴──────────┴────────┴────────┘

Plus: 1 OAuth2Callback, 1 Dashboard, 15 UI Components
```

---

## 9. ERROR HANDLING FLOW (Current vs Proposed)

### Current Error Handling (Incomplete)

```
Error Occurs
    │
    ├─ If Caught:
    │   └─ Logged to console
    │       │
    │       └─ Frontend shows generic "Error occurred"
    │
    ├─ If Not Caught:
    │   └─ 500 Internal Server Error
    │       │
    │       └─ Stack trace sent to client (security issue!)
    │
    └─ No specific error codes or structured responses
```

### Proposed Error Handling

```
Error Occurs
    │
    ├─ Service Layer catches
    │   ├─ Validates error type
    │   ├─ Logs with context (userId, action, timestamp)
    │   └─ Creates AppException
    │
    └─ Global Exception Handler (@ControllerAdvice)
        ├─ Catches AppException
        ├─ Maps to ErrorResponse DTO
        │   ├─ statusCode: 400/401/403/404/500
        │   ├─ message: User-friendly message
        │   ├─ errorCode: Unique error code
        │   ├─ timestamp: When error occurred
        │   └─ details: {field, constraint} for validation
        │
        ├─ Logs error with correlation ID
        ├─ Sends ErrorResponse (application/json)
        │
        └─ Frontend receives
            ├─ Parses error code
            ├─ Shows user-friendly message
            ├─ Logs to AuditLog
            └─ Displays Toast notification
```

---

## 10. DEPLOYMENT ARCHITECTURE

```
┌──────────────────────────────────────────────────────────┐
│                  CLIENT BROWSERS                         │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ React App    │  │ OAuth2 Login │  │ Dashboard    │   │
│  │ (Vite)       │  │ (GitHub)     │  │ Views        │   │
│  │ localhost:   │  │ flow         │  │              │   │
│  │ 3000 or 5173 │  │              │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                          │
└────────┬────────────────────────────────┬────────────────┘
         │                                │
         ▼ HTTP/HTTPS                     ▼ OAuth2
    ┌────────────┐                  ┌──────────────┐
    │  Spring    │                  │  GitHub API  │
    │  Boot API  │◄─ OAuth Tokens ──┤              │
    │ localhost: │                  │ oauth.github │
    │   8080     │                  │    .com      │
    └────────────┘                  └──────────────┘
         │
         ▼ JDBC/PostgreSQL Connections
    ┌──────────────────────────────────┐
    │ PostgreSQL Database              │
    │ (Supabase Cloud)                 │
    │                                  │
    │ aws-1-ap-northeast-2.pooler.    │
    │ supabase.com:6543               │
    │                                  │
    │ Tables:                          │
    │  • users                         │
    │  • appointments                  │
    │  • timeslots                     │
    │  • medical_records               │
    │  • notifications                 │
    └──────────────────────────────────┘

Production Deployment (Proposed):
┌──────────────────────────────────────────────────────────┐
│                    Nginx (Reverse Proxy)                │
└────┬─────────────────────────────────────┬───────────────┘
     │                                      │
     ▼ Static Assets                        ▼ /api requests
┌──────────────┐                   ┌──────────────────┐
│ S3/CloudFront│                   │ Docker Container │
│ (React Build)│                   │ Spring Boot App  │
└──────────────┘                   └────┬─────────────┘
                                        │
                                        ▼
                            ┌───────────────────────┐
                            │ PostgreSQL (Managed)  │
                            │ AWS RDS/Cloud SQL     │
                            └───────────────────────┘
```

---

## 11. TESTING PYRAMID

```
        ▲
       /│\
      / │ \
     /  │  \  E2E Tests (5-10%)
    /   │   \  ✗ Missing
   /    │    \──────────────────
  /     │     \
 /      │      \  Integration Tests (15-25%)
/       │       \  ✗ Missing
────────┼────────\──────────────────────
        │         \
        │          \ Unit Tests (65-80%)
        │           \ ✗ Missing (Except stubs)
        │            \
        ├────────────────────────────────
        │
       code

REQUIRED FOR REFACTORING:
1. Unit Tests
   ├── Service layer tests
   ├── Repository tests
   └── Component/Hook tests (React)
   
2. Integration Tests
   ├── API endpoint tests
   ├── Database transaction tests
   └── Cross-feature integration
   
3. E2E Tests
   ├── User workflows
   ├── Appointment booking flow
   └── OAuth2 login flow

Testing Frameworks:
Backend: JUnit 5 + Mockito + Spring Boot Test
Web:     Vitest/Jest + React Testing Library
Mobile:  JUnit + Mockito + Espresso
```

---

## 12. CONTINUOUS INTEGRATION PIPELINE (Proposed)

```
┌─ Developer pushes to GitHub
│
├─ GitHub Actions triggered
│
├─ Build Stage
│  ├─ Backend
│  │  ├─ mvn clean compile
│  │  ├─ Run SpotBugs/SonarQube
│  │  └─ Build JAR
│  │
│  ├─ Web
│  │  ├─ npm install
│  │  ├─ npm run build
│  │  └─ ESLint check
│  │
│  └─ Mobile
│     ├─ ./gradlew build
│     ├─ Android Lint
│     └─ Build APK
│
├─ Test Stage
│  ├─ Backend
│  │  ├─ JUnit tests
│  │  └─ Integration tests (TestContainers)
│  │
│  ├─ Web
│  │  ├─ Vitest/Jest
│  │  └─ Coverage report
│  │
│  └─ Mobile
│     ├─ JUnit tests
│     └─ Espresso E2E (if configured)
│
├─ Code Quality
│  ├─ SonarQube analysis
│  ├─ Code coverage
│  └─ Security scan (SAST)
│
└─ Deploy (if all pass)
   ├─ Backend → Cloud Run / App Engine
   ├─ Web → S3 + CloudFront
   └─ Mobile → Play Store / TestFlight
```

---

## Summary: Current vs Proposed Architecture

| Aspect | Current | Proposed | Complexity |
|--------|---------|----------|-----------|
| Backend Organization | Layered (by layer) | Vertical Slices (by feature) | ⭐⭐⭐ |
| Frontend Organization | Partial features | Full features | ⭐⭐ |
| Mobile Architecture | MVVM (partial) | Complete MVVM | ⭐⭐ |
| Testing | Stubs only | Full coverage | ⭐⭐⭐⭐ |
| Error Handling | Incomplete | Comprehensive | ⭐⭐⭐ |
| API Documentation | Comments | OpenAPI/Swagger | ⭐⭐ |
| Pagination | Missing | Implemented | ⭐⭐ |
| Caching | None | Redis cache | ⭐⭐⭐ |
| Logging | Minimal | Structured logging | ⭐⭐ |
| Deployment | Local only | CI/CD automated | ⭐⭐⭐ |
