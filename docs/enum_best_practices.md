# Kotlin Enums Best Practices (Android)

This document outlines the best practices for using Kotlin enums in the Cashbox Android project.

## Introduction

Kotlin enums provide a powerful way to define a set of named constants while adding behavior and type safety. In Android development, enums are particularly useful for managing UI states, network responses, user preferences, and business logic. This document provides guidelines for effectively using enums in our Android application.

## Types of Kotlin Enums

### Basic Enums

Simple enumerations without associated values.

```kotlin
enum class UiState {
    LOADING,
    SUCCESS,
    ERROR,
    EMPTY
}
```

### Enums with Properties

Enums can have properties and custom constructors.

```kotlin
enum class PenaltyType(
    val displayName: String,
    val iconRes: Int,
    val colorRes: Int
) {
    DRINK("Drink", R.drawable.ic_drink, R.color.penalty_drink),
    LATE_ARRIVAL("Late Arrival", R.drawable.ic_clock, R.color.penalty_late),
    MISSED_TRAINING("Missed Training", R.drawable.ic_training, R.color.penalty_training),
    CUSTOM("Custom", R.drawable.ic_custom, R.color.penalty_custom);
    
    @ColorRes
    fun getColorResource(): Int = colorRes
    
    @DrawableRes
    fun getIconResource(): Int = iconRes
}
```

### Enums with Methods

Enums can have methods and implement interfaces.

```kotlin
enum class Currency(
    val code: String,
    val symbol: String,
    val decimalPlaces: Int = 2
) {
    EUR("EUR", "€"),
    USD("USD", "$"),
    GBP("GBP", "£"),
    JPY("JPY", "¥", 0);
    
    fun formatAmount(amount: Double): String {
        return when (this) {
            EUR -> "${String.format("%.${decimalPlaces}f", amount)} $symbol"
            USD, GBP -> "$symbol${String.format("%.${decimalPlaces}f", amount)}"
            JPY -> "$symbol${amount.toInt()}"
        }
    }
    
    fun toCents(amount: Double): Long {
        return (amount * kotlin.math.pow(10.0, decimalPlaces.toDouble())).toLong()
    }
    
    fun fromCents(cents: Long): Double {
        return cents / kotlin.math.pow(10.0, decimalPlaces.toDouble())
    }
}
```

## Android-Specific Enum Usage

### UI State Management

Use sealed classes combined with enums for complex UI state management:

```kotlin
sealed class PenaltyListUiState {
    object Loading : PenaltyListUiState()
    object Empty : PenaltyListUiState()
    data class Success(
        val penalties: List<Penalty>,
        val filterState: FilterState = FilterState.ALL
    ) : PenaltyListUiState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.GENERIC
    ) : PenaltyListUiState()
}

enum class FilterState(val displayName: String) {
    ALL("All Penalties"),
    PAID("Paid"),
    UNPAID("Unpaid"),
    OVERDUE("Overdue");
    
    companion object {
        fun fromDisplayName(name: String): FilterState? {
            return values().find { it.displayName == name }
        }
    }
}

enum class ErrorType {
    NETWORK,
    AUTHENTICATION,
    VALIDATION,
    GENERIC;
    
    fun shouldShowRetry(): Boolean = when (this) {
        NETWORK, GENERIC -> true
        AUTHENTICATION, VALIDATION -> false
    }
}
```

### Network Response Handling

```kotlin
enum class ApiResult<out T> {
    LOADING,
    SUCCESS,
    ERROR;
    
    companion object {
        fun <T> loading(): ApiResult<T> = LOADING
        fun <T> success(data: T): ApiResult<T> = SUCCESS
        fun <T> error(exception: Throwable): ApiResult<T> = ERROR
    }
}

// Better approach using sealed classes with enums
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val exception: Throwable,
        val type: ErrorType = ErrorType.GENERIC
    ) : NetworkResult<Nothing>()
}

enum class NetworkErrorType {
    TIMEOUT,
    NO_INTERNET,
    SERVER_ERROR,
    AUTHENTICATION_FAILED,
    RATE_LIMITED,
    UNKNOWN;
    
    @StringRes
    fun getErrorMessageRes(): Int = when (this) {
        TIMEOUT -> R.string.error_network_timeout
        NO_INTERNET -> R.string.error_no_internet
        SERVER_ERROR -> R.string.error_server
        AUTHENTICATION_FAILED -> R.string.error_auth_failed
        RATE_LIMITED -> R.string.error_rate_limited
        UNKNOWN -> R.string.error_unknown
    }
}
```

### Preferences and Settings

```kotlin
enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");
    
    companion object {
        fun fromValue(value: String): ThemeMode {
            return values().find { it.value == value } ?: SYSTEM
        }
    }
}

enum class NotificationFrequency(
    val value: String,
    val displayName: String,
    val intervalMs: Long
) {
    IMMEDIATE("immediate", "Immediate", 0L),
    HOURLY("hourly", "Hourly", TimeUnit.HOURS.toMillis(1)),
    DAILY("daily", "Daily", TimeUnit.DAYS.toMillis(1)),
    WEEKLY("weekly", "Weekly", TimeUnit.DAYS.toMillis(7)),
    NEVER("never", "Never", Long.MAX_VALUE);
    
    fun isEnabled(): Boolean = this != NEVER
    
    companion object {
        fun fromValue(value: String): NotificationFrequency {
            return values().find { it.value == value } ?: DAILY
        }
    }
}
```

## Database Integration with Room

### Using Enums with Room TypeConverters

```kotlin
@Entity(tableName = "penalties")
data class PenaltyEntity(
    @PrimaryKey val id: String,
    val reason: String,
    val amount: Int,
    val currency: Currency,
    val type: PenaltyType,
    val status: PaymentStatus,
    val createdAt: Long
)

@TypeConverter
class Converters {
    @TypeConverter
    fun fromCurrency(currency: Currency): String = currency.code
    
    @TypeConverter
    fun toCurrency(code: String): Currency = Currency.values()
        .find { it.code == code } ?: Currency.EUR
    
    @TypeConverter
    fun fromPenaltyType(type: PenaltyType): String = type.name
    
    @TypeConverter
    fun toPenaltyType(name: String): PenaltyType = 
        PenaltyType.valueOf(name)
    
    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String = status.name
    
    @TypeConverter
    fun toPaymentStatus(name: String): PaymentStatus = 
        PaymentStatus.valueOf(name)
}

enum class PaymentStatus(
    val displayName: String,
    @ColorRes val colorRes: Int
) {
    PENDING("Pending", R.color.status_pending),
    PROCESSING("Processing", R.color.status_processing),
    COMPLETED("Completed", R.color.status_completed),
    FAILED("Failed", R.color.status_failed),
    CANCELLED("Cancelled", R.color.status_cancelled);
    
    fun isActive(): Boolean = when (this) {
        PENDING, PROCESSING -> true
        COMPLETED, FAILED, CANCELLED -> false
    }
}
```

## Jetpack Compose Integration

### Using Enums in Composable Functions

```kotlin
@Composable
fun PenaltyStatusChip(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val chipColors = when (status) {
        PaymentStatus.PENDING -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
        PaymentStatus.PROCESSING -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
        PaymentStatus.COMPLETED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
        PaymentStatus.FAILED, PaymentStatus.CANCELLED -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    }
    
    AssistChip(
        onClick = { /* Handle click */ },
        label = { Text(status.displayName) },
        leadingIcon = {
            Icon(
                imageVector = status.getIcon(),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = chipColors,
        modifier = modifier
    )
}

// Extension function for enum
fun PaymentStatus.getIcon(): ImageVector = when (this) {
    PaymentStatus.PENDING -> Icons.Default.Schedule
    PaymentStatus.PROCESSING -> Icons.Default.Sync
    PaymentStatus.COMPLETED -> Icons.Default.CheckCircle
    PaymentStatus.FAILED -> Icons.Default.Error
    PaymentStatus.CANCELLED -> Icons.Default.Cancel
}
```

### State Management with Enums

```kotlin
@Composable
fun PenaltyListScreen(
    viewModel: PenaltyListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    
    Column {
        FilterTabs(
            selectedFilter = filterState,
            onFilterChanged = viewModel::setFilter
        )
        
        when (uiState) {
            is PenaltyListUiState.Loading -> {
                LoadingIndicator()
            }
            is PenaltyListUiState.Success -> {
                PenaltyList(
                    penalties = uiState.penalties,
                    onPenaltyClick = viewModel::onPenaltyClick
                )
            }
            is PenaltyListUiState.Error -> {
                ErrorMessage(
                    message = uiState.message,
                    canRetry = uiState.type.shouldShowRetry(),
                    onRetry = viewModel::retryLoading
                )
            }
            is PenaltyListUiState.Empty -> {
                EmptyState(
                    message = getEmptyMessage(filterState)
                )
            }
        }
    }
}

@Composable
fun FilterTabs(
    selectedFilter: FilterState,
    onFilterChanged: (FilterState) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = FilterState.values().indexOf(selectedFilter),
        modifier = modifier
    ) {
        FilterState.values().forEach { filter ->
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterChanged(filter) },
                text = { Text(filter.displayName) }
            )
        }
    }
}
```

## ViewModel Integration

### Using Enums in ViewModels

```kotlin
@HiltViewModel
class PenaltyListViewModel @Inject constructor(
    private val penaltyRepository: PenaltyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PenaltyListUiState>(PenaltyListUiState.Loading)
    val uiState: StateFlow<PenaltyListUiState> = _uiState.asStateFlow()
    
    private val _filterState = MutableStateFlow(FilterState.ALL)
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    init {
        loadPenalties()
    }
    
    fun setFilter(filter: FilterState) {
        _filterState.value = filter
        loadPenalties()
    }
    
    private fun loadPenalties() {
        viewModelScope.launch {
            _uiState.value = PenaltyListUiState.Loading
            
            try {
                val penalties = when (_filterState.value) {
                    FilterState.ALL -> penaltyRepository.getAllPenalties()
                    FilterState.PAID -> penaltyRepository.getPaidPenalties()
                    FilterState.UNPAID -> penaltyRepository.getUnpaidPenalties()
                    FilterState.OVERDUE -> penaltyRepository.getOverduePenalties()
                }
                
                _uiState.value = if (penalties.isEmpty()) {
                    PenaltyListUiState.Empty
                } else {
                    PenaltyListUiState.Success(penalties, _filterState.value)
                }
            } catch (e: Exception) {
                _uiState.value = PenaltyListUiState.Error(
                    message = e.message ?: "Unknown error",
                    type = determineErrorType(e)
                )
            }
        }
    }
    
    private fun determineErrorType(exception: Exception): ErrorType {
        return when (exception) {
            is UnknownHostException, is ConnectException -> ErrorType.NETWORK
            is HttpException -> when (exception.code()) {
                401, 403 -> ErrorType.AUTHENTICATION
                400, 422 -> ErrorType.VALIDATION
                else -> ErrorType.GENERIC
            }
            else -> ErrorType.GENERIC
        }
    }
}
```

## Serialization with Gson/Moshi

### Custom Serialization for API Communication

```kotlin
// Gson TypeAdapter for enums
class PenaltyTypeAdapter : TypeAdapter<PenaltyType>() {
    override fun write(out: JsonWriter, value: PenaltyType?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name.lowercase())
        }
    }
    
    override fun read(reader: JsonReader): PenaltyType? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        
        val value = reader.nextString()
        return PenaltyType.values().find { 
            it.name.equals(value, ignoreCase = true) 
        }
    }
}

// Register the adapter
val gson = GsonBuilder()
    .registerTypeAdapter(PenaltyType::class.java, PenaltyTypeAdapter())
    .create()

// Or using Moshi
@JsonClass(generateAdapter = true)
data class PenaltyDto(
    val id: String,
    val reason: String,
    val amount: Int,
    @Json(name = "penalty_type") val type: PenaltyType,
    val status: PaymentStatus
)
```

## Testing Enums

```kotlin
class PenaltyTypeTest {
    
    @Test
    fun `penalty type should return correct display name`() {
        assertEquals("Drink", PenaltyType.DRINK.displayName)
        assertEquals("Late Arrival", PenaltyType.LATE_ARRIVAL.displayName)
        assertEquals("Missed Training", PenaltyType.MISSED_TRAINING.displayName)
        assertEquals("Custom", PenaltyType.CUSTOM.displayName)
    }
    
    @Test
    fun `penalty type should return correct icon resource`() {
        assertEquals(R.drawable.ic_drink, PenaltyType.DRINK.iconRes)
        assertEquals(R.drawable.ic_clock, PenaltyType.LATE_ARRIVAL.iconRes)
    }
    
    @Test
    fun `penalty type should return correct color resource`() {
        assertEquals(R.color.penalty_drink, PenaltyType.DRINK.colorRes)
        assertEquals(R.color.penalty_late, PenaltyType.LATE_ARRIVAL.colorRes)
    }
}

class CurrencyTest {
    
    @Test
    fun `currency should format amount correctly`() {
        assertEquals("10.50 €", Currency.EUR.formatAmount(10.50))
        assertEquals("$10.50", Currency.USD.formatAmount(10.50))
        assertEquals("£10.50", Currency.GBP.formatAmount(10.50))
        assertEquals("¥1050", Currency.JPY.formatAmount(1050.0))
    }
    
    @Test
    fun `currency should convert to cents correctly`() {
        assertEquals(1050L, Currency.EUR.toCents(10.50))
        assertEquals(1050L, Currency.USD.toCents(10.50))
        assertEquals(1050L, Currency.JPY.toCents(1050.0))
    }
    
    @Test
    fun `currency should convert from cents correctly`() {
        assertEquals(10.50, Currency.EUR.fromCents(1050L), 0.001)
        assertEquals(10.50, Currency.USD.fromCents(1050L), 0.001)
        assertEquals(1050.0, Currency.JPY.fromCents(1050L), 0.001)
    }
}
```

## Performance Considerations

### Efficient Enum Usage

```kotlin
// Good: Use companion object for frequently accessed collections
enum class PenaltyType(val displayName: String) {
    DRINK("Drink"),
    LATE_ARRIVAL("Late Arrival"),
    MISSED_TRAINING("Missed Training"),
    CUSTOM("Custom");
    
    companion object {
        // Cache the values array to avoid repeated allocation
        private val VALUES = values()
        
        fun fromDisplayName(name: String): PenaltyType? {
            // Use cached array instead of calling values() repeatedly
            return VALUES.find { it.displayName == name }
        }
        
        fun getActiveTypes(): List<PenaltyType> {
            // Cache commonly used filtered lists
            return VALUES.filter { it != CUSTOM }
        }
    }
}

// Good: Use when expressions for type-safe handling
fun handlePaymentStatus(status: PaymentStatus): String {
    return when (status) {
        PaymentStatus.PENDING -> "Payment is being processed"
        PaymentStatus.PROCESSING -> "Payment is in progress"
        PaymentStatus.COMPLETED -> "Payment completed successfully"
        PaymentStatus.FAILED -> "Payment failed"
        PaymentStatus.CANCELLED -> "Payment was cancelled"
        // Compiler ensures all cases are handled
    }
}
```

### Memory Optimization

```kotlin
// Good: Use object declarations for stateless enum methods
enum class NotificationChannel(val channelId: String) {
    PENALTIES("penalties"),
    PAYMENTS("payments"),
    REMINDERS("reminders");
    
    companion object {
        // Use object for expensive operations
        val channelManager = object {
            fun createAllChannels(context: Context) {
                values().forEach { channel ->
                    channel.createChannel(context)
                }
            }
        }
    }
    
    private fun createChannel(context: Context) {
        // Implementation
    }
}
```

## Best Practices Summary

1. **Use Descriptive Names**: Enum values should be self-explanatory
2. **Add Properties and Methods**: Leverage enum capabilities for rich behavior
3. **Implement Interfaces**: Make enums implement common interfaces when needed
4. **Use Companion Objects**: Store constants and helper methods efficiently
5. **Type-Safe with When**: Always use `when` expressions for exhaustive handling
6. **Cache Collections**: Avoid repeated `values()` calls in performance-critical code
7. **Room Integration**: Use TypeConverters for database storage
8. **Compose Integration**: Create extension functions for UI-related behavior
9. **Testing**: Write comprehensive tests for enum behavior
10. **Documentation**: Document complex enum logic and usage patterns

## Android-Specific Considerations

### Resource Integration

```kotlin
enum class AppTheme(
    @StyleRes val themeRes: Int,
    val isDark: Boolean
) {
    LIGHT(R.style.Theme_Cashbox_Light, false),
    DARK(R.style.Theme_Cashbox_Dark, true),
    BLACK(R.style.Theme_Cashbox_Black, true);
    
    fun applyTheme(activity: Activity) {
        activity.setTheme(themeRes)
    }
}
```

### Permission Handling

```kotlin
enum class AppPermission(
    val permission: String,
    @StringRes val rationaleRes: Int
) {
    CAMERA(Manifest.permission.CAMERA, R.string.permission_camera_rationale),
    LOCATION(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_location_rationale),
    STORAGE(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_storage_rationale);
    
    fun isGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == 
            PackageManager.PERMISSION_GRANTED
    }
    
    fun getRationale(context: Context): String {
        return context.getString(rationaleRes)
    }
}
```

## Conclusion

Kotlin enums are powerful tools for creating type-safe, maintainable Android applications. By following these best practices, we can leverage enums effectively for state management, UI logic, data persistence, and business rules while maintaining high code quality and performance.