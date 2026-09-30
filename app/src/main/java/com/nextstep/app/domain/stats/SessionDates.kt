package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.live
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.ZoneId

/** 공부를 시작한 날. 공부한 날 세기·연속·주간 비교가 같은 날짜를 봅니다. */
fun StudySessionEntity.day(zone: ZoneId = DateUtils.zone): LocalDate = DateUtils.toLocalDate(startAt, zone)

/** 공부한 날 모음(지운 기록 제외). */
fun List<StudySessionEntity>.studyDays(zone: ZoneId = DateUtils.zone): Set<LocalDate> = live().map { it.day(zone) }.toSet()
