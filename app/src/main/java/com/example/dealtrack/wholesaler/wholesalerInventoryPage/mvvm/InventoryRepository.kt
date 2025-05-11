package com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm

import com.example.dealtrack.wholesaler.wholesalerHomePage.InventoryStockCategory
import com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm.ProductData
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InventoryRepository {
    private val db = FirebaseFirestore.getInstance()


    suspend fun getInventoryDisplayItems(wholesalerId: String, filter: InventoryFilter): List<InventoryDisplayItem> {
        val inventorySnapshots = db.collection("inventory")
            .whereEqualTo("wholesalerId", wholesalerId)
            .get().await()

        val inventoryList = inventorySnapshots.toObjects(Inventory::class.java)
        val productIds = inventoryList.map { it.productId }

        // Check if productIds list is empty
        if (productIds.isEmpty()) {
            return emptyList()  // Return empty list if no products found
        }

        val productSnapshots = db.collection("products")
            .whereIn(FieldPath.documentId(), productIds)
            .get().await()

        val productMap = productSnapshots.documents.associateBy { it.id }

        val combinedList = inventoryList.mapNotNull { inv ->
            val prodDoc = productMap[inv.productId] ?: return@mapNotNull null
            val product = prodDoc.toObject(ProductData::class.java) ?: return@mapNotNull null

            val lowStockThreshold = product.lowStockAlert
            val isLowStock = inv.stock < lowStockThreshold

            val isExpiringSoon = try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val expiryDate = sdf.parse(product.expiryDate)
                val threshold = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.time
                expiryDate != null && expiryDate.before(threshold)
            } catch (e: Exception) {
                false
            }

            if (!shouldInclude(filter, isLowStock, isExpiringSoon, inv.stock)) return@mapNotNull null

            InventoryDisplayItem(
                productId = inv.productId,
                productName = product.name,
                productCode = product.productCode,
                imageUrl = product.imageUrl ?: "",
                expiryDate = product.expiryDate,
                manufacturedDate = product.manufacturerDate,
                stock = inv.stock,
                lowStockAlert = lowStockThreshold,
                isLowStock = isLowStock,
                isExpiringSoon = isExpiringSoon
            )
        }

        return combinedList
    }


    private fun shouldInclude(
        filter: InventoryFilter,
        isLowStock: Boolean,
        isExpiringSoon: Boolean,
        stock: Int
    ): Boolean {
        return when (filter) {
            InventoryFilter.ALL -> true
            InventoryFilter.LOW_STOCK -> isLowStock
            InventoryFilter.RESTOCK -> stock == 0
            InventoryFilter.EXPIRING -> isExpiringSoon
        }
    }

    // Real-time listener for inventory changes
    fun listenToInventoryChanges(wholesalerId: String, onInventoryUpdated: (List<Inventory>) -> Unit) {
        db.collection("inventory")
            .whereEqualTo("wholesalerId", wholesalerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    // Handle error (optional)
                    return@addSnapshotListener
                }
                val updatedInventoryList = snapshot.toObjects(Inventory::class.java)
                onInventoryUpdated(updatedInventoryList) // Notify the UI
            }
    }

    //homescreen
    suspend fun getInventoryStats(wholesalerId: String): Map<InventoryStockCategory, Int> {
        val inventorySnapshot = FirebaseFirestore.getInstance()
            .collection("inventory")
            .whereEqualTo("wholesalerId", wholesalerId)
            .get()
            .await()

        val productSnapshot = FirebaseFirestore.getInstance()
            .collection("products")
            .whereEqualTo("wholesalerId", wholesalerId)
            .get()
            .await()

        val productMap = productSnapshot.documents.associateBy { it.id }

        return inventorySnapshot.documents.map { doc ->
            val productId = doc.getString("productId") ?: ""
            val stock = doc.getLong("stock")?.toInt() ?: 0
            val lowStockAlert = productMap[productId]?.getLong("lowStockAlert")?.toInt() ?: 10

            when {
                stock == 0 -> InventoryStockCategory.OUT_OF_STOCK
                stock <= lowStockAlert -> InventoryStockCategory.LOW_STOCK
                else -> InventoryStockCategory.IN_STOCK
            }
        }.groupingBy { it }.eachCount()
    }

}
