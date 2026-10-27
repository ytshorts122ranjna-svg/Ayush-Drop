package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AyushDropDao
import com.example.data.model.FileRecordEntity
import com.example.data.model.GameScoreEntity
import com.example.data.model.ShareLinkEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FileRecordEntity::class,
        ShareLinkEntity::class,
        GameScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AyushDropDatabase : RoomDatabase() {
    abstract fun dao(): AyushDropDao

    companion object {
        @Volatile
        private var INSTANCE: AyushDropDatabase? = null

        fun getInstance(context: Context): AyushDropDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AyushDropDatabase::class.java,
                    "ayushdrop_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
