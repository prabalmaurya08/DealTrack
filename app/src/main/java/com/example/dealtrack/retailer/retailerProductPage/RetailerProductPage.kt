package com.example.dealtrack.retailer.retailerProductPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar


import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentRetailerProductPageBinding
import com.example.dealtrack.retailer.retailerCartPage.mvvm.CartViewModel
import com.example.dealtrack.retailer.retailerWishList.mvvm.WishlistViewModel

import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductViewModel

class RetailerProductPage : Fragment() {

    private var _binding: FragmentRetailerProductPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressBar: ProgressBar

    private lateinit var cartViewModel: CartViewModel
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: RetailerProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerProductPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.commonProgressBar)
        cartViewModel = ViewModelProvider(this)[CartViewModel::class.java]
        wishlistViewModel = ViewModelProvider(this)[WishlistViewModel::class.java]
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        setupRecyclerView()
        showLoading(true)
        viewModel.fetchProductsForRetailer()

        viewModel.products.observe(viewLifecycleOwner) { productList ->
            adapter.updateList(productList)
            showLoading(false)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = RetailerProductAdapter(
            onItemClick = { product ->
                // Navigate to product detail page with product info
            },
            onAddToCart = { product ->
                if (product.stock > 0) {
                    cartViewModel.addToCart(product, quantity = 1)
                    Toast.makeText(requireContext(), "Added to Cart: ${product.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Sorry, this product is out of stock.", Toast.LENGTH_SHORT).show()
                }
            },
            onWishlist = { product ->
                wishlistViewModel.addToWishlist(product)
                Toast.makeText(requireContext(), "Added to Wishlist: ${product.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

