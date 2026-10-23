package com.sevadiary.app.feature.seva.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sevadiary.app.core.network.dto.CreateSevaRecordRequest
import com.sevadiary.app.core.network.dto.MasterDataResponse
import com.sevadiary.app.core.network.dto.SevaRecordDto
import com.sevadiary.app.feature.seva.data.SevaRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class SevaUiState(
    val records: List<SevaRecordDto> = emptyList(),
    val masterData: MasterDataResponse? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val filterAshram: String? = null,
    val filterPurpose: String? = null
)

class SevaViewModel(
    private val repository: SevaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SevaUiState())
    val uiState: StateFlow<SevaUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
        loadMasterData()
    }

    fun loadMasterData() {
        viewModelScope.launch {
            try {
                withTimeout(6000L) {
                    val result = repository.getMasterData()
                    result.onSuccess { data ->
                        _uiState.value = _uiState.value.copy(masterData = data)
                    }
                }
            } catch (_: Exception) {
                // Non-critical: defaults will be used for dropdowns
            }
        }
    }

    fun loadRecords() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                withTimeout(8000L) {
                    val result = repository.getRecords(
                        ashramCode = _uiState.value.filterAshram,
                        purpose = _uiState.value.filterPurpose,
                        search = _uiState.value.searchQuery.ifBlank { null }
                    )
                    result.fold(
                        onSuccess = { list ->
                            _uiState.value = _uiState.value.copy(
                                records = list,
                                isLoading = false,
                                errorMessage = null
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
            } catch (e: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Server response timed out. Please check your connection."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Unable to load visits."
                )
            } finally {
                // Guaranteed failsafe: isLoading will NEVER remain true
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadRecords()
    }

    fun onFilterAshram(ashramCode: String?) {
        _uiState.value = _uiState.value.copy(filterAshram = ashramCode)
        loadRecords()
    }

    fun onFilterPurpose(purposeCode: String?) {
        _uiState.value = _uiState.value.copy(filterPurpose = purposeCode)
        loadRecords()
    }

    fun deleteRecord(id: String) {
        viewModelScope.launch {
            val result = repository.deleteRecord(id)
            result.onSuccess {
                loadRecords()
            }
        }
    }

    fun saveVisit(
        recordId: String? = null,
        ashramCode: String,
        customAshramName: String,
        startDate: String,
        endDate: String,
        purpose: String,
        sevaType: String,
        customPurposeOrSeva: String,
        notes: String,
        locationDetails: String
    ) {
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, saveSuccess = false)
        viewModelScope.launch {
            try {
                withTimeout(10000L) {
                    val request = CreateSevaRecordRequest(
                        ashramCode = ashramCode,
                        customAshramName = customAshramName.trim(),
                        startDate = startDate,
                        endDate = endDate,
                        purpose = purpose,
                        sevaType = if (purpose == "SEVA") sevaType else "",
                        customPurposeOrSeva = customPurposeOrSeva.trim(),
                        notes = notes.trim(),
                        locationDetails = locationDetails.trim()
                    )

                    val result = if (recordId != null) {
                        repository.updateRecord(recordId, request)
                    } else {
                        repository.createRecord(request)
                    }

                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                            loadRecords()
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = error.message)
                        }
                    )
                }
            } catch (e: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Save request timed out. Please retry.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.localizedMessage ?: "Unable to save visit record.")
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false, errorMessage = null)
    }
}
