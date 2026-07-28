package com.petlingo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petlingo.app.PetLingoState
import com.petlingo.app.PetLingoViewModel
import com.petlingo.app.data.Accent
import com.petlingo.app.data.StudyRecord
import com.petlingo.app.data.Word
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Tab(val label: String) { HOME("首頁"), WORDS("單字"), QUIZ("測驗"), SPEAK("口說"), RECORDS("紀錄") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetLingoApp(vm: PetLingoViewModel, onSpeak: (String, Accent) -> Unit, onStartListening: (Accent) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    val scheme = lightColorScheme(
        primary = Color(0xFF8C6755), secondary = Color(0xFF78927B), tertiary = Color(0xFFD59B72),
        background = Color(0xFFFFFCF7), surface = Color(0xFFFFFDF9), surfaceVariant = Color(0xFFF4EEE5)
    )
    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { TopAppBar(title = { Text("PetLingo", fontWeight = FontWeight.ExtraBold) }, actions = { AccentMenu(state.accent, vm::setAccent) }) },
            bottomBar = {
                NavigationBar {
                    val icons = listOf(Icons.Default.Home, Icons.Default.MenuBook, Icons.Default.Quiz, Icons.Default.Mic, Icons.Default.History)
                    Tab.entries.forEachIndexed { i, item -> NavigationBarItem(selected = tab == item, onClick = { tab = item }, icon = { Icon(icons[i], item.label) }, label = { Text(item.label) }) }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center)) else when (tab) {
                    Tab.HOME -> HomeScreen(state)
                    Tab.WORDS -> WordsScreen(state, vm, onSpeak)
                    Tab.QUIZ -> QuizScreen(state, vm, onSpeak)
                    Tab.SPEAK -> SpeakScreen(state, vm, onSpeak, onStartListening)
                    Tab.RECORDS -> RecordsScreen(state, vm)
                }
            }
        }
    }
}

@Composable private fun AccentMenu(accent: Accent, onChange: (Accent) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) { Text(if (accent == Accent.US) "🇺🇸 美式" else "🇬🇧 英式") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Accent.entries.forEach { item -> DropdownMenuItem(text = { Text(if (item == Accent.US) "🇺🇸 美式發音" else "🇬🇧 英式發音") }, onClick = { onChange(item); open = false }) }
        }
    }
}

@Composable private fun HomeScreen(state: PetLingoState) {
    val today = state.records.filter { isToday(it.timeMillis) }
    val average = today.map { it.score }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
    val wrong = state.records.count { !it.correct }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { HeroCard() }
        item { Text("今日學習概況", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("練習", "${today.size} 次", Modifier.weight(1f)); StatCard("平均", "$average 分", Modifier.weight(1f)); StatCard("錯題", "$wrong 題", Modifier.weight(1f))
            }
        }
        item { Text("功能完整：詳細學習紀錄、錯題本、即時答題提示、口說評分，以及英式／美式發音。", style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable private fun HeroCard() {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF6EFE5))) {
        Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🐱  🐶  🐈", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp)); Text("和 PetLingo 一起學英文", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("柔和繪本風・每天一點點", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun StatCard(title: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Bold); Text(title, style = MaterialTheme.typography.labelMedium) } }
}

@Composable private fun WordsScreen(state: PetLingoState, vm: PetLingoViewModel, onSpeak: (String, Accent) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val words = remember(state.words, query) { state.words.filter { it.english.contains(query, true) || it.chinese.contains(query) }.take(80) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("搜尋單字") }, leadingIcon = { Icon(Icons.Default.Search, null) })
        Spacer(Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(words, key = { it.id }) { word ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(word.english, fontWeight = FontWeight.Bold); Text(word.chinese) }
                        IconButton(onClick = { onSpeak(word.english, state.accent) }) { Icon(Icons.Default.VolumeUp, "播放") }
                        IconButton(onClick = { vm.toggleFavorite(word.id) }) { Icon(if (word.id in state.favorites) Icons.Default.Star else Icons.Default.StarBorder, "收藏") }
                    }
                }
            }
        }
    }
}

@Composable private fun QuizScreen(state: PetLingoState, vm: PetLingoViewModel, onSpeak: (String, Accent) -> Unit) {
    val answer = state.words.firstOrNull { it.id == state.quizIndex }
    if (answer == null) return
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("選出正確中文", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp)); Text(answer.english, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        IconButton(onClick = { onSpeak(answer.english, state.accent) }) { Icon(Icons.Default.VolumeUp, "播放發音") }
        Spacer(Modifier.height(12.dp))
        state.quizOptions.forEach { option ->
            val selected = state.quizSelectedId == option.id
            val isAnswer = option.id == answer.id
            val color = when { !state.quizAnswered -> MaterialTheme.colorScheme.surfaceVariant; isAnswer -> Color(0xFFDDF1DE); selected -> Color(0xFFFFD9D3); else -> MaterialTheme.colorScheme.surfaceVariant }
            Button(onClick = { vm.answerQuiz(option) }, enabled = !state.quizAnswered, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = MaterialTheme.colorScheme.onSurface)) { Text(option.chinese) }
        }
        if (state.quizAnswered) {
            val correct = state.quizSelectedId == answer.id
            Card(colors = CardDefaults.cardColors(containerColor = if (correct) Color(0xFFDDF1DE) else Color(0xFFFFE2DD))) {
                Text(if (correct) "✓ 回答正確！" else "✗ 回答錯誤，正確答案：${answer.chinese}", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp)); Button(onClick = vm::prepareQuiz) { Text("下一題") }
        }
    }
}

@Composable private fun SpeakScreen(state: PetLingoState, vm: PetLingoViewModel, onSpeak: (String, Accent) -> Unit, onStartListening: (Accent) -> Unit) {
    val target = state.speechTarget ?: return
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("口說練習", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Box { OutlinedButton(onClick = { expanded = true }) { Text("練習單字：${target.english}") }; DropdownMenu(expanded, { expanded = false }) { state.words.take(30).forEach { word -> DropdownMenuItem({ Text("${word.english}  ${word.chinese}") }, { vm.chooseSpeechWord(word); expanded = false }) } } }
        Spacer(Modifier.height(22.dp)); Text(target.english, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold); Text(target.chinese)
        Row { IconButton(onClick = { onSpeak(target.english, state.accent) }) { Icon(Icons.Default.VolumeUp, "播放") }; FilledTonalButton(onClick = { onStartListening(state.accent) }) { Icon(Icons.Default.Mic, null); Spacer(Modifier.width(6.dp)); Text("開始錄音") } }
        state.speechScore?.let { score ->
            Spacer(Modifier.height(20.dp)); Text("辨識結果：${state.speechRecognized}"); Text("口說分數 $score / 100", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = { score / 100f }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            Text(when { score >= 90 -> "發音非常接近目標。"; score >= 70 -> "整體清楚，請再注意重音與尾音。"; score >= 50 -> "部分音節正確，建議慢速跟讀。"; else -> "請先聽示範，再分音節練習。" }, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(20.dp)); Text("評分依手機語音辨識結果與目標文字相似度計算，適合日常練習，不等同專業語音學測驗。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun RecordsScreen(state: PetLingoState, vm: PetLingoViewModel) {
    var wrongOnly by rememberSaveable { mutableStateOf(false) }
    val list = if (wrongOnly) state.records.filter { !it.correct } else state.records
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("詳細學習紀錄", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(onClick = vm::clearRecords) { Text("清除") } }
        Row(verticalAlignment = Alignment.CenterVertically) { Switch(wrongOnly, { wrongOnly = it }); Spacer(Modifier.width(8.dp)); Text("只顯示錯題／需加強") }
        if (list.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("尚無紀錄") } else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { items(list, key = { it.id }) { RecordCard(it) } }
    }
}

@Composable private fun RecordCard(record: StudyRecord) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (record.correct) Color(0xFFF1F6EE) else Color(0xFFFFF0ED))) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row { Text(record.type, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("${record.score} 分", fontWeight = FontWeight.Bold) }
            Text(record.question, style = MaterialTheme.typography.titleMedium); Text("你的答案：${record.answer}"); Text("正確答案：${record.correctAnswer}")
            Text(record.detail, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(formatTime(record.timeMillis), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatTime(time: Long): String = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.TAIWAN).format(Date(time))
private fun isToday(time: Long): Boolean = SimpleDateFormat("yyyyMMdd", Locale.TAIWAN).format(Date()) == SimpleDateFormat("yyyyMMdd", Locale.TAIWAN).format(Date(time))
