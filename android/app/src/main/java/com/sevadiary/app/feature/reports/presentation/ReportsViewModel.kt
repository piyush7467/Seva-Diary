package com.sevadiary.app.feature.reports.presentation

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.pdf.AshramVisitPdfItem
import com.sevadiary.app.core.pdf.ChantingPdfItem
import com.sevadiary.app.core.pdf.PdfReportGenerator
import com.sevadiary.app.feature.chanting.data.ChantingRepository
import com.sevadiary.app.feature.seva.data.AshramMasterData
import com.sevadiary.app.feature.seva.data.SevaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ReportsUiState(
    val selectedReportType: String = "ASHRAM", // ASHRAM, JAAP, COMBINED
    val isGenerating: Boolean = false,
    val generatedPdfFile: File? = null,
    val totalRecordsFound: Int = 0,
    val errorMessage: String? = null
)

class ReportsViewModel(
    private val sevaRepository: SevaRepository,
    private val chantingRepository: ChantingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun selectReportType(type: String) {
        _uiState.value = _uiState.value.copy(selectedReportType = type, generatedPdfFile = null, errorMessage = null)
    }

    fun generatePdf(context: Context, devotionalName: String) {
        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val pdfGenerator = PdfReportGenerator(context)
                val type = _uiState.value.selectedReportType

                val generatedFile: File = withContext(Dispatchers.IO) {
                    when (type) {
                        "ASHRAM" -> {
                            val sevaResult = sevaRepository.getRecords(limit = 100)
                            val records = sevaResult.getOrDefault(emptyList()).map {
                                val isOtherAshram = AshramMasterData.isOtherAshram(it.ashramCode)
                                val resolvedAshramName = when {
                                    isOtherAshram && it.customAshramName.isNotBlank() -> it.customAshramName
                                    isOtherAshram -> "Other Ashram"
                                    else -> it.ashramCode.replace("_", " ")
                                }
                                val resolvedPurpose = if (AshramMasterData.isOtherPurpose(it.purpose) && it.customPurposeOrSeva.isNotBlank()) {
                                    it.customPurposeOrSeva
                                } else {
                                    it.purpose.replace("_", " ")
                                }
                                val resolvedSeva = if (AshramMasterData.isOtherSeva(it.sevaType) && it.customPurposeOrSeva.isNotBlank()) {
                                    it.customPurposeOrSeva
                                } else {
                                    it.sevaType.replace("_", " ")
                                }
                                AshramVisitPdfItem(
                                    ashramName = resolvedAshramName,
                                    startDate = it.startDate,
                                    endDate = it.endDate,
                                    durationDays = it.durationDays,
                                    purpose = resolvedPurpose,
                                    sevaType = resolvedSeva,
                                    notes = it.notes
                                )
                            }
                            pdfGenerator.generateAshramVisitsPdf(records, devotionalName)
                        }
                        "JAAP" -> {
                            val jaapResult = chantingRepository.getHistory()
                            val summaryResult = chantingRepository.getSummary()
                            val entries = jaapResult.getOrDefault(emptyList()).map {
                                ChantingPdfItem(date = it.date, count = it.count, target = it.target)
                            }
                            val total = summaryResult.getOrNull()?.allTimeTotal ?: 0
                            pdfGenerator.generateNaamJaapPdf(entries, devotionalName, total)
                        }
                        else -> { // COMBINED
                            val sevaResult = sevaRepository.getRecords(limit = 100)
                            val records = sevaResult.getOrDefault(emptyList()).map {
                                val isOtherAshram = AshramMasterData.isOtherAshram(it.ashramCode)
                                val resolvedAshramName = when {
                                    isOtherAshram && it.customAshramName.isNotBlank() -> it.customAshramName
                                    isOtherAshram -> "Other Ashram"
                                    else -> it.ashramCode.replace("_", " ")
                                }
                                val resolvedPurpose = if (AshramMasterData.isOtherPurpose(it.purpose) && it.customPurposeOrSeva.isNotBlank()) {
                                    it.customPurposeOrSeva
                                } else {
                                    it.purpose.replace("_", " ")
                                }
                                val resolvedSeva = if (AshramMasterData.isOtherSeva(it.sevaType) && it.customPurposeOrSeva.isNotBlank()) {
                                    it.customPurposeOrSeva
                                } else {
                                    it.sevaType.replace("_", " ")
                                }
                                AshramVisitPdfItem(
                                    ashramName = resolvedAshramName,
                                    startDate = it.startDate,
                                    endDate = it.endDate,
                                    durationDays = it.durationDays,
                                    purpose = resolvedPurpose,
                                    sevaType = resolvedSeva,
                                    notes = it.notes
                                )
                            }
                            pdfGenerator.generateAshramVisitsPdf(records, devotionalName)
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generatedPdfFile = generatedFile
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = e.localizedMessage ?: "Failed to generate PDF report."
                )
            }
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Seva Diary Devotional Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Devotional Report"))
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Could not share PDF: ${e.message}")
        }
    }
}
