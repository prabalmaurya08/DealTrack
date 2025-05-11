package com.example.dealtrack.retailer.retailerOrderPage.mvvm



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dealtrack.retailer.retailerCartPage.mvvm.CartDataClass
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class RetailerOrderViewmodel : ViewModel() {

    private val orderRepository = RetailerOrderRepository()

    private val _orderPlacedSuccess = MutableLiveData<Boolean>()
    val orderPlacedSuccess: LiveData<Boolean> get() = _orderPlacedSuccess

    private val _orderError = MutableLiveData<String?>()
    val orderError: LiveData<String?> get() = _orderError

    fun placeOrder() {
        orderRepository.placeOrder(
            onSuccess = {
                _orderPlacedSuccess.postValue(true)
            },
            onFailure = { exception ->
                _orderError.postValue(exception.message)
            }
        )
    }

    private val _cartItems = MutableLiveData<List<CartDataClass>>()
    val cartItems: LiveData<List<CartDataClass>> get() = _cartItems

    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?> get() = _address

    fun fetchCartItems() {
        orderRepository.getCartItems(
            onSuccess = { _cartItems.value = it },
            onFailure = { _cartItems.value = emptyList() }
        )
    }

    fun fetchAddress() {
        orderRepository.getAddress(
            onSuccess = { _address.value = it },
            onFailure = { _address.value = "Error fetching address" }
        )
    }


    fun clearError() {
        _orderError.value = null
    }

    //for order section




        private val _orders = MutableLiveData<List<OrderDataClass>>()
        val orders: LiveData<List<OrderDataClass>> get() = _orders

        fun fetchOrders() {
            orderRepository.getRetailerOrders(
                onSuccess = { _orders.value = it },
                onFailure = { _orders.value = emptyList() }
            )
        }


    //for order detail screen
    private val _order = MutableLiveData<OrderDataClass?>()
    val order: LiveData<OrderDataClass?> get() = _order



    fun getOrderById(orderId: String) {
        viewModelScope.launch {
            _order.value = orderRepository.getOrderById(orderId)
        }
    }



    fun listenToOrderRealtime(retailerId: String, orderId: String) {
        orderRepository.listenToOrderRealtime(retailerId, orderId).observeForever {
            _order.value = it
        }
    }




}
