package com.nextstep.app.domain.hub

import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentUiLevel

/**
 * 기록 탭을 지금 보는 사람: 자리([audience])와, 학생이면 화면 단계([level]).
 * 학부모·멘토는 [level] 이 null 이라 단계로 줄지 않습니다.
 */
data class HubViewer(val audience: HubAudience, val level: StudentUiLevel? = null) {
    companion object {
        val PARENT = HubViewer(HubAudience.PARENT)
        val MENTOR = HubViewer(HubAudience.MENTOR)

        fun student(level: StudentUiLevel) = HubViewer(HubAudience.STUDENT, level)

        fun of(caps: Capabilities, studentLevel: StudentUiLevel?): HubViewer =
            HubViewer(caps.hubAudience, if (caps.hubAudience == HubAudience.STUDENT) studentLevel ?: StudentUiLevel.TREE else null)
    }
}
