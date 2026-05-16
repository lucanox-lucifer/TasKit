package com.arcanox.taskit.di

import android.content.Context
import androidx.room.Room
import com.arcanox.taskit.data.local.TasKitDatabase
import com.arcanox.taskit.data.local.dao.CategoryDao
import com.arcanox.taskit.data.local.dao.TaskDao
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
    fun provideDatabase(@ApplicationContext context: Context): TasKitDatabase {
        return Room.databaseBuilder(
            context,
            TasKitDatabase::class.java,
            "taskit_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideTaskDao(db: TasKitDatabase): TaskDao {
        return db.taskDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: TasKitDatabase): CategoryDao {
        return db.categoryDao()
    }
}
