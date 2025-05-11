package com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dealtrack.databinding.WholesalerProductCardBinding


class ProductAdapter(
    private val onEditClick: (ProductData) -> Unit,
    private val onDeleteClick: (ProductData) -> Unit
) : ListAdapter<ProductData, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    class ProductViewHolder(val binding: WholesalerProductCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductData) {
            binding.productName.text = product.name
            binding.productCode.text = "Code: ${product.productCode}"
            binding.stock.text = "Stock: ${product.stock}"
            binding.manufactureDate.text = "Manufactured: ${product.manufacturerDate}"
            binding.expiryDate.text = "Expiry: ${product.expiryDate}"

            Glide.with(binding.productImage.context)
                .load(product.imageUrl)
                .into(binding.productImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = WholesalerProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)

        holder.binding.btnEdit.setOnClickListener { onEditClick(product) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick(product) }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<ProductData>() {
    override fun areItemsTheSame(oldItem: ProductData, newItem: ProductData): Boolean {
        return oldItem.productId == newItem.productId
    }

    override fun areContentsTheSame(oldItem: ProductData, newItem: ProductData): Boolean {
        return oldItem == newItem
    }
}