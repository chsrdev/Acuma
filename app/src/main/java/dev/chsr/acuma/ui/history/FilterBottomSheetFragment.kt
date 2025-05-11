package dev.chsr.acuma.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetHistoryFilterBinding
import dev.chsr.acuma.databinding.BottomSheetWithdrawBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.repository.TransactionRepository
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModel
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModelFactory
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.log

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetHistoryFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHistoryFilterBinding.inflate(inflater, container, false)
        val root = binding.root
        val categoriesViewmodel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]
        val transactionsViewmodel = ViewModelProvider(
            this,
            TransactionsViewModelFactory(
                TransactionRepository(
                    AppDatabase.getInstance(requireContext()).transactionDao()
                )
            )
        )[TransactionsViewModel::class.java]

        val categoriesSpinner = binding.categoriesSpinner
        var categories: List<Category> = listOf()

        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewmodel.categories.collect { list ->
                categories = list.filter { category -> category.deleted == 0 }

                val names = mutableListOf("All categories")
                names.addAll(categories.map { it.name })

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoriesSpinner.adapter = spinnerAdapter
            }
        }

        binding.filterBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                dismiss()
//                val list = transactionsViewmodel.transactions.first()
//                var filtered = list
//                if (categoriesSpinner.selectedItemPosition != 0) {
//                    val selectedCategoryId = categories[categoriesSpinner.selectedItemPosition - 1].id
//                    filtered = list.filter { it.toId == selectedCategoryId || it.fromId == selectedCategoryId }
//                }
//                transactionsViewmodel.setFilteredTransactions(filtered)
            }
        }

        return root
    }
}