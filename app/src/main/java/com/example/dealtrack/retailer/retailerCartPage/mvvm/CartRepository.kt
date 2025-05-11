package com.example.dealtrack.retailer.retailerCartPage.mvvm

import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class CartRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getCartCollection(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
        return firestore.collection("retailers").document(uid).collection("cart")
    }

    fun getCartItems(onResult: (List<CartDataClass>) -> Unit, onError: (Exception) -> Unit) {
        getCartCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(CartDataClass::class.java) ?: emptyList()
                onResult(items)
            }
    }

    fun addToCart(product: ProductData, quantity: Int = 1, onComplete: (Boolean) -> Unit) {
        val cartItem = CartDataClass(
            productId = product.productId,
            productName = product.name,
            imageUrl = product.imageUrl,
            price = product.price,
            quantity = quantity,
            wholesalerId = product.wholesalerId
        )
        getCartCollection().document(product.productId).set(cartItem)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateQuantity(productId: String, quantity: Int, onComplete: (Boolean) -> Unit) {
        getCartCollection().document(productId)
            .update("quantity", quantity)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeFromCart(productId: String, onComplete: (Boolean) -> Unit) {
        getCartCollection().document(productId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
