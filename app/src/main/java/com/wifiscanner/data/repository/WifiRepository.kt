package com.wifiscanner.data.repository

import com.wifiscanner.data.model.NetworkInfo
import com.wifiscanner.data.model.PasswordStrength
import com.wifiscanner.data.model.SecurityReport
import kotlin.math.max
import kotlin.random.Random

class WifiRepository {
    fun getNearbyNetworks(): List<NetworkInfo> = listOf(
        NetworkInfo(
            ssid = "Home WiFi",
            signalStrength = -52,
            securityType = "WPA2",
            frequency = "2.4 GHz",
            channel = 6,
            encryptionType = "AES",
            isLegacySecurity = false,
            notes = "شبكة منزلية، حماية مناسبة، لا توجد مؤشرات إرهابية واضحة."
        ),
        NetworkInfo(
            ssid = "CafeFreeNet",
            signalStrength = -66,
            securityType = "WPA2",
            frequency = "5 GHz",
            channel = 44,
            encryptionType = "AES",
            isLegacySecurity = false,
            notes = "شبكة عامة، يوصى بتقييد الوصول إلى الشبكات العامة مثل هذه."
        ),
        NetworkInfo(
            ssid = "OldRouter_2G",
            signalStrength = -75,
            securityType = "WEP",
            frequency = "2.4 GHz",
            channel = 11,
            encryptionType = "WEP",
            isLegacySecurity = true,
            notes = "شبكة قديمة تستخدم بروتوكول WEP، وهو غير آمن ويحتاج إلى تحديث." 
        ),
        NetworkInfo(
            ssid = "OfficeNet",
            signalStrength = -58,
            securityType = "WPA3",
            frequency = "5 GHz",
            channel = 36,
            encryptionType = "AES",
            isLegacySecurity = false,
            notes = "شبكة مهنية تستعمل أحدث معايير الأمان المناسبة." 
        )
    )

    fun analyzeSecurity(network: NetworkInfo): SecurityReport {
        val issues = mutableListOf<String>()
        if (network.isLegacySecurity) {
            issues.add("التشفير قديم ويستخدم بروتوكولًا ضعيفًا.")
        }
        if (network.securityType == "WPA" || network.securityType == "WEP") {
            issues.add("نوع الحماية يحتاج إلى تحديث إلى WPA2 أو WPA3.")
        }
        if (network.signalStrength < -70) {
            issues.add("إشارة الشبكة ضعيفة وقد تشير إلى ضعف الاتصال أو نقطة وصول غير مستقرة.")
        }
        if (issues.isEmpty()) {
            issues.add("لا توجد مشكلات حرجة في التكوين الأساسي للشبكة.")
        }

        val recommendations = mutableListOf<String>()
        recommendations.add("تحديث إعدادات الأمان إلى WPA3 إن أمكن.")
        recommendations.add("استخدام كلمة مرور قوية وطويلة ومختلفة عن الشبكات السابقة.")
        recommendations.add("إيقاف أي إعدادات قديمة أو غير مستخدمة في جهاز التوجيه.")

        val riskLevel = when {
            network.isLegacySecurity || network.securityType == "WEP" -> "High"
            network.securityType == "WPA" -> "Medium"
            else -> "Low"
        }

        return SecurityReport(riskLevel, issues, recommendations)
    }

    fun generatePassword(length: Int = 16, includeNumbers: Boolean = true, includeSymbols: Boolean = true): String {
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#%^&*()_+-=[]{};:,.<>/?"
        val source = buildString {
            append(letters)
            if (includeNumbers) append(numbers)
            if (includeSymbols) append(symbols)
        }

        val random = Random(System.currentTimeMillis())
        return (1..max(8, length)).map {
            source[random.nextInt(source.length)]
        }.joinToString("")
    }

    fun evaluatePassword(password: String): PasswordStrength {
        val score = when {
            password.length < 8 -> 1
            password.length < 12 -> 2
            password.length < 16 -> 3
            else -> 4
        }

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        val bonus = if (hasLetter && hasDigit && hasSymbol) 1 else 0
        val total = score + bonus

        return when {
            total <= 2 -> PasswordStrength.WEAK
            total == 3 -> PasswordStrength.MEDIUM
            total == 4 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }
}
