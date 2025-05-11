package com.example.dealtrack.retailer.retailerWishList.mvvm

import com.google.firebase.Timestamp

data class WishListDataClass (
    val productId: String = "",
    val productName: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val wholesalerId: String = "",
    val addedAt: Timestamp = Timestamp.now()
)