package dev.chsr.acuma.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetCreateCategoryBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.repository.CategoryRepository

class CreateCategoryBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCreateCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateCategoryBinding.inflate(inflater, container, false)
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
        val categoryGoalText = binding.categoryGoal
        val categoryPercentSlider = binding.categoryPercentSlider
        // todo initial value of this slider will be maximum that can be (100 - sum of percents of all categories)
        categoryPercentSlider.setValues(0f)
        val categoryPercentText = binding.categoryPercentText

        categoryPercentText.text = "${categoryPercentSlider.values[0].toInt()}%"
        categoryPercentSlider.addOnChangeListener { _, value, _ ->
            categoryPercentText.text = "${value.toInt()}%"
        }

        val createCategoryButton = binding.createCategoryBtn
        createCategoryButton.setOnClickListener {
            categoriesViewmodel.addCategory(
                Category(
                    name = categoryNameText.text.toString(),
                    goal = if (categoryGoalText.text.toString()
                            .isEmpty()
                    ) null else categoryGoalText.text.toString().toInt(),
                    percent = categoryPercentSlider.values[0].toInt()
                )
            )
            dismiss()
        }

        return root
    }
}