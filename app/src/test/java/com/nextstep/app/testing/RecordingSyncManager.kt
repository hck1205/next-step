package com.nextstep.app.testing

import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.sync.FamilyInfo
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow

/** 호출을 기록만 하는 동기화 매니저. 저장소가 쓰기 후 push 를 요청하는지 검증합니다. */
class RecordingSyncManager(
    override val isAvailable: Boolean = true,
    private val families: MutableMap<String, FamilyInfo> = mutableMapOf(),
    var createResult: Result<Unit> = Result.success(Unit),
) : SyncManager {
    override val status = MutableStateFlow(SyncStatus.SYNCED)
    var pushRequests = 0
    var startedWith: String? = null
    var stopped = 0

    override suspend fun createFamily(info: FamilyInfo): Result<Unit> {
        if (createResult.isSuccess) families[info.pairingCode] = info
        return createResult
    }

    override suspend fun findFamilyByCode(code: String): Result<FamilyInfo?> = Result.success(families[code])
    override fun start(familyId: String) { startedWith = familyId }
    override fun stop() { stopped++ }
    override fun requestPush() { pushRequests++ }
}
