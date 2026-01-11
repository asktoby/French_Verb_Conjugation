package com.example.conjugation

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class QuizResult(val attempts: Int, val durationInSeconds: Int)

class StatsRepository(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveResult(result: QuizResult) {
        val results = getAllResults().toMutableList()
        results.add(result)
        val json = gson.toJson(results)
        sharedPreferences.edit().putString("results", json).apply()
    }

    fun getAllResults(): List<QuizResult> {
        val json = sharedPreferences.getString("results", null)
        return if (json != null) {
            val type = object : TypeToken<List<QuizResult>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
