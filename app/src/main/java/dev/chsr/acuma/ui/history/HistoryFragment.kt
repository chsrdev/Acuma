package dev.chsr.acuma.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.FragmentHistoryBinding
import dev.chsr.acuma.repository.TransactionRepository
import dev.chsr.acuma.ui.history.adapter.TransactionsAdapter
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModel
import dev.chsr.acuma.ui.viewmodel.TransactionsViewModelFactory
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val transactionsViewmodel = ViewModelProvider(
            this,
            TransactionsViewModelFactory(
                TransactionRepository(
                    AppDatabase.getInstance(requireContext()).transactionDao()
                )
            )
        )[TransactionsViewModel::class.java]
        val transactionsAdapter = TransactionsAdapter(parentFragmentManager, this)
        binding.transactionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsList.adapter = transactionsAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            transactionsViewmodel.transactions.collect { list ->
                transactionsAdapter.submitList(list)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}