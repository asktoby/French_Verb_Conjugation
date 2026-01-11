package com.example.conjugation

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Calendar

@Composable
fun StartScreen(onStartClick: () -> Unit, statsRepository: StatsRepository) {
    val results = statsRepository.getAllResults()
    val streak = statsRepository.getStreak()
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showTimePicker(context)
        } else {
            // Handle the case where the user denies the permission
        }
    }

    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onConfirm = {
                showPermissionDialog = false
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Conjugation Crush", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("🔥 Daily Streak: $streak", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(16.dp))

        if (results.isNotEmpty()) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                factory = { ctx ->
                    LineChart(ctx).apply {
                        val attemptsEntries = results.mapIndexed { index, result -> Entry(index.toFloat(), result.attempts.toFloat()) }
                        val durationEntries = results.mapIndexed { index, result -> Entry(index.toFloat(), result.durationInSeconds.toFloat()) }

                        val attemptsDataSet = LineDataSet(attemptsEntries, "Attempts").apply {
                            color = android.graphics.Color.RED
                        }
                        val durationDataSet = LineDataSet(durationEntries, "Duration (s)").apply {
                            color = android.graphics.Color.BLUE
                        }

                        data = LineData(attemptsDataSet, durationDataSet)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        invalidate()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(onClick = onStartClick) {
            Text("Start Quiz")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    showTimePicker(context)
                }
                else -> {
                    showPermissionDialog = true
                }
            }
        }) {
            Text("Set Daily Reminder")
        }
    }
}

fun showTimePicker(context: Context) {
    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            NotificationScheduler.scheduleDailyNotification(context, selectedHour, selectedMinute)
        },
        16, // 4 PM
        0,
        false
    ).show()
}

@Composable
fun PermissionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("To set a daily reminder, you need to grant the notification permission.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
