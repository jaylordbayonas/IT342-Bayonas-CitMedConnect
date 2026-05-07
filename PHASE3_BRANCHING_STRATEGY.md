# PHASE 3: BRANCHING STRATEGY & SETUP

**Duration**: 30 minutes  
**Goal**: Create proper branching structure for safe refactoring  
**Status**: READY TO EXECUTE

---

## 🎯 OBJECTIVES

1. Create a dedicated refactoring branch
2. Set up commit conventions
3. Establish branch protection rules (optional)
4. Prepare for long-running refactoring work

---

## 📋 BRANCHING STRATEGY

### Branch Naming Convention

We will use **feature/refactoring branches** following Git Flow:

```
refactor/vertical-slice-architecture

Pattern: <type>/<description>
- Type: refactor (for architectural changes)
- Description: clear, hyphen-separated
```

### Why This Branch?

- **Isolates refactoring work** from main branch
- **Allows for incremental commits** without affecting production
- **Enables code review** before merging back to main
- **Provides rollback point** if issues arise
- **Professional workflow** following industry standards

---

## 🔄 COMMIT CONVENTION

### Commit Message Format

All commits during refactoring should follow this pattern:

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Types:
- `refactor` - Code restructuring, no behavior change
- `test` - Adding tests
- `docs` - Documentation updates
- `chore` - Build, dependencies, tooling
- `fix` - Bug fixes discovered during refactoring
- `feat` - New features (if needed)

#### Scope:
- `backend-auth` - Authentication feature
- `backend-appointments` - Appointments feature
- `web-auth` - Web auth feature
- `config` - Shared configuration
- etc.

#### Examples:

```
refactor(backend-auth): move UserService to auth feature folder

Move UserService and related classes from service/ to 
features/auth/service/ to establish vertical slice boundary

- Move UserService.java
- Move UserRepository.java
- Update package imports
- Run tests to verify no regressions
```

```
test(backend-auth): add unit tests for AuthService

Added comprehensive unit tests for authentication service:
- testRegisterNewUser()
- testLoginWithValidCredentials()
- testLoginWithInvalidPassword()

Coverage increased from 0% to 85% for AuthService
```

### Why Good Commit Messages?

- **Traceable history** - Understand why changes were made
- **Bisect-friendly** - Find regressions easily
- **Professional** - Shows engineering rigor
- **Documentation** - Serves as commit history documentation

---

## 🚀 STEP-BY-STEP EXECUTION

### STEP 1: Create Refactoring Branch

```bash
# Make sure you're on main branch
git status

# Verify main is up-to-date
git pull origin main

# Create new branch
git checkout -b refactor/vertical-slice-architecture

# Verify you're on new branch
git status
```

**Expected Output**:
```
On branch refactor/vertical-slice-architecture
nothing to commit, working tree clean
```

### STEP 2: Push Branch to Remote (Optional but Recommended)

```bash
# Push branch to GitHub
git push -u origin refactor/vertical-slice-architecture

# Verify it's on remote
git branch -v
```

**Expected Output**:
```
* refactor/vertical-slice-architecture [...] 
  main                               [...] origin/main
```

### STEP 3: Update Local Repo Config for Commits

(Optional - Sets up commit template if desired)

```bash
# Create .gitmessage file in project root
cat > .gitmessage << 'EOF'
# <type>(<scope>): <subject>
# |<----  Using 50 characters for subject  ----->|
#
# Explain why this change is needed (context)
# |<----  Wrap at 72 characters for body  ----->|
#
# <body>
#
# <footer>
# Closes #
EOF

# Configure git to use this template
git config commit.template .gitmessage
```

This makes it easier to follow commit conventions.

---

## 📊 BRANCH STRUCTURE AFTER SETUP

```
Origin (GitHub):
  main ← stable branch, don't work directly here
    └── refactor/vertical-slice-architecture ← YOUR WORK BRANCH

Local:
  main ← synced with origin/main
  refactor/vertical-slice-architecture ← YOUR WORKING BRANCH (current)
```

---

## ⚠️ IMPORTANT RULES DURING REFACTORING

### DO ✅
- ✅ Commit frequently (every 30-60 minutes)
- ✅ Write descriptive commit messages
- ✅ Test after each feature refactoring
- ✅ Keep commits focused on one feature
- ✅ Push to remote regularly for backup
- ✅ Document breaking changes
- ✅ Verify all builds pass

### DON'T ❌
- ❌ Make unrelated changes in refactoring commits
- ❌ Refactor multiple features in one commit
- ❌ Ignore test failures
- ❌ Commit to main directly
- ❌ Forget to test after refactoring
- ❌ Push broken code
- ❌ Make major API changes without planning

---

## 🔀 MERGING BACK TO MAIN (After Refactoring Complete)

### When Refactoring is Done:

```bash
# Make sure everything is committed
git status  # Should show "working tree clean"

# Switch to main
git checkout main

# Pull latest
git pull origin main

# Merge refactoring branch
git merge --no-ff refactor/vertical-slice-architecture \
  -m "Merge: vertical slice architecture refactoring"

# Push merged code to main
git push origin main

# Delete refactoring branch (keep history)
git branch -d refactor/vertical-slice-architecture
git push origin --delete refactor/vertical-slice-architecture
```

### Merge Verification:
```bash
# Verify all tests pass
mvn clean test  # Backend
npm run test:run  # Web

# Verify main builds
mvn clean compile  # Backend
npm run build  # Web

# Tag release (optional)
git tag -a v1.1.0-vertical-slice -m "Vertical Slice Architecture"
git push origin v1.1.0-vertical-slice
```

---

## 📈 REFACTORING PHASES (Next Phase)

Once this branch is created, Phase 4 will follow this sequence:

```
Phase 4: Vertical Slice Refactoring
├── 4.1 Shared Configuration (0.5h)
│   └── Create shared/ folder for cross-cutting concerns
│
├── 4.2 Auth Feature (1h)
│   ├── Create features/auth/ folder structure
│   ├── Move auth-related classes
│   └── Test auth endpoint functionality
│
├── 4.3 Users Feature (0.75h)
│   ├── Create features/users/ folder structure
│   ├── Move user-related classes
│   └── Test user endpoints
│
├── 4.4 Appointments Feature (1.5h)
│   ├── Create features/appointments/ folder structure
│   ├── Move appointment + timeslot classes
│   └── Test appointment workflows
│
├── 4.5 Medical Records Feature (1.25h)
│   ├── Create features/medical-records/ folder structure
│   ├── Move medical record classes
│   └── Test medical record operations
│
├── 4.6 Notifications Feature (1h)
│   ├── Create features/notifications/ folder structure
│   ├── Move notification classes
│   └── Test notification system
│
├── 4.7 OAuth2 Feature (0.75h)
│   ├── Create features/oauth2/ folder structure
│   ├── Move OAuth2 classes
│   └── Test GitHub OAuth2 flow
│
├── 4.8 Web Frontend (2h)
│   ├── Organize into features/ structure
│   ├── Update imports and service calls
│   └── Verify all pages load correctly
│
└── 4.9 Verification (1h)
    ├── Run full test suite
    ├── Verify all endpoints work
    └── Check for circular dependencies
```

---

## 🧪 COMMIT STRATEGY DURING REFACTORING

### Commit After Each Feature:

**Example commits that will be made**:

```
refactor(backend-config): create shared configuration folder
refactor(backend-auth): move auth feature to vertical slice
refactor(backend-users): move users feature to vertical slice
refactor(backend-appointments): move appointments to vertical slice
refactor(backend-medical): move medical records to vertical slice
refactor(backend-notifications): move notifications to vertical slice
refactor(backend-oauth2): move oauth2 to vertical slice
refactor(web): organize frontend into feature folders
test(backend-auth): add unit tests for auth service
test(backend-appointments): add appointment service tests
docs(architecture): update documentation for new structure
```

### How Frequently to Commit:

- **After moving each feature** (1 commit per feature = 7-8 commits)
- **After fixing imports** (1 commit)
- **After writing tests** (1 commit per test suite)
- **Total expected**: 15-20 commits

Each commit should be:
- ✅ Functional (code compiles)
- ✅ Testable (tests pass)
- ✅ Isolated (one feature or one type of change)
- ✅ Documented (clear commit message)

---

## 📋 BRANCH STATUS COMMANDS

```bash
# Check current branch
git status

# List all branches
git branch -v

# Show commit history
git log --oneline --graph --all

# Show branches tracking remote
git branch -r

# Compare main vs refactoring branch
git diff main refactor/vertical-slice-architecture --stat

# See commits not yet on main
git log main..refactor/vertical-slice-architecture --oneline
```

---

## ⏱️ TIMELINE

| Task | Time | Status |
|------|------|--------|
| Create branch | 2 min | ⏳ TO DO |
| Push to remote | 1 min | ⏳ TO DO |
| Configure commits | 3 min | ⏳ TO DO |
| Verify setup | 2 min | ⏳ TO DO |
| **Total Phase 3** | **~10 min** | |

**Then → Phase 4 (8-10 hours) begins immediately**

---

## ✅ PHASE 3 SUCCESS CRITERIA

Verify ALL of these before starting Phase 4:

- [ ] New branch created: `refactor/vertical-slice-architecture`
- [ ] `git status` shows correct branch
- [ ] Branch pushed to remote
- [ ] `git log --oneline` shows you're on new branch
- [ ] No uncommitted changes
- [ ] Ready to start refactoring
- [ ] Commit convention understood
- [ ] Know when to commit

---

## 🚀 READY TO START PHASE 3?

Run this command:

```bash
git status
```

You should see:
```
On branch main
Your branch is ahead of 'origin/main' by 3 commits.
nothing to commit, working tree clean
```

Then proceed with Step 1 above!

---

**Next**: Phase 4 - Vertical Slice Refactoring (8-10 hours)

