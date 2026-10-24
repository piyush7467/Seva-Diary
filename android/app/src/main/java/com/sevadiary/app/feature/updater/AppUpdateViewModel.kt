package com.sevadiary.app.feature.updater

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.network.SevaDiaryApiService
import com.sevadiary.app.core.network.dto.AppUpdateDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    data class UpdateAvailable(val updateInfo: AppUpdateDto) : UpdateUiState()
    data class Downloading(val updateInfo: AppUpdateDto, val progress: Int) : UpdateUiState()
    data class ReadyToInstall(val updateInfo: AppUpdateDto, val apkFile: File) : UpdateUiState()
    data class Error(val updateInfo: AppUpdateDto, val message: String) : UpdateUiState()
    object Dismissed : UpdateUiState()
}

class AppUpdateViewModel(
    private val apiService: SevaDiaryApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    private var hasCheckedThisSession = false
    private var isDismissedByUser = false

    fun checkForUpdates(currentVersionCode: Int) {
        if (hasCheckedThisSession || isDismissedByUser) return
        hasCheckedThisSession = true

        viewModelScope.launch {
            try {
                val response = apiService.getAppVersion()
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    val updateData = response.body()!!.data!!

                    if (updateData.versionCode > currentVersionCode && updateData.downloadUrl.isNotBlank()) {
                        _uiState.value = UpdateUiState.UpdateAvailable(updateData)
                    }
                }
            } catch (_: Exception) {
                // Silently ignore network failures on startup so user is never interrupted
            }
        }
    }

    fun startDownload(context: Context, updateInfo: AppUpdateDto) {
        _uiState.value = UpdateUiState.Downloading(updateInfo, 0)

        viewModelScope.launch {
            val result = AppUpdateDownloader.downloadApk(
                context = context,
                downloadUrl = updateInfo.downloadUrl,
                versionCode = updateInfo.versionCode,
                onProgress = { progress ->
                    _uiState.value = UpdateUiState.Downloading(updateInfo, progress)
                }
            )

            result.fold(
                onSuccess = { apkFile ->
                    _uiState.value = UpdateUiState.ReadyToInstall(updateInfo, apkFile)
                    AppUpdateDownloader.launchInstaller(context, apkFile)
                },
                onFailure = { error ->
                    _uiState.value = UpdateUiState.Error(
                        updateInfo,
                        error.localizedMessage ?: "Failed to download update."
                    )
                }
            )
        }
    }

    fun installDownloadedApk(context: Context, apkFile: File) {
        AppUpdateDownloader.launchInstaller(context, apkFile)
    }

    fun dismissUpdate() {
        isDismissedByUser = true
        _uiState.value = UpdateUiState.Dismissed
    }
}
