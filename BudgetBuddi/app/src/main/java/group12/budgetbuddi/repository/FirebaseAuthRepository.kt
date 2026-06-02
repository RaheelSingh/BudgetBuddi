package group12.budgetbuddi.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirebaseAuthRepo"
        private const val USERS_COLLECTION = "users"
    }

    suspend fun registerUser(
        email: String,
        password: String,
        username: String
    ): AuthResult {
        return try {
            Log.d(TAG, "Registering: $email")

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            user?.let {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                it.updateProfile(profileUpdates).await()
                Log.d(TAG, "Username set: $username")

                // Create Firestore user document
                createUserDocument(user.uid, email, username)
            }

            AuthResult.Success(user?.uid ?: "", user?.email ?: "", user?.displayName ?: username)
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Password is too weak. Use at least 6 characters.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Invalid email address format.")
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("Email already registered. Please login instead.")
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    suspend fun loginUser(email: String, password: String): AuthResult {
        return try {
            Log.d(TAG, "Logging in: $email")

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            // Sync existing user to Firestore (for users registered before Firestore existed)
            user?.let {
                ensureUserDocumentExists(it.uid, it.email ?: "", it.displayName ?: "")
            }

            AuthResult.Success(user?.uid ?: "", user?.email ?: "", user?.displayName ?: "")
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Incorrect password. Please try again.")
        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.message}")
        }
    }

    suspend fun sendPasswordResetEmail(email: String): PasswordResetResult {
        return try {
            Log.d(TAG, "Sending reset email to: $email")
            auth.sendPasswordResetEmail(email).await()
            PasswordResetResult.Success("Password reset email sent! Check your inbox.")
        } catch (e: FirebaseAuthInvalidUserException) {
            PasswordResetResult.Error("No account found with this email.")
        } catch (e: Exception) {
            PasswordResetResult.Error("Failed to send reset email. Please try again.")
        }
    }

    suspend fun signOut(): Boolean {
        return try {
            auth.signOut()
            Log.d(TAG, "User signed out")
            true
        } catch (e: Exception) {
            false
        }
    }

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(FirebaseUser(user.uid, user.email ?: "", user.displayName ?: ""))
                Log.d(TAG, "User logged in: ${user.email}")
            } else {
                trySend(null)
                Log.d(TAG, "No user logged in")
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // ==================== FIRESTORE USER METHODS ====================

    private suspend fun createUserDocument(uid: String, email: String, username: String) {
        try {
            val userData = hashMapOf(
                "email" to email.lowercase(),
                "username" to username,
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to System.currentTimeMillis(),
                // GAMIFICATION FIELDS - NEW USER
                "xp" to 0,
                "level" to 1,
                "levelTitle" to "Novice Budgeter",
                "currentStreak" to 0,
                "longestStreak" to 0,
                "lastLoginDate" to "",
                "selectedAvatarBadgeId" to "",
                "unlockedBadges" to emptyList<String>(),
                "totalExpenses" to 0,
                "totalIncome" to 0,
                "totalBudgetGoals" to 0,
                "totalCategoriesCreated" to 0,
                "totalPhotosAdded" to 0
            )
            firestore.collection(USERS_COLLECTION).document(uid).set(userData).await()
            Log.d(TAG, "Firestore user document created for: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Firestore user document: ${e.message}", e)
        }
    }

    private suspend fun ensureUserDocumentExists(uid: String, email: String, username: String) {
        try {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)
            val snapshot = userDocRef.get().await()

            if (!snapshot.exists()) {
                val userData = hashMapOf(
                    "email" to email.lowercase(),
                    "username" to username,
                    "createdAt" to System.currentTimeMillis(),
                    "lastLogin" to System.currentTimeMillis(),
                    "migrated" to true,
                    // GAMIFICATION FIELDS - EXISTING USER
                    "xp" to 0,
                    "level" to 1,
                    "levelTitle" to "Novice Budgeter",
                    "currentStreak" to 0,
                    "longestStreak" to 0,
                    "lastLoginDate" to "",
                    "selectedAvatarBadgeId" to "",
                    "unlockedBadges" to emptyList<String>(),
                    "totalExpenses" to 0,
                    "totalIncome" to 0,
                    "totalBudgetGoals" to 0,
                    "totalCategoriesCreated" to 0,
                    "totalPhotosAdded" to 0
                )
                userDocRef.set(userData).await()
                Log.d(TAG, "Firestore user document created for existing user: $email")
            } else {
                // Check if existing user needs gamification fields (migration)
                if (!snapshot.contains("xp")) {
                    val updates = mapOf<String, Any>(
                        "xp" to 0,
                        "level" to 1,
                        "levelTitle" to "Novice Budgeter",
                        "currentStreak" to 0,
                        "longestStreak" to 0,
                        "lastLoginDate" to "",
                        "selectedAvatarBadgeId" to "",
                        "unlockedBadges" to emptyList<String>(),
                        "totalExpenses" to 0,
                        "totalIncome" to 0,
                        "totalBudgetGoals" to 0,
                        "totalCategoriesCreated" to 0,
                        "totalPhotosAdded" to 0
                    )
                    userDocRef.update(updates).await()
                    Log.d(TAG, "Added gamification fields to existing user: $email")
                }
                userDocRef.update("lastLogin", System.currentTimeMillis()).await()
                Log.d(TAG, "Updated last login for: $email")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure Firestore user document: ${e.message}", e)
        }
    }

    suspend fun getUserData(uid: String): Map<String, Any>? {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            snapshot.data
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user data: ${e.message}", e)
            null
        }
    }
}

sealed class AuthResult {
    data class Success(val uid: String, val email: String, val username: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class PasswordResetResult {
    data class Success(val message: String) : PasswordResetResult()
    data class Error(val message: String) : PasswordResetResult()
}

data class FirebaseUser(
    val uid: String,
    val email: String,
    val username: String
)