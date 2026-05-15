# FUNCTIONAL REQUIREMENTS DOCUMENT
# CitMedConnect - Multi-Platform Healthcare System

**Date**: May 7, 2026  
**Version**: 1.0  
**Status**: Complete (All requirements implemented)

---

## 1. SYSTEM OVERVIEW

CitMedConnect is a comprehensive healthcare appointment and medical records management system designed for:
- **Students/Patients**: Schedule appointments, view medical history, receive notifications
- **Staff/Healthcare Providers**: Manage appointments, maintain medical records, view schedules
- **Administrators**: User management, system oversight

---

## 2. FUNCTIONAL REQUIREMENTS BY FEATURE

### FEATURE 1: AUTHENTICATION & AUTHORIZATION

#### FR-1.1: User Registration
- **Description**: Users can create new accounts in the system
- **Actors**: Anonymous user (not logged in)
- **Preconditions**: User not already registered with email
- **Steps**:
  1. User navigates to registration page
  2. Enters full name, email, password
  3. Confirms password
  4. Clicks "Sign Up"
- **Success Criteria**:
  - Account created in database
  - User redirected to dashboard
  - Welcome notification shown
  - User can now login
- **Error Handling**:
  - Email already exists → Show error: "Email already registered"
  - Password < 6 chars → Show error: "Password minimum 6 characters"
  - Invalid email format → Show error: "Invalid email format"
- **API Endpoint**: `POST /api/auth/register`
- **HTTP Status**: 201 (Created) on success, 400/409 on error

#### FR-1.2: User Login
- **Description**: Registered users can login to the system
- **Actors**: Registered user
- **Preconditions**: User account exists, credentials are correct
- **Steps**:
  1. User navigates to login page
  2. Enters email and password
  3. Clicks "Sign In"
- **Success Criteria**:
  - User authenticated
  - JWT token generated and stored
  - User redirected to dashboard
  - User role loaded (Student/Staff/Admin)
- **Error Handling**:
  - Invalid email → Show error: "Invalid credentials"
  - Invalid password → Show error: "Invalid credentials"
  - Account locked → Show error: "Account temporarily locked"
- **API Endpoint**: `POST /api/auth/login`
- **HTTP Status**: 200 on success, 401 on failure

#### FR-1.3: GitHub OAuth2 Integration
- **Description**: Users can login using GitHub account
- **Actors**: User with GitHub account
- **Preconditions**: GitHub OAuth app configured
- **Steps**:
  1. User clicks "Sign in with GitHub"
  2. Redirected to GitHub authorization page
  3. User authorizes CitMedConnect
  4. Redirected back with authorization code
  5. Token exchanged server-side
- **Success Criteria**:
  - User authenticated via GitHub
  - Local account created if first time
  - User redirected to dashboard
  - GitHub username/email stored
- **API Endpoint**: `POST /api/oauth2/github/callback`
- **HTTP Status**: 200 on success

#### FR-1.4: Role-Based Access Control
- **Description**: Different users have different permissions based on role
- **Roles**:
  - **Student**: Can schedule appointments, view own medical records
  - **Staff**: Can manage appointments, view patient records, create records
  - **Admin**: Can manage all users, system configuration
- **Access Rules**:
  - Students cannot delete other users' records
  - Staff cannot modify other staff schedules
  - Only admins can delete accounts
- **Enforcement**: Implemented at controller level via Spring Security

#### FR-1.5: Session Management
- **Description**: Sessions expire after inactivity
- **Timeout Duration**: 24 hours (configurable)
- **Refresh Token**: JWT can be refreshed without re-login
- **API Endpoint**: `POST /api/auth/refresh-token`
- **Logout**: `POST /api/auth/logout` clears token

---

### FEATURE 2: APPOINTMENT MANAGEMENT

#### FR-2.1: View Appointments
- **Description**: Users can view their scheduled appointments
- **Actors**: Student (own appointments), Staff (all appointments)
- **Student View**:
  - Personal appointments only
  - Shows date, time, healthcare provider, status
  - Can filter by status (Pending, Confirmed, Completed, Cancelled)
- **Staff View**:
  - All appointments across patients
  - Shows patient name, date, time, status
  - Can filter by date, patient, status
- **API Endpoint**: 
  - `GET /api/appointments/student/my-appointments` (Student)
  - `GET /api/appointments/staff/all` (Staff)
- **Response**: List of AppointmentDTO objects

#### FR-2.2: Schedule Appointment
- **Description**: Student can schedule new appointment with staff
- **Actors**: Student
- **Preconditions**: 
  - Student logged in
  - Staff member exists
  - Time slot available
- **Steps**:
  1. Student selects staff member
  2. Views available time slots
  3. Selects time slot
  4. Enters reason for visit (optional)
  5. Submits appointment request
- **Success Criteria**:
  - Appointment created (status: Pending)
  - Confirmation email sent
  - Appointment appears in both student and staff views
  - Notification sent to staff member
- **Error Handling**:
  - Time slot not available → "Selected slot is no longer available"
  - Staff not accepting appointments → "Staff not available"
- **API Endpoint**: `POST /api/appointments`
- **HTTP Status**: 201 (Created)

#### FR-2.3: Confirm Appointment
- **Description**: Staff confirms scheduled appointment
- **Actors**: Staff member
- **Preconditions**: Appointment exists with status "Pending"
- **Steps**:
  1. Staff views pending appointments
  2. Reviews appointment details
  3. Clicks "Confirm"
- **Success Criteria**:
  - Appointment status changes to "Confirmed"
  - Notification sent to student
  - Calendar updated
- **API Endpoint**: `POST /api/appointments/{id}/confirm`
- **HTTP Status**: 200

#### FR-2.4: Reschedule Appointment
- **Description**: User can change appointment date/time
- **Actors**: Student (own), Staff (any)
- **Preconditions**: Appointment exists, status not "Completed" or "Cancelled"
- **Steps**:
  1. User views appointment
  2. Clicks "Reschedule"
  3. Selects new time slot
  4. Confirms change
- **Success Criteria**:
  - Appointment date/time updated
  - Both parties notified
  - Calendar reflects change
- **API Endpoint**: `POST /api/appointments/{id}/reschedule`
- **HTTP Status**: 200

#### FR-2.5: Cancel Appointment
- **Description**: User can cancel scheduled appointment
- **Actors**: Student (own), Staff (any), Admin (any)
- **Preconditions**: Appointment exists, status not "Completed"
- **Steps**:
  1. User views appointment
  2. Clicks "Cancel"
  3. Optionally enters cancellation reason
  4. Confirms cancellation
- **Success Criteria**:
  - Appointment status changes to "Cancelled"
  - Time slot becomes available again
  - Both parties notified
  - Appointment history preserved
- **API Endpoint**: `DELETE /api/appointments/{id}`
- **HTTP Status**: 200

#### FR-2.6: Complete Appointment
- **Description**: Staff marks appointment as completed after visit
- **Actors**: Staff member
- **Preconditions**: Appointment status "Confirmed", appointment time has passed
- **Steps**:
  1. Staff views confirmed appointments
  2. After patient visit, clicks "Mark Complete"
  3. Optionally adds visit notes
- **Success Criteria**:
  - Appointment status changes to "Completed"
  - Visit notes saved to medical record
  - Student notified
- **API Endpoint**: `POST /api/appointments/{id}/complete`
- **HTTP Status**: 200

---

### FEATURE 3: TIME SLOT MANAGEMENT

#### FR-3.1: Create Time Slots
- **Description**: Staff members define their availability
- **Actors**: Staff, Admin
- **Steps**:
  1. Staff member navigates to "Availability"
  2. Selects date and time
  3. Enters duration (typically 30-60 min)
  4. Sets limit (e.g., 1 patient per slot)
- **Success Criteria**:
  - Time slot created and visible in system
  - Appears in student booking interface
- **API Endpoint**: `POST /api/timeslots`
- **HTTP Status**: 201

#### FR-3.2: View Available Time Slots
- **Description**: Students can see available appointment times
- **Actors**: Student
- **Steps**:
  1. Student selects staff member
  2. Clicks "View Availability"
  3. Views calendar with available slots
- **Success Criteria**:
  - Only available slots shown
  - Past dates hidden
  - Slots show date, time, staff name
- **API Endpoint**: `GET /api/timeslots/available`
- **Response**: List of available TimeSlotDTO

#### FR-3.3: Update Time Slots
- **Description**: Staff can modify their availability
- **Actors**: Staff member
- **Preconditions**: Time slot exists and not booked
- **API Endpoint**: `PUT /api/timeslots/{id}`
- **HTTP Status**: 200

#### FR-3.4: Delete Time Slots
- **Description**: Staff can remove availability
- **Actors**: Staff member
- **Preconditions**: No appointments booked in slot
- **API Endpoint**: `DELETE /api/timeslots/{id}`
- **HTTP Status**: 200

---

### FEATURE 4: MEDICAL RECORDS

#### FR-4.1: View Medical Records
- **Description**: Users can access their medical history
- **Actors**: 
  - Student: Own records only
  - Staff: Patient records they have access to
  - Admin: All records
- **Information Displayed**:
  - Vital signs (BP, HR, Temperature, etc.)
  - Diagnoses and treatments
  - Medications
  - Medical history
  - Appointment notes
- **API Endpoint**: `GET /api/medical-records/user/{userId}`
- **Response**: List of MedicalRecordDTO

#### FR-4.2: Create Medical Record
- **Description**: Staff creates new medical record for patient
- **Actors**: Staff member, Doctor
- **Preconditions**: Patient exists, authenticated staff
- **Steps**:
  1. Staff selects patient
  2. Fills in medical information
  3. Saves record
- **Success Criteria**:
  - Record created and timestamped
  - Patient can view in their records
  - Searchable and indexed
- **API Endpoint**: `POST /api/medical-records`
- **HTTP Status**: 201

#### FR-4.3: Update Medical Record
- **Description**: Staff can modify existing medical records
- **Actors**: Staff member who created record
- **Preconditions**: Record exists, user is staff
- **API Endpoint**: `PUT /api/medical-records/{id}`
- **HTTP Status**: 200

#### FR-4.4: Delete Medical Record
- **Description**: Admin can delete records (audit trail preserved)
- **Actors**: Admin
- **API Endpoint**: `DELETE /api/medical-records/{id}`
- **HTTP Status**: 200

#### FR-4.5: Search Medical Records
- **Description**: Users can search medical history
- **Actors**: Staff, Admin
- **Searchable Fields**:
  - Patient name
  - Date range
  - Diagnosis
  - Medication
- **API Endpoint**: `GET /api/medical-records/search?query=&dateFrom=&dateTo=`
- **Response**: Filtered list of MedicalRecordDTO

---

### FEATURE 5: NOTIFICATIONS

#### FR-5.1: Send Notification
- **Description**: System sends notifications to users
- **Types**:
  - Appointment reminders (24 hours before)
  - Appointment confirmations
  - Medical record updates
  - System announcements
- **Delivery Methods**:
  - In-app notification center
  - (Future: Email, SMS)
- **API Endpoint**: `POST /api/notifications/send`
- **HTTP Status**: 201

#### FR-5.2: View Notifications
- **Description**: Users can view their notifications
- **Actors**: All authenticated users
- **Display**:
  - List of notifications (newest first)
  - Read/unread status
  - Notification type
  - Timestamp
  - Action links (e.g., "View appointment")
- **Pagination**: 20 per page
- **API Endpoint**: `GET /api/notifications`
- **Response**: Paginated list of NotificationDTO

#### FR-5.3: Mark Notification as Read
- **Description**: User marks notification as read
- **Actors**: User who received notification
- **Steps**:
  1. User clicks notification
  2. Notification marked as read
  3. Unread count updated
- **API Endpoint**: `PUT /api/notifications/{id}/read`
- **HTTP Status**: 200

#### FR-5.4: Delete Notification
- **Description**: User can delete notifications
- **Actors**: User
- **API Endpoint**: `DELETE /api/notifications/{id}`
- **HTTP Status**: 200

---

### FEATURE 6: USER MANAGEMENT

#### FR-6.1: View User Profile
- **Description**: Users can view their profile information
- **Actors**: All authenticated users
- **Information**:
  - Name
  - Email
  - School ID
  - Role
  - Contact information
  - Account creation date
- **API Endpoint**: `GET /api/auth/me`
- **Response**: UserDTO

#### FR-6.2: Update User Profile
- **Description**: Users can update their profile
- **Actors**: User (own profile), Admin (any profile)
- **Updatable Fields**:
  - Name
  - Phone number
  - Address (optional)
  - Profile picture (optional)
- **API Endpoint**: `PUT /api/users/{schoolId}`
- **HTTP Status**: 200

#### FR-6.3: User Search (Admin)
- **Description**: Admin can search and manage users
- **Actors**: Admin only
- **Search Criteria**:
  - Name
  - Email
  - Role
  - Registration date
- **Actions**:
  - View user details
  - Modify user role
  - Deactivate/Reactivate account
- **API Endpoint**: `POST /api/users/search`
- **Response**: List of UserDTO

#### FR-6.4: List All Users (Admin)
- **Description**: Admin can view all users in system
- **Actors**: Admin only
- **Display**: Name, email, role, status
- **API Endpoint**: `GET /api/users`
- **Response**: Paginated list of UserDTO

#### FR-6.5: Delete User
- **Description**: Admin can remove user accounts
- **Actors**: Admin only
- **Preconditions**: Cannot delete last admin
- **API Endpoint**: `DELETE /api/users/{schoolId}`
- **HTTP Status**: 200

---

## 3. NON-FUNCTIONAL REQUIREMENTS

### Performance Requirements
- **API Response Time**: < 500ms for 95th percentile
- **Page Load Time**: < 2 seconds on 3G connection
- **Database Query Time**: < 100ms average
- **Concurrent Users**: Support 100+ simultaneous users

### Security Requirements
- **Authentication**: JWT tokens, OAuth2
- **Password Hashing**: BCrypt with salt
- **HTTPS**: All communications encrypted
- **SQL Injection Prevention**: Parameterized queries
- **CORS**: Configured for web and mobile origins
- **Authorization**: Role-based access control

### Reliability Requirements
- **Uptime**: 99.5% availability target
- **Data Backup**: Daily automated backups
- **Recovery Time Objective (RTO)**: < 4 hours
- **Recovery Point Objective (RPO)**: < 1 hour

### Scalability Requirements
- **Horizontal Scaling**: Stateless backend services
- **Database Scaling**: Connection pooling, read replicas
- **Caching**: Redis for session and query caching

---

## 4. SYSTEM CONSTRAINTS

### Technical Constraints
- **Backend**: Java 17, Spring Boot 3.5.11
- **Database**: PostgreSQL 12+
- **Frontend**: React 18.3.1, modern browsers (Chrome, Firefox, Safari, Edge)
- **Mobile**: Android 8.0+ (API 26)

### Business Constraints
- **Data Privacy**: GDPR compliance for EU users
- **Retention**: Medical records retained for 7 years minimum
- **Audit Trail**: All changes logged with timestamp and user

---

## 5. ACCEPTANCE CRITERIA FOR REGRESSION TESTING

### All 38 API Endpoints Must Work
```
Authentication: 7 endpoints
Appointments: 10 endpoints
TimeSlots: 8 endpoints
MedicalRecords: 6 endpoints
Notifications: 5 endpoints
Users: 5 endpoints
OAuth2: 3 endpoints
```

### All Features Must Remain Functional
- ✅ User can register and login
- ✅ User can schedule, reschedule, and cancel appointments
- ✅ Staff can manage time slots and confirm appointments
- ✅ Medical records can be created and viewed
- ✅ Notifications are sent and received
- ✅ User profiles can be created and updated
- ✅ OAuth2 GitHub login works
- ✅ Role-based access control enforced

### No Regressions After Refactoring
- ✅ Database schema unchanged
- ✅ API contracts maintained (request/response format)
- ✅ Business logic unchanged
- ✅ Performance not degraded > 10%
- ✅ No new bugs introduced

---

## 6. TEST COVERAGE TARGETS

| Component | Target | Priority |
|-----------|--------|----------|
| Controllers | 80% | HIGH |
| Services | 90% | HIGH |
| Repositories | 70% | MEDIUM |
| DTOs/Entities | 30% | LOW |
| Utilities | 85% | MEDIUM |
| **Overall Backend** | **70%** | **HIGH** |

| Component | Target | Priority |
|-----------|--------|----------|
| Pages | 60% | HIGH |
| Components | 70% | HIGH |
| Services | 80% | HIGH |
| Hooks | 75% | MEDIUM |
| Utils | 80% | MEDIUM |
| **Overall Web** | **60%** | **HIGH** |

| Component | Target | Priority |
|-----------|--------|----------|
| ViewModels | 70% | HIGH |
| Activities | 50% | HIGH |
| Repositories | 60% | MEDIUM |
| **Overall Mobile** | **50%** | **HIGH** |

---

## 7. TEST SCENARIOS

### Scenario 1: New User Registration and First Appointment
1. New user registers with email
2. Logs in successfully
3. Views available appointments
4. Schedules appointment
5. Receives confirmation notification
6. Reschedules appointment
7. Cancels appointment

### Scenario 2: Staff Member Workflow
1. Staff logs in
2. Views available time slots
3. Modifies availability
4. Reviews pending appointments
5. Confirms appointment
6. Creates medical record after appointment
7. Marks appointment complete

### Scenario 3: Role-Based Access
1. Admin views all users
2. Admin modifies user role
3. Staff member tries to access admin features (should fail)
4. Student tries to access staff features (should fail)
5. Each role sees appropriate data

### Scenario 4: Data Integrity
1. Create appointment
2. Reschedule appointment (old slot available, new slot unavailable)
3. Cancel appointment (all slots available)
4. View medical records (no data loss)
5. Verify audit trail

---

## 8. KNOWN LIMITATIONS

1. **Mobile App**: Only 20% complete (Auth feature only)
2. **Email Notifications**: Not yet implemented (in-app only)
3. **SMS Notifications**: Not implemented
4. **Appointment Reminders**: Manual trigger only (not automated)
5. **Payment Integration**: Not implemented
6. **Telemedicine**: Not implemented

---

## 9. FUTURE ENHANCEMENTS

1. **Appointment Reminders**: Automated email 24 hours before
2. **Video Consultations**: Telemedicine integration
3. **Prescription Management**: Digital prescriptions
4. **Insurance Integration**: Insurance claim submission
5. **Analytics Dashboard**: Appointment and medical trends
6. **Mobile App Completion**: Full feature parity with web
7. **AI-Powered Scheduling**: Intelligent appointment matching
8. **Multi-language Support**: Internationalization

---

**End of Functional Requirements Document**

This document serves as the reference for all regression testing activities.

