package com.nextstep.app.di

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nextstep.app.data.local.AppDatabase
import com.nextstep.app.data.prefs.UserPreferences
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.data.sync.FirestoreSyncManager
import com.nextstep.app.data.sync.NoOpSyncManager
import com.nextstep.app.data.sync.SyncManager

/**
 * 수동 의존성 컨테이너. Hilt 없이도 충분히 단순한 그래프라 직접 조립합니다.
 */
class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.get(context)
    val preferences: UserPreferences = UserPreferences(context)

    /** google-services.json 이 있어 FirebaseApp 이 초기화됐을 때만 원격 동기화를 켭니다. */
    val syncManager: SyncManager = if (FirebaseApp.getApps(context).isNotEmpty()) {
        try {
            FirestoreSyncManager(database, FirebaseFirestore.getInstance(), FirebaseAuth.getInstance())
        } catch (e: Exception) {
            Log.w("AppContainer", "Firebase 초기화 실패, 로컬 전용 모드로 동작합니다", e)
            NoOpSyncManager()
        }
    } else {
        Log.i("AppContainer", "Firebase 미설정: 로컬 전용 모드")
        NoOpSyncManager()
    }

    val repository: StudyRepository = StudyRepository(database, preferences, syncManager)
}
