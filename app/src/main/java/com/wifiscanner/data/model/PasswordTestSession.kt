package com.wifiscanner.data.model

data class PasswordTestSession(
    val targetWord: String,
    val wordlist: List<String>,
    val attempts: Int,
    val elapsedMillis: Long,
    val matchesFound: Int,
    val wordsPerMinute: Double,
    val matchedWord: String?
)
