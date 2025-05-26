# Security Guidelines (Android)

This document outlines the security practices and guidelines for the Cashbox Android project.

## Overview

Security is paramount for our Android application, which handles sensitive financial data. This document provides comprehensive guidelines for implementing secure coding practices, protecting against mobile-specific vulnerabilities, and ensuring data protection across the Android ecosystem.

## Android Security Principles

### Defense in Depth

Implement multiple layers of security controls throughout the Android application:

- Application sandbox and permissions
- Network security and certificate pinning
- Data encryption at rest and in transit
- Authentication and authorization
- Input validation and output encoding
- Runtime application self-protection (RASP)
- Code obfuscation and tamper detection

### Principle of Least Privilege

- Request only necessary Android permissions
- Use scoped storage for file operations
- Implement fine-grained user permissions
- Regularly audit granted permissions
- Use temporary permissions when possible

### Security by Default

- All features should be secure in their default configuration
- Security should not rely on proper user configuration
- Disable unnecessary features and permissions
- Use secure defaults for all security-related settings

## Authentication and Authorization

### Secure Authentication Implementation

```kotlin
@Singleton
class AuthenticationManager @Inject constructor(
    private val biometricManager: BiometricManager,
    private val encryptedPreferences: EncryptedSharedPreferences,
    private val keyStoreManager: KeyStoreManager
) {
    
    suspend fun authenticateUser(
        context: Context,
        callback: AuthenticationCallback
    ) {
        when {
            biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS -> {
                authenticateWithBiometric(context, callback)
            }
            else -> {
                authenticateWithCredentials(callback)
            }
        }
    }
    
    private fun authenticateWithBiometric(
        context: Context,
        callback: AuthenticationCallback
    ) {
        val biometricPrompt = BiometricPrompt(
            context as FragmentActivity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    callback.onSuccess()
                }
                
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.onError("Biometric authentication failed: $errString")
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onError("Biometric authentication failed")
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Use your fingerprint or face to access Cashbox")
            .setNegativeButtonText("Cancel")
            .setConfirmationRequired(true)
            .build()
            
        biometricPrompt.authenticate(promptInfo)
    }
}
```

### JWT Token Management

```kotlin
@Singleton
class TokenManager @Inject constructor(
    private val encryptedPreferences: EncryptedSharedPreferences,
    private val keyStoreManager: KeyStoreManager
) {
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
    
    fun saveTokens(accessToken: String, refreshToken: String, expiryTime: Long) {
        val encryptedAccessToken = keyStoreManager.encrypt(accessToken)
        val encryptedRefreshToken = keyStoreManager.encrypt(refreshToken)
        
        encryptedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, encryptedAccessToken)
            .putString(KEY_REFRESH_TOKEN, encryptedRefreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply()
    }
    
    fun getAccessToken(): String? {
        val encryptedToken = encryptedPreferences.getString(KEY_ACCESS_TOKEN, null)
            ?: return null
            
        return try {
            keyStoreManager.decrypt(encryptedToken)
        } catch (e: Exception) {
            clearTokens()
            null
        }
    }
    
    fun isTokenExpired(): Boolean {
        val expiryTime = encryptedPreferences.getLong(KEY_TOKEN_EXPIRY, 0L)
        return System.currentTimeMillis() >= expiryTime
    }
    
    fun clearTokens() {
        encryptedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }
}
```

### Session Management

```kotlin
@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager,
    private val userPreferences: UserPreferences
) {
    
    private val sessionTimeoutMs = TimeUnit.MINUTES.toMillis(30)
    private var lastActivityTime = System.currentTimeMillis()
    private val sessionListeners = mutableSetOf<SessionListener>()
    
    fun updateLastActivity() {
        lastActivityTime = System.currentTimeMillis()
    }
    
    fun isSessionValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastActivity = currentTime - lastActivityTime
        
        return when {
            tokenManager.isTokenExpired() -> false
            timeSinceLastActivity > sessionTimeoutMs -> false
            else -> true
        }
    }
    
    fun invalidateSession() {
        tokenManager.clearTokens()
        userPreferences.clearUserData()
        sessionListeners.forEach { it.onSessionInvalidated() }
    }
    
    interface SessionListener {
        fun onSessionInvalidated()
    }
}
```

## Data Protection

### Encrypted Data Storage

```kotlin
@Singleton
class SecureDataManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val encryptedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "secure_preferences",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    fun saveSecureData(key: String, value: String) {
        encryptedPreferences.edit()
            .putString(key, value)
            .apply()
    }
    
    fun getSecureData(key: String): String? {
        return encryptedPreferences.getString(key, null)
    }
    
    fun removeSecureData(key: String) {
        encryptedPreferences.edit()
            .remove(key)
            .apply()
    }
    
    fun clearAllSecureData() {
        encryptedPreferences.edit()
            .clear()
            .apply()
    }
}
```

### Android Keystore Integration

```kotlin
@Singleton
class KeyStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val KEYSTORE_ALIAS = "CashboxSecretKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }
    
    init {
        generateKeyIfNeeded()
    }
    
    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
                
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    fun encrypt(plainText: String): String {
        val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        val iv = cipher.iv
        
        // Combine IV and encrypted data
        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
    
    fun decrypt(encryptedData: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = combined.sliceArray(0..11) // GCM IV is 12 bytes
        val encryptedBytes = combined.sliceArray(12 until combined.size)
        
        val secretKey = keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
```

## Network Security

### Certificate Pinning Implementation

```kotlin
@Singleton
class NetworkSecurityManager @Inject constructor() {
    
    fun createSecureOkHttpClient(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add("api.cashbox.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .add("api.cashbox.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=") // Backup pin
            .build()
        
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        
        val trustManager = trustManagerFactory.trustManagers[0] as X509TrustManager
        
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .connectionSpecs(
                listOf(
                    ConnectionSpec.MODERN_TLS,
                    ConnectionSpec.COMPATIBLE_TLS
                )
            )
            .addInterceptor(createSecurityHeadersInterceptor())
            .addInterceptor(createRequestSigningInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private fun createSecurityHeadersInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val secureRequest = originalRequest.newBuilder()
                .header("X-Requested-With", "XMLHttpRequest")
                .header("X-Content-Type-Options", "nosniff")
                .header("X-Frame-Options", "DENY")
                .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .build()
            
            chain.proceed(secureRequest)
        }
    }
    
    private fun createRequestSigningInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val body = originalRequest.body
            
            val signature = if (body != null) {
                val buffer = Buffer()
                body.writeTo(buffer)
                val bodyString = buffer.readUtf8()
                generateRequestSignature(bodyString)
            } else {
                generateRequestSignature("")
            }
            
            val signedRequest = originalRequest.newBuilder()
                .header("X-Request-Signature", signature)
                .build()
                
            chain.proceed(signedRequest)
        }
    }
    
    private fun generateRequestSignature(data: String): String {
        // Implement HMAC-SHA256 signature
        val algorithm = "HmacSHA256"
        val key = getApiSecretKey()
        val mac = Mac.getInstance(algorithm)
        val secretKeySpec = SecretKeySpec(key.toByteArray(), algorithm)
        mac.init(secretKeySpec)
        
        val signature = mac.doFinal(data.toByteArray())
        return Base64.encodeToString(signature, Base64.NO_WRAP)
    }
    
    private fun getApiSecretKey(): String {
        // Retrieve from secure storage or build config
        return BuildConfig.API_SECRET_KEY
    }
}
```

### API Request Validation

```kotlin
@Singleton
class ApiRequestValidator @Inject constructor(
    private val tokenManager: TokenManager
) {
    
    fun validateRequest(request: ApiRequest): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate authentication
        if (request.requiresAuth && tokenManager.getAccessToken() == null) {
            errors.add("Authentication required")
        }
        
        // Validate input sanitization
        if (request.hasUserInput()) {
            val sanitizedInput = sanitizeInput(request.getUserInput())
            if (sanitizedInput != request.getUserInput()) {
                errors.add("Input contains potentially harmful content")
            }
        }
        
        // Validate request size
        if (request.getContentLength() > MAX_REQUEST_SIZE) {
            errors.add("Request too large")
        }
        
        // Validate rate limiting
        if (!isWithinRateLimit(request)) {
            errors.add("Rate limit exceeded")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    private fun sanitizeInput(input: String): String {
        return input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }
    
    private fun isWithinRateLimit(request: ApiRequest): Boolean {
        // Implement rate limiting logic
        return true
    }
    
    companion object {
        private const val MAX_REQUEST_SIZE = 10 * 1024 * 1024 // 10MB
    }
}
```

## Application Security

### Code Obfuscation and Protection

```kotlin
// proguard-rules.pro
-keep class com.cashbox.android.data.remote.dto.** { *; }
-keep class com.cashbox.android.data.local.entities.** { *; }

# Security: Hide internal implementation details
-keepnames class com.cashbox.android.security.**
-keep class com.cashbox.android.security.** { *; }

# Obfuscate but keep public API
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# String obfuscation
-adaptclassstrings
-obfuscationdictionary proguard-dictionary.txt
```

### Runtime Application Self-Protection (RASP)

```kotlin
@Singleton
class SecurityMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityReporter: SecurityReporter
) {
    
    fun initializeSecurity() {
        checkRootAccess()
        checkDebuggingStatus()
        checkEmulatorEnvironment()
        checkAppIntegrity()
        monitorRuntimeThreats()
    }
    
    private fun checkRootAccess(): Boolean {
        val rootIndicators = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        val isRooted = rootIndicators.any { File(it).exists() } ||
            canExecuteCommand("su") ||
            canExecuteCommand("which su")
        
        if (isRooted) {
            securityReporter.reportSecurityEvent(
                SecurityEvent.ROOT_DETECTION,
                "Device appears to be rooted"
            )
        }
        
        return isRooted
    }
    
    private fun checkDebuggingStatus(): Boolean {
        val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val isDebuggerAttached = Debug.isDebuggerConnected()
        
        if (isDebuggable || isDebuggerAttached) {
            securityReporter.reportSecurityEvent(
                SecurityEvent.DEBUG_DETECTION,
                "Debug mode detected"
            )
        }
        
        return isDebuggable || isDebuggerAttached
    }
    
    private fun checkEmulatorEnvironment(): Boolean {
        val emulatorIndicators = listOf(
            Build.FINGERPRINT.contains("generic"),
            Build.FINGERPRINT.contains("unknown"),
            Build.MODEL.contains("google_sdk"),
            Build.MODEL.contains("Emulator"),
            Build.MODEL.contains("Android SDK"),
            Build.MANUFACTURER.contains("Genymotion"),
            Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
        )
        
        val isEmulator = emulatorIndicators.any { it }
        
        if (isEmulator) {
            securityReporter.reportSecurityEvent(
                SecurityEvent.EMULATOR_DETECTION,
                "Running on emulator"
            )
        }
        
        return isEmulator
    }
    
    private fun checkAppIntegrity(): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            
            val signature = packageInfo.signatures[0]
            val signatureHash = MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
            
            val expectedHash = getExpectedSignatureHash()
            val isValid = signatureHash.contentEquals(expectedHash)
            
            if (!isValid) {
                securityReporter.reportSecurityEvent(
                    SecurityEvent.TAMPERING_DETECTION,
                    "App signature validation failed"
                )
            }
            
            isValid
        } catch (e: Exception) {
            securityReporter.reportSecurityEvent(
                SecurityEvent.INTEGRITY_CHECK_FAILED,
                "Failed to verify app integrity: ${e.message}"
            )
            false
        }
    }
    
    private fun canExecuteCommand(command: String): Boolean {
        return try {
            Runtime.getRuntime().exec(command)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getExpectedSignatureHash(): ByteArray {
        // Return the expected signature hash for your app
        return BuildConfig.SIGNATURE_HASH.decodeBase64()
    }
}
```

### Input Validation and Sanitization

```kotlin
@Singleton
class InputValidator @Inject constructor() {
    
    fun validatePenaltyInput(input: PenaltyInput): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Validate reason
        if (input.reason.isBlank()) {
            errors.add("Reason cannot be empty")
        } else if (input.reason.length > MAX_REASON_LENGTH) {
            errors.add("Reason too long")
        } else if (containsHarmfulPatterns(input.reason)) {
            errors.add("Reason contains invalid characters")
        }
        
        // Validate amount
        if (input.amount <= 0) {
            errors.add("Amount must be positive")
        } else if (input.amount > MAX_PENALTY_AMOUNT) {
            errors.add("Amount exceeds maximum limit")
        }
        
        // Validate currency
        if (!isValidCurrency(input.currency)) {
            errors.add("Invalid currency")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    fun sanitizeInput(input: String): String {
        return input
            .trim()
            .replace(Regex("[<>\"'&]"), "")
            .take(MAX_INPUT_LENGTH)
    }
    
    private fun containsHarmfulPatterns(input: String): Boolean {
        val harmfulPatterns = listOf(
            Regex("(?i)<script[^>]*>.*?</script>"),
            Regex("(?i)javascript:"),
            Regex("(?i)on\\w+\\s*="),
            Regex("(?i)<iframe[^>]*>.*?</iframe>"),
            Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]") // Control characters
        )
        
        return harmfulPatterns.any { it.containsMatchIn(input) }
    }
    
    private fun isValidCurrency(currency: String): Boolean {
        return Currency.values().any { it.code == currency }
    }
    
    companion object {
        private const val MAX_REASON_LENGTH = 500
        private const val MAX_PENALTY_AMOUNT = 1000000 // 10,000 in cents
        private const val MAX_INPUT_LENGTH = 1000
    }
}
```

## Privacy Protection

### Data Minimization

```kotlin
@Singleton
class PrivacyManager @Inject constructor(
    private val userPreferences: UserPreferences,
    private val analyticsManager: AnalyticsManager
) {
    
    fun collectMinimalData(event: AnalyticsEvent) {
        val privacySettings = userPreferences.getPrivacySettings()
        
        val filteredEvent = when (privacySettings.dataCollectionLevel) {
            DataCollectionLevel.NONE -> return
            DataCollectionLevel.MINIMAL -> event.removePersonalData()
            DataCollectionLevel.STANDARD -> event.anonymizeData()
            DataCollectionLevel.FULL -> event
        }
        
        analyticsManager.track(filteredEvent)
    }
    
    fun scheduleDataCleanup() {
        // Schedule automatic data cleanup based on retention policies
        val workRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(7, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
            
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "data_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
```

### Secure Logging

```kotlin
object SecureLogger {
    
    private const val TAG = "Cashbox"
    
    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, sanitizeLogMessage(message))
        }
    }
    
    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, sanitizeLogMessage(message))
        }
    }
    
    fun w(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, sanitizeLogMessage(message), throwable)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        // Error logs are allowed in production but sanitized
        Log.e(TAG, sanitizeLogMessage(message), throwable)
    }
    
    private fun sanitizeLogMessage(message: String): String {
        return message
            .replace(Regex("password=[^\\s]+"), "password=***")
            .replace(Regex("token=[^\\s]+"), "token=***")
            .replace(Regex("email=[^\\s]+"), "email=***")
            .replace(Regex("\\b\\d{4}[\\s-]*\\d{4}[\\s-]*\\d{4}[\\s-]*\\d{4}\\b"), "****-****-****-****")
    }
}
```

## Security Testing

### Automated Security Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class SecurityTest {
    
    @Test
    fun testDataEncryption() {
        val keyStoreManager = KeyStoreManager(ApplicationProvider.getApplicationContext())
        val plainText = "sensitive data"
        
        val encrypted = keyStoreManager.encrypt(plainText)
        assertNotEquals(plainText, encrypted)
        
        val decrypted = keyStoreManager.decrypt(encrypted)
        assertEquals(plainText, decrypted)
    }
    
    @Test
    fun testInputValidation() {
        val validator = InputValidator()
        
        // Test SQL injection attempt
        val maliciousInput = PenaltyInput(
            reason = "'; DROP TABLE penalties; --",
            amount = 100,
            currency = "EUR"
        )
        
        val result = validator.validatePenaltyInput(maliciousInput)
        assertTrue(result is ValidationResult.Error)
    }
    
    @Test
    fun testTokenExpiration() {
        val tokenManager = TokenManager(mockEncryptedPreferences(), mockKeyStoreManager())
        
        // Set expired token
        val expiredTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)
        tokenManager.saveTokens("access", "refresh", expiredTime)
        
        assertTrue(tokenManager.isTokenExpired())
    }
    
    @Test
    fun testSecureNetworkCommunication() {
        val securityManager = NetworkSecurityManager()
        val client = securityManager.createSecureOkHttpClient()
        
        // Verify certificate pinning is configured
        assertNotNull(client.certificatePinner)
        assertTrue(client.certificatePinner.pins.isNotEmpty())
    }
}
```

## Compliance and Auditing

### Security Audit Logging

```kotlin
@Singleton
class SecurityAuditLogger @Inject constructor(
    private val localDatabase: SecurityAuditDatabase,
    private val remoteLogger: RemoteSecurityLogger
) {
    
    suspend fun logSecurityEvent(event: SecurityAuditEvent) {
        val auditEntry = SecurityAuditEntry(
            id = UUID.randomUUID().toString(),
            eventType = event.type,
            description = event.description,
            severity = event.severity,
            userId = event.userId,
            timestamp = Instant.now(),
            metadata = event.metadata
        )
        
        // Store locally first
        localDatabase.insertAuditEntry(auditEntry)
        
        // Send to remote logging service
        try {
            remoteLogger.logEvent(auditEntry)
        } catch (e: Exception) {
            // Failed to send to remote, will retry later
            localDatabase.markForRetry(auditEntry.id)
        }
    }
    
    suspend fun getAuditTrail(
        userId: String? = null,
        fromTime: Instant? = null,
        toTime: Instant? = null
    ): List<SecurityAuditEntry> {
        return localDatabase.getAuditEntries(userId, fromTime, toTime)
    }
}
```

## Security Configuration

### Security Config

```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.cashbox.com</domain>
        <pin-set expiration="2025-12-31">
            <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
            <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
        </pin-set>
    </domain-config>
    
    <!-- Block cleartext traffic by default -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```

### Manifest Security Configuration

```xml
<!-- AndroidManifest.xml -->
<application
    android:name=".CashboxApplication"
    android:allowBackup="false"
    android:fullBackupContent="false"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false"
    android:extractNativeLibs="false"
    android:hardwareAccelerated="true"
    android:largeHeap="false"
    android:persistent="false"
    android:supportsRtl="true"
    android:theme="@style/Theme.Cashbox">
    
    <!-- Prevent app from being backed up -->
    <meta-data
        android:name="com.google.android.backup.api_key"
        android:value="false" />
    
    <!-- Security provider -->
    <provider
        android:name="com.google.android.gms.security.ProviderInstaller"
        android:authorities="${applicationId}.provider"
        android:exported="false" />
</application>

<!-- Minimal permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
<uses-permission android:name="android.permission.USE_FINGERPRINT" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.NFC" />

<!-- Remove dangerous permissions from release builds -->
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    tools:remove="true" />
```

## Best Practices Summary

1. **Authentication**: Implement multi-factor authentication with biometric support
2. **Data Protection**: Use Android Keystore and EncryptedSharedPreferences
3. **Network Security**: Implement certificate pinning and secure communication
4. **Input Validation**: Validate and sanitize all user inputs
5. **Code Protection**: Use ProGuard/R8 obfuscation and runtime protection
6. **Privacy**: Implement data minimization and secure logging
7. **Monitoring**: Log security events and monitor for threats
8. **Testing**: Implement comprehensive security testing
9. **Compliance**: Maintain audit trails and follow privacy regulations
10. **Updates**: Keep security libraries and dependencies updated

## Conclusion

Security is an ongoing process that requires continuous attention and improvement. By following these guidelines and implementing the provided security measures, we can create a robust and secure Android application that protects user data and maintains the trust of our users. Regular security audits, penetration testing, and staying updated with the latest security threats are essential for maintaining a secure application.