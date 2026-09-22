package com.nextstep.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import java.util.UUID

/**
 * 모든 동기화 대상 엔티티가 공유하는 필드.
 * - updatedAt: 마지막 수정 시각(epoch millis). 충돌 시 최신 값이 이깁니다.
 * - deleted: 소프트 삭제 플래그. 삭제 정보도 상대 기기에 전파돼야 하므로 바로 지우지 않습니다.
 * - dirty: 아직 서버에 올리지 못한 로컬 변경.
 */
interface Syncable {
    val id: String
    val familyId: String
    val updatedAt: Long
    val deleted: Boolean
    val dirty: Boolean
}

fun newId(): String = UUID.randomUUID().toString()

@Entity(tableName = "subjects", indices = [Index("familyId")])
data class SubjectEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val name: String,
    /** ARGB 색상 값. 차트/캘린더에서 과목 식별에 사용. */
    val color: Long,
    val teacher: String = "",
    /** 주간 학습 목표(분). 0이면 목표 없음. */
    val weeklyGoalMinutes: Int = 0,
    val orderIndex: Int = 0,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable

@Entity(tableName = "topics", indices = [Index("familyId"), Index("subjectId")])
data class TopicEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String,
    val title: String,
    val orderIndex: Int,
    /** 학급(수업)에서 이미 다룬 단원인지. 학급 진도를 나타냅니다. */
    val classCovered: Boolean = false,
    /** 학생 본인의 학습 상태. */
    val status: TopicStatus = TopicStatus.NOT_STARTED,
    /** 자기 평가 이해도 0~100. */
    val confidence: Int = 0,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable

@Entity(tableName = "tasks", indices = [Index("familyId"), Index("dueDate")])
data class TaskEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val topicId: String? = null,
    val title: String,
    val type: TaskType = TaskType.OTHER,
    /** 마감일 (epoch day). */
    val dueDate: Long,
    val done: Boolean = false,
    val createdByRole: String,
    val note: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable

@Entity(tableName = "events", indices = [Index("familyId"), Index("startAt")])
data class EventEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val title: String,
    val type: EventType = EventType.OTHER,
    /** 시작 시각 (epoch millis). 반복 일정이면 첫 발생일 기준. */
    val startAt: Long,
    val endAt: Long,
    /** true 면 매주 같은 요일/시간에 반복 (시간표). */
    val repeatWeekly: Boolean = false,
    val location: String = "",
    val memo: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable

@Entity(tableName = "grades", indices = [Index("familyId"), Index("subjectId")])
data class GradeEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String,
    val title: String,
    val examType: ExamType = ExamType.QUIZ,
    val score: Double,
    val maxScore: Double = 100.0,
    /** 반 평균. 입력하지 않으면 null. */
    val classAverage: Double? = null,
    /** 시험일 (epoch day). */
    val date: Long,
    val memo: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val percent: Double get() = if (maxScore > 0) score / maxScore * 100.0 else 0.0
}

@Entity(tableName = "study_sessions", indices = [Index("familyId"), Index("startAt")])
data class StudySessionEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val startAt: Long,
    val endAt: Long,
    val durationMinutes: Int,
    val note: String = "",
    /** 타이머로 기록했는지, 직접 입력했는지. */
    val fromTimer: Boolean = false,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable

/**
 * 가족(학생 1명 단위)에 연결된 구성원. 학생 본인, 학부모, 멘토 모두 한 행씩 가집니다.
 * 멘토는 여러 명이 연결될 수 있고, 각자 담당 과목을 지정합니다.
 */
@Entity(tableName = "members", indices = [Index("familyId")])
data class MemberEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val role: String,
    val name: String,
    /** 멘토의 구분 (예: 수학 과외, 담임 선생님). */
    val title: String = "",
    /** 담당 과목 ID 목록. 쉼표로 구분. 비어 있으면 전 과목. */
    val subjectIds: String = "",
    /**
     * 멘토 기능(로드맵 큐레이팅, 과제 배정, 학급 진도 관리) 사용 여부.
     * 멘토는 항상 true, 학부모는 설정에서 켜서 "학부모 겸 멘토"가 될 수 있습니다.
     */
    val mentorEnabled: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val subjectIdList: List<String> get() = subjectIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun covers(subjectId: String?): Boolean = subjectIdList.isEmpty() || (subjectId != null && subjectId in subjectIdList)
}

/**
 * 멘토(또는 학부모 겸 멘토)가 큐레이팅한 학습 로드맵 항목.
 * "이 순서로, 이 자료로, 이 날짜까지" 를 제안하고 학생이 진행 상태를 갱신합니다.
 */
@Entity(tableName = "roadmap_items", indices = [Index("familyId"), Index("subjectId")])
data class RoadmapItemEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val title: String,
    val description: String = "",
    /** 참고 자료 링크나 교재명. */
    val resource: String = "",
    /** 목표일 (epoch day). null 이면 기한 없음. */
    val targetDate: Long? = null,
    val orderIndex: Int = 0,
    val status: RoadmapStatus = RoadmapStatus.PLANNED,
    val createdByName: String = "",
    val createdByRole: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable

/** 구성원(학부모/멘토/학생)이 서로에게 남기는 짧은 메모(격려, 요청, 피드백 등). */
@Entity(tableName = "notes", indices = [Index("familyId"), Index("createdAt")])
data class NoteEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val authorRole: String,
    val authorName: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
