package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.ui.screens.EffectsStoreScreen
import com.example.ui.screens.ExportProgressScreen
import com.example.ui.screens.ExportSettingsScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MediaGalleryScreen
import com.example.ui.screens.NewProjectScreen
import com.example.ui.screens.ProfessionalEditorScreen
import com.example.ui.screens.ProjectManagementScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NovaObsidian
import com.example.viewmodel.NovaCutViewModel

class MainActivity : ComponentActivity() {
    private var pendingAppLinkUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == Intent.ACTION_VIEW && intent?.data != null) {
            pendingAppLinkUri = intent.data
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NovaObsidian
                ) {
                    NovaCutAppRoot(
                        incomingAppLinkUri = pendingAppLinkUri,
                        onAppLinkConsumed = { pendingAppLinkUri = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            pendingAppLinkUri = intent.data
        }
    }
}

@Composable
fun NovaCutAppRoot(
    viewModel: NovaCutViewModel = viewModel(),
    incomingAppLinkUri: Uri? = null,
    onAppLinkConsumed: () -> Unit = {}
) {
    LaunchedEffect(incomingAppLinkUri) {
        if (incomingAppLinkUri != null) {
            viewModel.handleIncomingAppLink(incomingAppLinkUri)
            onAppLinkConsumed()
        }
    }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    when (currentScreen) {
        AppScreen.SPLASH -> SplashScreen(
            onSkipToHome = { viewModel.navigateTo(AppScreen.HOME_DASHBOARD) }
        )
        AppScreen.HOME_DASHBOARD -> HomeDashboardScreen(viewModel = viewModel)
        AppScreen.NEW_PROJECT -> NewProjectScreen(viewModel = viewModel)
        AppScreen.MEDIA_GALLERY -> MediaGalleryScreen(viewModel = viewModel)
        AppScreen.VIDEO_EDITOR,
        AppScreen.AUDIO_LIBRARY,
        AppScreen.TEXT_STICKER_EDITOR,
        AppScreen.FILTERS_EFFECTS -> ProfessionalEditorScreen(viewModel = viewModel)
        AppScreen.EFFECTS_STORE -> EffectsStoreScreen(viewModel = viewModel)
        AppScreen.EXPORT_SETTINGS -> ExportSettingsScreen(viewModel = viewModel)
        AppScreen.EXPORT_PROGRESS -> ExportProgressScreen(viewModel = viewModel)
        AppScreen.PROJECT_MANAGEMENT -> ProjectManagementScreen(viewModel = viewModel)
    }
}
