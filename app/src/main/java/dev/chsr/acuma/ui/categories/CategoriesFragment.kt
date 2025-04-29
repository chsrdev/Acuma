package dev.chsr.acuma.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dev.chsr.acuma.database.AppDatabase
import dev.chsr.acuma.databinding.FragmentCategoriesBinding
import dev.chsr.acuma.repository.CategoryRepository
import dev.chsr.acuma.ui.categories.adapter.CategoriesAdapter
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        val categoriesViewmodel = ViewModelProvider(
            this,
            CategoriesViewModelFactory(
                CategoryRepository(
                    AppDatabase.getInstance(requireContext()).categoryDao()
                )
            )
        )[CategoriesViewModel::class.java]
        val root: View = binding.root
        val createCategoryButton = binding.createCategoryBtn

        createCategoryButton.setOnClickListener {
            val createCategoryBottomSheet = CreateCategoryBottomSheetFragment()
            createCategoryBottomSheet.show(parentFragmentManager, "createCategoryBottomSheet")
        }

        val categoriesAdapter = CategoriesAdapter()
        binding.categoriesList.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesList.adapter = categoriesAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            categoriesViewmodel.categories.collect { list ->
                Log.d("categories", list.toString())
                categoriesAdapter.submitList(list)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}