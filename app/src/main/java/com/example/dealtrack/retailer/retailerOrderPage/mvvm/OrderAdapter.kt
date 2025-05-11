package com.example.dealtrack.retailer.retailerOrderPage.mvvm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dealtrack.databinding.OrderDetailCardBinding
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private val onClick: (OrderDataClass) -> Unit
) : ListAdapter<OrderDataClass, OrderAdapter.OrderViewHolder>(DiffCallback()) {

    class OrderViewHolder(val binding: OrderDetailCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = OrderDetailCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        with(holder.binding) {
            orderCode.text = order.orderCode
            orderDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(order.timestamp.toDate())
            orderTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(order.timestamp.toDate())
            orderStatus.text = order.orderStatus
            totalAmount.text = "₹${order.totalAmount}"

            root.setOnClickListener { onClick(order) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<OrderDataClass>() {
        override fun areItemsTheSame(old: OrderDataClass, new: OrderDataClass) = old.orderId == new.orderId
        override fun areContentsTheSame(old: OrderDataClass, new: OrderDataClass) = old == new
    }
}
