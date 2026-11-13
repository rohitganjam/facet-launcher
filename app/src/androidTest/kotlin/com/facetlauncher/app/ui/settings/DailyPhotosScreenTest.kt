package com.facetlauncher.app.ui.settings

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.facetlauncher.app.data.model.DailyPhoto
import com.facetlauncher.app.data.model.DailyPhotoSource
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DailyPhotosScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private class Calls {
        var picked: DailyPhoto? = null
        var source: DailyPhotoSource? = null
        var retried = 0
        var ownImage = 0
        var back = 0
    }

    private val photos = persistentListOf(
        DailyPhoto("North Window, Arches", "© lightphoto/Getty Image", "https://img.peapix.com/a_640.jpg", "https://img.peapix.com/a.jpg", "2026-11-12"),
        DailyPhoto("Braided glacial rivers", "© Someone", "https://img.peapix.com/b_640.jpg", "https://img.peapix.com/b.jpg"),
    )

    private fun setContent(state: DailyPhotosUiState, calls: Calls) {
        composeRule.setContent {
            FacetLauncherTheme {
                DailyPhotosContent(
                    uiState = state,
                    loadThumbnail = { Bitmap.createBitmap(8, 6, Bitmap.Config.ARGB_8888) },
                    onBack = { calls.back++ },
                    onSelectSource = { calls.source = it },
                    onRetry = { calls.retried++ },
                    onPick = { calls.picked = it },
                    onChooseImage = { calls.ownImage++ },
                )
            }
        }
    }

    @Test
    fun `the photos show as tiles under a bing and a spotlight tab`() {
        // Given a week's photos
        setContent(DailyPhotosUiState(photos = photos, loading = false), Calls())

        // Then both tabs and both tiles show
        composeRule.onNodeWithTag("daily_source_bing").assertIsDisplayed()
        composeRule.onNodeWithTag("daily_source_spotlight").assertIsDisplayed()
        composeRule.onAllNodesWithTag("daily_photo_tile").assertCountEquals(2)
    }

    @Test
    fun `tapping a tile picks that photo and tapping a tab chooses that source`() {
        // Given photos
        val calls = Calls()
        setContent(DailyPhotosUiState(photos = photos, loading = false), calls)

        // When a tile and the Spotlight tab are tapped
        composeRule.onAllNodesWithTag("daily_photo_tile").onFirst().performClick()
        composeRule.onNodeWithTag("daily_source_spotlight").performClick()

        // Then the first photo is picked and Spotlight is chosen
        assertEquals("North Window, Arches", calls.picked?.title)
        assertEquals(DailyPhotoSource.SPOTLIGHT, calls.source)
    }

    @Test
    fun `loading shows a spinner and a failure shows a message that can be retried`() {
        // Given the screen still loading
        val calls = Calls()
        setContent(DailyPhotosUiState(loading = true), calls)
        composeRule.onNodeWithTag("daily_loading").assertIsDisplayed()
    }

    @Test
    fun `a failed load offers a retry`() {
        // Given nothing could be loaded
        val calls = Calls()
        setContent(DailyPhotosUiState(loading = false, failed = true), calls)

        // When retry is tapped
        composeRule.onNodeWithTag("daily_failed").assertIsDisplayed()
        composeRule.onNodeWithTag("daily_retry").performClick()

        // Then it reports once
        assertEquals(1, calls.retried)
    }

    @Test
    fun `photos saved earlier are marked and a download shows its overlay`() {
        // Given photos from an earlier day, while a photo downloads
        setContent(DailyPhotosUiState(photos = photos, loading = false, savedEarlier = true, downloading = true), Calls())

        // Then the note and the overlay show
        composeRule.onNodeWithTag("daily_saved_earlier").assertIsDisplayed()
        composeRule.onNodeWithTag("daily_downloading").assertIsDisplayed()
    }

    @Test
    fun `a failed download says so`() {
        setContent(DailyPhotosUiState(photos = photos, loading = false, downloadFailed = true), Calls())
        composeRule.onNodeWithTag("daily_download_failed").assertIsDisplayed()
    }

    @Test
    fun `at the foot there is a way to choose your own image`() {
        // Given a week of photos
        val calls = Calls()
        setContent(DailyPhotosUiState(photos = photos, loading = false), calls)

        // When the option at the bottom is reached and tapped
        composeRule.onNodeWithTag("daily_photos_screen").performScrollToNode(androidx.compose.ui.test.hasTestTag("daily_own_image_row"))
        composeRule.onNodeWithTag("daily_own_image_row").performClick()

        // Then the picker is asked for once
        assertEquals(1, calls.ownImage)
    }

    @Test
    fun `the own image option is there even when no photos could be loaded`() {
        // Given nothing could be loaded
        val calls = Calls()
        setContent(DailyPhotosUiState(loading = false, failed = true), calls)

        // Then choosing your own image still works
        composeRule.onNodeWithTag("daily_own_image_row").performClick()
        assertEquals(1, calls.ownImage)
    }
}
