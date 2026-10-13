package com.facetlauncher.app.data

import android.media.MediaMetadata
import android.media.session.PlaybackState
import com.facetlauncher.app.data.model.MediaPlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaSessionMappingTest {

    private fun metadata(title: String? = "Song", artist: String? = "Artist", durationMs: Long = 200_000L) =
        MediaMetadata.Builder().apply {
            title?.let { putString(MediaMetadata.METADATA_KEY_TITLE, it) }
            artist?.let { putString(MediaMetadata.METADATA_KEY_ARTIST, it) }
            putLong(MediaMetadata.METADATA_KEY_DURATION, durationMs)
        }.build()

    private fun playback(state: Int, positionMs: Long = 50_000L, speed: Float = 1f, actions: Long = 0L) =
        PlaybackState.Builder()
            .setState(state, positionMs, speed)
            .setActions(actions)
            .build()

    private fun state(isPlaying: Boolean, packageName: String = "pkg") = MediaPlayerState(
        packageName = packageName, title = "t", artist = null, art = null, isPlaying = isPlaying,
        positionMs = 0L, durationMs = 0L, positionUpdatedAtMs = 0L, playbackSpeed = 1f,
        canSkipPrevious = false, canSkipNext = false,
    )

    @Test
    fun `maps title artist and playing state from a playing session`() {
        // Given a playing session with title and artist
        // When mapped
        val result = buildMediaPlayerState("com.example", metadata(), playback(PlaybackState.STATE_PLAYING))

        // Then the fields carry over
        assertEquals("Song", result?.title)
        assertEquals("Artist", result?.artist)
        assertEquals("com.example", result?.packageName)
        assertTrue(result!!.isPlaying)
        assertEquals(200_000L, result.durationMs)
    }

    @Test
    fun `a paused session is kept but not playing`() {
        // When a paused session is mapped
        val result = buildMediaPlayerState("p", metadata(), playback(PlaybackState.STATE_PAUSED, speed = 0f))

        // Then it still produces a state, marked paused
        assertFalse(result!!.isPlaying)
    }

    @Test
    fun `a stopped or errored session maps to null`() {
        // When a stopped / errored / none session is mapped
        // Then Home shows nothing for it
        assertNull(buildMediaPlayerState("p", metadata(), playback(PlaybackState.STATE_STOPPED)))
        assertNull(buildMediaPlayerState("p", metadata(), playback(PlaybackState.STATE_ERROR)))
        assertNull(buildMediaPlayerState("p", metadata(), playback(PlaybackState.STATE_NONE)))
    }

    @Test
    fun `a session without a title maps to null`() {
        // When metadata has no usable title
        // Then there's nothing to show
        assertNull(buildMediaPlayerState("p", metadata(title = null), playback(PlaybackState.STATE_PLAYING)))
        assertNull(buildMediaPlayerState("p", metadata(title = "  "), playback(PlaybackState.STATE_PLAYING)))
    }

    @Test
    fun `skip availability follows the session's advertised actions`() {
        // Given a session that only supports skip to next
        val result = buildMediaPlayerState("p", metadata(), playback(PlaybackState.STATE_PLAYING, actions = PlaybackState.ACTION_SKIP_TO_NEXT))

        // Then previous is unavailable and next is available
        assertFalse(result!!.canSkipPrevious)
        assertTrue(result.canSkipNext)
    }

    @Test
    fun `a playing session reporting zero speed advances at normal speed`() {
        // When a playing session reports speed 0 (some players do)
        val result = buildMediaPlayerState("p", metadata(), playback(PlaybackState.STATE_PLAYING, speed = 0f))

        // Then progress still advances
        assertEquals(1f, result!!.playbackSpeed)
    }

    @Test
    fun `pickActiveSession prefers a playing session over an earlier paused one`() {
        // Given a paused session listed before a playing one
        val paused = state(isPlaying = false, packageName = "a")
        val playing = state(isPlaying = true, packageName = "b")

        // Then the playing one wins
        assertSame(playing, pickActiveSession(listOf(paused, playing)))
    }

    @Test
    fun `pickActiveSession falls back to the first when none are playing`() {
        val first = state(isPlaying = false, packageName = "a")
        val second = state(isPlaying = false, packageName = "b")

        assertSame(first, pickActiveSession(listOf(first, second)))
        assertNull(pickActiveSession(emptyList()))
    }

    @Test
    fun `progress advances with elapsed time while playing and holds while paused`() {
        // Given a track 100s long, reported at 20s at t=1000
        val playing = state(isPlaying = true).copy(positionMs = 20_000L, durationMs = 100_000L, positionUpdatedAtMs = 1_000L)

        // Then 10s later it's at 30%, and a paused one holds at 20%
        assertEquals(0.3f, playing.progressAt(11_000L)!!, 0.001f)
        assertEquals(0.2f, playing.copy(isPlaying = false).progressAt(11_000L)!!, 0.001f)
    }

    @Test
    fun `progress is null when the duration is unknown and clamps at one`() {
        // Given a live stream with no duration
        assertNull(state(isPlaying = true).progressAt(5_000L))

        // And a position past the end
        val overrun = state(isPlaying = true).copy(positionMs = 99_000L, durationMs = 100_000L, positionUpdatedAtMs = 0L)
        assertEquals(1f, overrun.progressAt(60_000L)!!, 0.001f)
    }

    @Test
    fun `with nothing playing the most recently played session wins`() {
        // Given two paused sessions where b played after a
        val a = state(isPlaying = false, packageName = "a")
        val b = state(isPlaying = false, packageName = "b")

        // Then b is picked, whatever the list order
        assertSame(b, pickActiveSession(listOf(a, b), lastPlayedAt = mapOf("a" to 1L, "b" to 2L)))
    }

    @Test
    fun `a playing session beats a more recently played paused one`() {
        val playing = state(isPlaying = true, packageName = "a")
        val paused = state(isPlaying = false, packageName = "b")

        assertSame(playing, pickActiveSession(listOf(paused, playing), lastPlayedAt = mapOf("a" to 1L, "b" to 2L)))
    }

    @Test
    fun `sessions never seen playing keep the platform order`() {
        val first = state(isPlaying = false, packageName = "a")
        val second = state(isPlaying = false, packageName = "b")

        assertSame(first, pickActiveSession(listOf(first, second)))
    }

    @Test
    fun `a MediaStyle template marks a notification as media`() {
        val extras = android.os.Bundle().apply { putString(android.app.Notification.EXTRA_TEMPLATE, "android.app.Notification\$MediaStyle") }

        assertTrue(isMediaNotification(extras))
    }

    @Test
    fun `a media session token marks a notification as media`() {
        val extras = android.os.Bundle().apply { putParcelable(android.app.Notification.EXTRA_MEDIA_SESSION, android.os.Bundle()) }

        assertTrue(isMediaNotification(extras))
    }

    @Test
    fun `an ordinary notification is not media`() {
        val extras = android.os.Bundle().apply { putString(android.app.Notification.EXTRA_TEMPLATE, "android.app.Notification\$BigTextStyle") }

        assertFalse(isMediaNotification(extras))
        assertFalse(isMediaNotification(null))
    }

    @Test
    fun `the picker remembers which session played last once both are paused`() {
        val picker = ActiveSessionPicker()
        val a = state(isPlaying = true, packageName = "a")
        val b = state(isPlaying = true, packageName = "b")
        val both = setOf("a", "b")

        // Given a played, then b played
        picker.pick(listOf("ca" to a), both)
        picker.pick(listOf("cb" to b), both)

        // When both are paused (a listed first)
        val result = picker.pick(listOf("ca" to a.copy(isPlaying = false), "cb" to b.copy(isPlaying = false)), both)

        // Then the one that played last wins
        assertEquals("cb", result?.first)
    }

    @Test
    fun `the picker ignores sessions without a media notification`() {
        val picker = ActiveSessionPicker()
        val playing = state(isPlaying = true, packageName = "a")

        assertNull(picker.pick(listOf("ca" to playing), packagesWithNotification = emptySet()))
    }
}
