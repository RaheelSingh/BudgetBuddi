package group12.budgetbuddi.utils

object PasswordUtils {

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    // Comprehensive password validation for registration
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

}

data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)