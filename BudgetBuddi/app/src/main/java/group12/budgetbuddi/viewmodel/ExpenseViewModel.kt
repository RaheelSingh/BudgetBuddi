package group12.budgetbuddi.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import group12.budgetbuddi.SupabaseClient
import group12.budgetbuddi.data.entity.ExpenseEntity
import group12.budgetbuddi.repository.ExpenseRepository
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "ExpenseViewModel"
        private const val BUCKET_NAME = "expense-receipts"
    }

    // Current logged-in user email
    private val userEmail: String
        get() = auth.currentUser?.email ?: ""


    val allExpenses: Flow<List<ExpenseEntity>>
        get() = if (userEmail.isNotBlank()) repository.getAllExpenses(userEmail) else emptyFlow()

    val expensesOnly: Flow<List<ExpenseEntity>>
        get() = if (userEmail.isNotBlank()) repository.getExpensesOnly(userEmail) else emptyFlow()

    val incomeOnly: Flow<List<ExpenseEntity>>
        get() = if (userEmail.isNotBlank()) repository.getIncomeOnly(userEmail) else emptyFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Image Upload to Supabase

    /**
     * Uploads an image to Supabase storage and returns its public URL.
     * Returns null if no image is selected or upload fails.
     */
    suspend fun uploadImageToSupabase(context: Context, imageUri: Uri): String? {
        return try {
            val imageBytes = context.contentResolver
                .openInputStream(imageUri)
                ?.use { it.readBytes() }

            if (imageBytes == null) {
                Log.e(TAG, "Could not read image bytes")
                return null
            }

            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val filename = "receipt_${System.currentTimeMillis()}.$extension"
            val filePath = "${userEmail.replace("@", "_").replace(".", "_")}/$filename"

            val bucket = SupabaseClient.client.storage.from(BUCKET_NAME)

            bucket.upload(
                path = filePath,
                data = imageBytes
            ) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }

            // Get public URL (use this for public buckets)
            val publicUrl = bucket.publicUrl(filePath)
            Log.d(TAG, "Uploaded image to Supabase: $publicUrl")
            publicUrl

        } catch (e: Exception) {
            Log.e(TAG, "Supabase upload failed: ${e.message}", e)
            null
        }
    }

    // Save Expense/Income

    /**
     * Saves a transaction. Optionally uploads an image first.
     * @param expense The expense to save (photoPath should already be set to URL or null)
     */
    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val expenseWithUser = expense.copy(userEmail = userEmail)
                repository.insertExpense(expenseWithUser)
                _saveSuccess.value = true
                Log.d(TAG, "Expense saved successfully")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save: ${e.message}"
                Log.e(TAG, "Save failed: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Saves expense with image upload in one call.
     */
    fun saveExpenseWithImage(context: Context, expense: ExpenseEntity, imageUri: Uri?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Upload image first if provided
                val photoUrl = if (imageUri != null) {
                    uploadImageToSupabase(context, imageUri)
                } else null

                val expenseWithUser = expense.copy(
                    userEmail = userEmail,
                    photoPath = photoUrl
                )
                repository.insertExpense(expenseWithUser)
                _saveSuccess.value = true
                Log.d(TAG, "Expense with image saved successfully")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save: ${e.message}"
                Log.e(TAG, "Save with image failed: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update: ${e.message}"
                Log.e(TAG, "Update failed: ${e.message}", e)
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete: ${e.message}"
                Log.e(TAG, "Delete failed: ${e.message}", e)
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}