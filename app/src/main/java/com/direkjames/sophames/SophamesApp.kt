package com.direkjames.sophames

import android.app.Application
import com.direkjames.sophames.data.AppContainer
import com.direkjames.sophames.reminders.Reminders

class SophamesApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Reminders.ensureChannel(this)
    }
}
