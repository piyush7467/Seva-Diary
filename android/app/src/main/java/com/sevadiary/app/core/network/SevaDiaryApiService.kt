package com.sevadiary.app.core.network

import com.sevadiary.app.core.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SevaDiaryApiService {

    // Auth Endpoints
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserProfileDto>>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponseData>>

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<ApiResponse<RefreshResponseData>>

    @POST("api/v1/auth/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<ApiResponse<Map<String, String>>>

    @POST("api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Map<String, String>>>

    @POST("api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Map<String, String>>>

    @POST("api/v1/auth/change-password/request-otp")
    suspend fun requestChangePasswordOtp(): Response<ApiResponse<Map<String, String>>>

    @POST("api/v1/auth/change-password/verify-otp")
    suspend fun verifyChangePasswordOtp(@Body request: ChangePasswordOtpRequest): Response<ApiResponse<Map<String, String>>>

    // User Profile
    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<ApiResponse<UserProfileDto>>

    @PATCH("api/v1/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserProfileDto>>

    // Chanting
    @GET("api/v1/chanting")
    suspend fun getChantingEntries(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<List<ChantingEntryDto>>>

    @GET("api/v1/chanting/summary")
    suspend fun getChantingSummary(): Response<ApiResponse<ChantingSummaryDto>>

    @PUT("api/v1/chanting/{date}")
    suspend fun updateDailyChanting(
        @Path("date") date: String,
        @Body request: ChantingUpdateRequest
    ): Response<ApiResponse<ChantingEntryDto>>

    // Seva
    @GET("api/v1/seva")
    suspend fun getSevaRecords(
        @Query("ashramCode") ashramCode: String? = null,
        @Query("purpose") purpose: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<SevaRecordDto>>>

    @POST("api/v1/seva")
    suspend fun createSevaRecord(@Body request: CreateSevaRecordRequest): Response<ApiResponse<SevaRecordDto>>

    @GET("api/v1/seva/{id}")
    suspend fun getSevaRecordById(@Path("id") id: String): Response<ApiResponse<SevaRecordDto>>

    @PATCH("api/v1/seva/{id}")
    suspend fun updateSevaRecord(
        @Path("id") id: String,
        @Body request: CreateSevaRecordRequest
    ): Response<ApiResponse<SevaRecordDto>>

    @DELETE("api/v1/seva/{id}")
    suspend fun deleteSevaRecord(@Path("id") id: String): Response<ApiResponse<Map<String, String>>>

    // Master Data
    @GET("api/v1/master-data")
    suspend fun getMasterData(): Response<ApiResponse<MasterDataResponse>>
}
