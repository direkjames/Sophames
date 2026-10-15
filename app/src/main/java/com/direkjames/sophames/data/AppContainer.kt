package com.direkjames.sophames.data

import android.content.Context

/** Tiny manual DI container. Supabase sync will plug in here later. */
class AppContainer(val appContext: Context) {
    val database: SophamesDatabase = SophamesDatabase.build(appContext)
    val settings: SettingsStore = SettingsStore(appContext)
    val photos: PhotoStore = PhotoStore(appContext)
}
