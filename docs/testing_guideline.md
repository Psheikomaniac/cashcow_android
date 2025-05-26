# Testing Guidelines (Android)

This document outlines the testing strategy, best practices, and guidelines for the Cashbox Android application.

## Overview

Testing is a critical component of our Android development process. We follow a comprehensive testing approach to ensure the quality, reliability, and performance of our mobile application. This document provides guidelines for implementing effective tests at various levels of the Android application.

## Testing Principles

1. **Test Early, Test Often**: Testing should be integrated throughout the development process
2. **Test Automation**: Automate tests whenever possible to enable frequent execution
3. **Test Coverage**: Aim for high test coverage (minimum 80%) for critical code paths
4. **Test Independence**: Tests should be independent and not rely on the state of other tests
5. **Test Readability**: Tests should be clear and readable, serving as documentation
6. **Test on Real Devices**: Include testing on physical devices alongside emulators

## Test Types

### Unit Tests

Unit tests verify the behavior of individual components in isolation using JUnit and MockK.

#### Guidelines

- Test each class in isolation
- Mock external dependencies using MockK
- Focus on testing business logic in ViewModels and Use Cases
- Cover edge cases and error conditions
- Keep tests small and focused
- Use meaningful test names

#### Example

```kotlin
@ExtendWith(MockKExtension::class)
class PenaltyViewModelTest {
    
    @MockK
    private lateinit var penaltyRepository: PenaltyRepository
    
    @MockK
    private lateinit var getUserPenaltiesUseCase: GetUserPenaltiesUseCase
    
    private lateinit var viewModel: PenaltyViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = PenaltyViewModel(penaltyRepository, getUserPenaltiesUseCase)
    }
    
    @Test
    fun `when loading penalties, should update ui state to loading`() = runTest {
        // Given
        coEvery { getUserPenaltiesUseCase() } returns flowOf(emptyList())
        
        // When
        viewModel.loadPenalties()
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isLoading)
    }
    
    @Test
    fun `when penalties loaded successfully, should update penalties list`() = runTest {
        // Given
        val expectedPenalties = listOf(
            createMockPenalty(id = "1", reason = "Late arrival"),
            createMockPenalty(id = "2", reason = "Missed training")
        )
        coEvery { getUserPenaltiesUseCase() } returns flowOf(expectedPenalties)
        
        // When
        viewModel.loadPenalties()
        
        // Then
        assertEquals(expectedPenalties, viewModel.penalties.value)
        assertFalse(viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `when loading penalties fails, should show error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getUserPenaltiesUseCase() } throws Exception(errorMessage)
        
        // When
        viewModel.loadPenalties()
        
        // Then
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }
    
    private fun createMockPenalty(
        id: String = "1",
        reason: String = "Test penalty"
    ): Penalty {
        return Penalty(
            id = id,
            teamUser = mockk(),
            type = mockk(),
            reason = reason,
            amount = Money.fromCents(500),
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
```

### Integration Tests

Integration tests verify the interaction between components, particularly database operations and repository implementations.

#### Guidelines

- Test interactions between components
- Use Room's in-memory database for testing
- Test repository implementations with real database
- Focus on data flow between layers
- Test error handling scenarios

#### Example

```kotlin
@RunWith(AndroidJUnit4::class)
class PenaltyRepositoryTest {
    
    private lateinit var database: CashboxDatabase
    private lateinit var penaltyDao: PenaltyDao
    private lateinit var apiService: CashboxApiService
    private lateinit var repository: PenaltyRepository
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CashboxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        penaltyDao = database.penaltyDao()
        apiService = mockk()
        repository = PenaltyRepository(penaltyDao, apiService, mockk())
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun getAllPenalties_returnsFlowOfPenalties() = runTest {
        // Given
        val penalty1 = createTestPenaltyEntity(id = "1", reason = "Late arrival")
        val penalty2 = createTestPenaltyEntity(id = "2", reason = "Missed training")
        penaltyDao.insertPenalty(penalty1)
        penaltyDao.insertPenalty(penalty2)
        
        // When
        val penalties = repository.getAllPenalties().first()
        
        // Then
        assertEquals(2, penalties.size)
        assertEquals("Late arrival", penalties[0].reason)
        assertEquals("Missed training", penalties[1].reason)
    }
    
    @Test
    fun createPenalty_whenOnline_shouldSyncWithApi() = runTest {
        // Given
        val penalty = createTestPenalty()
        val apiResponse = createTestPenaltyDto()
        coEvery { apiService.createPenalty(any()) } returns apiResponse
        
        // When
        val result = repository.createPenalty(penalty)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { apiService.createPenalty(any()) }
        
        val savedPenalties = penaltyDao.getAllActivePenalties().first()
        assertEquals(1, savedPenalties.size)
    }
    
    private fun createTestPenaltyEntity(
        id: String = "1",
        reason: String = "Test penalty"
    ): PenaltyEntity {
        return PenaltyEntity(
            id = id,
            teamUserId = "user1",
            typeId = "type1",
            reason = reason,
            amount = 500,
            currency = "EUR"
        )
    }
}
```

### UI Tests (Instrumented Tests)

UI tests verify the application's behavior from a user's perspective using Espresso.

#### Guidelines

- Test complete user workflows
- Use Espresso for UI interactions
- Test different screen sizes and orientations
- Verify accessibility features
- Test error states and loading indicators
- Use Hilt for dependency injection in tests

#### Example

```kotlin
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class PenaltyListFragmentTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @BindValue
    @MockK
    lateinit var penaltyRepository: PenaltyRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
        
        // Mock repository responses
        coEvery { penaltyRepository.getAllPenalties() } returns flowOf(
            listOf(
                createMockPenalty(reason = "Late arrival", amount = Money.fromCents(500)),
                createMockPenalty(reason = "Missed training", amount = Money.fromCents(1000))
            )
        )
    }
    
    @Test
    fun displaysPenaltiesList() {
        // Navigate to penalty list
        onView(withId(R.id.navigation_penalties)).perform(click())
        
        // Verify penalties are displayed
        onView(withText("Late arrival")).check(matches(isDisplayed()))
        onView(withText("Missed training")).check(matches(isDisplayed()))
        onView(withText("5.00 €")).check(matches(isDisplayed()))
        onView(withText("10.00 €")).check(matches(isDisplayed()))
    }
    
    @Test
    fun clickingPenaltyNavigatesToDetail() {
        // Navigate to penalty list
        onView(withId(R.id.navigation_penalties)).perform(click())
        
        // Click on first penalty
        onView(withText("Late arrival")).perform(click())
        
        // Verify navigation to detail screen
        onView(withId(R.id.penalty_detail_container)).check(matches(isDisplayed()))
    }
    
    @Test
    fun fabOpensCreatePenaltyDialog() {
        // Navigate to penalty list
        onView(withId(R.id.navigation_penalties)).perform(click())
        
        // Click FAB
        onView(withId(R.id.fab_add_penalty)).perform(click())
        
        // Verify create penalty dialog is shown
        onView(withText("Create Penalty")).check(matches(isDisplayed()))
        onView(withId(R.id.edit_penalty_reason)).check(matches(isDisplayed()))
    }
    
    @Test
    fun showsLoadingIndicatorWhileFetchingData() {
        // Mock loading state
        coEvery { penaltyRepository.getAllPenalties() } returns flow {
            delay(1000) // Simulate loading
            emit(emptyList())
        }
        
        // Navigate to penalty list
        onView(withId(R.id.navigation_penalties)).perform(click())
        
        // Verify loading indicator is shown
        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
    }
}
```

### API Tests

API tests verify the behavior of network layer and API integrations.

#### Guidelines

- Test API service interfaces
- Mock network responses using MockWebServer
- Test error handling for network failures
- Verify request/response serialization
- Test authentication token handling

#### Example

```kotlin
@RunWith(AndroidJUnit4::class)
class CashboxApiServiceTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: CashboxApiService
    private val gson = Gson()
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(CashboxApiService::class.java)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun getPenalties_returnsListOfPenalties() = runTest {
        // Given
        val mockPenalties = listOf(
            createMockPenaltyDto(id = "1", reason = "Late arrival"),
            createMockPenaltyDto(id = "2", reason = "Missed training")
        )
        val mockResponse = ApiResponse(data = mockPenalties, success = true)
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(gson.toJson(mockResponse))
        )
        
        // When
        val response = apiService.getPenalties()
        
        // Then
        assertEquals(2, response.data.size)
        assertEquals("Late arrival", response.data[0].reason)
        assertEquals("Missed training", response.data[1].reason)
        
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/penalties", request.path)
    }
    
    @Test
    fun createPenalty_sendsCorrectRequest() = runTest {
        // Given
        val createPenaltyDto = CreatePenaltyDto(
            teamUserId = "user1",
            typeId = "type1",
            reason = "New penalty",
            amount = 500,
            currency = "EUR"
        )
        val responseDto = createMockPenaltyDto(reason = "New penalty")
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
                .setBody(gson.toJson(responseDto))
        )
        
        // When
        val response = apiService.createPenalty(createPenaltyDto)
        
        // Then
        assertEquals("New penalty", response.reason)
        
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/penalties", request.path)
        
        val requestBody = gson.fromJson(request.body.readUtf8(), CreatePenaltyDto::class.java)
        assertEquals("New penalty", requestBody.reason)
    }
}
```

### Repository Pattern Tests

Test repository implementations that coordinate between local and remote data sources.

#### Example

```kotlin
@RunWith(AndroidJUnit4::class)
class PenaltyRepositoryIntegrationTest {
    
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: CashboxDatabase
    private lateinit var repository: PenaltyRepository
    private lateinit var apiService: CashboxApiService
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CashboxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        apiService = mockk()
        repository = PenaltyRepository(
            penaltyDao = database.penaltyDao(),
            apiService = apiService,
            networkConnectivityManager = mockk()
        )
    }
    
    @Test
    fun synchronizePenalties_updatesLocalDatabase() = runTest {
        // Given
        val remotePenalties = listOf(
            createMockPenaltyDto(id = "1", reason = "Remote penalty 1"),
            createMockPenaltyDto(id = "2", reason = "Remote penalty 2")
        )
        coEvery { apiService.getPenalties() } returns ApiResponse(remotePenalties, true)
        
        // When
        repository.synchronizePenalties()
        
        // Then
        val localPenalties = database.penaltyDao().getAllActivePenalties().first()
        assertEquals(2, localPenalties.size)
        assertEquals("Remote penalty 1", localPenalties[0].reason)
    }
}
```

## Test Dependencies

All testing dependencies should be added to the app-level build.gradle:

```kotlin
dependencies {
    // Unit Testing
    testImplementation "junit:junit:4.13.2"
    testImplementation "io.mockk:mockk:1.13.8"
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
    testImplementation "androidx.arch.core:core-testing:2.2.0"
    testImplementation "app.cash.turbine:turbine:1.0.0"
    
    // Integration Testing
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test:runner:1.5.2"
    androidTestImplementation "androidx.test:rules:1.5.0"
    androidTestImplementation "androidx.room:room-testing:2.6.1"
    
    // UI Testing
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
    androidTestImplementation "androidx.test.espresso:espresso-contrib:3.5.1"
    androidTestImplementation "androidx.test.espresso:espresso-intents:3.5.1"
    androidTestImplementation "androidx.fragment:fragment-testing:1.6.2"
    
    // Hilt Testing
    androidTestImplementation "com.google.dagger:hilt-android-testing:2.48"
    kaptAndroidTest "com.google.dagger:hilt-compiler:2.48"
    
    // Network Testing
    testImplementation "com.squareup.okhttp3:mockwebserver:4.12.0"
    androidTestImplementation "com.squareup.okhttp3:mockwebserver:4.12.0"
}
```

## Test Organization

### Directory Structure

```
app/src/
├── test/                           # Unit tests
│   ├── java/com/cashbox/
│   │   ├── data/
│   │   │   ├── repository/        # Repository unit tests
│   │   │   └── mapper/            # Mapper unit tests
│   │   ├── domain/
│   │   │   └── usecase/           # Use case unit tests
│   │   ├── presentation/
│   │   │   └── viewmodel/         # ViewModel unit tests
│   │   └── utils/                 # Utility unit tests
├── androidTest/                    # Instrumented tests
│   ├── java/com/cashbox/
│   │   ├── data/
│   │   │   ├── local/             # Database integration tests
│   │   │   ├── remote/            # API integration tests
│   │   │   └── repository/        # Repository integration tests
│   │   ├── presentation/
│   │   │   └── ui/                # UI tests
│   │   └── utils/                 # Test utilities
└── sharedTest/                     # Shared test utilities
    └── java/com/cashbox/
        ├── factory/                # Test data factories
        ├── rule/                   # Custom test rules
        └── matcher/                # Custom Espresso matchers
```

### Naming Conventions

- Test classes should be named with the class they test followed by "Test"
- Test methods should describe the scenario being tested using backticks for readability
- Use "Given-When-Then" pattern in test method names when appropriate

```kotlin
class PenaltyViewModelTest {
    
    @Test
    fun `when loading penalties succeeds, should update penalties list and hide loading`() {
        // Test implementation
    }
    
    @Test
    fun `given network error, when loading penalties, should show error message`() {
        // Test implementation
    }
}
```

## Test Configuration

### Gradle Configuration

```kotlin
// app/build.gradle
android {
    // ...
    
    testOptions {
        unitTests {
            includeAndroidResources = true
            returnDefaultValues = true
        }
        
        animationsDisabled = true
    }
    
    testInstrumentationRunner "com.cashbox.HiltTestRunner"
}
```

### Custom Test Runner

```kotlin
class HiltTestRunner : AndroidJUnitRunner() {
    
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
```

### Test Application

```kotlin
@HiltAndroidApp
class HiltTestApplication : Application()
```

## Testing Best Practices

### 1. Test Data Factories

Create factories for consistent test data creation:

```kotlin
object PenaltyTestFactory {
    
    fun createPenalty(
        id: String = "penalty_${UUID.randomUUID()}",
        reason: String = "Test penalty",
        amount: Money = Money.fromCents(500),
        archived: Boolean = false
    ): Penalty {
        return Penalty(
            id = id,
            teamUser = TeamUserTestFactory.createTeamUser(),
            type = PenaltyTypeTestFactory.createPenaltyType(),
            reason = reason,
            amount = amount,
            archived = archived,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
    
    fun createPenaltyEntity(
        id: String = "penalty_${UUID.randomUUID()}",
        reason: String = "Test penalty",
        amount: Int = 500
    ): PenaltyEntity {
        return PenaltyEntity(
            id = id,
            teamUserId = "user1",
            typeId = "type1",
            reason = reason,
            amount = amount,
            currency = "EUR"
        )
    }
}
```

### 2. Custom Espresso Matchers

Create custom matchers for complex UI validations:

```kotlin
object CustomMatchers {
    
    fun hasItemCount(expectedCount: Int): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item count: $expectedCount")
            }
            
            override fun matchesSafely(view: RecyclerView): Boolean {
                return view.adapter?.itemCount == expectedCount
            }
        }
    }
    
    fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }
            
            override fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder = view.findViewHolderForAdapterPosition(position)
                    ?: return false
                return itemMatcher.matches(viewHolder.itemView)
            }
        }
    }
}
```

### 3. Test Rules

Create custom test rules for common setup:

```kotlin
class DatabaseRule : TestWatcher() {
    
    lateinit var database: CashboxDatabase
        private set
    
    override fun starting(description: Description?) {
        super.starting(description)
        
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CashboxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
    
    override fun finished(description: Description?) {
        super.finished(description)
        database.close()
    }
}
```

## Continuous Integration

### GitHub Actions Configuration

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

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
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Run instrumented tests
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: 29
        script: ./gradlew connectedAndroidTest
    
    - name: Generate test report
      run: ./gradlew jacocoTestReport
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
```

## Performance Testing

### Memory and Performance

```kotlin
@RunWith(AndroidJUnit4::class)
@LargeTest
class PerformanceTest {
    
    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    @Test
    fun benchmarkPenaltyListScrolling() {
        benchmarkRule.measureRepeated {
            // Setup large dataset
            val penalties = (1..1000).map { 
                PenaltyTestFactory.createPenalty(id = "penalty_$it") 
            }
            
            // Measure scrolling performance
            onView(withId(R.id.recycler_view_penalties))
                .perform(RecyclerViewActions.scrollToPosition(500))
        }
    }
}
```

## Test Documentation

Document complex test scenarios and explain testing decisions:

```kotlin
/**
 * Tests the penalty synchronization flow between local and remote data sources.
 * 
 * This test verifies that:
 * 1. Local penalties are uploaded when network becomes available
 * 2. Remote penalties are downloaded and stored locally
 * 3. Conflicts are resolved using last-write-wins strategy
 */
class PenaltySynchronizationTest {
    // Test implementation
}
```

## Conclusion

A comprehensive testing strategy is essential for maintaining code quality and preventing regressions in Android applications. By following these guidelines and using the appropriate testing frameworks, we can create reliable, maintainable, and high-quality mobile software.