package com.nextstep.app.ui.kidfamily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.access.ParentGate
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlin.random.Random

/**
 * 아이용 가족 탭: 나를 뺀 가족 얼굴과, 설정 앞의 어른 확인.
 */
class KidFamilyViewModel(
    streams: FamilyDataStreams,
    private val random: Random = Random.Default,
) : ViewModel() {
    private val local = MutableStateFlow(Local())

    val state: StateFlow<KidFamilyUiState> = combine(streams.members, streams.myMember, local) { members, me, l ->
        KidFamilyUiState(
            family = members.filter { !it.deleted && it.id != me?.id },
            gate = l.gate, gateError = l.gateError, unlocked = l.unlocked,
        )
    }.asUiState(viewModelScope, KidFamilyUiState())

    fun openSettings() { local.value = local.value.copy(gate = ParentGate.next(random), gateError = false) }

    fun answer(text: String) {
        val gate = local.value.gate ?: return
        local.value = if (gate.accepts(text)) local.value.copy(gate = null, gateError = false, unlocked = true) else local.value.copy(gate = ParentGate.next(random), gateError = true)
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: KidFamilyEvent) {
        when (event) {
            KidFamilyEvent.OpenSettings -> openSettings()
            is KidFamilyEvent.AnswerGate -> answer(event.answer)
            KidFamilyEvent.CloseGate -> local.value = local.value.copy(gate = null, gateError = false)
            KidFamilyEvent.ConsumeUnlock -> local.value = local.value.copy(unlocked = false)
        }
    }

    private data class Local(val gate: ParentGate? = null, val gateError: Boolean = false, val unlocked: Boolean = false)
}
