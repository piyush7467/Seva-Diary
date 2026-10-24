package com.sevadiary.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.feature.seva.data.AshramColors
import com.sevadiary.app.feature.seva.data.rememberAshramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeaturesScreen(
    onBackClick: () -> Unit,
    onNavigateToChanting: () -> Unit,
    onNavigateToAshram: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val colors = rememberAshramColors()

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.all_features_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.darkGreen,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.all_features_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.bg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // 1. Daily Naam Jaap
                FeatureCardItem(
                    title = stringResource(R.string.jaap_title),
                    description = stringResource(R.string.feature_jaap_desc),
                    colors = colors,
                    iconBgColor = if (colors.isDark) Color(0xFF351F24) else Color(0xFFFFEEF1),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_devotional_jaap_mala),
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFFF28B9F) else Color(0xFFD94A6B),
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    onClick = onNavigateToChanting
                )
            }

            item {
                // 2. Satlok Ashram Visits
                FeatureCardItem(
                    title = stringResource(R.string.ashram_diary_title),
                    description = stringResource(R.string.feature_ashram_desc),
                    colors = colors,
                    iconBgColor = if (colors.isDark) Color(0xFF38291A) else Color(0xFFFEF0DE),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_devotional_ashram),
                            contentDescription = null,
                            tint = colors.gold,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    onClick = onNavigateToAshram
                )
            }

            item {
                // 3. Personal Reports
                FeatureCardItem(
                    title = stringResource(R.string.reports_title),
                    description = stringResource(R.string.feature_reports_desc),
                    colors = colors,
                    iconBgColor = if (colors.isDark) Color(0xFF193220) else Color(0xFFE8F7ED),
                    icon = {
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

            item {
                // 4. Devotional Profile & Settings
                FeatureCardItem(
                    title = stringResource(R.string.feature_profile_title),
                    description = stringResource(R.string.feature_profile_desc),
                    colors = colors,
                    iconBgColor = if (colors.isDark) Color(0xFF2B2138) else Color(0xFFEDE8F5),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFFB39DDB) else Color(0xFF5E35B1),
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    onClick = onNavigateToSettings
                )
            }
        }
    }
}

@Composable
private fun FeatureCardItem(
    title: String,
    description: String,
    colors: AshramColors,
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                icon()
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
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(34.dp)
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
