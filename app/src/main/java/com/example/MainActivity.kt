package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppScreen
import com.example.security.configureAppCheck
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NovaObsidian
                ) {
                    NovaCutAppRoot()
                }
            }
        }
    }
}

@Composable
fun NovaCutAppRoot(viewModel: NovaCutViewModel = viewModel()) {
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
        AppScreen.FILTERS_EFFECTS,
        AppScreen.AI_STUDIO -> ProfessionalEditorScreen(viewModel = viewModel)
        AppScreen.EFFECTS_STORE -> EffectsStoreScreen(viewModel = viewModel)
        AppScreen.EXPORT_SETTINGS -> ExportSettingsScreen(viewModel = viewModel)
        AppScreen.EXPORT_PROGRESS -> ExportProgressScreen(viewModel = viewModel)
        AppScreen.PROJECT_MANAGEMENT -> ProjectManagementScreen(viewModel = viewModel)
    }
}
