package com.nextstep.app.domain.year

/**
 * 해마다 학생이 할 일을 분류(탭)별로 잘게 나눈 목록. 키는 YearProfiles 와 같습니다(a0~a6, e1~e6, m1~m3, h1~h3, u, g).
 * 초중고 과목·단원은 2022 개정 교육과정, 유아는 표준보육·누리과정과 영유아 검진·접종 일정을 따릅니다.
 * 양은 상한에 가깝게 적었습니다: 더 시키라는 목록이 아니라 "이만큼이면 충분한" 목록이고, 학교부터는 줄마다 도달 기준([YearTask.bar])이 있습니다.
 * 그 위로 여유가 있을 때 미리 하면 앞서는 일은 [AheadPlans] 에 따로 둡니다(진행률에 넣지 않음).
 * 학령 전은 누가 하는지([YearDoer])를 꼭 적습니다: 만 0세는 전부 부모가, 만 1~2세는 부모가·같이, 만 3세부터 "스스로"가 생깁니다.
 * 학교부터는 2022 개정 교육과정의 학기별 단원, 성취평가(중)·내신 5등급(고)·창체 3영역, 2028 수능 개편을 따르고,
 * 수면은 권장 시간(초 9~12시간, 중·고 8~10시간) 아래로 적지 않습니다.
 * 영유아 검진 회차·월령은 국민건강보험 영유아 건강검진(1~8차, 구강 1~3차) 일정을 따릅니다.
 */
object YearPlans {
    /**
     * 그 해 할 일 전부: 학업·생활([plans]) + 건강([HealthPlans]) + 부모의 지원·서류·상담([ParentPlans]) + 멘토 코칭([MentorPlans])
     * + 여유가 있을 때의 앞서 가기([AheadPlans]).
     */
    fun forYear(key: String): List<YearTask> = base(key) + AheadPlans.forYear(key)

    /** 기본: 이만큼이면 충분한 것. 진행률은 이것만 셉니다. */
    fun base(key: String): List<YearTask> =
        plans[key].orEmpty() + HealthPlans.forYear(key) + ParentPlans.forYear(key) + MentorPlans.forYear(key)

    /** 앞서 가기: 기본을 채운 뒤 여유가 있을 때 미리 하면 다음 해가 쉬워지는 것. */
    fun ahead(key: String): List<YearTask> = AheadPlans.forYear(key)

    /** 그 해에 할 일이 있는 분류(탭 순서). */
    fun areasOf(key: String): List<YearArea> = forYear(key).map { it.area }.distinct().sortedBy { it.ordinal }

    /** 학업·생활: 학령 전 · 초등 · 중고(대학 이후 포함) 표를 한 번 합쳐 둡니다. */
    private val plans: Map<String, List<YearTask>> = PreschoolPlans.byYear + ElementaryPlans.byYear + SecondaryPlans.byYear
}
