package com.example.dealtrack.retailer.retailerProductPage


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dealtrack.databinding.ProductCatalogDetailCardBinding

import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData

class RetailerProductAdapter(
    private val onItemClick: (ProductData) -> Unit,  // Card tap action
    private val onAddToCart: (ProductData) -> Unit,  // Add to Cart button
    private val onWishlist: (ProductData) -> Unit    // Wishlist button
) : RecyclerView.Adapter<RetailerProductAdapter.ProductViewHolder>() {

    private var productList: List<ProductData> = emptyList()

    inner class ProductViewHolder(private val binding: ProductCatalogDetailCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductData) {
            binding.productName.text = product.name
            binding.costPrice.text = "₹${product.price}"
            binding.mrp.text = "₹${product.mrp}"
            binding.buttonStock.text = "Stock: ${product.stock}"

            // Load product image
            Glide.with(binding.productImage.context)
                .load(product.imageUrl)
                .into(binding.productImage)

            // Click Listeners
            binding.root.setOnClickListener { onItemClick(product) }  // Open Details
            binding.addToCartButton.setOnClickListener { onAddToCart(product) } // Add to Cart
            binding.wishlistButton.setOnClickListener { onWishlist(product) }   // Add to Wishlist
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductCatalogDetailCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount() = productList.size

    // Update list using DiffUtil
    fun updateList(newList: List<ProductData>) {
        val diffResult = DiffUtil.calculateDiff(ProductDiffCallback(productList, newList))
        productList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil Callback
    class ProductDiffCallback(
        private val oldList: List<ProductData>,
        private val newList: List<ProductData>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].productId == newList[newItemPosition].productId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
