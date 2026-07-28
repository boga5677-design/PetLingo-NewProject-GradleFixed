package com.petlingo.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

private val Paper = Color(0xFFFFFDF8)
private val Sage = Color(0xFFA9B77A)
private val SoftSage = Color(0xFFEAF0D8)
private val Ink = Color(0xFF2D2B27)
private val Warm = Color(0xFFD9B276)
private val Peach = Color(0xFFF5D8C5)
private val Success = Color(0xFF5D8A62)
private val Error = Color(0xFFB65F55)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PetLingoApp() }
    }
}

enum class Page(val label: String) {
    Home("首頁"), Words("單字"), Reading("閱讀"), Speaking("口說"), Quiz("測驗"), Records("記錄"), More("更多")
}
enum class PetKind { Tabby, Dog, Calico }
enum class Accent(val label: String, val locale: Locale) {
    American("美式", Locale.US), British("英式", Locale.UK)
}

data class WordItem(val english: String, val chinese: String, val example: String)
data class QuizQuestion(val prompt: String, val answer: String, val options: List<String>, val explanation: String)
data class LearningRecord(
    val type: String,
    val title: String,
    val result: String,
    val score: Int,
    val timestamp: Long,
    val detail: String
)

data class AppState(
    val favorites: Set<String> = setOf("check out"),
    val accent: Accent = Accent.American,
    val records: List<LearningRecord> = emptyList()
)

private val words = listOf(
    WordItem("check out", "退房；查看", "Please check out before noon."),
    WordItem("plant", "植物", "This plant needs more sunlight."),
    WordItem("inspire", "激勵；啟發", "Her story inspired the whole team."),
    WordItem("traditional", "傳統的", "This is a traditional workplace custom."),
    WordItem("available", "可取得的；有空的", "The manager is available after lunch."),
    WordItem("confirm", "確認", "Please confirm your reservation by Friday.")
)

private val questions = listOf(
    QuizQuestion("check out", "退房；查看", listOf("退房；查看", "植物", "激勵；啟發", "傳統的"), "check out 可表示『退房』，也可以表示『查看』。"),
    QuizQuestion("inspire", "激勵；啟發", listOf("確認", "激勵；啟發", "有空的", "傳統的"), "inspire 是『激勵、啟發』的意思。"),
    QuizQuestion("available", "可取得的；有空的", listOf("植物", "可取得的；有空的", "退房", "確認"), "available 常用來表示人『有空』或物品『可取得』。"),
    QuizQuestion("confirm", "確認", listOf("傳統的", "激勵", "確認", "植物"), "confirm 是『確認、證實』。")
)

@Composable
fun PetLingoApp() {
    val context = LocalContext.current
    var page by remember { mutableStateOf(Page.Home) }
    var state by remember { mutableStateOf(loadState(context)) }

    fun update(newState: AppState) {
        state = newState
        saveState(context, newState)
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Sage, background = Paper, surface = Paper, onSurface = Ink)) {
        Scaffold(
            containerColor = Paper,
            bottomBar = { BottomNav(page) { page = it } }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).background(Paper)) {
                Header(state.accent)
                when (page) {
                    Page.Home -> HomePage(state.records) { page = it }
                    Page.Words -> WordsPage(
                        favorites = state.favorites,
                        accent = state.accent,
                        onAccent = { update(state.copy(accent = it)) },
                        onToggle = { word ->
                            val next = if (word in state.favorites) state.favorites - word else state.favorites + word
                            update(state.copy(favorites = next))
                        },
                        onRecord = { update(state.copy(records = listOf(it) + state.records)) }
                    )
                    Page.Reading -> ReadingPage { update(state.copy(records = listOf(it) + state.records)) }
                    Page.Speaking -> SpeakingPage(
                        accent = state.accent,
                        onAccent = { update(state.copy(accent = it)) },
                        onRecord = { update(state.copy(records = listOf(it) + state.records)) }
                    )
                    Page.Quiz -> QuizPage { update(state.copy(records = listOf(it) + state.records)) }
                    Page.Records -> RecordsPage(state.records, onClear = { update(state.copy(records = emptyList())) })
                    Page.More -> MorePage(state.favorites.size, state.records.size)
                }
            }
        }
    }
}

@Composable
private fun Header(accent: Accent) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("🐾", fontSize = 29.sp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text("PetLingo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("目前發音：${accent.label}", fontSize = 13.sp, color = Ink.copy(alpha = .65f))
        }
        Icon(Icons.Default.AutoStories, null, tint = Sage)
    }
}

@Composable
private fun HomePage(records: List<LearningRecord>, onGo: (Page) -> Unit) {
    val todayStart = remember {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.HOUR_OF_DAY, 0); now.set(java.util.Calendar.MINUTE, 0); now.set(java.util.Calendar.SECOND, 0); now.timeInMillis
    }
    val todayRecords = records.filter { it.timestamp >= todayStart }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PetIllustration(PetKind.Tabby, Modifier.size(205.dp))
        Text("今天也一起進步吧！", fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("今日完成 ${todayRecords.size} 次練習", color = Ink.copy(alpha = .68f), modifier = Modifier.padding(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("總練習", records.size.toString(), Modifier.weight(1f))
            StatCard("平均分", if (records.isEmpty()) "—" else "${records.map { it.score }.average().roundToInt()}分", Modifier.weight(1f))
            StatCard("錯題", records.count { it.type == "測驗" && it.score == 0 }.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        HomeCard("單字練習", "英式／美式發音與例句", Icons.Default.MenuBook) { onGo(Page.Words) }
        HomeCard("TOEIC 閱讀", "短篇文章與閱讀記錄", Icons.Default.Article) { onGo(Page.Reading) }
        HomeCard("口說評分", "語音辨識、準確度與逐字回饋", Icons.Default.Mic) { onGo(Page.Speaking) }
        HomeCard("小測驗", "答題立即顯示正確、錯誤與解析", Icons.Default.Quiz) { onGo(Page.Quiz) }
        HomeCard("學習記錄", "查看錯題、口說分數與完整歷程", Icons.Default.Assessment) { onGo(Page.Records) }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = .75f), shadowElevation = 1.dp) {
        Column(Modifier.padding(vertical = 13.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Sage)
            Text(label, fontSize = 12.sp, color = Ink.copy(alpha = .65f))
        }
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = .76f), shadowElevation = 2.dp) {
        Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(47.dp).clip(CircleShape).background(SoftSage), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Sage) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) { Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = Ink.copy(alpha = .64f), fontSize = 14.sp) }
            Icon(Icons.Default.ChevronRight, null, tint = Warm)
        }
    }
}

@Composable
private fun AccentSelector(accent: Accent, onAccent: (Accent) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Accent.entries.forEach { item ->
            FilterChip(
                selected = accent == item,
                onClick = { onAccent(item) },
                label = { Text("${if (item == Accent.American) "🇺🇸" else "🇬🇧"} ${item.label}發音") },
                leadingIcon = if (accent == item) { { Icon(Icons.Default.Check, null, Modifier.size(17.dp)) } } else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WordsPage(
    favorites: Set<String>,
    accent: Accent,
    onAccent: (Accent) -> Unit,
    onToggle: (String) -> Unit,
    onRecord: (LearningRecord) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Text("單字花園", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("可切換英式或美式發音，播放也會列入學習記錄。", color = Ink.copy(alpha = .65f))
        Spacer(Modifier.height(10.dp))
        AccentSelector(accent, onAccent)
        Spacer(Modifier.height(8.dp))
        words.forEachIndexed { index, item ->
            WordCard(item, index, item.english in favorites, accent, onToggle) {
                onRecord(LearningRecord("單字", item.english, "已聆聽${accent.label}發音", 100, System.currentTimeMillis(), item.example))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WordCard(item: WordItem, index: Int, favorite: Boolean, accent: Accent, onToggle: () -> Unit, onPlayed: () -> Unit) {
    val context = LocalContext.current
    val tts = rememberTts(context, accent.locale)
    Surface(Modifier.fillMaxWidth().padding(vertical = 7.dp), shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = .78f), shadowElevation = 2.dp) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.english, Modifier.weight(1f), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    tts?.language = accent.locale
                    tts?.speak(item.english, TextToSpeech.QUEUE_FLUSH, null, "word_${item.english}")
                    onPlayed()
                }) { Icon(Icons.Default.VolumeUp, null, tint = Sage) }
                IconButton(onClick = onToggle) { Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, null, tint = Warm) }
            }
            Text(item.chinese, fontSize = 17.sp, color = Ink.copy(alpha = .78f))
            Spacer(Modifier.height(9.dp))
            Text(item.example, color = Ink.copy(alpha = .65f))
            LinearProgressIndicator(progress = { (index + 1) / words.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 13.dp), color = Sage, trackColor = SoftSage)
        }
    }
}

@Composable
private fun ReadingPage(onRecord: (LearningRecord) -> Unit) {
    var completed by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PetIllustration(PetKind.Calico, Modifier.size(180.dp))
        Surface(shape = RoundedCornerShape(26.dp), color = Color.White.copy(alpha = .8f), shadowElevation = 2.dp) {
            Column(Modifier.padding(22.dp)) {
                Text("📖 TOEIC 閱讀練習", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text("The customer-service workshop will begin at 9:30 a.m. Please arrive early and bring the workbook distributed last week.", fontSize = 19.sp, lineHeight = 30.sp)
                HorizontalDivider(Modifier.padding(vertical = 17.dp), color = SoftSage)
                Text("客服研習將於上午 9:30 開始，請提早抵達並攜帶上週發放的講義。", fontSize = 17.sp, lineHeight = 27.sp, color = Ink.copy(alpha = .8f))
                Spacer(Modifier.height(17.dp))
                Button(
                    onClick = {
                        if (!completed) onRecord(LearningRecord("閱讀", "Customer-service workshop", "完成閱讀", 100, System.currentTimeMillis(), "已閱讀英文與中文翻譯"))
                        completed = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Sage),
                    shape = RoundedCornerShape(18.dp)
                ) { Icon(if (completed) Icons.Default.CheckCircle else Icons.Default.BookmarkAdded, null); Spacer(Modifier.width(8.dp)); Text(if (completed) "已完成並記錄" else "完成閱讀") }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun SpeakingPage(accent: Accent, onAccent: (Accent) -> Unit, onRecord: (LearningRecord) -> Unit) {
    val context = LocalContext.current
    val target = "I will do my best"
    val tts = rememberTts(context, accent.locale)
    var recognized by remember { mutableStateOf("") }
    var score by remember { mutableStateOf<Int?>(null) }
    var feedback by remember { mutableStateOf("按下麥克風後，清楚讀出上方句子。") }
    var listening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        listening = false
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        recognized = text
        if (text.isNotBlank()) {
            val newScore = pronunciationScore(target, text)
            score = newScore
            feedback = pronunciationFeedback(target, text, newScore)
            onRecord(LearningRecord("口說", target, "辨識：$text", newScore, System.currentTimeMillis(), feedback))
        } else {
            feedback = "沒有辨識到語音，請靠近麥克風再試一次。"
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            listening = true
            speechLauncher.launch(speechIntent(accent.locale))
        } else feedback = "需要麥克風權限才能進行口說評分。"
    }

    fun startSpeaking() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            feedback = "此裝置目前沒有可用的語音辨識服務。"
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            listening = true
            speechLauncher.launch(speechIntent(accent.locale))
        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PetIllustration(PetKind.Dog, Modifier.size(205.dp))
        AccentSelector(accent, onAccent)
        Spacer(Modifier.height(12.dp))
        Text("I will do my best.", fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("我會盡我的全力。", fontSize = 19.sp, modifier = Modifier.padding(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                tts?.language = accent.locale
                tts?.speak(target, TextToSpeech.QUEUE_FLUSH, null, "sentence")
            }, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Sage)) {
                Icon(Icons.Default.VolumeUp, null); Spacer(Modifier.width(7.dp)); Text("播放${accent.label}示範")
            }
            OutlinedButton(onClick = { startSpeaking() }, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Sage)) {
                Icon(if (listening) Icons.Default.Hearing else Icons.Default.Mic, null); Spacer(Modifier.width(7.dp)); Text(if (listening) "聆聽中" else "開始評分")
            }
        }
        Spacer(Modifier.height(16.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = .8f), shadowElevation = 2.dp) {
            Column(Modifier.padding(20.dp)) {
                Text("口說評分", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                if (score != null) {
                    Text("$score 分", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = scoreColor(score!!), modifier = Modifier.padding(vertical = 6.dp))
                    LinearProgressIndicator(progress = { score!! / 100f }, modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape), color = scoreColor(score!!), trackColor = SoftSage)
                    Spacer(Modifier.height(12.dp))
                    Text("辨識結果：${if (recognized.isBlank()) "—" else recognized}", fontWeight = FontWeight.SemiBold)
                }
                Text(feedback, color = Ink.copy(alpha = .75f), lineHeight = 23.sp, modifier = Modifier.padding(top = 10.dp))
                Text("評分依語音辨識文字與目標句子的字詞相似度計算，適合作為練習回饋，不等同專業語音測驗。", fontSize = 12.sp, color = Ink.copy(alpha = .52f), modifier = Modifier.padding(top = 12.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun QuizPage(onRecord: (LearningRecord) -> Unit) {
    var questionIndex by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var answered by remember { mutableIntStateOf(0) }
    var correct by remember { mutableIntStateOf(0) }
    val q = questions[questionIndex]

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("測驗 ${questionIndex + 1}/${questions.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("答對 $correct 題", color = Sage, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { (questionIndex + 1) / questions.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), color = Sage, trackColor = SoftSage)
        PetIllustration(PetKind.Tabby, Modifier.size(145.dp).align(Alignment.CenterHorizontally))
        Text(q.prompt, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("選出正確中文意思", fontSize = 19.sp, modifier = Modifier.padding(bottom = 13.dp))
        q.options.forEach { option ->
            val correctOption = option == q.answer
            val selectedOption = option == selected
            val bg = when {
                selected != null && correctOption -> SoftSage
                selectedOption && !correctOption -> Peach
                else -> Color.White.copy(alpha = .76f)
            }
            val borderColor = when {
                selected != null && correctOption -> Success
                selectedOption && !correctOption -> Error
                else -> Sage.copy(alpha = .4f)
            }
            Surface(
                Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(enabled = selected == null) {
                    selected = option
                    answered++
                    val isCorrect = option == q.answer
                    if (isCorrect) correct++
                    onRecord(LearningRecord("測驗", q.prompt, if (isCorrect) "答對" else "答錯：$option", if (isCorrect) 100 else 0, System.currentTimeMillis(), "正確答案：${q.answer}。${q.explanation}"))
                },
                shape = RoundedCornerShape(23.dp), color = bg, border = BorderStroke(1.5.dp, borderColor)
            ) {
                Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(option, Modifier.weight(1f), fontSize = 17.sp, textAlign = TextAlign.Center)
                    if (selected != null && correctOption) Icon(Icons.Default.CheckCircle, null, tint = Success)
                    if (selectedOption && !correctOption) Icon(Icons.Default.Cancel, null, tint = Error)
                }
            }
        }
        if (selected != null) {
            val isCorrect = selected == q.answer
            Surface(Modifier.fillMaxWidth().padding(top = 13.dp), shape = RoundedCornerShape(20.dp), color = if (isCorrect) SoftSage else Peach) {
                Column(Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Error, null, tint = if (isCorrect) Success else Error)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isCorrect) "答對了！" else "答錯了", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isCorrect) Success else Error)
                    }
                    Text("正確答案：${q.answer}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    Text(q.explanation, color = Ink.copy(alpha = .75f), modifier = Modifier.padding(top = 4.dp))
                }
            }
            Button(onClick = {
                questionIndex = (questionIndex + 1) % questions.size
                selected = null
            }, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Sage)) {
                Text(if (questionIndex == questions.lastIndex) "重新測驗" else "下一題")
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun RecordsPage(records: List<LearningRecord>, onClear: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val mistakes = records.filter { it.type == "測驗" && it.score == 0 }
    val speaking = records.filter { it.type == "口說" }
    val shown = when (tab) { 1 -> mistakes; 2 -> speaking; else -> records }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("學習記錄", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("完整保存在裝置內，重新開啟 App 仍會保留。", color = Ink.copy(alpha = .62f), fontSize = 13.sp)
            }
            if (records.isNotEmpty()) TextButton(onClick = onClear) { Icon(Icons.Default.DeleteSweep, null); Text("清除") }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            listOf("全部 ${records.size}", "錯題 ${mistakes.size}", "口說 ${speaking.size}").forEachIndexed { i, label ->
                FilterChip(selected = tab == i, onClick = { tab = i }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        if (shown.isEmpty()) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = .76f)) {
                Column(Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryEdu, null, tint = Sage, modifier = Modifier.size(48.dp))
                    Text("目前沒有記錄", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    Text("完成單字、閱讀、口說或測驗後，詳細結果會顯示在這裡。", textAlign = TextAlign.Center, color = Ink.copy(alpha = .65f), modifier = Modifier.padding(top = 6.dp))
                }
            }
        } else shown.forEach { record -> RecordCard(record) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RecordCard(record: LearningRecord) {
    val icon = when (record.type) { "測驗" -> Icons.Default.Quiz; "口說" -> Icons.Default.Mic; "閱讀" -> Icons.Default.Article; else -> Icons.Default.MenuBook }
    Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = .78f), shadowElevation = 1.dp) {
        Column(Modifier.padding(17.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(SoftSage), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Sage) }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("${record.type}｜${record.title}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(formatTime(record.timestamp), color = Ink.copy(alpha = .55f), fontSize = 12.sp)
                }
                Text("${record.score}分", fontWeight = FontWeight.Bold, color = scoreColor(record.score))
            }
            Text(record.result, modifier = Modifier.padding(top = 10.dp), fontWeight = FontWeight.SemiBold)
            Text(record.detail, color = Ink.copy(alpha = .68f), fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun MorePage(favorites: Int, records: Int) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        PetIllustration(PetKind.Calico, Modifier.size(175.dp))
        Text("PetLingo 2.0", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text("已加入錯題記錄、詳細學習歷程、測驗即時提示、口說辨識評分，以及英式／美式發音切換。", textAlign = TextAlign.Center, color = Ink.copy(alpha = .72f), lineHeight = 23.sp, modifier = Modifier.padding(12.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = .78f)) {
            Column(Modifier.padding(18.dp)) {
                Text("本機資料", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                Text("收藏單字：$favorites 個")
                Text("學習記錄：$records 筆")
                Text("資料以 SharedPreferences 儲存在目前裝置。", color = Ink.copy(alpha = .6f), fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun BottomNav(current: Page, onSelect: (Page) -> Unit) {
    NavigationBar(containerColor = Color(0xFFF6F2F8), tonalElevation = 0.dp) {
        val icons = mapOf(
            Page.Home to Icons.Default.Home,
            Page.Words to Icons.Default.MenuBook,
            Page.Reading to Icons.Default.Article,
            Page.Speaking to Icons.Default.Mic,
            Page.Quiz to Icons.Default.Quiz,
            Page.Records to Icons.Default.Assessment,
            Page.More to Icons.Default.MoreHoriz
        )
        Page.entries.forEach { page ->
            NavigationBarItem(
                selected = current == page,
                onClick = { onSelect(page) },
                icon = { Icon(icons.getValue(page), page.label) },
                label = { Text(page.label, fontSize = 9.sp) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFFDDF4E4), selectedIconColor = Ink, selectedTextColor = Ink)
            )
        }
    }
}

@Composable
private fun rememberTts(context: Context, locale: Locale): TextToSpeech? {
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) tts?.language = locale }
        tts = engine
        onDispose { engine.stop(); engine.shutdown() }
    }
    LaunchedEffect(locale) { tts?.language = locale }
    return tts
}

private fun speechIntent(locale: Locale) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.toLanguageTag())
    putExtra(RecognizerIntent.EXTRA_PROMPT, "請朗讀句子")
    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
}

private fun normalize(text: String): String = text.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9 ]"), "").trim().replace(Regex("\\s+"), " ")

private fun pronunciationScore(target: String, spoken: String): Int {
    val a = normalize(target)
    val b = normalize(spoken)
    if (a.isEmpty() || b.isEmpty()) return 0
    val distance = levenshtein(a, b)
    val charScore = (1.0 - distance.toDouble() / max(a.length, b.length)).coerceIn(0.0, 1.0)
    val targetWords = a.split(" ").toSet()
    val spokenWords = b.split(" ").toSet()
    val wordScore = targetWords.intersect(spokenWords).size.toDouble() / targetWords.size
    return ((charScore * 0.55 + wordScore * 0.45) * 100).roundToInt().coerceIn(0, 100)
}

private fun pronunciationFeedback(target: String, spoken: String, score: Int): String {
    val t = normalize(target).split(" ")
    val s = normalize(spoken).split(" ")
    val missing = t.filter { it !in s }
    return when {
        score >= 90 -> "非常清楚！句子內容幾乎完全正確。"
        score >= 75 -> "表現很好。${if (missing.isNotEmpty()) "再注意：${missing.joinToString(", ")}。" else "再放慢一點會更自然。"}"
        score >= 55 -> "已有基本準確度。建議分段跟讀：I will／do my best。${if (missing.isNotEmpty()) "未清楚辨識：${missing.joinToString(", ")}。" else ""}"
        else -> "辨識差異較大，請先播放示範，再靠近麥克風慢速朗讀。${if (missing.isNotEmpty()) "需要加強：${missing.joinToString(", ")}。" else ""}"
    }
}

private fun levenshtein(a: String, b: String): Int {
    val costs = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        var previous = costs[0]
        costs[0] = i
        for (j in 1..b.length) {
            val old = costs[j]
            costs[j] = minOf(costs[j] + 1, costs[j - 1] + 1, previous + if (a[i - 1] == b[j - 1]) 0 else 1)
            previous = old
        }
    }
    return costs[b.length]
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Success
    score >= 60 -> Warm
    else -> Error
}

private fun formatTime(timestamp: Long): String = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN).format(Date(timestamp))

private fun loadState(context: Context): AppState {
    return try {
        val prefs = context.getSharedPreferences("petlingo", Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorites", setOf("check out")) ?: emptySet()
        val accent = runCatching { Accent.valueOf(prefs.getString("accent", Accent.American.name)!!) }.getOrDefault(Accent.American)
        val array = JSONArray(prefs.getString("records", "[]"))
        val records = buildList {
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                add(LearningRecord(o.getString("type"), o.getString("title"), o.getString("result"), o.getInt("score"), o.getLong("timestamp"), o.getString("detail")))
            }
        }
        AppState(favorites, accent, records)
    } catch (_: Exception) { AppState() }
}

private fun saveState(context: Context, state: AppState) {
    val array = JSONArray()
    state.records.take(500).forEach { r ->
        array.put(JSONObject().apply {
            put("type", r.type); put("title", r.title); put("result", r.result); put("score", r.score); put("timestamp", r.timestamp); put("detail", r.detail)
        })
    }
    context.getSharedPreferences("petlingo", Context.MODE_PRIVATE).edit()
        .putStringSet("favorites", state.favorites)
        .putString("accent", state.accent.name)
        .putString("records", array.toString())
        .apply()
}

@Composable
private fun PetIllustration(kind: PetKind, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawCircle(Color(0xFFF7EEDB).copy(alpha = .38f), radius = w * .42f, center = Offset(w * .5f, h * .52f))
        for (i in 0..8) {
            val x = w * (.1f + (i % 5) * .19f)
            val y = h * (.16f + (i % 4) * .19f)
            drawCircle(if (i % 2 == 0) Color(0xFFE8D9B5).copy(alpha = .17f) else Color(0xFFDDE8C8).copy(alpha = .22f), w * .022f, Offset(x, y))
        }
        val face = when (kind) { PetKind.Tabby -> Color(0xFFB99B70); PetKind.Dog -> Color(0xFFFFE8B9); PetKind.Calico -> Color(0xFF3F3931) }
        val ear = Path().apply { moveTo(w*.27f,h*.38f); lineTo(w*.34f,h*.12f); lineTo(w*.45f,h*.39f); close() }
        val ear2 = Path().apply { moveTo(w*.55f,h*.39f); lineTo(w*.68f,h*.12f); lineTo(w*.75f,h*.4f); close() }
        drawPath(ear, face); drawPath(ear2, face)
        drawCircle(face, w*.28f, Offset(w*.5f,h*.47f))
        drawOval(Color.White.copy(alpha=.8f), Offset(w*.38f,h*.48f), Size(w*.24f,h*.25f))
        drawCircle(Color.White, w*.055f, Offset(w*.41f,h*.43f)); drawCircle(Color.White, w*.055f, Offset(w*.59f,h*.43f))
        drawCircle(Color(0xFF4A321E), w*.034f, Offset(w*.41f,h*.44f)); drawCircle(Color(0xFF4A321E), w*.034f, Offset(w*.59f,h*.44f))
        drawCircle(Color.White, w*.012f, Offset(w*.398f,h*.425f)); drawCircle(Color.White, w*.012f, Offset(w*.578f,h*.425f))
        drawCircle(Color(0xFFB86F5E), w*.025f, Offset(w*.5f,h*.52f))
        drawArc(Ink, 0f, 180f, false, Offset(w*.45f,h*.515f), Size(w*.1f,h*.08f), style=Stroke(w*.009f))
        drawCircle(Color(0xFFF2B6A6).copy(alpha=.65f), w*.035f, Offset(w*.34f,h*.53f)); drawCircle(Color(0xFFF2B6A6).copy(alpha=.65f), w*.035f, Offset(w*.66f,h*.53f))
        drawOval(face, Offset(w*.31f,h*.66f), Size(w*.38f,h*.22f))
        drawCircle(Color(0xFFFFF7E9), w*.075f, Offset(w*.39f,h*.79f)); drawCircle(Color(0xFFFFF7E9), w*.075f, Offset(w*.61f,h*.79f))
        if (kind == PetKind.Tabby) repeat(3) { i -> drawLine(Color(0xFF705A45), Offset(w*(.43f+i*.035f),h*.23f), Offset(w*(.45f+i*.025f),h*.32f), strokeWidth=w*.014f) }
        if (kind == PetKind.Calico) {
            drawCircle(Color(0xFFD58135), w*.07f, Offset(w*.39f,h*.31f)); drawCircle(Color(0xFFD58135), w*.06f, Offset(w*.64f,h*.48f)); drawCircle(Color.White.copy(alpha=.9f), w*.07f, Offset(w*.55f,h*.27f))
        }
        if (kind == PetKind.Dog) {
            drawOval(Color(0xFFE0C48D), Offset(w*.18f,h*.25f), Size(w*.17f,h*.35f)); drawOval(Color(0xFFE0C48D), Offset(w*.65f,h*.25f), Size(w*.17f,h*.35f)); drawArc(Color(0xFF7EA0B8), 190f, 160f, false, Offset(w*.23f,h*.13f), Size(w*.54f,h*.35f), style=Stroke(w*.04f))
        }
    }
}
