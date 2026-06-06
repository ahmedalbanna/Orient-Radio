package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.RadioStation
import com.example.ui.RadioViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    // Force Arab RTL Design layout direction for seamless Arabic support
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val stations by viewModel.filteredStations.collectAsStateWithLifecycle()
        val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
        val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val sleepTimeRemaining by viewModel.sleepTimeRemaining.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

        var isPlayerExpanded by remember { mutableStateOf(false) }
        var showSleepTimerMenu by remember { mutableStateOf(false) }

        val categories = listOf("الكل", "قرآن كريم", "طرب وأغاني", "أخبار وسياسة", "منوعات وأخبار", "رياضة")

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OrientDarkBg, Color(0xFF111827))
                    )
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Main Dashboard Content Layout
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Radio,
                                    contentDescription = "الراديو",
                                    tint = OrientGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "راديو الشرق",
                                    color = OrientTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag("search_station_input"),
                        placeholder = {
                            Text(
                                "البحث عن إذاعة أو بلد...",
                                color = OrientTextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث",
                                tint = OrientGold
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "مسح",
                                        tint = OrientTextSecondary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OrientTextPrimary,
                            unfocusedTextColor = OrientTextPrimary,
                            focusedBorderColor = OrientGold,
                            unfocusedBorderColor = OrientSurfaceVariant,
                            focusedContainerColor = OrientSurface,
                            unfocusedContainerColor = OrientSurface
                        )
                    )

                    // Categories Horizontal Scrolling row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) OrientGold else OrientSurface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) OrientGold else OrientSurfaceVariant,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.onCategorySelected(category) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) OrientDarkBg else OrientTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stations Grid / List
                    if (stations.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = "لا يوجد قنوات",
                                    tint = OrientTextSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "لا توجد قنوات تطابق البحث",
                                    color = OrientTextSecondary,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 90.dp) // Provide spacing so mini player doesn't occlude cards
                        ) {
                            items(stations, key = { it.id }) { station ->
                                val isCurrentPlaying = station.id == currentStation?.id
                                StationCardItem(
                                    station = station,
                                    isCurrentPlaying = isCurrentPlaying,
                                    isPlaying = isPlaying && isCurrentPlaying,
                                    onPlayClick = { viewModel.playStation(station) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(station) }
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Bottom Mini-Player Drawer
            AnimatedVisibility(
                visible = currentStation != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Keep clear of system gesture bars
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                currentStation?.let { station ->
                    MiniPlayerPanel(
                        station = station,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        sleepDurationText = formatSleepDuration(sleepTimeRemaining),
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onPanelClick = { isPlayerExpanded = true }
                    )
                }
            }

            // Beautiful Full-Page Media Dashboard Overlay
            AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 400, easing = EaseOutQuart)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 400, easing = EaseInQuart)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                currentStation?.let { station ->
                    FullPlayerOverlay(
                        station = station,
                        isPlaying = isPlaying,
                        isLoading = isLoading,
                        sleepTimeRemaining = sleepTimeRemaining,
                        onClose = { isPlayerExpanded = false },
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onFavoriteToggle = { viewModel.toggleFavorite(station) },
                        onSleepTimerClick = { showSleepTimerMenu = true }
                    )
                }
            }

            // Sleep Timer Duration Picker Dialog
            if (showSleepTimerMenu) {
                SleepTimerDialog(
                    activeTimer = sleepTimeRemaining,
                    onDismiss = { showSleepTimerMenu = false },
                    onSelectPreset = { minutes ->
                        viewModel.startSleepTimer(minutes)
                        showSleepTimerMenu = false
                    },
                    onCancelTimer = {
                        viewModel.cancelSleepTimer()
                        showSleepTimerMenu = false
                    }
                )
            }
        }
    }
}

// ---------------------- SUB-COMPOSABLES ----------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationCardItem(
    station: RadioStation,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val borderColor = if (isCurrentPlaying) OrientGold else Color.Transparent
    val cardBg = if (isCurrentPlaying) OrientSurfaceVariant else OrientSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onPlayClick,
                onLongClick = onFavoriteToggle
            )
            .testTag("station_card_${station.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Banner Art
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                AsyncImage(
                    model = station.imageUrl,
                    contentDescription = station.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCurrentPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlaying) {
                            AnimatedEqualizerBars()
                        } else {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "تشغيل",
                                tint = OrientGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Station Information titles
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    color = if (isCurrentPlaying) OrientGold else OrientTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "الدولة",
                        tint = OrientTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${station.country} • ${station.category}",
                        color = OrientTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Actions (Play trigger + Favorite Heart)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.testTag("favorite_button_${station.id}")
                ) {
                    Icon(
                        imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (station.isFavorite) OrientFavActive else OrientTextSecondary
                    )
                }

                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.testTag("play_button_${station.id}")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                        contentDescription = "تشغيل",
                        tint = OrientGold,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniPlayerPanel(
    station: RadioStation,
    isPlaying: Boolean,
    isLoading: Boolean,
    sleepDurationText: String?,
    onPlayPauseToggle: () -> Unit,
    onPanelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onPanelClick() }
            .testTag("mini_player_surface"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xF11E293B) // Frosted slate-translucent
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded streaming logo
            AsyncImage(
                model = station.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(OrientGold.copy(alpha = 0.1f))
                    .border(1.5.dp, OrientGold, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Subtitles
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    color = OrientGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = OrientGold,
                            modifier = Modifier.size(10.dp),
                            strokeWidth = 1.5.dp
                        )
                        Text(
                            text = "جاري الاتصال بالقناة...",
                            color = OrientTextSecondary,
                            fontSize = 11.sp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OrientMutedGreen)
                        )
                        Text(
                            text = "بث حي ومباشر",
                            color = OrientMutedGreen,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        if (sleepDurationText != null) {
                            Text(
                                text = "⏱️ $sleepDurationText",
                                color = OrientGoldLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Controls on the right side
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier.testTag("mini_play_pause")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = OrientGold,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "تشغيل/إيقاف مؤقت",
                            tint = OrientGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullPlayerOverlay(
    station: RadioStation,
    isPlaying: Boolean,
    isLoading: Boolean,
    sleepTimeRemaining: Long?,
    onClose: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onSleepTimerClick: () -> Unit
) {
    val pulsateScale by rememberInfiniteTransition(label = "").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF020617)) // Rich velvet dark hues
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Back header buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("full_player_close")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "إغلاق",
                    tint = OrientTextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "المشغل الحي",
                color = OrientTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onFavoriteToggle
            ) {
                Icon(
                    imageVector = if (station.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "تفضيل",
                    tint = if (station.isFavorite) OrientFavActive else OrientTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 24.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Elegant glowing centerpiece logo
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(if (isPlaying && !isLoading) pulsateScale else 1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(OrientSurface)
                        .border(
                            width = 2.5.dp,
                            color = if (isPlaying) OrientGold else OrientSurfaceVariant,
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = station.imageUrl,
                        contentDescription = "Cover Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay blur or glow during loading
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = OrientGold,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.5.dp
                            )
                        }
                    }
                }
            }

            // Radio Channel metadata titles
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = station.name,
                    color = OrientGold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = OrientTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${station.country} • ${station.category}",
                        color = OrientTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful Equalizer Waveform display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying && !isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(8) { index ->
                                SoundwavePill(delayMs = index * 150)
                            }
                        }
                    } else {
                        Text(
                            text = if (isLoading) "جاري تعبئة البث الحي..." else "البث المباشر متوقف",
                            color = if (isLoading) OrientGoldLight else OrientTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
            }

            // Controllers & Utilities (Sleep timer triggers & Play Buttons)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Sleep Timer state message/button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(OrientSurface)
                        .clickable { onSleepTimerClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "مؤقت النوم",
                            tint = OrientGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (sleepTimeRemaining != null) {
                                "مؤقت النوم: " + formatSleepDuration(sleepTimeRemaining)
                            } else {
                                "ضبط مؤقت النوم"
                            },
                            color = OrientTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Center Main play/pause giant buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Giant Circular Floating Gold Play button
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .clip(CircleShape)
                            .background(OrientGold)
                            .clickable { onPlayPauseToggle() }
                            .testTag("full_player_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = OrientDarkBg,
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "تشغيل/إيقاف مؤقت",
                                tint = OrientDarkBg,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quality Assurance standard disclaimer
                Text(
                    text = "مصدر البث: راديو إنترنت مباشر • صوت نقي ستريو",
                    color = OrientTextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SoundwavePill(delayMs: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val heightScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, delayMillis = delayMs, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Box(
        modifier = Modifier
            .width(5.dp)
            .height(28.dp * heightScale)
            .clip(RoundedCornerShape(3.dp))
            .background(OrientGold)
    )
}

@Composable
fun AnimatedEqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(16.dp)
    ) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(scale1).background(OrientGold))
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(scale2).background(OrientGold))
        Box(modifier = Modifier.width(3.dp).fillMaxHeight(scale3).background(OrientGold))
    }
}

@Composable
fun SleepTimerDialog(
    activeTimer: Long?,
    onDismiss: () -> Unit,
    onSelectPreset: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    val presets = listOf(
        Pair("5 دقائق", 5),
        Pair("15 دقيقة", 15),
        Pair("30 دقيقة", 30),
        Pair("45 دقيقة", 45),
        Pair("60 دقيقة", 60)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "مؤقت النوم ⏱️",
                color = OrientGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اختر الوقت ليتوقف الراديو عن البث تلقائياً ويحافظ على استهلاك الإنترنت والبطارية:",
                    color = OrientTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                if (activeTimer != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(OrientGold.copy(alpha = 0.1f))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "المؤقت نشط: " + formatSleepDuration(activeTimer) + " متبقية",
                            color = OrientGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Render preset rows
                presets.forEach { preset ->
                    Button(
                        onClick = { onSelectPreset(preset.second) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrientSurfaceVariant
                        )
                    ) {
                        Text(
                            text = preset.first,
                            color = OrientTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (activeTimer != null) {
                TextButton(onClick = onCancelTimer) {
                    Text(
                        text = "إلغاء المؤقت الحالي",
                        color = OrientFavActive,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "إغلاق", color = OrientTextSecondary)
            }
        },
        containerColor = OrientSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

// Format milliseconds remaining to classic MM:SS
private fun formatSleepDuration(ms: Long?): String? {
    if (ms == null) return null
    val secondsTotal = ms / 1000
    val minutes = secondsTotal / 60
    val seconds = secondsTotal % 60
    return String.format("%02d:%02d", minutes, seconds)
}
