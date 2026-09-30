package com.nextstep.app.domain.year

/** 앞서 가기 표([PreschoolAhead]·[SchoolAhead])의 줄 만들기: 모두 [YearTier.AHEAD], 도착점(bar)과 까닭(why)을 함께 적습니다. */
internal object AheadRows {
    fun y(area: YearArea, title: String, how: String, bar: String, why: String, who: YearDoer = YearDoer.CHILD) =
        YearTask(area, YearTerm.ALL_YEAR, title, how, who, bar, YearTier.AHEAD, why)
    fun f(area: YearArea, title: String, how: String, bar: String, why: String, who: YearDoer = YearDoer.CHILD) =
        YearTask(area, YearTerm.FIRST, title, how, who, bar, YearTier.AHEAD, why)
    fun s(area: YearArea, title: String, how: String, bar: String, why: String, who: YearDoer = YearDoer.CHILD) =
        YearTask(area, YearTerm.SECOND, title, how, who, bar, YearTier.AHEAD, why)
}
