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

private val AshramBgColor = Color(0xFFFAF8F2)
private val AshramForestGreen = Color(0xFF174C38)
private val AshramDarkGreen = Color(0xFF103C2D)
private val AshramSecondaryText = Color(0xFF646B65)
private val AshramBorderColor = Color(0xFFE5DFD3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFeaturesScreen(
    onBackClick: () -> Unit,
    onNavigateToChanting: () -> Unit,
    onNavigateToAshram: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        containerColor = AshramBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.all_features_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AshramDarkGreen,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.all_features_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AshramSecondaryText,
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
                            tint = Color(0xFF252B27)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AshramBgColor)
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
                    iconBgColor = Color(0xFFFFEEF1),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_devotional_jaap_mala),
                            contentDescription = null,
                            tint = Color(0xFFD94A6B),
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
                    iconBgColor = Color(0xFFFEF0DE),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_devotional_ashram),
                            contentDescription = null,
                            tint = Color(0xFFD97706),
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
                    iconBgColor = Color(0xFFE8F7ED),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF1E7E34),
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
                    iconBgColor = Color(0xFFEDE8F5),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF5E35B1),
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
    iconBgColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, AshramBorderColor),
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
                        color = Color(0xFF252B27),
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AshramSecondaryText,
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
                    .background(Color(0xFFF4EFE6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = AshramSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
