package com.efthemiosprime.pasabayan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.efthemiosprime.pasabayan.data.model.PackageRequest
import com.efthemiosprime.pasabayan.data.model.PackageSize
import com.efthemiosprime.pasabayan.data.model.PackageRequestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

/**
 * PackageViewModel manages package requests and operations
 * Mirrors iOS PackageViewModel structure
 */
class PackageViewModel : ViewModel() {
    
    private val _packageRequests = MutableStateFlow<List<PackageRequest>>(emptyList())
    val packageRequests: StateFlow<List<PackageRequest>> = _packageRequests.asStateFlow()
    
    private val _myPackageRequests = MutableStateFlow<List<PackageRequest>>(emptyList())
    val myPackageRequests: StateFlow<List<PackageRequest>> = _myPackageRequests.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadMockData()
    }
    
    private fun loadMockData() {
        // Mock data similar to iOS
        val mockPackages = listOf(
            PackageRequest(
                id = 1,
                shipperId = 1,
                title = "Electronics Package",
                description = "Laptop and accessories",
                pickupLocation = "SM Mall of Asia",
                deliveryLocation = "Makati CBD",
                preferredPickupDate = "2024-01-15",
                preferredPickupTime = "10:00:00",
                packageSize = PackageSize.MEDIUM,
                packageWeight = 2.5,
                packageValue = 1080.0,
                isFragile = true,
                status = PackageRequestStatus.PENDING,
                createdAt = "2024-01-14T10:00:00Z",
                updatedAt = "2024-01-14T10:00:00Z"
            ),
            PackageRequest(
                id = 2,
                shipperId = 1,
                title = "Documents",
                description = "Important business documents",
                pickupLocation = "BGC Taguig",
                deliveryLocation = "Ortigas Center",
                preferredPickupDate = "2024-01-16",
                preferredPickupTime = "09:00:00",
                packageSize = PackageSize.SMALL,
                packageWeight = 0.5,
                packageValue = 24.0,
                isFragile = false,
                status = PackageRequestStatus.MATCHED,
                createdAt = "2024-01-14T11:00:00Z",
                updatedAt = "2024-01-14T11:00:00Z"
            ),
            PackageRequest(
                id = 3,
                shipperId = 1,
                title = "Gift Package",
                description = "Birthday gift for family",
                pickupLocation = "Quezon City",
                deliveryLocation = "Manila",
                preferredPickupDate = "2024-01-17",
                preferredPickupTime = "14:00:00",
                packageSize = PackageSize.LARGE,
                packageWeight = 5.0,
                packageValue = 192.0,
                isFragile = false,
                status = PackageRequestStatus.DELIVERED,
                createdAt = "2024-01-13T15:00:00Z",
                updatedAt = "2024-01-17T18:30:00Z"
            )
        )
        
        _myPackageRequests.value = mockPackages
        _packageRequests.value = mockPackages + generateOtherPackages()
    }
    
    private fun generateOtherPackages(): List<PackageRequest> {
        return listOf(
            PackageRequest(
                id = 4,
                shipperId = 2,
                title = "Food Delivery",
                description = "Fresh groceries",
                pickupLocation = "Greenhills",
                deliveryLocation = "San Juan",
                preferredPickupDate = "2024-01-15",
                preferredPickupTime = "08:00:00",
                packageSize = PackageSize.MEDIUM,
                packageWeight = 3.0,
                packageValue = 60.0, // Converted from ₱2,500 at 0.024 CAD/PHP
                isFragile = false,
                status = PackageRequestStatus.PENDING,
                createdAt = "2024-01-14T12:00:00Z",
                updatedAt = "2024-01-14T12:00:00Z"
            )
        )
    }
    
    /**
     * Get packages by status
     */
    fun getPackagesByStatus(status: PackageRequestStatus): List<PackageRequest> {
        return _myPackageRequests.value.filter { it.status == status }
    }
    
    fun getMatchedRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.MATCHED)
    fun getBookedRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.BOOKED)
    fun getInTransitRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.IN_TRANSIT)
    fun getDeliveredRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.DELIVERED)
    fun getPendingRequests(): List<PackageRequest> = getPackagesByStatus(PackageRequestStatus.PENDING)
    
    /**
     * Refresh data (simulate network call)
     */
    suspend fun refreshData() {
        _isLoading.value = true
        delay(1000) // Simulate network delay
        loadMockData()
        _isLoading.value = false
    }
} 