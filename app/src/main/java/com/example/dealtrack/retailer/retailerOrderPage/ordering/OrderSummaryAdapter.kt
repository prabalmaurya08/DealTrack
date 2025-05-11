package com.example.dealtrack.retailer.retailerOrderPage.ordering

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dealtrack.databinding.ItemOrderCartBinding

import com.example.dealtrack.retailer.retailerCartPage.mvvm.CartDataClass

class OrderSummaryAdapter(
    private var items: List<CartDataClass>
) : RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder>() {

    inner class OrderSummaryViewHolder(private val binding: ItemOrderCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartDataClass) {
            binding.textProductName.text = item.productName
            binding.textQuantity.text = "Qty: ${item.quantity}"
            binding.textPrice.text = "₹${item.price * item.quantity}"

            Glide.with(binding.imageProduct.context)
                .load(item.imageUrl)
                .placeholder(com.example.dealtrack.R.drawable.app_icon)
                .into(binding.imageProduct)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSummaryViewHolder {
        val binding = ItemOrderCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderSummaryViewHolder(binding)
    }
    fun updateList(newList: List<CartDataClass>) {
        items = newList
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: OrderSummaryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
