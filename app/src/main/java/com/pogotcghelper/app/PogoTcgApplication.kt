package com.pogotcghelper.app

import android.app.Application
import com.pogotcghelper.app.di.AppContainer

class PogoTcgApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
