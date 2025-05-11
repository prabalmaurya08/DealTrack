package com.example.dealtrack.retailer.retailerWishList.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData

class WishlistViewModel : ViewModel() {

    private val repository = WishlistRepository()

    private val _wishlistItems = MutableLiveData<List<WishListDataClass>>()
    val wishlistItems: LiveData<List<WishListDataClass>> = _wishlistItems

    private val _wishlistError = MutableLiveData<String>()
    val wishlistError: LiveData<String> = _wishlistError

    init {
        loadWishlistItems()
    }

    fun loadWishlistItems() {
        repository.getWishlistItems(
            onResult = { items -> _wishlistItems.value = items },
            onError = { e -> _wishlistError.value = e.message ?: "Failed to load wishlist" }
        )
    }

    fun addToWishlist(product: ProductData) {
        repository.addToWishlist(product) { success ->
            if (!success) _wishlistError.value = "Failed to add item to wishlist"
        }
    }

    fun removeItem(productId: String) {
        repository.removeFromWishlist(productId) { success ->
            if (!success) _wishlistError.value = "Failed to remove item from wishlist"
        }
    }
}
