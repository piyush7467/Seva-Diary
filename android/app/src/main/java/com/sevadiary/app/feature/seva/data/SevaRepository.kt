package com.sevadiary.app.feature.seva.data

import com.sevadiary.app.core.network.SevaDiaryApiService
import com.sevadiary.app.core.network.dto.*

class SevaRepository(
    private val apiService: SevaDiaryApiService
) {

    private fun parseErrorMessage(response: retrofit2.Response<*>): String {
        return try {
            val raw = response.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                val json = org.json.JSONObject(raw)
                if (json.has("error")) {
                    val err = json.getJSONObject("error")
                    err.optString("message", "Request failed (HTTP ${response.code()})")
                } else {
                    raw
                }
            } else {
                "Request failed (HTTP ${response.code()})"
            }
        } catch (_: Exception) {
            "Request failed (HTTP ${response.code()})"
        }
    }

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
                Result.failure(Exception(parseErrorMessage(response)))
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
                Result.failure(Exception(parseErrorMessage(response)))
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
                Result.failure(Exception(parseErrorMessage(response)))
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
                Result.failure(Exception(parseErrorMessage(response)))
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
                Result.failure(Exception(parseErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Network error fetching master data."))
        }
    }
}
