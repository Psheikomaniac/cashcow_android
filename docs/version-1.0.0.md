# Version 1.0.0: Foundation and Penalties Management (Android)

## Overview

Version 1.0.0 focuses on establishing the foundation of the Cashbox Android application, with particular emphasis on the core functionality for tracking and managing penalties and drinks. This version will implement the essential components required for a functional mobile application.

## Release Timeline

- **Development Start**: June 1, 2025
- **Alpha Release**: June 15, 2025
- **Beta Release**: June 30, 2025
- **Production Release**: July 15, 2025

## Scope

### Core Android Components

1. **Application Architecture**
   - Implement MVVM architecture with Android Architecture Components
   - Set up dependency injection with Dagger Hilt
   - Configure Retrofit for API communication
   - Set up Room database for local caching
   - Implement Navigation Component
   - Configure logging and crash reporting

2. **Data Layer**
   - Implement Repository pattern for data management
   - Set up local database with Room
   - Configure API service interfaces with Retrofit
   - Implement data synchronization logic
   - Set up offline-first architecture
   - Configure data binding and validation

3. **Authentication and Authorization**
   - Implement JWT token management
   - Set up secure credential storage with EncryptedSharedPreferences
   - Configure biometric authentication
   - Implement auto-logout functionality
   - Set up refresh token handling
   - Create login and registration flows

4. **Core Features**
   - Team selection and management UI
   - User profile management
   - Penalty tracking and creation
   - Drink logging functionality
   - Payment status overview
   - Basic reporting views

5. **User Interface Components**
   - Material Design 3 implementation
   - Responsive layouts for different screen sizes
   - Dark mode support
   - Accessibility features
   - Custom themes and styling
   - Animation and transitions

6. **Data Import/Export**
   - CSV import capability through file picker
   - Share functionality for data export
   - Cloud backup integration
   - Data validation and error handling

## Technical Requirements

### Android Architecture

```kotlin
// Application Architecture Structure
app/
├── di/                     // Dependency Injection
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── data/
│   ├── local/
│   │   ├── dao/           // Room DAOs
│   │   ├── entities/      // Room Entities
│   │   └── database/      // Database setup
│   ├── remote/
│   │   ├── api/           // Retrofit API interfaces
│   │   ├── dto/           // Data Transfer Objects
│   │   └── interceptors/  // Network interceptors
│   └── repository/        // Repository implementations
├── domain/
│   ├── model/             // Domain models
│   ├── repository/        // Repository interfaces
│   └── usecase/           // Use cases
├── presentation/
│   ├── ui/
│   │   ├── activities/    // Activities
│   │   ├── fragments/     // Fragments
│   │   ├── adapters/      // RecyclerView adapters
│   │   └── dialogs/       // Dialog fragments
│   ├── viewmodel/         // ViewModels
│   └── utils/             // UI utilities
└── utils/                 // General utilities
```

### Key Android Classes

1. **PenaltyEntity (Room)**
   ```kotlin
   @Entity(tableName = "penalties")
   data class PenaltyEntity(
       @PrimaryKey val id: String,
       val teamUserId: String,
       val typeId: String,
       val reason: String,
       val amount: Int,
       val currency: String,
       val archived: Boolean = false,
       val paidAt: Long? = null,
       val createdAt: Long = System.currentTimeMillis(),
       val updatedAt: Long = System.currentTimeMillis()
   )
   ```

2. **PenaltyDao**
   ```kotlin
   @Dao
   interface PenaltyDao {
       @Query("SELECT * FROM penalties WHERE archived = 0")
       fun getAllActivePenalties(): Flow<List<PenaltyEntity>>
       
       @Query("SELECT * FROM penalties WHERE teamUserId = :userId")
       fun getPenaltiesByUser(userId: String): Flow<List<PenaltyEntity>>
       
       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insertPenalty(penalty: PenaltyEntity)
       
       @Update
       suspend fun updatePenalty(penalty: PenaltyEntity)
       
       @Delete
       suspend fun deletePenalty(penalty: PenaltyEntity)
   }
   ```

3. **PenaltyRepository**
   ```kotlin
   @Singleton
   class PenaltyRepository @Inject constructor(
       private val penaltyDao: PenaltyDao,
       private val apiService: CashboxApiService,
       private val networkConnectivityManager: NetworkConnectivityManager
   ) {
       
       fun getAllPenalties(): Flow<List<Penalty>> = 
           penaltyDao.getAllActivePenalties()
               .map { entities -> entities.map { it.toDomainModel() } }
       
       suspend fun createPenalty(penalty: Penalty): Result<Penalty> {
           return try {
               if (networkConnectivityManager.isConnected()) {
                   val response = apiService.createPenalty(penalty.toDto())
                   penaltyDao.insertPenalty(response.toEntity())
                   Result.success(response.toDomainModel())
               } else {
                   // Queue for later sync
                   penaltyDao.insertPenalty(penalty.toEntity().copy(syncStatus = SyncStatus.PENDING))
                   Result.success(penalty)
               }
           } catch (e: Exception) {
               Result.failure(e)
           }
       }
   }
   ```

4. **PenaltyViewModel**
   ```kotlin
   @HiltViewModel
   class PenaltyViewModel @Inject constructor(
       private val penaltyRepository: PenaltyRepository,
       private val getUserPenaltiesUseCase: GetUserPenaltiesUseCase,
       private val createPenaltyUseCase: CreatePenaltyUseCase
   ) : ViewModel() {
       
       private val _uiState = MutableStateFlow(PenaltyUiState())
       val uiState: StateFlow<PenaltyUiState> = _uiState.asStateFlow()
       
       private val _penalties = MutableLiveData<List<Penalty>>()
       val penalties: LiveData<List<Penalty>> = _penalties
       
       init {
           loadPenalties()
       }
       
       private fun loadPenalties() {
           viewModelScope.launch {
               _uiState.value = _uiState.value.copy(isLoading = true)
               
               getUserPenaltiesUseCase()
                   .catch { exception ->
                       _uiState.value = _uiState.value.copy(
                           isLoading = false,
                           errorMessage = exception.message
                       )
                   }
                   .collect { penaltiesList ->
                       _penalties.value = penaltiesList
                       _uiState.value = _uiState.value.copy(
                           isLoading = false,
                           errorMessage = null
                       )
                   }
           }
       }
       
       fun createPenalty(penalty: Penalty) {
           viewModelScope.launch {
               createPenaltyUseCase(penalty)
                   .onSuccess {
                       // Refresh penalties list
                       loadPenalties()
                   }
                   .onFailure { exception ->
                       _uiState.value = _uiState.value.copy(
                           errorMessage = exception.message
                       )
                   }
           }
       }
   }
   ```

### UI Implementation

1. **MainActivity**
   ```kotlin
   @AndroidEntryPoint
   class MainActivity : AppCompatActivity() {
       
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           
           enableEdgeToEdge()
           setContentView(R.layout.activity_main)
           
           setupBottomNavigation()
           setupNavigationController()
       }
       
       private fun setupBottomNavigation() {
           val navView: BottomNavigationView = findViewById(R.id.nav_view)
           val navController = findNavController(R.id.nav_host_fragment_activity_main)
           navView.setupWithNavController(navController)
       }
   }
   ```

2. **PenaltyListFragment**
   ```kotlin
   @AndroidEntryPoint
   class PenaltyListFragment : Fragment() {
       
       private var _binding: FragmentPenaltyListBinding? = null
       private val binding get() = _binding!!
       
       private val viewModel: PenaltyViewModel by viewModels()
       private lateinit var penaltyAdapter: PenaltyAdapter
       
       override fun onCreateView(
           inflater: LayoutInflater,
           container: ViewGroup?,
           savedInstanceState: Bundle?
       ): View {
           _binding = FragmentPenaltyListBinding.inflate(inflater, container, false)
           return binding.root
       }
       
       override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
           super.onViewCreated(view, savedInstanceState)
           
           setupRecyclerView()
           observeViewModel()
           setupFab()
       }
       
       private fun setupRecyclerView() {
           penaltyAdapter = PenaltyAdapter { penalty ->
               // Handle penalty item click
               findNavController().navigate(
                   PenaltyListFragmentDirections.actionToPenaltyDetail(penalty.id)
               )
           }
           
           binding.recyclerViewPenalties.apply {
               adapter = penaltyAdapter
               layoutManager = LinearLayoutManager(requireContext())
               addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
           }
       }
       
       private fun observeViewModel() {
           viewModel.penalties.observe(viewLifecycleOwner) { penalties ->
               penaltyAdapter.submitList(penalties)
           }
           
           viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
               binding.progressBar.isVisible = uiState.isLoading
               
               uiState.errorMessage?.let { message ->
                   Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
               }
           }
       }
       
       override fun onDestroyView() {
           super.onDestroyView()
           _binding = null
       }
   }
   ```

### Data Models

1. **Domain Models**
   ```kotlin
   data class Penalty(
       val id: String,
       val teamUser: TeamUser,
       val type: PenaltyType,
       val reason: String,
       val amount: Money,
       val archived: Boolean = false,
       val paidAt: Instant? = null,
       val createdAt: Instant,
       val updatedAt: Instant
   )
   
   data class PenaltyType(
       val id: String,
       val name: String,
       val description: String?,
       val type: PenaltyTypeCategory,
       val active: Boolean = true
   )
   
   enum class PenaltyTypeCategory {
       DRINK,
       LATE_ARRIVAL,
       MISSED_TRAINING,
       CUSTOM;
       
       fun getDisplayName(): String = when (this) {
           DRINK -> "Drink"
           LATE_ARRIVAL -> "Late Arrival"
           MISSED_TRAINING -> "Missed Training"
           CUSTOM -> "Custom"
       }
       
       fun isDrink(): Boolean = this == DRINK
   }
   ```

2. **Money Value Class**
   ```kotlin
   @JvmInline
   value class Money(private val cents: Int) {
       val amount: Int get() = cents
       
       fun toCurrency(currency: Currency): String {
           val amount = cents / 100.0
           return when (currency) {
               Currency.EUR -> "%.2f €".format(amount)
               Currency.USD -> "$%.2f".format(amount)
               Currency.GBP -> "£%.2f".format(amount)
           }
       }
       
       companion object {
           fun fromCents(cents: Int) = Money(cents)
           fun fromAmount(amount: Double) = Money((amount * 100).toInt())
       }
   }
   ```

### API Integration

1. **Retrofit API Service**
   ```kotlin
   interface CashboxApiService {
       
       @GET("penalties")
       suspend fun getPenalties(
           @Query("page") page: Int = 1,
           @Query("limit") limit: Int = 20,
           @Query("teamId") teamId: String? = null
       ): ApiResponse<List<PenaltyDto>>
       
       @POST("penalties")
       suspend fun createPenalty(@Body penalty: CreatePenaltyDto): PenaltyDto
       
       @PUT("penalties/{id}")
       suspend fun updatePenalty(
           @Path("id") id: String,
           @Body penalty: UpdatePenaltyDto
       ): PenaltyDto
       
       @DELETE("penalties/{id}")
       suspend fun deletePenalty(@Path("id") id: String): ResponseBody
       
       @POST("penalties/{id}/pay")
       suspend fun markPenaltyAsPaid(@Path("id") id: String): PenaltyDto
   }
   ```

2. **Network Configuration**
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   object NetworkModule {
       
       @Provides
       @Singleton
       fun provideOkHttpClient(
           authInterceptor: AuthInterceptor,
           loggingInterceptor: HttpLoggingInterceptor
       ): OkHttpClient {
           return OkHttpClient.Builder()
               .addInterceptor(authInterceptor)
               .addInterceptor(loggingInterceptor)
               .connectTimeout(30, TimeUnit.SECONDS)
               .readTimeout(30, TimeUnit.SECONDS)
               .build()
       }
       
       @Provides
       @Singleton
       fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
           return Retrofit.Builder()
               .baseUrl(BuildConfig.API_BASE_URL)
               .client(okHttpClient)
               .addConverterFactory(GsonConverterFactory.create())
               .build()
       }
       
       @Provides
       @Singleton
       fun provideCashboxApiService(retrofit: Retrofit): CashboxApiService {
           return retrofit.create(CashboxApiService::class.java)
       }
   }
   ```

## Implementation Plan

### Phase 1: Project Setup (Week 1)

1. Initialize Android project with latest SDK
2. Configure build.gradle with necessary dependencies
3. Set up Dagger Hilt for dependency injection
4. Configure Retrofit for API communication
5. Set up Room database
6. Implement basic navigation structure
7. Configure logging and crash reporting

### Phase 2: Core Data Layer (Week 2)

1. Implement Room entities and DAOs
2. Create repository interfaces and implementations
3. Set up API service interfaces
4. Implement data synchronization logic
5. Create domain models and mappers
6. Set up offline-first architecture

### Phase 3: Authentication Flow (Week 3)

1. Design login and registration UI
2. Implement JWT token management
3. Set up secure storage for credentials
4. Configure biometric authentication
5. Implement auto-logout functionality
6. Create user session management

### Phase 4: Core UI Implementation (Week 4)

1. Implement penalty list and detail screens
2. Create penalty creation flow
3. Implement team selection UI
4. Create user profile management
5. Set up navigation between screens
6. Implement Material Design 3 theming

### Phase 5: Advanced Features (Week 5)

1. Implement data import/export functionality
2. Add search and filtering capabilities
3. Create basic reporting views
4. Implement push notifications
5. Add offline data synchronization
6. Create settings and preferences

### Phase 6: Testing and Polish (Week 6)

1. Write unit and integration tests
2. Perform UI/UX testing
3. Test offline functionality
4. Optimize performance
5. Add accessibility features
6. Prepare for release

## Dependencies

### Core Android Dependencies
```kotlin
// build.gradle (Module: app)
dependencies {
    // Android Core
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
    implementation "androidx.activity:activity-compose:1.8.2"
    
    // Architecture Components
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    implementation "androidx.navigation:navigation-fragment-ktx:2.7.6"
    implementation "androidx.navigation:navigation-ui-ktx:2.7.6"
    
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
    
    // UI
    implementation "com.google.android.material:material:1.11.0"
    implementation "androidx.constraintlayout:constraintlayout:2.1.4"
    implementation "androidx.recyclerview:recyclerview:1.3.2"
    
    // Security
    implementation "androidx.security:security-crypto:1.1.0-alpha06"
    implementation "androidx.biometric:biometric:1.1.0"
    
    // Image Loading
    implementation "com.github.bumptech.glide:glide:4.16.0"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.mockito:mockito-core:5.8.0"
    testImplementation "androidx.arch.core:core-testing:2.2.0"
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
    
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
    androidTestImplementation "androidx.test:runner:1.5.2"
    androidTestImplementation "androidx.test:rules:1.5.0"
}
```

## Testing Strategy

1. **Unit Testing**
   - Test ViewModels with MockK
   - Test Repository implementations
   - Test Use Cases
   - Test data mappers and transformations

2. **Integration Testing**
   - Test Room database operations
   - Test API service implementations
   - Test data synchronization logic

3. **UI Testing**
   - Test user flows with Espresso
   - Test fragment navigation
   - Test RecyclerView interactions
   - Test form validation

4. **Performance Testing**
   - Profile memory usage
   - Test database query performance
   - Monitor network request efficiency

## Acceptance Criteria

- All core penalty management features work offline
- Authentication flow is secure and user-friendly
- UI follows Material Design 3 guidelines
- App handles network connectivity changes gracefully
- Data synchronization works reliably
- All tests pass successfully
- App is accessible and supports different screen sizes

## Risks and Mitigation

1. **Risk**: Complex offline synchronization logic
   **Mitigation**: Implement robust conflict resolution and error handling

2. **Risk**: Performance issues with large datasets
   **Mitigation**: Implement pagination and lazy loading

3. **Risk**: Security vulnerabilities in local storage
   **Mitigation**: Use EncryptedSharedPreferences and proper security practices

4. **Risk**: Network reliability issues
   **Mitigation**: Implement retry mechanisms and offline-first architecture

## Post-Release Activities

1. Monitor crash reports and performance metrics
2. Collect user feedback through in-app surveys
3. Analyze usage patterns and optimize UX
4. Plan for version 1.1.0 with enhanced features
5. Conduct security audit of authentication flow

## Documentation

- Android development setup guide
- API integration documentation
- UI/UX design system documentation
- Testing strategy documentation
- Security implementation guide