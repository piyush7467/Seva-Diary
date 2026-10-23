package com.sevadiary.app.feature.chanting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Data model representing each step in the 6-step Naam Jaap devotional onboarding.
 */
data class JaapOnboardingStep(
    val stepIndex: Int,
    @param:DrawableRes val imageRes: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val hindiRes: Int,
    @param:StringRes val englishRes: Int
)

val jaapOnboardingSteps = listOf(
    JaapOnboardingStep(
        stepIndex = 1,
        imageRes = R.drawable.chantingonboarding1,
        titleRes = R.string.jaap_onboarding_step_1_title,
        hindiRes = R.string.jaap_onboarding_step_1_hi,
        englishRes = R.string.jaap_onboarding_step_1_en
    ),
    JaapOnboardingStep(
        stepIndex = 2,
        imageRes = R.drawable.chantingonboarding2,
        titleRes = R.string.jaap_onboarding_step_2_title,
        hindiRes = R.string.jaap_onboarding_step_2_hi,
        englishRes = R.string.jaap_onboarding_step_2_en
    ),
    JaapOnboardingStep(
        stepIndex = 3,
        imageRes = R.drawable.chantingonboarding3,
        titleRes = R.string.jaap_onboarding_step_3_title,
        hindiRes = R.string.jaap_onboarding_step_3_hi,
        englishRes = R.string.jaap_onboarding_step_3_en
    ),
    JaapOnboardingStep(
        stepIndex = 4,
        imageRes = R.drawable.chantingonboarding4,
        titleRes = R.string.jaap_onboarding_step_4_title,
        hindiRes = R.string.jaap_onboarding_step_4_hi,
        englishRes = R.string.jaap_onboarding_step_4_en
    ),
    JaapOnboardingStep(
        stepIndex = 5,
        imageRes = R.drawable.chantingonboarding5,
        titleRes = R.string.jaap_onboarding_step_5_title,
        hindiRes = R.string.jaap_onboarding_step_5_hi,
        englishRes = R.string.jaap_onboarding_step_5_en
    ),
    JaapOnboardingStep(
        stepIndex = 6,
        imageRes = R.drawable.chantingonboarding6,
        titleRes = R.string.jaap_onboarding_step_6_title,
        hindiRes = R.string.jaap_onboarding_step_6_hi,
        englishRes = R.string.jaap_onboarding_step_6_en
    )
)

// Devotional Palette
private val DevotionalWarmIvory = Color(0xFFFFF8E8)
private val DevotionalForestGreen = Color(0xFF174C38)
private val DevotionalDeepGreen = Color(0xFF103C2D)
private val DevotionalSaffronGold = Color(0xFFD99A32)
private val DevotionalLightGold = Color(0xFFF4D58A)
private val DevotionalDarkText = Color(0xFF26352D)

@Composable
fun JaapOnboardingScreen(
    onFinishOnboarding: () -> Unit,
    onBackClick: () -> Unit
) {
    val steps = jaapOnboardingSteps
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DevotionalWarmIvory)
    ) {
        // 1. Full-screen background artwork pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = steps[page].imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
        }

        // 2. Soft translucent cream gradient overlay over bottom section
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            0.70f to Color(0x66FFF8E8),
                            0.95f to Color(0xF2FFF8E8),
                            1.0f to Color(0xFFFFF8E8)
                        )
                    )
                )
        )

        // 3. Foreground content layer
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Back icon, Title, Step counter, Skip button
            OnboardingHeader(
                pagerState = pagerState,
                totalSteps = steps.size,
                onBackClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else {
                        onBackClick()
                    }
                },
                onSkipClick = onFinishOnboarding
            )

            // Flexible space letting saint portrait remain visible and clear
            Spacer(modifier = Modifier.weight(1f))

            // Golden Message Card displaying the saint/greeting reverence
            GoldenMessageCard(
                step = steps[pagerState.currentPage],
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator Dots (1 to 6)
            OnboardingProgressIndicator(
                pagerState = pagerState,
                totalSteps = steps.size
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Navigation Buttons: Back and Next / Start Jaap
            OnboardingNavigationButtons(
                pagerState = pagerState,
                totalSteps = steps.size,
                coroutineScope = coroutineScope,
                onBackClick = onBackClick,
                onStartJaap = onFinishOnboarding
            )
        }
    }
}

/**
 * Top App Header with back arrow, "Naam Jaap" title, step indicator "1 / 6", and Skip button.
 */
@Composable
private fun OnboardingHeader(
    pagerState: PagerState,
    totalSteps: Int,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back circular/pill button
        Surface(
            onClick = onBackClick,
            shape = CircleShape,
            color = Color(0xEBFFF8E8),
            border = BorderStroke(1.dp, DevotionalSaffronGold.copy(alpha = 0.5f)),
            shadowElevation = 2.dp,
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = DevotionalForestGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Screen Title: "Naam Jaap"
        Text(
            text = stringResource(R.string.jaap_onboarding_header_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = DevotionalDeepGreen,
                fontSize = 18.sp
            )
        )

        // Step counter & Skip button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${pagerState.currentPage + 1} / $totalSteps",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DevotionalDarkText,
                    fontSize = 14.sp
                )
            )

            Surface(
                onClick = onSkipClick,
                shape = RoundedCornerShape(18.dp),
                color = Color(0xEBFFF8E8),
                border = BorderStroke(1.dp, DevotionalSaffronGold.copy(alpha = 0.5f)),
                shadowElevation = 2.dp
            ) {
                Text(
                    text = stringResource(R.string.btn_skip),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DevotionalForestGreen,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Premium Golden Message Card with saffron gold border, devotional lotus ornaments,
 * and prominent reverence typography.
 */
@Composable
private fun GoldenMessageCard(
    step: JaapOnboardingStep,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = DevotionalSaffronGold.copy(alpha = 0.35f)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFAFFFDF8),
        border = BorderStroke(1.5.dp, DevotionalSaffronGold.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Lotus motif
            Icon(
                painter = painterResource(id = R.drawable.ic_devotional_lotus),
                contentDescription = null,
                tint = DevotionalSaffronGold,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Reverence Title (Hindi)
            Text(
                text = stringResource(id = step.hindiRes),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = DevotionalForestGreen,
                    fontSize = 21.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Ornamental Divider with central miniature lotus
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(DevotionalSaffronGold.copy(alpha = 0.35f))
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_devotional_lotus),
                    contentDescription = null,
                    tint = DevotionalSaffronGold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(DevotionalSaffronGold.copy(alpha = 0.35f))
                )
            }

            // Secondary Reverence Subtitle (English)
            Text(
                text = stringResource(id = step.englishRes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = DevotionalDarkText,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 6-step progress dots with animated width and devotional colors.
 */
@Composable
private fun OnboardingProgressIndicator(
    pagerState: PagerState,
    totalSteps: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isSelected = pagerState.currentPage == index
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) 20.dp else 8.dp,
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(dotWidth)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) DevotionalForestGreen else Color(0x4026352D)
                    )
            )
        }
    }
}

/**
 * Bottom action buttons: "< Back" in soft ivory and "Next →" / "Start Jaap →" in forest green.
 */
@Composable
private fun OnboardingNavigationButtons(
    pagerState: PagerState,
    totalSteps: Int,
    coroutineScope: CoroutineScope,
    onBackClick: () -> Unit,
    onStartJaap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        val isFirstPage = pagerState.currentPage == 0
        Surface(
            onClick = {
                if (isFirstPage) {
                    onBackClick()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            },
            shape = RoundedCornerShape(26.dp),
            color = Color(0xEBFFF8E8),
            border = BorderStroke(1.dp, DevotionalSaffronGold.copy(alpha = 0.6f)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = DevotionalForestGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.btn_back),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DevotionalForestGreen,
                        fontSize = 15.sp
                    )
                )
            }
        }

        // Next / Start Jaap Button
        val isLastPage = pagerState.currentPage == totalSteps - 1
        Surface(
            onClick = {
                if (isLastPage) {
                    onStartJaap()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            shape = RoundedCornerShape(26.dp),
            color = DevotionalForestGreen,
            shadowElevation = 3.dp,
            modifier = Modifier
                .weight(1.3f)
                .height(52.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = if (isLastPage) {
                        stringResource(R.string.btn_start_jaap)
                    } else {
                        stringResource(R.string.btn_next)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
