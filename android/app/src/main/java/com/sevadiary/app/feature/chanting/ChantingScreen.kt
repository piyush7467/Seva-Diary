package com.sevadiary.app.feature.chanting

import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
        containerColor = Color(0xFF0F1115),
        topBar = {
            // Devotional Gradient Top Bar (Orange to Deep Crimson)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF8C00), // Vibrant Saffron / Orange
                                Color(0xFFE53935), // Red
                                Color(0xFF7A0800)  // Deep Burgundy / Crimson
                            )
                        )
                    )
                    .padding(horizontal = 8.dp),
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
                        Text(
                            text = "Counter",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Row {
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Upper Control Row: Circular Reset Button | Circular Count Dial | Circular Mute Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Reset Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141820))
                        .border(BorderStroke(1.dp, Color(0xFF3E4452)), CircleShape)
                        .clickable { showResetDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Counter",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Centered Circular Devotional Count Medallion with Progress Ring
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
                                    Color(0xFF1B202A),
                                    Color(0xFF0F1217),
                                    Color(0xFF07090C)
                                )
                            )
                        )
                        .border(BorderStroke(2.dp, Color(0xFF8A94A6)), CircleShape)
                        .clickable { onCountTap() },
                    contentAlignment = Alignment.Center
                ) {
                    // Circular Progress indicator track
                    CircularProgressIndicator(
                        progress = { targetProgress },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(3.dp),
                        color = SaffronGold,
                        trackColor = Color(0xFF222834),
                        strokeWidth = 3.5.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.todayCount}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = if (uiState.todayCount > 9999) 20.sp else 24.sp
                            )
                        )
                        Text(
                            text = if (uiState.target > 0) "/${uiState.target}" else "JAAP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                // Circular Sound / Mute Toggle Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141820))
                        .border(BorderStroke(1.dp, Color(0xFF3E4452)), CircleShape)
                        .clickable { isMuted = !isMuted },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = if (isMuted) Color(0xFFE53935) else SaffronGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Devotional Mandala Center Stage (Dark framed card with ornate corners)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E222A),
                                Color(0xFF13161D),
                                Color(0xFF0D0F13)
                            )
                        )
                    )
                    .border(BorderStroke(1.5.dp, Color(0xFF5A6270)), RoundedCornerShape(8.dp))
                    .clickable { onCountTap() }
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                // 4 Ornate Silver Corner Filigrees
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFC5CCD6),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(36.dp)
                        .padding(2.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFC5CCD6),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .padding(2.dp)
                        .graphicsLayer { rotationY = 180f }
                )
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFC5CCD6),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(36.dp)
                        .padding(2.dp)
                        .graphicsLayer { rotationX = 180f }
                )
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_ornate_corner),
                    contentDescription = null,
                    tint = Color(0xFFC5CCD6),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .padding(2.dp)
                        .graphicsLayer { rotationZ = 180f }
                )

                // Grid of 7 Sacred Images
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: Central Guru Image (img1) with Silver Circular Border
                    Box(
                        modifier = Modifier
                            .size(132.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(BorderStroke(2.5.dp, Color(0xFFD6DCE5)), CircleShape),
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: 3 Lotus Chakras (img2, img3, img4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img2),
                            contentDescription = "Brahma Savitri Chakra",
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            painter = painterResource(R.drawable.img3),
                            contentDescription = "Ganesh Chakra",
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            painter = painterResource(R.drawable.img4),
                            contentDescription = "Vishnu Lakshmi Chakra",
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontal Divider
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        thickness = 1.dp,
                        color = Color(0xFF474E59)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: 3 Lotus Chakras (img5, img6, img7)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img5),
                            contentDescription = "Durga Sheranwali Chakra",
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            painter = painterResource(R.drawable.img6),
                            contentDescription = "Shiv Parvati Chakra",
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit
                        )
                        Image(
                            painter = painterResource(R.drawable.img7),
                            contentDescription = "Kabir Sahib Jyoti Chakra",
                            modifier = Modifier.size(92.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                    .size(94.dp)
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF282F3B),
                                Color(0xFF141820),
                                Color(0xFF090B0E)
                            )
                        )
                    )
                    .border(BorderStroke(2.5.dp, Color(0xFFB0B8C4)), CircleShape)
                    .clickable {
                        isButtonPressed = true
                        onCountTap()
                    },
                contentAlignment = Alignment.Center
            ) {
                // Concentric inner bevel ring
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                        text = "COUNT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.2.sp,
                            fontSize = 14.sp
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
            Spacer(modifier = Modifier.height(10.dp))
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = ForestGreen)
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
                    color = ForestGreen,
                    trackColor = ForestGreen.copy(alpha = 0.15f)
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
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
                    val parsed = targetInput.toIntOrNull() ?: 1000
                    viewModel.setTarget(parsed)
                    showTargetDialog = false
                }) {
                    Text(text = stringResource(R.string.btn_save), color = ForestGreen)
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
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
                                            color = if (item.count >= item.target && item.target > 0) ForestGreen else SaffronGold
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
