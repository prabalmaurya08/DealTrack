package com.example.dealtrack.retailer.retailerCartPage.mvvm



import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dealtrack.R


class RetailerCartAdapter(
    private var items: MutableList<CartDataClass>,
    private val onQuantityChanged: (CartDataClass) -> Unit,
    private val onItemRemoved: (CartDataClass) -> Unit
) : RecyclerView.Adapter<RetailerCartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageProduct)
        val productName: TextView = itemView.findViewById(R.id.textProductName)
        val productUnit: TextView = itemView.findViewById(R.id.textUnit)
        val productPrice: TextView = itemView.findViewById(R.id.textPrice)
        val quantityText: TextView = itemView.findViewById(R.id.textQuantity)
        val increaseButton: ImageButton = itemView.findViewById(R.id.buttonIncrease)
        val decreaseButton: ImageButton = itemView.findViewById(R.id.buttonDecrease)
        val removeButton: ImageButton = itemView.findViewById(R.id.buttonRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.productName.text = item.productName
        holder.productUnit.text = item.quantity.toString()
        holder.productPrice.text = "₹${item.price * item.quantity}"
        holder.quantityText.text = item.quantity.toString()

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.productImage)

        holder.increaseButton.setOnClickListener {
            item.quantity++
            notifyItemChanged(position)
            onQuantityChanged(item)
        }

        holder.decreaseButton.setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                notifyItemChanged(position)
                onQuantityChanged(item)
            }
        }

        holder.removeButton.setOnClickListener {
            val removedItem = items.removeAt(position)
            notifyItemRemoved(position)
            onItemRemoved(removedItem)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateCart(newList: List<CartDataClass>) {
        items = newList.toMutableList()
        notifyDataSetChanged()
    }
}
