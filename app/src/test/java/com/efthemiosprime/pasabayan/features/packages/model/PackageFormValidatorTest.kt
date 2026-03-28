package com.efthemiosprime.pasabayan.features.packages.model

import org.junit.Assert.assertTrue
import org.junit.Test

class PackageFormValidatorTest {

    private fun validForm() = PackageFormState(
        pickupAddress = "123 Main St",
        pickupCity = "Toronto",
        pickupCountry = "Canada",
        deliveryAddress = "456 Oak Ave",
        deliveryCity = "Montreal",
        deliveryCountry = "Canada",
        packageWeightKg = 10.0,
        packageType = "electronics",
        urgencyLevel = "normal",
        pickupDatePreferred = "2026-04-05",
        deliveryDateNeeded = "2026-04-07",
    )

    @Test
    fun `valid form returns no errors`() {
        val errors = PackageFormValidator.validate(validForm())
        assertTrue("Expected no errors but got: $errors", errors.isEmpty())
    }

    @Test
    fun `empty pickup city returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(pickupCity = ""))
        assertTrue(errors.any { it is PackageValidationError.PickupCityRequired })
    }

    @Test
    fun `empty pickup country returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(pickupCountry = ""))
        assertTrue(errors.any { it is PackageValidationError.PickupCountryRequired })
    }

    @Test
    fun `empty delivery city returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(deliveryCity = ""))
        assertTrue(errors.any { it is PackageValidationError.DeliveryCityRequired })
    }

    @Test
    fun `empty delivery country returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(deliveryCountry = ""))
        assertTrue(errors.any { it is PackageValidationError.DeliveryCountryRequired })
    }

    @Test
    fun `null weight returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(packageWeightKg = null))
        assertTrue(errors.any { it is PackageValidationError.WeightRequired })
    }

    @Test
    fun `zero weight returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(packageWeightKg = 0.0))
        assertTrue(errors.any { it is PackageValidationError.WeightRequired })
    }

    @Test
    fun `negative weight returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(packageWeightKg = -1.0))
        assertTrue(errors.any { it is PackageValidationError.WeightRequired })
    }

    @Test
    fun `empty package type returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(packageType = ""))
        assertTrue(errors.any { it is PackageValidationError.PackageTypeRequired })
    }

    @Test
    fun `empty urgency level returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(urgencyLevel = ""))
        assertTrue(errors.any { it is PackageValidationError.UrgencyRequired })
    }

    @Test
    fun `empty pickup date returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(pickupDatePreferred = ""))
        assertTrue(errors.any { it is PackageValidationError.PickupDateRequired })
    }

    @Test
    fun `empty delivery date returns error`() {
        val errors = PackageFormValidator.validate(validForm().copy(deliveryDateNeeded = ""))
        assertTrue(errors.any { it is PackageValidationError.DeliveryDateRequired })
    }

    @Test
    fun `pickup address is optional`() {
        val errors = PackageFormValidator.validate(validForm().copy(pickupAddress = ""))
        assertTrue(errors.none { it is PackageValidationError.PickupCityRequired })
    }

    @Test
    fun `multiple errors returned at once`() {
        val errors = PackageFormValidator.validate(
            validForm().copy(
                pickupCity = "",
                deliveryCity = "",
                packageWeightKg = null,
                packageType = "",
            ),
        )
        assertTrue(errors.size >= 4)
    }
}
