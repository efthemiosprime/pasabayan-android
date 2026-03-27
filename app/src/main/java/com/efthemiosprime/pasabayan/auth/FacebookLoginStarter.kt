package com.efthemiosprime.pasabayan.auth

import android.app.Activity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facebook SDK login — forwards [CallbackManager] activity results from [Activity.onActivityResult].
 * Parity with pasabayan-android-develop / iOS [LoginManager].
 */
@Singleton
class FacebookLoginStarter @Inject constructor() {

    val callbackManager: CallbackManager = CallbackManager.Factory.create()

    private val loginManager: LoginManager = LoginManager.getInstance()

    private var tokenConsumer: ((Result<String>) -> Unit)? = null

    init {
        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val token = result.accessToken?.token
                    if (token.isNullOrBlank()) {
                        tokenConsumer?.invoke(Result.failure(IllegalStateException("No Facebook access token")))
                    } else {
                        tokenConsumer?.invoke(Result.success(token))
                    }
                    tokenConsumer = null
                }

                override fun onCancel() {
                    tokenConsumer?.invoke(Result.failure(FacebookLoginCancelledException()))
                    tokenConsumer = null
                }

                override fun onError(error: FacebookException) {
                    tokenConsumer?.invoke(Result.failure(error))
                    tokenConsumer = null
                }
            },
        )
    }

    /**
     * Shows the Facebook login UI. The result is delivered to [onFinished] on the main thread
     * (Facebook SDK thread — caller should post to UI if needed).
     */
    fun startLogin(activity: Activity, onFinished: (Result<String>) -> Unit) {
        tokenConsumer = onFinished
        loginManager.logInWithReadPermissions(activity, listOf("public_profile", "email"))
    }
}

class FacebookLoginCancelledException : Exception("Facebook sign-in cancelled")
