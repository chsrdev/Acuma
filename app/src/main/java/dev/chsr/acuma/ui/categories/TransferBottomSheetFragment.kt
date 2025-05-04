package dev.chsr.acuma.ui.categories

import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.chsr.acuma.R
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.BottomSheetTransferBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.repository.TransactionRepository
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModel
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModelFactory
import kotlinx.coroutines.launch


class TransferBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetTransferBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTransferBinding.inflate(inflater, container, false)
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

        val categoriesSpinner1 = binding.categoriesSpinner1
        val categoriesSpinner2 = binding.categoriesSpinner2
        var categories: List<Category> = listOf()

        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewmodel.categories.collect { list ->
                categories = list

                val names = mutableListOf<String>()
                names.addAll(list.map { it.name })

                val spinnerAdapter1 = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names
                )

                spinnerAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoriesSpinner1.adapter = spinnerAdapter1

                val names2 = names.toMutableList()
                names2.add(0, getString(R.string.distribute))

                val spinnerAdapter2 = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    names2
                )

                spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categoriesSpinner2.adapter = spinnerAdapter2
            }
        }

        val amountText = binding.amount
        val transferButton = binding.transferBtn

        transferButton.setOnClickListener {
            val amount = (amountText.text.toString().toFloat() * 100).toInt()
            val category1 = categories[categoriesSpinner1.selectedItemPosition]

            val updatedCategory1 = Category(
                category1.id,
                category1.name,
                category1.percent,
                category1.balance - amount,
                category1.goal
            )
            categoriesViewmodel.updateCategory(updatedCategory1)

            if (categoriesSpinner2.selectedItemPosition == 0) {
                var percentSum = 0
                categories.forEach { category ->
                    if (categories.indexOf(category) != categoriesSpinner1.selectedItemPosition) {
                        if (amount * category.percent / 100 != 0) {
                            percentSum += amount * category.percent / 1000
                            val updatedCategory2 = Category(
                                category.id,
                                category.name,
                                category.percent,
                                category.balance + amount * category.percent / 100,
                                category.goal
                            )
                            categoriesViewmodel.updateCategory(updatedCategory2)
                            transactionsViewmodel.addTransaction(
                                Transaction(
                                    fromId = category1.id,
                                    toId = category.id,
                                    amount = amount * category.percent / 100,
                                    comment = binding.comment.text.toString(),
                                    date = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
                Log.d("percent", percentSum.toString())
                if (percentSum < 100) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        categoriesViewmodel.getById(-1).collect { reserve ->
                            val updatedReserve = Category(
                                reserve.id,
                                reserve.name,
                                reserve.percent,
                                reserve.balance + amount * (100 - percentSum) / 100,
                                reserve.goal
                            )
                            categoriesViewmodel.updateCategory(updatedReserve)
                            transactionsViewmodel.addTransaction(
                                Transaction(
                                    fromId = category1.id,
                                    toId = reserve.id,
                                    amount = amount * (100 - percentSum) / 100,
                                    comment = binding.comment.text.toString(),
                                    date = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            } else {
                val category2 = categories[categoriesSpinner2.selectedItemPosition - 1]
                val updatedCategory2 = Category(
                    category2.id,
                    category2.name,
                    category2.percent,
                    category2.balance + amount,
                    category2.goal
                )
                categoriesViewmodel.updateCategory(updatedCategory2)

                transactionsViewmodel.addTransaction(
                    Transaction(
                        fromId = category1.id,
                        toId = category2.id,
                        amount = amount,
                        comment = binding.comment.text.toString(),
                        date = System.currentTimeMillis()
                    )
                )
            }


            dismiss()
        }

        return root
    }
}