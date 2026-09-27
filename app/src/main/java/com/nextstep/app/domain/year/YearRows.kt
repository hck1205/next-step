package com.nextstep.app.domain.year

/**
 * 해마다 할 일 표([PreschoolPlans]·[ElementaryPlans]·[SecondaryPlans])가 같이 쓰는 줄 만들기.
 * 이름이 짧은 것은 표를 한 줄에 여러 개 적기 위해서입니다: a·pa·ta·ca = 1년 내내(누가: 스스로·부모·같이·스스로),
 * f·s·y = 1학기·2학기·1년 내내 + 도달 기준(bar).
 */
internal object YearRows {
    val P = YearDoer.PARENT
    val T = YearDoer.TOGETHER

    fun a(area: YearArea, title: String, how: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.ALL_YEAR, title, how, who)
    fun pa(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how, YearDoer.PARENT)
    fun ta(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how, YearDoer.TOGETHER)
    fun ca(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how, YearDoer.CHILD)
    /** 학교부터: 1학기([f]) · 2학기([s]) · 1년 내내([y]), 도달 기준([bar]) 과 함께. */
    fun f(area: YearArea, title: String, how: String, bar: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.FIRST, title, how, who, bar)
    fun s(area: YearArea, title: String, how: String, bar: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.SECOND, title, how, who, bar)
    fun y(area: YearArea, title: String, how: String, bar: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.ALL_YEAR, title, how, who, bar)
}
