package com.sh.an.tsd.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.sh.an.tsd.data.model.UnitOfMeasure

@Database(
    entities = [UnitOfMeasure::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TsdDatabase : RoomDatabase() {

    abstract fun unitOfMeasureDao(): UnitOfMeasureDao

    companion object {
        @Volatile
        private var INSTANCE: TsdDatabase? = null

        fun getDatabase(context: Context): TsdDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TsdDatabase::class.java,
                    "tsd_database_v2"
                )
                .fallbackToDestructiveMigration() // Для разработки - удаляет БД при проблемах
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
