package com.sevadiary.app.feature.seva.data

import androidx.annotation.StringRes
import com.sevadiary.app.R

/**
 * Reusable Master Ashram Location entity containing stable identifier,
 * localized display name string resource, and state/region/country metadata.
 */
data class AshramLocation(
    val code: String,
    @param:StringRes val nameRes: Int,
    val stateOrCountry: String = "",
    val order: Int = 0
)

object AshramMasterData {
    val ashrams: List<AshramLocation> = listOf(
        AshramLocation(
            code = "DHANANA_DHAM",
            nameRes = R.string.ashram_dhanana_dham,
            stateOrCountry = "Haryana",
            order = 1
        ),
        AshramLocation(
            code = "KARONTHA_DHAM",
            nameRes = R.string.ashram_karontha_dham,
            stateOrCountry = "Haryana",
            order = 2
        ),
        AshramLocation(
            code = "KURUKSHETRA",
            nameRes = R.string.ashram_kurukshetra,
            stateOrCountry = "Haryana",
            order = 3
        ),
        AshramLocation(
            code = "MUNDKA",
            nameRes = R.string.ashram_mundka,
            stateOrCountry = "Delhi",
            order = 4
        ),
        AshramLocation(
            code = "DHURI",
            nameRes = R.string.ashram_dhuri,
            stateOrCountry = "Punjab",
            order = 5
        ),
        AshramLocation(
            code = "KHAMANO",
            nameRes = R.string.ashram_khamano,
            stateOrCountry = "Punjab",
            order = 6
        ),
        AshramLocation(
            code = "SHAMLI",
            nameRes = R.string.ashram_shamli,
            stateOrCountry = "Uttar Pradesh",
            order = 7
        ),
        AshramLocation(
            code = "SITAPUR",
            nameRes = R.string.ashram_sitapur,
            stateOrCountry = "Uttar Pradesh",
            order = 8
        ),
        AshramLocation(
            code = "SOJAT",
            nameRes = R.string.ashram_sojat,
            stateOrCountry = "Rajasthan",
            order = 9
        ),
        AshramLocation(
            code = "BETUL",
            nameRes = R.string.ashram_betul,
            stateOrCountry = "Madhya Pradesh",
            order = 10
        ),
        AshramLocation(
            code = "INDORE",
            nameRes = R.string.ashram_indore,
            stateOrCountry = "Madhya Pradesh",
            order = 11
        ),
        AshramLocation(
            code = "DHAWALAPURI",
            nameRes = R.string.ashram_dhawalapuri,
            stateOrCountry = "Maharashtra",
            order = 12
        ),
        AshramLocation(
            code = "DHANUSHA",
            nameRes = R.string.ashram_dhanusha,
            stateOrCountry = "Nepal",
            order = 13
        ),
        AshramLocation(
            code = "DHARWAD",
            nameRes = R.string.ashram_dharwad,
            stateOrCountry = "Karnataka",
            order = 14
        ),
        // Preserving existing Barwala, Rohtak, Bhiwani for backward compatibility
        AshramLocation(
            code = "BARWALA",
            nameRes = R.string.ashram_barwala,
            stateOrCountry = "Haryana",
            order = 15
        ),
        AshramLocation(
            code = "ROHTAK",
            nameRes = R.string.ashram_rohtak,
            stateOrCountry = "Haryana",
            order = 16
        ),
        AshramLocation(
            code = "BHIWANI",
            nameRes = R.string.ashram_bhiwani,
            stateOrCountry = "Haryana",
            order = 17
        ),
        AshramLocation(
            code = "OTHER_ASHRAM",
            nameRes = R.string.ashram_other,
            stateOrCountry = "",
            order = 99
        )
    )

    fun findByCode(code: String?): AshramLocation? {
        if (code.isNullOrBlank()) return null
        return ashrams.find { it.code.equals(code, ignoreCase = true) }
            ?: if (code.equals("KHAMANON", ignoreCase = true)) {
                ashrams.find { it.code == "KHAMANO" }
            } else null
    }

    fun isOtherAshram(code: String?): Boolean {
        if (code.isNullOrBlank()) return false
        val c = code.trim().uppercase()
        return c == "OTHER_ASHRAM" || c == "OTHER" || c.startsWith("OTHER")
    }

    fun isOtherPurpose(code: String?): Boolean {
        if (code.isNullOrBlank()) return false
        val c = code.trim().uppercase()
        return c == "OTHER_PURPOSE" || c == "OTHER" || c.startsWith("OTHER")
    }

    fun isOtherSeva(code: String?): Boolean {
        if (code.isNullOrBlank()) return false
        val c = code.trim().uppercase()
        return c == "OTHER_SEVA" || c == "OTHER" || c.startsWith("OTHER")
    }

    fun getPurposeNameRes(code: String?): Int {
        return when (code?.trim()?.uppercase()) {
            "SATSANG" -> R.string.purpose_satsang
            "SEVA" -> R.string.purpose_seva
            "BHANDARA" -> R.string.purpose_bhandara
            "PROGRAMME" -> R.string.purpose_programme
            "NAAM_DAAN" -> R.string.purpose_naam_daan
            "OTHER_PURPOSE", "OTHER" -> R.string.purpose_other
            else -> R.string.purpose_other
        }
    }

    fun getSevaTypeNameRes(code: String?): Int {
        return when (code?.trim()?.uppercase()) {
            "BHANDARA_SEVA" -> R.string.seva_bhandara
            "SAFAI_SEVA" -> R.string.seva_safai
            "VYAVASTHA_SEVA" -> R.string.seva_vyavastha
            "JAL_SEVA" -> R.string.seva_jal
            "SECURITY_SEVA" -> R.string.seva_security
            "IT_MEDIA_SEVA" -> R.string.seva_it_media
            "OTHER_SEVA", "OTHER" -> R.string.seva_other
            else -> R.string.seva_other
        }
    }

    val defaultPurposes: List<MasterDataItem> = listOf(
        MasterDataItem("SATSANG", "Satsang (Spiritual Discourse)", "सत्संग"),
        MasterDataItem("SEVA", "Seva (Selfless Service)", "सेवा"),
        MasterDataItem("BHANDARA", "Bhandara (Community Meal)", "भंडारा"),
        MasterDataItem("PROGRAMME", "Ashram Event / Programme", "आश्रम समागम / कार्यक्रम"),
        MasterDataItem("NAAM_DAAN", "Naam Daan Related Visit", "नाम दान संबंधी यात्रा"),
        MasterDataItem("OTHER_PURPOSE", "Other", "अन्य")
    )

    val defaultSevaTypes: List<MasterDataItem> = listOf(
        MasterDataItem("BHANDARA_SEVA", "Bhandara Seva (Kitchen & Meal)", "भंडारा सेवा"),
        MasterDataItem("SAFAI_SEVA", "Sanitation & Cleanliness (Safai)", "सफाई सेवा"),
        MasterDataItem("VYAVASTHA_SEVA", "Discipline & Crowd Management", "व्यवस्था / अनुशासन सेवा"),
        MasterDataItem("JAL_SEVA", "Water Distribution (Jal Seva)", "जल सेवा"),
        MasterDataItem("SECURITY_SEVA", "Premises Security", "सुरक्षा सेवा"),
        MasterDataItem("IT_MEDIA_SEVA", "IT, Sound & Broadcasting", "ध्वनि एवं प्रसारण सेवा"),
        MasterDataItem("OTHER_SEVA", "Other Seva", "अन्य सेवा")
    )
}

data class MasterDataItem(
    val code: String,
    val enLabel: String,
    val hiLabel: String
)
