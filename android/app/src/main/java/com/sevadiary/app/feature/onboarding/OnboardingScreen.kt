package com.sevadiary.app.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.DevotionalCard
import com.sevadiary.app.core.designsystem.component.SevaPrimaryButton
import com.sevadiary.app.core.designsystem.theme.ForestGreen
import com.sevadiary.app.core.designsystem.theme.SaffronGold
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val titleRes: Int,
    val descRes: Int,
    val imageRes: Int? = null,
    val vectorIcon: ImageVector? = null
)

@Composable
fun OnboardingScreen(
    currentLanguage: String,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pages = listOf(
        OnboardingPageData(R.string.onboarding_1_title, R.string.onboarding_1_desc, imageRes = R.drawable.logo),
        OnboardingPageData(R.string.onboarding_2_title, R.string.onboarding_2_desc, imageRes = R.drawable.img1, vectorIcon = Icons.Default.FilterVintage),
        OnboardingPageData(R.string.onboarding_3_title, R.string.onboarding_3_desc, imageRes = R.drawable.img2, vectorIcon = Icons.Default.AccountBalance),
        OnboardingPageData(R.string.onboarding_4_title, R.string.onboarding_4_desc, imageRes = R.drawable.img4, vectorIcon = Icons.Default.Assessment),
        OnboardingPageData(R.string.onboarding_5_title, R.string.onboarding_5_desc, imageRes = R.drawable.logo, vectorIcon = Icons.Default.Tune)
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }) {
                        Text(
                            text = stringResource(R.string.btn_back),
                            color = ForestGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = onFinishOnboarding) {
                        Text(
                            text = stringResource(R.string.btn_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicator dots
                Row(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) ForestGreen else ForestGreen.copy(alpha = 0.25f)
                        val width = if (pagerState.currentPage == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                if (pagerState.currentPage == pages.size - 1) {
                    SevaPrimaryButton(
                        text = stringResource(R.string.btn_get_started),
                        onClick = onFinishOnboarding
                    )
                } else {
                    SevaPrimaryButton(
                        text = stringResource(R.string.btn_next),
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val pageData = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (pageData.imageRes != null) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = pageData.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else if (pageData.vectorIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(ForestGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = pageData.vectorIcon,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = ForestGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(pageData.titleRes),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(pageData.descRes),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                if (page == 4) {
                    // Preferences Selector: Language & Theme
                    Spacer(modifier = Modifier.height(24.dp))
                    PreferencesSelector(
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        onLanguageChange = onLanguageChange,
                        onThemeChange = onThemeChange
                    )
                }
            }
        }
    }
}

@Composable
fun PreferencesSelector(
    currentLanguage: String,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Language Option
        DevotionalCard {
            Text(
                text = stringResource(R.string.pref_language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageChip(
                    text = "English",
                    isSelected = currentLanguage == "en",
                    onClick = { onLanguageChange("en") },
                    modifier = Modifier.weight(1f)
                )
                LanguageChip(
                    text = "हिंदी",
                    isSelected = currentLanguage == "hi",
                    onClick = { onLanguageChange("hi") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Theme Option
        DevotionalCard {
            Text(
                text = stringResource(R.string.pref_theme),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageChip(
                    text = stringResource(R.string.theme_light),
                    isSelected = currentTheme == "light",
                    onClick = { onThemeChange("light") },
                    modifier = Modifier.weight(1f)
                )
                LanguageChip(
                    text = stringResource(R.string.theme_dark),
                    isSelected = currentTheme == "dark",
                    onClick = { onThemeChange("dark") },
                    modifier = Modifier.weight(1f)
                )
                LanguageChip(
                    text = stringResource(R.string.theme_system),
                    isSelected = currentTheme == "system",
                    onClick = { onThemeChange("system") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun LanguageChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ForestGreen else ForestGreen.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            textAlign = TextAlign.Center
        )
    }
}
