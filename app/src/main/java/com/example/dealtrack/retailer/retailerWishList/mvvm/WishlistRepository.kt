package com.example.dealtrack.retailer.retailerWishList.mvvm

import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class WishlistRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getWishlistCollection(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
        return firestore.collection("retailers").document(uid).collection("wishlist")
    }

    fun getWishlistItems(
        onResult: (List<WishListDataClass>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        getWishlistCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(WishListDataClass::class.java) ?: emptyList()
                onResult(items)
            }
    }

    fun addToWishlist(product: ProductData, onComplete: (Boolean) -> Unit) {
        val wishlistItem = WishListDataClass(
            productId = product.productId,
            productName = product.name,
            imageUrl = product.imageUrl,
            price = product.price,
            wholesalerId = product.wholesalerId
        )
        getWishlistCollection().document(product.productId)
            .set(wishlistItem)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeFromWishlist(productId: String, onComplete: (Boolean) -> Unit) {
        getWishlistCollection().document(productId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
