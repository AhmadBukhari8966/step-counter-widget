package com.ahmadbukhari.stepcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmadbukhari.stepcounter.ui.theme.StepCounterTheme

/**
 * Health Connect shows this when someone asks why Step Counter wants their data.
 * Apps must provide it, or Health Connect won't show the permission request.
 */
class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StepCounterTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .safeDrawingPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text("How Step Counter uses your data", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Step Counter reads your step count from Health Connect to show today's steps, your " +
                                "last 7 days and your progress toward a daily goal, in the app and in its widget.",
                        )
                        Text(
                            "It only reads steps. It doesn't write to Health Connect, doesn't use the internet and " +
                                "doesn't collect analytics. Your daily totals are stored on this phone so the widget " +
                                "can show them, and they're deleted when you uninstall the app.",
                        )
                        Text(
                            "You can stop sharing at any time in Health Connect, under App permissions › Step Counter.",
                        )
                        Button(onClick = ::finish) { Text("Done") }
                    }
                }
            }
        }
    }
}
