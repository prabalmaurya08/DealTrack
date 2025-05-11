package com.example.dealtrack.retailer.retailerCartPage.mvvm

import com.google.firebase.Timestamp

data class CartDataClass (
    val productId: String = "",
    val productName: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    var quantity: Int = 1,
    val wholesalerId: String = "",
    val addedAt: Timestamp = Timestamp.now()
)