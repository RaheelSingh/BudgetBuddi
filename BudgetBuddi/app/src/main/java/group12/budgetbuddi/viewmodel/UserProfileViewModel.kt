package group12.budgetbuddi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import group12.budgetbuddi.data.AchievementsData
import group12.budgetbuddi.data.entity.AchievementEntity
import group12.budgetbuddi.data.entity.UserProfileEntity
import group12.budgetbuddi.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UserProfileEvent {
    data class AchievementUnlocked(
        val achievement: AchievementEntity,
        val oldLevel: Int,
        val newLevel: Int
    ) : UserProfileEvent()
}

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfileEntity? = null,
    val allAchievements: List<AchievementEntity> = AchievementsData.achievements,
    val unlockedAchievements: List<AchievementEntity> = emptyList(),
    val lockedAchievements: List<AchievementEntity> = emptyList(),
    val currentXP: Int = 0,
    val currentLevel: Int = 1,
    val levelTitle: String = "Novice Budgeter",
    val xpToNextLevel: Int = 100,
    val levelProgress: Float = 0f,
    val error: String? = null
)

class UserProfileViewModel : ViewModel() {

    private val repository = UserProfileRepository()

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UserProfileEvent>()
    val events: SharedFlow<UserProfileEvent> = _events.asSharedFlow()

    private var currentUserId: String? = null

    fun setUserId(userId: String) {
        currentUserId = userId
        observeUserProfile(userId)
    }

    private fun observeUserProfile(userId: String) {
        viewModelScope.launch {
            repository.observeUserProfile(userId).collect { profile ->
                if (profile != null) {
                    updateUiState(profile)
                }
            }
        }
    }

    private fun updateUiState(profile: UserProfileEntity) {
        val unlockedIds = profile.unlockedBadges.toSet()
        val unlocked = AchievementsData.achievements.filter { unlockedIds.contains(it.id) }
        val locked = AchievementsData.achievements.filter { !unlockedIds.contains(it.id) }

        val currentXP = profile.xp
        val currentLevel = profile.level
        val xpToNext = AchievementsData.getXpToNextLevel(currentXP, currentLevel)
        val progress = AchievementsData.getLevelProgress(currentXP, currentLevel)

        _uiState.update {
            it.copy(
                userProfile = profile,
                unlockedAchievements = unlocked,
                lockedAchievements = locked,
                currentXP = currentXP,
                currentLevel = currentLevel,
                levelTitle = profile.levelTitle,
                xpToNextLevel = xpToNext,
                levelProgress = progress
            )
        }
    }

    suspend fun checkAndHandleNewAchievements(): AchievementEntity? {
        val userId = currentUserId ?: return null

        val snapshot = repository.userDocument(userId).get().await()
        val oldLevel = snapshot.getLong("level")?.toInt() ?: 1

        val newAchievement = repository.checkAndUnlockAchievements(userId)

        if (newAchievement != null) {
            val updatedSnapshot = repository.userDocument(userId).get().await()
            val newLevel = updatedSnapshot.getLong("level")?.toInt() ?: oldLevel

            _events.emit(UserProfileEvent.AchievementUnlocked(newAchievement, oldLevel, newLevel))
        }

        return newAchievement
    }

    fun updateLoginStreak() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.updateLoginStreak(userId)
        }
    }

    // ===== ADD THIS FUNCTION FOR AVATAR UPDATES =====
    fun updateAvatar(badgeId: String?) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.updateUserAvatar(userId, badgeId)
        }
    }
    // ===== END OF ADDED FUNCTION =====

    fun onExpenseAdded(hasPhoto: Boolean = false) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.incrementCounter(userId, "totalExpenses", hasPhoto)
            checkAndHandleNewAchievements()
        }
    }

    fun onIncomeAdded() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.incrementCounter(userId, "totalIncome")
            checkAndHandleNewAchievements()
        }
    }

    fun onBudgetGoalCreated() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.incrementCounter(userId, "totalBudgetGoals")
            checkAndHandleNewAchievements()
        }
    }

    fun onCategoryCreated() {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            repository.incrementCounter(userId, "totalCategoriesCreated")
            checkAndHandleNewAchievements()
        }
    }
}