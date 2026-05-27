package com.urlrouter.data.db

import android.content.Context
import androidx.room.*
import com.urlrouter.model.BrowserInfo
import com.urlrouter.model.MatchType
import com.urlrouter.model.RoutingRule
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromMatchType(value: MatchType): String = value.name
    @TypeConverter
    fun toMatchType(value: String): MatchType = MatchType.valueOf(value)
}

@Dao
interface BrowserDao {
    @Query("SELECT * FROM browsers ORDER BY displayOrder ASC")
    fun observeAll(): Flow<List<BrowserInfo>>

    @Query("SELECT * FROM browsers ORDER BY displayOrder ASC")
    suspend fun getAll(): List<BrowserInfo>

    @Query("SELECT * FROM browsers WHERE isEnabled = 1 ORDER BY displayOrder ASC")
    fun observeEnabled(): Flow<List<BrowserInfo>>

    @Query("SELECT * FROM browsers WHERE packageName = :pkg LIMIT 1")
    suspend fun getByPackage(pkg: String): BrowserInfo?

    @Upsert
    suspend fun upsertAll(browsers: List<BrowserInfo>)

    @Upsert
    suspend fun upsert(browser: BrowserInfo)

    @Query("UPDATE browsers SET isEnabled = :enabled WHERE packageName = :pkg")
    suspend fun setEnabled(pkg: String, enabled: Boolean)

    @Query("UPDATE browsers SET displayOrder = :order WHERE packageName = :pkg")
    suspend fun setOrder(pkg: String, order: Int)

    @Query("DELETE FROM browsers WHERE packageName NOT IN (:packages)")
    suspend fun removeStale(packages: List<String>)

    @Query("DELETE FROM browsers")
    suspend fun deleteAll()
}

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY matchType ASC, id ASC")
    fun observeAll(): Flow<List<RoutingRule>>

    @Query("SELECT * FROM rules WHERE isEnabled = 1 ORDER BY matchType ASC, id ASC")
    suspend fun getEnabled(): List<RoutingRule>

    @Query("SELECT * FROM rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RoutingRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RoutingRule): Long

    @Update
    suspend fun update(rule: RoutingRule)

    @Delete
    suspend fun delete(rule: RoutingRule)

    @Query("UPDATE rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM rules")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RoutingRule>)
}

@Database(
    entities = [BrowserInfo::class, RoutingRule::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun browserDao(): BrowserDao
    abstract fun ruleDao(): RuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "url_router.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
