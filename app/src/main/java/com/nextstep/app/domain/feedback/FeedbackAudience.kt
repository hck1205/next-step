package com.nextstep.app.domain.feedback

import com.nextstep.app.data.model.Role

/** 피드백을 듣는 사람. 같은 사실을 학생은 격려와 다음 한 걸음으로, 학부모는 챙길 것과 건넬 한마디로, 멘토는 과제·로드맵 조정으로 듣습니다. */
enum class FeedbackAudience {
    STUDENT,
    PARENT,
    MENTOR;

    companion object {
        /** 프로필 역할로. 학부모 겸 멘토는 학부모 자리에서 듣습니다. */
        fun of(role: Role): FeedbackAudience = when (role) {
            Role.STUDENT -> STUDENT
            Role.PARENT -> PARENT
            Role.MENTOR -> MENTOR
        }
    }
}
