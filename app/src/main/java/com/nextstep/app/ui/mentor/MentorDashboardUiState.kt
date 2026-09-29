package com.nextstep.app.ui.mentor

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.prefs.LinkedChild
import com.nextstep.app.domain.feedback.FeedbackLine
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.insight.Insight
import com.nextstep.app.domain.stats.FamilyTrends
import com.nextstep.app.domain.stats.RoadmapSummary
import com.nextstep.app.domain.stats.SubjectMinutes
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.domain.today.MentorTodayCard
import com.nextstep.app.domain.today.TodayGroup
import com.nextstep.app.domain.today.TodayLayout

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
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val recentGrades: List<GradeEntity> = emptyList(),
    val myTasks: List<TaskEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val roadmap: RoadmapSummary = RoadmapSummary(),
    val stage: GrowthStage? = null,
    val mentorTip: String? = null,
    /** 이 기기에 연결된 학생들과 지금 보고 있는 학생. */
    val students: List<LinkedChild> = emptyList(),
    val activeFamilyId: String? = null,
    /** 담당 과목으로 좁힌 차트 값(과제 제출 · 8주 흐름 · 과목별 점수). */
    val trends: FamilyTrends = FamilyTrends(),
    /** 이번 주 피드백(멘토의 말, 담당 과목 기록으로). */
    val feedback: List<FeedbackLine> = emptyList(),
) {
    val needsSubjectSetup: Boolean get() = me != null && me.subjectIdList.isEmpty() && allSubjects.isNotEmpty()

    /** 내용이 있는 카드(MentorTodayCard 순서)와 관심사로 묶은 것(오늘 화면의 "먼저 볼 것" · "더 보기" 줄). */
    val visibleCards: List<MentorTodayCard> by lazy { MentorTodayCard.entries.filter { !it.shortcut && hasContent(it) } }
    val todayGroups: List<TodayGroup<MentorTodayCard>> by lazy { TodayLayout.group(visibleCards) { it.concern } }

    fun hasContent(card: MentorTodayCard): Boolean = when (card) {
        MentorTodayCard.STAGE, MentorTodayCard.SUBJECTS, MentorTodayCard.TASKS, MentorTodayCard.ROADMAP, MentorTodayCard.CONTENT -> true
        MentorTodayCard.SUBMISSIONS -> trends.submissions.total > 0
        MentorTodayCard.STUDY_WEEKS -> trends.hasStudy
        MentorTodayCard.SCORES -> trends.scores.isNotEmpty()
        MentorTodayCard.INSIGHTS -> insights.isNotEmpty()
        MentorTodayCard.FEEDBACK -> feedback.isNotEmpty()
        MentorTodayCard.WEEK_CHART -> weeklyBySubject.isNotEmpty()
        MentorTodayCard.PROGRESS -> progress.isNotEmpty()
        MentorTodayCard.GRADES -> recentGrades.isNotEmpty()
    }
}
