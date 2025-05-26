# Gradle Dependency Guidelines (Android)

This document outlines the guidelines for managing dependencies in the Cashbox Android project using Gradle.

## Overview

Gradle is the build system for Android that allows us to declare, manage, and configure libraries that our project depends on. In the Cashbox Android project, all dependencies must be managed through Gradle to ensure consistency, maintainability, and security.

## Core Principles

1. **Use Established Libraries**: Do not create custom implementations when well-maintained Android libraries exist
2. **Document Dependencies**: All dependencies must be properly documented in `build.gradle` files
3. **Version Catalogs**: Use Gradle Version Catalogs for centralized dependency management
4. **Regular Updates**: Keep dependencies up to date to receive security fixes and improvements
5. **Test Dependencies**: Use appropriate test scopes for dependencies only needed in testing
6. **Android-First**: Prefer Android Jetpack libraries over third-party alternatives

## Gradle Project Structure

```
project/
├── build.gradle.kts (Project level)
├── app/
│   └── build.gradle.kts (Module level)
├── gradle/
│   └── libs.versions.toml (Version Catalog)
├── gradle.properties
└── settings.gradle.kts
```

## Version Catalog Setup

Create `gradle/libs.versions.toml` for centralized dependency management:

```toml
[versions]
# Android & Kotlin
android-gradle-plugin = "8.2.0"
kotlin = "1.9.22"
kotlin-coroutines = "1.7.3"

# AndroidX Core
core-ktx = "1.12.0"
lifecycle = "2.7.0"
activity-compose = "1.8.2"
navigation = "2.7.6"

# Compose BOM
compose-bom = "2024.02.00"

# Architecture Components
room = "2.6.1"
hilt = "2.48"
hilt-navigation-compose = "1.1.0"

# Network
retrofit = "2.9.0"
okhttp = "4.12.0"
gson = "2.10.1"

# UI & Design
material3 = "1.1.2"
accompanist = "0.32.0"

# Security
security-crypto = "1.1.0-alpha06"
biometric = "1.1.0"

# Image Loading
coil = "2.5.0"

# Testing
junit = "4.13.2"
junit-ext = "1.1.5"
espresso = "3.5.1"
mockk = "1.13.8"
turbine = "1.0.0"

# Code Quality
detekt = "1.23.4"
ktlint = "11.6.1"

# Firebase
firebase-bom = "32.7.0"

[libraries]
# Android Core
android-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
android-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
android-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activity-compose" }

# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }

[plugins]
android-application = { id = "com.android.application", version.ref = "android-gradle-plugin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

## Project-Level build.gradle.kts

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory.get().asFile)
}
```

## Module-Level build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.cashbox.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cashbox.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.cashbox.android.HiltTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // BOM (Bill of Materials) - manages versions of related libraries
    implementation(platform(libs.compose.bom))
    implementation(platform(libs.firebase.bom))

    // Core Android
    implementation(libs.android.core.ktx)
    implementation(libs.android.lifecycle.runtime.ktx)
    implementation(libs.android.activity.compose)

    // Compose UI
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    // Architecture Components
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    // Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
```

## Adding Dependencies

### Production Dependencies

For libraries required in production:

```bash
# Using Android Studio: File → Project Structure → Dependencies → Add Library Dependency
# Or manually add to build.gradle.kts
```

Examples of production dependencies:

```kotlin
dependencies {
    // Core Android Libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
```

### Test Dependencies

For libraries only needed during testing:

```kotlin
dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // Android Instrumented Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug Dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

## Version Constraints

Use appropriate version constraints to balance stability with updates:

- `1.0.0` - Exact version (avoid in most cases)
- `1.0.+` - Latest patch version
- `1.+` - Latest minor version (use with caution)
- `+` - Latest version (never use in production)

Prefer exact versions for stability:

```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.12.0") // Recommended
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Recommended
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Recommended
}
```

## Updating Dependencies

Regularly update dependencies to receive security fixes and improvements:

```bash
# Check for outdated dependencies
./gradlew dependencyUpdates

# Update specific dependency by changing version in build.gradle.kts
# Then sync project
```

Use Android Studio's built-in dependency update notifications or plugins like:
- Version Catalog Update Plugin
- Gradle Versions Plugin

## Security Considerations

Regularly check for security vulnerabilities in dependencies:

```bash
# OWASP Dependency Check for Android
./gradlew dependencyCheckAnalyze
```

Consider using tools like:
- **Snyk**: Vulnerability scanning
- **GitHub Dependabot**: Automated security updates
- **OWASP Dependency Check**: Security vulnerability detection

## Recommended Android Libraries

Below are recommended libraries for common functionalities. Always use these established libraries rather than creating custom implementations:

### Core Android & UI
- `androidx.core:core-ktx`: Kotlin extensions for Android
- `androidx.compose.ui:ui`: Jetpack Compose UI toolkit
- `androidx.compose.material3:material3`: Material Design 3 components
- `androidx.activity:activity-compose`: Activity integration with Compose
- `androidx.fragment:fragment-ktx`: Fragment Kotlin extensions

### Architecture Components
- `androidx.lifecycle:lifecycle-viewmodel-ktx`: ViewModel for MVVM
- `androidx.lifecycle:lifecycle-livedata-ktx`: LiveData for reactive programming
- `androidx.navigation:navigation-compose`: Navigation component
- `androidx.room:room-ktx`: Local database with SQLite
- `androidx.work:work-runtime-ktx`: Background job processing

### Network & Data
- `com.squareup.retrofit2:retrofit`: Type-safe HTTP client
- `com.squareup.retrofit2:converter-gson`: JSON serialization
- `com.squareup.okhttp3:okhttp`: HTTP client
- `com.squareup.okhttp3:logging-interceptor`: Network logging

### Dependency Injection
- `com.google.dagger:hilt-android`: Dependency injection framework
- `androidx.hilt:hilt-navigation-compose`: Hilt integration with Navigation

### Image Loading
- `io.coil-kt:coil-compose`: Image loading for Compose
- `com.github.bumptech.glide:glide`: Image loading library

### Security
- `androidx.security:security-crypto`: Encrypted SharedPreferences
- `androidx.biometric:biometric`: Biometric authentication

### Reactive Programming
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`: Coroutines for Android
- `org.jetbrains.kotlinx:kotlinx-coroutines-core`: Core coroutines

### JSON & Serialization
- `com.google.code.gson:gson`: JSON parsing
- `org.jetbrains.kotlinx:kotlinx-serialization-json`: Kotlin serialization

### Testing Libraries
- `junit:junit`: Unit testing framework
- `io.mockk:mockk`: Mocking library for Kotlin
- `androidx.test.ext:junit`: Android testing extensions
- `androidx.test.espresso:espresso-core`: UI testing framework
- `androidx.compose.ui:ui-test-junit4`: Compose testing utilities

### Code Quality
- `io.gitlab.arturbosch.detekt:detekt-gradle-plugin`: Static analysis
- `org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin`: Code formatting

## Build Variants and Flavors

Configure different build variants for different environments:

```kotlin
android {
    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.cashbox.com/\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://api.cashbox.com/\"")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }

        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }

        create("production") {
            dimension = "environment"
        }
    }
}
```

## Custom Gradle Tasks

Define custom tasks for common operations:

```kotlin
tasks.register("generateChangeLog") {
    doLast {
        println("Generating changelog...")
        // Custom changelog generation logic
    }
}

tasks.register("runQualityChecks") {
    dependsOn("detekt", "lintDebug", "testDebugUnitTest")
    doLast {
        println("All quality checks completed!")
    }
}

// Run quality checks before build
tasks.named("preBuild") {
    dependsOn("runQualityChecks")
}
```

## Gradle Properties Configuration

Configure `gradle.properties` for project-wide settings:

```properties
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true

# Android
android.useAndroidX=true
android.enableJetifier=true

# Kotlin
kotlin.code.style=official

# Build optimization
android.nonTransitiveRClass=true
android.nonFinalResIds=true
```

## ProGuard/R8 Configuration

Configure code obfuscation and optimization:

```proguard
# proguard-rules.pro

# Keep data classes used in API responses
-keep class com.cashbox.android.data.remote.dto.** { *; }

# Keep Room entities
-keep class com.cashbox.android.data.local.entities.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowshrinking,allowoptimization interface * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
```

## Dependency Scope Best Practices

### Implementation vs API

```kotlin
dependencies {
    // Use 'implementation' for dependencies that don't leak to consumers
    implementation("androidx.core:core-ktx:1.12.0")

    // Use 'api' only when the dependency is part of your public API
    // (Rarely needed in Android apps)
    api("some.library:public-api:1.0.0")

    // Use 'compileOnly' for compile-time only dependencies
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    // Use 'runtimeOnly' for runtime-only dependencies
    runtimeOnly("mysql:mysql-connector-java:8.0.33")
}
```

### Test Dependencies

```kotlin
dependencies {
    // Unit tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")

    // Android instrumented tests
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Debug builds only
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Kapt (annotation processing)
    kapt("com.google.dagger:hilt-compiler:2.48")
    kaptTest("com.google.dagger:hilt-compiler:2.48")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.48")
}
```

## Conclusion

Proper dependency management is crucial for maintaining a healthy, secure, and maintainable Android codebase. Always prefer established Android libraries over custom implementations, keep dependencies up to date, and document why specific libraries are chosen. Use Gradle's powerful features like Version Catalogs and build variants to manage complexity effectively.
