package com.efthemiosprime.pasabayan.ui.screens.profile.components

/**
 * Dialog state management for ProfileScreen
 * Centralizes all dialog/sheet presentation states
 */
sealed class ProfileDialogState {
    object None : ProfileDialogState()
    
    // Common dialogs
    object EditProfile : ProfileDialogState()
    object HelpSupport : ProfileDialogState()
    object TermsPrivacy : ProfileDialogState()
    
    // Carrier-specific dialogs
    object VehicleInfo : ProfileDialogState()
    object DeliveryHistory : ProfileDialogState()
    object CarrierPaymentMethods : ProfileDialogState()
    object AvailabilitySettings : ProfileDialogState()
    object RoutePreferences : ProfileDialogState()
    object DriverDocuments : ProfileDialogState()
    
    // Shipper-specific dialogs
    object ShippingAddresses : ProfileDialogState()
    object OrderHistory : ProfileDialogState()
    object ShipperPaymentMethods : ProfileDialogState()
    object ActiveShipments : ProfileDialogState()
    object Notifications : ProfileDialogState()
    object ShippingPreferences : ProfileDialogState()
    object BillingHistory : ProfileDialogState()
    object BulkShippingTools : ProfileDialogState()
    
    // System dialogs
    object LogoutAlert : ProfileDialogState()
} 