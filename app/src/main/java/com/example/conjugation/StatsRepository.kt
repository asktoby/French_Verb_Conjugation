package com.example.conjugation

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar

data class QuizResult(val attempts: Int, val durationInSeconds: Int)

class StatsRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_RESULTS = "results"
        private const val KEY_STREAK = "current_streak"
        private const val KEY_LAST_PLAYED = "last_played_timestamp"
    }

    fun saveResult(result: QuizResult) {
        val results = getAllResults().toMutableList()
        results.add(result)
        val json = gson.toJson(results)

        updateStreak()

        sharedPreferences.edit().putString(KEY_RESULTS, json).apply()
    }

    private fun updateStreak() {
        val lastPlayedTimestamp = sharedPreferences.getLong(KEY_LAST_PLAYED, 0L)
        var currentStreak = sharedPreferences.getInt(KEY_STREAK, 0)

        if (lastPlayedTimestamp > 0) {
            val today = Calendar.getInstance()
            val lastPlayedDay = Calendar.getInstance().apply { timeInMillis = lastPlayedTimestamp }

            if (!isSameDay(today, lastPlayedDay)) {
                lastPlayedDay.add(Calendar.DAY_OF_YEAR, 1)
                if (isSameDay(today, lastPlayedDay)) {
                    // Played yesterday, increment streak
                    currentStreak++
                } else {
                    // Didn't play yesterday, reset streak
                    currentStreak = 1
                }
            }
            // If played today, do nothing to streak.
        } else {
            // First time playing
            currentStreak = 1
        }

        sharedPreferences.edit()
            .putInt(KEY_STREAK, currentStreak)
            .putLong(KEY_LAST_PLAYED, System.currentTimeMillis())
            .apply()
    }

    fun getStreak(): Int {
        val lastPlayedTimestamp = sharedPreferences.getLong(KEY_LAST_PLAYED, 0L)
        val currentStreak = sharedPreferences.getInt(KEY_STREAK, 0)

        if (lastPlayedTimestamp > 0) {
            val today = Calendar.getInstance()
            val lastPlayedDay = Calendar.getInstance().apply { timeInMillis = lastPlayedTimestamp }

            // if last played was not today
            if (!isSameDay(today, lastPlayedDay)) {
                lastPlayedDay.add(Calendar.DAY_OF_YEAR, 1)
                // and was not yesterday, then streak is broken.
                if (!isSameDay(today, lastPlayedDay)) {
                    sharedPreferences.edit().putInt(KEY_STREAK, 0).apply() // Reset stored streak
                    return 0
                }
            }
        }
        return currentStreak
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun getAllResults(): List<QuizResult> {
        val json = sharedPreferences.getString(KEY_RESULTS, null)
        return if (json != null) {
            val type = object : TypeToken<List<QuizResult>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
