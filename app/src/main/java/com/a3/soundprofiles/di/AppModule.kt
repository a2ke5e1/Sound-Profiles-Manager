package com.a3.soundprofiles.di

import androidx.room.Room
import com.a3.soundprofiles.data.local.AppDatabase
import com.a3.soundprofiles.data.repository.AppSettingsRepository
import com.a3.soundprofiles.data.repository.ScheduleRepository
import com.a3.soundprofiles.data.repository.SoundProfileRepository
import com.a3.soundprofiles.ui.SoundProfileViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "sound_profiles_db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    single { get<AppDatabase>().soundProfileDao() }
    single { get<AppDatabase>().scheduleDao() }
    
    single { SoundProfileRepository(get()) }
    single { ScheduleRepository(get()) }
    single { AppSettingsRepository(androidContext()) }

    viewModel { SoundProfileViewModel(androidApplication(), get(), get(), get()) }
}
