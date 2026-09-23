package com.nextstep.app.domain.mentor

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.Role

/**
 * 멘토가 담당하는 과목 범위. 담당 과목을 지정하지 않았으면 전 과목입니다.
 * 성적·세션·단원은 [own] 으로, 과목이 없는 할 일도 포함하려면 [ownOrGeneral] 로 좁힙니다.
 */
data class MentorScope(val subjects: List<SubjectEntity>) {
    val subjectIds: Set<String> = subjects.map { it.id }.toSet()

    fun <T> own(items: List<T>, subjectId: (T) -> String?): List<T> = items.filter { subjectId(it) in subjectIds }
    fun <T> ownOrGeneral(items: List<T>, subjectId: (T) -> String?): List<T> = items.filter { subjectId(it).let { id -> id == null || id in subjectIds } }

    companion object {
        fun of(me: MemberEntity?, all: List<SubjectEntity>): MentorScope =
            if (me == null || me.subjectIdList.isEmpty()) MentorScope(all) else MentorScope(all.filter { it.id in me.subjectIdList })

        /** 멘토 화면의 메모: 학생·학부모 메모 전부 + 다른 멘토 메모는 제외하고 내 것만. */
        fun visibleNotes(notes: List<NoteEntity>, me: MemberEntity?): List<NoteEntity> =
            notes.filter { it.authorRole != Role.MENTOR.name || me == null || it.authorName == me.name }
    }
}
