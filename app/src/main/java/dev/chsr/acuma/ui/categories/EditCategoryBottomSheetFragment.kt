package dev.chsr.acuma.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetEditCategoryBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import kotlinx.coroutines.launch

class EditCategoryBottomSheetFragment(val category: Category) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetEditCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditCategoryBinding.inflate(inflater, container, false)
        val root = binding.root
        val categoriesViewmodel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]

        val categoryNameText = binding.categoryName
        categoryNameText.setText(category.name)
        val categoryGoalText = binding.categoryGoal
        if (category.goal != null)
            categoryGoalText.setText((category.goal / 100f).toInt().toString())
        val categoryPercentSlider = binding.categoryPercentSlider
        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewmodel.categories.collect { list ->
                val percentSum =
                    list.sumOf { _category -> if (category != _category && category.id != -1) _category.percent else 0 }
                if (percentSum == 100)
                    categoryPercentSlider.isEnabled = false
                else
                    categoryPercentSlider.valueTo = 100f - percentSum
            }
        }
        categoryPercentSlider.setValues(category.percent.toFloat())

        if (category.id == -1)
            categoryPercentSlider.isEnabled = false

        val categoryPercentText = binding.categoryPercentText

        categoryPercentText.text = "${categoryPercentSlider.values[0].toInt()}%"
        categoryPercentSlider.addOnChangeListener { _, value, _ ->
            categoryPercentText.text = "${value.toInt()}%"
        }

        val saveButton = binding.saveBtn
        val deleteButton = binding.deleteBtn
        saveButton.setOnClickListener {
            val percent = categoryPercentSlider.values[0].toInt()
            val updatedCategoory = Category(
                id = category.id,
                name = categoryNameText.text.toString(),
                goal = if (categoryGoalText.text.toString()
                        .isEmpty()
                ) null else (categoryGoalText.text.toString().toFloat() * 100).toInt(),
                balance = category.balance,
                percent = percent
            )
            categoriesViewmodel.updateCategory(updatedCategoory)
            viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewmodel.getById(-1).collect { reserveCategory ->
                    categoriesViewmodel.updateCategory(Category(
                        id = reserveCategory.id,
                        name = reserveCategory.name,
                        percent = reserveCategory.percent -  (percent - category.percent ),
                        balance = reserveCategory.balance,
                        goal = reserveCategory.goal
                    ))
                }
            }
            dismiss()
        }
        deleteButton.setOnClickListener {
            categoriesViewmodel.deleteCategory(category)
            viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewmodel.getById(-1).collect { reserveCategory ->
                    categoriesViewmodel.updateCategory(Category(
                        id = reserveCategory.id,
                        name = reserveCategory.name,
                        percent = reserveCategory.percent + category.percent,
                        balance = reserveCategory.balance,
                        goal = reserveCategory.goal
                    ))
                }
            }
            dismiss()
        }

        return root
    }
}