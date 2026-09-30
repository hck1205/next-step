package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.Syncable

/** 가족 문서마다 들어가는 공통 필드(id · 가족 · 수정 시각 · 삭제). `dirty` 는 기기 로컬 상태라 넣지 않습니다. */
internal fun Syncable.syncFields(): Map<String, Any?> = mapOf("id" to id, "familyId" to familyId, "updatedAt" to updatedAt, "deleted" to deleted)
