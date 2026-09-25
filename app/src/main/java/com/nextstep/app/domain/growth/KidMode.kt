package com.nextstep.app.domain.growth

/**
 * 아이가 자기 폰으로 앱을 직접 쓸 때의 도움 장치. 어릴수록 많이 켜지고 학년이 오르면 하나씩 꺼집니다.
 *
 * - [stickerMe]: "나" 탭이 기록 허브 대신 스티커판(공부한 날 별, 한 활동 그림)
 * - [kidFamily]: "가족" 탭이 설정 대신 가족 얼굴과 한 번 누르는 말 보내기. 설정은 어른 확인([ParentGate]) 뒤에만
 * - [pictureRecord]: + 가 입력 양식 대신 그림 타일 한 번 누르기("책 읽었어요")
 * - [readsAloud]: 할 일 옆 스피커를 누르면 소리로 읽어 줌(아직 글이 서툰 나이)
 * - [visualTimer]: 타이머가 숫자 대신 줄어드는 원(남은 시간이 눈에 보임)
 */
data class KidMode(
    val stickerMe: Boolean,
    val kidFamily: Boolean,
    val pictureRecord: Boolean,
    val readsAloud: Boolean,
    val visualTimer: Boolean,
) {
    /** 켜진 도움 장치 수. 어린 단계일수록 많습니다. */
    val helpCount: Int get() = listOf(stickerMe, kidFamily, pictureRecord, readsAloud, visualTimer).count { it }

    companion object {
        /** 학령 전·초1~2: 전부 켬. */
        val EARLY = KidMode(stickerMe = true, kidFamily = true, pictureRecord = true, readsAloud = true, visualTimer = true)
        /** 초3~4: 기록 허브를 직접 쓰고 글도 읽지만, 가족·기록하기·타이머는 아직 쉽게. */
        val MIDDLE = KidMode(stickerMe = false, kidFamily = true, pictureRecord = true, readsAloud = false, visualTimer = true)
        /** 초5부터: 일반 화면. */
        val NONE = KidMode(stickerMe = false, kidFamily = false, pictureRecord = false, readsAloud = false, visualTimer = false)
    }
}
