package com.nextstep.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.nextstep.app.di.AppContainer
import kotlin.concurrent.thread

class NextStepApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // 광고 SDK 초기화는 느릴 수 있어 백그라운드에서. 실패해도 앱은 정상 동작합니다.
        thread(name = "ads-init") { runCatching { MobileAds.initialize(this) } }
    }
}
