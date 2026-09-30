package com.nextstep.app.data.notice

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.noticeStore: DataStore<Preferences> by preferencesDataStore(name = "nextstep_notice")

class DataStoreNoticeSettings(private val context: Context) : NoticeSettings {
    private val key = booleanPreferencesKey("enabled")

    override val enabled: Flow<Boolean> = context.noticeStore.data.map { it[key] ?: true }

    override suspend fun setEnabled(enabled: Boolean) {
        context.noticeStore.edit { it[key] = enabled }
    }
}
