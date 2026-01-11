package com.example.conjugation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.conjugation.ui.theme.ConjugationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConjugationTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ConjugationApp()
                }
            }
        }
    }
}

@Composable
fun ConjugationApp() {
    val navController = rememberNavController()
    val statsRepository = StatsRepository(navController.context)

    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(
                onStartClick = { navController.navigate("game") },
                statsRepository = statsRepository
            )
        }
        composable("game") {
            GameScreen(statsRepository = statsRepository) { navController.popBackStack() }
        }
    }
}
