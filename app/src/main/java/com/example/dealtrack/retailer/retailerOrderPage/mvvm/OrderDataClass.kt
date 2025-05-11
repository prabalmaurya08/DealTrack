package com.example.dealtrack.retailer.retailerOrderPage.mvvm

import com.google.firebase.Timestamp

data class OrderDataClass(
    val orderId: String = "",
    val orderCode: String = "",
    val retailerId: String = "",
    val wholesalerId: String = "",
    val products: List<OrderProduct> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderStatus: String = "Pending",
    val timestamp: Timestamp = Timestamp.now()
)

data class OrderProduct(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0
)
