package dev.chsr.acuma.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val transactionsAdapter = TransactionsAdapter(this)
        binding.transactionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsList.adapter = transactionsAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            transactionsViewmodel.transactions.collect { list ->
                transactionsAdapter.submitList(list)
            }
        }

        val filterButton = binding.filterBtn
        binding.transactionsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && filterButton.isShown) {
                    filterButton.hide()
                } else if (dy < 0 && !filterButton.isShown) {
                    filterButton.show()
                }
            }
        })


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}