package com.nextstep.app.ui.common

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** 앱이 만든 파일(PDF)을 다른 앱(카카오톡·메일·드라이브 …)으로 보냅니다. 받는 앱에는 읽기 권한만 잠시 줍니다. */
fun Context.shareFile(file: File, mimeType: String, chooserTitle: String) {
    val uri = FileProvider.getUriForFile(this, "$packageName.exports", file)
    val send = Intent(Intent.ACTION_SEND).setType(mimeType).putExtra(Intent.EXTRA_STREAM, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
