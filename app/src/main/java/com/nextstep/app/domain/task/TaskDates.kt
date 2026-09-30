package com.nextstep.app.domain.task

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.ZoneId

/** 할 일을 끝낸 날(끝내지 않았거나 끝낸 시각이 없으면 null). 달성률·기록·응원·게임 요소가 같은 날짜를 봅니다. */
fun TaskEntity.doneOn(zone: ZoneId = DateUtils.zone): LocalDate? = doneAt?.takeIf { done }?.let { DateUtils.toLocalDate(it, zone) }

/** 마감([TaskEntity.dueDate])이 [today] 보다 앞인데 아직 안 끝낸 할 일(밀린 일). */
fun TaskEntity.isOverdue(today: LocalDate = DateUtils.today()): Boolean = !done && dueDate < today.toEpochDay()
