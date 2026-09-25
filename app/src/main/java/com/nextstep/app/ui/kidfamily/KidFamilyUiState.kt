package com.nextstep.app.ui.kidfamily

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.domain.access.ParentGate

/**
 * 아이용 "가족" 탭: 가족 얼굴, 한 번 누르는 말, 최근 한마디, 어른 확인 상태.
 * [gate] 가 있으면 어른 확인 창이 열려 있고, [unlocked] 가 true 가 되면 화면이 설정으로 보내고 되돌립니다.
 */
data class KidFamilyUiState(
    val family: List<MemberEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val gate: ParentGate? = null,
    val gateError: Boolean = false,
    val unlocked: Boolean = false,
    val sentMessage: String? = null,
)
