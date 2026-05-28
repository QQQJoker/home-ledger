package com.joker.homeledger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.joker.homeledger.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category WHERE type = :type AND isHidden = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeVisibleByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE type = :type ORDER BY sortOrder ASC, id ASC")
    fun observeAllByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(1) FROM transaction_record WHERE categoryId = :categoryId")
    suspend fun countRelatedTransactions(categoryId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CategoryEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: CategoryEntity): Long

    @Update
    suspend fun update(item: CategoryEntity)

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(1) FROM category")
    suspend fun totalCount(): Int

    @Query("SELECT * FROM category WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoryEntity?

    @Query(
        """
        SELECT COUNT(1) FROM category
        WHERE type = :type AND name = :name AND id != :excludeId
        """
    )
    suspend fun countByName(type: String, name: String, excludeId: Long): Int

    @Query("SELECT MIN(sortOrder) FROM category WHERE type = :type")
    suspend fun minSortOrder(type: String): Int?

    @Query("SELECT * FROM category ORDER BY id ASC")
    suspend fun listAll(): List<CategoryEntity>

    @Query("DELETE FROM category")
    suspend fun clearAll()
}
