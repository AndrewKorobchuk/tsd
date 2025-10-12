package com.sh.an.tsd.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.sh.an.tsd.data.model.UnitOfMeasure
import com.sh.an.tsd.data.model.NomenclatureCategory
import com.sh.an.tsd.data.model.Nomenclature

@Database(
    entities = [UnitOfMeasure::class, NomenclatureCategory::class, Nomenclature::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TsdDatabase : RoomDatabase() {

    abstract fun unitOfMeasureDao(): UnitOfMeasureDao
    abstract fun nomenclatureCategoryDao(): NomenclatureCategoryDao
    abstract fun nomenclatureDao(): NomenclatureDao

    companion object {
        @Volatile
        private var INSTANCE: TsdDatabase? = null

        fun getDatabase(context: Context): TsdDatabase {
            return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        TsdDatabase::class.java,
                        "tsd_database_v3"
                    )
                .fallbackToDestructiveMigration() // Для разработки - удаляет БД при проблемах
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
