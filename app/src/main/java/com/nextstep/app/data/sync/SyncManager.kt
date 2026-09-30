package com.nextstep.app.data.sync

import com.nextstep.app.data.model.SyncStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * 학생 기기와 학부모·멘토 기기 사이의 데이터 동기화 경계.
 * 구현체가 없으면(Firebase 미설정) [NoOpSyncManager] 로 로컬 전용 모드가 됩니다.
 */
interface SyncManager {
    val status: StateFlow<SyncStatus>

    /** 원격 동기화 백엔드가 구성되어 있는지. */
    val isAvailable: Boolean

    /** 가족 문서를 서버에 생성합니다 (학생 온보딩). */
    suspend fun createFamily(info: FamilyInfo): Result<Unit>

    /** 연결 코드로 가족을 찾습니다 (학부모·멘토 온보딩). */
    suspend fun findFamilyByCode(code: String): Result<FamilyInfo?>

    /** 실시간 수신을 시작하고 미전송 변경을 올립니다. */
    fun start(familyId: String)

    fun stop()

    /** 로컬 변경 후 호출. 디바운스 뒤 서버로 밀어 올립니다. */
    fun requestPush()
}
