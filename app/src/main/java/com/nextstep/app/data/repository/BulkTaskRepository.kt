package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.TaskEntity

/**
 * 멘토가 맡은 다른 학생들(다른 가족)에게 같은 과제를 한 번에 냅니다(튜터 Pro 로 나눌 수 있는 기능 — 지금은 모두 열림).
 * 과목은 이름으로 그 가족의 과목을 찾아 붙이고, 없으면 과목 없이 냅니다. 다른 가족 것은 그 학생을 열어 동기화할 때 올라갑니다.
 */
interface BulkTaskRepository {
    /** 낸 수. */
    suspend fun assign(task: TaskEntity, familyIds: List<String>, subjectName: String?): Int
}
