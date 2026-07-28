package com.petlingo.app

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petlingo.app.data.Word
import com.petlingo.app.data.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class PetLingoState(
    val words: List<Word> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val studied: Set<Int> = emptySet(),
    val answered: Int = 0,
    val correct: Int = 0,
    val darkMode: Boolean = false,
    val loading: Boolean = true,
    val ttsReady: Boolean = false
) {
    val accuracy: Int get() = if (answered == 0) 0 else correct * 100 / answered
    val level: Int get() = studied.size / 50 + 1
    val xp: Int get() = studied.size * 5 + correct * 10
}

class PetLingoViewModel(app: Application) : AndroidViewModel(app), TextToSpeech.OnInitListener {
    private val prefs = app.getSharedPreferences("petlingo_new", 0)
    private val _state = MutableStateFlow(
        PetLingoState(
            favorites = readSet("favorites"),
            studied = readSet("studied"),
            answered = prefs.getInt("answered", 0),
            correct = prefs.getInt("correct", 0),
            darkMode = prefs.getBoolean("dark", false)
        )
    )
    val state: StateFlow<PetLingoState> = _state.asStateFlow()
    private val tts = TextToSpeech(app, this)

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(words = WordRepository.load(app), loading = false)
        }
    }

    override fun onInit(status: Int) {
        val ready = status == TextToSpeech.SUCCESS &&
            tts.setLanguage(Locale.US) >= TextToSpeech.LANG_AVAILABLE
        if (ready) tts.setSpeechRate(0.85f)
        _state.value = _state.value.copy(ttsReady = ready)
    }

    fun speak(text: String) {
        if (_state.value.ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "petlingo-${text.hashCode()}")
        }
    }

    fun toggleFavorite(id: Int) {
        val next = _state.value.favorites.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        saveSet("favorites", next)
        _state.value = _state.value.copy(favorites = next)
    }

    fun markStudied(id: Int) {
        val next = _state.value.studied + id
        saveSet("studied", next)
        _state.value = _state.value.copy(studied = next)
    }

    fun recordAnswer(word: Word, correct: Boolean) {
        val studied = _state.value.studied + word.id
        val answered = _state.value.answered + 1
        val correctCount = _state.value.correct + if (correct) 1 else 0
        saveSet("studied", studied)
        prefs.edit().putInt("answered", answered).putInt("correct", correctCount).apply()
        _state.value = _state.value.copy(
            studied = studied,
            answered = answered,
            correct = correctCount
        )
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark", enabled).apply()
        _state.value = _state.value.copy(darkMode = enabled)
    }

    private fun readSet(key: String): Set<Int> =
        prefs.getStringSet(key, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()

    private fun saveSet(key: String, values: Set<Int>) {
        prefs.edit().putStringSet(key, values.map(Int::toString).toSet()).apply()
    }

    override fun onCleared() {
        tts.stop()
        tts.shutdown()
        super.onCleared()
    }
}
