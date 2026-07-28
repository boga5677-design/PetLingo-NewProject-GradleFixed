package com.petlingo.app.data

data class Word(val id: Int, val english: String, val chinese: String)

enum class Accent(val label: String, val languageTag: String) {
    US("美式", "en-US"), UK("英式", "en-GB")
}

data class StudyRecord(
    val id: Long,
    val timeMillis: Long,
    val type: String,
    val question: String,
    val answer: String,
    val correctAnswer: String,
    val correct: Boolean,
    val score: Int,
    val detail: String
)
