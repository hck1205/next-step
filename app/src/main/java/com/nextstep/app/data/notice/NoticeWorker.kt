package com.nextstep.app.data.notice

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nextstep.app.NextStepApp
import com.nextstep.app.domain.notice.NoticeComposer
import com.nextstep.app.domain.notice.NoticeKind
import com.nextstep.app.domain.time.DateUtils
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first

/** 예약한 때에 알림 한 장을 만들어 보내고(월요일 아침에는 학교 학사일정도 새로 받고), 다음 때를 다시 예약합니다. 실패해도 다음 예약은 이어집니다. */
class NoticeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        runCatching { deliver(NoticeKind.from(inputData.getString(KEY_KIND))) }.onFailure { Log.w(TAG, "notice delivery failed", it) }
        NoticeScheduler.scheduleNext(applicationContext)
        return Result.success()
    }

    private suspend fun deliver(kind: NoticeKind) {
        val c = (applicationContext as NextStepApp).container
        if (!c.streams.profile.first().onboarded) return
        val today = DateUtils.today()
        // 월요일 아침마다 학교 학사일정을 한 번 더 받아 새로 생긴 것만 가족 달력에(알림을 꺼도).
        if (kind == NoticeKind.MORNING && today.dayOfWeek == DayOfWeek.MONDAY) c.school.syncNow(today)
        if (!c.noticeSettings.enabled.first()) return
        val input = NoticeInputs.of(c.streams, today)
        val notice = when (kind) {
            NoticeKind.MORNING -> NoticeComposer.morning(input, today)
            NoticeKind.WEEKEND -> NoticeComposer.weekend(input)
        } ?: return
        NoticePoster(applicationContext).post(notice)
    }

    companion object {
        const val KEY_KIND = "kind"
        private const val TAG = "NoticeWorker"
    }
}
