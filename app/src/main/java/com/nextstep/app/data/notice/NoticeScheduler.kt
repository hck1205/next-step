package com.nextstep.app.data.notice

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.nextstep.app.domain.notice.NoticeSchedule
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** 알림 예약: 다음 때(NoticeSchedule) 하나만 걸어 둡니다. 앱을 열 때 없으면 걸고, 알림을 보낸 뒤 다음 것을 겁니다. */
object NoticeScheduler {
    private const val WORK = "daily-notice"

    /** 이미 걸려 있으면 그대로 둡니다. */
    fun ensure(context: Context) = enqueue(context, ExistingWorkPolicy.KEEP)

    /** 지금 돌고 있는 알림이 끝난 뒤 다음 때를 겁니다. */
    fun scheduleNext(context: Context) = enqueue(context, ExistingWorkPolicy.APPEND_OR_REPLACE)

    private fun enqueue(context: Context, policy: ExistingWorkPolicy) {
        val now = LocalDateTime.now()
        val slot = NoticeSchedule.next(now)
        val request = OneTimeWorkRequestBuilder<NoticeWorker>()
            .setInitialDelay(Duration.between(now, slot.at).toMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(NoticeWorker.KEY_KIND to slot.kind.name))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, policy, request)
    }
}
