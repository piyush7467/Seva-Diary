package com.sevadiary.app.feature.chanting

import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.DevotionalCard
import com.sevadiary.app.core.designsystem.component.SevaTextField
import com.sevadiary.app.core.designsystem.theme.ForestGreen
import com.sevadiary.app.core.designsystem.theme.SaffronGold
import com.sevadiary.app.feature.chanting.presentation.ChantingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChantingScreen(
    viewModel: ChantingViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    var isMuted by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf(uiState.target.toString()) }
    var showHistorySheet by remember { mutableStateOf(false) }

    fun onCountTap() {
        viewModel.increment()
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (!isMuted) {
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFE65100), // Saffron / Orange
                                    Color(0xFFC2185B), // Devotional Rose / Red
                                    Color(0xFF7A0800)  // Deep Burgundy / Crimson
                                )
                            )
                        )
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "Naam Jaap Counter",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "सत साहेब",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.loadData() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
                            }
                            IconButton(onClick = { showHistorySheet = true }) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "History", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Upper Control Row: Circular Reset Button | Circular Count Dial | Circular Mute Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Reset Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161A22))
                        .border(BorderStroke(1.dp, Color(0xFF3F4756)), CircleShape)
                        .clickable { showResetDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Counter",
                        tint = Color(0xFFD6DCE5),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Centered Circular Devotional Count Medallion (Smooth Canvas Progress, Inset Padded, No Border Overlap)
                val targetProgress = if (uiState.target > 0) {
                    (uiState.todayCount.toFloat() / uiState.target).coerceIn(0f, 1f)
                } else {
                    ((uiState.todayCount % 108).toFloat() / 108f)
                }

                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF222834),
                                    Color(0xFF13171F),
                                    Color(0xFF090B0E)
                                )
                            )
                        )
                        .border(BorderStroke(1.5.dp, Color(0xFF5A6374)), CircleShape)
                        .clickable { onCountTap() },
                    contentAlignment = Alignment.Center
                ) {
                    // Custom Canvas Progress Track: safely padded 6dp inside so it NEVER touches or overlaps the outer border
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                    ) {
                        val stroke = 3.5.dp.toPx()
                        // Inactive track
                        drawCircle(
                            color = Color(0xFF262C38),
                            style = Stroke(width = stroke)
                        )
                        // Active progress arc
                        if (targetProgress > 0.001f) {
                            drawArc(
                                color = SaffronGold,
                                startAngle = -90f,
                                sweepAngle = targetProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.todayCount}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = if (uiState.todayCount > 9999) 19.sp else 23.sp
                            )
                        )
                        Text(
                            text = if (uiState.target > 0) "/${uiState.target}" else "JAAP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                    }
                }

                // Circular Sound / Mute Toggle Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161A22))
                        .border(BorderStroke(1.dp, Color(0xFF3F4756)), CircleShape)
                        .clickable { isMuted = !isMuted },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) Color(0xFFEF5350) else SaffronGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Devotional Mandala Center Stage (Dark framed card with ornate corners)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1B1F27),
                                Color(0xFF11141A),
                                Color(0xFF0A0C0F)
                            )
                        )
                    )
                    .border(BorderStroke(1.5.dp, Color(0xFF6B7280)), RoundedCornerShape(12.dp))
                    .clickable { onCountTap() }
            ) {
                // 1. Grid of 7 Sacred Images (padded 22dp inside so it NEVER touches corners)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: Central Guru Image (img1) with Gold and Silver Circular Border
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(BorderStroke(2.5.dp, SaffronGold), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img1),
                            contentDescription = "Satguru Rampal Ji Maharaj & Kabir Sahib",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: 3 Sacred Lotus Chakras (img2, img3, img4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChakraMedallion(R.drawable.img2, "Brahma Savitri Chakra")
                        ChakraMedallion(R.drawable.img3, "Ganesh Chakra")
                        ChakraMedallion(R.drawable.img4, "Vishnu Lakshmi Chakra")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subtle Devotional Divider with Gold Center Dot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFF374151))
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SaffronGold)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color(0xFF374151))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: 3 Sacred Lotus Chakras (img5, img6, img7)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChakraMedallion(R.drawable.img5, "Durga Sheranwali Chakra")
                        ChakraMedallion(R.drawable.img6, "Shiv Parvati Chakra")
                        ChakraMedallion(R.drawable.img7, "Kabir Sahib Jyoti Chakra")
                    }
                }

                // 2. The 4 Ornate Silver Corner Filigrees (Drawn AFTER content with zIndex so they are ALWAYS fully visible)
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(42.dp)
                        .zIndex(10f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(42.dp)
                        .graphicsLayer { scaleX = -1f }
                        .zIndex(10f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(42.dp)
                        .graphicsLayer { scaleY = -1f }
                        .zIndex(10f)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(42.dp)
                        .graphicsLayer { scaleX = -1f; scaleY = -1f }
                        .zIndex(10f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unique Tactile Circular Counter Action Button (Depresses with spring bounce on tap)
            var isButtonPressed by remember { mutableStateOf(false) }
            val buttonScale by animateFloatAsState(
                targetValue = if (isButtonPressed) 0.88f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "buttonScale"
            )

            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF2A313E),
                                Color(0xFF151821),
                                Color(0xFF090B0E)
                            )
                        )
                    )
                    .border(BorderStroke(2.dp, Color(0xFFB0B8C4)), CircleShape)
                    .clickable {
                        isButtonPressed = true
                        onCountTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Inner concentric groove
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .border(BorderStroke(1.dp, Color(0xFF5A6270).copy(alpha = 0.5f)), CircleShape)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "+1",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SaffronGold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "JAAP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.2.sp,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            LaunchedEffect(isButtonPressed) {
                if (isButtonPressed) {
                    delay(120)
                    isButtonPressed = false
                }
            }

            // Devotional Mala Tracking Badge
            val completedMalas = uiState.todayCount / 108
            val currentBeads = uiState.todayCount % 108
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFF141720),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF2E3544))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_devotional_jaap_mala),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SaffronGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$completedMalas Mala completed ($currentBeads/108 beads)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD6DCE5)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Target Progress Card
            DevotionalCard(
                onClick = {
                    targetInput = uiState.target.toString()
                    showTargetDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.jaap_target),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Target",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${uiState.todayCount} / ${uiState.target}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val progress = if (uiState.target > 0) (uiState.todayCount.toFloat() / uiState.target).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DevotionalCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.jaap_summary_week),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.summary.thisWeek}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }

                DevotionalCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.jaap_summary_month),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.summary.thisMonth}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }

                DevotionalCard(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.jaap_summary_total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.summary.allTimeTotal}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SaffronGold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = stringResource(R.string.jaap_reset)) },
            text = { Text(text = stringResource(R.string.jaap_confirm_reset)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetToday()
                    showResetDialog = false
                }) {
                    Text(text = stringResource(R.string.btn_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Target Edit Dialog
    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text(text = "Set Daily Target") },
            text = {
                Column {
                    Text(text = "Enter your desired daily Naam Jaap count target:")
                    Spacer(modifier = Modifier.height(8.dp))
                    SevaTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it },
                        label = "Target Count",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsed = targetInput.toIntOrNull() ?: 108
                    viewModel.setTarget(parsed)
                    showTargetDialog = false
                }) {
                    Text(text = stringResource(R.string.btn_save), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text(text = stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Full History Bottom Sheet / Modal
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Naam Jaap Calendar History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.history.isEmpty()) {
                    Text(
                        text = "No history entries logged yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.history) { item ->
                            DevotionalCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = item.date,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Target: ${item.target}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${item.count}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.count >= item.target && item.target > 0) MaterialTheme.colorScheme.primary else SaffronGold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChakraMedallion(resId: Int, description: String) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(BorderStroke(1.5.dp, Color(0xFFC5CCD6)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = description,
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )
    }
}
