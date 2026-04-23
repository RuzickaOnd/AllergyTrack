package com.allergytrack.app.di

import android.content.Context
import androidx.room.Room
import com.allergytrack.app.data.local.database.AllergyDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AllergyDatabase =
        Room.databaseBuilder(
            context,
            AllergyDatabase::class.java,
            "allergy_track.db"
        )
        .addMigrations(AllergyDatabase.MIGRATION_1_2)
        .build()

    @Provides fun provideFoodLogDao(db: AllergyDatabase) = db.foodLogDao()
    @Provides fun provideSymptomLogDao(db: AllergyDatabase) = db.symptomLogDao()
    @Provides fun provideEnvironmentLogDao(db: AllergyDatabase) = db.environmentLogDao()
}
