package com.nextstep.app.ui.common

import android.content.Context
import android.content.Intent

/** 글 한 편을 다른 앱(카카오톡·문자 …)으로 보냅니다. 받을 앱은 사용자가 고릅니다. */
fun Context.shareText(text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
