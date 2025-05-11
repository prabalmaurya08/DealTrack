package com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProductViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _uploadResult = MutableLiveData<Boolean?>()
    val uploadResult: LiveData<Boolean?> = _uploadResult

    private val _products = MutableLiveData<List<ProductData>>()
    val products: LiveData<List<ProductData>> = _products

    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = _updateResult

    private val _deleteResult = MutableLiveData<Boolean?>()
    val deleteResult: LiveData<Boolean?> = _deleteResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _product = MutableLiveData<ProductData?>()
    val product: LiveData<ProductData?> get() = _product

    // 🔹 Loading status LiveData
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchProductById(productId: String): LiveData<ProductData?> {
        viewModelScope.launch {
            _isLoading.value = true
            _product.value = repository.getProductById(productId)
            _isLoading.value = false
        }
        return _product
    }

    fun addProduct(product: ProductData, imageBitmap: Bitmap?) {
        viewModelScope.launch {
            _uploadResult.value = null
            _isLoading.value = true
            try {
                val imageBytes = imageBitmap?.let { convertBitmapToByteArray(it) }
                val success = repository.addProduct(product, imageBytes)
                if (success) {
                    _uploadResult.value = true
                } else {
                    _error.value = "Failed to add product (duplicate detected)."
                    _uploadResult.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _uploadResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchProducts(wholesalerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val productList = repository.getProductsByWholesaler(wholesalerId)
                _products.value = productList
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProductDetails(productId: String, updatedData: Map<String, Any>, imageBitmap: Bitmap?) {
        viewModelScope.launch {
            _updateResult.value = null
            _isLoading.value = true
            try {
                val imageBytes = imageBitmap?.let { convertBitmapToByteArray(it) }
                val success = repository.updateProduct(productId, updatedData, imageBytes)
                _updateResult.value = success
            } catch (e: Exception) {
                _error.value = e.message
                _updateResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProductById(productId: String) {
        viewModelScope.launch {
            _deleteResult.value = null
            _isLoading.value = true
            try {
                val success = repository.deleteProduct(productId)
                _deleteResult.value = success
            } catch (e: Exception) {
                _error.value = e.message
                _deleteResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray = withContext(Dispatchers.IO) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.toByteArray()
    }

    fun fetchProductsForRetailer() {
        viewModelScope.launch {
            _isLoading.value = true
            val retailerId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                _isLoading.value = false
                return@launch
            }

            val wholesalerId = getWholesalerIdForRetailer(retailerId)
            if (wholesalerId != null) {
                _products.value = repository.getProductsByWholesaler(wholesalerId)
            }
            _isLoading.value = false
        }
    }

    private suspend fun getWholesalerIdForRetailer(retailerId: String): String? {
        return try {
            val retailerDoc = FirebaseFirestore.getInstance()
                .collection("retailers")
                .document(retailerId)
                .get()
                .await()
            retailerDoc.getString("wholesalerId")
        } catch (e: Exception) {
            null
        }
    }
}

