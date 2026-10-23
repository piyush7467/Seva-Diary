package com.sevadiary.app.feature.chanting.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.network.dto.ChantingEntryDto
import com.sevadiary.app.core.network.dto.ChantingSummaryDto
import com.sevadiary.app.feature.chanting.data.ChantingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChantingUiState(
    val todayCount: Int = 0,
    val target: Int = 108,
    val summary: ChantingSummaryDto = ChantingSummaryDto(0, 0, 0, 0, 0),
    val history: List<ChantingEntryDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChantingViewModel(
    private val repository: ChantingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChantingUiState())
    val uiState: StateFlow<ChantingUiState> = _uiState.asStateFlow()

    private val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            // Load summary
            val summaryResult = repository.getSummary()
            summaryResult.fold(
                onSuccess = { summary ->
                    _uiState.value = _uiState.value.copy(
                        todayCount = summary.today,
                        summary = summary
                    )
                },
                onFailure = { /* Retain current count */ }
            )

            // Load history
            val historyResult = repository.getHistory()
            historyResult.fold(
                onSuccess = { list ->
                    val todayEntry = list.find { it.date == todayDateStr }
                    _uiState.value = _uiState.value.copy(
                        history = list,
                        todayCount = todayEntry?.count ?: _uiState.value.todayCount,
                        target = todayEntry?.target ?: _uiState.value.target,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun increment() {
        val newCount = _uiState.value.todayCount + 1
        val currentTarget = _uiState.value.target
        _uiState.value = _uiState.value.copy(todayCount = newCount)

        viewModelScope.launch {
            repository.updateCount(
                date = todayDateStr,
                increment = 1,
                target = currentTarget
            )
        }
    }

    fun decrement() {
        if (_uiState.value.todayCount <= 0) return
        val newCount = _uiState.value.todayCount - 1
        _uiState.value = _uiState.value.copy(todayCount = newCount)

        viewModelScope.launch {
            repository.updateCount(
                date = todayDateStr,
                increment = -1,
                target = _uiState.value.target
            )
        }
    }

    fun resetToday() {
        _uiState.value = _uiState.value.copy(todayCount = 0)
        viewModelScope.launch {
            repository.updateCount(
                date = todayDateStr,
                count = 0,
                target = _uiState.value.target
            )
            loadData()
        }
    }

    fun setTarget(newTarget: Int) {
        if (newTarget < 1) return
        _uiState.value = _uiState.value.copy(target = newTarget)
        viewModelScope.launch {
            repository.updateCount(
                date = todayDateStr,
                target = newTarget
            )
        }
    }
}
