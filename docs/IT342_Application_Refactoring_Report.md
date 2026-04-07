# IT342 Design Patterns - Application and Refactoring Report

## Project
CitMedConnect

## Branch
feature/design-patterns-refactor

## Refactoring Scope Chosen
Only explicit pattern targets were changed. Pre-existing unrelated untracked files were intentionally left untouched.

## Pattern 1: Factory Pattern (Backend Notifications)
### Problem
`NotificationService` created `NotificationEntity` objects in multiple methods with repeated field assignment.

### Refactor
Introduced a factory abstraction:
- `NotificationFactory` interface
- `DefaultNotificationFactory` implementation

`NotificationService` now delegates notification construction to the factory.

### Benefits
- Removes duplication in notification object construction.
- Centralizes notification defaults (`id`, `createdAt`, `isGlobal` policy input).
- Makes future notification variants easier to add.

## Pattern 2: Strategy-Style Role Loaders (Frontend Appointments)
### Problem
`AppointmentContext` repeated role-based branching for loading appointments and slots across many operations.

### Refactor
Extracted reusable role-specific loading functions:
- `loadAppointmentsForRole(currentUser)`
- `loadSlotsForRole(currentUser)`

These are now reused in:
- initial data load
- post-booking refresh
- cancel/create/update/delete slot flows
- manual refresh flow

### Benefits
- Reduces repeated condition chains.
- Keeps role behavior consistent in all flows.
- Makes role-policy updates safer and faster.

## Validation Performed
- Backend compile: Maven test/compile run.
- Frontend build: Vite production build run.

## Files Changed
- `backend/src/main/java/edu/cit/bayonas/citmedconnect/service/NotificationService.java`
- `backend/src/main/java/edu/cit/bayonas/citmedconnect/service/notification/NotificationFactory.java`
- `backend/src/main/java/edu/cit/bayonas/citmedconnect/service/notification/DefaultNotificationFactory.java`
- `web/src/context/AppointmentContext.jsx`

## Reflection
These changes prioritize maintainability without altering domain behavior. The selected patterns fit current pain points (duplication and role-branch sprawl) and provide a clear foundation for further pattern work (for example, appointment status transition strategies or event-driven notifications).
