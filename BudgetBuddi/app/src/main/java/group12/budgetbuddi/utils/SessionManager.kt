package group12.budgetbuddi.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session")

class SessionManager(private val context: Context) {

    companion object {

        private val IS_LOGGED_IN = stringPreferencesKey("is_logged_in")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USERNAME = stringPreferencesKey("username")
    }

    suspend fun saveSession(email: String, username: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = "true"
            preferences[USER_EMAIL] = email
            preferences[USERNAME] = username
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] == "true"
        }
    }

    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL]
        }
    }

    fun getUsername(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME]
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}