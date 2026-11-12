package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.media.VideoExportEngine
import com.example.model.ExportBitratePreset
import com.example.model.ExportResolution
import com.example.model.FilterPreset
import com.example.ui.components.buildFilterPresetMedia3Effects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun novacut_launchesSplashAndNavigatesToDashboardAndEditor() {
        // Verify Splash Screen renders and can enter Home Dashboard
        composeTestRule.onNodeWithTag("splash_enter_studio_button").performClick()
        composeTestRule.waitForIdle()

        // Verify Home Dashboard New Project CTA exists
        composeTestRule.onNodeWithTag("home_new_project_button").assertExists()

        // Navigate directly to Active Multi-Track Editor
        composeTestRule.onNodeWithTag("open_active_editor_chip").performClick()
        composeTestRule.waitForIdle()

        // Verify Upper Video Preview Stage, Media3 VideoPlayer component & Multi-Track Timeline exist
        composeTestRule.onNodeWithTag("video_preview_stage").assertExists()
        composeTestRule.onNodeWithTag("media3_timeline_video_player").assertExists()
        composeTestRule.onNodeWithTag("multi_track_timeline").assertExists()

        // Open the Video Filters Bottom Sheet via the Filters tab
        composeTestRule.onNodeWithTag("editor_tab_FILTERS").performClick()
        composeTestRule.waitForIdle()

        // Verify the Bottom Sheet and selectable grid of video filters render
        composeTestRule.onNodeWithTag("video_filters_bottom_sheet").assertExists()
        composeTestRule.onNodeWithTag("video_filters_grid").assertExists()
        composeTestRule.onNodeWithTag("filter_grid_item_TEAL_ORANGE").performClick()
        composeTestRule.waitForIdle()

        // Verify Media3 Effect transformations are constructed for FilterPreset
        val tealEffects = buildFilterPresetMedia3Effects(
            preset = FilterPreset.TEAL_ORANGE,
            intensity = 1.0f
        )
        assertTrue(tealEffects.isNotEmpty())
    }

    @Test
    fun novacut_opensExportSettingsBottomSheetAndConfiguresMedia3Transformer() {
        composeTestRule.onNodeWithTag("splash_enter_studio_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("open_active_editor_chip").performClick()
        composeTestRule.waitForIdle()

        // Open Export Settings Bottom Sheet from editor Export button
        composeTestRule.onNodeWithTag("editor_export_button").performClick()
        composeTestRule.waitForIdle()

        // Verify Export Settings Bottom Sheet & controls render
        composeTestRule.onNodeWithTag("export_settings_bottom_sheet").assertExists()
        composeTestRule.onNodeWithTag("export_sheet_res_RES_1080P").performClick()
        composeTestRule.onNodeWithTag("export_sheet_fps_60").performClick()
        composeTestRule.onNodeWithTag("export_sheet_bitrate_CINEMA_MASTER").performClick()
        composeTestRule.onNodeWithTag("export_sheet_render_button").assertExists()
        composeTestRule.waitForIdle()

        // Verify Media3 VideoEncoderSettings reflects selected resolution & bitrate
        val encoderSettings = VideoExportEngine.buildVideoEncoderSettings(
            resolution = ExportResolution.RES_1080P,
            bitratePreset = ExportBitratePreset.CINEMA_MASTER
        )
        assertEquals(22_400_000, encoderSettings.bitrate)
    }
}

