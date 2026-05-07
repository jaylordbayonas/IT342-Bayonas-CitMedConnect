# PHASE 2: ENVIRONMENT PREPARATION GUIDE

**Status**: IN PROGRESS  
**Duration**: 2-3 hours  
**Goal**: Prepare the project for safe refactoring by resolving blockers and adding test infrastructure

---

## EXECUTIVE SUMMARY

Before we refactor, we must:
1. ✅ **Resolve uncommitted changes** (10 min) - CLEAN GIT STATE
2. ✅ **Add testing dependencies** (DONE - 30 min)
3. ✅ **Build and verify all platforms** (1-2 hours)
4. ✅ **Create test configuration files** (30 min)

---

## STEP-BY-STEP CHECKLIST

### STEP 1: Resolve Uncommitted Changes ⛔ (10 MINUTES)

**Current Status**:
```
Modified:   web/package-lock.json
Untracked:  web/public/
```

**Actions**:

```bash
# Navigate to project root
cd "c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect"

# Check current status
git status

# Stage all uncommitted changes
git add web/package-lock.json web/public/

# Commit with clear message
git commit -m "chore: update web dependencies and public assets for Phase 2"

# Verify clean working tree
git status
```

**Expected Output**:
```
On branch main
Your branch is ahead of 'origin/main' by 1 commit.
  (use "git push" if you want to share your local commits)

nothing to commit, working tree clean
```

**✅ COMPLETE**: Working tree should be clean before proceeding.

---

### STEP 2: Backend - Build with New Testing Dependencies (30 MINUTES)

**Status**: Dependencies added to pom.xml ✅

**Actions**:

```bash
# Navigate to backend directory
cd backend

# Clean and build with test dependencies
mvn clean install

# This will:
# - Download all test dependencies
# - Compile main and test code
# - Run any existing tests (CitmedconnectApplicationTests)
# - Generate classpath for IDE
```

**Expected Output**:
```
[INFO] Building citmedconnect 0.0.1-SNAPSHOT
[INFO] -------< edu.cit.bayonas:citmedconnect >-------
...
[INFO] --- maven-compiler-plugin:3.11.0:compile (default-compile) ---
[INFO] BUILD SUCCESS
```

**If you see errors**:
- ❌ Check Java version: `java -version` (should be 17+)
- ❌ Check Maven: `mvn -version` (should be 3.9+)
- ❌ Clear cache: `mvn clean`

**⏳ This will take 2-5 minutes** (first run downloads many jars)

---

### STEP 3: Web Frontend - Install Testing Dependencies (20 MINUTES)

**Status**: Dependencies added to package.json ✅

**Actions**:

```bash
# Navigate to web directory
cd ../web

# Install dependencies (includes testing libraries)
npm install

# This will install:
# - Vitest (test runner)
# - React Testing Library
# - jsdom (browser simulation)
# - Coverage tools

# Verify installation
npm list vitest @testing-library/react
```

**Expected Output**:
```
npm notice
npm notice New patch version of npm available: X.X.X → X.X.X
npm notice
added XXX packages
```

**⏳ This will take 3-5 minutes**

**Verify Vitest works**:
```bash
npm run test:run
```

You should see: `0 test files` (no tests written yet, which is normal)

---

### STEP 4: Mobile - Build with Testing Dependencies (30 MINUTES)

**Status**: Dependencies added to build.gradle ✅

**Actions**:

```bash
# Navigate to mobile directory
cd ../mobile

# For Windows, use gradlew.bat (already configured)
# This will download Gradle and all testing dependencies
gradlew clean build

# This will:
# - Download Gradle wrapper
# - Resolve all test dependencies
# - Compile Kotlin code
# - Validate Android manifest
# - Create APK
```

**Expected Output**:
```
> Task :app:assembleDebug
> Task :app:build

BUILD SUCCESSFUL in XXs
```

**If you see errors**:
- ❌ Android SDK not found: Need to set ANDROID_HOME environment variable
- ❌ Java version mismatch: Need JDK 17+
- ❌ Permission denied on gradlew: Run `chmod +x gradlew` (Mac/Linux only)

---

### STEP 5: Create Test Configuration Files (30 MINUTES)

#### Backend: Create test properties file

**File**: `backend/src/test/resources/application-test.properties`

```properties
# Test Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Security Configuration
spring.security.oauth2.client.registration.github.clientId=test-client-id
spring.security.oauth2.client.registration.github.clientSecret=test-client-secret
spring.security.oauth2.client.provider.github.user-info-uri=https://api.github.com/user

# Logging Configuration
logging.level.root=WARN
logging.level.edu.cit.bayonas=DEBUG
```

**Purpose**: H2 in-memory database for fast unit tests without PostgreSQL

#### Web: Create Vitest configuration file

**File**: `web/vitest.config.js`

```javascript
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.js',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
      ]
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
})
```

**File**: `web/src/test/setup.js`

```javascript
import '@testing-library/jest-dom'

// Mock window.matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
})

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
}
global.localStorage = localStorageMock
```

#### Mobile: Create test configuration

**File**: `mobile/app/src/test/resources/robolectric.properties`

```properties
# Robolectric Configuration
sdk=34
```

---

### STEP 6: Verify All Builds Succeed (20 MINUTES)

**Run from project root**:

```bash
cd "c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect"

# Backend check
echo "=== BACKEND BUILD ==="
cd backend && mvn clean compile -q && echo "✅ Backend compiles" && cd ..

# Web check
echo "=== WEB BUILD ==="
cd web && npm run build -q && echo "✅ Web builds" && cd ..

# Mobile check
echo "=== MOBILE BUILD ==="
cd mobile && gradlew assembleDebug -q && echo "✅ Mobile builds" && cd ..

echo "=== ALL BUILDS SUCCESSFUL ==="
```

**Expected Output**:
```
=== BACKEND BUILD ===
✅ Backend compiles

=== WEB BUILD ===
✅ Web builds

=== MOBILE BUILD ===
✅ Mobile builds

=== ALL BUILDS SUCCESSFUL ===
```

---

### STEP 7: Create Functional Requirements Document (30 MINUTES)

**File**: Create `FUNCTIONAL_REQUIREMENTS.md` at project root

This document will help us understand what to test during regression testing.

---

## ✅ PHASE 2 SUCCESS CRITERIA

Before moving to Phase 3, verify ALL of these:

- [ ] **Uncommitted changes committed** - `git status` shows clean working tree
- [ ] **Backend compiles** - `mvn clean compile` succeeds
- [ ] **Web builds** - `npm run build` succeeds
- [ ] **Mobile builds** - `gradlew assembleDebug` succeeds
- [ ] **Test frameworks installed** - `npm list vitest` shows v1.1.0+
- [ ] **Maven test dependencies** - pom.xml has Mockito, H2, TestContainers
- [ ] **Test config files created** - application-test.properties, vitest.config.js exist
- [ ] **Git commits pushed** (optional but recommended)

---

## 🚨 TROUBLESHOOTING

### Issue: Maven build fails - "JAVA_HOME not set"

**Solution**:
```bash
# Find Java installation
where java

# Set JAVA_HOME (replace path with your Java location)
setx JAVA_HOME "C:\Program Files\Java\jdk-17"

# Restart terminal and try again
mvn -version
```

### Issue: npm install fails - "ERR! code ERESOLVE"

**Solution**:
```bash
# Clear npm cache
npm cache clean --force

# Install with legacy peer deps
npm install --legacy-peer-deps
```

### Issue: gradlew fails - "Permission denied"

**Solution** (Mac/Linux only):
```bash
chmod +x gradlew
./gradlew clean build
```

### Issue: "Cannot resolve symbol" in IDE after adding dependencies

**Solution**:
- ✅ Maven: Right-click project → Maven → Reload Projects
- ✅ Gradle: Sync Now in Android Studio
- ✅ NPM: Restart IDE and run `npm install` again

---

## 📝 COMMANDS SUMMARY

```bash
# Phase 2 Complete Execution (copy-paste all at once)
cd "c:\Users\JAYLORD\OneDrive\Desktop\IT342-Bayonas-CitMedConnect"

# 1. Commit changes
git add web/package-lock.json web/public/
git commit -m "chore: update web dependencies and public assets"

# 2. Build all platforms
cd backend && mvn clean install && cd ..
cd web && npm install && npm run build && cd ..
cd mobile && gradlew clean build && cd ..

# 3. Verify clean state
git status
echo "✅ Phase 2 Complete!"
```

---

## ⏭️ NEXT PHASE

Once Phase 2 is complete:
1. ✅ Git state is clean
2. ✅ All platforms build successfully
3. ✅ Test dependencies installed
4. ✅ Test configuration files created

**Next**: Phase 3 - Branching Strategy

---

**Estimated Time**: 2-3 hours  
**Difficulty**: Low  
**Risk**: None (preparing environment, no code changes yet)

