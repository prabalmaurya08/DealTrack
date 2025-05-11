package com.example.dealtrack.wholesaler.wholesalerProductPage.mvvm

import com.google.firebase.Timestamp

data class ProductData (
    val productId: String = "",
    val productCode: String = "",
    val wholesalerId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val mrp: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true,
    val stock: Int = 0,
    val expiryDate: String = "",
    val manufacturerDate: String = "",
    val lowStockAlert: Int = 5,
    val lastUpdated: Timestamp? = null

)


