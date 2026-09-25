package com.nextstep.app.ui.kidfamily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.domain.access.ParentGate
import com.nextstep.app.domain.cheer.KidMessage
import com.nextstep.app.ui.common.asUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 아이용 가족 탭: 나를 뺀 가족, 최근 한마디 3개, 한 번 누르는 말 보내기, 설정 앞의 어른 확인.
 */
class KidFamilyViewModel(
    streams: FamilyDataStreams,
    private val notes: NoteRepository,
    private val random: Random = Random.Default,
) : ViewModel() {
    private val local = MutableStateFlow(Local())

    val state: StateFlow<KidFamilyUiState> = combine(streams.members, streams.myMember, streams.notes, local) { members, me, notes, l ->
        KidFamilyUiState(
            family = members.filter { !it.deleted && it.id != me?.id },
            notes = notes.sortedByDescending { it.createdAt }.take(RECENT_NOTES),
            gate = l.gate, gateError = l.gateError, unlocked = l.unlocked, sentMessage = l.sent,
        )
    }.asUiState(viewModelScope, KidFamilyUiState())

    fun send(message: KidMessage) = viewModelScope.launch {
        notes.add(message.text)
        local.value = local.value.copy(sent = message.text)
    }

    fun openSettings() { local.value = local.value.copy(gate = ParentGate.next(random), gateError = false) }

    fun answer(text: String) {
        val gate = local.value.gate ?: return
        local.value = if (gate.accepts(text)) local.value.copy(gate = null, gateError = false, unlocked = true) else local.value.copy(gate = ParentGate.next(random), gateError = true)
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: KidFamilyEvent) {
        when (event) {
            is KidFamilyEvent.Send -> send(event.message)
            KidFamilyEvent.OpenSettings -> openSettings()
            is KidFamilyEvent.AnswerGate -> answer(event.answer)
            KidFamilyEvent.CloseGate -> local.value = local.value.copy(gate = null, gateError = false)
            KidFamilyEvent.ConsumeUnlock -> local.value = local.value.copy(unlocked = false)
            KidFamilyEvent.ClearSent -> local.value = local.value.copy(sent = null)
        }
    }

    private data class Local(val gate: ParentGate? = null, val gateError: Boolean = false, val unlocked: Boolean = false, val sent: String? = null)

    private companion object {
        const val RECENT_NOTES = 3
    }
}
