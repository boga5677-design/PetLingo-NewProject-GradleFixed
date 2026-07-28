package com.petlingo.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class StudyStore(context: Context) {
    private val prefs = context.getSharedPreferences("petlingo_records", Context.MODE_PRIVATE)

    fun loadRecords(): List<StudyRecord> {
        val text = prefs.getString("records", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(text)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        StudyRecord(
                            id = o.getLong("id"), timeMillis = o.getLong("timeMillis"),
                            type = o.getString("type"), question = o.getString("question"),
                            answer = o.getString("answer"), correctAnswer = o.getString("correctAnswer"),
                            correct = o.getBoolean("correct"), score = o.getInt("score"),
                            detail = o.getString("detail")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveRecords(records: List<StudyRecord>) {
        val array = JSONArray()
        records.take(500).forEach { r ->
            array.put(JSONObject().apply {
                put("id", r.id); put("timeMillis", r.timeMillis); put("type", r.type)
                put("question", r.question); put("answer", r.answer); put("correctAnswer", r.correctAnswer)
                put("correct", r.correct); put("score", r.score); put("detail", r.detail)
            })
        }
        prefs.edit().putString("records", array.toString()).apply()
    }

    fun loadAccent(): Accent = runCatching { Accent.valueOf(prefs.getString("accent", Accent.US.name)!!) }.getOrDefault(Accent.US)
    fun saveAccent(accent: Accent) = prefs.edit().putString("accent", accent.name).apply()
    fun loadFavorites(): Set<Int> = prefs.getStringSet("favorites", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()
    fun saveFavorites(ids: Set<Int>) = prefs.edit().putStringSet("favorites", ids.map(Int::toString).toSet()).apply()
}
