package com.efthemiosprime.pasabayan.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Google Sign-In configured with the **web client ID** from Firebase (same as pasabayan-android-develop).
 * Sends [com.google.android.gms.auth.api.signin.GoogleSignInAccount.getIdToken] to `/auth/google/login` as `access_token`.
 */
@Singleton
class GoogleSignInHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val client: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.efthemiosprime.pasabayan.R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val signInIntent get() = client.signInIntent

    /** Clears the Google account session on device (backend token is cleared separately). */
    fun signOutGoogle() {
        client.signOut()
    }
}
