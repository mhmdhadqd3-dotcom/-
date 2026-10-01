package com.wifiscanner.data.model

data class NetworkInfo(
    val ssid: String,
    val signalStrength: Int,
    val securityType: String,
    val frequency: String,
    val channel: Int,
    val encryptionType: String,
    val isLegacySecurity: Boolean,
    val notes: String = "Android provides standard metadata for monitoring and evaluation."
)

data class SecurityReport(
    val riskLevel: String,
    val issues: List<String>,
    val recommendations: List<String>
)

enum class PasswordStrength(val label: String) {
    WEAK("ضعيف"),
    MEDIUM("متوسط"),
    STRONG("قوي"),
    VERY_STRONG("قوي جدًا")
}
