package com.petlingo.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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

private val Paper = Color(0xFFFFFDF8)
private val PaperCard = Color(0xFFFFFEFB)
private val Moss = Color(0xFF899B5B)
private val PaleMoss = Color(0xFFEFF2DF)
private val Clay = Color(0xFFC98F61)
private val PaleClay = Color(0xFFF8EEE3)
private val Ink = Color(0xFF2D2A27)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetLingoApp(vm: PetLingoViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.HOME) }

    val light = lightColorScheme(
        primary = Moss,
        onPrimary = Color.White,
        primaryContainer = PaleMoss,
        onPrimaryContainer = Ink,
        secondary = Clay,
        secondaryContainer = PaleClay,
        tertiary = Color(0xFFD2B36B),
        tertiaryContainer = Color(0xFFF8F1D8),
        background = Paper,
        surface = PaperCard,
        surfaceVariant = Color(0xFFF6F2E9),
        onBackground = Ink,
        onSurface = Ink,
        outline = Color(0xFFD7CFBF)
    )

    MaterialTheme(colorScheme = if (state.darkMode) darkColorScheme() else light) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Column {
                            Text("🐾 PetLingo", fontWeight = FontWeight.ExtraBold)
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
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val icons = listOf(Icons.Default.Home, Icons.Default.MenuBook, Icons.Default.Article, Icons.Default.Mic, Icons.Default.Quiz, Icons.Default.Star, Icons.Default.MoreHoriz)
                    Tab.entries.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(icons[index], item.title) },
                            label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = PaleMoss)
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
                if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else when (tab) {
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

@Composable
private fun WatercolorImage(resId: Int, description: String?, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(resId),
        contentDescription = description,
        modifier = modifier.clip(RoundedCornerShape(28.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun HomeScreen(state: PetLingoState) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { WatercolorImage(R.drawable.petlingo_hero_soft, "三隻寵物", Modifier.fillMaxWidth().aspectRatio(976f / 474f)) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("已學", "${state.studied.size}", Modifier.weight(1f))
                StatCard("正確率", "${state.accuracy}%", Modifier.weight(1f))
                StatCard("收藏", "${state.favorites.size}", Modifier.weight(1f))
            }
        }
        item {
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WatercolorImage(R.drawable.chihuahua_soft, null, Modifier.size(84.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("寵物等級 Lv.${state.level}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        Text("${state.xp} XP", color = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { (state.xp % 250) / 250f }, modifier = Modifier.fillMaxWidth(), color = Moss, trackColor = PaleMoss)
                    }
                }
            }
        }
        item {
            SoftCard(container = Color(0xFFFBF7EA)) {
                Text("每日挑戰", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("○ 學習 10 個單字\n○ 完成 5 題單字測驗\n○ 完成 1 篇閱讀\n○ 完成 1 次口說", lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2)
            }
        }
    }
}

@Composable
private fun SoftCard(container: Color = PaperCard, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = container), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), content = content)
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PaperCard)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Moss)
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun WordListScreen(state: PetLingoState, vm: PetLingoViewModel) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, state.words) { if (query.isBlank()) state.words else state.words.filter { it.english.contains(query, true) || it.chinese.contains(query) } }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("搜尋英文或中文") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(20.dp))
        LazyColumn(contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered.take(500), key = { it.id }) { WordCard(it, it.id in state.favorites, vm) }
        }
    }
}

@Composable
private fun WordCard(word: Word, favorite: Boolean, vm: PetLingoViewModel) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = PaperCard)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            WatercolorImage(R.drawable.tabby_soft, null, Modifier.size(64.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(word.english, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(word.chinese, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { vm.speak(word.english); vm.markStudied(word.id) }) { Icon(Icons.Default.VolumeUp, "播放", tint = Clay) }
            IconButton(onClick = { vm.toggleFavorite(word.id) }) { Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, "收藏", tint = Moss) }
        }
    }
}

@Composable
private fun ReadingScreen() {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { WatercolorImage(R.drawable.tortoiseshell_soft, null, Modifier.fillMaxWidth().aspectRatio(1.25f)) }
        item {
            SoftCard {
                Text("📖 TOEIC 閱讀練習", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                Text("The customer-service workshop will begin at 9:30 a.m. Please arrive early and bring the workbook distributed last week.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))
                Text("客服研習將於上午 9:30 開始，請提早抵達並攜帶上週發放的講義。", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun SpeakingScreen(vm: PetLingoViewModel) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        WatercolorImage(R.drawable.chihuahua_soft, null, Modifier.fillMaxWidth().weight(1f, fill = false).aspectRatio(1f))
        Text("I will do my best.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Text("我會盡我的全力。", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { vm.speak("I will do my best.") }, shape = RoundedCornerShape(22.dp), colors = ButtonDefaults.buttonColors(containerColor = Moss), contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)) {
            Icon(Icons.Default.VolumeUp, null); Spacer(Modifier.width(8.dp)); Text("播放示範")
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun QuizScreen(words: List<Word>, vm: PetLingoViewModel) {
    var current by remember(words) { mutableStateOf(words.randomOrNull()) }
    var options by remember(current) { mutableStateOf(current?.let { target -> (words.filter { it.id != target.id }.shuffled().take(3) + target).shuffled() }.orEmpty()) }
    var answered by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("選出正確中文意思") }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        current?.let { word ->
            item { WatercolorImage(R.drawable.tabby_soft, null, Modifier.fillMaxWidth().aspectRatio(1.25f)) }
            item { Text(word.english, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold); Text(message) }
            items(options) { option ->
                OutlinedButton(onClick = { if (!answered) { val correct = option.id == word.id; vm.recordAnswer(word, correct); message = if (correct) "答對了！" else "答案是：${word.chinese}"; answered = true } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), contentPadding = PaddingValues(16.dp)) { Text(option.chinese) }
            }
            item {
                Button(onClick = { current = words.randomOrNull(); options = current?.let { target -> (words.filter { it.id != target.id }.shuffled().take(3) + target).shuffled() }.orEmpty(); answered = false; message = "選出正確中文意思" }, enabled = answered, shape = RoundedCornerShape(22.dp)) { Text("下一題") }
            }
        }
    }
}

@Composable
private fun FavoritesScreen(state: PetLingoState, vm: PetLingoViewModel) {
    val favorites = state.words.filter { it.id in state.favorites }
    if (favorites.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            WatercolorImage(R.drawable.tortoiseshell_soft, null, Modifier.size(250.dp))
            Spacer(Modifier.height(14.dp))
            Text("還沒有收藏喔！", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    } else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(favorites, key = { it.id }) { WordCard(it, true, vm) } }
}

@Composable
private fun MoreScreen(state: PetLingoState) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { WatercolorImage(R.drawable.petlingo_hero_soft, null, Modifier.fillMaxWidth().aspectRatio(976f / 474f)) }
        item {
            SoftCard {
                Text("PetLingo 水彩繪本版", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                Text("學習資料：${state.words.size} 筆")
                Text("背景已淡化，圖片使用完整比例顯示，不再以裁切方式填滿畫面。")
                Text("卡片、按鈕與導覽列改為柔和米白、鼠尾草綠及淡棕色系。")
            }
        }
    }
}
