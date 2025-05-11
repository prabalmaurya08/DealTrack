package com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm

import java.util.UUID

data class Wholesaler(
    val businessName: String = "",
    val registrationNumber: String = "",
    val ownerName: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val inviteCode: String = "",  // Generated after signup
    val retailerIds: List<String> = emptyList(),  // Retailers assigned via invite
    val productIds: List<String> = emptyList()    // Products linked to this wholesaler
) {
    fun toMap(): Map<String, Any> {
        return mutableMapOf<String, Any>(
            "businessName" to businessName,
            "registrationNumber" to registrationNumber,
            "ownerName" to ownerName,
            "contactNumber" to contactNumber,
            "email" to email,
            "retailerIds" to retailerIds,
            "productIds" to productIds
        ).apply {
            if (inviteCode.isNotEmpty()) this["inviteCode"] = inviteCode
        }
    }

    companion object {
        fun generateInviteCode(): String {
            return UUID.randomUUID().toString().substring(0, 6).uppercase()
        }
    }
}
