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
import com.efthemiosprime.pasabayan.auth.AuthViewModel
import com.efthemiosprime.pasabayan.auth.FacebookLoginStarter
import com.efthemiosprime.pasabayan.root.AppEntryContent
import com.efthemiosprime.pasabayan.root.RootViewModel
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.designsystem.component.PScaffold
import com.google.android.gms.auth.api.signin.GoogleSignIn
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.activity.ComponentActivity() {

    @Inject
    lateinit var facebookLoginStarter: FacebookLoginStarter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookLoginStarter.callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
