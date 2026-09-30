package com.nextstep.app.data.sync

import com.nextstep.app.data.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Firebase 가 구성되지 않았을 때의 로컬 전용 구현. 모든 호출이 즉시 성공하고 아무것도 보내지 않습니다. */
class NoOpSyncManager : SyncManager {
    override val status: StateFlow<SyncStatus> = MutableStateFlow(SyncStatus.LOCAL_ONLY).asStateFlow()
    override val isAvailable: Boolean = false
    override suspend fun createFamily(info: FamilyInfo): Result<Unit> = Result.success(Unit)
    override suspend fun findFamilyByCode(code: String): Result<FamilyInfo?> = Result.success(null)
    override fun start(familyId: String) = Unit
    override fun stop() = Unit
    override fun requestPush() = Unit
}
