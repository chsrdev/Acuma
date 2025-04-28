package dev.chsr.acuma.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import dev.chsr.acuma.entity.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getAll(): List<Category>

    @Insert
    fun insertAll(vararg categories: Category)

    @Delete
    fun delete(category: Category)
}