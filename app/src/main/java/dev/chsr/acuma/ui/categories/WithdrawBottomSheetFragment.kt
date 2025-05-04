package dev.chsr.acuma.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetWithdrawBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.repository.TransactionRepository
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModel
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModelFactory
import kotlinx.coroutines.launch


class WithdrawBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetWithdrawBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWithdrawBinding.inflate(inflater, container, false)
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
                categories = list

                val names = mutableListOf<String>()
                names.addAll(list.map { it.name })

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoriesSpinner.adapter = spinnerAdapter
            }
        }

        val amountText = binding.amount
        val withdrawButton = binding.withdrawBtn

        withdrawButton.setOnClickListener {
            val amount = (amountText.text.toString().toFloat() * 100).toInt()
            val selected = categories[categoriesSpinner.selectedItemPosition]
            val updatedCategory = Category(
                selected.id,
                selected.name,
                selected.percent,
                selected.balance - amount,
                selected.goal
            )
            categoriesViewmodel.updateCategory(updatedCategory)

            transactionsViewmodel.addTransaction(
                Transaction(
                    fromId = selected.id,
                    toId = null,
                    amount = amount,
                    comment = binding.comment.text.toString(),
                    date = System.currentTimeMillis()
                )
            )

            dismiss()
        }

        return root
    }
}