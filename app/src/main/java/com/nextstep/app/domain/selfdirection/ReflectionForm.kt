package com.nextstep.app.domain.selfdirection

/** 주간 돌아보기의 모양. 어릴수록 고르기만, 클수록 스스로 쓰는 칸이 늘어납니다. */
enum class ReflectionForm {
    /** 기분 얼굴 셋 중 하나. */
    FACES,
    /** 기분 + 제일 좋았던 것 한 줄. */
    FACE_AND_BEST,
    /** 기분 + 잘된 것 · 어려웠던 것 · 다음 주에 바꿀 것. */
    THREE_LINES,
}
