package dev.chsr.acuma.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.HistoryDepositItemBinding
import dev.chsr.acuma.databinding.HistoryTransferItemBinding
import dev.chsr.acuma.databinding.HistoryWithdrawItemBinding
import dev.chsr.acuma.entity.Category
import dev.chsr.acuma.entity.Transaction
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.ui.history.HistoryFragment
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModel
import dev.chsr.acuma.ui.viewmodel.CategoriesViewModelFactory
import kotlinx.coroutines.launch

class TransactionsAdapter(val fragmentManager: FragmentManager, val owner: HistoryFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var transactions: List<Transaction> = emptyList()

    sealed class ListItem {
        data class DepositItem(val transaction: Transaction, val category: Category) : ListItem()
        data class WithdrawItem(val transaction: Transaction, val category: Category) : ListItem()
        data class TransferItem(
            val transaction: Transaction,
            val fromCategory: Category,
            val toCategory: Category
        ) : ListItem()
    }

    class DepositViewHolder(val binding: HistoryDepositItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.DepositItem) {
            binding.category.text = item.category.name
            binding.amount.text = "+${item.transaction.amount / 100f}"
        }
    }

    class WithdrawViewHolder(val binding: HistoryWithdrawItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.WithdrawItem) {
            binding.category.text = item.category.name
            binding.amount.text = "-${item.transaction.amount / 100f}"
        }
    }

    class TransferViewHolder(val binding: HistoryTransferItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.TransferItem) {
            binding.fromCategory.text = item.fromCategory.name
            binding.toCategory.text = item.toCategory.name
            binding.amount.text = (item.transaction.amount / 100f).toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val transaction = transactions[position]

        if (transaction.fromId == null && transaction.toId != null)
            return 0
        if (transaction.fromId != null && transaction.toId == null)
            return 1
        return 2
    }

    fun submitList(newList: List<Transaction>) {
        transactions = newList.reversed()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> DepositViewHolder(
                HistoryDepositItemBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )

            1 -> WithdrawViewHolder(
                HistoryWithdrawItemBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )

            2 -> TransferViewHolder(
                HistoryTransferItemBinding.inflate(
                    LayoutInflater.from(viewGroup.context),
                    viewGroup,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val transaction = transactions[position]
        val categoriesViewmodel = ViewModelProvider(
            owner,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(holder.itemView.context).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]

        if (transaction.fromId == null && transaction.toId != null)
            owner.viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewmodel.getById(transaction.toId).collect { category ->
                    (holder as DepositViewHolder).bind(ListItem.DepositItem(transaction, category))
                }
            }
        else if (transaction.fromId != null && transaction.toId == null)
            owner.viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewmodel.getById(transaction.fromId).collect { category ->
                    (holder as WithdrawViewHolder).bind(
                        ListItem.WithdrawItem(
                            transaction,
                            category
                        )
                    )
                }
            }
        else
            owner.viewLifecycleOwner.lifecycleScope.launch {
                categoriesViewmodel.getById(transaction.fromId!!).collect { fromCategory ->
                    categoriesViewmodel.getById(transaction.toId!!).collect { toCategory ->
                        (holder as TransferViewHolder).bind(
                            ListItem.TransferItem(
                                transaction,
                                fromCategory,
                                toCategory
                            )
                        )
                    }
                }
            }
    }

    override fun getItemCount() = transactions.size
}