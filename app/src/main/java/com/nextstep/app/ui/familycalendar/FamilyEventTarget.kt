package com.nextstep.app.ui.familycalendar

import com.nextstep.app.data.local.entity.FamilyEventEntity
import java.time.LocalDate

/** 열려 있는 입력창: 고칠 일정([existing], null 이면 새로) 과 새 일정의 기본 날짜. */
internal data class FamilyEventTarget(val existing: FamilyEventEntity?, val date: LocalDate)
