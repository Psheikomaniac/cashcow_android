# Cashbox Android

A comprehensive Android application for managing team finances, including penalties, drinks, and contributions. Built with modern Android development practices and Material Design 3.

## Overview

The Cashbox Android application provides an intuitive mobile interface for tracking and managing financial transactions within sports teams. The app allows team members to:

- Track financial penalties (for late arrivals, missed training, etc.)
- Manage drink purchases and consumption
- Record and monitor membership contributions
- Generate reports and view statistics
- Monitor payment status and outstanding balances
- Sync data across devices with offline-first approach

This application is built using modern Android technologies and follows best practices for scalable, maintainable, and secure mobile development.

## Repository

The Android application repository:

- **Repository URL**: `git@github.com:Psheikomaniac/cashbox-android.git`
- **Web interface**: [https://github.com/Psheikomaniac/cashbox-android](https://github.com/Psheikomaniac/cashbox-android)
- **Issue tracker**: [https://github.com/Psheikomaniac/cashbox-android/issues](https://github.com/Psheikomaniac/cashbox-android/issues)
- **Releases**: [https://github.com/Psheikomaniac/cashbox-android/releases](https://github.com/Psheikomaniac/cashbox-android/releases)

## Screenshots

| Penalty List | Create Penalty | User Dashboard | Reports |
|--------------|---------------|----------------|---------|
| ![Penalty List](screenshots/penalty_list.png) | ![Create Penalty](screenshots/create_penalty.png) | ![Dashboard](screenshots/dashboard.png) | ![Reports](screenshots/reports.png) |

## Technology Stack

### Core Technologies
- **Kotlin**: Modern programming language for Android development
- **Android SDK**: Target SDK 34, Minimum SDK 24 (Android 7.0)
- **Jetpack Compose**: Modern declarative UI toolkit
- **Material Design 3**: Google's latest design system
- **Android Architecture Components**: ViewModel, LiveData, Navigation, etc.

### Architecture & Design Patterns
- **MVVM (Model-View-ViewModel)**: Separation of concerns and testability
- **Clean Architecture**: Domain-driven design with clear layer separation
- **Repository Pattern**: Abstract data sources and business logic
- **Dependency Injection**: Dagger Hilt for maintainable code

### Data Management
- **Room Database**: Local SQLite database with compile-time verification
- **Retrofit**: Type-safe HTTP client for API communication
- **Kotlin Coroutines & Flow**: Asynchronous programming and reactive streams
- **DataStore**: Modern data storage solution for preferences

### Security & Authentication
- **JWT Authentication**: Secure token-based authentication
- **EncryptedSharedPreferences**: Secure local storage
- **Biometric Authentication**: Fingerprint and face unlock
- **Certificate Pinning**: Protection against man-in-the-middle attacks

### Testing & Quality
- **JUnit 5**: Unit testing framework
- **Espresso**: UI testing framework
- **MockK**: Mocking library for Kotlin
- **Detekt**: Static code analysis for Kotlin
- **Android Lint**: Built-in Android code quality checks

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/cashbox/
│   │   │   ├── di/                     # Dependency Injection
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   └── RepositoryModule.kt
│   │   │   ├── data/                   # Data Layer
│   │   │   │   ├── local/              # Local Database
│   │   │   │   │   ├── dao/
│   │   │   │   │   ├── entities/
│   │   │   │   │   └── database/
│   │   │   │   ├── remote/             # Remote API
│   │   │   │   │   ├── api/
│   │   │   │   │   ├── dto/
│   │   │   │   │   └── interceptors/
│   │   │   │   └── repository/         # Repository Implementations
│   │   │   ├── domain/                 # Domain Layer
│   │   │   │   ├── model/              # Domain Models
│   │   │   │   ├── repository/         # Repository Interfaces
│   │   │   │   └── usecase/            # Use Cases
│   │   │   ├── presentation/           # Presentation Layer
│   │   │   │   ├── ui/                 # UI Components
│   │   │   │   │   ├── screens/        # Compose Screens
│   │   │   │   │   ├── components/     # Reusable Components
│   │   │   │   │   └── theme/          # Material Design Theme
│   │   │   │   ├── viewmodel/          # ViewModels
│   │   │   │   └── navigation/         # Navigation Logic
│   │   │   └── utils/                  # Utility Classes
│   │   ├── res/                        # Android Resources
│   │   │   ├── layout/                 # XML Layouts (if any)
│   │   │   ├── values/                 # Colors, Strings, Themes
│   │   │   ├── drawable/               # Vector Drawables
│   │   │   └── mipmap/                 # App Icons
│   │   └── AndroidManifest.xml
│   ├── test/                           # Unit Tests
│   │   └── java/com/cashbox/
│   │       ├── data/
│   │       ├── domain/
│   │       ├── presentation/
│   │       └── utils/
│   └── androidTest/                    # Instrumented Tests
│       └── java/com/cashbox/
│           ├── data/
│           ├── presentation/
│           └── utils/
├── build.gradle                        # Module-level build configuration
└── proguard-rules.pro                 # ProGuard configuration
```

## Getting Started

### Prerequisites

- **Android Studio**: Electric Eel (2022.1.1) or newer
- **JDK**: Version 17 or higher
- **Android SDK**: API level 34
- **Kotlin**: Version 1.9.x or newer

### Installation

1. **Clone the repository:**
   ```bash
   git clone git@github.com:Psheikomaniac/cashbox-android.git
   cd cashbox-android
   ```

2. **Open in Android Studio:**
    - Launch Android Studio
    - Select "Open an existing project"
    - Navigate to the cloned repository folder

3. **Configure the project:**
    - Copy `local.properties.example` to `local.properties`
    - Update API endpoints and keys in `local.properties`:
      ```properties
      api.base.url=https://api.cashbox.com/
      api.key=your_api_key_here
      google.maps.api.key=your_maps_api_key
      ```

4. **Build the project:**
   ```bash
   ./gradlew build
   ```

5. **Run the application:**
    - Connect an Android device or start an emulator
    - Click "Run" in Android Studio or use:
      ```bash
      ./gradlew installDebug
      ```

### Dependencies

All dependencies are managed through Gradle. Key dependencies include:

```kotlin
// Core Android
implementation "androidx.core:core-ktx:1.12.0"
implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
implementation "androidx.activity:activity-compose:1.8.2"

// Jetpack Compose
implementation platform("androidx.compose:compose-bom:2024.02.00")
implementation "androidx.compose.ui:ui"
implementation "androidx.compose.ui:ui-tooling-preview"
implementation "androidx.compose.material3:material3"

// Architecture Components
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
implementation "androidx.navigation:navigation-compose:2.7.6"

// Dependency Injection
implementation "com.google.dagger:hilt-android:2.48"
kapt "com.google.dagger:hilt-compiler:2.48"

// Database
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// Network
implementation "com.squareup.retrofit2:retrofit:2.9.0"
implementation "com.squareup.retrofit2:converter-gson:2.9.0"
implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"

// Security
implementation "androidx.security:security-crypto:1.1.0-alpha06"
implementation "androidx.biometric:biometric:1.1.0"
```

## Features

### Current Features (v1.0.0)

- ✅ **User Authentication**: Secure login with biometric support
- ✅ **Penalty Management**: Create, view, and manage penalties
- ✅ **Team Management**: Switch between teams and manage members
- ✅ **Offline Support**: Full offline functionality with automatic sync
- ✅ **Material Design 3**: Modern, accessible UI design
- ✅ **Dark Mode**: System-wide dark theme support
- ✅ **Push Notifications**: Real-time updates and reminders
- ✅ **Data Sync**: Seamless synchronization across devices

### Planned Features

- 🔄 **Advanced Reporting**: Interactive charts and detailed analytics
- 🔄 **Contribution Management**: Track membership fees and contributions
- 🔄 **Payment Integration**: Mobile payment support
- 🔄 **Voice Commands**: Voice-to-text for penalty creation
- 🔄 **Camera Integration**: Receipt scanning and image recognition
- 🔄 **Wear OS Support**: Companion app for smartwatches
- 🔄 **Widget Support**: Home screen widgets for quick access

## Architecture

The application follows Clean Architecture principles with MVVM pattern:

### Data Flow

```
UI Layer (Compose) 
    ↓ User Interactions
ViewModel (State Management)
    ↓ Business Logic
Use Cases (Domain Logic)
    ↓ Data Operations
Repository (Data Abstraction)
    ↓ Local/Remote
Room Database ←→ Retrofit API
```

### Key Components

1. **UI Layer**: Jetpack Compose screens and components
2. **Presentation Layer**: ViewModels managing UI state
3. **Domain Layer**: Use cases containing business logic
4. **Data Layer**: Repositories coordinating local and remote data

## Testing

The project includes comprehensive testing at all levels:

### Unit Tests
```bash
# Run unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Integration Tests
```bash
# Run integration tests
./gradlew connectedAndroidTest
```

### UI Tests
```bash
# Run UI tests on device/emulator
./gradlew connectedDebugAndroidTest
```

### Code Quality

```bash
# Run static analysis
./gradlew detekt

# Run lint checks
./gradlew lintDebug

# Fix code formatting
./gradlew ktlintFormat
```

## Building for Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
# Build APK
./gradlew assembleRelease

# Build App Bundle (recommended for Play Store)
./gradlew bundleRelease
```

### Code Signing

Configure release signing in `app/build.gradle`:

```kotlin
android {
    signingConfigs {
        release {
            storeFile file("../keystore/release.keystore")
            storePassword System.getenv("KEYSTORE_PASSWORD")
            keyAlias System.getenv("KEY_ALIAS")
            keyPassword System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## API Integration

The app communicates with the Cashbox backend API:

### Base Configuration
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createOkHttpClient())
            .build()
    }
}
```

### API Endpoints
- `GET /api/penalties` - Retrieve penalties
- `POST /api/penalties` - Create new penalty
- `PUT /api/penalties/{id}` - Update penalty
- `DELETE /api/penalties/{id}` - Delete penalty
- `GET /api/teams` - Get user teams
- `GET /api/users/profile` - Get user profile

## Performance Optimization

### Memory Management
- Use of `remember` in Compose for expensive calculations
- Proper lifecycle management for ViewModels
- Efficient bitmap loading and caching
- Lazy loading for large datasets

### Battery Optimization
- Doze mode and App Standby compliance
- Efficient background synchronization
- Minimal use of location services
- Optimized network requests

### Network Efficiency
- Request caching and deduplication
- Compression for large payloads
- Retry mechanisms with exponential backoff
- Offline-first architecture

## Security

### Data Protection
- EncryptedSharedPreferences for sensitive data
- Certificate pinning for API communication
- Secure token storage in Android Keystore
- Data obfuscation in release builds

### Authentication Security
- JWT token management with refresh rotation
- Biometric authentication integration
- Session timeout and automatic logout
- Device binding for enhanced security

## Accessibility

The app is designed to be accessible to all users:

- **Screen Reader Support**: Full TalkBack compatibility
- **High Contrast**: Support for high contrast themes
- **Large Text**: Dynamic text scaling support
- **Color Blind Friendly**: Color choices that work for color-blind users
- **Voice Navigation**: Voice commands for key actions
- **Keyboard Navigation**: Full keyboard navigation support

## Localization

Currently supported languages:
- English (default)
- German
- Spanish
- French

To add a new language:
1. Create new `values-{language}/strings.xml` file
2. Translate all string resources
3. Test RTL layout if applicable
4. Update language selection in settings

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and add tests
4. Ensure all tests pass: `./gradlew test connectedAndroidTest`
5. Run code quality checks: `./gradlew detekt lintDebug`
6. Commit your changes: `git commit -m 'Add amazing feature'`
7. Push to the branch: `git push origin feature/amazing-feature`
8. Open a Pull Request

### Coding Standards

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Write unit tests for new functionality
- Document public APIs with KDoc
- Follow Android Architecture Components best practices

## Deployment

### Play Store Release

1. **Prepare Release**
   ```bash
   ./gradlew bundleRelease
   ```

2. **Upload to Play Console**
    - Go to Google Play Console
    - Upload the generated AAB file
    - Fill in release notes and store listing
    - Submit for review

3. **Internal Testing**
    - Use internal testing track for initial validation
    - Gather feedback from team members

4. **Staged Rollout**
    - Start with 5% rollout
    - Monitor crash reports and user feedback
    - Gradually increase to 100%

### CI/CD Pipeline

The project uses GitHub Actions for automated testing and deployment:

```yaml
name: Android CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: ./gradlew test
      - name: Upload test results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: app/build/test-results/
```

## Monitoring and Analytics

### Crash Reporting
- **Firebase Crashlytics**: Real-time crash reporting
- **Automatic crash analysis**: Identify and fix critical issues
- **Custom logging**: Detailed error context

### Performance Monitoring
- **Firebase Performance**: App startup and network performance
- **Custom metrics**: Business-specific performance indicators
- **Battery usage tracking**: Monitor power consumption

### User Analytics
- **Firebase Analytics**: User behavior and engagement
- **Custom events**: Track feature usage and user flows
- **A/B testing**: Optimize user experience

## Troubleshooting

### Common Issues

1. **Build Errors**
   ```bash
   # Clean and rebuild
   ./gradlew clean build
   
   # Update dependencies
   ./gradlew --refresh-dependencies build
   ```

2. **API Connection Issues**
    - Check network connectivity
    - Verify API endpoints in `local.properties`
    - Check authentication tokens

3. **Database Migration Issues**
   ```bash
   # Clear app data and restart
   adb shell pm clear com.cashbox.android
   ```

4. **ProGuard Issues**
   ```bash
   # Add rules to proguard-rules.pro
   -keep class com.cashbox.data.remote.dto.** { *; }
   ```

## License

This project is proprietary and confidential. All rights reserved.

## Support

For support and questions:

- 📧 Email: support@cashbox.com
- 💬 Discord: [Cashbox Community](https://discord.gg/cashbox)
- 📱 In-app support: Settings → Help & Support
- 🐛 Bug reports: [GitHub Issues](https://github.com/Psheikomaniac/cashbox-android/issues)

## Acknowledgments

- Material Design team for design guidelines
- Android Developer Relations team for best practices
- Open source contributors for libraries and tools
- Beta testers for valuable feedback

---

Built with ❤️ using Kotlin and Jetpack Compose