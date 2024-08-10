package com.a3.soundprofiles.core.di

import android.content.Context
import com.a3.soundprofiles.core.database.SoundProfileDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Singleton
  @Provides
  fun providerSoundProfileDB(@ApplicationContext appContext: Context) =
      SoundProfileDB.getInstance(appContext)

  @Singleton
  @Provides
  fun providerSoundProfileDao(soundProfileDB: SoundProfileDB) = soundProfileDB.soundProfileDao()
}
