package com.sevadiary.app.core.network.dto

// Chanting DTOs
data class ChantingUpdateRequest(
    val count: Int? = null,
    val increment: Int? = null,
    val target: Int? = null
)

data class ChantingEntryDto(
    val date: String,
    val count: Int,
    val target: Int,
    val updatedAt: String? = null
)

data class ChantingSummaryDto(
    val today: Int,
    val thisWeek: Int,
    val thisMonth: Int,
    val allTimeTotal: Int,
    val totalActiveDays: Int
)

// Seva DTOs
data class CreateSevaRecordRequest(
    val ashramCode: String,
    val customAshramName: String = "",
    val startDate: String,
    val endDate: String,
    val purpose: String,
    val sevaType: String = "",
    val customPurposeOrSeva: String = "",
    val notes: String = "",
    val locationDetails: String = ""
)

data class SevaRecordDto(
    val id: String,
    val ashramCode: String,
    val customAshramName: String = "",
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val purpose: String,
    val sevaType: String = "",
    val customPurposeOrSeva: String = "",
    val notes: String = "",
    val locationDetails: String = "",
    val createdAt: String? = null
)

// Master Data DTOs
data class MasterDataResponse(
    val ashrams: List<MasterDataItemDto>,
    val purposes: List<MasterDataItemDto>,
    val sevaTypes: List<MasterDataItemDto>
)

data class MasterDataItemDto(
    val code: String,
    val labels: MasterDataLabelsDto,
    val location: MasterDataLocationDto? = null,
    val order: Int = 0
)

data class MasterDataLabelsDto(
    val en: String,
    val hi: String
)

data class MasterDataLocationDto(
    val city: String = "",
    val state: String = ""
)
