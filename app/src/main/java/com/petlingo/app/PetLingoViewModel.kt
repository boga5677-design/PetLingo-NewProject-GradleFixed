package com.petlingo.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petlingo.app.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class PetLingoState(
    val loading: Boolean = true,
    val words: List<Word> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val records: List<StudyRecord> = emptyList(),
    val accent: Accent = Accent.US,
    val quizIndex: Int = 0,
    val quizOptions: List<Word> = emptyList(),
    val quizSelectedId: Int? = null,
    val quizAnswered: Boolean = false,
    val speechTarget: Word? = null,
    val speechRecognized: String = "",
    val speechScore: Int? = null
)

class PetLingoViewModel(app: Application) : AndroidViewModel(app) {
    private val store = StudyStore(app)
    private val _state = MutableStateFlow(PetLingoState())
    val state: StateFlow<PetLingoState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val words = WordRepository.load(app).take(400)
            _state.value = PetLingoState(
                loading = false, words = words, favorites = store.loadFavorites(),
                records = store.loadRecords(), accent = store.loadAccent(),
                speechTarget = words.firstOrNull()
            )
            prepareQuiz()
        }
    }

    fun setAccent(accent: Accent) { store.saveAccent(accent); _state.value = _state.value.copy(accent = accent) }

    fun toggleFavorite(id: Int) {
        val next = _state.value.favorites.toMutableSet().apply { if (!add(id)) remove(id) }
        store.saveFavorites(next); _state.value = _state.value.copy(favorites = next)
    }

    fun prepareQuiz() {
        val s = _state.value
        if (s.words.size < 4) return
        val answer = s.words.random()
        val options = (s.words.filter { it.id != answer.id }.shuffled().take(3) + answer).shuffled()
        _state.value = s.copy(quizIndex = answer.id, quizOptions = options, quizSelectedId = null, quizAnswered = false)
    }

    fun answerQuiz(selected: Word) {
        val s = _state.value
        if (s.quizAnswered) return
        val answer = s.words.firstOrNull { it.id == s.quizIndex } ?: return
        val correct = selected.id == answer.id
        val record = StudyRecord(
            id = System.currentTimeMillis(), timeMillis = System.currentTimeMillis(), type = "測驗",
            question = answer.english, answer = selected.chinese, correctAnswer = answer.chinese,
            correct = correct, score = if (correct) 100 else 0,
            detail = if (correct) "答對了" else "你選擇「${selected.chinese}」，正確答案是「${answer.chinese}」"
        )
        addRecord(record)
        _state.value = _state.value.copy(quizSelectedId = selected.id, quizAnswered = true)
    }

    fun chooseSpeechWord(word: Word) { _state.value = _state.value.copy(speechTarget = word, speechRecognized = "", speechScore = null) }

    fun scoreSpeech(recognized: String) {
        val target = _state.value.speechTarget ?: return
        val score = similarity(target.english, recognized)
        val detail = when {
            score >= 90 -> "發音非常接近目標，可繼續保持。"
            score >= 70 -> "整體清楚，請再注意重音與尾音。"
            score >= 50 -> "部分音節正確，建議先慢速跟讀。"
            else -> "辨識差異較大，請聽示範後分音節練習。"
        }
        addRecord(
            StudyRecord(System.currentTimeMillis(), System.currentTimeMillis(), "口說", target.english,
                recognized, target.english, score >= 70, score, detail)
        )
        _state.value = _state.value.copy(speechRecognized = recognized, speechScore = score)
    }

    fun clearRecords() { store.saveRecords(emptyList()); _state.value = _state.value.copy(records = emptyList()) }

    private fun addRecord(record: StudyRecord) {
        val records = listOf(record) + _state.value.records
        store.saveRecords(records)
        _state.value = _state.value.copy(records = records)
    }

    private fun similarity(target: String, actual: String): Int {
        val a = target.lowercase().trim().filter { it.isLetter() || it == ' ' }
        val b = actual.lowercase().trim().filter { it.isLetter() || it == ' ' }
        if (a.isBlank() || b.isBlank()) return 0
        val d = levenshtein(a, b)
        return ((1.0 - d.toDouble() / maxOf(a.length, b.length)) * 100).coerceIn(0.0, 100.0).roundToInt()
    }

    private fun levenshtein(a: String, b: String): Int {
        val prev = IntArray(b.length + 1) { it }
        val cur = IntArray(b.length + 1)
        for (i in a.indices) {
            cur[0] = i + 1
            for (j in b.indices) cur[j + 1] = minOf(cur[j] + 1, prev[j + 1] + 1, prev[j] + if (a[i] == b[j]) 0 else 1)
            for (j in prev.indices) prev[j] = cur[j]
        }
        return prev[b.length]
    }
}
