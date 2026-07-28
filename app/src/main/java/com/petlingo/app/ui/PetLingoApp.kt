package com.petlingo.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petlingo.app.PetLingoState
import com.petlingo.app.PetLingoViewModel
import com.petlingo.app.R
import com.petlingo.app.data.Word

private enum class Tab(val title: String) {
    HOME("首頁"), WORDS("單字"), READING("閱讀"), SPEAK("口說"), QUIZ("測驗"), FAVORITES("收藏"), MORE("更多")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetLingoApp(vm: PetLingoViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.HOME) }

    val light = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFFE95D75),
        primaryContainer = androidx.compose.ui.graphics.Color(0xFFFFD9DE),
        secondary = androidx.compose.ui.graphics.Color(0xFF65A987),
        secondaryContainer = androidx.compose.ui.graphics.Color(0xFFDDF5E8),
        tertiary = androidx.compose.ui.graphics.Color(0xFFE9B949),
        tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFFFEDB6),
        background = androidx.compose.ui.graphics.Color(0xFFFFF9F2),
        surface = androidx.compose.ui.graphics.Color(0xFFFFFCF8)
    )

    MaterialTheme(colorScheme = if (state.darkMode) darkColorScheme() else light) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("PetLingo", fontWeight = FontWeight.ExtraBold)
                            Text("全新專案・三隻寵物完整顯示", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = { vm.setDarkMode(!state.darkMode) }) {
                            Icon(if (state.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode, "深色模式")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    val icons = listOf(
                        Icons.Default.Home, Icons.Default.MenuBook, Icons.Default.Article,
                        Icons.Default.Mic, Icons.Default.Quiz, Icons.Default.Star, Icons.Default.MoreHoriz
                    )
                    Tab.entries.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(icons[index], item.title) },
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (state.loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    when (tab) {
                        Tab.HOME -> HomeScreen(state)
                        Tab.WORDS -> WordListScreen(state, vm)
                        Tab.READING -> ReadingScreen()
                        Tab.SPEAK -> SpeakingScreen(vm)
                        Tab.QUIZ -> QuizScreen(state.words, vm)
                        Tab.FAVORITES -> FavoritesScreen(state, vm)
                        Tab.MORE -> MoreScreen(state)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(state: PetLingoState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(28.dp)) {
                Image(
                    painter = painterResource(R.drawable.petlingo_hero),
                    contentDescription = "虎斑貓、長毛吉娃娃與玳瑁貓",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(976f / 474f),
                    contentScale = ContentScale.Fit
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("已學", "${state.studied.size}", Modifier.weight(1f))
                StatCard("正確率", "${state.accuracy}%", Modifier.weight(1f))
                StatCard("收藏", "${state.favorites.size}", Modifier.weight(1f))
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.chihuahua),
                            null,
                            Modifier.size(72.dp).clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("寵物等級 Lv.${state.level}", fontWeight = FontWeight.ExtraBold)
                            Text("${state.xp} XP")
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (state.xp % 250) / 250f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("每日挑戰", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("○ 學習 10 個單字")
                    Text("○ 完成 5 題單字測驗")
                    Text("○ 完成 1 篇閱讀")
                    Text("○ 完成 1 次口說")
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(20.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun WordListScreen(state: PetLingoState, vm: PetLingoViewModel) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, state.words) {
        if (query.isBlank()) state.words
        else state.words.filter { it.english.contains(query, true) || it.chinese.contains(query) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("搜尋英文或中文") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
        LazyColumn(
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered.take(500), key = { it.id }) { word ->
                WordCard(word, word.id in state.favorites, vm)
            }
        }
    }
}

@Composable
private fun WordCard(word: Word, favorite: Boolean, vm: PetLingoViewModel) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.tabby),
                null,
                Modifier.size(58.dp).clip(CircleShape),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(word.english, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(word.chinese)
            }
            IconButton(onClick = { vm.speak(word.english); vm.markStudied(word.id) }) {
                Icon(Icons.Default.VolumeUp, "播放")
            }
            IconButton(onClick = { vm.toggleFavorite(word.id) }) {
                Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, "收藏")
            }
        }
    }
}

@Composable
private fun ReadingScreen() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Image(
                painterResource(R.drawable.tortoiseshell),
                null,
                Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Fit
            )
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("TOEIC 閱讀練習", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("The customer-service workshop will begin at 9:30 a.m. Please arrive early and bring the workbook distributed last week.")
                    Text("客服研習將於上午 9:30 開始，請提早抵達並攜帶上週發放的講義。")
                }
            }
        }
    }
}

@Composable
private fun SpeakingScreen(vm: PetLingoViewModel) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painterResource(R.drawable.chihuahua),
            null,
            Modifier.size(230.dp),
            contentScale = ContentScale.Fit
        )
        Text("I will do my best.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("我會盡我的全力。")
        Button(onClick = { vm.speak("I will do my best.") }) {
            Icon(Icons.Default.VolumeUp, null)
            Spacer(Modifier.width(8.dp))
            Text("播放示範")
        }
    }
}

@Composable
private fun QuizScreen(words: List<Word>, vm: PetLingoViewModel) {
    var current by remember(words) { mutableStateOf(words.randomOrNull()) }
    var options by remember(current) {
        mutableStateOf(
            current?.let { target ->
                (words.filter { it.id != target.id }.shuffled().take(3) + target).shuffled()
            }.orEmpty()
        )
    }
    var answered by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("選出正確中文意思") }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        current?.let { word ->
            item {
                Image(
                    painterResource(R.drawable.tabby),
                    null,
                    Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Fit
                )
            }
            item {
                Text(word.english, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(message)
            }
            items(options) { option ->
                OutlinedButton(
                    onClick = {
                        if (!answered) {
                            val correct = option.id == word.id
                            vm.recordAnswer(word, correct)
                            message = if (correct) "答對了！" else "答案是：${word.chinese}"
                            answered = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(option.chinese)
                }
            }
            item {
                Button(
                    onClick = {
                        current = words.randomOrNull()
                        options = current?.let { target ->
                            (words.filter { it.id != target.id }.shuffled().take(3) + target).shuffled()
                        }.orEmpty()
                        answered = false
                        message = "選出正確中文意思"
                    },
                    enabled = answered
                ) { Text("下一題") }
            }
        }
    }
}

@Composable
private fun FavoritesScreen(state: PetLingoState, vm: PetLingoViewModel) {
    val favorites = state.words.filter { it.id in state.favorites }
    if (favorites.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(painterResource(R.drawable.tortoiseshell), null, Modifier.size(220.dp), contentScale = ContentScale.Fit)
            Text("還沒有收藏喔！", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(favorites, key = { it.id }) { WordCard(it, true, vm) }
        }
    }
}

@Composable
private fun MoreScreen(state: PetLingoState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Image(
                painterResource(R.drawable.petlingo_hero),
                null,
                Modifier.fillMaxWidth().aspectRatio(976f / 474f),
                contentScale = ContentScale.Fit
            )
        }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("全新 PetLingo 專案", fontWeight = FontWeight.ExtraBold)
                    Text("學習資料：${state.words.size} 筆")
                    Text("這個專案不再沿用舊 UI 圖片或舊裁切邏輯。")
                    Text("首頁只使用一張完整三寵物 Hero 圖，並固定以 Fit 顯示。")
                }
            }
        }
    }
}
