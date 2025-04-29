package dev.chsr.acuma.repository

import dev.chsr.acuma.dao.CategoryDao
import dev.chsr.acuma.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAll()
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertAll(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }
}
