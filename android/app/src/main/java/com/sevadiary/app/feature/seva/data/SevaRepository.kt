package com.sevadiary.app.feature.seva.data

import com.sevadiary.app.core.network.SevaDiaryApiService
import com.sevadiary.app.core.network.dto.*

class SevaRepository(
    private val apiService: SevaDiaryApiService
) {

    suspend fun getRecords(
        ashramCode: String? = null,
        purpose: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        search: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): Result<List<SevaRecordDto>> {
        return try {
            val response = apiService.getSevaRecords(
                ashramCode = ashramCode,
                purpose = purpose,
                startDate = startDate,
                endDate = endDate,
                search = search,
                page = page,
                limit = limit
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to fetch seva records."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error fetching seva records."))
        }
    }

    suspend fun createRecord(request: CreateSevaRecordRequest): Result<SevaRecordDto> {
        return try {
            val response = apiService.createSevaRecord(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to save visit record."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error saving visit record."))
        }
    }

    suspend fun updateRecord(id: String, request: CreateSevaRecordRequest): Result<SevaRecordDto> {
        return try {
            val response = apiService.updateSevaRecord(id, request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.error?.message ?: "Failed to update visit record."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error updating visit record."))
        }
    }

    suspend fun deleteRecord(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteSevaRecord(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete record."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error deleting record."))
        }
    }

    suspend fun getMasterData(): Result<MasterDataResponse> {
        return try {
            val response = apiService.getMasterData()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to fetch master data."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error fetching master data."))
        }
    }
}
