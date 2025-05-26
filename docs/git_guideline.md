# Git Guidelines (Android)

## Introduction
This document outlines the Git workflow and best practices for the Cashbox Android project. Following these guidelines ensures a consistent and efficient development process for mobile development.

## Branching Strategy

We follow a Git Flow-based branching strategy adapted for Android development with the following branches:

### Main Branches
- **main**: Production-ready code only. This branch is protected and requires pull request reviews.
- **develop**: Integration branch for features. This is where feature branches are merged after review.

### Supporting Branches
- **feature/[feature-name]**: For new features and non-emergency bug fixes.
- **hotfix/[hotfix-name]**: For urgent production fixes.
- **release/[version]**: Preparation for a new production release.

## Branch Naming Conventions
- Use lowercase letters and hyphens
- Prefix with the type (feature, hotfix, etc.)
- Include issue/ticket number when applicable
- Examples:
    - `feature/penalty-creation-ui`
    - `feature/biometric-authentication`
    - `hotfix/crash-on-penalty-sync`
    - `feature/CASH-123-team-management`

## Commit Guidelines

### Commit Message Format
Follow the Conventional Commits specification adapted for Android development:
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types
- **feat**: A new feature or UI component
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code changes that neither fix bugs nor add features
- **perf**: Performance improvements
- **test**: Adding or correcting tests
- **chore**: Changes to the build process or auxiliary tools
- **ui**: UI/UX improvements and design changes
- **deps**: Dependency updates

### Android-Specific Scopes
- **compose**: Jetpack Compose related changes
- **room**: Database related changes
- **api**: API integration changes
- **auth**: Authentication related changes
- **ui**: General UI changes
- **navigation**: Navigation related changes
- **viewmodel**: ViewModel changes
- **repository**: Repository pattern changes

### Examples
```
feat(compose): add penalty creation screen with Material Design 3
fix(room): resolve database migration crash on app update
docs: update README with new build instructions
ui(compose): improve accessibility for penalty list screen
perf(api): optimize network requests with caching
test(viewmodel): add unit tests for PenaltyViewModel
chore(deps): update Kotlin to 1.9.22
```

## Android-Specific Commit Practices

### APK/AAB Files
- Never commit APK or AAB files to the repository
- Add build outputs to `.gitignore`:
```gitignore
# Android build outputs
*.apk
*.aab
app/release/
app/debug/
```

### Keystore Files
- Never commit keystore files or signing credentials
- Use environment variables for release signing
- Add keystore files to `.gitignore`:
```gitignore
# Signing files
*.keystore
*.jks
key.properties
```

### Generated Files
- Do not commit generated files:
```gitignore
# Generated files
**/build/
.gradle/
local.properties
.navigation/
captures/
```

### Resource Files
- Commit all drawable and layout resources
- Ensure proper naming conventions for resources
- Use vector drawables when possible

## Pull Request Workflow

1. Create a branch from `develop` (for features) or `main` (for hotfixes)
2. Develop and test your changes locally on device/emulator
3. Run all quality checks before pushing:
   ```bash
   ./gradlew detekt lintDebug ktlintCheck
   ./gradlew testDebugUnitTest
   ```
4. Push your branch to the remote repository
5. Create a pull request with a clear description
6. Include screenshots/videos for UI changes
7. Ensure all CI tests pass (including UI tests)
8. Request review from at least one Android developer
9. Address any feedback from the review
10. Once approved, merge using squash merge for feature branches

## Android-Specific Code Review Guidelines

### UI/UX Reviews
- Include screenshots or screen recordings for UI changes
- Test on different screen sizes and orientations
- Verify accessibility with TalkBack enabled
- Check both light and dark theme variations
- Ensure proper Material Design 3 implementation

### Performance Reviews
- Review memory allocations in hot paths
- Check for potential memory leaks (especially with Context references)
- Verify background job optimizations
- Check battery usage implications

### Security Reviews
- Verify sensitive data is not logged
- Check for proper encryption of local data
- Ensure API keys are not hardcoded
- Review permission usage and justification

### Architecture Reviews
- Verify separation of concerns (MVVM pattern)
- Check proper dependency injection usage
- Review Repository and Use Case implementations
- Ensure reactive programming best practices

## Tagging and Versioning

We follow Android versioning conventions with Semantic Versioning:

### Version Code and Version Name
```kotlin
// app/build.gradle
android {
    defaultConfig {
        versionCode 1000100  // Major.Minor.Patch (1.0.1 = 1000100)
        versionName "1.0.1"
    }
}
```

### Git Tags
- Format: `v1.0.1` for releases
- Include release notes in tag description
- Tag releases after merging to `main`:

```bash
git tag -a v1.0.1 -m "Version 1.0.1
- Fixed penalty sync crash
- Improved biometric authentication
- Updated Material Design components
- Performance optimizations"
git push origin v1.0.1
```

### Release Branches
Create release branches for final testing:
```bash
git checkout -b release/1.0.1 develop
# Final testing and bug fixes
git checkout main
git merge --no-ff release/1.0.1
git tag -a v1.0.1
```

## Repository Information

- **Repository URL**: `git@github.com:Psheikomaniac/cashbox-android.git`
- **Main Branch**: `main`
- **Development Branch**: `develop`
- **CI/CD**: GitHub Actions with Android-specific workflows

## Android-Specific Git Hooks

### Pre-commit Hook
Create `.git/hooks/pre-commit`:

```bash
#!/bin/sh

echo "Running Android pre-commit checks..."

# Check for Android lint issues
echo "Running Android Lint..."
./gradlew lintDebug
if [ $? -ne 0 ]; then
    echo "❌ Android Lint check failed. Please fix the issues before committing."
    exit 1
fi

# Check for Kotlin code style
echo "Running KtLint..."
./gradlew ktlintCheck
if [ $? -ne 0 ]; then
    echo "❌ KtLint check failed. Run './gradlew ktlintFormat' to fix formatting issues."
    exit 1
fi

# Run Detekt static analysis
echo "Running Detekt..."
./gradlew detekt
if [ $? -ne 0 ]; then
    echo "❌ Detekt check failed. Please fix the issues before committing."
    exit 1
fi

# Run unit tests
echo "Running unit tests..."
./gradlew testDebugUnitTest
if [ $? -ne 0 ]; then
    echo "❌ Unit tests failed. Please fix the failing tests before committing."
    exit 1
fi

# Check for hardcoded strings (basic check)
echo "Checking for hardcoded strings..."
if grep -r "android:text=\"[^@]" app/src/main/res/layout/ 2>/dev/null; then
    echo "⚠️  Warning: Found potential hardcoded strings in layouts. Consider using string resources."
fi

# Check for TODO comments in release builds
if git diff --cached --name-only | grep -E "(\.kt|\.java)$" | xargs grep -l "TODO\|FIXME" 2>/dev/null; then
    echo "⚠️  Warning: Found TODO/FIXME comments in staged files."
fi

echo "✅ All pre-commit checks passed!"
```

### Pre-push Hook
Create `.git/hooks/pre-push`:

```bash
#!/bin/sh

echo "Running Android pre-push checks..."

# Run full test suite
echo "Running full test suite..."
./gradlew test
if [ $? -ne 0 ]; then
    echo "❌ Test suite failed. Please fix the failing tests before pushing."
    exit 1
fi

# Check if we can build release APK
echo "Testing release build..."
./gradlew assembleRelease
if [ $? -ne 0 ]; then
    echo "❌ Release build failed. Please fix build issues before pushing."
    exit 1
fi

echo "✅ All pre-push checks passed!"
```

## Android CI/CD Integration

Our project uses GitHub Actions for CI/CD with Android-specific workflows:

### Main Workflow Features
- Multi-API level testing with Android emulators
- Automated APK/AAB generation
- Play Store deployment for releases
- Firebase App Distribution for internal testing
- Automated screenshot testing
- Performance benchmarking

### Workflow Triggers
```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  release:
    types: [ published ]
```

### Android-Specific Jobs
- **lint**: Android Lint checks
- **detekt**: Kotlin static analysis
- **test**: Unit and integration tests
- **ui-test**: Espresso UI tests on emulators
- **build**: APK/AAB generation
- **deploy**: Play Store or Firebase distribution

## File Organization Best Practices

### Resource Naming
Follow Android naming conventions:
```kotlin
// Layouts
activity_main.xml
fragment_penalty_list.xml
item_penalty.xml
dialog_create_penalty.xml

// Drawables
ic_penalty_24dp.xml
bg_rounded_corner.xml
selector_button_state.xml

// Colors
primary_color
secondary_color
error_color

// Strings
penalty_list_title
create_penalty_button
error_network_connection
```

### Package Organization
```kotlin
com.cashbox.android
├── di/                    # Dependency injection
├── data/
│   ├── local/
│   ├── remote/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── ui/
│   ├── viewmodel/
│   └── navigation/
└── utils/
```

## Android-Specific Git Best Practices

1. **Keep commits focused**: One feature or fix per commit
2. **Test on multiple devices**: Before pushing, test on different screen sizes
3. **Include APK size tracking**: Monitor APK size changes in PRs
4. **Document breaking changes**: Especially for database schema changes
5. **Use feature flags**: For experimental features that shouldn't be in release
6. **Regular dependency updates**: Keep Android dependencies current
7. **Proguard/R8 testing**: Test obfuscated release builds regularly

### Commit Frequency for Android

- **CRITICAL**: Make a commit after EACH completed Android-specific task:
    - New Activity/Fragment implementation
    - ViewModel with business logic
    - Database migration or schema change
    - API integration
    - UI component creation
    - Test implementation
    - Bug fix verification

- **MANDATORY**: Push directly to GitHub IMMEDIATELY after each commit - NO EXCEPTIONS!

- **ESSENTIAL**: Even for the smallest UI change or string resource addition, if it modifies a file, it MUST be committed and pushed right away

### Android-Specific Commit Examples

```bash
# UI Development
git commit -m "feat(compose): implement penalty creation form with validation"
git commit -m "ui(compose): add dark theme support for penalty list screen"
git commit -m "feat(compose): integrate biometric authentication dialog"

# Database Changes  
git commit -m "feat(room): add penalty entity with UUID primary key"
git commit -m "fix(room): resolve migration crash from version 1 to 2"
git commit -m "refactor(room): optimize penalty queries with indexes"

# API Integration
git commit -m "feat(api): implement penalty sync with conflict resolution"
git commit -m "fix(api): handle network timeout for penalty creation"
git commit -m "perf(api): add request caching for user profile"

# Testing
git commit -m "test(viewmodel): add unit tests for penalty creation flow"
git commit -m "test(ui): add Espresso tests for penalty list navigation"
git commit -m "test(repository): add integration tests for offline sync"
```

## Troubleshooting Git Issues

### Large File Issues
```bash
# Remove accidentally committed APK
git filter-branch --force --index-filter \
'git rm --cached --ignore-unmatch app/build/outputs/apk/debug/app-debug.apk' \
--prune-empty --tag-name-filter cat -- --all
```

### Merge Conflicts in Generated Files
```bash
# For build.gradle conflicts, prefer the feature branch version
git checkout --theirs app/build.gradle

# For strings.xml conflicts, manually merge both changes
git mergetool app/src/main/res/values/strings.xml
```

### Undoing Android-Specific Changes
```bash
# Undo uncommitted changes to generated files
git checkout -- app/build/
git checkout -- .gradle/

# Reset to last commit (dangerous - will lose all changes)
git reset --hard HEAD
```

## Security Considerations

- Never commit API keys, keystore passwords, or certificates
- Use environment variables or secure CI/CD secrets
- Regularly rotate access tokens and API keys
- Monitor for accidentally committed sensitive data

This Git workflow ensures that Android-specific development practices are properly managed while maintaining code quality and team collaboration efficiency.