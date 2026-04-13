# IT342 Phase 3 - Short Summary

## Main Feature Description
The main web feature implemented is the Appointment Management workflow for CIT MedConnect.
It allows admins to create and manage time slots, and allows students to view available slots and book appointments.
The booking flow is connected to the backend and database, so successful bookings are persisted and reflected in records.

## Inputs and Validations Used
### Time Slot Creation (Admin)
Inputs:
- `slotDate`
- `startTime`
- `endTime`
- `maxBookings`
- `staffId`

Validations:
- Only admin can create, update, or delete time slots.
- `maxBookings` defaults to at least `1`.
- Business-hour flag is computed from the provided time range.

### Appointment Booking (Student)
Inputs:
- `studentId`
- `reason`
- `notes` (optional)
- `timeSlotId` (path parameter)

Validations:
- `studentId` is required.
- Time slot must exist.
- Time slot must be available.
- Current bookings must be less than max bookings.
- User must exist in the system.
- Appointment status is normalized to valid database-supported values.

## How the Feature Works
1. Admin creates available time slots.
2. Students fetch available slots from backend.
3. Student selects a slot and submits booking details.
4. Backend validates user and slot availability.
5. Backend creates appointment and updates slot booking count.
6. If slot reaches max bookings, it is marked unavailable.
7. Booking is saved to database and returned to frontend.
8. Notifications are sent to staff/admin for new appointment events.

## API Endpoints Used
### Time Slots
- `POST /api/timeslots` (admin-only create)
- `GET /api/timeslots` (list all)
- `GET /api/timeslots/available?date=YYYY-MM-DD` (available slots by date)
- `PUT /api/timeslots/{timeSlotId}` (admin-only update)
- `DELETE /api/timeslots/{timeSlotId}` (admin-only delete)

### Appointments
- `POST /api/timeslots/{timeSlotId}/book` (book appointment)
- `GET /api/appointments/student/my-appointments` (student view)
- `GET /api/appointments/staff/all` (staff/admin view)
- `PUT /api/appointments/{id}/cancel`
- `PUT /api/appointments/{id}/reschedule`
- `PUT /api/appointments/{id}/complete`

### Notifications
- `POST /api/notifications/broadcast/staff`
- `POST /api/notifications/broadcast/students`
- `POST /api/notifications/broadcast/all`

## Database Tables Involved
- `users`
  - Stores student, staff, and admin accounts.
- `time_slots`
  - Stores schedulable slot date/time, capacity, and availability.
- `appointments`
  - Stores booked appointment records with status and references to user and slot.
- `notification`
  - Stores user-specific and broadcast notifications.

## Notes
- Appointment persistence is database-backed (not local-only).
- Status handling is normalized in backend to avoid database constraint violations.
- Admin-only slot management is enforced at backend controller level.
