package com.sevadiary.app.feature.updater

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sevadiary.app.R
import com.sevadiary.app.feature.seva.data.rememberAshramColors
import java.io.File

@Composable
fun AppUpdateDialog(
    viewModel: AppUpdateViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = rememberAshramColors()
    val context = LocalContext.current

    val updateInfo = when (val state = uiState) {
        is UpdateUiState.UpdateAvailable -> state.updateInfo
        is UpdateUiState.Downloading -> state.updateInfo
        is UpdateUiState.ReadyToInstall -> state.updateInfo
        is UpdateUiState.Error -> state.updateInfo
        else -> return
    }

    val isDownloading = uiState is UpdateUiState.Downloading
    val downloadProgress = (uiState as? UpdateUiState.Downloading)?.progress ?: 0
    val readyApkFile: File? = (uiState as? UpdateUiState.ReadyToInstall)?.apkFile
    val errorStr: String? = (uiState as? UpdateUiState.Error)?.message

    Dialog(
        onDismissRequest = {
            if (!isDownloading && !updateInfo.isForceUpdate) {
                viewModel.dismissUpdate()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDownloading && !updateInfo.isForceUpdate,
            dismissOnClickOutside = !isDownloading && !updateInfo.isForceUpdate
        )
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardBg),
            border = BorderStroke(1.2.dp, colors.border),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Update Icon and Version
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.badgeSageBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = updateInfo.title.ifBlank { stringResource(R.string.app_update_available_title) },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.app_update_version_label, updateInfo.versionName),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.gold,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.app_update_available_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        lineHeight = 18.sp
                    )
                )

                // Release Notes Card (if present)
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bannerBg,
                        border = BorderStroke(1.dp, colors.bannerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .heightIn(max = 120.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.app_update_release_notes_label),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            )
                            Text(
                                text = updateInfo.releaseNotes,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.primaryText,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                            )
                        }
                    }
                }

                // Downloading Progress Indicator
                if (isDownloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.bannerBg
                        )
                        Text(
                            text = stringResource(R.string.app_update_downloading, downloadProgress),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Error Message if download failed
                if (!errorStr.isNullOrBlank()) {
                    Text(
                        text = errorStr,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Update Now / Install / Later
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!updateInfo.isForceUpdate && !isDownloading) {
                        TextButton(onClick = { viewModel.dismissUpdate() }) {
                            Text(
                                text = stringResource(R.string.btn_update_later),
                                color = colors.secondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (readyApkFile != null) {
                                viewModel.installDownloadedApk(context, readyApkFile)
                            } else {
                                viewModel.startDownload(context, updateInfo)
                            }
                        },
                        enabled = !isDownloading,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (readyApkFile != null) "Install Now" else stringResource(R.string.btn_update_now),
                            color = colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
