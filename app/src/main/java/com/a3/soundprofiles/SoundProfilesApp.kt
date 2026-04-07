package com.a3.soundprofiles

import android.app.Application
import com.a3.soundprofiles.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SoundProfilesApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SoundProfilesApp)
            modules(appModule)
        }
    }
}
