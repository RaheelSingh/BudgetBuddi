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

    // Keep old method for backward compatibility
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 6
    }

    // Comprehensive password validation
    fun isValidPassword(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < 6) {
            errors.add("• Password must be at least 6 characters long")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("• Password must contain at least 1 uppercase letter (A-Z)")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("• Password must contain at least 1 lowercase letter (a-z)")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("• Password must contain at least 1 number (0-9)")
        }
        if (!password.any { it in "!@#$%^&*()_+-=[]{}|;':\",./<>?`~" }) {
            errors.add("• Password must contain at least 1 special character (!@#$%^&* etc.)")
        }

        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    // Get all errors as a list
    fun getPasswordErrors(password: String): List<String> {
        return isValidPassword(password).errors
    }

    // Get single combined error message
    fun getPasswordErrorMessage(password: String): String {
        val errors = getPasswordErrors(password)
        return if (errors.isEmpty()) "" else errors.joinToString("\n")
    }

    // Check password strength and return score (0-4)
    fun getPasswordStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it in "!@#$%^&*()_+-=[]{}|;':\",./<>?`~" }) score++
        return score
    }

    // Get password strength text
    fun getPasswordStrengthText(password: String): String {
        return when (getPasswordStrength(password)) {
            0, 1 -> "Weak - Very Poor"
            2 -> "Fair - Poor"
            3 -> "Good - Could be better"
            4 -> "Strong - Good password!"
            5 -> "Very Strong - Excellent!"
            else -> "Enter a password"
        }
    }

    // Get password strength color
    fun getPasswordStrengthColor(password: String): String {
        return when (getPasswordStrength(password)) {
            0, 1 -> "#D32F2F" // Red - Weak
            2 -> "#FF9800"    // Orange - Fair
            3 -> "#FFC107"    // Yellow - Good
            4 -> "#4CAF50"    // Green - Strong
            5 -> "#139A43"    // Dark Green - Very Strong
            else -> "#9E9E9E"  // Gray - Empty
        }
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
/**
 * references:
 *
 * Oracle (2026) 'MessageDigest - SHA-256 hashing', Java Platform SE Documentation.
 *    Available at: https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
 *    (Accessed: 29 April 2026).
 *
 * Oracle (2026) 'SecureRandom for cryptographic salt generation', Java Platform SE Documentation.
 *    Available at: https://docs.oracle.com/javase/8/docs/api/java/security/SecureRandom.html
 *    (Accessed: 29 April 2026).
 *
 * OWASP Foundation (2026) 'Password Storage Cheat Sheet', OWASP Cheat Sheet Series.
 *    Available at: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
 *    (Accessed: 29 April 2026).
 */