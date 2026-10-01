package com.wifiscanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifiscanner.data.model.NetworkInfo
import com.wifiscanner.data.model.PasswordStrength
import com.wifiscanner.data.model.SecurityReport
import com.wifiscanner.data.repository.WifiRepository

@Composable
fun WifiScannerApp() {
    val repository = remember { WifiRepository() }
    var darkMode by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(BottomNavItem.Scanner) }
    var nearbyNetworks by remember { mutableStateOf(repository.getNearbyNetworks()) }
    var selectedNetwork by remember { mutableStateOf<NetworkInfo?>(null) }
    var securityReport by remember { mutableStateOf<SecurityReport?>(null) }
    var passwordText by remember { mutableStateOf("P@ssw0rd!") }
    var passwordStrength by remember { mutableStateOf(repository.evaluatePassword(passwordText)) }
    var targetWord by remember { mutableStateOf("admin") }
    var wordlistSample by remember { mutableStateOf(repository.createWordlistSample(5000)) }
    var attempts by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf("0.00s") }
    var matchedWord by remember { mutableStateOf<String?>(null) }
    var wordsPerMinute by remember { mutableStateOf("0") }
    var totalWordCount by remember { mutableStateOf(5000) }
    var testHistory by remember {
        mutableStateOf(listOf("WPA2 Audit Started", "Password Strength Reviewed", "Security Report Created"))
    }

    val currentTheme = if (darkMode) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()

    androidx.compose.material3.MaterialTheme(
        colorScheme = currentTheme,
        content = {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        BottomNavItem.entries.forEach { item ->
                            NavigationBarItem(
                                selected = currentTab == item,
                                onClick = { currentTab = item },
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                ) {
                    when (currentTab) {
                        BottomNavItem.Scanner -> ScannerScreen(
                            networks = nearbyNetworks,
                            onRefresh = {
                                nearbyNetworks = repository.getNearbyNetworks()
                                selectedNetwork = nearbyNetworks.firstOrNull()
                                securityReport = selectedNetwork?.let { repository.analyzeSecurity(it) }
                                testHistory = listOf("Scan Refreshed", *testHistory.toTypedArray())
                            },
                            onSelectNetwork = { network ->
                                selectedNetwork = network
                                securityReport = repository.analyzeSecurity(network)
                            }
                        )

                        BottomNavItem.Security -> SecurityScreen(
                            network = selectedNetwork,
                            report = securityReport,
                            onRunSecurityTest = {
                                if (selectedNetwork != null) {
                                    securityReport = repository.analyzeSecurity(selectedNetwork!!)
                                    testHistory = listOf("Security Test Completed", *testHistory.toTypedArray())
                                }
                            }
                        )

                        BottomNavItem.Tools -> ToolsScreen(
                            password = passwordText,
                            onPasswordChange = {
                                passwordText = it
                                passwordStrength = repository.evaluatePassword(it)
                            },
                            onGenerate = {
                                passwordText = repository.generatePassword(length = 18)
                                passwordStrength = repository.evaluatePassword(passwordText)
                            },
                            passwordStrength = passwordStrength,
                            targetWord = targetWord,
                            onTargetWordChange = { targetWord = it },
                            attempts = attempts,
                            elapsedTime = elapsedTime,
                            wordsPerMinute = wordsPerMinute,
                            matchedWord = matchedWord,
                            totalWordCount = totalWordCount,
                            onRunWordTest = {
                                val session = repository.runPasswordWordTest(targetWord, wordlistSample, 20000)
                                attempts = session.attempts
                                elapsedTime = "${String.format("%.2f", session.elapsedMillis / 1000.0)}s"
                                wordsPerMinute = String.format("%.0f", session.wordsPerMinute)
                                matchedWord = session.matchedWord
                                totalWordCount = wordlistSample.size
                                testHistory = listOf("WordList Test Ran", *testHistory.toTypedArray())
                            },
                            onLoadWordlist = {
                                wordlistSample = repository.createWordlistSample(5000)
                                totalWordCount = wordlistSample.size
                            }
                        )

                        BottomNavItem.History -> HistoryScreen(testHistory)
                        BottomNavItem.Settings -> SettingsScreen(
                            darkMode = darkMode,
                            onDarkModeChange = { darkMode = it },
                            selectedLanguage = "العربية",
                            onLanguageChange = { }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ScannerScreen(
    networks: List<NetworkInfo>,
    onRefresh: () -> Unit,
    onSelectNetwork: (NetworkInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scan Networks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = onRefresh) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Refresh")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Available networks", style = MaterialTheme.typography.titleMedium)
                Divider()
                networks.forEach { network ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectNetwork(network) }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(network.ssid, fontWeight = FontWeight.SemiBold)
                            Text("${network.securityType} • ${network.frequency}")
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Icon(Icons.Default.SignalCellularAlt, contentDescription = null)
                            Text("${network.signalStrength} dBm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityScreen(
    network: NetworkInfo?,
    report: SecurityReport?,
    onRunSecurityTest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Security Test", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        if (network == null) {
            Text("Select a network first to run the analysis.")
            return
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(network.ssid, style = MaterialTheme.typography.titleLarge)
                Text("Security Type: ${network.securityType}")
                Text("Encryption: ${network.encryptionType}")
                Text("Channel: ${network.channel} • Frequency: ${network.frequency}")
                Text("Signal: ${network.signalStrength} dBm")
                Button(onClick = onRunSecurityTest) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Run Security Test")
                }
            }
        }

        if (report != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Risk Level: ${report.riskLevel}", fontWeight = FontWeight.Bold)
                    report.issues.forEach { issue ->
                        Text("• $issue")
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Recommendations", fontWeight = FontWeight.SemiBold)
                    report.recommendations.forEach { rec ->
                        Text("• $rec")
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolsScreen(
    password: String,
    onPasswordChange: (String) -> Unit,
    onGenerate: () -> Unit,
    passwordStrength: PasswordStrength,
    targetWord: String,
    onTargetWordChange: (String) -> Unit,
    attempts: Int,
    elapsedTime: String,
    wordsPerMinute: String,
    matchedWord: String?,
    totalWordCount: Int,
    onRunWordTest: () -> Unit,
    onLoadWordlist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Password Tools", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Password Generator")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.size(8.dp))
                    Button(onClick = onGenerate) {
                        Text("Generate")
                    }
                }
                Text("Strength: ${passwordStrength.label}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Password Lab")
                OutlinedTextField(
                    value = targetWord,
                    onValueChange = onTargetWordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onRunWordTest) {
                        Text("Test 1000+ Words")
                    }
                    Button(onClick = onLoadWordlist) {
                        Text("Load List")
                    }
                }
                Text("Attempts: $attempts")
                Text("Time: $elapsedTime")
                Text("Rate: $wordsPerMinute words/min")
                Text("Wordlist Size: $totalWordCount")
                matchedWord?.let { Text("Matched: $it") } ?: Text("No match found in current test list.")
            }
        }
    }
}

@Composable
private fun HistoryScreen(testHistory: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Test History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(testHistory) { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    selectedLanguage: String,
    onLanguageChange: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode")
                    Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language")
                    OutlinedButton(onClick = onLanguageChange) {
                        Icon(Icons.Default.Language, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text(selectedLanguage)
                    }
                }
            }
        }
    }
}

enum class BottomNavItem(val title: String, val icon: ImageVector) {
    Scanner("Scanner", Icons.Default.NetworkWifi),
    Security("Security", Icons.Default.Shield),
    Tools("Tools", Icons.Default.Password),
    History("History", Icons.Default.History),
    Settings("Settings", Icons.Default.Settings)
}

val BottomNavItem.Companion.entries: List<BottomNavItem>
    get() = listOf(BottomNavItem.Scanner, BottomNavItem.Security, BottomNavItem.Tools, BottomNavItem.History, BottomNavItem.Settings)
