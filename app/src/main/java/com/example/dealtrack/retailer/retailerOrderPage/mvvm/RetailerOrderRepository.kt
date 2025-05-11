package com.example.dealtrack.retailer.retailerOrderPage.mvvm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dealtrack.retailer.retailerCartPage.mvvm.CartDataClass
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class RetailerOrderRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String
        get() = auth.currentUser?.uid ?: throw Exception("User not logged in")

    private val cartCollection
        get() = firestore.collection("retailers").document(userId).collection("cart")

    private val retailerOrdersCollection
        get() = firestore.collection("retailers").document(userId).collection("orders")

    private val globalOrdersCollection
        get() = firestore.collection("orders")

    private val productsCollection
        get() = firestore.collection("products")

    // Step 1: Fetch all cart items
    fun placeOrder(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        cartCollection.get()
            .addOnSuccessListener { snapshot ->
                val cartItems = snapshot.toObjects(CartDataClass::class.java)

                if (cartItems.isEmpty()) {
                    onFailure(Exception("Cart is empty"))
                    return@addOnSuccessListener
                }

                val orderId = UUID.randomUUID().toString()
                val wholesalerId = cartItems[0].wholesalerId // Assuming the same wholesaler for all items
                val totalAmount = cartItems.sumOf { it.price * it.quantity }

                val orderProducts = cartItems.map {
                    OrderProduct(
                        productId = it.productId,
                        productName = it.productName,
                        quantity = it.quantity,
                        price = it.price
                    )
                }

                val order = OrderDataClass(
                    orderId = orderId,
                    orderCode = "#"+generateRandomOrderCode(),
                    retailerId = userId,
                    wholesalerId = wholesalerId,
                    products = orderProducts,
                    totalAmount = totalAmount,
                    orderStatus = "Pending",
                    timestamp = Timestamp.now()
                )

                // Step 1: Check stock availability for each product in the cart
                val stockCheckTasks = cartItems.map { item ->
                    val productRef = productsCollection.document(item.productId)
                    productRef.get()
                        .addOnSuccessListener { productSnapshot ->
                            val currentStock = productSnapshot.getLong("stock") ?: 0
                            if (currentStock < item.quantity) {
                                // If stock is insufficient, notify the user and stop order processing
                                onFailure(Exception("Insufficient stock for ${item.productName}"))
                            }
                        }
                }

                // Wait for stock check results (you might want to handle this using coroutines or other async logic)

                // After confirming stock availability, proceed with order placement
                val batch = firestore.batch()
                val retailerOrderRef = retailerOrdersCollection.document(orderId)
                val globalOrderRef = globalOrdersCollection.document(orderId)
                batch.set(retailerOrderRef, order)
                batch.set(globalOrderRef, order)

                // Step 2: Decrease stock in both products and inventory collections if everything is ok
                for (item in cartItems) {
                    // Decrease stock in the products collection
                    val productRef = productsCollection.document(item.productId)
                    batch.update(productRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))

                    // Decrease stock in the inventory collection
                    val inventoryQueryRef = firestore.collection("inventory")
                        .whereEqualTo("productId", item.productId)
                        .whereEqualTo("wholesalerId", wholesalerId)
                        .limit(1)

                    inventoryQueryRef.get().addOnSuccessListener { inventorySnapshot ->
                        if (!inventorySnapshot.isEmpty) {
                            val inventoryDoc = inventorySnapshot.documents[0]
                            val currentStock = inventoryDoc.getLong("stock")?.toInt() ?: 0
                            val newStock = currentStock - item.quantity

                            // Update the inventory document with the new stock value
                            firestore.collection("inventory")
                                .document(inventoryDoc.id)
                                .update("stock", newStock)
                        }
                    }
                }

                // Step 3: Clear cart
                for (item in cartItems) {
                    val cartItemRef = cartCollection.document(item.productId)
                    batch.delete(cartItemRef)
                }

                // Step 4: Commit everything
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }

            }
            .addOnFailureListener { onFailure(it) }
    }


    fun generateRandomOrderCode(length: Int = 5): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun getCartItems(
        onSuccess: (List<CartDataClass>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        cartCollection.get()
            .addOnSuccessListener {
                val items = it.toObjects(CartDataClass::class.java)
                onSuccess(items)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getAddress(
        onSuccess: (String?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("retailers").document(userId).collection("address")
            .document("default")
            .get()
            .addOnSuccessListener { document ->
                val addressLine = document.getString("addressLine") ?: ""
                val city = document.getString("city") ?: ""
                val zip = document.getString("zipCode") ?: ""

                // Combine them into a full address string
                val fullAddress = "$addressLine, $city - $zip".trim().removeSuffix(", -")

                // If all fields are empty, return null
                if (fullAddress.isBlank() || fullAddress == ", -") {
                    onSuccess(null)
                } else {
                    onSuccess(fullAddress)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }


    //for order section
    fun getRetailerOrders(
        onSuccess: (List<OrderDataClass>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("retailers").document(userId)
            .collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.toObjects(OrderDataClass::class.java)
                onSuccess(orders)
            }
            .addOnFailureListener { onFailure(it) }
    }



    //for order details screen


    suspend fun getOrderById(orderId: String): OrderDataClass? = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: return@withContext null
            val snapshot = firestore.collection("retailers")
                .document(uid)
                .collection("orders")
                .document(orderId)
                .get()
                .await()

            return@withContext snapshot.toObject(OrderDataClass::class.java)
        } catch (e: Exception) {
            Log.e("OrderRepo", "Failed to fetch order by id", e)
            null
        }
    }


    suspend fun updateOrderStatusForBoth(orderId: String, newStatus: String): Result<Unit> {
        return try {
            val globalRef = firestore.collection("orders").document(orderId)
            val snapshot = globalRef.get().await()
            val retailerId = snapshot.getString("retailerId") ?: return Result.failure(Exception("No retailerId found"))

            val retailerRef = firestore.collection("retailers")
                .document(retailerId)
                .collection("orders")
                .document(orderId)

            val updateMap = mutableMapOf<String, Any>(
                "orderStatus" to newStatus
            )

            val timestamp = Timestamp.now()
            when (newStatus) {
                "Packed" -> updateMap["packedAt"] = timestamp
                "Shipped" -> updateMap["shippedAt"] = timestamp
                "Delivered" -> updateMap["deliveredAt"] = timestamp
            }

            val batch = firestore.batch()
            batch.update(globalRef, updateMap)
            batch.update(retailerRef, updateMap)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // RetailerOrderRepository.kt
    fun listenToOrderRealtime(retailerId: String, orderId: String): LiveData<OrderDataClass?> {
        val liveData = MutableLiveData<OrderDataClass?>()

        val retailerOrderRef = firestore.collection("retailers")
            .document(retailerId)
            .collection("orders")
            .document(orderId)

        val globalOrderRef = firestore.collection("orders")
            .document(orderId)

        // Listen to retailer path
        retailerOrderRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                liveData.value = null
                return@addSnapshotListener
            }
            liveData.value = snapshot.toObject(OrderDataClass::class.java)
        }

        // Optional: also listen to global path if you want redundancy
        globalOrderRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }
            // You could compare timestamps and update if needed
            // liveData.value = snapshot.toObject(OrderDataClass::class.java)
        }

        return liveData
    }








}
