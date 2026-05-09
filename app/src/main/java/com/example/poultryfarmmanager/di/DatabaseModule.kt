package com.example.poultryfarmmanager.di

import android.content.Context
import androidx.room.Room
import com.example.poultryfarmmanager.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "poultry_farm.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBirdDao(db: AppDatabase): BirdDao = db.birdDao()
    @Provides fun provideEggProductionDao(db: AppDatabase): EggProductionDao = db.eggProductionDao()
    @Provides fun provideFeedDao(db: AppDatabase): FeedDao = db.feedDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideHealthDao(db: AppDatabase): HealthDao = db.healthDao()
}
