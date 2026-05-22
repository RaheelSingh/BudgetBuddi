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
import group12.budgetbuddi.utils.SessionManager
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExpenseRepository()


    private val sessionManager = SessionManager(application)
    private val auth = FirebaseAuth.getInstance()

    val currentUserEmail: String
        get() = auth.currentUser?.email
            ?: sessionManager.getUserEmail()
            ?: "guest"

    companion object {
        private const val TAG = "ExpenseViewModel"
        private const val BUCKET_NAME = "expense-receipts"
    }

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _isUploadingImage = MutableStateFlow(false)
    val isUploadingImage: StateFlow<Boolean> = _isUploadingImage

    val allExpenses: StateFlow<List<ExpenseEntity>> =
        repository.getAllExpenses(currentUserEmail)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesOnly: StateFlow<List<ExpenseEntity>> =
        repository.getExpensesOnly(currentUserEmail)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    /**
     * Uploads an image URI to Supabase storage.
     * Returns the public URL on success, or null on failure.
     * Handles both content:// URIs (gallery) and file:// URIs (camera).
     */
    private suspend fun uploadImageToSupabase(context: Context, imageUri: Uri): String? {
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

            // Folder per user, unique filename per upload
            val safeEmail = currentUserEmail
                .replace("@", "_at_")
                .replace(".", "_")
            val filename = "receipt_${System.currentTimeMillis()}.$extension"
            val filePath = "$safeEmail/$filename"

            val bucket = SupabaseClient.client.storage.from(BUCKET_NAME)

            bucket.upload(
                path = filePath,
                data = imageBytes
            ) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }

            // publicUrl() works for public buckets — change to createSignedUrl() for private later
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

    /**
     * Saves a transaction to Firestore.
     * photoPath here is a local file path or content URI string from the screen.
     * If a photoPath is provided, it will be uploaded to Supabase first.
     * The stored photoPath in Firestore will be the Supabase URL.
     */
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
                // If there's a local photo, upload it to Supabase and get back the URL
                val finalPhotoPath: String? = if (!photoPath.isNullOrBlank()) {
                    val context = getApplication<Application>().applicationContext
                    val uri = Uri.parse(photoPath)
                    uploadImageToSupabase(context, uri)
                    // If upload fails, uploadImageToSupabase returns null and sets uiMessage
                    // We still proceed with saving the expense (just without a photo)
                } else {
                    null
                }

                val expense = ExpenseEntity(
                    userEmail = currentUserEmail,
                    amount = amount,
                    type = type,
                    category = category,
                    subCategory = subCategory,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    note = note,
                    photoPath = finalPhotoPath,   // Supabase URL or null
                    paymentMethod = paymentMethod,
                    isRecurring = isRecurring
                )

                repository.insertExpense(expense)
                _uiMessage.value = "Saved successfully"
                _saveSuccess.value = true

            } catch (e: Exception) {
                Log.e(TAG, "Failed to save expense: ${e.message}", e)
                _uiMessage.value = "Failed to save: ${e.message}"
            }
        }
    }


    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
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