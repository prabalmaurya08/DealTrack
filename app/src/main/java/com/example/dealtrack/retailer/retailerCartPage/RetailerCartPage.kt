package com.example.dealtrack.retailer.retailerCartPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.databinding.FragmentRetailerCartPageBinding
import com.example.dealtrack.retailer.retailerCartPage.mvvm.CartDataClass
import com.example.dealtrack.retailer.retailerCartPage.mvvm.CartViewModel
import com.example.dealtrack.retailer.retailerCartPage.mvvm.RetailerCartAdapter

class RetailerCartPage : Fragment() {

    private var _binding: FragmentRetailerCartPageBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: RetailerCartAdapter



    private var listener: OnRetailerCartPageClicked? = null

    interface OnRetailerCartPageClicked {
        fun onRetailerCartPageCheckoutClicked()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRetailerCartPageClicked) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnWholesalerLoginClicked interface")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerCartPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[CartViewModel::class.java]
        setupRecyclerView()
        observeCartItems()

        binding.buttonCheckout.setOnClickListener {
            Toast.makeText(requireContext(), "Proceed to checkout clicked", Toast.LENGTH_SHORT).show()
            // Navigate to checkout or place order screen
            listener?.onRetailerCartPageCheckoutClicked()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = RetailerCartAdapter(
            items = mutableListOf(),
            onQuantityChanged = { item ->
                viewModel.updateQuantity(item.productId, item.quantity)
            },
            onItemRemoved = { item ->
                viewModel.removeItem(item.productId)
            }
        )
        binding.recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun observeCartItems() {
        viewModel.cartItems.observe(viewLifecycleOwner) { cartList ->
            cartAdapter.updateCart(cartList)

            val isCartEmpty = cartList.isEmpty()

            binding.recyclerViewCart.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
            binding.cartSummaryCard.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
            binding.emptyCartLayout.visibility = if (isCartEmpty) View.VISIBLE else View.GONE

            if (!isCartEmpty) {
                val totalItems = cartList.sumOf { it.quantity }
                val totalPrice = cartList.sumOf { it.quantity * it.price }

                binding.textTotalItems.text = "Total items: $totalItems"
                binding.textTotalPrice.text = "Total: ₹$totalPrice"
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
