package com.sevadiary.app.feature.chanting.data

import com.sevadiary.app.core.network.SevaDiaryApiService
import com.sevadiary.app.core.network.dto.ChantingEntryDto
import com.sevadiary.app.core.network.dto.ChantingSummaryDto
import com.sevadiary.app.core.network.dto.ChantingUpdateRequest

class ChantingRepository(
    private val apiService: SevaDiaryApiService
) {

    suspend fun updateCount(date: String, count: Int? = null, increment: Int? = null, target: Int? = null): Result<ChantingEntryDto> {
        return try {
            val response = apiService.updateDailyChanting(
                date = date,
                request = ChantingUpdateRequest(count = count, increment = increment, target = target)
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to update chanting count."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error while saving count."))
        }
    }

    suspend fun getSummary(): Result<ChantingSummaryDto> {
        return try {
            val response = apiService.getChantingSummary()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to fetch summary."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error fetching summary."))
        }
    }

    suspend fun getHistory(startDate: String? = null, endDate: String? = null): Result<List<ChantingEntryDto>> {
        return try {
            val response = apiService.getChantingEntries(startDate, endDate)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to fetch history."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error fetching history."))
        }
    }
}
