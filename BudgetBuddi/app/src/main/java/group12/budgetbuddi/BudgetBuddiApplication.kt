package group12.budgetbuddi

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class BudgetBuddiApplication : Application() {

    companion object {
        private const val TAG = "BudgetBuddiApp"
    }

    override fun onCreate() {
        super.onCreate()

        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
        }
    }
}