package com.nextstep.app.domain.stats

/** 하루를 넷으로 나눈 때. 공부를 시작한 시각으로 나눕니다. 밤은 22시부터 새벽 4시까지입니다. */
enum class DayPart(val label: String, val hours: IntRange) {
    MORNING("오전", 5..11),
    AFTERNOON("오후", 12..17),
    EVENING("저녁", 18..21),
    NIGHT("밤", 22..23);

    companion object {
        fun of(hour: Int): DayPart = when (hour) {
            in MORNING.hours -> MORNING
            in AFTERNOON.hours -> AFTERNOON
            in EVENING.hours -> EVENING
            else -> NIGHT
        }
    }
}
