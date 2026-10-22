package com.sevadiary.app.feature.onboarding

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import kotlinx.coroutines.launch

data class OnboardingPageTheme(
    val titleRes: Int,
    val descRes: Int,
    val imageRes: Int,
    val motifIcon: ImageVector,
    val backgroundColor: Color,
    val titleColor: Color,
    val iconColor: Color,
    val descColor: Color,
    val accentColor: Color,
    val skipColor: Color
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

    // 4 Devotional Artwork screens + 1 Dedicated Preferences setup screen
    val pages = remember {
        listOf(
            // 1. Welcome to Seva Diary - Warm Temple Golden Ivory
            OnboardingPageTheme(
                titleRes = R.string.onboarding_1_title,
                descRes = R.string.onboarding_1_desc,
                imageRes = R.drawable.onboarding1,
                motifIcon = Icons.Default.Spa,
                backgroundColor = Color(0xFFFCF8EE),
                titleColor = Color(0xFF2C1F14),
                iconColor = Color(0xFFB8822A),
                descColor = Color(0xFF5D4E41),
                accentColor = Color(0xFFA6711E),
                skipColor = Color(0xFF9E846E)
            ),
            // 2. Naam Jaap - Soft Devotional Rose Cream
            OnboardingPageTheme(
                titleRes = R.string.onboarding_2_title,
                descRes = R.string.onboarding_2_desc,
                imageRes = R.drawable.onboarding2,
                motifIcon = Icons.Default.Spa,
                backgroundColor = Color(0xFFFDF1F4),
                titleColor = Color(0xFF4E1624),
                iconColor = Color(0xFF9A2C4D),
                descColor = Color(0xFF63464F),
                accentColor = Color(0xFF6B1D38),
                skipColor = Color(0xFF9B7480)
            ),
            // 3. Learn & Grow - Serene Devotional Sky Mist Blue
            OnboardingPageTheme(
                titleRes = R.string.onboarding_3_title,
                descRes = R.string.onboarding_3_desc,
                imageRes = R.drawable.onboarding3,
                motifIcon = Icons.AutoMirrored.Filled.MenuBook,
                backgroundColor = Color(0xFFF0F5F8),
                titleColor = Color(0xFF103352),
                iconColor = Color(0xFF1B5586),
                descColor = Color(0xFF455666),
                accentColor = Color(0xFF103B64),
                skipColor = Color(0xFF6E8699)
            ),
            // 4. Seva & Devotion - Sunset Golden Amber
            OnboardingPageTheme(
                titleRes = R.string.onboarding_4_title,
                descRes = R.string.onboarding_4_desc,
                imageRes = R.drawable.onboarding4,
                motifIcon = Icons.Default.Spa,
                backgroundColor = Color(0xFFFCF4EA),
                titleColor = Color(0xFF5C2908),
                iconColor = Color(0xFFA64F13),
                descColor = Color(0xFF634839),
                accentColor = Color(0xFFB54704),
                skipColor = Color(0xFF9E7761)
            ),
            // 5. Choose Your Preferences - Peaceful Temple Sage & Ivory
            OnboardingPageTheme(
                titleRes = R.string.onboarding_5_title,
                descRes = R.string.onboarding_5_desc,
                imageRes = R.drawable.logo,
                motifIcon = Icons.Default.Spa,
                backgroundColor = Color(0xFFFBF8F3),
                titleColor = Color(0xFF2C2016),
                iconColor = Color(0xFFA6711E),
                descColor = Color(0xFF5D4E41),
                accentColor = Color(0xFF1E4D2B),
                skipColor = Color(0xFF8C735E)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPageTheme = pages[pagerState.currentPage]
    val animatedBgColor by animateColorAsState(
        targetValue = currentPageTheme.backgroundColor,
        label = "onboardingBgColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBgColor)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (pagerState.currentPage < pages.size - 1) {
                    // Screens 0–3: Skip on Left, Animated Dots in Center, Pill Next on Right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip action
                        Text(
                            text = stringResource(R.string.btn_skip),
                            color = currentPageTheme.skipColor,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onFinishOnboarding() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )

                        // Symmetrical Indicator Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(pages.size) { index ->
                                val isSelected = pagerState.currentPage == index
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isSelected) 18.dp else 6.dp,
                                    label = "dotWidth"
                                )
                                val dotColor = if (isSelected) {
                                    currentPageTheme.accentColor
                                } else {
                                    currentPageTheme.accentColor.copy(alpha = 0.25f)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(dotWidth)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }

                        // Next Pill Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPageTheme.accentColor,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.btn_next),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Page 4: Centered Dots on Top, Full-width Get Started Button Below
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.padding(bottom = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(pages.size) { index ->
                                val isSelected = pagerState.currentPage == index
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isSelected) 18.dp else 6.dp,
                                    label = "dotWidth"
                                )
                                val dotColor = if (isSelected) {
                                    currentPageTheme.accentColor
                                } else {
                                    currentPageTheme.accentColor.copy(alpha = 0.25f)
                                }
                                Box(
                                    modifier = Modifier
                                        .height(6.dp)
                                        .width(dotWidth)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }

                        Button(
                            onClick = onFinishOnboarding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPageTheme.accentColor,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.btn_get_started),
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
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) { page ->
                val pageTheme = pages[page]

                if (page == 4) {
                    ChoosePreferencesOnboardingPage(
                        pageTheme = pageTheme,
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        onLanguageChange = onLanguageChange,
                        onThemeChange = onThemeChange
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val totalHeight = maxHeight
                        // Proportionate top image height covering ~48% of screen height
                        val imageHeight = (totalHeight * 0.48f).coerceIn(300.dp, 440.dp)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Full Devotional Image covering top space without circle crop
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(imageHeight)
                            ) {
                                Image(
                                    painter = painterResource(id = pageTheme.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.TopCenter
                                )

                                // Soft bottom gradient fade seamlessly blending into page background
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    pageTheme.backgroundColor.copy(alpha = 0.4f),
                                                    pageTheme.backgroundColor.copy(alpha = 0.85f),
                                                    pageTheme.backgroundColor
                                                )
                                            )
                                        )
                                )
                            }

                            // Content Section directly below the image
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 10.dp, bottom = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(pageTheme.titleRes),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = pageTheme.titleColor,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                // Devotional Lotus / Wisdom motif flourish with delicate accent lines
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(1.dp)
                                            .background(pageTheme.iconColor.copy(alpha = 0.45f))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = pageTheme.motifIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = pageTheme.iconColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(1.dp)
                                            .background(pageTheme.iconColor.copy(alpha = 0.45f))
                                    )
                                }

                                Text(
                                    text = stringResource(pageTheme.descRes),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = pageTheme.descColor
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.widthIn(max = 340.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Back Button at top-left when past the first screen
        if (pagerState.currentPage > 0) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 8.dp)
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.28f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Clean, balanced "Choose Your Preferences" onboarding page.
 * - Circular logo with matching golden ochre halo
 * - Devotional typography and ornamental lotus flourish
 * - Segmented Language card (English / हिंदी)
 * - Segmented Theme card (Light / Dark / System)
 */
@Composable
private fun ChoosePreferencesOnboardingPage(
    pageTheme: OnboardingPageTheme,
    currentLanguage: String,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit
) {
    val isHindi = currentLanguage == "hi"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Sacred Logo with Gold Halo
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = pageTheme.iconColor.copy(alpha = 0.4f)
                )
                .border(
                    width = 2.5.dp,
                    color = pageTheme.iconColor,
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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(pageTheme.titleRes),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = pageTheme.titleColor,
                letterSpacing = (-0.2).sp
            ),
            textAlign = TextAlign.Center
        )

        // Motif flourish
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(1.dp)
                    .background(pageTheme.iconColor.copy(alpha = 0.45f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = pageTheme.motifIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = pageTheme.iconColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(1.dp)
                    .background(pageTheme.iconColor.copy(alpha = 0.45f))
            )
        }

        Text(
            text = stringResource(pageTheme.descRes),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = pageTheme.descColor,
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Language Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, pageTheme.iconColor.copy(alpha = 0.25f))
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
                        tint = pageTheme.accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pref_language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = pageTheme.titleColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(pageTheme.backgroundColor)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SegmentedLanguageItem(
                        label = "English",
                        isSelected = currentLanguage == "en",
                        accentColor = pageTheme.accentColor,
                        onClick = { onLanguageChange("en") },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedLanguageItem(
                        label = "हिंदी",
                        isSelected = currentLanguage == "hi",
                        accentColor = pageTheme.accentColor,
                        onClick = { onLanguageChange("hi") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Theme Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, pageTheme.iconColor.copy(alpha = 0.25f))
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
                        tint = pageTheme.accentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pref_theme),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = pageTheme.titleColor
                        )
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
                        .background(pageTheme.backgroundColor)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SegmentedThemeItem(
                        icon = Icons.Default.LightMode,
                        label = lightLabel,
                        isSelected = currentTheme == "light",
                        accentColor = pageTheme.accentColor,
                        onClick = { onThemeChange("light") },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedThemeItem(
                        icon = Icons.Default.DarkMode,
                        label = darkLabel,
                        isSelected = currentTheme == "dark",
                        accentColor = pageTheme.accentColor,
                        onClick = { onThemeChange("dark") },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentedThemeItem(
                        icon = Icons.Default.BrightnessAuto,
                        label = systemLabel,
                        isSelected = currentTheme == "system",
                        accentColor = pageTheme.accentColor,
                        onClick = { onThemeChange("system") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SegmentedLanguageItem(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
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
                        .background(accentColor)
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
            color = if (isSelected) Color.White else Color(0xFF333333),
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
    accentColor: Color,
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
                        .background(accentColor)
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
                tint = if (isSelected) Color.White else Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF333333),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
