package com.example.conjugation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class ConjugationQuestion(val pronoun: String, val answer: String)

val jouerConjugations = listOf(
    ConjugationQuestion("je", "joue"),
    ConjugationQuestion("tu", "joues"),
    ConjugationQuestion("il/elle", "joue"),
    ConjugationQuestion("nous", "jouons"),
    ConjugationQuestion("vous", "jouez"),
    ConjugationQuestion("ils/elles", "jouent")
)

@Composable
fun GameScreen() {
    var questions by remember { mutableStateOf(jouerConjugations.shuffled()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var correctAnswersInARow by remember { mutableStateOf(0) }
    var gameWon by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentQuestion = questions[currentQuestionIndex]
    val options = remember(currentQuestionIndex, questions) {
        val incorrectAnswers = jouerConjugations.map { it.answer }.filter { it != currentQuestion.answer }.distinct()
        (incorrectAnswers.shuffled().take(3) + currentQuestion.answer).shuffled()
    }

    val progress by animateFloatAsState(targetValue = correctAnswersInARow / 6f, label = "progress")

    LaunchedEffect(feedback) {
        if (feedback == "Correct!") {
            delay(1000L) // Wait for 1 second
            if (correctAnswersInARow < 6) {
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                } else {
                    questions = jouerConjugations.shuffled()
                    currentQuestionIndex = 0
                }
                selectedAnswer = null
                feedback = null
            }
        }
    }

    if (gameWon) {
        GameWonScreen {
            correctAnswersInARow = 0
            questions = jouerConjugations.shuffled()
            currentQuestionIndex = 0
            selectedAnswer = null
            feedback = null
            gameWon = false
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Conjugate the verb: Jouer", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Pronoun: ${currentQuestion.pronoun}", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            options.forEach { option ->
                Button(
                    onClick = { if (feedback == null) selectedAnswer = option },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAnswer == option) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(text = option)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isFeedbackVisible = feedback != null

            if (!isFeedbackVisible) {
                Button(
                    onClick = {
                        if (selectedAnswer == currentQuestion.answer) {
                            feedback = "Correct!"
                            correctAnswersInARow++
                            if (correctAnswersInARow >= 4) {
                                vibrate(context)
                            }
                            if (correctAnswersInARow == 6) {
                                gameWon = true
                            }
                        } else {
                            feedback = "Wrong!"
                            correctAnswersInARow = 0
                        }
                    },
                    enabled = selectedAnswer != null
                ) {
                    Text(text = "Check")
                }
            }

            AnimatedVisibility(visible = isFeedbackVisible) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = feedback ?: "",
                        color = if (feedback == "Correct!") Color(0xFF00C853) else Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (feedback == "Wrong!") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${currentQuestion.pronoun} ${currentQuestion.answer}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                            } else {
                                questions = jouerConjugations.shuffled()
                                currentQuestionIndex = 0
                            }
                            selectedAnswer = null
                            feedback = null
                        }) {
                            Text(text = "Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameWonScreen(onPlayAgain: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("You got 6 in a row!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("🎉🏆🎉", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPlayAgain) {
            Text("Play Again")
        }
    }
}

fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}
