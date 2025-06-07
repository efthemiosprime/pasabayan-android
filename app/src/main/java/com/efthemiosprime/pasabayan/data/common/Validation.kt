package com.efthemiosprime.pasabayan.data.common

/**
 * Functional validation utilities
 * Pure functions for data validation without side effects
 */
object Validation {
    
    // Email validation
    fun validateEmail(email: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (email.isBlank()) {
            errors.add("Email is required")
        } else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))) {
            errors.add("Invalid email format")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    // Phone number validation (Philippine format)
    fun validatePhoneNumber(phone: String): ValidationResult {
        val errors = mutableListOf<String>()
        val cleanPhone = phone.replace("[^\\d]".toRegex(), "")
        
        when {
            phone.isBlank() -> errors.add("Phone number is required")
            cleanPhone.length < 10 -> errors.add("Phone number must be at least 10 digits")
            cleanPhone.length > 13 -> errors.add("Phone number must be at most 13 digits")
            !cleanPhone.matches(Regex("^(09|639|\\+639)\\d{9}$")) -> {
                errors.add("Invalid Philippine phone number format")
            }
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    // Password validation
    fun validatePassword(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            password.isBlank() -> errors.add("Password is required")
            password.length < 8 -> errors.add("Password must be at least 8 characters")
            !password.any { it.isUpperCase() } -> errors.add("Password must contain at least one uppercase letter")
            !password.any { it.isLowerCase() } -> errors.add("Password must contain at least one lowercase letter")
            !password.any { it.isDigit() } -> errors.add("Password must contain at least one number")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    // Name validation
    fun validateName(name: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            name.isBlank() -> errors.add("Name is required")
            name.length < 2 -> errors.add("Name must be at least 2 characters")
            name.length > 50 -> errors.add("Name must be at most 50 characters")
            !name.matches(Regex("^[a-zA-Z\\s'-]+$")) -> errors.add("Name can only contain letters, spaces, hyphens, and apostrophes")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    // Required field validation
    fun validateRequired(value: String, fieldName: String): ValidationResult {
        return if (value.isBlank()) {
            ValidationResult.Invalid(listOf("$fieldName is required"))
        } else {
            ValidationResult.Valid
        }
    }
    
    // Numeric validation
    fun validatePositiveNumber(value: Double, fieldName: String): ValidationResult {
        return if (value <= 0) {
            ValidationResult.Invalid(listOf("$fieldName must be a positive number"))
        } else {
            ValidationResult.Valid
        }
    }
    
    // Date validation (ISO format)
    fun validateISODate(dateString: String, fieldName: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (dateString.isBlank()) {
            errors.add("$fieldName is required")
        } else {
            try {
                // Simple ISO date format validation
                if (!dateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z?$"))) {
                    errors.add("$fieldName must be in ISO format (YYYY-MM-DDTHH:mm:ssZ)")
                }
            } catch (e: Exception) {
                errors.add("Invalid $fieldName format")
            }
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    // Weight validation (kg)
    fun validateWeight(weight: Double): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            weight < 0 -> errors.add("Weight cannot be negative")
            weight > 1000 -> errors.add("Weight cannot exceed 1000kg")
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
    
    // Price validation (Canadian dollar)
    fun validatePrice(price: Double): ValidationResult {
        val errors = mutableListOf<String>()
        
        when {
            price < 0 -> errors.add("Price cannot be negative")
            price > 240 -> errors.add("Price cannot exceed $240") // Converted from ₱10,000 at 0.024 CAD/PHP
        }
        
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}

/**
 * Validation builder for combining multiple validations
 */
class ValidationBuilder {
    private val validations = mutableListOf<ValidationResult>()
    
    fun validate(validation: ValidationResult): ValidationBuilder {
        validations.add(validation)
        return this
    }
    
    fun build(): ValidationResult = validations.combine()
}

/**
 * DSL for validation
 */
fun validate(block: ValidationBuilder.() -> Unit): ValidationResult {
    return ValidationBuilder().apply(block).build()
} 