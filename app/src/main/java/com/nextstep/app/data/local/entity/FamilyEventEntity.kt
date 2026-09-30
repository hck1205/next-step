package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 가족 달력의 일정 하나(학생·학부모가 함께 보고 고치고, 멘토에게는 보이지 않음 — MentorScopedStreams).
 * 날짜는 epoch day, 시각은 하루의 분이라 반복·여러 날 계산이 시간대와 상관없습니다(domain/familycalendar/FamilyCalendar).
 */
@Entity(tableName = "family_events", indices = [Index("familyId"), Index("startDate")])
data class FamilyEventEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val title: String,
    /** FamilyEventKind 이름. */
    val kind: String = "",
    val startDate: Long,
    /** 끝나는 날(여러 날 일정). 하루면 [startDate] 와 같습니다. */
    val endDate: Long = startDate,
    val allDay: Boolean = false,
    /** 시작·끝 시각(0시부터 분). [allDay] 면 쓰지 않습니다. */
    val startMinute: Int = 0,
    val endMinute: Int = 0,
    /** 누구의 일정인지(구성원 id, 쉼표). 비어 있으면 가족 모두. */
    val memberIds: String = "",
    /** 챙기는 사람(데려다주기·예약 등) 구성원 id. 비어 있으면 없음. */
    val keeperId: String = "",
    /** FamilyRepeat 이름. */
    val repeat: String = "",
    /** 반복이 끝나는 날(epoch day). null 이면 계속. */
    val repeatUntil: Long? = null,
    /** FamilyHeadsUp 이름: 오늘 화면에 며칠 전부터 미리 띄울지. */
    val headsUp: String = "",
    val location: String = "",
    /** 준비물(줄마다 하나). */
    val bring: String = "",
    val memo: String = "",
    val createdById: String = "",
    val createdByRole: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val memberIdList: List<String> get() = memberIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val bringList: List<String> get() = bring.lines().map { it.trim() }.filter { it.isNotEmpty() }
}
