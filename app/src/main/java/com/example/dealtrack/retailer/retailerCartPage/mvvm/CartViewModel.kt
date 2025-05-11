package com.example.dealtrack.retailer.retailerCartPage.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData

class CartViewModel : ViewModel() {

    private val repository = CartRepository()

    private val _cartItems = MutableLiveData<List<CartDataClass>>()
    val cartItems: LiveData<List<CartDataClass>> = _cartItems

    private val _cartError = MutableLiveData<String>()
    val cartError: LiveData<String> = _cartError

    init {
        loadCartItems()
    }

    fun loadCartItems() {
        repository.getCartItems(
            onResult = { items -> _cartItems.value = items },
            onError = { e -> _cartError.value = e.message ?: "Failed to load cart" }
        )
    }

    fun addToCart(product: ProductData, quantity: Int = 1) {
        repository.addToCart(product, quantity) { success ->
            if (!success) _cartError.value = "Failed to add item to cart"
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        repository.updateQuantity(productId, quantity) { success ->
            if (!success) _cartError.value = "Failed to update quantity"
        }
    }

    fun removeItem(productId: String) {
        repository.removeFromCart(productId) { success ->
            if (!success) _cartError.value = "Failed to remove item from cart"
        }
    }
}
