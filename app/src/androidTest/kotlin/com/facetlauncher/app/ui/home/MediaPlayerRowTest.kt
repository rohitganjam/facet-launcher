package com.facetlauncher.app.ui.home

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.facetlauncher.app.data.model.MediaPlayerState
import com.facetlauncher.app.data.model.MediaProgressStyle
import com.facetlauncher.app.ui.theme.FacetLauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MediaPlayerRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val track = MediaPlayerState(
        packageName = "com.example.music", title = "I Dont Care", artist = "Apocalyptica", art = null,
        isPlaying = true, positionMs = 0L, durationMs = 200_000L, positionUpdatedAtMs = 0L, playbackSpeed = 1f,
        canSkipPrevious = true, canSkipNext = true,
    )

    private fun setContent(
        state: MediaPlayerState = track,
        showSkipButtons: Boolean = true,
        progressStyle: MediaProgressStyle = MediaProgressStyle.FLAT,
        onPrevious: () -> Unit = {},
        onPlayPause: () -> Unit = {},
        onNext: () -> Unit = {},
        onOpen: () -> Unit = {},
    ) {
        composeRule.setContent {
            FacetLauncherTheme {
                MediaPlayerRow(
                    state = state,
                    showSkipButtons = showSkipButtons,
                    progressStyle = progressStyle,
                    onPrevious = onPrevious,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onOpen = onOpen,
                )
            }
        }
    }

    @Test
    fun showsTitleAndArtist() {
        // Given a playing track
        setContent()

        // Then both lines render
        composeRule.onNodeWithText("I Dont Care").assertExists()
        composeRule.onNodeWithText("Apocalyptica").assertExists()
    }

    @Test
    fun transportButtonsFireTheirCallbacks() {
        // Given a row wired to counters
        var previous = 0
        var playPause = 0
        var next = 0
        setContent(onPrevious = { previous++ }, onPlayPause = { playPause++ }, onNext = { next++ })

        // When each button is tapped
        composeRule.onNodeWithTag("home_media_previous").performClick()
        composeRule.onNodeWithTag("home_media_play_pause").performClick()
        composeRule.onNodeWithTag("home_media_next").performClick()

        // Then each callback fired exactly once
        assertEquals(1, previous)
        assertEquals(1, playPause)
        assertEquals(1, next)
    }

    @Test
    fun tappingTheArtOrTitleOpensThePlayer() {
        // Given a row wired to an open counter
        var opened = 0
        setContent(onOpen = { opened++ })

        // When the art and the title are tapped
        composeRule.onNodeWithTag("home_media_art").performClick()
        // (the title merges into its clickable column, so it only exists in the unmerged tree)
        composeRule.onNodeWithTag("home_media_title", useUnmergedTree = true).performClick()

        // Then the player opens each time
        assertEquals(2, opened)
    }

    @Test
    fun playingShowsPauseAndPausedShowsPlay() {
        // Given a playing track
        setContent(state = track.copy(isPlaying = true))

        // Then the control offers Pause, not Play
        composeRule.onNodeWithContentDescription("Pause").assertExists()
        composeRule.onNodeWithContentDescription("Play").assertDoesNotExist()
    }

    @Test
    fun pausedShowsPlay() {
        // Given a paused track
        setContent(state = track.copy(isPlaying = false))

        // Then the control offers Play
        composeRule.onNodeWithContentDescription("Play").assertExists()
    }

    @Test
    fun skipButtonsAreHiddenWhenTheSettingIsOff() {
        // Given skip buttons switched off
        setContent(showSkipButtons = false)

        // Then only play/pause remains
        composeRule.onNodeWithTag("home_media_previous").assertDoesNotExist()
        composeRule.onNodeWithTag("home_media_next").assertDoesNotExist()
        composeRule.onNodeWithTag("home_media_play_pause").assertExists()
    }

    @Test
    fun artIsASquareAsTallAsTheTextBlockNotAFullWidthTile() {
        // Given a row in a normal phone-width container
        setContent()

        // Then the art stays a small square matching the row height, instead of growing to the row's width
        val bounds = composeRule.onNodeWithTag("home_media_art").getUnclippedBoundsInRoot()
        assertTrue("art is ${bounds.height} tall", bounds.height < 100.dp)
        assertEquals(bounds.height.value, bounds.width.value, 1f)
    }

    @Test
    fun flatAndWavyProgressBothRenderForPlayingAndPausedTracks() {
        // Given the wavy style on a playing track
        setContent(progressStyle = MediaProgressStyle.WAVY)

        // Then the progress line renders
        composeRule.onNodeWithTag("home_media_progress").assertExists()
    }

    @Test
    fun wavyProgressStillRendersWhenPaused() {
        // Given the wavy style on a paused track (the wave goes flat but the line stays)
        setContent(state = track.copy(isPlaying = false), progressStyle = MediaProgressStyle.WAVY)

        // Then the progress line is still there
        composeRule.onNodeWithTag("home_media_progress").assertExists()
    }

    @Test
    fun skipButtonIsDisabledWhenTheSessionCannotSkipThatWay() {
        // Given a session that can't skip back
        setContent(state = track.copy(canSkipPrevious = false))

        // Then previous is disabled and next still works
        composeRule.onNodeWithTag("home_media_previous").assertIsNotEnabled()
        composeRule.onNodeWithTag("home_media_next").assertExists()
    }
}
