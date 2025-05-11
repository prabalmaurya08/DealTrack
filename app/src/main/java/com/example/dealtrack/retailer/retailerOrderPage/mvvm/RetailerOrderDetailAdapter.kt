package com.example.dealtrack.retailer.retailerOrderPage.mvvm



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dealtrack.databinding.ItemOrderCartBinding



class RetailerOrderDetailAdapter(private var productList: List<OrderProduct>) :
    RecyclerView.Adapter<RetailerOrderDetailAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(private val binding: ItemOrderCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: OrderProduct) {
            binding.textProductName.text = product.productName
            binding.textQuantity.text = "Qty: ${product.quantity}"
            binding.textPrice.text = "₹${product.price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemOrderCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    // ✅ Add this method to update list and refresh
    fun updateList(newList: List<OrderProduct>) {
        productList = newList
        notifyDataSetChanged()
    }
}
