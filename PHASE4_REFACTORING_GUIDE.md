# PHASE 4: VERTICAL SLICE REFACTORING - IMPLEMENTATION GUIDE

**Duration**: 8-10 hours  
**Branch**: refactor/vertical-slice-architecture  
**Goal**: Restructure CitMedConnect into feature-based vertical slices

---

## 🎯 PHASE 4 OVERVIEW

### What is Vertical Slice Architecture?

**Layered Architecture (Current)**:
```
API Layer (Controllers)
    ↓
Business Layer (Services)
    ↓
Data Layer (Repositories)
    ↓
Database
```
Problem: Features are scattered horizontally across all layers

**Vertical Slice Architecture (Target)**:
```
Feature 1          Feature 2          Feature 3
├── Controller     ├── Controller     ├── Controller
├── Service        ├── Service        ├── Service
├── Repository     ├── Repository     ├── Repository
├── Entity         ├── Entity         ├── Entity
├── DTO            ├── DTO            ├── DTO
└── Tests          └── Tests          └── Tests
```
Benefit: Each feature is self-contained and independently testable

### Why This Matters

✅ **Easier to understand** - One folder = one feature  
✅ **Easier to test** - Independent feature testing  
✅ **Easier to scale** - Add new features without touching existing ones  
✅ **Easier to maintain** - Clear boundaries between features  
✅ **Team-friendly** - Different teams can work on different slices  

---

## 📋 REFACTORING ROADMAP

### Phase 4 Structure

```
4.1: Backend Refactoring (4-5 hours)
├── 4.1.1: Prepare & Create shared config folder (0.5h)
├── 4.1.2: Refactor Auth Feature (1h)
├── 4.1.3: Refactor Users Feature (0.75h)
├── 4.1.4: Refactor Appointments Feature (1.5h)
├── 4.1.5: Refactor Medical Records Feature (1.25h)
├── 4.1.6: Refactor Notifications Feature (1h)
└── 4.1.7: Refactor OAuth2 Feature (0.75h)

4.2: Web Frontend Refactoring (2-3 hours)
└── 4.2.1: Organize into feature folders

4.3: Verification (1 hour)
├── 4.3.1: Full test suite
├── 4.3.2: Endpoint verification
└── 4.3.3: Regression checklist

4.4: Commit & Cleanup (0.5 hours)
└── 4.4.1: Final commit and documentation
```

---

## 🔧 STEP-BY-STEP BACKEND REFACTORING

### STEP 4.1.1: Prepare and Create Shared Config (30 min)

**Objective**: Create folder structure for shared, cross-cutting concerns

**1.1a: Create the shared folder structure**

```bash
cd backend/src/main/java/edu/cit/bayonas/citmedconnect

# Create directories
mkdir -p shared/config
mkdir -p shared/exception
mkdir -p shared/util
mkdir -p shared/dto
```

**1.1b: Files to move to shared/config/**

From `config/`:
- Move: `SecurityConfig.java` → `shared/config/SecurityConfig.java`

**1.1c: Create shared exception handlers (NEW)**

Create: `shared/exception/GlobalExceptionHandler.java`

```java
package edu.cit.bayonas.citmedconnect.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

**1.1d: Update pom.xml**

The pom.xml already has all needed dependencies from Phase 2. No changes needed.

**1.1e: Test the shared config**

```bash
cd backend
mvn clean compile
# Should compile without errors
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-config): create shared configuration folder structure"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.2: Refactor Auth Feature (1 hour)

**Objective**: Move all authentication-related classes into vertical slice

**2.1a: Create auth feature folder structure**

```bash
cd backend/src/main/java/edu/cit/bayonas/citmedconnect

mkdir -p features/auth/controller
mkdir -p features/auth/service
mkdir -p features/auth/repository
mkdir -p features/auth/entity
mkdir -p features/auth/dto
mkdir -p features/auth/mapper
mkdir -p features/auth/test
```

**2.1b: Files to move**

Move these files INTO `features/auth/`:

FROM `controller/`:
- `AuthController.java` → `features/auth/controller/AuthController.java`

FROM `service/`:
- `AuthService.java` → `features/auth/service/AuthService.java`

FROM `repository/`:
- `UserRepository.java` → `features/auth/repository/UserRepository.java`

FROM `entity/`:
- `UserEntity.java` → `features/auth/entity/UserEntity.java`

FROM `dto/`:
- `RegisterRequest.java` → `features/auth/dto/RegisterRequest.java`
- `LoginRequest.java` → `features/auth/dto/LoginRequest.java`
- `AuthResponse.java` → `features/auth/dto/AuthResponse.java`

FROM `mapper/`:
- `UserMapper.java` → `features/auth/mapper/UserMapper.java`

**2.1c: Update package declarations**

In each moved file, update the package statement. For example:

```java
// OLD
package edu.cit.bayonas.citmedconnect.controller;

// NEW
package edu.cit.bayonas.citmedconnect.features.auth.controller;
```

**2.1d: Update imports in moved files**

Example in `features/auth/service/AuthService.java`:

```java
// Before
import edu.cit.bayonas.citmedconnect.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;

// After
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
```

**2.1e: Update other services that depend on Auth**

Files like `OAuth2Service.java` depend on auth. Update their imports:

```java
// Update to
import edu.cit.bayonas.citmedconnect.features.auth.service.AuthService;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
```

**2.1f: Update SecurityConfig imports**

In `shared/config/SecurityConfig.java`:

```java
// Update to
import edu.cit.bayonas.citmedconnect.features.auth.service.AuthService;
```

**2.1g: Test compilation**

```bash
cd backend
mvn clean compile
# Should compile without errors
```

**2.1h: Test endpoints**

Verify auth endpoints still work:
```bash
# Just check compilation for now
# Full testing comes in Phase 6
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-auth): move authentication feature to vertical slice"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.3: Refactor Users Feature (45 min)

**Objective**: Move user management classes (separate from auth)

**3.1a: Create users feature folder**

```bash
mkdir -p features/users/controller
mkdir -p features/users/service
mkdir -p features/users/repository
mkdir -p features/users/entity
mkdir -p features/users/dto
mkdir -p features/users/mapper
```

**3.1b: Files to move**

Note: `UserEntity` is already moved to auth. Users feature is mostly about the *UserController* for admin operations.

FROM `controller/`:
- `UserController.java` → `features/users/controller/UserController.java`

FROM `service/`:
- `UserService.java` → `features/users/service/UserService.java`

FROM `dto/`:
- `UserDTO.java` → `features/users/dto/UserDTO.java`

FROM `mapper/`:
- `UserMapper.java` → `features/users/mapper/UserMapper.java`

**3.1c: Handle UserEntity and UserRepository**

These are now in auth. Update imports in UserService:

```java
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
```

**3.1d: Test compilation**

```bash
mvn clean compile
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-users): move user management to vertical slice"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.4: Refactor Appointments Feature (1.5 hours)

**Objective**: Move appointment and timeslot classes

**4.1a: Create appointments feature folders**

```bash
mkdir -p features/appointments/controller
mkdir -p features/appointments/service
mkdir -p features/appointments/repository
mkdir -p features/appointments/entity
mkdir -p features/appointments/dto
mkdir -p features/appointments/mapper
```

**4.1b: Files to move**

FROM `controller/`:
- `AppointmentController.java` → `features/appointments/controller/`
- `TimeSlotController.java` → `features/appointments/controller/`

FROM `service/`:
- `AppointmentService.java` → `features/appointments/service/`
- `TimeSlotService.java` → `features/appointments/service/`

FROM `repository/`:
- `AppointmentRepository.java` → `features/appointments/repository/`
- `TimeSlotRepository.java` → `features/appointments/repository/`

FROM `entity/`:
- `AppointmentEntity.java` → `features/appointments/entity/`
- `TimeSlot.java` → `features/appointments/entity/`

FROM `dto/`:
- `AppointmentDTO.java` → `features/appointments/dto/`
- `TimeSlotDTO.java` → `features/appointments/dto/`

**4.1c: Update all package declarations and imports**

Update imports to:
- `edu.cit.bayonas.citmedconnect.features.appointments.*`
- `edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity` (dependency)

**4.1d: Update NotificationService dependency**

AppointmentService depends on NotificationService. Add:

```java
import edu.cit.bayonas.citmedconnect.features.notifications.service.NotificationService;
```

**4.1e: Test compilation**

```bash
mvn clean compile
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-appointments): move appointments to vertical slice"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.5: Refactor Medical Records Feature (1.25 hours)

**Objective**: Move medical record classes

**5.1a: Create medical records feature folders**

```bash
mkdir -p features/medical-records/controller
mkdir -p features/medical-records/service
mkdir -p features/medical-records/repository
mkdir -p features/medical-records/entity
mkdir -p features/medical-records/dto
mkdir -p features/medical-records/mapper
```

**5.1b: Files to move**

FROM `controller/`:
- `MedicalRecordController.java`

FROM `service/`:
- `MedicalRecordService.java`

FROM `repository/`:
- `MedicalRecordRepository.java`

FROM `entity/`:
- `MedicalRecordEntity.java`

FROM `dto/`:
- `MedicalRecordDTO.java`

FROM `mapper/`:
- `MedicalRecordMapper.java`

**5.1c: Update all imports**

Package: `edu.cit.bayonas.citmedconnect.features.medical-records.*`

Dependencies:
- UserEntity from auth
- Appointments from appointments

**5.1d: Test compilation**

```bash
mvn clean compile
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-medical-records): move medical records to vertical slice"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.6: Refactor Notifications Feature (1 hour)

**Objective**: Move notification system

**6.1a: Create notifications feature folders**

```bash
mkdir -p features/notifications/controller
mkdir -p features/notifications/service
mkdir -p features/notifications/repository
mkdir -p features/notifications/entity
mkdir -p features/notifications/dto
```

**6.1b: Files to move**

Move all notification-related files to `features/notifications/`

**6.1c: Update imports**

Package: `edu.cit.bayonas.citmedconnect.features.notifications.*`

**6.1d: Test compilation**

```bash
mvn clean compile
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-notifications): move notifications to vertical slice"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.7: Refactor OAuth2 Feature (45 min)

**Objective**: Move OAuth2 authentication

**7.1a: Create oauth2 feature folders**

```bash
mkdir -p features/oauth2/controller
mkdir -p features/oauth2/service
mkdir -p features/oauth2/dto
```

**7.1b: Files to move**

FROM `controller/`:
- `OAuth2Controller.java`

FROM `service/`:
- `OAuth2Service.java`

FROM `dto/`:
- `GitHubOAuth2UserInfo.java`
- `OAuth2AuthResponse.java`

**7.1c: Update imports**

Package: `edu.cit.bayonas.citmedconnect.features.oauth2.*`

Dependencies:
- UserEntity from auth
- AuthService from auth

**7.1d: Test compilation**

```bash
mvn clean compile
```

**Commit:**
```bash
git add -A
git commit -m "refactor(backend-oauth2): move oauth2 to vertical slice"
git push origin refactor/vertical-slice-architecture
```

---

### STEP 4.1.8: Final Backend Verification

**Check directory structure:**

```bash
ls -R backend/src/main/java/edu/cit/bayonas/citmedconnect/

# Should show:
# features/
#   ├── auth/
#   ├── users/
#   ├── appointments/
#   ├── medical-records/
#   ├── notifications/
#   └── oauth2/
# shared/
#   └── config/
```

**Full compilation test:**

```bash
cd backend
mvn clean install -q
echo "✅ Backend compiles and all tests pass"
```

**Commit final verification:**

```bash
git add -A
git commit -m "refactor(backend): verify vertical slice refactoring complete"
git push origin refactor/vertical-slice-architecture
```

---

## 🌐 WEB FRONTEND REFACTORING

### STEP 4.2: Reorganize Web Frontend (2-3 hours)

**Objective**: Organize React components into feature folders

**Current structure:**
```
src/
├── pages/
├── components/
├── services/
├── context/
├── hooks/
├── utils/
```

**Target structure:**
```
src/
├── features/
│   ├── auth/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── services/
│   │   ├── context/
│   │   ├── hooks/
│   │   └── __tests__/
│   ├── appointments/
│   ├── medical-records/
│   ├── notifications/
│   ├── users/
│   └── shared/
│       ├── components/
│       ├── services/
│       ├── hooks/
│       ├── context/
│       ├── utils/
│       └── types/
```

**2.1: Create feature folder structure**

```bash
cd web/src

mkdir -p features/auth
mkdir -p features/appointments
mkdir -p features/medical-records
mkdir -p features/notifications
mkdir -p features/users
mkdir -p features/shared
```

**2.2: Move auth-related files**

From `pages/`: Landing.jsx, OAuth2Callback.jsx → `features/auth/pages/`  
From `components/`: (login/register specific) → `features/auth/components/`  
From `context/`: AuthContext.jsx → `features/auth/context/`  
From `hooks/`: useAuth.jsx → `features/auth/hooks/`  
From `services/`: auth-helper.js, github-oauth-service.js → `features/auth/services/`  

**2.3: Move other features similarly**

Appointments, Medical Records, Notifications, Users

**2.4: Move common/shared items**

From `components/common/`: → `features/shared/components/`  
From `utils/`: → `features/shared/utils/`  
From `types/`: → `features/shared/types/`  

**2.5: Update imports**

This is the tedious part. Update imports in all moved files to use new paths:

```javascript
// OLD
import { useAuth } from '../../hooks/useAuth'
import Button from '../../components/common/Button'

// NEW
import { useAuth } from '../auth/hooks/useAuth'
import Button from '../shared/components/Button'
```

**2.6: Update App.jsx**

Update route imports:

```javascript
// OLD
import Landing from './pages/Landing'

// NEW
import Landing from './features/auth/pages/Landing'
```

**2.7: Test web build**

```bash
npm run build
# Should succeed
```

**Commit:**

```bash
git add -A
git commit -m "refactor(web): organize frontend into feature-based structure"
git push origin refactor/vertical-slice-architecture
```

---

## ✅ VERIFICATION & TESTING

### STEP 4.3: Full Verification (1 hour)

**3.1: Backend comprehensive test**

```bash
cd backend
mvn clean test  # Run all unit tests

# Should show: BUILD SUCCESS
```

**3.2: Backend API verification**

Test key endpoints still work (manual or with REST Client):
- POST /api/auth/register
- POST /api/auth/login
- GET /api/appointments
- etc.

**3.3: Web build and serve**

```bash
cd web
npm run build  # Production build
npm run dev    # Start dev server
# Verify pages load in browser
```

**3.4: Check for import errors**

```bash
# Look for any "Cannot find module" errors in console
# Fix any import paths
```

**3.5: Verify no circular dependencies**

Circular dependencies are when:
- Feature A imports from Feature B, AND
- Feature B imports from Feature A

This breaks the vertical slice pattern!

Check:
- Auth should NOT import from other features
- Notifications can import from other features (it's cross-cutting)
- Each feature should be mostly independent

**Commit verification:**

```bash
git add -A
git commit -m "refactor: verify vertical slice refactoring - all tests pass"
git push origin refactor/vertical-slice-architecture
```

---

## 📚 FINAL DOCUMENTATION

### STEP 4.4: Update Architecture Documentation

Create: `UPDATED_ARCHITECTURE.md`

Document:
- New folder structure
- Feature boundaries
- Import rules
- Dependency diagram

---

## 🎯 PHASE 4 SUCCESS CRITERIA

Verify ALL before moving to Phase 5:

- ✅ All files organized into features/
- ✅ Backend compiles without errors
- ✅ Web builds successfully
- ✅ All unit tests pass
- ✅ API endpoints still functional
- ✅ No circular dependencies
- ✅ Clear feature boundaries
- ✅ Commit history shows feature-by-feature refactoring
- ✅ Documentation updated

---

## 📊 ESTIMATED TIMELINE

| Task | Duration |
|------|----------|
| Backend Shared Config | 0.5h |
| Backend Auth | 1h |
| Backend Users | 0.75h |
| Backend Appointments | 1.5h |
| Backend Medical Records | 1.25h |
| Backend Notifications | 1h |
| Backend OAuth2 | 0.75h |
| Backend Verification | 0.5h |
| **Backend Total** | **7 hours** |
| Web Refactoring | 2-2.5h |
| Web Verification | 0.5h |
| **Web Total** | **2.5-3 hours** |
| **TOTAL PHASE 4** | **9.5-10 hours** |

---

**Ready to begin? Start with Step 4.1.1 (Prepare Shared Config)**

