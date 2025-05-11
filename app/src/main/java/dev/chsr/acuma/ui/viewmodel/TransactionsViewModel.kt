package dev.chsr.acuma.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.chsr.acuma.entity.Transaction
import dev.chsr.acuma.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransactionsViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {
    val transactions = transactionRepository.getAllTransactions()

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }
}

class TransactionsViewModelFactory(private val transactionRepository: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransactionsViewModel(transactionRepository) as T
    }
}