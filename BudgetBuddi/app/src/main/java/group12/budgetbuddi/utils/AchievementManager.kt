package group12.budgetbuddi.utils

import android.util.Log
import group12.budgetbuddi.viewmodel.UserProfileViewModel

object AchievementManager {
    private const val TAG = "AchievementManager"

    lateinit var userProfileViewModel: UserProfileViewModel

    fun init(viewModel: UserProfileViewModel) {
        userProfileViewModel = viewModel
        Log.d(TAG, "UserProfileViewModel initialized successfully")
    }

    fun isInitialized(): Boolean = ::userProfileViewModel.isInitialized
}