package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.YearArea.BODY
import com.nextstep.app.domain.year.YearArea.MIND
import com.nextstep.app.domain.year.YearDoer.CHILD
import com.nextstep.app.domain.year.YearDoer.PARENT
import com.nextstep.app.domain.year.YearDoer.TOGETHER
import com.nextstep.app.domain.year.YearTerm.ALL_YEAR
import com.nextstep.app.domain.year.YearTerm.FIRST
import com.nextstep.app.domain.year.YearTerm.SECOND

/** 건강 표([PreschoolHealth]·[SchoolHealth])가 같이 쓰는 줄 만들기와, 여러 해에 되풀이되는 항목. */
internal object HealthRows {
    fun x(area: YearArea, term: YearTerm, who: YearDoer, title: String, how: String) = YearTask(area, term, title, how, who)

    val FLU = x(BODY, SECOND, PARENT, "인플루엔자 접종", "매년 10~11월, 생후 6개월~만 13세 무료")
    val DENTAL = x(BODY, ALL_YEAR, PARENT, "치과 정기검진", "6개월마다, 불소 도포 함께")
    val PUBERTY_EARLY = x(BODY, ALL_YEAR, PARENT, "성조숙 신호 살피기", "여아 8세 전 가슴 멍울, 남아 9세 전 고환 커짐이면 소아내분비 상담")
    val MYOPIA = x(BODY, ALL_YEAR, TOGETHER, "근시 늦추기", "하루 2시간 바깥 활동, 가까이 보기 40분마다 쉬기")
    val MOVE = x(BODY, ALL_YEAR, CHILD, "하루 60분 몸 쓰기", "숨이 찰 정도로, 주 3회는 격하게(WHO 권고)")
    val SCHOOL_CHECK = x(BODY, FIRST, PARENT, "학생 건강검진", "검진기관에서, 학교 안내문 확인")
    val SCHOOL_SURVEY = x(BODY, FIRST, CHILD, "신체발달·건강조사", "학교에서 키·몸무게·시력 측정")
    val DEPRESSION = x(MIND, ALL_YEAR, PARENT, "우울 신호 알기", "2주 넘는 무기력·짜증·잠 변화면 Wee클래스나 청소년상담 1388")
}
