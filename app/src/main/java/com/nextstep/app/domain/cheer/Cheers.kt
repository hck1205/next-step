package com.nextstep.app.domain.cheer

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.task.doneOn
import com.nextstep.app.domain.text.Josa
import java.time.LocalDate

/**
 * 응원 계산 한 곳. 학부모는 최근 [CHEER_DAYS] 일 안에 끝낸 일에 응원을 붙이고, 아이는 아직 "고마워요"를 누르지 않은 응원을 받습니다.
 * 응원은 끝낸 일에만(끝내지 못한 일·점수에는 붙이지 않음), 한 일에 한 사람이 하나.
 */
object Cheers {

    /** 응원할 수 있는 해낸 일(최근 끝낸 것부터 [MAX_TARGETS] 개)과 [myId] 가 붙인 응원. */
    fun targets(tasks: List<TaskEntity>, cheers: List<CheerEntity>, myId: String, today: LocalDate): List<CheerTarget> {
        val from = today.minusDays(CHEER_DAYS - 1)
        val mine = cheers.filter { !it.deleted && it.fromId == myId }.associateBy { it.taskId }
        return tasks.filter { t -> !t.deleted && t.doneOn()?.let { it >= from && it <= today } == true }
            .sortedByDescending { it.doneAt }
            .take(MAX_TARGETS)
            .map { t -> CheerTarget(t, mine[t.id]?.let { CheerKind.from(it.kind) }) }
    }

    /** 아이가 아직 보지 않은 응원(새것부터). */
    fun unseen(cheers: List<CheerEntity>): List<CheerEntity> = cheers.filter { !it.deleted && it.seenAt == null }.sortedByDescending { it.createdAt }

    /** 받은 응원 한 줄: "엄마가 '분수 20문제'에 👏 짝짝짝". */
    fun line(cheer: CheerEntity): String {
        val kind = CheerKind.from(cheer.kind)
        return "${Josa.withSubject(cheer.fromName.ifBlank { "가족" })} '${cheer.taskTitle}'에 ${kind.emoji} ${kind.word}"
    }

    /** 같은 응원을 다시 누르면 거두고(null), 다른 것을 누르면 바꿉니다. */
    fun toggle(current: CheerKind?, pressed: CheerKind): CheerKind? = if (current == pressed) null else pressed

    /** 해낸 일을 응원할 수 있는 날 수(오늘 포함). */
    const val CHEER_DAYS = 2L
    const val MAX_TARGETS = 5
}
