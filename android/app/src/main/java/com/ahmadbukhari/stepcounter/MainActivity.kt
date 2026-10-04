package com.ahmadbukhari.stepcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.ahmadbukhari.stepcounter.ui.StepCounterScreen
import com.ahmadbukhari.stepcounter.ui.theme.StepCounterTheme
import com.ahmadbukhari.stepcounter.widget.WidgetPreviews
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch { WidgetPreviews.publishIfNeeded(this@MainActivity) }
        setContent {
            StepCounterTheme {
                StepCounterScreen()
            }
        }
    }
}
