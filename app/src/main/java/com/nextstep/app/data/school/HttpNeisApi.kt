package com.nextstep.app.data.school

import com.nextstep.app.domain.school.School
import com.nextstep.app.domain.school.SchoolDay
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** NEIS 오픈 API 를 HTTPS 로 부릅니다. 인증키([key])는 빌드 설정(NEIS_API_KEY)에서 옵니다. */
class HttpNeisApi(private val key: String) : NeisApi {
    override val available: Boolean get() = key.isNotBlank()

    override suspend fun searchSchools(name: String): List<School> =
        NeisParser.schools(get("schoolInfo", mapOf("SCHUL_NM" to name.trim(), "pSize" to SEARCH_SIZE)))

    override suspend fun schedule(school: School, from: LocalDate, to: LocalDate): List<SchoolDay> = NeisParser.schedule(
        get(
            "SchoolSchedule",
            mapOf(
                "ATPT_OFCDC_SC_CODE" to school.officeCode, "SD_SCHUL_CODE" to school.code,
                "AA_FROM_YMD" to from.format(DateTimeFormatter.BASIC_ISO_DATE), "AA_TO_YMD" to to.format(DateTimeFormatter.BASIC_ISO_DATE),
                "pSize" to SCHEDULE_SIZE,
            ),
        ),
    )

    private suspend fun get(service: String, params: Map<String, String>): String = withContext(Dispatchers.IO) {
        val query = (params + mapOf("KEY" to key, "Type" to "json", "pIndex" to "1"))
            .entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
        val connection = URL("$BASE/$service?$query").openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val BASE = "https://open.neis.go.kr/hub"
        const val SEARCH_SIZE = "20"
        /** 한 학년도 학사일정은 수백 줄이라 한 번에(NEIS 한 번 최대 1000). */
        const val SCHEDULE_SIZE = "1000"
        const val TIMEOUT_MS = 15_000
    }
}
