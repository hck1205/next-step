package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.YearArea.ADMIN
import com.nextstep.app.domain.year.YearArea.CAREER
import com.nextstep.app.domain.year.YearArea.GUIDE
import com.nextstep.app.domain.year.YearDoer.PARENT
import com.nextstep.app.domain.year.YearDoer.TOGETHER
import com.nextstep.app.domain.year.YearTerm.ALL_YEAR
import com.nextstep.app.domain.year.YearTerm.FIRST
import com.nextstep.app.domain.year.YearTerm.SECOND

/**
 * 해마다 부모가 챙길 지원·서류·제도와 상담. 금액·연령은 2026년 기준이며 바뀌면 여기만 고칩니다.
 * - 첫만남이용권 첫째 200만·둘째부터 300만 원, 부모급여 0세 월 100만·1세 월 50만 원, 아동수당 만 9세 미만 월 10만 5천 원(2026.4~).
 *   부모급여·아동수당은 출생일 포함 60일 안에 신청하면 출생월부터 받습니다.
 * - 늘봄학교: 초1·2 희망자 매일 2시간 무료, 초3 이상 연 50만 원 상당 이용권(2026~).
 * - 맞춤형 학업성취도 자율평가: 초3~고2, 3~4월. 교육비 지원: 3월 집중 신청.
 * - 고교학점제 최소 성취수준 보장: 학업성취율 40%·출석률 3분의 2. 고교 내신 성취도(A~E)와 5등급.
 * - 수능 원서 8월 말~9월 초, 수시 원서 9월, 정시 원서 12월 말~1월 초, 국가장학금 신입생 1차 신청 11~12월.
 */
internal object ParentPlans {
    fun forYear(key: String): List<YearTask> = plans[key].orEmpty()

    private val MEETING_1 = x(GUIDE, FIRST, PARENT, "1학기 학부모 상담", "3~4월 상담 주간, 아이 성향·걱정 한 가지 전하기")
    private val MEETING_2 = x(GUIDE, SECOND, PARENT, "2학기 학부모 상담", "9~10월, 1학기 결과를 보고")
    private val EDU_AID = x(ADMIN, FIRST, PARENT, "교육비 지원 확인", "소득 기준 해당하면 3월 집중 신청(교육급여·방과후 자유수강권)")
    private val ACHIEVEMENT = x(GUIDE, FIRST, TOGETHER, "학업성취도 결과 보기", "3~4월 자율평가 결과표로 강점·약점 하나씩")
    private val NEIS = x(ADMIN, SECOND, PARENT, "학생부 확인", "학기말 나이스 학부모서비스에서")

    private val plans: Map<String, List<YearTask>> = mapOf(
        "a0" to listOf(
            x(ADMIN, ALL_YEAR, PARENT, "출생신고", "출생 후 1개월 안"),
            x(ADMIN, ALL_YEAR, PARENT, "부모급여·아동수당 신청", "출생 60일 안에(0세 월 100만 원 + 아동수당 10만 5천 원)"),
            x(ADMIN, ALL_YEAR, PARENT, "첫만남이용권", "첫째 200만·둘째부터 300만 원, 국민행복카드"),
            x(ADMIN, ALL_YEAR, PARENT, "어린이집 입소 대기", "아이사랑 포털에서 여러 곳 대기 신청"),
            x(ADMIN, ALL_YEAR, PARENT, "예방접종도우미 등록", "접종 기록·알림 받기"),
            x(ADMIN, ALL_YEAR, PARENT, "육아휴직 급여 확인", "고용24에서 부모 함께 쓰기 계획"),
            x(GUIDE, ALL_YEAR, PARENT, "양육자끼리 역할 나누기", "밤중·병원·서류 담당 정하기"),
        ),
        "a1" to listOf(
            x(ADMIN, ALL_YEAR, PARENT, "부모급여 1세", "월 50만 원, 어린이집 다니면 보육료를 빼고 차액만"),
            x(ADMIN, ALL_YEAR, PARENT, "어린이집 대기 순번 확인", "국공립은 대기가 길어요"),
            x(GUIDE, ALL_YEAR, PARENT, "발달 걱정은 검진 때", "궁금한 것을 적어 가기"),
        ),
        "a2" to listOf(
            x(ADMIN, ALL_YEAR, PARENT, "가정양육수당·보육료", "어린이집 안 다니면 가정양육수당, 다니면 보육료 바우처"),
            x(ADMIN, SECOND, PARENT, "유치원 알아보기", "만 3세반은 11월 '처음학교로' 접수"),
            x(GUIDE, SECOND, PARENT, "어린이집·유치원 비교", "놀이 시간·바깥놀이·특별활동 비용 확인"),
        ),
        "a3" to listOf(
            x(ADMIN, FIRST, PARENT, "누리과정 지원 신청", "만 3~5세 유아학비·보육료 지원"),
            MEETING_1,
            x(GUIDE, ALL_YEAR, PARENT, "사교육 기준 정하기", "예체능 위주 주 1~2개, 시험 보는 곳은 피하기"),
        ),
        "a4" to listOf(
            MEETING_1,
            x(GUIDE, ALL_YEAR, PARENT, "영어유치원 판단 기준", "놀이 비중·바깥놀이·한국어 시간 확인, 레벨테스트 요구는 불법(2026.10~)"),
        ),
        "a5" to listOf(
            MEETING_1,
            x(GUIDE, SECOND, PARENT, "초등학교 정보 모으기", "통학 거리·늘봄·학교 알림 앱"),
        ),
        "a6" to listOf(
            MEETING_1,
            x(ADMIN, SECOND, PARENT, "취학통지서·예비소집", "12월 통지서, 1월 예비소집"),
            x(ADMIN, SECOND, PARENT, "늘봄학교 신청", "초1 희망자 매일 2시간 무료, 1~2월 안내"),
            x(ADMIN, SECOND, PARENT, "입학 준비물", "학교 안내문 받은 뒤에 사기"),
        ),
        "e1" to listOf(
            MEETING_1, MEETING_2, EDU_AID,
            x(ADMIN, FIRST, PARENT, "학교 알림 앱 설치", "e알리미·하이클래스 등 학교가 쓰는 것"),
            x(ADMIN, FIRST, PARENT, "방과후·늘봄 프로그램", "3월 신청, 놀이·예체능 위주"),
            x(GUIDE, ALL_YEAR, TOGETHER, "학교폭력 신고 알아 두기", "117, 담임에게 먼저"),
        ),
        "e2" to listOf(
            MEETING_1, MEETING_2, EDU_AID,
            x(ADMIN, FIRST, PARENT, "늘봄학교(초2)", "희망자 매일 2시간 무료"),
            x(ADMIN, SECOND, PARENT, "초3 방과후 이용권 알아보기", "초3부터 연 50만 원 상당(2026~)"),
        ),
        "e3" to listOf(
            MEETING_1, MEETING_2, EDU_AID, ACHIEVEMENT,
            x(ADMIN, FIRST, PARENT, "방과후 이용권 사용", "연 50만 원 상당, 학기 초 신청"),
            x(GUIDE, ALL_YEAR, TOGETHER, "용돈 교육 시작", "주 단위 용돈과 기입장"),
        ),
        "e4" to listOf(
            MEETING_1, MEETING_2, EDU_AID, ACHIEVEMENT,
            x(GUIDE, SECOND, PARENT, "정서·행동검사 결과 확인", "관심군이면 학교 상담 연계"),
        ),
        "e5" to listOf(
            MEETING_1, MEETING_2, EDU_AID, ACHIEVEMENT,
            x(GUIDE, ALL_YEAR, PARENT, "사교육 점검", "주당 학원 시간·숙제·잠을 한 표로 보기"),
        ),
        "e6" to listOf(
            MEETING_1, MEETING_2, EDU_AID, ACHIEVEMENT,
            x(ADMIN, SECOND, PARENT, "중학교 배정 원서", "12월 원서, 1월 말 발표(서울 기준)"),
            x(ADMIN, SECOND, PARENT, "교복 지원 확인", "지역마다 무상 교복·지원금 달라요"),
        ),
        "m1" to listOf(
            MEETING_1, EDU_AID, ACHIEVEMENT, NEIS,
            x(GUIDE, FIRST, PARENT, "자유학기 활동 알기", "학교마다 1학기 또는 2학기, 무엇을 하는지 물어보기"),
            x(GUIDE, ALL_YEAR, TOGETHER, "스마트폰 규칙 다시 정하기", "밤에는 거실 충전"),
        ),
        "m2" to listOf(
            MEETING_1, EDU_AID, ACHIEVEMENT, NEIS,
            x(GUIDE, FIRST, PARENT, "성적표 읽는 법", "원점수·과목평균·성취도(A~E)를 함께 보기"),
            x(CAREER, SECOND, TOGETHER, "고입 전형 알아보기", "일반고·특목고·자사고·특성화고 차이"),
        ),
        "m3" to listOf(
            MEETING_1, EDU_AID, NEIS,
            x(CAREER, FIRST, TOGETHER, "고입 일정표 만들기", "영재학교 4~5월, 과학고 8월, 특목·자사고·일반고 12월"),
            x(CAREER, SECOND, TOGETHER, "고등학교 설명회", "관심 학교 2~3곳"),
            x(GUIDE, SECOND, PARENT, "고교학점제 이해", "선택과목, 최소 성취수준(학업성취율 40% · 출석 3분의 2)"),
        ),
        "h1" to listOf(
            MEETING_1, EDU_AID, ACHIEVEMENT, NEIS,
            x(GUIDE, FIRST, PARENT, "고교 성적표 읽는 법", "원점수·성취도(A~E)·석차 5등급(1등급 상위 10%)"),
            x(CAREER, SECOND, TOGETHER, "선택과목 함께 고르기", "희망 학과의 권장 과목 확인"),
            x(CAREER, ALL_YEAR, PARENT, "대학별 전형 계획 보기", "매년 4월 대학별 시행계획 발표"),
        ),
        "h2" to listOf(
            MEETING_1, EDU_AID, ACHIEVEMENT, NEIS,
            x(CAREER, FIRST, TOGETHER, "입시 설명회", "교육청·대학 무료 설명회부터"),
            x(CAREER, SECOND, TOGETHER, "수시·정시 방향 이야기", "내신·모의고사 추이로"),
            x(GUIDE, SECOND, PARENT, "고3 1년 계획", "비용·일정·가족 행사 미리 조정"),
        ),
        "h3" to listOf(
            MEETING_1, NEIS,
            x(ADMIN, SECOND, PARENT, "수능 원서 접수 확인", "8월 말~9월 초, 학교에서"),
            x(CAREER, SECOND, TOGETHER, "수시 6장 함께 정하기", "상향·적정·안정 나누기"),
            x(ADMIN, SECOND, PARENT, "수능 준비물", "신분증·수험표·아날로그 시계"),
            x(ADMIN, SECOND, PARENT, "국가장학금 1차 신청", "11~12월, 합격 전에도 신청"),
        ),
        "u" to listOf(
            x(ADMIN, ALL_YEAR, TOGETHER, "국가장학금", "매 학기 신청 기간 확인"),
        ),
    )

    private fun x(area: YearArea, term: YearTerm, who: YearDoer, title: String, how: String) = YearTask(area, term, title, how, who)
}
