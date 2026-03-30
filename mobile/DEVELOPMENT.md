# CitMedConnect Mobile Development Guide

## Development Setup

### 1. Environment Requirements

- **Android Studio**: Download from [developer.android.com](https://developer.android.com/studio)
- **JDK 17**: Required for Kotlin compilation
- **Android SDK**: 
  - Min SDK: API 26
  - Target SDK: API 34
  - Compile SDK: API 34

### 2. Initial Setup

#### Clone and Open Project
```bash
# Navigate to mobile folder
cd mobile

# Open in Android Studio
# File > Open > Select mobile folder
```

#### Gradle Sync
```
File > Sync Now
```

This will download all dependencies and build the project structure.

### 3. Configure Backend Connection

Edit `app/src/main/java/edu/cit/bayonas/citmedconnect/data/api/RetrofitClient.kt`:

```kotlin
// For Android Emulator
private const val BASE_URL = "http://10.0.2.2:8080/"

// For Physical Device
private const val BASE_URL = "http://<your-machine-ip>:8080/"
```

### 4. Running the Application

#### Method A: Using Emulator

1. **Create Virtual Device**:
   - Android Studio > Device Manager
   - Create New Device (Pixel 5, API 34)
   - Start the AVD

2. **Run App**:
   ```
   Run > Run 'app' (Shift + F10)
   ```

#### Method B: Using Physical Device

1. **Enable USB Debugging**:
   - Settings > About > Tap build number 7 times
   - Settings > Developer Options > USB Debugging

2. **Connect Device**:
   ```
   adb devices  # Verify device is listed
   ```

3. **Run App**:
   ```
   Run > Run 'app' (Shift + F10)
   ```

## Project Architecture

### MVVM Pattern

```
UI Layer (Activities)
    ↓
ViewModel (State Management)
    ↓
Repository (Data Access)
    ↓
API Service (Network)
    ↓
Data Models
```

### File Organization

```
data/
├── api/           # Network calls (Retrofit)
├── model/         # Data classes (DTO)
└── repository/    # Business logic

ui/
├── LoginActivity.kt
├── RegisterActivity.kt
├── DashboardActivity.kt
└── viewmodel/     # MVVM ViewModels
```

## Making API Calls

### Step 1: Add Request/Response Models

Update `AuthModels.kt` with new models:

```kotlin
data class UserProfileRequest(
    val userId: String
)

data class UserProfileResponse(
    val success: Boolean,
    val data: UserProfile?
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String
)
```

### Step 2: Add API Endpoint

Update `AuthService.kt`:

```kotlin
@GET("api/user/{userId}")
suspend fun getUserProfile(@Path("userId") userId: String): UserProfileResponse
```

### Step 3: Update Repository

Add method to `AuthRepository.kt`:

```kotlin
suspend fun getUserProfile(userId: String): UserProfileResponse {
    return authService.getUserProfile(userId)
}
```

### Step 4: Update ViewModel

Add to `AuthViewModel.kt`:

```kotlin
private val _userProfile = MutableLiveData<UserProfile>()
val userProfile: LiveData<UserProfile> = _userProfile

fun fetchUserProfile(userId: String) {
    _isLoading.value = true
    viewModelScope.launch {
        try {
            val response = repository.getUserProfile(userId)
            _userProfile.value = response.data
        } catch (e: Exception) {
            // Handle error
        } finally {
            _isLoading.value = false
        }
    }
}
```

### Step 5: Use in Activity

```kotlin
viewModel.userProfile.observe(this) { profile ->
    // Update UI with profile data
}

viewModel.fetchUserProfile(userId)
```

## Testing

### Unit Tests

Create test in `app/src/test/java/`:

```kotlin
class AuthViewModelTest {
    @Test
    fun testLoginSuccess() {
        // Test login functionality
    }
}
```

### Run Tests

```
Run > Run 'Tests' (Shift + F10)
```

## Common Development Tasks

### Add New Activity

1. **Create Layout** (`activity_*.xml`):
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <LinearLayout ...>
   </LinearLayout>
   ```

2. **Create Activity** (`.kt`):
   ```kotlin
   class MyActivity : AppCompatActivity() {
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           setContentView(R.layout.activity_my)
       }
   }
   ```

3. **Register in Manifest** (`AndroidManifest.xml`):
   ```xml
   <activity android:name=".ui.MyActivity" android:exported="true" />
   ```

### Add New API Endpoint

1. Add method to `AuthService.kt`
2. Add method to `AuthRepository.kt`
3. Add LiveData observer to `AuthViewModel.kt`
4. Observe in Activity and update UI

### Debug Network Requests

Enable verbose logging in `RetrofitClient.kt`:

```kotlin
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY  // Shows request/response body
}
```

View logs in Logcat:
```
Logcat > Filter: "OkHttp"
```

## Troubleshooting

### Issue: Cannot connect to backend

**Solution**:
- Verify backend IP in `RetrofitClient.kt`
- For emulator, use `10.0.2.2` instead of `localhost`
- Check if backend is running: `curl http://localhost:8080`

### Issue: Gradle Sync Fails

**Solution**:
```
File > Invalidate Caches > Invalidate and Restart
```

### Issue: App Crashes on Launch

**Solution**:
1. Check Logcat for error message
2. Verify all Activities are in `AndroidManifest.xml`
3. Ensure internet permission is added

### Issue: Cannot reach localhost from emulator

**Solution**:
Use special IP for Android emulator:
```kotlin
// Instead of
BASE_URL = "http://localhost:8080/"

// Use
BASE_URL = "http://10.0.2.2:8080/"
```

## Git Workflow

### Create Feature Branch
```bash
git checkout -b feature/login-screen
```

### Commit Changes
```bash
git add .
git commit -m "Add: Login screen implementation"
```

### Push to Remote
```bash
git push origin feature/login-screen
```

### Create Pull Request
Open PR on GitHub for code review

## Performance Tips

1. **Use ViewBinding** instead of findViewById
2. **Load images asynchronously** to prevent UI blocking
3. **Cancel coroutines** when Activity is destroyed
4. **Use ProGuard** for release builds

## Resources

- [Android Developer Docs](https://developer.android.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/)

## Getting Help

For issues or questions:
1. Check Logcat for error messages
2. Search existing GitHub issues
3. Create new issue with detailed description
4. Contact development team
