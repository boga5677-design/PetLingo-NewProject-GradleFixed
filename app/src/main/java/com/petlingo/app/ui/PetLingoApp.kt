package com.petlingo.app.ui

import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petlingo.app.PetLingoViewModel
import com.petlingo.app.R
import com.petlingo.app.data.Word
import com.petlingo.app.data.WordRepository
import java.util.Locale

data class KidsWord(
    val category: String,
    val emoji: String,
    val english: String,
    val chinese: String
)

private val kidsWords = listOf(
    KidsWord("數字", "1️⃣", "One", "一"),
    KidsWord("數字", "2️⃣", "Two", "二"),
    KidsWord("數字", "3️⃣", "Three", "三"),
    KidsWord("數字", "4️⃣", "Four", "四"),
    KidsWord("數字", "5️⃣", "Five", "五"),
    KidsWord("數字", "6️⃣", "Six", "六"),
    KidsWord("數字", "7️⃣", "Seven", "七"),
    KidsWord("數字", "8️⃣", "Eight", "八"),
    KidsWord("數字", "9️⃣", "Nine", "九"),
    KidsWord("數字", "🔟", "Ten", "十"),
    KidsWord("顏色", "🔴", "Red", "紅色"),
    KidsWord("顏色", "🔵", "Blue", "藍色"),
    KidsWord("顏色", "🟡", "Yellow", "黃色"),
    KidsWord("顏色", "🟢", "Green", "綠色"),
    KidsWord("動物", "🐶", "Dog", "狗"),
    KidsWord("動物", "🐱", "Cat", "貓"),
    KidsWord("動物", "🐰", "Rabbit", "兔子"),
    KidsWord("動物", "🐻", "Bear", "熊"),
    KidsWord("動物", "🐦", "Bird", "鳥"),
    KidsWord("動物", "🐟", "Fish", "魚"),
    KidsWord("食物", "🍎", "Apple", "蘋果"),
    KidsWord("食物", "🍌", "Banana", "香蕉"),
    KidsWord("食物", "🍞", "Bread", "麵包"),
    KidsWord("食物", "🥚", "Egg", "蛋"),
    KidsWord("食物", "🥛", "Milk", "牛奶"),
    KidsWord("食物", "🍚", "Rice", "飯"),
    KidsWord("生活用品", "🪥", "Toothbrush", "牙刷"),
    KidsWord("生活用品", "🧼", "Soap", "肥皂"),
    KidsWord("生活用品", "🥄", "Spoon", "湯匙"),
    KidsWord("生活用品", "🥤", "Cup", "杯子"),
    KidsWord("生活用品", "🪑", "Chair", "椅子"),
    KidsWord("生活用品", "🛏️", "Bed", "床"),
    KidsWord("生活用品", "🚪", "Door", "門"),
    KidsWord("生活用品", "💡", "Light", "燈"),
    KidsWord("生活用品", "📕", "Book", "書"),
    KidsWord("生活用品", "✏️", "Pencil", "鉛筆"),
    KidsWord("交通工具", "🚗", "Car", "汽車"),
    KidsWord("交通工具", "🚌", "Bus", "公車"),
    KidsWord("交通工具", "🚲", "Bicycle", "腳踏車"),
    KidsWord("交通工具", "✈️", "Airplane", "飛機"),
    KidsWord("身體", "👀", "Eyes", "眼睛"),
    KidsWord("身體", "👂", "Ear", "耳朵"),
    KidsWord("身體", "👃", "Nose", "鼻子"),
    KidsWord("身體", "👄", "Mouth", "嘴巴"),
    KidsWord("家人", "👩", "Mom", "媽媽"),
    KidsWord("家人", "👨", "Dad", "爸爸"),
    KidsWord("家人", "👦", "Brother", "哥哥／弟弟"),
    KidsWord("家人", "👧", "Sister", "姊姊／妹妹")
)

private enum class Page { HOME, LEVELS, LEARN, QUIZ, WORDS, ACHIEVEMENTS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetLingoApp(vm: PetLingoViewModel) {
    val context = LocalContext.current
    var ttsReady by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        }
    }
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }

    DisposableEffect(Unit) {
        tts.language = Locale.US
        tts.setSpeechRate(0.72f)
        tts.setPitch(1.08f)
        onDispose {
            tts.stop()
            tts.shutdown()
            tone.release()
        }
    }

    fun speak(text: String) {
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "petlingo-kids-v3")
    }

    fun successSound() {
        tone.startTone(ToneGenerator.TONE_PROP_ACK, 180)
    }

    var page by remember { mutableStateOf(Page.HOME) }
    var selectedCategory by remember { mutableStateOf("全部") }
    var selectedLevel by remember { mutableIntStateOf(1) }
    var unlockedLevel by remember { mutableIntStateOf(1) }
    var stars by remember { mutableIntStateOf(0) }
    var correctAnswers by remember { mutableIntStateOf(0) }

    val colors = lightColorScheme(
        primary = Color(0xFFFF6B8A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE3EA),
        secondary = Color(0xFF49B98A),
        secondaryContainer = Color(0xFFDDF8EB),
        tertiary = Color(0xFFFFB83E),
        tertiaryContainer = Color(0xFFFFEFC8),
        background = Color(0xFFFFFBF2),
        surface = Color.White
    )

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("PetLingo Kids 3.0", fontWeight = FontWeight.Black)
                            Text("和毛孩一起學英文！", style = MaterialTheme.typography.labelMedium)
                        }
                    },
                    navigationIcon = {
                        if (page != Page.HOME) {
                            IconButton(onClick = { page = Page.HOME }) {
                                Icon(Icons.Default.Home, "回首頁")
                            }
                        }
                    },
                    actions = {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Text(
                                "⭐ $stars",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (page) {
                    Page.HOME -> HomeScreen(
                        onLevels = { page = Page.LEVELS },
                        onLearn = {
                            selectedCategory = "全部"
                            page = Page.LEARN
                        },
                        onWords = { page = Page.WORDS },
                        onAchievements = { page = Page.ACHIEVEMENTS }
                    )
                    Page.LEVELS -> LevelMap(
                        unlockedLevel = unlockedLevel,
                        onLevel = {
                            selectedLevel = it
                            page = Page.QUIZ
                        }
                    )
                    Page.LEARN -> LearningScreen(
                        words = if (selectedCategory == "全部") kidsWords
                        else kidsWords.filter { it.category == selectedCategory },
                        speak = ::speak,
                        onStar = { stars++ },
                        onSuccess = ::successSound
                    )
                    Page.QUIZ -> LevelQuiz(
                        level = selectedLevel,
                        words = levelWords(selectedLevel),
                        speak = ::speak,
                        onCorrect = {
                            stars++
                            correctAnswers++
                            successSound()
                        },
                        onLevelComplete = {
                            if (selectedLevel == unlockedLevel && unlockedLevel < 10) {
                                unlockedLevel++
                            }
                            stars += 3
                            page = Page.LEVELS
                        }
                    )
                    Page.WORDS -> WordLibrary(speak = ::speak)
                    Page.ACHIEVEMENTS -> AchievementsScreen(
                        stars = stars,
                        unlockedLevel = unlockedLevel,
                        correctAnswers = correctAnswers
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    onLevels: () -> Unit,
    onLearn: () -> Unit,
    onWords: () -> Unit,
    onAchievements: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "mascot")
    val bob by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "bob"
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDFF5FF))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "一起開心學英文！",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF315B8A)
                    )
                    Text("玳瑁貓・虎斑貓・長毛吉娃娃陪你闖關", fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth().height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Mascot(R.drawable.tortoiseshell, "玳瑁貓", bob)
                        Mascot(R.drawable.tabby, "虎斑貓", -bob)
                        Mascot(R.drawable.chihuahua, "長毛吉娃娃", bob)
                    }
                }
            }
        }

        item {
            BigMenuButton(
                icon = "🎮",
                title = "開始闖關",
                subtitle = "10 個關卡，答對就有星星",
                container = Color(0xFFFFC34D),
                onClick = onLevels
            )
        }
        item {
            BigMenuButton(
                icon = "📖",
                title = "單字學習",
                subtitle = "大圖卡、英文、中文與發音",
                container = Color(0xFF86D75D),
                onClick = onLearn
            )
        }
        item {
            BigMenuButton(
                icon = "🔤",
                title = "7000+ 單字庫",
                subtitle = "搜尋 TOEIC 進階單字並播放發音",
                container = Color(0xFF68C9F2),
                onClick = onWords
            )
        }
        item {
            BigMenuButton(
                icon = "🏆",
                title = "我的成就",
                subtitle = "查看星星、答對題數與闖關進度",
                container = Color(0xFFC9A1F2),
                onClick = onAchievements
            )
        }
    }
}

@Composable
private fun Mascot(drawable: Int, label: String, offset: Float) {
    Column(
        Modifier.width(104.dp).graphicsLayer { translationY = offset },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(drawable),
            contentDescription = label,
            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Crop
        )
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BigMenuButton(
    icon: String,
    title: String,
    subtitle: String,
    container: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 52.sp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(subtitle, fontSize = 15.sp)
            }
            Icon(Icons.Default.ChevronRight, null, Modifier.size(36.dp))
        }
    }
}

@Composable
private fun LevelMap(unlockedLevel: Int, onLevel: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF5FF))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("闖關地圖", fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("完成每關 5 題，就能解鎖下一關")
                }
            }
        }

        items((1..10).toList()) { level ->
            val unlocked = level <= unlockedLevel
            val completed = level < unlockedLevel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = unlocked) { onLevel(level) },
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        completed -> Color(0xFFDDF8EB)
                        unlocked -> Color(0xFFFFEFC8)
                        else -> Color(0xFFE7E7E7)
                    }
                )
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(58.dp),
                        shape = CircleShape,
                        color = if (unlocked) Color(0xFFFFB83E) else Color(0xFFAAAAAA)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (unlocked) {
                                Text("$level", fontSize = 25.sp, fontWeight = FontWeight.Black)
                            } else {
                                Icon(Icons.Default.Lock, "尚未解鎖")
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("第 $level 關", fontSize = 23.sp, fontWeight = FontWeight.Black)
                        Text(if (completed) "完成！⭐⭐⭐" else if (unlocked) "5 題聽音選圖" else "先完成上一關")
                    }
                    Icon(
                        if (completed) Icons.Default.Star else Icons.Default.ChevronRight,
                        null,
                        Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

private fun levelWords(level: Int): List<KidsWord> {
    val start = ((level - 1) * 5) % kidsWords.size
    return List(5) { index -> kidsWords[(start + index) % kidsWords.size] }
}

@Composable
private fun LearningScreen(
    words: List<KidsWord>,
    speak: (String) -> Unit,
    onStar: () -> Unit,
    onSuccess: () -> Unit
) {
    var index by remember(words) { mutableIntStateOf(0) }
    var showChinese by remember { mutableStateOf(true) }
    var celebrate by remember { mutableStateOf(false) }
    val word = words[index.coerceIn(words.indices)]

    LaunchedEffect(celebrate) {
        if (celebrate) {
            kotlinx.coroutines.delay(900)
            celebrate = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { (index + 1).toFloat() / words.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text("${index + 1} / ${words.size}", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).clickable { speak(word.english) },
                shape = RoundedCornerShape(34.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(word.emoji, fontSize = 135.sp, textAlign = TextAlign.Center)
                    Text(word.english, fontSize = 44.sp, fontWeight = FontWeight.Black)
                    AnimatedVisibility(showChinese) {
                        Text(word.chinese, fontSize = 29.sp, fontWeight = FontWeight.Bold, color = Color(0xFF33946F))
                    }
                    Spacer(Modifier.height(18.dp))
                    FilledTonalButton(
                        onClick = { speak(word.english) },
                        modifier = Modifier.height(62.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, null, Modifier.size(31.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("聽發音", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { showChinese = !showChinese },
                    modifier = Modifier.weight(1f).height(58.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Translate, null)
                    Spacer(Modifier.width(5.dp))
                    Text(if (showChinese) "隱藏中文" else "顯示中文")
                }
                Button(
                    onClick = {
                        onStar()
                        onSuccess()
                        celebrate = true
                        index = if (index == words.lastIndex) 0 else index + 1
                    },
                    modifier = Modifier.weight(1f).height(58.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("會了 ⭐", fontSize = 19.sp, fontWeight = FontWeight.Black)
                }
            }
            Row {
                IconButton(onClick = { index = if (index == 0) words.lastIndex else index - 1 }) {
                    Icon(Icons.Default.ArrowBack, "上一個", Modifier.size(34.dp))
                }
                Spacer(Modifier.width(34.dp))
                IconButton(onClick = { index = if (index == words.lastIndex) 0 else index + 1 }) {
                    Icon(Icons.Default.ArrowForward, "下一個", Modifier.size(34.dp))
                }
            }
        }

        EncouragementOverlay(show = celebrate, text = "太棒了！")
    }
}

@Composable
private fun LevelQuiz(
    level: Int,
    words: List<KidsWord>,
    speak: (String) -> Unit,
    onCorrect: () -> Unit,
    onLevelComplete: () -> Unit
) {
    var questionIndex by remember(level) { mutableIntStateOf(0) }
    var target by remember(level, questionIndex) { mutableStateOf(words[questionIndex]) }
    var options by remember(level, questionIndex) { mutableStateOf(makeOptions(kidsWords, target)) }
    var message by remember { mutableStateOf("按喇叭，找出正確圖片") }
    var answered by remember { mutableStateOf(false) }
    var celebrate by remember { mutableStateOf(false) }

    LaunchedEffect(level, questionIndex) {
        target = words[questionIndex]
        options = makeOptions(kidsWords, target)
        message = "按喇叭，找出正確圖片"
        answered = false
    }
    LaunchedEffect(celebrate) {
        if (celebrate) {
            kotlinx.coroutines.delay(850)
            celebrate = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("第 $level 關", fontSize = 27.sp, fontWeight = FontWeight.Black)
            LinearProgressIndicator(
                progress = { (questionIndex + 1) / 5f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(10.dp))
            )
            Text("第 ${questionIndex + 1} 題／共 5 題", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(message, fontSize = 19.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { speak(target.english) },
                        modifier = Modifier.size(76.dp),
                        shape = RoundedCornerShape(26.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, "播放發音", Modifier.size(42.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            options.chunked(2).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { item ->
                        Card(
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable(enabled = !answered) {
                                if (item == target) {
                                    message = "答對了！Good job! ⭐"
                                    answered = true
                                    celebrate = true
                                    onCorrect()
                                    speak(target.english)
                                } else {
                                    message = "再想一想，加油！"
                                }
                            },
                            shape = RoundedCornerShape(25.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(item.emoji, fontSize = 72.sp)
                                Text(item.chinese, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            if (answered) {
                Button(
                    onClick = {
                        if (questionIndex == 4) onLevelComplete()
                        else questionIndex++
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(if (questionIndex == 4) "完成關卡 🎉" else "下一題", fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }

        EncouragementOverlay(show = celebrate, text = listOf("太棒了！", "你好厲害！", "答對了！").random())
    }
}

@Composable
private fun EncouragementOverlay(show: Boolean, text: String) {
    val scale by animateFloatAsState(if (show) 1f else 0.65f, label = "celebrateScale")
    AnimatedVisibility(show) {
        Box(
            Modifier.fillMaxSize().background(Color(0x88000000)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(28.dp).graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2B8))
            ) {
                Column(
                    Modifier.padding(30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉 ⭐ 🎊", fontSize = 48.sp)
                    Text(text, fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFE45272))
                    Text("繼續加油喔！", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Row(Modifier.height(100.dp)) {
                        Image(painterResource(R.drawable.tortoiseshell), null, Modifier.size(90.dp), contentScale = ContentScale.Crop)
                        Image(painterResource(R.drawable.tabby), null, Modifier.size(90.dp), contentScale = ContentScale.Crop)
                        Image(painterResource(R.drawable.chihuahua), null, Modifier.size(90.dp), contentScale = ContentScale.Crop)
                    }
                }
            }
        }
    }
}

private fun makeOptions(words: List<KidsWord>, target: KidsWord): List<KidsWord> =
    (words.filter { it != target }.shuffled().take(3) + target).shuffled()

@Composable
private fun WordLibrary(speak: (String) -> Unit) {
    val context = LocalContext.current
    var allWords by remember { mutableStateOf<List<Word>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        allWords = WordRepository.load(context)
    }

    val shown = remember(allWords, query) {
        if (query.isBlank()) allWords.take(100)
        else allWords.filter {
            it.english.contains(query, ignoreCase = true) || it.chinese.contains(query)
        }.take(200)
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            label = { Text("搜尋 7000+ 英文或中文") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(22.dp)
        )
        Text(
            if (query.isBlank()) "顯示前 100 筆，共 ${allWords.size} 筆" else "搜尋到 ${shown.size} 筆",
            modifier = Modifier.padding(horizontal = 18.dp),
            fontWeight = FontWeight.Bold
        )
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(shown, key = { it.id }) { word ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { speak(word.english) },
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(
                                "${word.id}",
                                modifier = Modifier.padding(11.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                word.english,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(word.chinese, fontSize = 17.sp)
                        }
                        IconButton(onClick = { speak(word.english) }) {
                            Icon(Icons.Default.VolumeUp, "發音")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsScreen(stars: Int, unlockedLevel: Int, correctAnswers: Int) {
    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.navigationBarsPadding()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8D8FF))
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.EmojiEvents, null, Modifier.size(72.dp), tint = Color(0xFFFFA900))
                    Text("我的成就", fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Text("總星星數 ⭐ $stars", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { AchievementCard("🐾", "學習小高手", "$correctAnswers / 10", correctAnswers >= 10) }
        item { AchievementCard("📖", "單字達人", "$correctAnswers / 50", correctAnswers >= 50) }
        item { AchievementCard("👑", "闖關王者", "${unlockedLevel - 1} / 10", unlockedLevel > 10) }
        item { AchievementCard("⭐", "星星收藏家", "$stars / 100", stars >= 100) }
    }
}

@Composable
private fun AchievementCard(icon: String, title: String, progress: String, achieved: Boolean) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achieved) Color(0xFFFFEFC8) else Color.White
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 52.sp)
            Spacer(Modifier.width(15.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(progress, fontSize = 18.sp)
            }
            Text(if (achieved) "完成！" else "繼續加油", fontWeight = FontWeight.Bold)
        }
    }
}
