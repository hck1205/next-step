package com.nextstep.app.ui.common

import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.access.Capabilities
import kotlinx.coroutines.flow.first

/**
 * 지금 쓰는 사람이 할 일·목표에 남길 작성자 역할([Capabilities.actingRoleName]: 학부모 겸 멘토는 MENTOR).
 * ViewModel 이 저장하기 직전에 읽으므로 화면은 역할을 이벤트에 싣지 않습니다. 역할이 아직 없으면 빈 값(저장소가 채움).
 */
suspend fun FamilyDataStreams.actingRoleName(): String {
    val role = profile.first().role ?: return ""
    return Capabilities.of(role, myMember.first()).actingRoleName
}
