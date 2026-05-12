package com.efthemiosprime.pasabayan.features.verification.model

import androidx.annotation.StringRes
import com.efthemiosprime.pasabayan.R

/**
 * Why a phone-verification prompt is being shown. Drives the body copy in
 * [com.efthemiosprime.pasabayan.features.verification.ui.VerifyPhonePromptSheet].
 */
enum class VerifyPhoneReason(@StringRes val bodyRes: Int) {
    CreatePackage(R.string.verification_gate_body_create_package),
    CreateTrip(R.string.verification_gate_body_create_trip),
    BookTrip(R.string.verification_gate_body_book_trip),
    RequestToCarry(R.string.verification_gate_body_request_to_carry),
}
