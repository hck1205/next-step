package com.nextstep.app.data.sync

import com.nextstep.app.data.model.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 페어링 코드로 찾은 가족 정보. */
data class FamilyInfo(val familyId: String, val pairingCode: String, val studentName: String)

/**
 * 학생 기기와 학부모 기기 사이의 데이터 동기화를 담당합니다.
 * 구현체가 없으면(Firebase 미설정) 로컬 전용 모드로 동작합니다.
 */
interface SyncManager {
    val status: StateFlow<SyncStatus>

    /** 원격 동기화 백엔드가 구성되어 있는지. */
    val isAvailable: Boolean

    /** 가족 문서를 서버에 생성합니다 (학생이 온보딩할 때). */
    suspend fun createFamily(info: FamilyInfo): Result<Unit>

    /** 페어링 코드로 가족을 찾습니다 (학부모가 온보딩할 때). */
    suspend fun findFamilyByCode(code: String): Result<FamilyInfo?>

    /** 실시간 수신을 시작하고 로컬의 미전송 변경을 올립니다. */
    fun start(familyId: String)

    fun stop()

    /** 로컬 변경이 생겼을 때 호출. 디바운스 후 서버로 밀어 올립니다. */
    fun requestPush()
}

/** Firebase 가 구성되지 않았을 때 사용하는 로컬 전용 구현. */
class NoOpSyncManager : SyncManager {
    override val status = MutableStateFlow(SyncStatus.LOCAL_ONLY)
    override val isAvailable: Boolean = false
    override suspend fun createFamily(info: FamilyInfo): Result<Unit> = Result.success(Unit)
    override suspend fun findFamilyByCode(code: String): Result<FamilyInfo?> = Result.success(null)
    override fun start(familyId: String) = Unit
    override fun stop() = Unit
    override fun requestPush() = Unit
}
