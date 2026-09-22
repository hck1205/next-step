package com.nextstep.app.ui.mentor

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.domain.insight.Insight
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.stats.SubjectScore

/**
 * 멘토 대시보드 상태. 멘토가 담당 과목을 지정했으면 모든 지표를 그 과목으로 좁혀 보여줍니다.
 */
data class MentorDashboardUiState(
    val me: MemberEntity? = null,
    val studentName: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val allSubjects: List<SubjectEntity> = emptyList(),
    /** 담당 과목 (미지정이면 전 과목). */
    val subjects: List<SubjectEntity> = emptyList(),
    val otherMentors: List<MemberEntity> = emptyList(),
    val weekMinutes: Int = 0,
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val recentGrades: List<GradeEntity> = emptyList(),
    val myTasks: List<TaskEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val roadmapTotal: Int = 0,
    val roadmapInProgress: Int = 0,
    val roadmapDone: Int = 0,
    val roadmapOverdue: Int = 0,
    val stage: com.nextstep.app.domain.growth.GrowthStage? = null,
    val mentorTip: String? = null,
) {
    val needsSubjectSetup: Boolean get() = me != null && me.subjectIdList.isEmpty() && allSubjects.isNotEmpty()
}
