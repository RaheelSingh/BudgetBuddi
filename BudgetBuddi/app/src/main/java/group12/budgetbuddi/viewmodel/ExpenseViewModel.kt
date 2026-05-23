package group12.budgetbuddi.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    private val authRepository = group12.budgetbuddi.repository.CategoryRepository()
    private val currentUserId: String? = authRepository.getCurrentUserId()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage.asStateFlow()

    private val _allExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val allExpenses: StateFlow<List<ExpenseEntity>> = _allExpenses.asStateFlow()

    val expensesOnly: StateFlow<List<ExpenseEntity>> = allExpenses
        .map { expenses ->
            expenses.filter { it.type == "expense" }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeOnly: StateFlow<List<ExpenseEntity>> = allExpenses
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
                    _uiMessage.value = "Failed to load expenses: ${error.message}"
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
    ): String {
        try {
            _isUploadingImage.value = true
            _uiMessage.value = "Uploading image..."

            Log.d(TAG, "Starting Supabase upload for URI: $imageUri")

            val imageBytes = context.contentResolver
                .openInputStream(imageUri)
                ?.use { inputStream -> inputStream.readBytes() }

            if (imageBytes == null || imageBytes.isEmpty()) {
                throw IllegalStateException("Could not read selected image")
            }

            Log.d(TAG, "Image bytes read successfully. Size=${imageBytes.size}")

            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

            val extension = when (mimeType) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/jpeg" -> "jpeg"
                "image/jpg" -> "jpg"
                else -> "jpg"
            }

            val filename = "receipt_${System.currentTimeMillis()}.$extension"
            val filePath = "$userId/$filename"

            Log.d(TAG, "Uploading to bucket=$BUCKET_NAME path=$filePath mimeType=$mimeType")

            val bucket = SupabaseClient.client.storage.from(BUCKET_NAME)

            bucket.upload(
                path = filePath,
                data = imageBytes
            ) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }

            val publicUrl = bucket.publicUrl(filePath)
            Log.d(TAG, "Supabase upload successful. Public URL=$publicUrl")

            return publicUrl
        } catch (e: Exception) {
            Log.e(TAG, "Supabase upload failed: ${e.message}", e)
            throw e
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
        note: String,
        photoPath: String?,
        paymentMethod: String,
        isRecurring: Boolean
    ) {
        val userId = currentUserId

        if (userId.isNullOrBlank()) {
            _uiMessage.value = "No logged-in user found"
            Log.e(TAG, "Cannot save transaction because Firebase userId is null")
            return
        }

        val cleanType = when (type.trim().lowercase()) {
            "income" -> "income"
            else -> "expense"
        }

        if (amount <= 0.0) {
            _uiMessage.value = "Please enter a valid amount"
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
                _uiMessage.value = "Saving transaction..."

                val finalPhotoPath: String? = if (!photoPath.isNullOrBlank()) {
                    val context = getApplication<Application>().applicationContext
                    val uri = Uri.parse(photoPath)

                    try {
                        uploadImageToSupabase(
                            context = context,
                            userId = userId,
                            imageUri = uri
                        )
                    } catch (uploadError: Exception) {
                        _uiMessage.value = "Image upload failed: ${uploadError.message}"
                        Log.e(TAG, "Stopping transaction save because image upload failed", uploadError)
                        return@launch
                    }
                } else {
                    null
                }

                Log.d(TAG, "Final photoPath before Firestore save: $finalPhotoPath")

                val now = System.currentTimeMillis()

                val expense = ExpenseEntity(
                    userId = userId,
                    amount = amount,
                    type = cleanType,
                    category = category.trim(),
                    subCategory = subCategory.trim(),
                    date = date.trim(),
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
                Log.e(TAG, "Failed to save transaction: ${e.message}", e)
                _uiMessage.value = "Failed to save transaction: ${e.message}"
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