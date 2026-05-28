package com.joker.homeledger.core.data.repository

import com.joker.homeledger.core.database.dao.CategoryDao
import com.joker.homeledger.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun observeAllByType(type: String): Flow<List<CategoryEntity>> = categoryDao.observeAllByType(type)

    fun observeVisibleByType(type: String): Flow<List<CategoryEntity>> = categoryDao.observeVisibleByType(type)

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun isNameDuplicated(type: String, name: String, excludeId: Long = -1L): Boolean =
        categoryDao.countByName(type, name.trim(), excludeId) > 0

    suspend fun addCategory(item: CategoryEntity): Long = categoryDao.insert(item)

    suspend fun updateCategory(item: CategoryEntity) = categoryDao.update(item)

    suspend fun canDelete(id: Long): Boolean = categoryDao.countRelatedTransactions(id) == 0

    suspend fun deleteCategory(id: Long) = categoryDao.deleteById(id)

    suspend fun pinToTop(category: CategoryEntity) {
        val minOrder = categoryDao.minSortOrder(category.type) ?: 0
        categoryDao.update(
            category.copy(
                sortOrder = minOrder - 1,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setHidden(category: CategoryEntity, hidden: Boolean) {
        categoryDao.update(
            category.copy(
                isHidden = hidden,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
