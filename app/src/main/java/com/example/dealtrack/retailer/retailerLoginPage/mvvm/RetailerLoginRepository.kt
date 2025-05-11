package com.example.dealtrack.retailer.retailerLoginPage.mvvm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class RetailerLoginRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ✅ REGISTER RETAILER
    suspend fun registerRetailer(email: String, password: String, retailer: Retailer, inviteCode: String): Result<String> {
        return try {
            // Fetch Wholesaler ID using Invite Code
            val wholesalerQuery = firestore.collection("wholesalers") // ✅ Fixed collection name
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()

            if (wholesalerQuery.isEmpty) {
                return Result.failure(Exception("Invalid Invite Code"))
            }

            val wholesalerId = wholesalerQuery.documents[0].id

            // Create Retailer in Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val retailerId = authResult.user?.uid ?: return Result.failure(Exception("User ID is null"))

            val updatedRetailer = retailer.copy(uid = retailerId, wholesalerId = wholesalerId)

            // Firestore Batch Write (Ensures Atomic Update)
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(retailerId)
            val retailerRef = firestore.collection("retailers").document(retailerId)
            val wholesalerRef = firestore.collection("wholesalers").document(wholesalerId) // ✅ Fixed collection name

            // ✅ Stores role and email in `users`
            batch.set(userRef, mapOf("role" to "retailer", "email" to email))

            // ✅ Stores Retailer separately in `retailers`
            batch.set(retailerRef, updatedRetailer)

            // ✅ Updates Wholesaler's `retailerIds` field (Stores in array)
            batch.set(wholesalerRef, mapOf("retailerIds" to FieldValue.arrayUnion(retailerId)), SetOptions.merge())

            batch.commit().await()

            Result.success(retailerId)
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_WEAK_PASSWORD" -> "Password should be at least 6 characters."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
                "ERROR_INVALID_EMAIL" -> "Invalid email format."
                else -> "Signup failed: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Signup failed due to network or server issue. Try again."))
        }
    }

    // ✅ LOGIN RETAILER
    suspend fun loginRetailer(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val retailerId = authResult.user?.uid ?: return Result.failure(Exception("User not found"))

            // Fetch User Role
            val userDoc = firestore.collection("users").document(retailerId).get().await()
            val role = userDoc.getString("role")

            if (role != "retailer") {
                return Result.failure(Exception("Unauthorized access: Not a retailer account"))
            }

            Result.success(retailerId)
        } catch (e: FirebaseAuthException) {
            val errorMessage = when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Try again."
                else -> "Login failed: ${e.message}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(Exception("Login failed due to network or server issue. Try again."))
        }
    }

    // ✅ LOGOUT RETAILER
    fun logoutRetailer() {
        auth.signOut()
    }
}
