package dev.chsr.acuma.ui.categories.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.chsr.acuma.R
import dev.chsr.acuma.databinding.CategoryItemBinding
import dev.chsr.acuma.entity.Category

class CategoriesAdapter :
        RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    class ViewHolder(val binding: CategoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    private var categories: List<Category> = emptyList()
    fun submitList(newList: List<Category>) {
        categories = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.categoryName.text = category.name
        holder.binding.categoryBalance.text = category.balance.toString()
        if (category.goal != null) {
            val balanceText = category.balance.toString() + "/" + category.goal
            holder.binding.categoryBalance.text = getSpannableBalanceWithGoal(holder.binding.root.context, balanceText)
            holder.binding.categoryGoalProcess.visibility = View.VISIBLE
            holder.binding.categoryGoalProcess.progress = 100*category.balance / category.goal
        }
    }

    private fun getSpannableBalanceWithGoal(context: Context, balanceText: String): Spannable {
        val spanBalanceText = SpannableString(balanceText)
        spanBalanceText.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            balanceText.indexOf("/"),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBalanceText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.primary)),
            0,
            balanceText.indexOf("/"),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBalanceText.setSpan(
            StyleSpan(Typeface.NORMAL),
            balanceText.indexOf("/"),
            balanceText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spanBalanceText.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.category_item_goal_text_color)),
            balanceText.indexOf("/"),
            balanceText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spanBalanceText
    }

    override fun getItemCount() = categories.size
}