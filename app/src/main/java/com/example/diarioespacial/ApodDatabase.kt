package com.example.diarioespacial

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "apod_items")
data class ApodEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val url: String,
    val mediaType: String,
    val hdUrl: String?,
    val copyright: String?,
    val isFavorite: Boolean = false
)

@Dao
interface ApodDao {
    @Query("SELECT * FROM apod_items ORDER BY date DESC")
    fun observeAll(): Flow<List<ApodEntity>>

    @Query("SELECT * FROM apod_items WHERE isFavorite = 1 ORDER BY date DESC")
    fun observeFavorites(): Flow<List<ApodEntity>>

    @Query("SELECT * FROM apod_items WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<ApodEntity?>

    @Query("SELECT date FROM apod_items WHERE isFavorite = 1")
    suspend fun getFavoriteDates(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ApodEntity>)

    @Query("UPDATE apod_items SET isFavorite = :isFav WHERE date = :date")
    suspend fun updateFavorite(date: String, isFav: Boolean)
}

@Database(
    entities = [ApodEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diario_espacial.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}