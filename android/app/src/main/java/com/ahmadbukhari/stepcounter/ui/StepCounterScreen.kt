package com.ahmadbukhari.stepcounter.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmadbukhari.stepcounter.R

@Composable
fun StepCounterScreen(viewModel: StepsViewModel = viewModel()) {
    val context = LocalContext.current
    val requestPermissions = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted -> viewModel.onPermissionResult(granted) }

    // Check access and refresh every time the app comes to the foreground.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshIfConnected()
        onPauseOrDispose { }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        when (viewModel.access) {
            StepsViewModel.Access.CHECKING -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            StepsViewModel.Access.CONNECTED -> DashboardScreen(viewModel)
            StepsViewModel.Access.NOT_CONNECTED -> IntroScreen(
                title = "Your steps at a glance",
                body = "Step Counter reads your daily step count from Health Connect and shows it here and in a " +
                    "widget on your Home Screen. It doesn't send your data anywhere.",
                buttonTitle = "Connect Health Connect",
                onButton = { requestPermissions.launch(viewModel.permissionsToRequest()) },
                note = if (viewModel.permissionDenied) {
                    "Step Counter needs permission to read your steps. If the request doesn't appear, allow it in Health Connect."
                } else {
                    null
                },
                secondaryTitle = if (viewModel.permissionDenied) "Open Health Connect" else null,
                onSecondary = { openHealthConnectSettings(context) },
            )
            StepsViewModel.Access.NEEDS_INSTALL -> IntroScreen(
                title = "Get Health Connect",
                body = "On Android 13 and earlier, Step Counter needs the Health Connect app from Google Play. " +
                    "It's free and keeps your health data on your phone.",
                buttonTitle = "Get Health Connect",
                onButton = { openHealthConnectInPlayStore(context) },
            )
            StepsViewModel.Access.UNAVAILABLE -> IntroScreen(
                title = "Health Connect isn't available",
                body = "Step Counter reads your steps from Health Connect, which this device doesn't support.",
            )
        }
    }
}

@Composable
private fun IntroScreen(
    title: String,
    body: String,
    buttonTitle: String? = null,
    onButton: () -> Unit = {},
    note: String? = null,
    secondaryTitle: String? = null,
    onSecondary: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.intro_badge),
                contentDescription = null,
                modifier = Modifier.size(128.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            if (buttonTitle != null) {
                Button(onClick = onButton, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(buttonTitle) }
            }
            if (note != null) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            if (secondaryTitle != null) {
                OutlinedButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) { Text(secondaryTitle) }
            }
        }
    }
}

fun openHealthConnectSettings(context: Context) {
    context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS))
}

private fun openHealthConnectInPlayStore(context: Context) {
    val packageName = "com.google.android.apps.healthdata"
    val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName&url=healthconnect%3A%2F%2Fonboarding".toUri())
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
    }
}
