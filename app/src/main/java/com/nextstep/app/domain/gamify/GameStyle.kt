package com.nextstep.app.domain.gamify

/**
 * 나이에 맞춘 게임 모양. 어떤 모양을 쓸지는 학생 화면 단계([com.nextstep.app.domain.growth.StudentUiLevel.game])가 정합니다.
 * 경험치 기록은 모양과 상관없이 같은 규칙으로 쌓이므로, 단계가 바뀌어도 레벨이 줄지 않습니다.
 *
 * - [STICKERS] 스티커판(학령 전 ~ 초2): 한 일마다 스티커 한 장, 10장이면 한 판. 멀리 있는 목표보다 바로 보이는 보상이 맞는 나이라
 *   레벨·경험치·연속 기록을 보여 주지 않고, 도전도 "이번 주 스티커판 채우기" 하나뿐입니다.
 * - [LEVELS] 레벨·배지(초3 ~ 초6): 레벨 이름 · 배지 모으기 · 연속 기록 · 이번 주 도전 셋. 모으고 완성하는 재미가 큰 나이.
 * - [GROWTH] 성장 기록(중1 이후): 귀여운 이름 대신 Lv 과 누적 기록, 연속 기록은 하루 쉬어도 이어지고(번아웃 방지),
 *   도전은 스스로 세운 이번 주 계획을 지키는 것. 스스로 끌 수도 있습니다(자율성).
 */
enum class GameStyle(
    /** 오늘 카드 제목. */
    val title: String,
    /** 배지를 부르는 말. */
    val badgeWord: String,
    /** 레벨 이름(첫걸음·꾸준이…)을 붙일지. false 면 "Lv 12". */
    val showsLevelTitle: Boolean,
    /** 레벨·경험치를 보여 줄지. false 면 스티커판 수로만. */
    val showsLevel: Boolean,
    val showsStreak: Boolean,
    /** 연속 기록에서 쉬어도 이어지는 날 수(이틀 쉬면 끊김). */
    val restDays: Int,
    /** "무언가 한 날" 도전의 목표 날수. */
    val activeDaysTarget: Int,
    /** 학생이 스스로 끌 수 있는지. 아니면 학부모만. */
    val studentCanTurnOff: Boolean,
) {
    STICKERS("나의 스티커판", "특별 스티커", showsLevelTitle = false, showsLevel = false, showsStreak = false, restDays = 0, activeDaysTarget = 0, studentCanTurnOff = false),
    LEVELS("나의 레벨", "배지", showsLevelTitle = true, showsLevel = true, showsStreak = true, restDays = 0, activeDaysTarget = 4, studentCanTurnOff = false),
    GROWTH("나의 성장 기록", "기록", showsLevelTitle = false, showsLevel = true, showsStreak = true, restDays = 1, activeDaysTarget = 5, studentCanTurnOff = true);

    companion object {
        /** 스티커판 한 판의 칸 수. */
        const val BOARD_SIZE = 10
    }
}
