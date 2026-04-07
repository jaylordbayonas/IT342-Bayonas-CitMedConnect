# IT342 Refactored Code Guide (CitMedConnect)

## Objective
Use design patterns in the existing CitMedConnect codebase with realistic, low-risk refactoring.

## Completed Refactor Targets
1. Factory Pattern for notification creation in backend service layer.
2. Strategy-style role loaders for appointment and time-slot retrieval in frontend context layer.

## Step-by-Step Workflow You Can Follow

### Step 1 - Pick only one pain point per pattern
Use this filter:
- Is there repeated object creation? -> Factory candidate.
- Is behavior changing by role/state/type? -> Strategy candidate.
- Is object adaptation needed between modules? -> Adapter candidate.

### Step 2 - Create a small integration-safe abstraction
Example done in backend:
- NotificationFactory interface
- DefaultNotificationFactory implementation

### Step 3 - Replace duplicate code incrementally
Update one service method at a time to use the abstraction.
Verify each replacement still preserves previous behavior.

### Step 4 - Centralize branch-heavy logic
Example done in frontend:
- loadAppointmentsForRole(currentUser)
- loadSlotsForRole(currentUser)

Then reuse these in:
- initial loading
- refresh
- booking/cancellation follow-up
- slot CRUD follow-up

### Step 5 - Validate before commit
Run:
- Backend: mvnw.cmd -DskipTests compile
- Frontend: npm run build

### Step 6 - Commit with evidence-based messages
Suggested sequence:
1. Applied Factory Pattern to Notification Service
2. Applied role-based loading strategy in AppointmentContext
3. Added complete refactoring report with before/after evidence

## Refactor Boundaries (to avoid overengineering)
- Do not rewrite working modules unless there is clear duplication or complexity.
- Keep business behavior unchanged.
- Prefer introducing one abstraction at a time.

## Optional Next Pattern (if instructor asks for more)
Possible third target in this system:
- Strategy for appointment status transitions in backend AppointmentService

Why this is valid:
- Status rules are conditional and evolve over time.
- Transition policies can be encapsulated per status action.

Keep it optional unless required, because current two-pattern implementation is already functional and defensible.
