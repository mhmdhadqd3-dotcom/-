package com.wifiscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.wifiscanner.ui.WifiScannerApp
import com.wifiscanner.ui.theme.WifiScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WifiScannerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WifiScannerApp()
                }
            }
        }
    }
}
