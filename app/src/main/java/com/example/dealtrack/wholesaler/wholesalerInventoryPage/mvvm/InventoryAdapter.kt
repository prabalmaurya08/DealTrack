package com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dealtrack.R
import com.example.dealtrack.databinding.ItemInventoryBinding

class InventoryAdapter(
    private val onClick: (InventoryDisplayItem) -> Unit
) : ListAdapter<InventoryDisplayItem, InventoryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventoryDisplayItem) {
            binding.productName.text = item.productName
            binding.productCode.text = "Code: ${item.productCode}"
            binding.expiryDate.text = "Expiry: ${item.expiryDate}"
            binding.manufacturedDate.text = "MFD: ${item.manufacturedDate}"
            binding.stock.text = "Stock: ${item.stock}"
            Glide.with(binding.productImage.context)
                .load(item.imageUrl)
                .into(binding.productImage)

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        // Apply fade-in animation
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_in)
        holder.itemView.startAnimation(animation)
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<InventoryDisplayItem>() {
        override fun areItemsTheSame(oldItem: InventoryDisplayItem, newItem: InventoryDisplayItem) =
            oldItem.productId == newItem.productId

        override fun areContentsTheSame(oldItem: InventoryDisplayItem, newItem: InventoryDisplayItem) =
            oldItem == newItem
    }
}
