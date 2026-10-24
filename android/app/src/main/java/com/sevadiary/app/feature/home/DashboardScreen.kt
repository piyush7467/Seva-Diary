package com.sevadiary.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.theme.*
import com.sevadiary.app.feature.chanting.presentation.ChantingUiState
import com.sevadiary.app.feature.chanting.presentation.ChantingViewModel
import com.sevadiary.app.feature.seva.data.AshramColors
import com.sevadiary.app.feature.seva.data.AshramMasterData
import com.sevadiary.app.feature.seva.data.rememberAshramColors
import com.sevadiary.app.feature.seva.presentation.SevaUiState
import com.sevadiary.app.feature.seva.presentation.SevaViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    devotionalName: String,
    chantingViewModel: ChantingViewModel? = null,
    sevaViewModel: SevaViewModel? = null,
    onNavigateToChanting: () -> Unit,
    onNavigateToAshram: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAllFeatures: () -> Unit = onNavigateToChanting
) {
    val colors = rememberAshramColors()
    val isDark = colors.isDark

    // Refresh stats when dashboard is opened or resumed
    LaunchedEffect(Unit) {
        chantingViewModel?.loadData()
        sevaViewModel?.loadRecords()
    }

    val chantingUiState = chantingViewModel?.uiState?.collectAsState()?.value ?: ChantingUiState()
    val sevaUiState = sevaViewModel?.uiState?.collectAsState()?.value ?: SevaUiState()

    // Naam Jaap summary values
    val todayCount = chantingUiState.todayCount
    val target = if (chantingUiState.target > 0) chantingUiState.target else 108
    val progress = (todayCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val percentage = ((todayCount.toFloat() / target.toFloat()) * 100).toInt()
    val remaining = (target - todayCount).coerceAtLeast(0)
    val thisMonthJaap = chantingUiState.summary.thisMonth

    // Ashram visit summary values
    val records = sevaUiState.records
    val recentVisit = records.firstOrNull()
    val visitDateText = remember(recentVisit?.startDate) {
        formatVisitDisplayDate(recentVisit?.startDate)
    }
    val visitDurationText = if (recentVisit != null) {
        if (recentVisit.durationDays == 1) "1 day stay" else "${recentVisit.durationDays} days stay"
    } else {
        stringResource(R.string.log_your_visit)
    }
    val recentVisitLocation = remember(recentVisit) {
        if (recentVisit == null) ""
        else if (recentVisit.customAshramName.isNotBlank()) recentVisit.customAshramName
        else {
            val master = AshramMasterData.findByCode(recentVisit.ashramCode)
            if (master != null) {
                master.code.replace("_", " ").lowercase()
                    .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            } else {
                recentVisit.ashramCode.replace("_", " ")
            }
        }
    }
    val totalVisitsCount = records.size
    val distinctAshramsCount = remember(records) {
        records.map { it.ashramCode }.distinct().size
    }
    val totalSevaDays = remember(records) {
        records.sumOf { it.durationDays }
    }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            DashboardTopBar(
                devotionalName = devotionalName,
                colors = colors,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            DashboardBottomBar(
                colors = colors,
                onHomeClick = { /* Already on Home */ },
                onJaapClick = onNavigateToChanting,
                onVisitsClick = onNavigateToAshram,
                onReportsClick = onNavigateToReports,
                onProfileClick = onNavigateToSettings
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. Hero Card: "Today's Naam Jaap" with Full-Width Progress & Target Badge
            NaamJaapSummaryHeroCard(
                todayCount = todayCount,
                target = target,
                progress = progress,
                percentage = percentage,
                remaining = remaining,
                colors = colors,
                onClick = onNavigateToChanting
            )

            // 2. Horizontally Scrollable "Quick Insights" Section
            QuickInsightsSection(
                thisMonthJaap = thisMonthJaap,
                recentVisitDate = visitDateText,
                recentVisitDuration = visitDurationText,
                recentVisitLocation = recentVisitLocation,
                totalVisitsCount = totalVisitsCount,
                distinctAshramsCount = distinctAshramsCount,
                totalSevaDays = totalSevaDays,
                colors = colors,
                onNavigateToChanting = onNavigateToChanting,
                onNavigateToAshram = onNavigateToAshram,
                onNavigateToReports = onNavigateToReports
            )

            // 3. Section: "Explore Features" with functional "View All >"
            ExploreFeaturesSection(
                colors = colors,
                onNavigateToChanting = onNavigateToChanting,
                onNavigateToAshram = onNavigateToAshram,
                onNavigateToReports = onNavigateToReports,
                onViewAllClick = onNavigateToAllFeatures
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Top App Bar with status bar insets, circular avatar with gold border, devotee greeting, and profile action.
 * Note: Notification bell is removed completely as requested.
 */
@Composable
private fun DashboardTopBar(
    devotionalName: String,
    colors: AshramColors,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Logo Avatar with gold ring
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(if (colors.isDark) Color(0xFF1E1C1A) else Color.White)
                    .border(
                        BorderStroke(1.5.dp, colors.gold.copy(alpha = 0.7f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = stringResource(R.string.greeting_sat_saheb),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.secondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                )

                val defaultName = stringResource(R.string.devotee_title)
                val displayName = devotionalName.ifBlank { defaultName }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.darkGreen,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Profile / Settings Button (Only profile icon kept, notification bell removed)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(BorderStroke(1.dp, colors.border), CircleShape)
                .clickable(onClick = onNavigateToSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Redesigned Hero Card for Today's Naam Jaap.
 * Full width layout, large counter, progress bar, completion badge, and no quote section.
 */
@Composable
private fun NaamJaapSummaryHeroCard(
    todayCount: Int,
    target: Int,
    progress: Float,
    percentage: Int,
    remaining: Int,
    colors: AshramColors,
    onClick: () -> Unit
) {
    val isCompleted = todayCount >= target

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (colors.isDark) Color(0xFF14331E) else Color(0xFF174C38),
        border = BorderStroke(
            1.dp,
            if (colors.isDark) DevotionalEmerald.copy(alpha = 0.35f) else Color(0x334DB670)
        ),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Top Row: Title and Trailing Chevron Circle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.today_naam_jaap),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Jaap Counter Display
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = NumberFormat.getIntegerInstance().format(todayCount),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 36.sp
                    )
                )
                Text(
                    text = " / ${NumberFormat.getIntegerInstance().format(target)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 22.sp
                    ),
                    modifier = Modifier.padding(bottom = 4.dp, start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Full-Width Progress Bar with Percentage on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF34C759))
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completion / Remaining Capsule Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.25f),
                modifier = Modifier.wrapContentWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4ADE80),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.jaap_target_completed_banner),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                R.string.jaap_target_remaining_banner,
                                NumberFormat.getIntegerInstance().format(remaining)
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontally Scrollable "Quick Insights" Section using LazyRow.
 */
@Composable
private fun QuickInsightsSection(
    thisMonthJaap: Int,
    recentVisitDate: String,
    recentVisitDuration: String,
    recentVisitLocation: String,
    totalVisitsCount: Int,
    distinctAshramsCount: Int,
    totalSevaDays: Int,
    colors: AshramColors,
    onNavigateToChanting: () -> Unit,
    onNavigateToAshram: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header: "Quick Insights" & "View All >"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.quick_insights),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.darkGreen,
                    fontSize = 18.sp
                )
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onNavigateToReports)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontally Scrollable Cards Row
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            // 1. This Month Total Jaap
            item {
                InsightCard(
                    title = stringResource(R.string.this_month),
                    subtitle = stringResource(R.string.total_jaap),
                    value = NumberFormat.getIntegerInstance().format(thisMonthJaap),
                    footerText = stringResource(R.string.trend_from_last_month),
                    footerIcon = Icons.AutoMirrored.Filled.TrendingUp,
                    bgColor = if (colors.isDark) Color(0xFF17281D) else Color(0xFFF0F7F3),
                    borderColor = if (colors.isDark) Color(0xFF253D2E) else Color(0xFFDCEAE1),
                    iconBgColor = if (colors.isDark) Color(0xFF1D3B26) else Color(0xFFD9EFE2),
                    iconTint = if (colors.isDark) Color(0xFF4ADE80) else Color(0xFF1E7E34),
                    colors = colors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFF4ADE80) else Color(0xFF1E7E34),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    width = 160.dp,
                    onClick = onNavigateToChanting
                )
            }

            // 2. Last Ashram Visit
            item {
                InsightCard(
                    title = stringResource(R.string.last_ashram_visit),
                    subtitle = recentVisitDuration,
                    value = recentVisitDate.ifBlank { stringResource(R.string.no_visits_yet) },
                    footerText = recentVisitLocation.ifBlank { "Satlok Ashram" },
                    footerIcon = Icons.Default.Place,
                    bgColor = if (colors.isDark) Color(0xFF282014) else Color(0xFFFDF7EC),
                    borderColor = if (colors.isDark) Color(0xFF3E311D) else Color(0xFFF4E7CE),
                    iconBgColor = if (colors.isDark) Color(0xFF382914) else Color(0xFFFDE8C8),
                    iconTint = if (colors.isDark) colors.gold else Color(0xFFD97706),
                    colors = colors,
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_devotional_ashram),
                            contentDescription = null,
                            tint = if (colors.isDark) colors.gold else Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    width = 170.dp,
                    onClick = onNavigateToAshram
                )
            }

            // 3. Total Visits
            item {
                val ashramCountText = if (distinctAshramsCount == 1) {
                    stringResource(R.string.distinct_ashram_single)
                } else {
                    stringResource(R.string.distinct_ashrams_format, distinctAshramsCount)
                }
                InsightCard(
                    title = stringResource(R.string.total_visits_title),
                    subtitle = "",
                    value = totalVisitsCount.toString(),
                    footerText = ashramCountText,
                    footerIcon = null,
                    bgColor = if (colors.isDark) Color(0xFF2A1717) else Color(0xFFFDF2F2),
                    borderColor = if (colors.isDark) Color(0xFF422121) else Color(0xFFF9DCDC),
                    iconBgColor = if (colors.isDark) Color(0xFF3A1919) else Color(0xFFFDDCDC),
                    iconTint = if (colors.isDark) Color(0xFFFF8A80) else Color(0xFFE53935),
                    colors = colors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFFFF8A80) else Color(0xFFE53935),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    width = 150.dp,
                    onClick = onNavigateToAshram
                )
            }

            // 4. Total Seva Days
            item {
                val sevaDaysText = if (totalSevaDays == 1) {
                    stringResource(R.string.day_at_ashram_single)
                } else {
                    stringResource(R.string.days_at_ashram_format, totalSevaDays)
                }
                InsightCard(
                    title = stringResource(R.string.total_seva_days_title),
                    subtitle = "",
                    value = totalSevaDays.toString(),
                    footerText = "days at ashram",
                    footerIcon = null,
                    bgColor = if (colors.isDark) Color(0xFF14222E) else Color(0xFFF0F6FA),
                    borderColor = if (colors.isDark) Color(0xFF1E3447) else Color(0xFFDCE8F2),
                    iconBgColor = if (colors.isDark) Color(0xFF162B3D) else Color(0xFFDCEAF6),
                    iconTint = if (colors.isDark) Color(0xFF64B5F6) else Color(0xFF1976D2),
                    colors = colors,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFF64B5F6) else Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    width = 150.dp,
                    onClick = onNavigateToAshram
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scroll Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val isSelected = listState.firstVisibleItemIndex == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 6.dp else 4.5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) colors.primary else colors.border
                        )
                )
            }
        }
    }
}

/**
 * Reusable Insight Card for Quick Insights section.
 */
@Composable
private fun InsightCard(
    title: String,
    subtitle: String,
    value: String,
    footerText: String,
    footerIcon: androidx.compose.ui.graphics.vector.ImageVector?,
    bgColor: Color,
    borderColor: Color,
    iconBgColor: Color,
    iconTint: Color,
    colors: AshramColors,
    icon: @Composable () -> Unit,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 1.dp,
        modifier = Modifier
            .width(width)
            .height(142.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Icon + Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.secondaryText,
                                fontSize = 10.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Big Metric Value
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primaryText,
                    fontSize = 20.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Footer Trend / Detail Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (footerIcon != null) {
                    Icon(
                        imageVector = footerIcon,
                        contentDescription = null,
                        tint = colors.secondaryText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = footerText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.secondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Explore Features section listing primary devotional tools.
 */
@Composable
private fun ExploreFeaturesSection(
    colors: AshramColors,
    onNavigateToChanting: () -> Unit,
    onNavigateToAshram: () -> Unit,
    onNavigateToReports: () -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.explore_features),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.darkGreen,
                    fontSize = 18.sp
                )
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onViewAllClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.view_all),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Feature 1: Daily Naam Jaap
        FeatureActionCard(
            title = stringResource(R.string.jaap_title),
            description = stringResource(R.string.feature_jaap_desc),
            colors = colors,
            iconBgColor = if (colors.isDark) Color(0xFF351F24) else Color(0xFFFFEEF1),
            iconContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_devotional_jaap_mala),
                    contentDescription = null,
                    tint = if (colors.isDark) Color(0xFFF28B9F) else Color(0xFFD94A6B),
                    modifier = Modifier.size(28.dp)
                )
            },
            onClick = onNavigateToChanting
        )

        // Feature 2: Satlok Ashram Visits
        FeatureActionCard(
            title = stringResource(R.string.ashram_diary_title),
            description = stringResource(R.string.feature_ashram_desc),
            colors = colors,
            iconBgColor = if (colors.isDark) Color(0xFF38291A) else Color(0xFFFEF0DE),
            iconContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_devotional_ashram),
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(28.dp)
                )
            },
            onClick = onNavigateToAshram
        )

        // Feature 3: Personal Reports
        FeatureActionCard(
            title = stringResource(R.string.reports_title),
            description = stringResource(R.string.feature_reports_desc),
            colors = colors,
            iconBgColor = if (colors.isDark) Color(0xFF193220) else Color(0xFFE8F7ED),
            iconContent = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(26.dp)
                )
            },
            onClick = onNavigateToReports
        )
    }
}

/**
 * Standardized Feature Action Card.
 */
@Composable
private fun FeatureActionCard(
    title: String,
    description: String,
    colors: AshramColors,
    iconBgColor: Color,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        fontSize = 12.sp,
                        lineHeight = 16.5.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) Color(0xFF262320) else Color(0xFFF4EFE6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 5-item Bottom Navigation Bar matching the reference UI.
 */
@Composable
private fun DashboardBottomBar(
    colors: AshramColors,
    onHomeClick: () -> Unit,
    onJaapClick: () -> Unit,
    onVisitsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home (Selected)
            BottomNavItem(
                label = stringResource(R.string.nav_home),
                selected = true,
                colors = colors,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.nav_home),
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = onHomeClick
            )

            // 2. Naam Jaap
            BottomNavItem(
                label = stringResource(R.string.nav_jaap),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        painter = painterResource(R.drawable.ic_devotional_jaap_mala),
                        contentDescription = stringResource(R.string.nav_jaap),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onJaapClick
            )

            // 3. Visits
            BottomNavItem(
                label = stringResource(R.string.nav_visits),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        painter = painterResource(R.drawable.ic_devotional_ashram),
                        contentDescription = stringResource(R.string.nav_visits),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onVisitsClick
            )

            // 4. Reports
            BottomNavItem(
                label = stringResource(R.string.nav_reports),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.nav_reports),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onReportsClick
            )

            // 5. Profile
            BottomNavItem(
                label = stringResource(R.string.nav_profile),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.nav_profile),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    colors: AshramColors,
    icon: @Composable (tint: Color) -> Unit,
    onClick: () -> Unit
) {
    val activeColor = colors.primary
    val inactiveColor = if (colors.isDark) Color(0xFF8E8880) else Color(0xFF8E9690)
    val color = if (selected) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = color,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        // Active indicator pill
        if (selected) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(activeColor)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

/**
 * Parses and formats dates from 'yyyy-MM-dd' to 'dd MMM yyyy' safely.
 */
private fun formatVisitDisplayDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        if (date != null) outputFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
