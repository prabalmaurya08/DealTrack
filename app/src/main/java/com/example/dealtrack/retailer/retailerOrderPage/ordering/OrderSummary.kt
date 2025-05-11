package com.example.dealtrack.retailer.retailerOrderPage.ordering

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentOrderSummaryBinding

import com.example.dealtrack.retailer.retailerOrderPage.mvvm.RetailerOrderViewmodel

class OrderSummary : Fragment() {

    private var _binding: FragmentOrderSummaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RetailerOrderViewmodel by viewModels()
    private lateinit var adapter: OrderSummaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()

        // Observe data
        observeCartItems()
        observeAddress()
        observeOrderStatus()

        // Trigger data fetch
        viewModel.fetchCartItems()
        viewModel.fetchAddress()

        binding.changeAddress.setOnClickListener {
            findNavController().navigate(R.id.action_orderSummary_to_enterAddress)
        }

        setFragmentResultListener("address_updated") { _, _ ->
            viewModel.fetchAddress()
        }

        binding.placeOrderButton.setOnClickListener {
            viewModel.placeOrder()
        }
    }

    private fun setupRecyclerView() {
        adapter = OrderSummaryAdapter(emptyList())
        binding.orderItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@OrderSummary.adapter
        }
    }

    private fun observeCartItems() {
        viewModel.cartItems.observe(viewLifecycleOwner, Observer { cartList ->
            adapter.updateList(cartList)

            val totalItems = cartList.sumOf { it.quantity }
            val totalPrice = cartList.sumOf { it.price * it.quantity }

            binding.totalItemsText.text = "Items: $totalItems"
            binding.totalPriceText.text = "Total: ₹%.2f".format(totalPrice)
        })
    }

    private fun observeAddress() {
        viewModel.address.observe(viewLifecycleOwner, Observer { address ->
            binding.addressDetails.text = address ?: "No address found"
        })
    }

    private fun observeOrderStatus() {
        viewModel.orderPlacedSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        })

        viewModel.orderError.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Toast.makeText(requireContext(), "Failed: $it", Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
