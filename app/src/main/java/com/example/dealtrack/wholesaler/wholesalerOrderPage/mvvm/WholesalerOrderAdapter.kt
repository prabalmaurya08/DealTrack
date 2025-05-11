package com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dealtrack.R
import com.example.dealtrack.databinding.ItemWholesalerOrderBinding
import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass

class WholesalerOrderAdapter(
    private val onOrderClicked: (Pair<OrderDataClass, String>) -> Unit
) : ListAdapter<Pair<OrderDataClass, String>, WholesalerOrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemWholesalerOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val (order, storeName) = getItem(position) // Unwrap Pair
        holder.bind(order, storeName)
    }

    inner class OrderViewHolder(private val binding: ItemWholesalerOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderDataClass, storeName: String) {
            binding.orderCodeText.text = order.orderCode
            binding.orderTotalText.text = "Total: ₹${order.totalAmount}"
            binding.orderDateText.text = formatDate(order.timestamp.seconds)
            binding.orderStatusText.text = order.orderStatus
            binding.retailerNameText.text = "Retailer: $storeName" // Use store name instead of retailerId

            // Color status text dynamically
            when (order.orderStatus) {
                "Pending" -> binding.orderStatusText.setTextColor(itemView.context.getColor(R.color.Pending))
                "Delivered" -> binding.orderStatusText.setTextColor(itemView.context.getColor(R.color.Delivered_Order))
                else -> binding.orderStatusText.setTextColor(itemView.context.getColor(R.color.Black3))
            }

            binding.root.setOnClickListener {
                onOrderClicked(Pair(order, storeName)) // Pass the pair to the click listener
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(timestamp))
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Pair<OrderDataClass, String>>() {
        override fun areItemsTheSame(
            oldItem: Pair<OrderDataClass, String>,
            newItem: Pair<OrderDataClass, String>
        ): Boolean {
            return oldItem.first.orderId == newItem.first.orderId
        }

        override fun areContentsTheSame(
            oldItem: Pair<OrderDataClass, String>,
            newItem: Pair<OrderDataClass, String>
        ): Boolean {
            val oldOrder = oldItem.first
            val newOrder = newItem.first

            return oldOrder.orderStatus == newOrder.orderStatus &&
                    oldOrder.totalAmount == newOrder.totalAmount &&
                    oldOrder.timestamp == newOrder.timestamp &&
                    oldOrder.orderCode == newOrder.orderCode &&
                    oldItem.second == newItem.second
        }
    }

}
