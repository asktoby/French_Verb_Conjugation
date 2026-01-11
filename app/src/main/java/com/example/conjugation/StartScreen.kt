package com.example.conjugation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun StartScreen(onStartClick: () -> Unit, statsRepository: StatsRepository) {
    val results = statsRepository.getAllResults()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("French Conjugation Challenge", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        if (results.isNotEmpty()) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                factory = { context ->
                    LineChart(context).apply {
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
    }
}
