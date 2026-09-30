package com.nextstep.app.domain.reward

/** 보상을 걸 수 있는 곳 하나: 종류 · 저장할 대상 id(목표 id 또는 번호) · 보이는 이름. */
data class RewardTarget(val kind: RewardKind, val id: String, val label: String)
