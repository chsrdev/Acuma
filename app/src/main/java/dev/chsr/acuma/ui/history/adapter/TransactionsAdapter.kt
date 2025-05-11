package dev.chsr.acuma.ui.history.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import dev.chsr.acuma.R
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

fun LocalDateTime.formatDate(context: Context): String {
    val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
        ?: Locale.getDefault()
    val formatter = DateTimeFormatter.ofPattern("d MMMM", locale)
    return this.format(formatter)
}

fun LocalDateTime.formatTime(context: Context): String {
    val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
        ?: Locale.getDefault()
    val formatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    return this.format(formatter)
}

class TransactionsAdapter(private val owner: HistoryFragment) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<ListItem> = emptyList()

    sealed class ListItem {
        data class DateHeader(val date: String) : ListItem()
        data class DepositItem(val transaction: Transaction, val category: Category) : ListItem()
        data class WithdrawItem(val transaction: Transaction, val category: Category) : ListItem()
        data class TransferItem(
            val transaction: Transaction,
            val fromCategory: Category,
            val toCategory: Category
        ) : ListItem()

        data class RawTransaction(val transaction: Transaction) : ListItem()
    }

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_DEPOSIT = 1
        private const val TYPE_WITHDRAW = 2
        private const val TYPE_TRANSFER = 3
    }

    fun submitList(transactions: List<Transaction>) {
        val sorted = transactions.sortedByDescending { it.date }

        val result = mutableListOf<ListItem>()
        var lastDate: String? = null

        for (tx in sorted) {
            val dateStr = tx.date.toLocalDateTime().formatDate(owner.requireContext())

            if (dateStr != lastDate) {
                result.add(ListItem.DateHeader(dateStr))
                lastDate = dateStr
            }

            result.add(ListItem.RawTransaction(tx))
        }

        items = result
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = items[position]) {
            is ListItem.DateHeader -> TYPE_DATE_HEADER
            is ListItem.RawTransaction -> {
                val tx = item.transaction
                if (tx.fromId == null) TYPE_DEPOSIT
                else if (tx.toId == null) TYPE_WITHDRAW
                else TYPE_TRANSFER
            }

            else -> throw IllegalStateException("Unexpected item: $item")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_DATE_HEADER -> DateHeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_splitter, parent, false)
            )

            TYPE_DEPOSIT -> DepositViewHolder(
                HistoryDepositItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            TYPE_WITHDRAW -> WithdrawViewHolder(
                HistoryWithdrawItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            TYPE_TRANSFER -> TransferViewHolder(
                HistoryTransferItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val categoriesViewmodel = ViewModelProvider(
            owner,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(holder.itemView.context).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]

        when (item) {
            is ListItem.DateHeader -> {
                (holder as DateHeaderViewHolder).bind(item)
            }

            is ListItem.RawTransaction -> {
                val tx = item.transaction

                owner.viewLifecycleOwner.lifecycleScope.launch {
                    if (tx.fromId == null && tx.toId != null) {
                        categoriesViewmodel.getById(tx.toId).collect { category ->
                            (holder as DepositViewHolder).bind(
                                ListItem.DepositItem(tx, category)
                            )
                        }
                    } else if (tx.fromId != null && tx.toId == null) {
                        categoriesViewmodel.getById(tx.fromId).collect { category ->
                            (holder as WithdrawViewHolder).bind(
                                ListItem.WithdrawItem(tx, category)
                            )
                        }
                    } else if (tx.fromId != null && tx.toId != null) {
                        categoriesViewmodel.getById(tx.fromId).collect { fromCategory ->
                                categoriesViewmodel.getById(tx.toId).collect { toCategory ->
                                        (holder as TransferViewHolder).bind(
                                            ListItem.TransferItem(tx, fromCategory, toCategory)
                                        )
                                }
                        }
                    }
                }
            }

            else -> {}
        }
    }

    class DateHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dateText = view.findViewById<TextView>(R.id.date_text)
        fun bind(item: ListItem.DateHeader) {
            dateText.text = item.date
        }
    }

    class DepositViewHolder(val binding: HistoryDepositItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.DepositItem) {
            binding.category.text = item.category.name
            binding.amount.text = "+${item.transaction.amount / 100f}"
            binding.time.text =
                item.transaction.date.toLocalDateTime().formatTime(binding.root.context)
            if (item.transaction.comment != "") {
                binding.comment.text = item.transaction.comment
                binding.line.visibility = View.VISIBLE
                binding.comment.visibility = View.VISIBLE
            } else {
                binding.line.visibility = View.GONE
                binding.comment.visibility = View.GONE
            }
        }
    }

    class WithdrawViewHolder(val binding: HistoryWithdrawItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.WithdrawItem) {
            binding.category.text = item.category.name
            binding.amount.text = "-${item.transaction.amount / 100f}"
            binding.time.text =
                item.transaction.date.toLocalDateTime().formatTime(binding.root.context)
            if (item.transaction.comment != "") {
                binding.comment.text = item.transaction.comment
                binding.line.visibility = View.VISIBLE
                binding.comment.visibility = View.VISIBLE
            } else {
                binding.line.visibility = View.GONE
                binding.comment.visibility = View.GONE
            }
        }
    }

    class TransferViewHolder(val binding: HistoryTransferItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem.TransferItem) {
            binding.fromCategory.text = item.fromCategory.name
            binding.toCategory.text = item.toCategory.name
            binding.amount.text = (item.transaction.amount / 100f).toString()
            binding.time.text =
                item.transaction.date.toLocalDateTime().formatTime(binding.root.context)
            if (item.transaction.comment != "") {
                binding.comment.text = item.transaction.comment
                binding.line.visibility = View.VISIBLE
                binding.comment.visibility = View.VISIBLE
            } else {
                binding.line.visibility = View.GONE
                binding.comment.visibility = View.GONE
            }
        }
    }
}