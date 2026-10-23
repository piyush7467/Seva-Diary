package com.sevadiary.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.theme.*
import com.sevadiary.app.feature.chanting.presentation.ChantingUiState
import com.sevadiary.app.feature.chanting.presentation.ChantingViewModel
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
    onNavigateToSettings: () -> Unit
) {
    // Refresh stats when dashboard is opened or resumed
    LaunchedEffect(Unit) {
        chantingViewModel?.loadData()
        sevaViewModel?.loadRecords()
    }

    val chantingUiState = chantingViewModel?.uiState?.collectAsState()?.value ?: ChantingUiState()
    val sevaUiState = sevaViewModel?.uiState?.collectAsState()?.value ?: SevaUiState()

    // Determine theme mode dynamically (dark vs light)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Naam Jaap summary values
    val todayCount = chantingUiState.todayCount
    val target = if (chantingUiState.target > 0) chantingUiState.target else 108
    val progress = (todayCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()
    val remaining = (target - todayCount).coerceAtLeast(0)
    val thisMonthJaap = chantingUiState.summary.thisMonth

    // Ashram visit summary values
    val recentVisit = sevaUiState.records.firstOrNull()
    val visitDateText = remember(recentVisit?.startDate) {
        formatVisitDisplayDate(recentVisit?.startDate)
    }
    val visitDurationText = if (recentVisit != null) {
        stringResource(R.string.days_stay, recentVisit.durationDays)
    } else {
        stringResource(R.string.log_your_visit)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                devotionalName = devotionalName,
                isDark = isDark,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            DashboardBottomBar(
                isDark = isDark,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. Hero Card: "Today's Naam Jaap" with target progress & devotional quote
            NaamJaapSummaryHeroCard(
                todayCount = todayCount,
                target = target,
                progress = progress,
                percentage = percentage,
                remaining = remaining,
                isDark = isDark,
                onClick = onNavigateToChanting
            )

            // 2. Row with Two Compact Stat Chips: "This Month Total Jaap" & "Last Ashram Visit"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // This Month Total Jaap
                StatChipCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.this_month),
                    subtitle = stringResource(R.string.total_jaap),
                    value = NumberFormat.getIntegerInstance().format(thisMonthJaap),
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF143820) else Color(0xFFD7EEDB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (isDark) DevotionalEmerald else Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (isDark) DevotionalEmerald else Color(0xFF2E7D32),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    cardBgColor = if (isDark) Color(0xFF18241B) else Color(0xFFF0F7F2),
                    cardBorderColor = if (isDark) DevotionalEmerald.copy(alpha = 0.3f) else Color(0x332E7D32),
                    onClick = onNavigateToChanting
                )

                // Last Ashram Visit
                StatChipCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.last_ashram_visit),
                    subtitle = visitDurationText,
                    value = visitDateText.ifBlank { stringResource(R.string.no_visits_yet) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF4A3414) else Color(0xFFFEE8C8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_devotional_ashram),
                                contentDescription = null,
                                tint = if (isDark) RadiantGold else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isDark) RadiantGold else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    cardBgColor = if (isDark) Color(0xFF262016) else Color(0xFFFFF8EC),
                    cardBorderColor = if (isDark) RadiantGold.copy(alpha = 0.3f) else Color(0x33D9822B),
                    onClick = onNavigateToAshram
                )
            }

            // 3. Section Header: "Explore Features" & "View All >"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.explore_features),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateToChanting)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.view_all),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 4. Feature Cards
            // Card 1: Daily Naam Jaap
            FeatureActionCard(
                title = stringResource(R.string.jaap_title),
                description = stringResource(R.string.feature_jaap_desc),
                iconBgColor = if (isDark) Color(0xFF351F24) else Color(0xFFFFEEF1),
                iconContent = {
                    Image(
                        painter = painterResource(id = R.drawable.counter_logo),
                        contentDescription = stringResource(R.string.jaap_title),
                        modifier = Modifier.size(36.dp)
                    )
                },
                onClick = onNavigateToChanting
            )

            // Card 2: Satlok Ashram Visits
            FeatureActionCard(
                title = stringResource(R.string.ashram_diary_title),
                description = stringResource(R.string.feature_ashram_desc),
                iconBgColor = if (isDark) Color(0xFF38291A) else Color(0xFFFEF0DE),
                iconContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_devotional_ashram),
                        contentDescription = stringResource(R.string.ashram_diary_title),
                        tint = if (isDark) RadiantGold else SaffronGold,
                        modifier = Modifier.size(28.dp)
                    )
                },
                onClick = onNavigateToAshram
            )

            // Card 3: Personal Reports
            FeatureActionCard(
                title = stringResource(R.string.reports_title),
                description = stringResource(R.string.feature_reports_desc),
                iconBgColor = if (isDark) Color(0xFF193220) else Color(0xFFE8F7ED),
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.reports_title),
                        tint = if (isDark) DevotionalEmerald else SuccessGreen,
                        modifier = Modifier.size(26.dp)
                    )
                },
                onClick = onNavigateToReports
            )

            // 5. Daily Devotional Quote Card
            DevotionalQuoteCard(
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Top App Bar with status bar insets, circular avatar with gold border, devotee greeting, and quick actions.
 */
@Composable
private fun DashboardTopBar(
    devotionalName: String,
    isDark: Boolean,
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
                    .background(Color.White)
                    .border(
                        BorderStroke(1.5.dp, SaffronGold.copy(alpha = 0.6f)),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                )

                val displayName = devotionalName.ifBlank { "Piyush Das" }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DevotionalEmerald else ForestGreen,
                        fontSize = 19.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Actions: Notification bell with unread indicator & Profile / Settings button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notifications Bell Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                // Red unread notification dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                )
            }

            // Profile / Settings Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                .clickable(onClick = onNavigateToSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Hero Card showing Today's Naam Jaap progress and inspirational devotional quote.
 */
@Composable
private fun NaamJaapSummaryHeroCard(
    todayCount: Int,
    target: Int,
    progress: Float,
    percentage: Int,
    remaining: Int,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF14331E) else ForestGreen,
        border = BorderStroke(
            1.dp,
            if (isDark) DevotionalEmerald.copy(alpha = 0.35f) else Color(0x334DB670)
        ),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Section: Naam Jaap counts & progress
            Column(modifier = Modifier.weight(1.25f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.today_naam_jaap),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = NumberFormat.getIntegerInstance().format(todayCount),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = " / ${NumberFormat.getIntegerInstance().format(target)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 17.sp
                        ),
                        modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar with percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.5.dp))
                                .background(Color(0xFF4ADE80))
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target Remaining Notice
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (remaining > 0) {
                            stringResource(
                                R.string.remaining_to_daily_target,
                                NumberFormat.getIntegerInstance().format(remaining)
                            )
                        } else {
                            stringResource(R.string.daily_target_completed)
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Vertical subtle divider
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .width(1.dp)
                    .height(95.dp)
                    .background(Color.White.copy(alpha = 0.18f))
            )

            // Right Section: Golden Lotus & Devotional Quote
            Column(
                modifier = Modifier.weight(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_devotional_lotus),
                    contentDescription = null,
                    tint = Color(0xFFF3C76D),
                    modifier = Modifier.size(38.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.quote_naam_aadhar),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.quote_author_rampal_ji),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

/**
 * Compact Stat Chip for This Month and Last Ashram Visit.
 */
@Composable
private fun StatChipCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    value: String,
    icon: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    cardBgColor: Color,
    cardBorderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            trailing()
        }
    }
}

/**
 * Redesigned Feature Card with standardized icon container, aligned text, and chevron.
 */
@Composable
private fun FeatureActionCard(
    title: String,
    description: String,
    iconBgColor: Color,
    iconContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Daily Devotional Quote Banner at the bottom of the screen.
 */
@Composable
private fun DevotionalQuoteCard(
    isDark: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF221F1B) else Color(0xFFFBF5E8),
        border = BorderStroke(
            1.dp,
            if (isDark) RadiantGold.copy(alpha = 0.3f) else Color(0x33D9822B)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_devotional_lotus),
                contentDescription = null,
                tint = if (isDark) RadiantGold else SaffronGold,
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(if (isDark) Color(0x33F2A848) else Color(0x33D9822B))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quote_life_purpose),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.quote_author_rampal_ji),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
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
    isDark: Boolean,
    onHomeClick: () -> Unit,
    onJaapClick: () -> Unit,
    onVisitsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Selected)
            BottomNavItem(
                label = stringResource(R.string.nav_home),
                selected = true,
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

            // Jaap
            BottomNavItem(
                label = stringResource(R.string.nav_jaap),
                selected = false,
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

            // Visits
            BottomNavItem(
                label = stringResource(R.string.nav_visits),
                selected = false,
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

            // Reports
            BottomNavItem(
                label = stringResource(R.string.nav_reports),
                selected = false,
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

            // Profile
            BottomNavItem(
                label = stringResource(R.string.nav_profile),
                selected = false,
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
    icon: @Composable (tint: Color) -> Unit,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
