package com.example.dealtrack.retailer.retailerOrderPage

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dealtrack.R
import com.example.dealtrack.databinding.FragmentRetailerOrderDetailBinding
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.RetailerOrderDetailAdapter
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.RetailerOrderViewmodel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class RetailerOrderDetail : Fragment() {

    private var _binding: FragmentRetailerOrderDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RetailerOrderDetailArgs by navArgs()
    private val viewModel: RetailerOrderViewmodel by viewModels()
    private var adapter: RetailerOrderDetailAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRetailerOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val orderId = args.orderId
        val retailerId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Start real-time listener
        viewModel.listenToOrderRealtime(retailerId, orderId)

        setupRecyclerView()
        viewModel.fetchAddress()

        viewModel.order.observe(viewLifecycleOwner) { order ->
            order?.let { bindOrderData(it) }
        }

        viewModel.address.observe(viewLifecycleOwner, Observer { address ->
            binding.addressText.text = address ?: "No address found"
        })
    }

    private fun setupRecyclerView() {
        binding.orderedProductsRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun bindOrderData(order: OrderDataClass) {
        binding.orderCodeText.text = order.orderCode
        binding.orderDateTimeText.text = order.timestamp.toFormattedString()
        binding.orderAmountText.text = "Total: ₹%.2f".format(order.totalAmount)

        if (adapter == null) {
            adapter = RetailerOrderDetailAdapter(order.products)
            binding.orderedProductsRecycler.adapter = adapter
        } else {
            adapter?.updateList(order.products)
        }

        setupTimeline(order.orderStatus)
    }

    private fun setupTimeline(currentStatus: String) {
        binding.statusTimeline.removeAllViews()

        val statusSteps = listOf("Ordered", "Packed", "Shipped", "Delivered")
        val currentIndex = statusSteps.indexOf(currentStatus).coerceAtLeast(0)

        statusSteps.forEachIndexed { index, status ->
            val stepView = layoutInflater.inflate(R.layout.item_timeliner_status, binding.statusTimeline, false)

            val dot = stepView.findViewById<View>(R.id.dot)
            val line = stepView.findViewById<View>(R.id.line)
            val statusLabel = stepView.findViewById<TextView>(R.id.statusLabel)
            val timestampText = stepView.findViewById<TextView>(R.id.timestamp)

            statusLabel.text = status
            timestampText.text = if (index <= currentIndex) {
                viewModel.order.value?.timestamp?.toFormattedString()
            } else {
                ""
            }

            // Highlight completed & current steps
            if (index <= currentIndex) {
                dot.setBackgroundResource(R.drawable.timeline_dot_done)
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
            } else {
                dot.setBackgroundResource(R.drawable.timeline_dot)
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
            }

            // Hide top line for the first item
            if (index == 0) {
                line.visibility = View.INVISIBLE
            }

            binding.statusTimeline.addView(stepView)
        }
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
