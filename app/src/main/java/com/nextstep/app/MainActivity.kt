package com.nextstep.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.nextstep.app.ui.navigation.NextStepRoot
import com.nextstep.app.ui.theme.NextStepTheme

class MainActivity : ComponentActivity() {
    /** 안드로이드 13+ 알림 권한. 거절해도 앱은 그대로 쓰고, 가족 탭의 알림 줄에서 다시 켤 수 있습니다. */
    private val askNotices = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            askNotices.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            NextStepTheme {
                NextStepRoot()
            }
        }
    }
}
