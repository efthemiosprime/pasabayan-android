package com.efthemiosprime.pasabayan.features.trips.model

import com.efthemiosprime.pasabayan.core.domain.`enum`.TransportationMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripFormValidatorTest {

    private fun validForm() = TripFormState(
        originCity = "Toronto",
        destinationCity = "Vancouver",
        pickupAddress = "123 Main St",
        dropoffAddress = "456 Oak Ave",
        weightCapacityKg = 25.0,
        spaceCapacityLiters = null,
        pricePerKg = 15.0,
        flatTripPrice = null,
        transportationMethod = TransportationMethod.FLIGHT,
        departureDateMillis = System.currentTimeMillis() + 600_000, // +10 min
        arrivalDateMillis = System.currentTimeMillis() + 4 * 3_600_000, // +4 hours
        specialNotes = null,
    )

    // -- Pickup / drop-off address — iOS parity (slice B) --

    @Test
    fun `empty pickup address returns PickupAddressRequired`() {
        val errors = TripFormValidator.validate(validForm().copy(pickupAddress = ""))
        assertTrue(errors.any { it is TripValidationError.PickupAddressRequired })
    }

    @Test
    fun `whitespace pickup address returns PickupAddressRequired`() {
        val errors = TripFormValidator.validate(validForm().copy(pickupAddress = "   "))
        assertTrue(errors.any { it is TripValidationError.PickupAddressRequired })
    }

    @Test
    fun `empty dropoff address returns DropoffAddressRequired`() {
        val errors = TripFormValidator.validate(validForm().copy(dropoffAddress = ""))
        assertTrue(errors.any { it is TripValidationError.DropoffAddressRequired })
    }

    @Test
    fun `whitespace dropoff address returns DropoffAddressRequired`() {
        val errors = TripFormValidator.validate(validForm().copy(dropoffAddress = " \t "))
        assertTrue(errors.any { it is TripValidationError.DropoffAddressRequired })
    }

    @Test
    fun `both addresses blank returns both errors independently`() {
        val errors = TripFormValidator.validate(
            validForm().copy(pickupAddress = "", dropoffAddress = ""),
        )
        assertTrue(errors.any { it is TripValidationError.PickupAddressRequired })
        assertTrue(errors.any { it is TripValidationError.DropoffAddressRequired })
    }

    @Test
    fun `valid form returns no errors`() {
        val errors = TripFormValidator.validate(validForm())
        assertTrue("Expected no errors but got: $errors", errors.isEmpty())
    }

    @Test
    fun `empty origin returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(originCity = ""))
        assertTrue(errors.any { it is TripValidationError.OriginRequired })
    }

    @Test
    fun `empty destination returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(destinationCity = ""))
        assertTrue(errors.any { it is TripValidationError.DestinationRequired })
    }

    @Test
    fun `same origin and destination returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(originCity = "Toronto", destinationCity = "Toronto"),
        )
        assertTrue(errors.any { it is TripValidationError.SameOriginDestination })
    }

    @Test
    fun `same origin and destination case-insensitive`() {
        val errors = TripFormValidator.validate(
            validForm().copy(originCity = "toronto", destinationCity = "TORONTO"),
        )
        assertTrue(errors.any { it is TripValidationError.SameOriginDestination })
    }

    @Test
    fun `zero weight returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(weightCapacityKg = 0.0))
        assertTrue(errors.any { it is TripValidationError.WeightRequired })
    }

    @Test
    fun `negative weight returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(weightCapacityKg = -5.0))
        assertTrue(errors.any { it is TripValidationError.WeightRequired })
    }

    @Test
    fun `null weight returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(weightCapacityKg = null))
        assertTrue(errors.any { it is TripValidationError.WeightRequired })
    }

    @Test
    fun `zero space returns error when provided`() {
        val errors = TripFormValidator.validate(validForm().copy(spaceCapacityLiters = 0.0))
        assertTrue(errors.any { it is TripValidationError.SpaceInvalid })
    }

    @Test
    fun `null space is ok`() {
        val errors = TripFormValidator.validate(validForm().copy(spaceCapacityLiters = null))
        assertTrue(errors.none { it is TripValidationError.SpaceInvalid })
    }

    @Test
    fun `flight with no pricePerKg returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(transportationMethod = TransportationMethod.FLIGHT, pricePerKg = null),
        )
        assertTrue(errors.any { it is TripValidationError.PriceRequired })
    }

    @Test
    fun `land transport with no flatTripPrice returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(
                transportationMethod = TransportationMethod.CAR,
                pricePerKg = null,
                flatTripPrice = null,
            ),
        )
        assertTrue(errors.any { it is TripValidationError.PriceRequired })
    }

    @Test
    fun `land transport with flatTripPrice is ok`() {
        val errors = TripFormValidator.validate(
            validForm().copy(
                transportationMethod = TransportationMethod.CAR,
                pricePerKg = null,
                flatTripPrice = 200.0,
            ),
        )
        assertTrue(errors.none { it is TripValidationError.PriceRequired })
    }

    @Test
    fun `transportation method NONE returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(transportationMethod = TransportationMethod.NONE),
        )
        assertTrue(errors.any { it is TripValidationError.TransportMethodRequired })
    }

    @Test
    fun `null departure returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(departureDateMillis = null))
        assertTrue(errors.any { it is TripValidationError.DepartureRequired })
    }

    @Test
    fun `departure in past returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(departureDateMillis = System.currentTimeMillis() - 60_000),
        )
        assertTrue(errors.any { it is TripValidationError.DepartureTooSoon })
    }

    @Test
    fun `departure less than 5 min from now returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(departureDateMillis = System.currentTimeMillis() + 60_000), // +1 min
        )
        assertTrue(errors.any { it is TripValidationError.DepartureTooSoon })
    }

    @Test
    fun `null arrival returns error`() {
        val errors = TripFormValidator.validate(validForm().copy(arrivalDateMillis = null))
        assertTrue(errors.any { it is TripValidationError.ArrivalRequired })
    }

    @Test
    fun `arrival before departure returns error`() {
        val departure = System.currentTimeMillis() + 600_000
        val errors = TripFormValidator.validate(
            validForm().copy(
                departureDateMillis = departure,
                arrivalDateMillis = departure - 60_000,
            ),
        )
        assertTrue(errors.any { it is TripValidationError.ArrivalBeforeDeparture })
    }

    @Test
    fun `flight duration less than 3 hours returns error`() {
        val departure = System.currentTimeMillis() + 600_000
        val errors = TripFormValidator.validate(
            validForm().copy(
                transportationMethod = TransportationMethod.FLIGHT,
                departureDateMillis = departure,
                arrivalDateMillis = departure + 2 * 3_600_000, // +2 hours
            ),
        )
        assertTrue(errors.any { it is TripValidationError.DurationTooShort })
    }

    @Test
    fun `land duration less than 20 min returns error`() {
        val departure = System.currentTimeMillis() + 600_000
        val errors = TripFormValidator.validate(
            validForm().copy(
                transportationMethod = TransportationMethod.CAR,
                flatTripPrice = 100.0,
                departureDateMillis = departure,
                arrivalDateMillis = departure + 10 * 60_000, // +10 min
            ),
        )
        assertTrue(errors.any { it is TripValidationError.DurationTooShort })
    }

    @Test
    fun `land duration of 20 min is ok`() {
        val departure = System.currentTimeMillis() + 600_000
        val errors = TripFormValidator.validate(
            validForm().copy(
                transportationMethod = TransportationMethod.CAR,
                flatTripPrice = 100.0,
                departureDateMillis = departure,
                arrivalDateMillis = departure + 20 * 60_000,
            ),
        )
        assertTrue(errors.none { it is TripValidationError.DurationTooShort })
    }

    @Test
    fun `special notes over 1000 chars returns error`() {
        val errors = TripFormValidator.validate(
            validForm().copy(specialNotes = "a".repeat(1001)),
        )
        assertTrue(errors.any { it is TripValidationError.NotesTooLong })
    }

    @Test
    fun `special notes at 1000 chars is ok`() {
        val errors = TripFormValidator.validate(
            validForm().copy(specialNotes = "a".repeat(1000)),
        )
        assertTrue(errors.none { it is TripValidationError.NotesTooLong })
    }

    @Test
    fun `multiple errors returned at once`() {
        val errors = TripFormValidator.validate(
            validForm().copy(
                originCity = "",
                destinationCity = "",
                weightCapacityKg = null,
                transportationMethod = TransportationMethod.NONE,
            ),
        )
        assertTrue(errors.size >= 3)
    }
}
