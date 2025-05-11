package com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm

import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass
import com.example.dealtrack.wholesaler.wholesalerHomePage.OrderStatusCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WholesalerOrderRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val wholesalerId = auth.currentUser?.uid ?: ""

    suspend fun getWholesalerOrdersWithRetailerNames(): List<Pair<OrderDataClass, String>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("wholesalerId", wholesalerId)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(OrderDataClass::class.java)?.copy(orderId = doc.id)
            }

            orders.map { order ->
                val storeName = getRetailerStoreName(order.retailerId)
                Pair(order, storeName)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun getInviteCode(): String{
        return try {
            val snapshot = firestore.collection("wholesalers")
                .document(wholesalerId)
                .get()
                .await()
            snapshot.getString("inviteCode") ?: "Unknown Store"
        } catch (e: Exception) {
            "Unknown Store"
        }


    }

    suspend fun getRetailerStoreName(retailerId: String): String {
        return try {
            val snapshot = firestore.collection("retailers")
                .document(retailerId)
                .get()
                .await()
            snapshot.getString("storeName") ?: "Unknown Store"
        } catch (e: Exception) {
            "Unknown Store"
        }
    }

    suspend fun updateOrderStatus(orderId: String, retailerId: String, newStatus: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val globalOrderRef = firestore.collection("orders").document(orderId)
            val retailerOrderRef = firestore.collection("retailers")
                .document(retailerId)
                .collection("orders")
                .document(orderId)

            batch.update(globalOrderRef, "orderStatus", newStatus)
            batch.update(retailerOrderRef, "orderStatus", newStatus)

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    //for homecsreen
    suspend fun getOrderStats(wholesalerId: String): Map<OrderStatusCategory, Int> {
        val snapshot = FirebaseFirestore.getInstance()
            .collection("orders")
            .whereEqualTo("wholesalerId", wholesalerId)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.getString("orderStatus")
        }.groupingBy {
            OrderStatusCategory.valueOf(it.uppercase())
        }.eachCount()
    }

}


//class WholesalerOrderRepository {
//
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//    private val wholesalerId = auth.currentUser?.uid ?: ""
//
//    fun getOrdersByStatus(filter: OrderStatusFilter): Flow<List<OrderDataClass>> = callbackFlow {
//        var query: Query = firestore.collection("orders")
//            .whereEqualTo("wholesalerId", wholesalerId)
//
//        when (filter) {
//            OrderStatusFilter.PENDING -> {
//                query = query.whereNotEqualTo("orderStatus", "Delivered")
//            }
//            OrderStatusFilter.DELIVERED -> {
//                query = query.whereEqualTo("orderStatus", "Delivered")
//            }
//            OrderStatusFilter.ALL -> {
//                // No extra filtering
//            }
//        }
//
//        val listener = query.addSnapshotListener { snapshot, error ->
//            if (error != null) {
//                close(error)
//                return@addSnapshotListener
//            }
//
//            val orders = snapshot?.documents?.mapNotNull { doc ->
//                doc.toObject(OrderDataClass::class.java)?.copy(orderId = doc.id)
//            } ?: emptyList()
//
//            trySend(orders)
//        }
//
//        awaitClose { listener.remove() }
//    }
//    suspend fun getRetailerStoreName(retailerId: String): String {
//        return try {
//            val snapshot = firestore.collection("retailers")
//                .document(retailerId)
//                .get()
//                .await()
//            snapshot.getString("storeName") ?: "Unknown Store"
//        } catch (e: Exception) {
//            "Unknown Store"
//        }
//    }
//
//
//    suspend fun updateOrderStatus(orderId: String, retailerId: String, newStatus: String): Result<Unit> {
//        return try {
//            val batch = firestore.batch()
//
//            val globalOrderRef = firestore.collection("orders").document(orderId)
//            val retailerOrderRef = firestore
//                .collection("retailers")
//                .document(retailerId)
//                .collection("orders")
//                .document(orderId)
//
//            batch.update(globalOrderRef, "orderStatus", newStatus)
//            batch.update(retailerOrderRef, "orderStatus", newStatus)
//
//            batch.commit().await()
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getOrderById(orderId: String): OrderDataClass? {
//        return try {
//            val snapshot = firestore.collection("orders")
//                .document(orderId)
//                .get()
//                .await()
//            snapshot.toObject(OrderDataClass::class.java)?.copy(orderId = snapshot.id)
//        } catch (e: Exception) {
//            null
//        }
//    }
//    fun listenToOrderRealtime(retailerId: String, orderId: String): LiveData<OrderDataClass?> {
//        val liveData = MutableLiveData<OrderDataClass?>()
//
//        val retailerOrderRef = firestore.collection("retailers")
//            .document(retailerId)
//            .collection("orders")
//            .document(orderId)
//
//        val globalOrderRef = firestore.collection("orders")
//            .document(orderId)
//
//        // Primary listener
//        retailerOrderRef.addSnapshotListener { snapshot, error ->
//            if (error != null || snapshot == null || !snapshot.exists()) {
//                liveData.value = null
//                return@addSnapshotListener
//            }
//            liveData.value = snapshot.toObject(OrderDataClass::class.java)
//        }
//
//        // Optional secondary listener
//        globalOrderRef.addSnapshotListener { snapshot, error ->
//            if (error != null || snapshot == null || !snapshot.exists()) {
//                return@addSnapshotListener
//            }
//            // Optional: Use timestamp to decide if this should override current value
//            // liveData.value = snapshot.toObject(OrderDataClass::class.java)
//        }
//        Log.e("OrderRealtime", "Order fetched: ${liveData.value}")
//
//        return liveData
//    }
//
//}
