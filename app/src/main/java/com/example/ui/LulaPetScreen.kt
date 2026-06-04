package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareLog
import com.example.data.ChatMessage
import com.example.data.PetStats
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LulaPetScreen(
    viewModel: LulaViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.petStatsFlow.collectAsState()
    val careLogs by viewModel.careLogsFlow.collectAsState()
    val chatMessages by viewModel.chatMessagesFlow.collectAsState()
    val lulaState by viewModel.lulaState.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    var activeTab by remember { mutableStateOf("chat") } // "chat", "safari", "journal"
    var showDialogAnimal by remember { mutableStateOf<String?>(null) } // girraf, zebra, elephant, hippo facts

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    // Warm African savanna style colors
    val savannaGold = Color(0xFFFFB300)
    val savannaSunset = Color(0xFFE65100)
    val savannaGreen = Color(0xFF2E7D32)
    val earthBrown = Color(0xFF7D5260)
    val softSand = Color(0xFFFFFDF6)
    val warmWhite = Color(0xFFFFF9E6)

    // Side effects to scroll to the latest chat message on loading / receiving responses
    LaunchedEffect(chatMessages.size, isChatLoading) {
        if (chatMessages.isNotEmpty()) {
            scrollState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(softSand, warmWhite))),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // 1. CHEERFUL SAFARI BANNER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lula's Safari Care",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = savannaSunset,
                            modifier = Modifier.testTag("app_title")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "🐾", fontSize = 24.sp)
                    }
                    Text(
                        text = "Bond: ${viewModel.bondLevelTitle} (Lvl ${stats.level})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = earthBrown
                    )
                }

                // Header Action Controls
                Row {
                    IconButton(
                        onClick = { viewModel.passSimulationTime() },
                        modifier = Modifier
                            .testTag("time_simulation_button")
                            .shadow(2.dp, CircleShape)
                            .background(savannaGold, CircleShape)
                    ) {
                        Text(text = "🌅", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.resetGame() },
                        modifier = Modifier
                            .testTag("reset_game_button")
                            .shadow(2.dp, CircleShape)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Game State",
                            tint = savannaSunset
                        )
                    }
                }
            }

            // 2. INTERACTIVE LULA CANVAS DISPLAY CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // African savannah background vector inside card
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Savannah sun
                        drawCircle(
                            color = Color(0xFFFFF0B2),
                            radius = 60.dp.toPx(),
                            center = Offset(canvasWidth - 70.dp.toPx(), 40.dp.toPx())
                        )

                        // Rolling green savanna hills
                        val hillPath = Path().apply {
                            moveTo(0f, canvasHeight)
                            quadraticTo(
                                canvasWidth * 0.35f, canvasHeight - 35.dp.toPx(),
                                canvasWidth * 0.70f, canvasHeight - 10.dp.toPx()
                            )
                            quadraticTo(
                                canvasWidth * 0.85f, canvasHeight - 5.dp.toPx(),
                                canvasWidth, canvasHeight - 20.dp.toPx()
                            )
                            lineTo(canvasWidth, canvasHeight)
                            lineTo(0f, canvasHeight)
                            close()
                        }
                        drawPath(hillPath, color = Color(0xFFE8F5E9))

                        val grassPath2 = Path().apply {
                            moveTo(0f, canvasHeight)
                            quadraticTo(
                                canvasWidth * 0.45f, canvasHeight - 15.dp.toPx(),
                                canvasWidth, canvasHeight - 8.dp.toPx()
                            )
                            lineTo(canvasWidth, canvasHeight)
                            lineTo(0f, canvasHeight)
                            close()
                        }
                        drawPath(grassPath2, color = Color(0xFFC8E6C9))
                    }

                    // Cute status indicators showing what she's feeling/acting
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                            .background(
                                color = savannaSunset.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusEmoji = when (lulaState) {
                            LulaState.NORMAL -> "🦁"
                            LulaState.EATING -> "🥩"
                            LulaState.PLAYING -> "⭐"
                            LulaState.SLEEPING -> "💤"
                            LulaState.LOVED -> "💖"
                        }
                        val statusText = when (lulaState) {
                            LulaState.NORMAL -> "Lula is exploring"
                            LulaState.EATING -> "Nom Nom Nom!"
                            LulaState.PLAYING -> "Chasing butterflies!"
                            LulaState.SLEEPING -> "Sleeping sweetly... Zzz"
                            LulaState.LOVED -> "Pride Affection!"
                        }
                        Text(text = "$statusEmoji ", fontSize = 16.sp)
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = savannaSunset
                        )
                    }

                    // THE DYNAMIC DYNAMIC CANVAS FOR LULA
                    LulaCanvasCharacter(
                        state = lulaState,
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.Center)
                    )

                    // Care feedback elements floating
                    FloatingActionOverlay(state = lulaState)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. HORIZONTAL STATS GAUGES
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Lula's Health & Wellness Gauges",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = earthBrown,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Fullness (Hunger)
                        Column(modifier = Modifier.weight(1f).padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🥩 Fullness", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${stats.hunger}%", fontSize = 11.sp, color = savannaSunset, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { stats.hunger / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("fullness_bar"),
                                color = savannaSunset,
                                trackColor = savannaSunset.copy(alpha = 0.15f)
                            )
                        }

                        // Hydration (Thirst)
                        Column(modifier = Modifier.weight(1f).padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💧 Hydration", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${stats.thirst}%", fontSize = 11.sp, color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { stats.thirst / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("thirst_bar"),
                                color = Color(0xFF0288D1),
                                trackColor = Color(0xFF0288D1).copy(alpha = 0.15f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Energy
                        Column(modifier = Modifier.weight(1f).padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "⚡ Energy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${stats.energy}%", fontSize = 11.sp, color = Color(0xFFFF9100), fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { stats.energy / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("energy_bar"),
                                color = Color(0xFFFF9100),
                                trackColor = Color(0xFFFF9100).copy(alpha = 0.15f)
                            )
                        }

                        // Happiness
                        Column(modifier = Modifier.weight(1f).padding(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🎯 Happiness", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${stats.happiness}%", fontSize = 11.sp, color = savannaGreen, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { stats.happiness / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("happiness_bar"),
                                color = savannaGreen,
                                trackColor = savannaGreen.copy(alpha = 0.15f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Bond Affection
                        Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                            val displayPercent = (stats.bond % 200) / 200f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💖 Affection (Bond points: ${stats.bond} pts)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Lvl Progress", fontSize = 11.sp, color = Color(0xFF8E24AA), fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { displayPercent.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("bond_bar"),
                                color = Color(0xFF8E24AA),
                                trackColor = Color(0xFF8E24AA).copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. BIG ACTION BUTTONS (> 48dp)
            Text(
                text = "Interactive Safari Care Actions",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = earthBrown,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FEED BUTTON
                ActionCareButton(
                    label = "Feed Bites",
                    emoji = "🥩",
                    onClick = { viewModel.feedLula() },
                    backgroundColor = savannaSunset.copy(alpha = 0.12f),
                    textColor = savannaSunset,
                    tag = "feed_button"
                )

                // WATER/DRINK BUTTON
                ActionCareButton(
                    label = "Cool Water",
                    emoji = "💧",
                    onClick = { viewModel.hydrateLula() },
                    backgroundColor = Color(0xFF0288D1).copy(alpha = 0.12f),
                    textColor = Color(0xFF0288D1),
                    tag = "drink_button"
                )

                // PLAY BUTTON
                ActionCareButton(
                    label = "Play Chase",
                    emoji = "🦋",
                    onClick = { viewModel.playWithLula() },
                    backgroundColor = savannaGreen.copy(alpha = 0.12f),
                    textColor = savannaGreen,
                    tag = "play_button"
                )

                // SLEEP BUTTON
                ActionCareButton(
                    label = "Baobab Nap",
                    emoji = "💤",
                    onClick = { viewModel.sleepLula() },
                    backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.12f),
                    textColor = Color(0xFF7B1FA2),
                    tag = "sleep_button"
                )

                // PET BUTTON
                ActionCareButton(
                    label = "Scratch Chin",
                    emoji = "💖",
                    onClick = { viewModel.petLula() },
                    backgroundColor = Color(0xFF8E24AA).copy(alpha = 0.12f),
                    textColor = Color(0xFF8E24AA),
                    tag = "pet_button"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. SEAMLESS NAVIGATION TABS (Within Single Screen)
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Modern styled navigation pill
                    TabRow(
                        selectedTabIndex = when (activeTab) {
                            "chat" -> 0
                            "safari" -> 1
                            else -> 2
                        },
                        containerColor = Color.Transparent,
                        contentColor = savannaSunset,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Tab(
                            selected = activeTab == "chat",
                            onClick = { activeTab = "chat" },
                            modifier = Modifier.testTag("tab_button_chat")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💬 Chat with Lula", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Tab(
                            selected = activeTab == "safari",
                            onClick = { activeTab = "safari" },
                            modifier = Modifier.testTag("tab_button_safari")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🦒 Safari Tour", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Tab(
                            selected = activeTab == "journal",
                            onClick = { activeTab = "journal" },
                            modifier = Modifier.testTag("tab_button_journal")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📜 Safari Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // TABS SWAP SPACE
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .background(softSand)
                    ) {
                        when (activeTab) {
                            "chat" -> LulaChatTabContent(
                                viewModel = viewModel,
                                chatMessages = chatMessages,
                                isLoading = isChatLoading,
                                scrollState = scrollState,
                                coroutineScope = coroutineScope
                            )
                            "safari" -> SafariTourTabContent(
                                onAnimalClick = { showDialogAnimal = it }
                            )
                            "journal" -> LocalJournalTabContent(
                                logs = careLogs
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Animal Safari Dialog Panel
    showDialogAnimal?.let { animalId ->
        SafariAnimalDialog(
            animalId = animalId,
            onDismiss = { showDialogAnimal = null }
        )
    }
}

// ================== DETAILED SUB-COMPONENTS ==================

@Composable
fun LulaCanvasCharacter(
    state: LulaState,
    modifier: Modifier = Modifier
) {
    // Dynamic spring-based breathing scale to make her look incredibly organic and alive
    val infiniteTransition = rememberInfiniteTransition(label = "lula_breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Interactive states
    val scope = rememberCoroutineScope()
    val leftEarWiggle = remember { Animatable(0f) }
    val rightEarWiggle = remember { Animatable(0f) }
    val noseBoopScale = remember { Animatable(1f) }
    
    var lulaTalkingBubble by remember { mutableStateOf<String?>(null) }
    var petSparklePosition by remember { mutableStateOf<Offset?>(null) }
    val petSparkleAlpha = remember { Animatable(0f) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .testTag("lula_canvas_pet")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val xDp = with(density) { offset.x.toDp() }
                    val yDp = with(density) { offset.y.toDp() }
                    
                    // Trigger sparkle effect
                    petSparklePosition = offset
                    scope.launch {
                        petSparkleAlpha.snapTo(1f)
                        petSparkleAlpha.animateTo(0f, animationSpec = tween(650))
                    }
                    
                    // Determine what part got tapped
                    // Canvas is 130dp width/height.
                    // cx = 65dp, cy = 69dp.
                    // leftEar center = (29dp, 33dp)
                    // rightEar center = (101dp, 33dp)
                    // nose center = (65dp, 78dp)
                    val distLeftEar = Math.hypot((xDp.value - 29f).toDouble(), (yDp.value - 33f).toDouble())
                    val distRightEar = Math.hypot((xDp.value - 101f).toDouble(), (yDp.value - 33f).toDouble())
                    val distNose = Math.hypot((xDp.value - 65f).toDouble(), (yDp.value - 78f).toDouble())
                    
                    scope.launch {
                        if (distLeftEar < 24) {
                            // Left Ear wiggly!
                            leftEarWiggle.animateTo(18f, animationSpec = tween(90))
                            leftEarWiggle.animateTo(-18f, animationSpec = tween(130))
                            leftEarWiggle.animateTo(0f, animationSpec = tween(90))
                            lulaTalkingBubble = listOf(
                                "Ooh! That tickles! *twitches left ear*",
                                "Is my ear dusty? *giggles*",
                                "*wiggles ears happily* Rawr!",
                                "Ear tickles feel like warm savanna wind!"
                            ).random()
                        } else if (distRightEar < 24) {
                            // Right Ear wiggly!
                            rightEarWiggle.animateTo(-18f, animationSpec = tween(90))
                            rightEarWiggle.animateTo(18f, animationSpec = tween(130))
                            rightEarWiggle.animateTo(0f, animationSpec = tween(90))
                            lulaTalkingBubble = listOf(
                                "Ah! Tickly ear! *wiggles ears*",
                                "Hehehe! Playful pounce! *giggles*",
                                "Is a butterfly landing on my ear?",
                                "Ear wiggles! *pounces*"
                            ).random()
                        } else if (distNose < 18) {
                            // Nose boop!
                            noseBoopScale.animateTo(1.4f, animationSpec = tween(100))
                            noseBoopScale.animateTo(0.7f, animationSpec = tween(100))
                            noseBoopScale.animateTo(1f, animationSpec = tween(80))
                            lulaTalkingBubble = listOf(
                                "BOOP! *little rawr!*",
                                "Ah-choo! Sweet pollen! *giggles*",
                                "My nose is nice and pink! Rawr!",
                                "Hey! That's my hunting nose! *sniffs*"
                            ).random()
                        } else {
                            // Cheek or chin scratch
                            lulaTalkingBubble = listOf(
                                "*happily purrs and licks your fingers*",
                                "You scratch just the right spot! 🦁",
                                "My soft golden cheeks love gentle pets!",
                                "Scratch behind my ears next! 🐾",
                                "Rawrr! You have warm friendly hands!"
                            ).random()
                        }
                        
                        delay(3500)
                        lulaTalkingBubble = null
                    }
                }
            }
            .padding(4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val scaleAdjust = if (state == LulaState.SLEEPING) 0.95f else breathingScale
            val w = size.width * scaleAdjust
            val h = size.height * scaleAdjust
            val cx = size.width / 2f
            val cy = size.height / 2f + 4.dp.toPx() // Lower slightly

            // Savannah gold mane circle padding/shadow
            drawCircle(
                color = Color(0xFFFFB300),
                radius = 50.dp.toPx() * scaleAdjust,
                center = Offset(cx, cy)
            )

            // Ears
            // Left Ear with animated wiggle offset
            val leftEarOffsetX = cx - 36.dp.toPx()
            val leftEarOffsetY = cy - 36.dp.toPx()
            val wiggleLeftRad = Math.toRadians(leftEarWiggle.value.toDouble())
            val wlOffsetX = (Math.sin(wiggleLeftRad) * 6.dp.toPx()).toFloat()
            val wlOffsetY = ((Math.cos(wiggleLeftRad) - 1.0) * 6.dp.toPx()).toFloat()

            drawCircle(
                color = Color(0xFFF57C00),
                radius = 18.dp.toPx() * scaleAdjust,
                center = Offset(leftEarOffsetX + wlOffsetX, leftEarOffsetY + wlOffsetY)
            )
            // Left Ear Inner Pink
            drawCircle(
                color = Color(0xFFFFCDD2),
                radius = 11.dp.toPx() * scaleAdjust,
                center = Offset(leftEarOffsetX + wlOffsetX, leftEarOffsetY + wlOffsetY)
            )

            // Right Ear with animated wiggle offset
            val rightEarOffsetX = cx + 36.dp.toPx()
            val rightEarOffsetY = cy - 36.dp.toPx()
            val wiggleRightRad = Math.toRadians(rightEarWiggle.value.toDouble())
            val wrOffsetX = (Math.sin(wiggleRightRad) * 6.dp.toPx()).toFloat()
            val wrOffsetY = ((Math.cos(wiggleRightRad) - 1.0) * 6.dp.toPx()).toFloat()

            drawCircle(
                color = Color(0xFFF57C00),
                radius = 18.dp.toPx() * scaleAdjust,
                center = Offset(rightEarOffsetX + wrOffsetX, rightEarOffsetY + wrOffsetY)
            )
            // Right Ear Inner Pink
            drawCircle(
                color = Color(0xFFFFCDD2),
                radius = 11.dp.toPx() * scaleAdjust,
                center = Offset(rightEarOffsetX + wrOffsetX, rightEarOffsetY + wrOffsetY)
            )

            // Main Face Cheek fluff
            drawCircle(
                color = Color(0xFFFFCC80),
                radius = 42.dp.toPx() * scaleAdjust,
                center = Offset(cx, cy)
            )

            // EXPRESSIVE EYES BASED ON LIFE STATUS
            when (state) {
                LulaState.SLEEPING -> {
                    // Closed sleepy eyes (crescent arcs)
                    drawArc(
                        color = Color(0xFF5D4037),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(cx - 24.dp.toPx(), cy - 14.dp.toPx()),
                        size = Size(14.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawArc(
                        color = Color(0xFF5D4037),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(cx + 10.dp.toPx(), cy - 14.dp.toPx()),
                        size = Size(14.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                LulaState.LOVED -> {
                    // Heart Eyes! (Drew gorgeous simple heart shapes)
                    drawHeartEye(cx - 16.dp.toPx(), cy - 11.dp.toPx(), 11.dp.toPx())
                    drawHeartEye(cx + 16.dp.toPx(), cy - 11.dp.toPx(), 11.dp.toPx())
                }
                else -> {
                    // Standard / Eating / Playing Expressive big golden-brown eyes
                    drawCircle(
                        color = Color(0xFF5D4037), // Dark chocolate iris
                        radius = 8.dp.toPx(),
                        center = Offset(cx - 16.dp.toPx(), cy - 8.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF8D6E63), // Light iris detail
                        radius = 4.dp.toPx(),
                        center = Offset(cx - 16.dp.toPx(), cy - 8.dp.toPx())
                    )
                    // Pupil
                    drawCircle(
                        color = Color.Black,
                        radius = 4.dp.toPx(),
                        center = Offset(cx - 16.dp.toPx(), cy - 8.dp.toPx())
                    )
                    // Eye shine
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(cx - 18.dp.toPx(), cy - 10.dp.toPx())
                    )

                    // Right Eye
                    drawCircle(
                        color = Color(0xFF5D4037),
                        radius = 8.dp.toPx(),
                        center = Offset(cx + 16.dp.toPx(), cy - 8.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFF8D6E63),
                        radius = 4.dp.toPx(),
                        center = Offset(cx + 16.dp.toPx(), cy - 8.dp.toPx())
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 4.dp.toPx(),
                        center = Offset(cx + 16.dp.toPx(), cy - 8.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(cx + 14.dp.toPx(), cy - 10.dp.toPx())
                    )
                }
            }

            // Snout/Muzzle golden-white area
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 15.dp.toPx() * scaleAdjust,
                center = Offset(cx - 7.dp.toPx(), cy + 12.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 15.dp.toPx() * scaleAdjust,
                center = Offset(cx + 7.dp.toPx(), cy + 12.dp.toPx())
            )

            // Nose: Cute pink rounded triangle with boopScale
            val currentNoseScale = noseBoopScale.value
            val nosePath = Path().apply {
                moveTo(cx - 6.dp.toPx() * currentNoseScale, cy + 6.dp.toPx())
                lineTo(cx + 6.dp.toPx() * currentNoseScale, cy + 6.dp.toPx())
                lineTo(cx, cy + (12.dp.toPx() * currentNoseScale))
                close()
            }
            drawPath(nosePath, color = Color(0xFFE91E63))

            // WHISKERS (White lines)
            drawLine(
                color = Color.White,
                start = Offset(cx - 14.dp.toPx(), cy + 10.dp.toPx()),
                end = Offset(cx - 38.dp.toPx(), cy + 8.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(cx - 14.dp.toPx(), cy + 13.dp.toPx()),
                end = Offset(cx - 36.dp.toPx(), cy + 16.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )

            drawLine(
                color = Color.White,
                start = Offset(cx + 14.dp.toPx(), cy + 10.dp.toPx()),
                end = Offset(cx + 38.dp.toPx(), cy + 8.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(cx + 14.dp.toPx(), cy + 13.dp.toPx()),
                end = Offset(cx + 36.dp.toPx(), cy + 16.dp.toPx()),
                strokeWidth = 1.5.dp.toPx()
            )

            // Dynamic smiling mouth
            when (state) {
                LulaState.EATING -> {
                    // Open mastication circle!
                    drawCircle(
                        color = Color(0xFF880E4F),
                        radius = 7.dp.toPx(),
                        center = Offset(cx, cy + 20.dp.toPx())
                    )
                }
                LulaState.SLEEPING -> {
                    // Small relaxed smile
                    drawArc(
                        color = Color(0xFF5D4037),
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(cx - 6.dp.toPx(), cy + 11.dp.toPx()),
                        size = Size(12.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                else -> {
                    // Cheerful happy open mouth/grin
                    val smilePath = Path().apply {
                        moveTo(cx - 8.dp.toPx(), cy + 14.dp.toPx())
                        quadraticTo(cx, cy + 25.dp.toPx(), cx + 8.dp.toPx(), cy + 14.dp.toPx())
                        quadraticTo(cx, cy + 16.dp.toPx(), cx - 8.dp.toPx(), cy + 14.dp.toPx())
                    }
                    drawPath(smilePath, color = Color(0xFFC2185B))
                }
            }

            // TAP SPARKLES / FEEDBACK EFFECTS DRAWN DIRECTLY ON LULA'S CANVAS
            petSparklePosition?.let { pos ->
                val alpha = petSparkleAlpha.value
                if (alpha > 0f) {
                    drawHeartEye(pos.x, pos.y - (14.dp.toPx() * (1f - alpha)), 22.dp.toPx() * alpha)
                }
            }
        }

        // Animated speech bubble above Lula's head
        AnimatedVisibility(
            visible = lulaTalkingBubble != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFB300)),
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = lulaTalkingBubble ?: "",
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Extended draw tool to draw vector heart shapes inside canvas easily
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeartEye(x: Float, y: Float, size: Float) {
    val heartPath = Path().apply {
        // Base coordinate calculations for a standard geometric heart
        moveTo(x, y + size * 0.25f)
        cubicTo(x - size * 0.5f, y - size * 0.25f, x - size * 0.5f, y + size * 0.5f, x, y + size * 0.9f)
        cubicTo(x + size * 0.5f, y + size * 0.5f, x + size * 0.5f, y - size * 0.25f, x, y + size * 0.25f)
        close()
    }
    drawPath(heartPath, color = Color(0xFFD81B60))
}

@Composable
fun BoxScope.FloatingActionOverlay(state: LulaState) {
    val transition = rememberInfiniteTransition(label = "overlay")
    val floatOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floating"
    )

    val fadeOut by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fade"
    )

    when (state) {
        LulaState.EATING -> {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = floatOffset.dp, x = 20.dp)
                    .graphicsLayer(alpha = fadeOut)
            ) {
                Text(text = "🥩❤️", fontSize = 28.sp)
            }
        }
        LulaState.PLAYING -> {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = floatOffset.dp, x = (-30).dp)
                    .graphicsLayer(alpha = fadeOut)
            ) {
                Text(text = "🦋✨", fontSize = 28.sp)
            }
        }
        LulaState.SLEEPING -> {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = floatOffset.dp, x = 30.dp)
                    .graphicsLayer(alpha = fadeOut)
            ) {
                Text(text = "Zzz...", fontSize = 20.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
            }
        }
        LulaState.LOVED -> {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = floatOffset.dp, x = 0.dp)
                    .graphicsLayer(alpha = fadeOut)
            ) {
                Text(text = "💖✨💖", fontSize = 30.sp)
            }
        }
        LulaState.NORMAL -> {}
    }
}

@Composable
fun ActionCareButton(
    label: String,
    emoji: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    tag: String
) {
    Card(
        modifier = Modifier
            .width(82.dp)
            .height(72.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------- TAB 1: AI CHAT COMPANION ----------------

@Composable
fun LulaChatTabContent(
    viewModel: LulaViewModel,
    chatMessages: List<ChatMessage>,
    isLoading: Boolean,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    var rawInputText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val actionCallback = {
        if (rawInputText.isNotBlank()) {
            val toSend = rawInputText
            rawInputText = ""
            viewModel.sendChatMessage(toSend)
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Conversation lists Scroll space
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            state = scrollState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🐾🦁🐾", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lula wants to talk to you!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = "Ask her about her savanna games, what she likes to eat, or her animal buddies!",
                            fontSize = 12.sp,
                            color = Color(0xFF7D5260),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(chatMessages) { message ->
                ChatBubbleRow(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFF0B2), RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Lula is typing a roar... 🐾🐾",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
        }

        // Child Predefined Starter Chips
        Text(
            text = "Tap a sweet question for Lula:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7D5260),
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val questions = listOf(
                "Do a roar! 🦁",
                "What's your favorite game? 🦋",
                "Are you sleepy? 💤",
                "Who is your tallest buddy? 🦒"
            )
            questions.forEach { q ->
                SuggestionChip(
                    onClick = { viewModel.sendChatMessage(q) },
                    label = { Text(text = q, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.White,
                        labelColor = Color(0xFFE65100)
                    ),
                    modifier = Modifier.shadow(0.5.dp, RoundedCornerShape(8.dp))
                )
            }
        }

        // Text inputs send bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = rawInputText,
                onValueChange = { rawInputText = it },
                placeholder = { Text("Ask Lula anything...", fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { actionCallback() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                singleLine = true
            )

            IconButton(
                onClick = { actionCallback() },
                modifier = Modifier
                    .testTag("chat_send_button")
                    .shadow(1.dp, CircleShape)
                    .background(Color(0xFFFFB300), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubbleRow(message: ChatMessage) {
    val isUser = message.sender == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(
                    color = if (isUser) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "Your Explorer" else "Lula the Cub 🐾",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) Color(0xFF2E7D32) else Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = Color.Black
                )
            }
        }
    }
}

// ---------------- TAB 2: SAFARI TOUR & SOUNDBOARD ----------------

@Composable
fun SafariTourTabContent(
    onAnimalClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "Lula's Savanna Animal Buddies",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Meet Lula's tall, striped, and roaring savanna neighbors! Tap to hear their sounds and read facts.",
            fontSize = 12.sp,
            color = Color(0xFF7D5260),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Grid of interactive buddies
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimalSoundCard(
                    name = "Kojo the Giraffe",
                    emoji = "🦒",
                    sound = "Crunch-crunch!",
                    onClick = { onAnimalClick("giraffe") },
                    modifier = Modifier.weight(1f)
                )
                AnimalSoundCard(
                    name = "Zari the Zebra",
                    emoji = "🦓",
                    sound = "Hee-haw-hee!",
                    onClick = { onAnimalClick("zebra") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnimalSoundCard(
                    name = "Tembo the Elephant",
                    emoji = "🐘",
                    sound = "Pawooo-toot!",
                    onClick = { onAnimalClick("elephant") },
                    modifier = Modifier.weight(1f)
                )
                AnimalSoundCard(
                    name = "Maji the Hippo",
                    emoji = "🦛",
                    sound = "Splish-splash!",
                    onClick = { onAnimalClick("hippo") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AnimalSoundCard(
    name: String,
    emoji: String,
    sound: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Sound: $sound",
                fontSize = 10.sp,
                color = Color(0xFF7D5260),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ---------------- TAB 3: LOCAL JOURNAL LOGS ----------------

@Composable
fun LocalJournalTabContent(
    logs: List<CareLog>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "Lula's Savanna Journal",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No care entries written under the sun yet! Go feed or play with Lula. 🌞",
                    fontSize = 12.sp,
                    color = Color(0xFF7D5260),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    JournalItemRow(log = log)
                }
            }
        }
    }
}

@Composable
fun JournalItemRow(log: CareLog) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeStr = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }

    val iconString = when (log.actionType) {
        "FEED" -> "🥩"
        "DRINK" -> "💧"
        "PLAY" -> "🦋"
        "SLEEP" -> "💤"
        "PET" -> "💖"
        else -> "🌟"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFFFF0B2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconString, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.message,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "Savanna Time: $timeStr",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ---------------- SAFARI ANIMAL INFORMATION WINDOW ----------------

@Composable
fun SafariAnimalDialog(
    animalId: String,
    onDismiss: () -> Unit
) {
    val (title, emoji, facts, soundText, lulaOpinion) = when (animalId) {
        "giraffe" -> TupleAnim(
            "Kojo the Giraffe",
            "🦒",
            "Giraffes are the tallest animals on land! They have gorgeous purple tongues that are extremely long, helping them pick yummy sweet leaves off tall acacia trees.",
            "Crunch-snort-crunch!",
            "Lula says: '*rubs toes* Kojo is so extremely tall, sometimes I try to reach up and touch his ankles but I am too tiny!'"
        )
        "zebra" -> TupleAnim(
            "Zari the Zebra",
            "🦓",
            "Zebra stripes are completely unique like human fingerprints! They use their striped patterns to puzzle and throw off lions or other predators during savanna stampedes.",
            "Hee-haw-hee-laugh-whinny!",
            "Lula says: '*twitches nose* Zari is super fast! I tried to play tag once but she ran off in circles, leaving me in a golden dust cloud!'"
        )
        "elephant" -> TupleAnim(
            "Tembo the Elephant",
            "🐘",
            "Elephants have amazing trunks that have over 40,000 muscles! They use their trunk as a hand, a nose, a hose, a straw, and for gentle friendly greetings.",
            "Pawoooon-tooooot!",
            "Lula says: '*swings tail* Tembo is a majestic giant! He sprays the coolest river water on hot afternoons to keep us fresh!'"
        )
        else -> TupleAnim(
            "Maji the Hippo",
            "🦛",
            "Hippos love to spend their sunny hot days in deep muddy rivers! They make their own special sunscreen oil that is natural and shiny pink.",
            "Honk-grunt-splash!",
            "Lula says: '*rolls eyes* Maji yawned so widely, I thought he was trying to swallow the whole river! Mud-bathing is super fun.'"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
        },
        text = {
            Column {
                Text(
                    text = "🔊 Wildlife Sound: $soundText",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = facts,
                    fontSize = 13.sp,
                    color = Color.Black,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = lulaOpinion,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7D5260)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
            ) {
                Text("Close Tour", color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

data class TupleAnim(
    val title: String,
    val emoji: String,
    val facts: String,
    val soundText: String,
    val lulaOpinion: String
)
