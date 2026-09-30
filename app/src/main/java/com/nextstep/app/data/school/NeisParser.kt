package com.nextstep.app.data.school

import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.school.SchoolDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.json.JSONObject

/**
 * NEIS JSON 응답 읽기. 정상 응답은 {"서비스명":[{"head":[…]},{"row":[…]}]}, 결과가 없으면 {"RESULT":{"CODE":"INFO-200"}}.
 * 결과 없음은 빈 목록, 그 밖의 결과 코드는 [NeisException].
 */
object NeisParser {

    fun schools(json: String): List<School> = rows(json, "schoolInfo").map {
        School(it.optString("ATPT_OFCDC_SC_CODE"), it.optString("SD_SCHUL_CODE"), it.optString("SCHUL_NM"), it.optString("SCHUL_KND_SC_NM"), it.optString("ORG_RDNMA"))
    }.filter { it.officeCode.isNotBlank() && it.code.isNotBlank() }

    fun schedule(json: String): List<SchoolDay> = rows(json, "SchoolSchedule").mapNotNull { r ->
        val date = runCatching { LocalDate.parse(r.optString("AA_YMD"), DateTimeFormatter.BASIC_ISO_DATE) }.getOrNull() ?: return@mapNotNull null
        SchoolDay(
            date = date, name = r.optString("EVENT_NM").trim(), dayOff = r.optString("SBTR_DD_SC_NM") in DAY_OFF,
            grades = GRADE_KEYS.mapIndexedNotNull { i, key -> (i + 1).takeIf { r.optString(key) == "Y" } }.toSet(),
        )
    }

    private fun rows(json: String, service: String): List<JSONObject> {
        val root = JSONObject(json)
        val blocks = root.optJSONArray(service) ?: run {
            val result = root.optJSONObject("RESULT")
            val code = result?.optString("CODE").orEmpty()
            if (code == NO_DATA) return emptyList()
            throw NeisException(code, result?.optString("MESSAGE").orEmpty())
        }
        val rowArray = (0 until blocks.length()).firstNotNullOfOrNull { blocks.optJSONObject(it)?.optJSONArray("row") } ?: return emptyList()
        return (0 until rowArray.length()).mapNotNull { rowArray.optJSONObject(it) }
    }

    private const val NO_DATA = "INFO-200"
    private val DAY_OFF = setOf("휴업일", "공휴일")
    private val GRADE_KEYS = listOf("ONE_GRADE_EVENT_YN", "TW_GRADE_EVENT_YN", "THREE_GRADE_EVENT_YN", "FR_GRADE_EVENT_YN", "FIV_GRADE_EVENT_YN", "SIX_GRADE_EVENT_YN")
}
