package com.novatech.terratech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.novatech.terratech.core.presentation.ui.TerraTechApp
import com.novatech.terratech.ui.theme.TerraTechTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TerraTechTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    TerraTechApp()
                }
            }
        }
    }
}
