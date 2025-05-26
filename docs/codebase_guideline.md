# Codebase Guidelines (Android)

## Introduction

This document outlines the coding standards and best practices for the Cashbox Android project. Adherence to these guidelines ensures code consistency, maintainability, quality, and security. The project emphasizes comprehensive testing, modern Android development practices, and user experience excellence.

## General Principles

### SOLID Principles

All code should follow the SOLID principles adapted for Android development:
- **S**ingle Responsibility Principle: Each class should have one reason to change
- **O**pen/Closed Principle: Open for extension, closed for modification
- **L**iskov Substitution Principle: Objects should be replaceable with instances of their subtypes
- **I**nterface Segregation Principle: Many client-specific interfaces are better than one general-purpose interface
- **D**ependency Inversion Principle: Depend on abstractions, not concretions

### Clean Code for Android

- Write self-documenting code with clear, intention-revealing names
- Keep methods and classes small and focused
- Avoid deep nesting and complex conditionals
- Follow the "Boy Scout Rule": Leave the code cleaner than you found it
- Consider mobile constraints: battery, memory, and performance

## Kotlin Standards

### Version and Features

- Use Kotlin 1.9.x features appropriately
- Leverage coroutines for asynchronous programming
- Use sealed classes for type-safe state management
- Employ data classes for DTOs and simple data containers
- Take advantage of extension functions for utility methods
- Use nullable types appropriately with safe calls
- Prefer immutable data structures when possible

### Android-Specific Kotlin Usage

```kotlin
// Prefer sealed classes for UI states
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Penalty>) : UiState()
    data class Error(val message: String) : UiState()
}

// Use data classes for entities
data class Penalty(
    val id: String,
    val amount: Money,
    val reason: String,
    val createdAt: Instant,
    val isPaid: Boolean = false
)

// Extension functions for Android utilities
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// Coroutines for async operations
suspend fun loadPenalties(): List<Penalty> = withContext(Dispatchers.IO) {
    penaltyDao.getAllPenalties()
}
```

### Formatting and Style

- Follow Android Kotlin Style Guide
- Use 4 spaces for indentation
- Keep line length around 120 characters
- Use trailing commas in multiline constructs
- Organize imports alphabetically

### Naming Conventions

- **Classes**: PascalCase, descriptive nouns (e.g., `PenaltyRepository`, `UserViewModel`)
- **Interfaces**: PascalCase, descriptive adjectives or nouns (e.g., `Cacheable`, `DataSource`)
- **Methods**: camelCase, verb or verb phrases (e.g., `calculateTotal()`, `findByUserId()`)
- **Properties**: camelCase, descriptive nouns (e.g., `paymentAmount`, `isLoggedIn`)
- **Constants**: UPPER_CASE with underscores (e.g., `MAX_RETRY_ATTEMPTS`)
- **Variables**: camelCase, clear and descriptive (e.g., `totalAmount`, `isNetworkAvailable`)

### Android-Specific Naming

```kotlin
// Activities
class MainActivity : AppCompatActivity()
class PenaltyDetailActivity : AppCompatActivity()

// Fragments
class PenaltyListFragment : Fragment()
class UserProfileFragment : Fragment()

// ViewModels
class PenaltyViewModel : ViewModel()
class AuthenticationViewModel : ViewModel()

// Repositories
class PenaltyRepository
class UserRepository

// Use Cases
class CreatePenaltyUseCase
class GetUserPenaltiesUseCase

// Data classes for API
data class PenaltyDto(...)
data class CreatePenaltyRequest(...)

// Room entities
@Entity(tableName = "penalties")
data class PenaltyEntity(...)
```

### Documentation

- All public classes, methods, and interfaces should have KDoc comments
- Document parameters, return types, and exceptions
- Include usage examples for complex APIs
- Keep comments current when code changes

```kotlin
/**
 * Repository for managing penalty data with offline-first approach.
 * 
 * This repository coordinates between local Room database and remote API,
 * providing a single source of truth for penalty data.
 * 
 * @param penaltyDao Local database access object
 * @param apiService Remote API service
 * @param networkManager Network connectivity manager
 */
class PenaltyRepository @Inject constructor(
    private val penaltyDao: PenaltyDao,
    private val apiService: CashboxApiService,
    private val networkManager: NetworkConnectivityManager
) {
    /**
     * Creates a new penalty with automatic sync.
     * 
     * @param penalty The penalty to create
     * @return Result containing the created penalty or error
     * @throws NetworkException if network is unavailable and local storage fails
     */
    suspend fun createPenalty(penalty: Penalty): Result<Penalty> {
        // Implementation
    }
}
```

## Android Architecture Guidelines

### MVVM with Clean Architecture

The application follows MVVM pattern with Clean Architecture:

```kotlin
// UI Layer - Compose/Fragment
@Composable
fun PenaltyListScreen(
    viewModel: PenaltyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> PenaltyList(uiState.data)
        is UiState.Error -> ErrorMessage(uiState.message)
    }
}

// Presentation Layer - ViewModel
@HiltViewModel
class PenaltyViewModel @Inject constructor(
    private val getPenaltiesUseCase: GetPenaltiesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun loadPenalties() {
        viewModelScope.launch {
            getPenaltiesUseCase()
                .catch { _uiState.value = UiState.Error(it.message ?: "Unknown error") }
                .collect { penalties -> _uiState.value = UiState.Success(penalties) }
        }
    }
}

// Domain Layer - Use Case
class GetPenaltiesUseCase @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) {
    operator fun invoke(): Flow<List<Penalty>> = 
        penaltyRepository.getAllPenalties()
}

// Data Layer - Repository
class PenaltyRepository @Inject constructor(
    private val localDataSource: PenaltyLocalDataSource,
    private val remoteDataSource: PenaltyRemoteDataSource
) {
    fun getAllPenalties(): Flow<List<Penalty>> = 
        localDataSource.getAllPenalties()
            .map { entities -> entities.map { it.toDomainModel() } }
}
```

### Dependency Injection with Hilt

- Use Hilt for dependency injection throughout the application
- Define modules for different layers
- Use appropriate scopes for different components
- Avoid manual dependency creation

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CashboxDatabase {
        return Room.databaseBuilder(
            context,
            CashboxDatabase::class.java,
            "cashbox_database"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }
    
    @Provides
    fun providePenaltyDao(database: CashboxDatabase): PenaltyDao = 
        database.penaltyDao()
}

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

## UI Development with Jetpack Compose

### Compose Best Practices

- Use state hoisting for reusable components
- Implement proper remember and recomposition optimizations
- Follow Material Design 3 guidelines
- Ensure accessibility compliance

```kotlin
@Composable
fun PenaltyCard(
    penalty: Penalty,
    onPayClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = penalty.reason,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { 
                    contentDescription = "Penalty reason: ${penalty.reason}"
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = penalty.amount.toCurrency(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (!penalty.isPaid) {
                Button(
                    onClick = { onPayClick(penalty.id) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Mark as Paid")
                }
            }
        }
    }
}
```

### State Management

- Use StateFlow and LiveData appropriately
- Implement unidirectional data flow
- Handle configuration changes properly

```kotlin
@HiltViewModel
class PenaltyViewModel @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PenaltyUiState())
    val uiState: StateFlow<PenaltyUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: PenaltyUiEvent) {
        when (event) {
            is PenaltyUiEvent.LoadPenalties -> loadPenalties()
            is PenaltyUiEvent.CreatePenalty -> createPenalty(event.penalty)
            is PenaltyUiEvent.MarkAsPaid -> markAsPaid(event.penaltyId)
        }
    }
    
    private fun loadPenalties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            penaltyRepository.getAllPenalties()
                .catch { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message
                        )
                    }
                }
                .collect { penalties ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            penalties = penalties,
                            errorMessage = null
                        )
                    }
                }
        }
    }
}

data class PenaltyUiState(
    val isLoading: Boolean = false,
    val penalties: List<Penalty> = emptyList(),
    val errorMessage: String? = null
)

sealed class PenaltyUiEvent {
    object LoadPenalties : PenaltyUiEvent()
    data class CreatePenalty(val penalty: Penalty) : PenaltyUiEvent()
    data class MarkAsPaid(val penaltyId: String) : PenaltyUiEvent()
}
```

## Data Layer Guidelines

### Room Database

- Use appropriate annotations and relationships
- Implement proper migrations
- Use TypeConverters for complex types
- Follow database normalization principles

```kotlin
@Entity(tableName = "penalties")
data class PenaltyEntity(
    @PrimaryKey val id: String,
    val teamUserId: String,
    val typeId: String,
    val reason: String,
    val amount: Int, // Store as cents
    val currency: String,
    val archived: Boolean = false,
    val paidAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface PenaltyDao {
    @Query("SELECT * FROM penalties WHERE archived = 0 ORDER BY createdAt DESC")
    fun getAllActivePenalties(): Flow<List<PenaltyEntity>>
    
    @Query("SELECT * FROM penalties WHERE teamUserId = :userId")
    suspend fun getPenaltiesByUser(userId: String): List<PenaltyEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenalty(penalty: PenaltyEntity)
    
    @Update
    suspend fun updatePenalty(penalty: PenaltyEntity)
    
    @Transaction
    suspend fun insertOrUpdatePenalties(penalties: List<PenaltyEntity>) {
        penalties.forEach { penalty ->
            insertPenalty(penalty)
        }
    }
}

@TypeConverter
class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()
    
    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
}
```

### Network Layer with Retrofit

- Use appropriate HTTP methods and status codes
- Implement proper error handling
- Use interceptors for common functionality
- Handle network connectivity properly

```kotlin
interface CashboxApiService {
    
    @GET("penalties")
    suspend fun getPenalties(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("teamId") teamId: String? = null
    ): ApiResponse<List<PenaltyDto>>
    
    @POST("penalties")
    suspend fun createPenalty(@Body penalty: CreatePenaltyRequest): PenaltyDto
    
    @PUT("penalties/{id}")
    suspend fun updatePenalty(
        @Path("id") id: String,
        @Body penalty: UpdatePenaltyRequest
    ): PenaltyDto
    
    @PATCH("penalties/{id}/pay")
    suspend fun markPenaltyAsPaid(@Path("id") id: String): PenaltyDto
}

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getAccessToken()
        
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
            
        return chain.proceed(newRequest)
    }
}
```

## Testing Guidelines

### Unit Testing

- Test ViewModels and business logic thoroughly
- Use MockK for mocking dependencies
- Test error scenarios and edge cases
- Aim for high test coverage

```kotlin
@ExtendWith(MockKExtension::class)
class PenaltyViewModelTest {
    
    @MockK
    private lateinit var penaltyRepository: PenaltyRepository
    
    private lateinit var viewModel: PenaltyViewModel
    
    @BeforeEach
    fun setup() {
        viewModel = PenaltyViewModel(penaltyRepository)
    }
    
    @Test
    fun `when loading penalties succeeds, should update state with penalties`() = runTest {
        // Given
        val expectedPenalties = listOf(
            createTestPenalty(id = "1", reason = "Late arrival")
        )
        every { penaltyRepository.getAllPenalties() } returns flowOf(expectedPenalties)
        
        // When
        viewModel.onEvent(PenaltyUiEvent.LoadPenalties)
        
        // Then
        val uiState = viewModel.uiState.value
        assertEquals(expectedPenalties, uiState.penalties)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
    }
}
```

### Integration Testing

- Test database operations with Room
- Test API integrations with MockWebServer
- Test repository implementations

```kotlin
@RunWith(AndroidJUnit4::class)
class PenaltyDaoTest {
    
    private lateinit var database: CashboxDatabase
    private lateinit var penaltyDao: PenaltyDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CashboxDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        penaltyDao = database.penaltyDao()
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun insertAndGetPenalty() = runTest {
        // Given
        val penalty = createTestPenaltyEntity()
        
        // When
        penaltyDao.insertPenalty(penalty)
        val penalties = penaltyDao.getAllActivePenalties().first()
        
        // Then
        assertEquals(1, penalties.size)
        assertEquals(penalty.id, penalties[0].id)
    }
}
```

### UI Testing with Compose

- Test user interactions and navigation
- Verify UI state changes
- Test accessibility features

```kotlin
@RunWith(AndroidJUnit4::class)
class PenaltyListScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun displaysLoadingState() {
        composeTestRule.setContent {
            PenaltyListScreen(
                uiState = PenaltyUiState(isLoading = true),
                onEvent = {}
            )
        }
        
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }
    
    @Test
    fun displaysPenaltiesList() {
        val penalties = listOf(
            createTestPenalty(reason = "Late arrival"),
            createTestPenalty(reason = "Missed training")
        )
        
        composeTestRule.setContent {
            PenaltyListScreen(
                uiState = PenaltyUiState(penalties = penalties),
                onEvent = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Late arrival")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Missed training")
            .assertIsDisplayed()
    }
}
```

## Performance Optimization

### Memory Management

- Avoid memory leaks with proper lifecycle management
- Use appropriate data structures
- Implement efficient image caching
- Profile memory usage regularly

```kotlin
// Avoid memory leaks in ViewModels
class PenaltyViewModel @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) : ViewModel() {
    
    private val _penalties = MutableLiveData<List<Penalty>>()
    val penalties: LiveData<List<Penalty>> = _penalties
    
    private var loadPenaltiesJob: Job? = null
    
    fun loadPenalties() {
        loadPenaltiesJob?.cancel()
        loadPenaltiesJob = viewModelScope.launch {
            // Load penalties
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        loadPenaltiesJob?.cancel()
    }
}
```

### Battery Optimization

- Follow Doze mode and App Standby guidelines
- Use WorkManager for background tasks
- Minimize location and sensor usage
- Implement efficient synchronization

```kotlin
@HiltWorker
class PenaltySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val penaltyRepository: PenaltyRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            penaltyRepository.syncPenalties()
            Result.success()
        } catch (exception: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): PenaltySyncWorker
    }
}
```

## Security Guidelines

### Data Protection

- Use EncryptedSharedPreferences for sensitive data
- Implement certificate pinning for network communication
- Avoid logging sensitive information
- Use Android Keystore for cryptographic operations

```kotlin
@Singleton
class SecureTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveAccessToken(token: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }
    
    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}
```

### Network Security

- Use HTTPS for all network communication
- Implement certificate pinning
- Validate all inputs
- Handle authentication securely

```kotlin
class NetworkSecurityConfig {
    
    fun createOkHttpClient(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add("api.cashbox.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()
            
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```

## Accessibility Guidelines

- Provide meaningful content descriptions
- Support TalkBack and other screen readers
- Implement proper focus management
- Support high contrast and large text

```kotlin
@Composable
fun AccessiblePenaltyCard(
    penalty: Penalty,
    onPayClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .semantics {
                contentDescription = "Penalty card for ${penalty.reason}, " +
                    "amount ${penalty.amount.toCurrency()}, " +
                    if (penalty.isPaid) "paid" else "unpaid"
            }
    ) {
        // Card content
        
        Button(
            onClick = { onPayClick(penalty.id) },
            modifier = Modifier.semantics {
                contentDescription = "Mark penalty as paid"
            }
        ) {
            Text("Mark as Paid")
        }
    }
}
```

## External Dependencies

### Package Management

- Use Gradle for all dependencies
- Do not create custom libraries when established packages exist
- Document all dependencies in `build.gradle.kts`
- Keep dependencies up to date with security patches
- Use version catalogs for centralized dependency management

### Recommended Libraries

The following libraries are recommended and should be used instead of creating custom implementations:

- **Android Architecture Components**: Use AndroidX libraries for lifecycle, navigation, etc.
- **Jetpack Compose**: For modern declarative UI development
- **Room**: For local database operations
- **Retrofit**: For network communication
- **Hilt**: For dependency injection
- **Coroutines**: For asynchronous programming
- **Coil**: For image loading
- **Timber**: For logging

## Conclusion

Following these codebase guidelines ensures that the Cashbox Android application maintains high standards of code quality, performance, security, and user experience. All developers working on the project should adhere to these guidelines and contribute to their continuous improvement.