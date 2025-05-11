package com.example.dealtrack.wholesaler.wholesalerLoginPage.mvvm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WholesalerLoginRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // ✅ REGISTER WHOLESALER (Stores Role + Generates Invite Code)
    suspend fun registerWholesaler(email: String, password: String, wholesaler: Wholesaler): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("User ID is null"))

            // Generate Invite Code
            val inviteCode = Wholesaler.generateInviteCode()
            val updatedWholesaler = wholesaler.copy(inviteCode = inviteCode)

            // Firestore Batch Write (Ensures Atomic Update)
            val batch = firestore.batch()
            val userRef = firestore.collection("users").document(uid)
            val wholesalerRef = firestore.collection("wholesalers").document(uid)

            batch.set(userRef, mapOf("role" to "wholesaler", "email" to email))
            batch.set(wholesalerRef, updatedWholesaler.toMap())

            batch.commit().await()

            Result.success(uid)
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

    // ✅ LOGIN WHOLESALER (Verifies Role)
    suspend fun loginWholesaler(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("User not found"))

            // Fetch User Role
            val userDoc = firestore.collection("users").document(uid).get().await()
            val role = userDoc.getString("role")

            if (role != "wholesaler") {
                return Result.failure(Exception("Unauthorized access: Not a wholesaler account"))
            }

            Result.success(uid)
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

    // ✅ LOGOUT WHOLESALER
    fun logoutWholesaler() {
        auth.signOut() // Logs out the user
    }
}
