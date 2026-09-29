package com.nextstep.app.data.notice

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nextstep.app.MainActivity
import com.nextstep.app.R
import com.nextstep.app.domain.notice.Notice

/** 알림 한 장을 보냅니다. 알림 권한이 없으면(안드로이드 13+) 조용히 넘어갑니다. 누르면 앱이 열립니다. */
class NoticePoster(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun post(notice: Notice) {
        if (!allowed()) return
        ensureChannel()
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_notice)
            .setContentTitle(notice.title)
            .setContentText(notice.lines.firstOrNull().orEmpty())
            .setStyle(NotificationCompat.BigTextStyle().bigText(notice.body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(notice.kind.ordinal + 1, n)
    }

    private fun allowed(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "하루 알림", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "아침에 오늘 챙길 것, 일요일 저녁에 주말 이야기를 한 번씩 알려 줘요"
        })
    }

    private companion object {
        const val CHANNEL = "daily"
    }
}
