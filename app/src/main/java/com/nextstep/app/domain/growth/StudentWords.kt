package com.nextstep.app.domain.growth

/**
 * 학생 화면의 말투. 어린 단계는 한자어 대신 쉬운 말("복습" → "다시 보기")을 씁니다.
 * 화면은 이 값만 읽고 학년으로 문구를 고르지 않습니다.
 */
data class StudentWords(
    val tasksTitle: String,
    val allDone: String,
    val timerIdle: String,
    val timerStart: String,
    val reviewTitle: String,
    val reviewDone: String,
    val previewTitle: String,
    val previewDone: String,
    val noteTitle: String,
    val weekTitle: String,
) {
    companion object {
        /** 학령 전: 글을 다 읽지 못해도 알 수 있게 아주 짧게. 그림과 별이 주인공입니다. */
        val EARLY = StudentWords(
            tasksTitle = "오늘 해 볼 것",
            allDone = "다 했어! 최고야",
            timerIdle = "놀이처럼 해 봐요",
            timerStart = "시작!",
            reviewTitle = "다시 보기",
            reviewDone = "다 봤어요",
            previewTitle = "미리 보기",
            previewDone = "다 봤어요",
            noteTitle = "가족 한마디",
            weekTitle = "이번 주 스티커",
        )

        /** 초1~4: 짧고 쉬운 말, 칭찬하는 말투. */
        val EASY = StudentWords(
            tasksTitle = "오늘 할 일",
            allDone = "다 했어요! 최고예요",
            timerIdle = "공부할 때 눌러요",
            timerStart = "시작!",
            reviewTitle = "다시 보기",
            reviewDone = "다 봤어요",
            previewTitle = "미리 보기",
            previewDone = "다 봤어요",
            noteTitle = "가족 한마디",
            weekTitle = "이번 주 별",
        )

        /** 초5 이상: 지금까지의 기본 문구. */
        val STANDARD = StudentWords(
            tasksTitle = "오늘 할 것",
            allDone = "할 일을 모두 끝냈어요",
            timerIdle = "공부를 시작할 때 눌러서 시간을 기록하세요",
            timerStart = "시작",
            reviewTitle = "복습할 단원",
            reviewDone = "복습 완료",
            previewTitle = "예습할 단원",
            previewDone = "예습 완료",
            noteTitle = "부모·멘토 한마디",
            weekTitle = "이번 주",
        )
    }
}
