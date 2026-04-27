package group12.budgetbuddi.utils

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random

object PasswordUtils {
    fun hashPassword(password: String, salt: String): String {
        val combined = password + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun createStoredHash(password: String): String {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        return "$salt:$hash"
    }

    fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val salt = parts[0]
        val hash = parts[1]
        val computedHash = hashPassword(inputPassword, salt)
        return computedHash == hash
    }

    fun generateResetToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    fun isStrongPassword(password: String): Boolean {
        return password.length >= 6
    }
}