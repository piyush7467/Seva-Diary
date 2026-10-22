package com.sevadiary.app.feature.reports

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.DevotionalCard
import com.sevadiary.app.core.designsystem.component.DevotionalHeader
import com.sevadiary.app.core.designsystem.component.SevaOutlinedButton
import com.sevadiary.app.core.designsystem.component.SevaPrimaryButton
import com.sevadiary.app.core.designsystem.theme.ForestGreen
import com.sevadiary.app.core.designsystem.theme.SaffronGold
import com.sevadiary.app.feature.reports.presentation.ReportsViewModel

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    devotionalName: String,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DevotionalHeader(
                title = stringResource(R.string.reports_title),
                subtitle = "Generate & Share Devotional Summaries",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Choose Report Category",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ForestGreen)
            )

            // Ashram Visits Report Option
            ReportOptionCard(
                title = stringResource(R.string.report_type_ashram),
                subtitle = "Chronological visits, seva duties, inclusive duration, grouped by ashram",
                drawableRes = R.drawable.ic_devotional_ashram,
                isSelected = uiState.selectedReportType == "ASHRAM",
                onClick = { viewModel.selectReportType("ASHRAM") }
            )

            // Naam Jaap Report Option
            ReportOptionCard(
                title = stringResource(R.string.report_type_jaap),
                subtitle = "Daily Naam Jaap counts, targets, period summaries, and totals",
                drawableRes = R.drawable.counter_logo,
                isSelected = uiState.selectedReportType == "JAAP",
                onClick = { viewModel.selectReportType("JAAP") }
            )

            // Combined Report Option
            ReportOptionCard(
                title = stringResource(R.string.report_type_combined),
                subtitle = "Unified devotional report covering both Ashram Visits and Naam Jaap",
                drawableRes = R.drawable.ic_devotional_lotus,
                isSelected = uiState.selectedReportType == "COMBINED",
                onClick = { viewModel.selectReportType("COMBINED") }
            )

            // Generated PDF Preview & Sharing Card
            AnimatedVisibility(visible = uiState.generatedPdfFile != null) {
                val file = uiState.generatedPdfFile
                if (file != null) {
                    DevotionalCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ForestGreen.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = ForestGreen)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${file.length() / 1024} KB • Generated locally on device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SevaPrimaryButton(
                                text = stringResource(R.string.btn_share),
                                icon = Icons.Default.Share,
                                onClick = { viewModel.sharePdf(context, file) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Error Message
            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Generate Button
            SevaPrimaryButton(
                text = if (uiState.generatedPdfFile == null) stringResource(R.string.btn_generate_pdf) else "Regenerate PDF",
                icon = Icons.Default.PictureAsPdf,
                isLoading = uiState.isGenerating,
                onClick = { viewModel.generatePdf(context, devotionalName) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    drawableRes: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DevotionalCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = ForestGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ForestGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (drawableRes != null) {
                    Icon(
                        painter = painterResource(drawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (isSelected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (isSelected) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
