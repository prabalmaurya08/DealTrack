package com.example.dealtrack.retailer.retailerLoginPage.mvvm

data class Retailer(
    val uid: String = "",
    val storeName: String = "",
    val storeNumber: String = "",
    val ownerName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val wholesalerId: String = "" // Stores the UID of the Wholesaler
)
