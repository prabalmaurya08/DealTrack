package com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm
import com.example.dealtrack.wholesaler.wholesalerInventoryPage.mvvm.Inventory


import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // ✅ Upload Product Image
    suspend fun uploadImage(imageBytes: ByteArray): String? {
        return try {
            val imageRef = storage.reference.child("product_images/${UUID.randomUUID()}.jpg")
            imageRef.putBytes(imageBytes).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProductById(productId: String): ProductData? {
        return try {
            val snapshot = db.collection("products")
                .document(productId)
                .get()
                .await()
            snapshot.toObject(ProductData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // ✅ Add Product with Stock Check
    suspend fun addProduct(product: ProductData, imageBytes: ByteArray?): Boolean {
        return try {
            // Ensure stock is not negative
            if (product.stock < 0) {
                return false // Or handle error for invalid stock
            }

            // Check for duplicates
            val duplicateQuery = db.collection("products")
                .whereEqualTo("productCode", product.productCode)
                .whereEqualTo("wholesalerId", product.wholesalerId)
                .get()
                .await()

            if (!duplicateQuery.isEmpty) {
                // Duplicate product found
                return false // Or throw an exception
            }

            // Upload image
            val imageUrl = imageBytes?.let { uploadImage(it) } ?: ""

            // Create batch
            val batch = db.batch()
            val productRef = db.collection("products").document()
            val inventoryRef = db.collection("inventory").document(productRef.id)

            // Create new product and inventory
            val newProduct = product.copy(
                productId = productRef.id,
                imageUrl = imageUrl,
                lastUpdated = Timestamp.now()
            )
            val newInventory = Inventory(
                productId = productRef.id,
                wholesalerId = product.wholesalerId,
                stock = product.stock.coerceAtLeast(0),  // Ensure stock is not negative
                lastUpdated = Timestamp.now()
            )

            // Commit batch
            batch.set(productRef, newProduct)
            batch.set(inventoryRef, newInventory)
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Fetch All Products for a Wholesaler
    suspend fun getProductsByWholesaler(wholesalerId: String): List<ProductData> {
        return try {
            db.collection("products")
                .whereEqualTo("wholesalerId", wholesalerId)
                .get()
                .await()
                .toObjects(ProductData::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ Update Product Details with Stock Check
    suspend fun updateProduct(productId: String, updatedData: Map<String, Any>, imageBytes: ByteArray?): Boolean {
        return try {
            val updates = updatedData.toMutableMap()

            imageBytes?.let {
                val imageUrl = uploadImage(it)
                if (imageUrl != null) {
                    updates["imageUrl"] = imageUrl
                }
            }

            // Check if stock is being updated and ensure it’s not negative
            updatedData["stock"]?.let {
                if (it is Long && it < 0) {
                    return false // Invalid stock value
                }
            }

            db.collection("products").document(productId)
                .update(updates + ("lastUpdated" to Timestamp.now()))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ✅ Delete Product and Remove from Inventory
    suspend fun deleteProduct(productId: String): Boolean {
        return try {
            val batch = db.batch()
            val productRef = db.collection("products").document(productId)
            val inventoryRef = db.collection("inventory").document(productId)

            batch.delete(productRef)
            batch.delete(inventoryRef)
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

