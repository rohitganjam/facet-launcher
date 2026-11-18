package com.facetlauncher.app.domain

import android.app.WallpaperManager
import com.facetlauncher.app.data.WallpaperCopyRepository
import com.facetlauncher.app.data.model.OtherScreenPromptKind
import com.facetlauncher.app.data.model.ScreenCopy
import com.facetlauncher.app.data.model.ScreenSize
import com.facetlauncher.app.data.model.WallpaperCopyNeed
import com.facetlauncher.app.data.model.promptKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ObserveWallpaperScreensUseCaseTest {

    private val inner = ScreenSize(2208, 1840)
    private val outer = ScreenSize(1080, 2092)

    private class FakeCopies(screens: List<ScreenCopy>, var hinge: Boolean = false) : WallpaperCopyRepository(RuntimeEnvironment.getApplication(), mock(WallpaperManager::class.java)) {
        val screenList = MutableStateFlow(screens)
        val said = MutableStateFlow(emptySet<String>())
        override val screens: Flow<List<ScreenCopy>> get() = screenList
        override val dismissedPrompts: Flow<Set<String>> get() = said
        val showing = MutableStateFlow(ScreenSize(1080, 2092))
        override val currentScreen: Flow<ScreenSize> get() = showing
        override suspend fun dismissPrompt(key: String) { said.value += key }
        override fun maybeFoldable() = hinge
    }

    private suspend fun prompt(copies: FakeCopies) = ObserveWallpaperScreensUseCase(copies)().first().prompt

    @Test
    fun `with no copy anywhere nothing is suggested`() = runTest {
        // Given two screens seen and no copy
        val copies = FakeCopies(listOf(ScreenCopy(inner, false), ScreenCopy(outer, false)))

        // Then there is nothing to complete
        assertEquals(OtherScreenPromptKind.None, prompt(copies).kind)
    }

    @Test
    fun `a screen without a copy is suggested once another has one`() = runTest {
        // Given the outer screen has a copy and the inner one was seen without
        val copies = FakeCopies(listOf(ScreenCopy(inner, false), ScreenCopy(outer, true)))

        // Then the missing copy is suggested, for the inner screen only
        val result = prompt(copies)
        assertEquals(OtherScreenPromptKind.MissingCopy, result.kind)
        assertEquals(listOf(inner.promptKey), result.dismissKeys)
    }

    @Test
    fun `a screen that already has a copy is not suggested`() = runTest {
        // Given both screens have a copy
        val copies = FakeCopies(listOf(ScreenCopy(inner, true), ScreenCopy(outer, true)))

        // Then nothing is suggested
        assertEquals(OtherScreenPromptKind.None, prompt(copies).kind)
    }

    @Test
    fun `a screen the user said no to is not suggested again`() = runTest {
        // Given the inner screen lacks a copy but was dismissed
        val copies = FakeCopies(listOf(ScreenCopy(inner, false), ScreenCopy(outer, true)))
        copies.dismissPrompt(inner.promptKey)

        // Then nothing is suggested
        assertEquals(OtherScreenPromptKind.None, prompt(copies).kind)
    }

    @Test
    fun `a foldable seen on one screen is told to unfold it`() = runTest {
        // Given one screen with a copy on a phone with a hinge sensor
        val copies = FakeCopies(listOf(ScreenCopy(outer, true)), hinge = true)

        // Then the foldable hint shows
        val result = prompt(copies)
        assertEquals(OtherScreenPromptKind.FoldableUnseen, result.kind)
        assertEquals(listOf(WallpaperCopyRepository.FOLDABLE_PROMPT), result.dismissKeys)
    }

    @Test
    fun `a phone without a hinge sensor is never told about another screen`() = runTest {
        // Given one screen with a copy and no hinge sensor
        val copies = FakeCopies(listOf(ScreenCopy(outer, true)), hinge = false)

        // Then nothing is suggested
        assertEquals(OtherScreenPromptKind.None, prompt(copies).kind)
    }

    @Test
    fun `the foldable hint is not shown again once dismissed`() = runTest {
        // Given the hint was dismissed
        val copies = FakeCopies(listOf(ScreenCopy(outer, true)), hinge = true)
        copies.dismissPrompt(WallpaperCopyRepository.FOLDABLE_PROMPT)

        // Then nothing is suggested
        assertEquals(OtherScreenPromptKind.None, prompt(copies).kind)
    }

    @Test
    fun `the screens come back largest first`() = runTest {
        // Given two screens
        val copies = FakeCopies(listOf(ScreenCopy(inner, true), ScreenCopy(outer, true)))

        // Then both are listed in the order given by the repository
        assertEquals(listOf(inner, outer), ObserveWallpaperScreensUseCase(copies)().first().sizes)
    }

    private suspend fun need(copies: FakeCopies) = ObserveWallpaperScreensUseCase(copies).copyNeed().first()

    private suspend fun needing(copies: FakeCopies) = need(copies)?.view

    @Test
    fun `the screen showing is offered an update when another has the current copy`() = runTest {
        // Given the inner screen has the current copy and the outer, showing, has an out-of-date one
        val copies = FakeCopies(listOf(ScreenCopy(inner, true), ScreenCopy(outer, false, outdated = true)))

        // Then Home should offer an update for the outer screen
        assertEquals(outer, needing(copies))
    }

    @Test
    fun `a screen with a current copy of its own is not offered an update`() = runTest {
        // Given the showing screen has a current copy
        val copies = FakeCopies(listOf(ScreenCopy(inner, false, outdated = true), ScreenCopy(outer, true)))

        // Then there is nothing to offer
        assertEquals(null, needing(copies))
    }

    @Test
    fun `no update is offered when there is no copy anywhere`() = runTest {
        // Given no screen has a copy, current or not
        val copies = FakeCopies(listOf(ScreenCopy(inner, false), ScreenCopy(outer, false)))

        // Then there is nothing to offer
        assertEquals(null, needing(copies))
    }

    @Test
    fun `a wallpaper changed outside Facet makes every screen out of date`() = runTest {
        // Given both screens only hold out-of-date copies
        val copies = FakeCopies(listOf(ScreenCopy(inner, false, outdated = true), ScreenCopy(outer, false, outdated = true)))

        // Then the screen showing is offered an update
        assertEquals(outer, needing(copies))
    }

    @Test
    fun `a screen the user said no to is not offered an update`() = runTest {
        // Given an out-of-date outer screen the user declined
        val copies = FakeCopies(listOf(ScreenCopy(inner, true), ScreenCopy(outer, false, outdated = true)))
        copies.dismissPrompt(outer.promptKey)

        // Then it is not offered
        assertEquals(null, needing(copies))
    }

    @Test
    fun `the prompt says when the missing screen is the one showing`() = runTest {
        // Given the outer screen is showing and lacks a copy, and the inner one has it
        val copies = FakeCopies(listOf(ScreenCopy(inner, true), ScreenCopy(outer, false)))
        assertEquals(true, prompt(copies).thisScreen)

        // When the inner screen shows instead
        copies.showing.value = inner

        // Then the missing screen is the other one
        assertEquals(false, prompt(copies).thisScreen)
    }

    private val outerTurned = outer.turned

    @Test
    fun `a turned view without a capture is suggested when the screen's other orientation has one`() = runTest {
        // Given the phone has a portrait capture and its landscape view has none
        val copies = FakeCopies(listOf(ScreenCopy(outer, true), ScreenCopy(outerTurned, false, screen = outer)))

        // Then the missing view is suggested, as the other orientation of a screen that has a capture
        val result = prompt(copies)
        assertEquals(OtherScreenPromptKind.MissingCopy, result.kind)
        assertEquals(listOf(outerTurned.promptKey), result.dismissKeys)
        assertTrue(result.otherOrientation)
    }

    @Test
    fun `a missing screen is not described as an orientation`() = runTest {
        // Given the outer screen has a capture and the inner screen, in both orientations, has none
        val copies = FakeCopies(listOf(ScreenCopy(inner, false), ScreenCopy(inner.turned, false, screen = inner), ScreenCopy(outer, true), ScreenCopy(outerTurned, true, screen = outer)))

        // Then the suggestion is about the other screen
        assertFalse(prompt(copies).otherOrientation)
    }

    @Test
    fun `the view showing is offered a capture by orientation when the other orientation has one`() = runTest {
        // Given a portrait capture and the phone held sideways
        val copies = FakeCopies(listOf(ScreenCopy(outer, true), ScreenCopy(outerTurned, false, screen = outer)))
        copies.showing.value = outerTurned

        // Then Home offers the landscape view, saying the other orientation is covered
        assertEquals(WallpaperCopyNeed(outerTurned, otherOrientation = true), need(copies))
    }

    @Test
    fun `the view showing is offered a capture without the orientation note when its screen has none at all`() = runTest {
        // Given the inner screen has a capture and the outer, showing in portrait, has none in either orientation
        val copies = FakeCopies(listOf(ScreenCopy(inner, true), ScreenCopy(outer, false), ScreenCopy(outerTurned, false, screen = outer)))

        // Then Home offers the outer view, with no note about orientation
        assertEquals(WallpaperCopyNeed(outer, otherOrientation = false), need(copies))
    }

    @Test
    fun `an orientation the user said no to is not offered`() = runTest {
        // Given the landscape view lacks a capture but was dismissed
        val copies = FakeCopies(listOf(ScreenCopy(outer, true), ScreenCopy(outerTurned, false, screen = outer)))
        copies.showing.value = outerTurned
        copies.dismissPrompt(outerTurned.promptKey)

        // Then nothing is offered
        assertEquals(null, need(copies))
    }
}
