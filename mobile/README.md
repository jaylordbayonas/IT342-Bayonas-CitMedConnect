"# CitMedConnect Mobile App

A native Android application built with Kotlin for the CitMedConnect medical system.

## Features

- **User Registration**: Create an account with name, email, and password
- **User Login**: Authenticate using registered credentials
- **Backend Integration**: Connect to backend API endpoints
- **Input Validation**: Client-side validation for form inputs
- **Error Handling**: Comprehensive error messages and network error handling

## Project Structure

```
mobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/edu/cit/bayonas/citmedconnect/
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/        # API services and Retrofit client
│   │   │   │   │   ├── model/      # Data models (DTOs)
│   │   │   │   │   └── repository/ # Repository pattern
│   │   │   │   ├── ui/
│   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   ├── RegisterActivity.kt
│   │   │   │   │   ├── DashboardActivity.kt
│   │   │   │   │   └── viewmodel/ # MVVM ViewModels
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/     # XML layouts
│   │   │   │   └── values/     # Strings, colors, styles
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── README.md
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Android XML Layouts
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + OkHttp3
- **Asynchronous**: Coroutines
- **State Management**: LiveData

## Setup Instructions

### Prerequisites

- Android Studio (Latest version)
- JDK 17 or higher
- Android SDK 26 or higher (for deployment)
- Target SDK 34

### Configuration

1. **Backend URL**: Update `BASE_URL` in `RetrofitClient.kt`
   - For emulator: `http://10.0.2.2:8080/`
   - For physical device: `http://<your-backend-ip>:8080/`

2. Open the project in Android Studio and sync Gradle files

### Running the App

1. **Emulator**:
   - Create AVD in Android Studio
   - Run the app (`Shift + F10`)

2. **Physical Device**:
   - Enable Developer Mode
   - Connect via USB
   - Run the app (`Shift + F10`)

## API Endpoints

The app connects to the following backend endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | Register new user |
| `/api/auth/login` | POST | Login user |

## Request/Response Format

### Login Request
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Register Request
```json
{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123"
}
```

### Success Response
```json
{
  "success": true,
  "message": "Login successful",
  "token": "jwt_token_here",
  "user": {
    "id": "user_id",
    "name": "John Doe",
    "email": "user@example.com"
  }
}
```

## Validation Rules

### Login
- Email must be valid format
- Password must not be empty

### Registration
- Name must not be empty
- Email must be valid format
- Password must be at least 6 characters

## Dependencies

Main dependencies added to `build.gradle`:

- **Retrofit**: HTTP client for API calls
- **OkHttp**: HTTP logging and interceptors
- **Coroutines**: Asynchronous programming
- **LiveData**: Reactive state management
- **ViewModel**: UI-related data management
- **Material Components**: UI design components

## Development Workflow

1. **Create Feature Branch**: `git checkout -b feature/feature-name`
2. **Implement Changes**: Follow MVVM architecture
3. **Test Locally**: Run on emulator/device
4. **Commit Changes**: `git commit -m "Add: feature description"`
5. **Push to Repository**: `git push origin feature/feature-name`

## Debugging

- **Logs**: Check Logcat in Android Studio
- **Network Requests**: HTTP logging interceptor logs all API calls
- **Breakpoints**: Set breakpoints in Android Studio debugger

## Future Enhancements

- [ ] Token persistence (SharedPreferences/DataStore)
- [ ] Auto-login with saved credentials
- [ ] Patient records viewing
- [ ] Appointment booking
- [ ] Medical history tracking
- [ ] Push notifications
- [ ] Profile management
- [ ] Image uploads for prescriptions

## Troubleshooting

### Backend Connection Error
- Check if backend is running on correct IP/port
- Verify emulator can reach backend (use `10.0.2.2` for localhost)
- Check internet permissions in AndroidManifest.xml

### Build Issues
- Clean project: `Build > Clean Project`
- Invalidate caches: `File > Invalidate Caches`
- Sync Gradle files: `File > Sync Now`

### Runtime Crashes
- Check Logcat for exception details
- Verify API endpoints match backend configuration
- Ensure network permissions are added

## License

Part of IT342-Bayonas-CitMedConnect project

## Contact

For issues or inquiries, contact the project maintainers through the GitHub repository." 
