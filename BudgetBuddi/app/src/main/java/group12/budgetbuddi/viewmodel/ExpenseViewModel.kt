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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "ExpenseViewModel"
        private const val BUCKET_NAME = "expense-receipts"
    }

    private val repository = ExpenseRepository()
    private val auth = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _allExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val allExpenses: StateFlow<List<ExpenseEntity>> = _allExpenses.asStateFlow()

    val expensesOnly: StateFlow<List<ExpenseEntity>> =
        allExpenses
            .map { expenses ->
                expenses.filter { it.type == "expense" }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val incomeOnly: StateFlow<List<ExpenseEntity>> =
        allExpenses
            .map { expenses ->
                expenses.filter { it.type == "income" }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        observeExpensesForCurrentUser()
    }

    private fun observeExpensesForCurrentUser() {
        val userId = currentUserId

        if (userId.isNullOrBlank()) {
            Log.e(TAG, "No Firebase user found. Cannot observe expenses.")
            _allExpenses.value = emptyList()
            return
        }

        viewModelScope.launch {
            repository.getAllExpenses(userId)
                .catch { error ->
                    Log.e(TAG, "Failed to observe expenses: ${error.message}", error)
                    _uiMessage.value = "Failed to load expenses"
                    _allExpenses.value = emptyList()
                }
                .collect { expenses ->
                    _allExpenses.value = expenses
                }
        }
    }

    private suspend fun uploadImageToSupabase(
        context: Context,
        userId: String,
        imageUri: Uri
    ): String? {
        return try {
            _isUploadingImage.value = true

            val imageBytes = context.contentResolver
                .openInputStream(imageUri)
                ?.use { it.readBytes() }

            if (imageBytes == null) {
                Log.e(TAG, "Could not read image bytes from URI: $imageUri")
                _uiMessage.value = "Could not read the selected image"
                return null
            }

            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }

            val filename = "receipt_${System.currentTimeMillis()}.$extension"

            /*
             Supabase storage path:

             expense-receipts/{uid}/receipt_timestamp.jpg

             Using uid is better than email because Firebase UID is stable.
            */
            val filePath = "$userId/$filename"

            val bucket = SupabaseClient.client.storage.from(BUCKET_NAME)

            bucket.upload(
                path = filePath,
                data = imageBytes
            ) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }

            val publicUrl = bucket.publicUrl(filePath)

            Log.d(TAG, "Image uploaded successfully: $publicUrl")

            publicUrl

        } catch (e: Exception) {
            Log.e(TAG, "Supabase upload failed: ${e.message}", e)
            _uiMessage.value = "Image upload failed: ${e.message}"
            null

        } finally {
            _isUploadingImage.value = false
        }
    }

    fun saveExpense(
        amount: Double,
        type: String,
        category: String,
        subCategory: String,
        date: String,
        startTime: String,
        endTime: String,
        note: String,
        photoPath: String?,
        paymentMethod: String,
        isRecurring: Boolean
    ) {
        val userId = currentUserId

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot save expense because Firebase userId is null")
            return
        }

        if (amount <= 0.0) {
            _uiMessage.value = "Please enter a valid amount"
            return
        }

        if (type.isBlank()) {
            _uiMessage.value = "Please select transaction type"
            return
        }

        if (category.isBlank()) {
            _uiMessage.value = "Please select a category"
            return
        }

        if (date.isBlank()) {
            _uiMessage.value = "Please select a date"
            return
        }

        viewModelScope.launch {
            try {
                val finalPhotoPath: String? = if (!photoPath.isNullOrBlank()) {
                    val context = getApplication<Application>().applicationContext
                    val uri = Uri.parse(photoPath)

                    uploadImageToSupabase(
                        context = context,
                        userId = userId,
                        imageUri = uri
                    )
                } else {
                    null
                }

                val now = System.currentTimeMillis()

                val expense = ExpenseEntity(
                    userId = userId,
                    amount = amount,
                    type = type.trim().lowercase(),
                    category = category.trim(),
                    subCategory = subCategory.trim(),
                    date = date.trim(),
                    startTime = startTime.trim(),
                    endTime = endTime.trim(),
                    note = note.trim(),
                    photoPath = finalPhotoPath,
                    paymentMethod = paymentMethod.trim(),
                    isRecurring = isRecurring,
                    createdAt = now,
                    updatedAt = now
                )

                repository.insertExpense(
                    userId = userId,
                    expense = expense
                )

                _uiMessage.value = "Saved successfully"
                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e(TAG, "Failed to save expense: ${e.message}", e)
                _uiMessage.value = "Failed to save: ${e.message}"
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        val userId = currentUserId

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot update expense because Firebase userId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.updateExpense(
                    userId = userId,
                    expense = expense.copy(userId = userId)
                )

                _uiMessage.value = "Expense updated"

            } catch (e: Exception) {
                Log.e(TAG, "Update failed: ${e.message}", e)
                _uiMessage.value = "Failed to update: ${e.message}"
            }
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        val userId = currentUserId

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot delete expense because Firebase userId is null")
            return
        }

        viewModelScope.launch {
            try {
                repository.deleteExpense(
                    userId = userId,
                    expense = expense
                )

                _uiMessage.value = "Expense deleted"

            } catch (e: Exception) {
                Log.e(TAG, "Delete failed: ${e.message}", e)
                _uiMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun setMessage(message: String) {
        _uiMessage.value = message
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}