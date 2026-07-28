package com.petlingo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.petlingo.app.data.Accent
import com.petlingo.app.ui.PetLingoApp
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private val vm: PetLingoViewModel by viewModels()
    private var tts: TextToSpeech? = null

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!text.isNullOrBlank()) vm.scoreSpeech(text)
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchSpeechRecognition(vm.state.value.accent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent {
            PetLingoApp(
                vm = vm,
                onSpeak = { text, accent -> speak(text, accent) },
                onStartListening = { accent -> requestSpeech(accent) }
            )
        }
    }

    override fun onInit(status: Int) = Unit

    private fun speak(text: String, accent: Accent) {
        tts?.language = Locale.forLanguageTag(accent.languageTag)
        tts?.setSpeechRate(0.85f)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "petlingo-${System.currentTimeMillis()}")
    }

    private fun requestSpeech(accent: Accent) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            launchSpeechRecognition(accent)
        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun launchSpeechRecognition(accent: Accent) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, accent.languageTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "請朗讀畫面上的英文")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechLauncher.launch(intent)
    }

    override fun onDestroy() {
        tts?.stop(); tts?.shutdown(); super.onDestroy()
    }
}
