package com.example.dealtrack.wholesaler.wholesalerOrderPage

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentWholesalerOrderDetailBinding
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.RetailerOrderDetailAdapter
import com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm.WholesalerOrderViewmodel
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WholesalerOrderDetail : Fragment() {

    private var _binding: FragmentWholesalerOrderDetailBinding? = null
    private val binding get() = _binding!!
    private val args: WholesalerOrderDetailArgs by navArgs()
    private val viewModel: WholesalerOrderViewmodel by viewModels()
    private var adapter: RetailerOrderDetailAdapter? = null

    private val orderStatusOptions = listOf("Ordered", "Packed", "Shipped", "Delivered")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWholesalerOrderDetailBinding.inflate(inflater, container, false)
        binding.saveOrderDetail.setOnClickListener {
            val newStatus = binding.statusSpinner.selectedItem.toString()
            updateOrderStatus(newStatus)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val orderId = args.orderId
        val retailerId = args.retailerId


        Log.d("OrderDebug", "Args - orderId: $orderId, retailerId: $retailerId")

        // ✅ Corrected method calls
        viewModel.getOrderById(orderId, retailerId)
        viewModel.fetchRetailerStoreName(retailerId)

        setupRecyclerView()
        setupStatusSpinner()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedOrder.collect { order ->
                        Log.e("OrderObserver", "Order: $order")
                        order?.let { bindOrderData(it) }
                    }
                }

                launch {
                    viewModel.retailerStoreName.collect { name ->
                        binding.retailerNameText.text = "Retailer: ${name ?: "Unknown"}"
                    }
                }
            }
        }

    }

    private fun setupRecyclerView() {
        binding.orderedProductsRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun bindOrderData(order: OrderDataClass) {
        binding.orderCodeText.text = "Order Code: ${order.orderCode}"
        binding.orderDateTimeText.text = "Ordered on: ${order.timestamp.toFormattedString()}"
        binding.orderAmountText.text = "Total: ₹%.2f".format(order.totalAmount)

        adapter = RetailerOrderDetailAdapter(order.products)
        binding.orderedProductsRecycler.adapter = adapter

        setupTimeline(order.orderStatus)
        updateSpinnerSelection(order.orderStatus)
       // binding.addressText.text = order. ?: "No address found"
    }

    private fun setupTimeline(currentStatus: String) {
        binding.statusTimeline.removeAllViews()
        val currentIndex = orderStatusOptions.indexOf(currentStatus).coerceAtLeast(0)

        orderStatusOptions.forEachIndexed { index, status ->
            val stepView = layoutInflater.inflate(R.layout.item_timeliner_status, binding.statusTimeline, false)
            val dot = stepView.findViewById<View>(R.id.dot)
            val line = stepView.findViewById<View>(R.id.line)
            val statusLabel = stepView.findViewById<TextView>(R.id.statusLabel)
            val timestampText = stepView.findViewById<TextView>(R.id.timestamp)

            statusLabel.text = status
            timestampText.text = if (index <= currentIndex) viewModel.selectedOrder.value?.timestamp?.toFormattedString() else ""

            if (index <= currentIndex) {
                dot.setBackgroundResource(R.drawable.timeline_dot_done)
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
            } else {
                dot.setBackgroundResource(R.drawable.timeline_dot)
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
            }

            if (index == 0) line.visibility = View.INVISIBLE

            binding.statusTimeline.addView(stepView)
        }
    }

    private fun setupStatusSpinner() {
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            orderStatusOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.statusSpinner.adapter = spinnerAdapter

        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStatus = orderStatusOptions[position]
                val currentStatus = viewModel.selectedOrder.value?.orderStatus ?: return

                if (selectedStatus != currentStatus) {
                    updateOrderStatus(selectedStatus)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateSpinnerSelection(currentStatus: String) {
        val index = orderStatusOptions.indexOf(currentStatus)
        if (index >= 0) {
            binding.statusSpinner.setSelection(index)
        }
    }

    private fun updateOrderStatus(newStatus: String) {
        val order = viewModel.selectedOrder.value ?: return
        // ✅ Updated call to include retailerId
        viewModel.updateOrderStatusForBoth(order.orderId, order.retailerId, newStatus)
    }

    private fun Timestamp.toFormattedString(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(this.toDate())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }
}

