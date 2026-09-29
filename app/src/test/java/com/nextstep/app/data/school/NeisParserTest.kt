package com.nextstep.app.data.school

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class NeisParserTest {
    @Test
    fun readsSchoolsAndSchedule() {
        val schools = """{"schoolInfo":[{"head":[{"list_total_count":1},{"RESULT":{"CODE":"INFO-000","MESSAGE":"정상 처리되었습니다."}}]},
            {"row":[{"ATPT_OFCDC_SC_CODE":"B10","SD_SCHUL_CODE":"7010057","SCHUL_NM":"서울대치초등학교","SCHUL_KND_SC_NM":"초등학교","ORG_RDNMA":"서울특별시 강남구"}]}]}"""
        val s = NeisParser.schools(schools).single()
        assertEquals("B10:7010057", s.key); assertEquals("서울대치초등학교", s.name); assertEquals("초등학교", s.kind)
        val schedule = """{"SchoolSchedule":[{"head":[{"list_total_count":2}]},{"row":[
            {"AA_YMD":"20290425","EVENT_NM":"1학기 중간고사","SBTR_DD_SC_NM":"해당없음","ONE_GRADE_EVENT_YN":"N","FIV_GRADE_EVENT_YN":"Y","SIX_GRADE_EVENT_YN":"Y"},
            {"AA_YMD":"20290504","EVENT_NM":" 재량휴업일 ","SBTR_DD_SC_NM":"휴업일"},
            {"AA_YMD":"bad","EVENT_NM":"x"}]}]}"""
        val days = NeisParser.schedule(schedule)
        assertEquals(2, days.size)
        assertEquals(LocalDate.of(2029, 4, 25), days[0].date); assertEquals(setOf(5, 6), days[0].grades)
        assertEquals("재량휴업일", days[1].name); assertTrue(days[1].dayOff)
    }

    @Test
    fun noDataIsEmptyAndErrorsThrow() {
        assertTrue(NeisParser.schedule("""{"RESULT":{"CODE":"INFO-200","MESSAGE":"해당하는 데이터가 없습니다."}}""").isEmpty())
        val e = assertThrows(NeisException::class.java) { NeisParser.schools("""{"RESULT":{"CODE":"ERROR-290","MESSAGE":"인증키가 유효하지 않습니다."}}""") }
        assertEquals("ERROR-290", e.code)
    }
}
