package com.efthemiosprime.pasabayan

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.efthemiosprime.pasabayan.auth.AuthRoute
import com.efthemiosprime.pasabayan.auth.AuthScreen
import com.efthemiosprime.pasabayan.auth.AuthViewModel
import com.efthemiosprime.pasabayan.auth.AuthScreenState
import com.efthemiosprime.pasabayan.auth.FacebookLoginStarter
import com.efthemiosprime.pasabayan.auth.SessionUiState
import com.efthemiosprime.pasabayan.core.designsystem.PasabayanTheme
import com.efthemiosprime.pasabayan.core.network.BuildConfig as NetworkBuildConfig
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
                val viewModel: AuthViewModel = hiltViewModel()
                val googleLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult(),
                ) { result ->
                    val data: Intent = result.data ?: return@rememberLauncherForActivityResult
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    viewModel.onGoogleSignInResult(task)
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthRoute(
                        viewModel = viewModel,
                        onLaunchGoogleSignIn = { googleLauncher.launch(viewModel.googleSignInIntent()) },
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

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    PasabayanTheme {
        AuthScreen(
            state = AuthScreenState(session = SessionUiState.SignedOut),
            apiBaseUrl = NetworkBuildConfig.API_BASE_URL,
            onSignInWithGoogle = {},
            onSignInWithFacebook = {},
            onLogout = {},
        )
    }
}
