package com.nextstep.app.data.repository

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.mentor.MentorScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * 멘토가 보는 가족 기록: 멘토(학부모 겸 멘토는 제외)는 담당 과목만 봅니다. 과목·단원·성적·공부 기록은 담당 과목 것만,
 * 할 일·로드맵은 담당 과목 것과 과목이 없는 것을 남깁니다. 담당 과목을 고르지 않았으면 전 과목([MentorScope.of]).
 * 가족 달력([familyEvents])은 멘토에게 비어 있습니다. 학생·학부모에게는 [family] 그대로입니다. 담당 과목을 고르는 화면(멘토 오늘·가족 설정)은 원본 [family] 를 씁니다.
 */
class MentorScopedStreams(private val family: FamilyDataStreams) : FamilyDataStreams by family {
    private val scope: Flow<MentorScope?> = combine(family.profile, family.myMember, family.subjects) { profile, me, subjects ->
        if (profile.role == Role.MENTOR) MentorScope.of(me, subjects) else null
    }

    override val subjects = combine(scope, family.subjects) { s, all -> s?.subjects ?: all }
    override val topics = own(family.topics) { it.subjectId }
    override val grades = own(family.grades) { it.subjectId }
    override val sessions = own(family.sessions) { it.subjectId }
    override val tasks = ownOrGeneral(family.tasks) { it.subjectId }
    override val roadmap = ownOrGeneral(family.roadmap) { it.subjectId }

    /** 가족 달력은 가족의 일이라 멘토(학부모 겸 멘토는 제외)에게는 한 건도 넘기지 않습니다. */
    override val familyEvents = combine(family.profile, family.familyEvents) { profile, events -> if (profile.role == Role.MENTOR) emptyList() else events }

    /** 응원도 가족 사이의 일이라 멘토에게 넘기지 않습니다. */
    override val cheers = combine(family.profile, family.cheers) { profile, list -> if (profile.role == Role.MENTOR) emptyList() else list }

    private fun <T> own(flow: Flow<List<T>>, subjectId: (T) -> String?): Flow<List<T>> =
        combine(scope, flow) { s, items -> s?.own(items, subjectId) ?: items }

    private fun <T> ownOrGeneral(flow: Flow<List<T>>, subjectId: (T) -> String?): Flow<List<T>> =
        combine(scope, flow) { s, items -> s?.ownOrGeneral(items, subjectId) ?: items }
}
