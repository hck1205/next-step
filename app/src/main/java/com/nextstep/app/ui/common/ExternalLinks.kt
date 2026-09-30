package com.nextstep.app.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri

/** 외부 링크(유튜브 등)를 여는 한 곳. 처리할 앱이 없어도 앱이 죽지 않습니다. */
object ExternalLinks {
    fun open(context: Context, url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }
}
