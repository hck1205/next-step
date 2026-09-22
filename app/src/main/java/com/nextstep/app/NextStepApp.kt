package com.nextstep.app

import android.app.Application
import com.nextstep.app.di.AppContainer

class NextStepApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
