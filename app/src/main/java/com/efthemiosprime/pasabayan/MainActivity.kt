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
