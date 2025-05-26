# Static Analysis and Code Quality Tools (Android)

This document outlines the static analysis and code quality tools used in the Cashbox Android project, along with configuration and best practices.

## Overview

Code quality is a top priority for this Android project. We use a comprehensive set of static analysis and code quality tools to ensure that our Kotlin code is clean, maintainable, secure, and follows Android best practices. These tools are integrated into our development workflow and CI/CD pipeline.

## Android Lint

[Android Lint](https://developer.android.com/studio/write/lint) is the primary static analysis tool for Android projects, helping us catch bugs, security issues, and performance problems.

### Configuration

We use a custom `lint.xml` file in the project root to configure lint rules:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- Treat all warnings as errors -->
    <issue id="all" severity="error" />

    <!-- Security issues -->
    <issue id="HardcodedDebugMode" severity="error" />
    <issue id="SetJavaScriptEnabled" severity="error" />
    <issue id="TrustAllX509TrustManager" severity="error" />
    <issue id="BadHostnameVerifier" severity="error" />

    <!-- Performance issues -->
    <issue id="UnusedResources" severity="warning" />
    <issue id="Overdraw" severity="warning" />
    <issue id="ViewHolder" severity="error" />

    <!-- Accessibility -->
    <issue id="ContentDescription" severity="error" />
    <issue id="ClickableViewAccessibility" severity="error" />

    <!-- Internationalization -->
    <issue id="HardcodedText" severity="error" />
    <issue id="RelativeOverlap" severity="error" />

    <!-- Code style -->
    <issue id="ObsoleteLayoutParam" severity="error" />
    <issue id="UnusedAttribute" severity="warning" />

    <!-- Allow specific cases -->
    <issue id="RtlHardcoded" severity="ignore" />
    <issue id="ContentDescription">
        <ignore path="**/test/**" />
    </issue>
</lint>
```

### Gradle Configuration

```kotlin
// app/build.gradle
android {
    lint {
        checkReleaseBuilds = true
        abortOnError = true
        warningsAsErrors = true
        xmlReport = true
        htmlReport = true
        xmlOutput = file("${layout.buildDirectory.get().asFile}/reports/lint/lint-report.xml")
        htmlOutput = file("${layout.buildDirectory.get().asFile}/reports/lint/lint-report.html")
    }
}
```

### Usage

```bash
# Run lint check
./gradlew lint

# Run lint check for release build
./gradlew lintRelease

# Generate lint report
./gradlew lintDebug
```

## Detekt

[Detekt](https://detekt.dev/) is a static code analysis tool for Kotlin that helps identify code smells and violations of coding conventions.

### Installation

Add to project-level `build.gradle`:

```kotlin
buildscript {
    dependencies {
        classpath "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.4"
    }
}
```

Add to app-level `build.gradle`:

```kotlin
plugins {
    id "io.gitlab.arturbosch.detekt"
}

detekt {
    toolVersion = "1.23.4"
    config = files("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)

        html.outputLocation.set(file("build/reports/detekt/detekt.html"))
        xml.outputLocation.set(file("build/reports/detekt/detekt.xml"))
        txt.outputLocation.set(file("build/reports/detekt/detekt.txt"))
    }
}

dependencies {
    detektPlugins "io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4"
    detektPlugins "io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.4"
}
```

### Configuration

Create `config/detekt/detekt.yml`:

```yaml
build:
  maxIssues: 0

console-reports:
  active: true

output-reports:
  active: true

complexity:
  ComplexMethod:
    active: true
    threshold: 15
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    threshold: 6
  TooManyFunctions:
    active: true
    thresholdInFiles: 15
    thresholdInClasses: 15

coroutines:
  active: true
  GlobalCoroutineUsage:
    active: true
  RedundantSuspendModifier:
    active: true
  SuspendFunWithFlowReturnType:
    active: true

empty-blocks:
  active: true

exceptions:
  active: true
  SwallowedException:
    active: true
  TooGenericExceptionCaught:
    active: true
  TooGenericExceptionThrown:
    active: true

formatting:
  active: true
  android: true
  MaximumLineLength:
    active: true
    maxLineLength: 120
  ImportOrdering:
    active: true
  SpacingAroundColon:
    active: true

naming:
  active: true
  ClassNaming:
    active: true
    classPattern: '[A-Z][a-zA-Z0-9]*'
  VariableNaming:
    active: true
    variablePattern: '[a-z][A-Za-z0-9]*'
    privateVariablePattern: '(_)?[a-z][A-Za-z0-9]*'
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'

performance:
  active: true
  ArrayPrimitive:
    active: true
  UnnecessaryTemporaryInstantiation:
    active: true

potential-bugs:
  active: true
  EqualsAlwaysReturnsTrueOrFalse:
    active: true
  UnconditionalJumpStatementInLoop:
    active: true
  UnreachableCode:
    active: true

style:
  active: true
  MagicNumber:
    active: true
    ignoreNumbers: ['-1', '0', '1', '2']
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: true
    ignoreAnnotation: true
  ReturnCount:
    active: true
    max: 3
  UnnecessaryAbstractClass:
    active: true
  UseDataClass:
    active: true
```

### Usage

```bash
# Run Detekt analysis
./gradlew detekt

# Run Detekt with auto-correction
./gradlew detektMain --auto-correct
```

## KtLint

[KtLint](https://ktlint.github.io/) ensures consistent Kotlin code style and formatting.

### Installation

Add to app-level `build.gradle`:

```kotlin
plugins {
    id "org.jlleitschuh.gradle.ktlint" version "11.6.1"
}

ktlint {
    version.set("0.50.0")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)

    reporters {
        reporter {
            reporterType = "plain"
            reporterName = "plain"
        }
        reporter {
            reporterType = "checkstyle"
            reporterName = "checkstyle"
            outputDir = "$buildDir/reports/ktlint"
        }
    }

    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}
```

### Configuration

Create `.editorconfig` in project root:

```ini
[*.{kt,kts}]
# Ktlint configuration
max_line_length = 120
indent_size = 4
insert_final_newline = true
disabled_rules = import-ordering,no-wildcard-imports
```

### Usage

```bash
# Check code style
./gradlew ktlintCheck

# Fix code style issues
./gradlew ktlintFormat
```

## SonarQube

[SonarQube](https://www.sonarqube.org/) provides comprehensive code quality analysis including bugs, vulnerabilities, and code smells.

### Installation

Add to project-level `build.gradle`:

```kotlin
plugins {
    id "org.sonarqube" version "4.4.1.3373"
}

sonarqube {
    properties {
        property "sonar.projectName", "Cashbox Android"
        property "sonar.projectKey", "cashbox-android"
        property "sonar.language", "kotlin"
        property "sonar.sources", "src/main"
        property "sonar.tests", "src/test"
        property "sonar.sourceEncoding", "UTF-8"

        // Android specific
        property "sonar.android.lint.report", "build/reports/lint/lint-report.xml"
        property "sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml"

        // Coverage
        property "sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"

        // Exclusions
        property "sonar.exclusions", "**/R.class,**/R$*.class,**/BuildConfig.*,**/Manifest*.*,**/*Test*.*,android/**/*.*,**/build/**/*.*"
        property "sonar.test.exclusions", "**/test/**,**/androidTest/**"
    }
}
```

### Usage

```bash
# Run SonarQube analysis
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000
```

## Code Coverage with JaCoCo

Configure JaCoCo for code coverage analysis:

```kotlin
// app/build.gradle
android {
    buildTypes {
        debug {
            testCoverageEnabled = true
        }
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/databinding/*Binding.*",
        "**/generated/**/*.*"
    )

    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("jacoco/testDebugUnitTest.exec", "outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
    })
}
```

## Dependency Vulnerability Scanning

### OWASP Dependency Check

Add to project-level `build.gradle`:

```kotlin
plugins {
    id "org.owasp.dependencycheck" version "8.4.2"
}

dependencyCheck {
    format = 'ALL'
    outputDirectory = "$buildDir/reports/dependency-check"
    suppressionFile = 'config/dependency-check-suppressions.xml'

    analyzers {
        experimentalEnabled = true
        archiveEnabled = true
        jarEnabled = true
        centralEnabled = true
        nexusEnabled = false
    }
}
```

## Integration with CI/CD

### GitHub Actions Workflow

```yaml
name: Android Code Quality

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  code-quality:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle packages
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run Android Lint
        run: ./gradlew lintDebug

      - name: Run Detekt
        run: ./gradlew detekt

      - name: Run KtLint Check
        run: ./gradlew ktlintCheck

      - name: Run Unit Tests with Coverage
        run: ./gradlew testDebugUnitTest jacocoTestReport

      - name: Run OWASP Dependency Check
        run: ./gradlew dependencyCheckAnalyze

      - name: Upload Lint Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: lint-results
          path: app/build/reports/lint/

      - name: Upload Detekt Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: detekt-results
          path: app/build/reports/detekt/

      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml

      - name: SonarQube Scan
        uses: sonarqube-quality-gate-action@master
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

## Pre-commit Hooks with Git Hooks

Create `.git/hooks/pre-commit`:

```bash
#!/bin/sh

echo "Running pre-commit checks..."

# Run KtLint check
echo "Running KtLint..."
./gradlew ktlintCheck
if [ $? -ne 0 ]; then
    echo "KtLint check failed. Run './gradlew ktlintFormat' to fix formatting issues."
    exit 1
fi

# Run Detekt
echo "Running Detekt..."
./gradlew detekt
if [ $? -ne 0 ]; then
    echo "Detekt check failed. Please fix the issues before committing."
    exit 1
fi

# Run Android Lint
echo "Running Android Lint..."
./gradlew lintDebug
if [ $? -ne 0 ]; then
    echo "Android Lint check failed. Please fix the issues before committing."
    exit 1
fi

# Run unit tests
echo "Running unit tests..."
./gradlew testDebugUnitTest
if [ $? -ne 0 ]; then
    echo "Unit tests failed. Please fix the failing tests before committing."
    exit 1
fi

echo "All pre-commit checks passed!"
```

## IDE Integration

### Android Studio Configuration

Configure Android Studio to use the same code quality tools:

1. **Code Style Settings**
    - File → Settings → Editor → Code Style → Kotlin
    - Import scheme from `ktlint` configuration

2. **Inspections**
    - File → Settings → Editor → Inspections
    - Enable relevant Kotlin and Android inspections
    - Set severity levels to match project standards

3. **Plugins**
    - Install Detekt plugin
    - Install SonarLint plugin
    - Install Ktlint plugin

### Live Templates

Create live templates for common patterns:

```kotlin
// Abbreviation: viewmodel
class $CLASS_NAME$ViewModel @Inject constructor(
    private val $REPOSITORY$: $REPOSITORY_TYPE$
) : ViewModel() {

    private val _uiState = MutableStateFlow($UI_STATE$())
    val uiState: StateFlow<$UI_STATE$> = _uiState.asStateFlow()

    $END$
}
```

## Quality Gates

Define quality gates that must be met before code can be merged:

### Minimum Requirements

1. **Code Coverage**: Minimum 80% line coverage
2. **Android Lint**: No errors, warnings allowed with justification
3. **Detekt**: No violations above "warning" level
4. **KtLint**: All formatting rules must pass
5. **SonarQube**: Quality gate must pass
6. **Security**: No high or critical security vulnerabilities
7. **Tests**: All unit and integration tests must pass

### Enforcement

```kotlin
// In app/build.gradle
tasks.withType(Test) {
    finalizedBy jacocoTestReport
}

tasks.named("jacocoTestReport") {
    doLast {
        def report = file("${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if (report.exists()) {
            def parser = new XmlParser()
            def results = parser.parse(report)
            def percentage = Math.round(results.@'line-rate' as Double * 100)

            if (percentage < 80) {
                throw new GradleException("Code coverage is below 80%: ${percentage}%")
            }

            println "Code coverage: ${percentage}%"
        }
    }
}
```

## Custom Rules

### Custom Detekt Rules

Create custom rules for project-specific requirements:

```kotlin
// config/detekt/custom-rules/src/main/kotlin/CustomRuleSet.kt
class CustomRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "custom-rules"

    override fun instance(config: Config): RuleSet {
        return RuleSet(
            ruleSetId,
            listOf(
                NoDirectViewModelAccess(config),
                MustUseRepositoryPattern(config),
                RequireHiltViewModel(config)
            )
        )
    }
}

class NoDirectViewModelAccess(config: Config = Config.empty) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Warning,
        "ViewModels should not be accessed directly in Fragments. Use by viewModels() delegate.",
        Debt.FIVE_MINS
    )

    override fun visitClass(klass: KtClass) {
        super.visitClass(klass)

        if (klass.isFragment()) {
            klass.body?.declarations?.forEach { declaration ->
                if (declaration is KtProperty && declaration.isViewModelProperty()) {
                    report(CodeSmell(issue, Entity.from(declaration), issue.description))
                }
            }
        }
    }
}
```

## Best Practices

1. **Early Integration**: Set up static analysis tools at the beginning of the project
2. **Incremental Adoption**: For existing projects, adopt tools incrementally
3. **CI/CD Integration**: Always integrate tools into your CI/CD pipeline
4. **Pre-commit Hooks**: Use pre-commit hooks to catch issues early
5. **Team Education**: Ensure all team members understand the tools and their benefits
6. **Regular Updates**: Keep tools and their configurations up to date
7. **Documentation**: Document tool-specific configurations and exceptions
8. **Quality Gates**: Enforce quality gates in the build process
9. **Custom Rules**: Create custom rules for project-specific requirements
10. **Monitoring**: Monitor code quality trends over time

## Conclusion

By using these tools consistently and properly configuring them for Android development, we maintain high code quality standards, catch bugs early, and ensure a more maintainable and secure Android application. All developers on the project are expected to use these tools and address any issues they identify.
