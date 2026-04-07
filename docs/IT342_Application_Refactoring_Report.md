# IT342 Design Patterns - Application and Refactoring Report

## 1. Project Context
- Project: CitMedConnect
- Branch: feature/design-patterns-refactor
- Reference commit: a15949355bc63a47a442dd1c63e68820cae41e75
- Scope rule used: only selected refactor targets were changed; unrelated untracked files were intentionally excluded.

## 2. Refactoring Goals
This refactoring focused on practical improvements in the existing codebase, not theoretical rewrites.

Target outcomes:
- Better code organization
- Higher reusability
- Easier maintenance
- Safer scalability for future features

## 3. Pattern A - Factory Pattern (Backend Notification Creation)

### 3.1 Original Implementation (Before)
In NotificationService, NotificationEntity objects were built manually in multiple methods:
- sendNotificationToAllStudents
- sendNotificationToEveryone
- sendNotificationToAllStaff
- sendNotificationToUser

Before snippet (representative):

```java
NotificationEntity notification = new NotificationEntity();
notification.setNotificationId(UUID.randomUUID().toString());
notification.setSchoolId(student.getSchoolId());
notification.setTitle(title);
notification.setMessage(message);
notification.setNotificationType(type);
notification.setIsGlobal(false);
notification.setCreatedAt(LocalDateTime.now());
notifications.add(notificationRepository.save(notification));
```

### 3.2 Problems / Limitations
- Repeated object-construction logic in many locations.
- High risk of inconsistent defaults when adding fields.
- Harder to maintain and extend broadcast rules.

### 3.3 Applied Pattern and Location
- Pattern name: Factory Pattern
- Applied in:
	- backend/src/main/java/edu/cit/bayonas/citmedconnect/service/NotificationService.java
	- backend/src/main/java/edu/cit/bayonas/citmedconnect/service/notification/NotificationFactory.java
	- backend/src/main/java/edu/cit/bayonas/citmedconnect/service/notification/DefaultNotificationFactory.java

### 3.4 Refactored Implementation (After)

Factory interface:

```java
public interface NotificationFactory {
		NotificationEntity create(String recipientSchoolId, String title, String message, String type, boolean isGlobal);
}
```

Factory usage inside NotificationService:

```java
NotificationEntity notification = notificationFactory.create(
		student.getSchoolId(),
		title,
		message,
		type,
		false
);
notifications.add(notificationRepository.save(notification));
```

### 3.5 Justification
Factory was selected because object creation was the repeated pain point. This pattern cleanly centralizes creation rules without altering service-level business logic.

### 3.6 Improvements Achieved
- Maintainability: single place to update notification defaults.
- Reusability: all service methods use the same creation mechanism.
- Scalability: easier to introduce specialized factories later (for example, templated notifications).

## 4. Pattern B - Strategy-Style Role Loader Refactor (Frontend)

### 4.1 Original Implementation (Before)
AppointmentContext repeated role-based if/else chains for loading appointments and slots across multiple flows:
- initial load
- refresh after booking
- cancel flow
- time slot CRUD refresh

Before snippet (representative):

```javascript
let appointmentsData = [];
if (isStaffOrAdmin(user?.role)) {
	appointmentsData = await appointmentService.getAllAppointments();
} else if (isStudentRole(user?.role)) {
	try {
		appointmentsData = await appointmentService.getStudentAppointments();
	} catch {
		appointmentsData = await appointmentService.getUserAppointments(user.userId || user.schoolId);
	}
} else {
	appointmentsData = await appointmentService.getUserAppointments(user.userId || user.schoolId);
}
```

### 4.2 Problems / Limitations
- Duplicated role-resolution logic in several methods.
- Increased chance of inconsistent behavior between flows.
- Difficult and risky role policy updates.

### 4.3 Applied Pattern and Location
- Pattern name: Strategy (role-based loading strategy, lightweight functional form)
- Applied in:
	- web/src/context/AppointmentContext.jsx

### 4.4 Refactored Implementation (After)

Reusable appointment loader:

```javascript
const loadAppointmentsForRole = useCallback(async (currentUser) => {
	const actualUserId = currentUser?.userId || currentUser?.schoolId;

	if (isStaffOrAdmin(currentUser?.role)) {
		return appointmentService.getAllAppointments();
	}

	if (isStudentRole(currentUser?.role)) {
		try {
			return await appointmentService.getStudentAppointments();
		} catch (studentError) {
			return appointmentService.getUserAppointments(actualUserId);
		}
	}

	return appointmentService.getUserAppointments(actualUserId);
}, [appointmentService]);
```

Reusable slot loader:

```javascript
const loadSlotsForRole = useCallback(async (currentUser) => {
	if (isStudentRole(currentUser?.role)) {
		return appointmentService.getAvailableSlots();
	}

	if (isStaffOrAdmin(currentUser?.role)) {
		try {
			return await appointmentService.getStaffSlots();
		} catch (staffSlotsError) {
			return appointmentService.getAvailableSlots();
		}
	}

	return [];
}, [appointmentService]);
```

### 4.5 Justification
Strategy was selected because behavior changes by role (student, staff/admin). Extracting role-dependent behavior into dedicated loader functions keeps the context logic consistent and reusable.

### 4.6 Improvements Achieved
- Code organization: role logic extracted from action handlers.
- Reusability: same loaders reused in multiple workflows.
- Maintainability: role changes now happen in one location.
- Scalability: easier to add new roles or access rules later.

## 5. Functional Validation
Validation executed after refactor:
- Backend compile: mvnw.cmd -DskipTests compile (success)
- Frontend build: npm run build (success)

## 6. Files Changed
- backend/src/main/java/edu/cit/bayonas/citmedconnect/service/NotificationService.java
- backend/src/main/java/edu/cit/bayonas/citmedconnect/service/notification/NotificationFactory.java
- backend/src/main/java/edu/cit/bayonas/citmedconnect/service/notification/DefaultNotificationFactory.java
- web/src/context/AppointmentContext.jsx

## 7. Commit Evidence
Completed refactor commit:
- Refactor: apply Factory and role-based loader strategies

Suggested commit-message style for future pattern commits:
- Applied Factory Pattern to Notification Service
- Refactored AppointmentContext with role-based loading strategy
- Added report: before-vs-after pattern refactoring analysis

## 8. Conclusion
The applied patterns were chosen based on real duplication and role-branch complexity in CitMedConnect. The refactor improved readability and maintainability while preserving behavior. This meets the activity requirement of practical, project-based design pattern application without overengineering.

## 9. PDF Submission Notes
To submit as PDF, export this markdown to PDF using VS Code (Markdown Preview -> Print -> Save as PDF) and attach it with the GitHub branch/commit evidence.
