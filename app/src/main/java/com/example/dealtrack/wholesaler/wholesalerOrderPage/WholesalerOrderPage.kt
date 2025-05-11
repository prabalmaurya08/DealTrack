package com.example.dealtrack.wholesaler.wholesalerOrderPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.databinding.FragmentWholesalerOrderPageBinding
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass
import com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm.OrderFilter
import com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm.OrderStatusFilter
import com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm.WholesalerOrderAdapter
import com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm.WholesalerOrderViewmodel

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WholesalerOrderPage : Fragment() {

    private var _binding: FragmentWholesalerOrderPageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WholesalerOrderViewmodel by viewModels()
    private lateinit var adapter: WholesalerOrderAdapter

    private var listener: OnWholesalerOrderPageClicked? = null

    interface OnWholesalerOrderPageClicked {
        fun onWholesalerOrderClicked(orderId: String, retailerId: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnWholesalerOrderPageClicked
            ?: throw ClassCastException("$context must implement OnWholesalerOrderPageClicked")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerOrderPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterCards()
        observeOrders()
        observeLoadingState()

        // Initial fetch
        viewModel.fetchOrders()
    }

    override fun onResume() {
        super.onResume()
        // Refresh every time user comes back to this screen
        viewModel.fetchOrders()
    }

    private fun setupRecyclerView() {
        adapter = WholesalerOrderAdapter { orderWithStoreName ->
            val order = orderWithStoreName.first
            listener?.onWholesalerOrderClicked(order.orderId, order.retailerId)
        }

        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ordersRecyclerView.adapter = adapter
    }

    private fun setupFilterCards() {
        val totalCard = binding.cardTotal
        val pendingCard = binding.cardPending
        val deliveredCard = binding.cardDelivered

        totalCard.setOnClickListener {
            viewModel.applyFilter(OrderFilter.ALL)
            highlightSelectedCard(totalCard)
        }

        pendingCard.setOnClickListener {
            viewModel.applyFilter(OrderFilter.PENDING)
            highlightSelectedCard(pendingCard)
        }

        deliveredCard.setOnClickListener {
            viewModel.applyFilter(OrderFilter.DELIVERED)
            highlightSelectedCard(deliveredCard)
        }

        // Default filter
        viewModel.applyFilter(OrderFilter.ALL)
        highlightSelectedCard(totalCard)
    }

    private fun highlightSelectedCard(selected: View) {
        val cards = listOf(binding.cardTotal, binding.cardPending, binding.cardDelivered)
        cards.forEach { card ->
            card.isSelected = card == selected
            card.alpha = if (card == selected) 1.0f else 0.6f
        }
    }

    private fun observeOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.orders.collectLatest { filteredOrders ->
                    adapter.submitList(filteredOrders)

                    binding.apply {
                        val isEmpty = filteredOrders.isEmpty()
                        ordersRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                        noOrdersTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE

                        updateCardCounts(viewModel.orders.value)
                    }
                }
            }
        }
    }

    private fun updateCardCounts(allOrders: List<Pair<OrderDataClass, String>>) {
        val total = allOrders.size
        val delivered = allOrders.count { it.first.orderStatus == "Delivered" }
        val pending = allOrders.count { it.first.orderStatus == "Pending" }

        binding.totalCount.text = total.toString()
        binding.deliveredCount.text = delivered.toString()
        binding.pendingCount.text = pending.toString()
    }

    private fun observeLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collectLatest { loading ->
                    binding.loadingAnimationView.visibility = if (loading) View.VISIBLE else View.GONE

                    // Hide empty message when loading
                    if (loading) {
                        binding.noOrdersTextView.visibility = View.GONE
                        binding.ordersRecyclerView.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
