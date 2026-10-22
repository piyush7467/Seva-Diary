package com.sevadiary.app.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Spa
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
    rawLanguage: String = currentLanguage,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val pages = remember {
        listOf(
            OnboardingPageData(R.string.onboarding_1_title, R.string.onboarding_1_desc, imageRes = R.drawable.onboarding1),
            OnboardingPageData(R.string.onboarding_2_title, R.string.onboarding_2_desc, imageRes = R.drawable.onboarding2),
            OnboardingPageData(R.string.onboarding_3_title, R.string.onboarding_3_desc, imageRes = R.drawable.onboarding3),
            OnboardingPageData(R.string.onboarding_4_title, R.string.onboarding_4_desc, imageRes = R.drawable.onboarding4),
            OnboardingPageData(R.string.onboarding_5_title, R.string.onboarding_5_desc, imageRes = R.drawable.logo)
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onFinishOnboarding,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.btn_skip),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Symmetrical Page Indicator Dots
                Row(
                    modifier = Modifier.padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val isSelected = (pagerState.currentPage == iteration)
                        val animatedWidth by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 6.dp,
                            label = "dotWidth"
                        )
                        val dotColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.5.dp)
                                .height(6.dp)
                                .width(animatedWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                // Full-width rounded Forest Green Next / Get Started button
                Button(
                    onClick = {
                        if (pagerState.currentPage == pages.size - 1) {
                            onFinishOnboarding()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) {
                                stringResource(R.string.btn_get_started)
                            } else {
                                stringResource(R.string.btn_next)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                letterSpacing = 0.3.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
            if (page == 4) {
                ChoosePreferencesOnboardingPage(
                    currentLanguage = currentLanguage,
                    currentTheme = currentTheme,
                    onLanguageChange = onLanguageChange,
                    onThemeChange = onThemeChange
                )
            } else {
                val pageData = pages[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (pageData.imageRes != null) {
                            Box(
                                modifier = Modifier
                                    .size(215.dp)
                                    .shadow(
                                        elevation = 10.dp,
                                        shape = CircleShape,
                                        spotColor = SaffronGold.copy(alpha = 0.35f),
                                        ambientColor = SaffronGold.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = SaffronGold.copy(alpha = 0.85f),
                                        shape = CircleShape
                                    )
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = pageData.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else if (pageData.vectorIcon != null) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .border(
                                        width = 2.5.dp,
                                        color = SaffronGold.copy(alpha = 0.85f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = pageData.vectorIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        Text(
                            text = stringResource(pageData.titleRes),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.3).sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        DevotionalOrnamentalDivider()

                        Text(
                            text = stringResource(pageData.descRes),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                lineHeight = 23.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(max = 340.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Devotional Lotus & Gold Divider that gracefully joins Title and Description.
 */
@Composable
private fun DevotionalOrnamentalDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(130.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SaffronGold.copy(alpha = 0.5f)
        )
        Box(
            modifier = Modifier.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SaffronGold
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = SaffronGold.copy(alpha = 0.5f)
        )
    }
}

/**
 * Consistent, compact, and balanced "Choose Your Preferences" screen.
 * - Circular logo with matching sacred gold rim
 * - Unified headline, ornamental lotus divider, and description
 * - Segmented Language card with two equal-width options: English and हिंदी
 * - Segmented Theme card with three equal-width options: Light, Dark, System
 */
@Composable
private fun ChoosePreferencesOnboardingPage(
    currentLanguage: String,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {
    val isHindi = currentLanguage == "hi"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Logo - Circular container with matching gold rim
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        spotColor = SaffronGold.copy(alpha = 0.35f)
                    )
                    .border(
                        width = 2.5.dp,
                        color = SaffronGold.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Seva Diary Logo",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Heading Typography
            Text(
                text = stringResource(R.string.onboarding_5_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.2).sp
                ),
                textAlign = TextAlign.Center
            )

            // Ornamental Divider matching pages 0-3
            DevotionalOrnamentalDivider()

            // 3. Description Typography
            Text(
                text = stringResource(R.string.onboarding_5_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Language Card with two equal-width segmented options: English and हिंदी
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.pref_language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SegmentedLanguageItem(
                            label = "English",
                            isSelected = currentLanguage == "en",
                            onClick = { onLanguageChange("en") },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedLanguageItem(
                            label = "हिंदी",
                            isSelected = currentLanguage == "hi",
                            onClick = { onLanguageChange("hi") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Theme Card with three equal-width options: Warm Light, Devotional Dark, and System Default
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.pref_theme),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val lightLabel = if (isHindi) "हल्की" else "Light"
                    val darkLabel = if (isHindi) "डार्क" else "Dark"
                    val systemLabel = if (isHindi) "सिस्टम" else "System"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SegmentedThemeItem(
                            icon = Icons.Default.LightMode,
                            label = lightLabel,
                            isSelected = currentTheme == "light",
                            onClick = { onThemeChange("light") },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedThemeItem(
                            icon = Icons.Default.DarkMode,
                            label = darkLabel,
                            isSelected = currentTheme == "dark",
                            onClick = { onThemeChange("dark") },
                            modifier = Modifier.weight(1f)
                        )
                        SegmentedThemeItem(
                            icon = Icons.Default.BrightnessAuto,
                            label = systemLabel,
                            isSelected = currentTheme == "system",
                            onClick = { onThemeChange("system") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SegmentedLanguageItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun SegmentedThemeItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val activeOnColor = MaterialTheme.colorScheme.onPrimary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                        .background(activeColor)
                } else {
                    Modifier.background(Color.Transparent)
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) activeOnColor else inactiveColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isSelected) activeOnColor else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
