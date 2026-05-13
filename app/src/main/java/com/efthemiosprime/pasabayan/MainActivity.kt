package com.efthemiosprime.pasabayan

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.features.auth.viewmodel.AuthViewModel
import com.efthemiosprime.pasabayan.features.auth.services.FacebookLoginStarter
import com.efthemiosprime.pasabayan.features.notifications.model.PushNotificationRoute
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationDisplay
import com.efthemiosprime.pasabayan.features.notifications.services.NotificationRouter
import com.efthemiosprime.pasabayan.shared.root.AppEntryContent
import com.efthemiosprime.pasabayan.shared.root.RootViewModel
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.activity.ComponentActivity() {

    @Inject
    lateinit var facebookLoginStarter: FacebookLoginStarter

    @Inject
    lateinit var notificationRouter: NotificationRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // iOS parity: when the activity is launched from a push tap, the FCM data map travels
        // through `NotificationDisplay.EXTRA_NOTIFICATION_DATA`. Parse it once on cold start.
        handleNotificationDataExtra(intent)
        setContent {
            PasabayanTheme {
                val rootViewModel: RootViewModel = hiltViewModel()
                val authViewModel: AuthViewModel = hiltViewModel()
                val googleLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    val data: Intent = result.data ?: return@rememberLauncherForActivityResult
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    authViewModel.onGoogleSignInResult(task)
                }
                PScaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppEntryContent(
                        rootViewModel = rootViewModel,
                        authViewModel = authViewModel,
                        onLaunchGoogleSignIn = {
                            googleLauncher.launch(authViewModel.googleSignInIntent())
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    /**
     * Activity is `singleTop` (per [NotificationDisplay.post] launch flags) so subsequent push
     * taps deliver here instead of re-creating the activity. Re-emit the route on each tap.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationDataExtra(intent)
        handleStripeRedirect(intent)
    }

    /**
     * Stripe 3DS return path. The manifest declares an intent-filter for `pasabayan://stripe-redirect`
     * (see [AndroidManifest.xml]) so the OS routes the browser callback back to this activity. The
     * Stripe SDK's `PaymentLauncher` / `PaymentSheet.Configuration(returnUrl = …)` flows own the
     * substantive state-machine via [androidx.activity.result.contract.ActivityResultContracts],
     * which surface results to their composables independently of this method.
     *
     * This hook exists so a stray redirect (e.g. cold-start from a 3DS browser tab) doesn't bounce
     * silently — we clear the data so a configuration change doesn't re-fire and let the SDK
     * launchers complete naturally on resume.
     */
    private fun handleStripeRedirect(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != STRIPE_REDIRECT_SCHEME || uri.host != STRIPE_REDIRECT_HOST) return
        // Defensive clear; Stripe launchers don't read intent.data for callback completion.
        intent.data = null
    }

    private companion object {
        const val STRIPE_REDIRECT_SCHEME = "pasabayan"
        const val STRIPE_REDIRECT_HOST = "stripe-redirect"
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleNotificationDataExtra(intent: Intent?) {
        val raw = intent?.getSerializableExtra(NotificationDisplay.EXTRA_NOTIFICATION_DATA)
            as? HashMap<String, String> ?: return
        // Clear the extra so a configuration change / process death restore doesn't re-fire.
        intent.removeExtra(NotificationDisplay.EXTRA_NOTIFICATION_DATA)
        val route = PushNotificationRoute.fromPushData(raw)
        notificationRouter.emitFromPush(route)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookLoginStarter.callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
