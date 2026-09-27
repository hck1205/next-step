package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.HealthRows.DENTAL
import com.nextstep.app.domain.year.HealthRows.DEPRESSION
import com.nextstep.app.domain.year.HealthRows.FLU
import com.nextstep.app.domain.year.HealthRows.MOVE
import com.nextstep.app.domain.year.HealthRows.MYOPIA
import com.nextstep.app.domain.year.HealthRows.PUBERTY_EARLY
import com.nextstep.app.domain.year.HealthRows.SCHOOL_CHECK
import com.nextstep.app.domain.year.HealthRows.SCHOOL_SURVEY
import com.nextstep.app.domain.year.HealthRows.x
import com.nextstep.app.domain.year.YearArea.BODY
import com.nextstep.app.domain.year.YearArea.LIFE
import com.nextstep.app.domain.year.YearArea.MIND
import com.nextstep.app.domain.year.YearDoer.CHILD
import com.nextstep.app.domain.year.YearDoer.PARENT
import com.nextstep.app.domain.year.YearDoer.TOGETHER
import com.nextstep.app.domain.year.YearTerm.ALL_YEAR
import com.nextstep.app.domain.year.YearTerm.FIRST
import com.nextstep.app.domain.year.YearTerm.SECOND

/** 학교부터(e1~g) 건강: 학교 건강검진·정서행동검사·수면·운동·마음. 출처와 모으는 곳은 [HealthPlans]. */
internal object SchoolHealth {
    val byYear: Map<String, List<YearTask>> = mapOf(
        "e1" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, PARENT, "학생정서·행동특성검사", "초1, 부모가 설문 작성"),
            x(MIND, FIRST, PARENT, "스마트폰 이용습관 진단", "초1, 부모가 설문"),
            FLU, DENTAL, MYOPIA, PUBERTY_EARLY,
            x(LIFE, FIRST, TOGETHER, "통학로 같이 걷기", "횡단보도·위험한 곳 표시"),
            x(MIND, ALL_YEAR, PARENT, "학교 스트레스 신호", "배 아픔·등교 거부가 2주 넘으면 담임과 상담"),
        ),
        "e2" to listOf(
            SCHOOL_SURVEY, FLU, DENTAL, MYOPIA, PUBERTY_EARLY,
            x(BODY, ALL_YEAR, PARENT, "두 번째 영구 어금니 전 점검", "홈메우기한 곳 떨어지지 않았는지"),
            x(MIND, ALL_YEAR, TOGETHER, "친구 관계 이야기", "저녁에 오늘 누구와 놀았는지"),
        ),
        "e3" to listOf(
            SCHOOL_SURVEY, FLU, DENTAL, MYOPIA, PUBERTY_EARLY,
            x(MIND, ALL_YEAR, PARENT, "학교폭력 신호 알기", "물건이 자주 없어지거나 등교를 피하면 담임·117"),
        ),
        "e4" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, PARENT, "학생정서·행동특성검사", "초4, 부모가 설문 작성"),
            x(MIND, FIRST, CHILD, "스마트폰 이용습관 진단", "초4, 학생이 직접"),
            x(BODY, SECOND, PARENT, "건강검진 결과 확인", "체질량지수(BMI)·시력·혈압"),
            x(BODY, ALL_YEAR, PARENT, "사춘기 시작 알기", "여아는 평균 10세 전후 가슴 발달, 초경은 평균 12세 전후"),
            FLU, DENTAL, MYOPIA, MOVE,
            x(MIND, ALL_YEAR, TOGETHER, "비교 대신 과정 칭찬", "형제·친구와 비교하지 않기"),
        ),
        "e5" to listOf(
            SCHOOL_SURVEY, FLU, DENTAL, MYOPIA, MOVE,
            x(BODY, ALL_YEAR, TOGETHER, "사춘기 준비", "생리대·속옷·면도기 미리, 몸의 변화 이야기"),
            x(BODY, ALL_YEAR, PARENT, "교정 상담", "영구치 교정은 대개 11~13세, 치과에서 시기 확인"),
            x(MIND, ALL_YEAR, TOGETHER, "감정 기복 이해하기", "호르몬 변화, 혼내기보다 들어 주기"),
        ),
        "e6" to listOf(
            SCHOOL_SURVEY,
            x(BODY, ALL_YEAR, PARENT, "만 11~12세 접종", "Tdap 6차 · 일본뇌염 추가(12세) · HPV 2회(12세 여아, 2026년부터 남아도)"),
            x(BODY, SECOND, PARENT, "척추측만 확인", "앞으로 숙였을 때 등 높이가 다르면 정형외과"),
            FLU, DENTAL, MYOPIA, MOVE,
            x(MIND, ALL_YEAR, TOGETHER, "SNS·단체방 갈등", "힘든 대화는 캡처해 어른에게"),
        ),
        "m1" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, CHILD, "학생정서·행동특성검사", "중1, 학생이 직접"),
            x(MIND, FIRST, CHILD, "스마트폰 이용습관 진단", "중1, 학생이 직접"),
            x(BODY, FIRST, PARENT, "입학 접종 확인", "Tdap · HPV · 일본뇌염, 빠진 것 보충"),
            x(BODY, ALL_YEAR, PARENT, "HPV 2차", "1차 6개월 뒤"),
            FLU, DENTAL, MOVE,
            x(BODY, ALL_YEAR, CHILD, "여드름·위생", "하루 두 번 세안, 짜지 않기"),
            DEPRESSION,
        ),
        "m2" to listOf(
            SCHOOL_SURVEY, DENTAL, MOVE,
            x(BODY, ALL_YEAR, CHILD, "에너지음료 피하기", "청소년 카페인은 체중 1kg당 2.5mg 안"),
            x(BODY, ALL_YEAR, CHILD, "바른 자세·목 건강", "책상 높이 맞추기, 50분마다 스트레칭"),
            x(MIND, ALL_YEAR, TOGETHER, "시험 스트레스 다루기", "시험 뒤 결과보다 과정 이야기"),
            DEPRESSION,
        ),
        "m3" to listOf(
            SCHOOL_SURVEY, DENTAL, MOVE,
            x(MIND, ALL_YEAR, TOGETHER, "진로 불안 나누기", "정하지 못해도 괜찮다고 말해 주기"),
            x(MIND, ALL_YEAR, CHILD, "스마트폰 사용 점검", "하루 사용 시간 스스로 확인"),
            DEPRESSION,
        ),
        "h1" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, CHILD, "학생정서·행동특성검사", "고1, 학생이 직접"),
            x(MIND, FIRST, CHILD, "스마트폰 이용습관 진단", "고1, 학생이 직접"),
            DENTAL, MOVE,
            x(MIND, ALL_YEAR, PARENT, "위기 신호 알기", "죽고 싶다는 말은 그냥 넘기지 않기 — 109(자살예방)·1388"),
        ),
        "h2" to listOf(
            SCHOOL_SURVEY, DENTAL, MOVE,
            x(BODY, ALL_YEAR, CHILD, "에너지음료 피하기", "청소년 카페인은 체중 1kg당 2.5mg 안"),
            x(MIND, ALL_YEAR, TOGETHER, "번아웃 살피기", "주 1회는 공부 없는 반나절"),
            DEPRESSION,
        ),
        "h3" to listOf(
            SCHOOL_SURVEY,
            x(BODY, SECOND, PARENT, "독감 접종", "수능 전 10월 초(만 13세 넘어 유료)"),
            x(BODY, SECOND, CHILD, "수능 생활 리듬", "한 달 전부터 6시 반 기상, 수능 시간표대로"),
            x(MIND, SECOND, CHILD, "시험 불안 호흡법", "4초 들이쉬고 6초 내쉬기, 시험 전 연습"),
            x(MIND, SECOND, TOGETHER, "수능 뒤 생활 되찾기", "잠·운동·만남부터"),
        ),
        "u" to listOf(
            x(BODY, FIRST, CHILD, "A형간염 항체 확인", "없으면 접종(20~30대 권장)"),
            x(MIND, ALL_YEAR, CHILD, "학교 상담센터", "무료 상담 한 번 받아 보기"),
        ),
        "g" to listOf(
            x(MIND, ALL_YEAR, CHILD, "연구실 번아웃 점검", "주 1일은 쉬기, 학교 상담센터"),
        ),
    )
}
