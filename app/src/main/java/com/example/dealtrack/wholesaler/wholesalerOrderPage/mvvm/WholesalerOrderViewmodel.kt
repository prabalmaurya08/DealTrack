package com.example.dealtrack.wholesaler.wholesalerOrderPage.mvvm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WholesalerOrderViewmodel : ViewModel() {

    private val repository = WholesalerOrderRepository()

    // List of orders paired with retailer store name
    private val _orders = MutableStateFlow<List<Pair<OrderDataClass, String>>>(emptyList())
    val orders: StateFlow<List<Pair<OrderDataClass, String>>> = _orders.asStateFlow()

    // Loading status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Full order list for filtering
    private var fullOrderList: List<Pair<OrderDataClass, String>> = emptyList()

    // Filter state
    private val _selectedFilter = MutableStateFlow(OrderFilter.ALL)
    val selectedFilter: StateFlow<OrderFilter> = _selectedFilter.asStateFlow()

    // Selected order for detail screen
    private val _selectedOrder = MutableStateFlow<OrderDataClass?>(null)
    val selectedOrder: StateFlow<OrderDataClass?> = _selectedOrder.asStateFlow()

    // Store name for selected order (used on detail screen)
    private val _selectedRetailerStoreName = MutableStateFlow<String?>(null)
    val selectedRetailerStoreName: StateFlow<String?> = _selectedRetailerStoreName.asStateFlow()

    // Optional: store name fetched by retailerId for any general purpose
    private val _retailerStoreName = MutableStateFlow<String?>(null)
    val retailerStoreName: StateFlow<String?> = _retailerStoreName.asStateFlow()

    init {
        fetchOrders()
    }

    // Fetch all orders for wholesaler with store names
    fun fetchOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            fullOrderList = repository.getWholesalerOrdersWithRetailerNames()
            applyFilter(_selectedFilter.value)
            _isLoading.value = false
        }
    }

    // Apply current selected filter to full list
    fun applyFilter(filter: OrderFilter) {
        _selectedFilter.value = filter
        _orders.value = when (filter) {
            OrderFilter.ALL -> fullOrderList
            OrderFilter.PENDING -> fullOrderList.filter { it.first.orderStatus == "Pending" }
            OrderFilter.DELIVERED -> fullOrderList.filter { it.first.orderStatus == "Delivered" }
        }
    }

    // Select an order and store name (used for navigating to detail screen)
    fun setSelectedOrder(order: OrderDataClass, storeName: String) {
        _selectedOrder.value = order
        _selectedRetailerStoreName.value = storeName
    }

    // Clear selected order when exiting detail screen
    fun clearSelectedOrder() {
        _selectedOrder.value = null
        _selectedRetailerStoreName.value = null
    }

    // Fetch retailer store name by retailerId (used in detail screen)
    fun fetchRetailerStoreName(retailerId: String) {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("retailers")
                    .document(retailerId)
                    .get()
                    .await()

                val name = snapshot.getString("storeName") ?: "Unknown Store"
                _retailerStoreName.value = name
            } catch (e: Exception) {
                _retailerStoreName.value = "Unknown Store"
                Log.e("WholesalerVM", "Error fetching retailer name: ${e.message}")
            }
        }
    }

    // Fetch a single order by ID from global orders collection
    fun getOrderById(orderId: String, retailerId: String) {
        viewModelScope.launch {
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("orders")
                    .document(orderId)
                    .get()
                    .await()

                val order = snapshot.toObject(OrderDataClass::class.java)
                _selectedOrder.value = order?.copy(orderId = snapshot.id)

                // Fetch associated store name
                fetchRetailerStoreName(retailerId)
            } catch (e: Exception) {
                Log.e("WholesalerVM", "Error fetching order: ${e.message}")
            }
        }
    }

        fun updateOrderStatusForBoth(orderId: String, retailerId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, retailerId, newStatus)
        }
    }

    // Optional: reapply current filter manually
    fun reapplyFilter() {
        applyFilter(_selectedFilter.value)
    }
}


enum class OrderFilter {
    ALL, PENDING, DELIVERED
}



//
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.dealtrack.retailer.retailerOrderPage.mvvm.OrderDataClass
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class WholesalerOrderViewmodel : ViewModel() {
//
//    private val repository = WholesalerOrderRepository()
//
//    private val _selectedFilter = MutableStateFlow(OrderStatusFilter.ALL)
//    val selectedFilter: StateFlow<OrderStatusFilter> = _selectedFilter
//
//    private val _allOrders = MutableStateFlow<List<Pair<OrderDataClass, String>>>(emptyList())
//    val allOrders: StateFlow<List<Pair<OrderDataClass, String>>> = _allOrders
//
//
//
//    private val _isLoading = MutableStateFlow(true)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//
//

//
//    init {
//
//        fetchOrders()
//    }
//
//    fun setFilter(filter: OrderStatusFilter) {
//        _selectedFilter.value = filter
//    }
//
//    fun refreshOrders() {
//        fetchOrders()
//    }
//
//    private fun fetchOrders() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            val result = repository.getWholesalerOrdersWithRetailerNames()
//            _allOrders.value = result
//            _isLoading.value = false
//            Log.d("WholesalerOrderPage", "Orders received: ${result.size}")
//        }
//    }
//
//
//    val orders = combine(_allOrders, _selectedFilter) { allOrders, selectedFilter ->
//        if (allOrders.isEmpty()) {
//            emptyList()
//        } else {
//            when (selectedFilter) {
//                OrderStatusFilter.ALL -> allOrders
//                OrderStatusFilter.PENDING -> allOrders.filter { it.first.orderStatus == "Pending" }
//                OrderStatusFilter.DELIVERED -> allOrders.filter { it.first.orderStatus == "Delivered" }
//            }
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//

//

//

//}
