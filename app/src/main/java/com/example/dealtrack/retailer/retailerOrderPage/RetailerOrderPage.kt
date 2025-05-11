package com.example.dealtrack.retailer.retailerOrderPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentRetailerOrderPageBinding
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderAdapter
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass

import com.example.dealtrack.retailer.retailerOrderPage.mvvm.RetailerOrderViewmodel

class RetailerOrderPage : Fragment() {

    private var _binding: FragmentRetailerOrderPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressBar: ProgressBar
    private val viewModel: RetailerOrderViewmodel by viewModels()
    private lateinit var adapter: OrderAdapter

    private var listener: OnRetailerOrderPageClicked? = null

    interface OnRetailerOrderPageClicked {
        fun onRetailerOrderClicked(orderId: String)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRetailerOrderPageClicked) {
            listener = context
        } else {
            throw ClassCastException("$context must implement OnWholesalerLoginClicked interface")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetailerOrderPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.commonProgressBar)
        showLoading(true)
        setupRecyclerView()
        observeData()
        viewModel.fetchOrders()
    }

    private fun setupRecyclerView() {
        adapter = OrderAdapter { order ->
            listener?.onRetailerOrderClicked(order.orderId)

        }

        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewOrders.adapter = adapter
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun observeData() {
        viewModel.orders.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            showLoading(false)
            binding.noOrdersText.isVisible = it.isEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
