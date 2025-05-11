package com.example.dealtrack.wholesaler.wholesalerProductPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.databinding.FragmentWholesalerProductPageBinding
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductAdapter
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductViewModel
import com.google.firebase.auth.FirebaseAuth

class WholesalerProductPage : Fragment() {
    private var _binding: FragmentWholesalerProductPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    private var listener: OnWholesalerProductPageClicked? = null

    interface OnWholesalerProductPageClicked {
        fun onWholesalerAddProductClicked()
        fun onEditProductClicked(productID: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnWholesalerProductPageClicked
            ?: throw ClassCastException("$context must implement OnWholesalerProductPageClicked interface")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerProductPageBinding.inflate(inflater, container, false)
        binding.addProductBtn.setOnClickListener {
            listener?.onWholesalerAddProductClicked()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wholesalerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        setupRecyclerView()
        observeViewModel()
        viewModel.fetchProducts(wholesalerId)
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onEditClick = { product ->
                listener?.onEditProductClicked(product.productId)
            },
            onDeleteClick = { product ->
                viewModel.deleteProductById(product.productId)
            }
        )
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                val products = viewModel.products.value
                if (products.isNullOrEmpty()) {
                    showEmpty()
                } else {
                    showProducts(products)
                }
            }
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (viewModel.isLoading.value == true) return@observe
            if (products.isNullOrEmpty()) {
                showEmpty()
            } else {
                showProducts(products)
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                val wholesalerId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                viewModel.fetchProducts(wholesalerId)
            } else if (success == false) {
                Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        binding.progressBarLottie.visibility = View.VISIBLE
        binding.recyclerViewProducts.visibility = View.GONE
        binding.textNoProducts.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.progressBarLottie.visibility = View.GONE
        binding.recyclerViewProducts.visibility = View.GONE
        binding.textNoProducts.visibility = View.VISIBLE
    }

    private fun showProducts(products: List<ProductData>) {
        adapter.submitList(products)
        binding.progressBarLottie.visibility = View.GONE
        binding.recyclerViewProducts.visibility = View.VISIBLE
        binding.textNoProducts.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
