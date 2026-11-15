package com.facetlauncher.app.ui.vault

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.facetlauncher.app.data.AppRepository
import com.facetlauncher.app.data.AppVaultRepository
import com.facetlauncher.app.data.FakePinHasher
import com.facetlauncher.app.data.FolderRepository
import com.facetlauncher.app.data.LaunchHistoryRepository
import com.facetlauncher.app.data.MutableTestClock
import com.facetlauncher.app.data.PlacementRepository
import com.facetlauncher.app.data.model.AppInfo
import com.facetlauncher.app.data.model.VAULT_FREE_ATTEMPTS
import com.facetlauncher.app.data.model.vaultKey
import com.facetlauncher.app.domain.GetInstalledAppsUseCase
import com.facetlauncher.app.domain.MoveToVaultUseCase
import com.facetlauncher.app.domain.SortAppsForPickerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner

/** [RobolectricTestRunner] because [AppInfo]'s default `userHandle` calls `Process.myUserHandle()`. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppVaultViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun app(name: String) = AppInfo(packageName = "com.example.$name", activityName = ".Main", label = name, icon = null)

    private class Fixture(
        val vault: AppVaultRepository,
        val appRepository: AppRepository,
        val placements: PlacementRepository,
        val folders: FolderRepository,
        val viewModel: AppVaultViewModel,
    )

    private fun TestScope.fixture(installed: List<AppInfo> = emptyList(), clock: MutableTestClock = MutableTestClock()): Fixture {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope, produceFile = { tempFolder.newFile("v-${System.nanoTime()}.preferences_pb") })
        val vault = AppVaultRepository(store, FakePinHasher(), clock, dispatcher)
        val appRepository = mock(AppRepository::class.java)
        `when`(appRepository.observeVaultedApps()).thenReturn(
            combine(MutableStateFlow(installed), vault.vaultedKeys) { apps, keys -> apps.filter { it.vaultKey in keys } },
        )
        val getInstalled = mock(GetInstalledAppsUseCase::class.java)
        // Like the real one: the vault's own apps are already filtered out of what pickers see.
        `when`(getInstalled.observe()).thenReturn(combine(MutableStateFlow(installed), vault.vaultedKeys) { apps, keys -> apps.filter { it.vaultKey !in keys } })
        val placements = mock(PlacementRepository::class.java)
        val folders = mock(FolderRepository::class.java)
        installed.forEach {
            `when`(placements.countByPackage(it.packageName, it.userHandle.hashCode())).thenReturn(0)
            `when`(folders.countByPackage(it.packageName, it.userHandle.hashCode())).thenReturn(0)
        }
        val moveToVault = MoveToVaultUseCase(vault, placements, folders, mock(LaunchHistoryRepository::class.java))
        val viewModel = AppVaultViewModel(vault, appRepository, getInstalled, moveToVault, SortAppsForPickerUseCase(mock(LaunchHistoryRepository::class.java)))
        backgroundScope.launch { viewModel.uiState.collect { } }
        return Fixture(vault, appRepository, placements, folders, viewModel)
    }

    private fun TestScope.type(viewModel: AppVaultViewModel, pin: String) {
        pin.forEach { viewModel.onDigit(it.digitToInt()) }
        runCurrent()
    }

    private fun TestScope.setPinThrough(f: Fixture, pin: String = "1234") {
        f.viewModel.onOpened()
        runCurrent()
        f.viewModel.onStartSetPin()
        type(f.viewModel, pin)
        type(f.viewModel, pin)
    }

    @Test
    fun `a closed vault shows nothing and a vault with no PIN opens on the intro`() = runTest(dispatcher) {
        val f = fixture()
        assertEquals(VaultStage.LOCKED, f.viewModel.uiState.value.stage)

        f.viewModel.onOpened()
        runCurrent()

        assertEquals(VaultStage.INTRO, f.viewModel.uiState.value.stage)
    }

    @Test
    fun `setting a PIN asks twice and then lands in the empty vault`() = runTest(dispatcher) {
        // Given the intro
        val f = fixture()
        f.viewModel.onOpened()
        runCurrent()

        // When a PIN is typed and typed again
        f.viewModel.onStartSetPin()
        type(f.viewModel, "1234")
        assertEquals(VaultStage.CONFIRM_PIN, f.viewModel.uiState.value.stage)
        assertEquals(0, f.viewModel.uiState.value.digitsEntered)
        type(f.viewModel, "1234")

        // Then the PIN is stored and the vault list shows
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)
        assertTrue(f.vault.hasPinNow())
    }

    @Test
    fun `a confirmation that does not match goes back to choosing a PIN and stores nothing`() = runTest(dispatcher) {
        val f = fixture()
        f.viewModel.onOpened()
        runCurrent()
        f.viewModel.onStartSetPin()
        type(f.viewModel, "1234")

        type(f.viewModel, "1235")

        assertEquals(VaultStage.SET_PIN, f.viewModel.uiState.value.stage)
        assertEquals(PinFeedback.MISMATCH, f.viewModel.uiState.value.feedback)
        assertFalse(f.vault.hasPinNow())
    }

    @Test
    fun `digits beyond four are ignored and the state only ever carries a count`() = runTest(dispatcher) {
        val f = fixture()
        f.viewModel.onOpened()
        runCurrent()
        f.viewModel.onStartSetPin()

        f.viewModel.onDigit(1)
        f.viewModel.onDigit(2)
        runCurrent()
        assertEquals(2, f.viewModel.uiState.value.digitsEntered)
        f.viewModel.onBackspace()
        runCurrent()
        assertEquals(1, f.viewModel.uiState.value.digitsEntered)
    }

    @Test
    fun `with a PIN set the vault opens on the PIN pad and the right PIN unlocks it`() = runTest(dispatcher) {
        // Given a vault with a PIN, closed again
        val f = fixture()
        setPinThrough(f)
        f.viewModel.lock()
        assertEquals(VaultStage.LOCKED, f.viewModel.uiState.value.stage)

        // When it is opened and the PIN typed
        f.viewModel.onOpened()
        runCurrent()
        assertEquals(VaultStage.UNLOCK, f.viewModel.uiState.value.stage)
        type(f.viewModel, "1234")

        // Then the list shows
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)
    }

    @Test
    fun `a wrong PIN says so and counts attempts, and the fifth locks the pad with a countdown`() = runTest(dispatcher) {
        val f = fixture()
        setPinThrough(f)
        f.viewModel.lock()
        f.viewModel.onOpened()
        runCurrent()

        type(f.viewModel, "0000")
        assertEquals(PinFeedback.WRONG, f.viewModel.uiState.value.feedback)
        assertEquals(VAULT_FREE_ATTEMPTS - 1, f.viewModel.uiState.value.attemptsLeft)

        repeat(VAULT_FREE_ATTEMPTS - 1) { type(f.viewModel, "0000") }

        assertEquals(PinFeedback.LOCKED_OUT, f.viewModel.uiState.value.feedback)
        assertEquals(30, f.viewModel.uiState.value.lockoutSecondsLeft)

        // And typing is ignored while it runs
        type(f.viewModel, "1234")
        assertEquals(VaultStage.UNLOCK, f.viewModel.uiState.value.stage)
    }

    @Test
    fun `closing the vault forgets where it was`() = runTest(dispatcher) {
        val f = fixture()
        setPinThrough(f)
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)

        f.viewModel.lock()
        runCurrent()

        assertEquals(VaultStage.LOCKED, f.viewModel.uiState.value.stage)
    }

    @Test
    fun `adding apps from the picker vaults them and lists them A to Z`() = runTest(dispatcher) {
        // Given an unlocked vault and three installed apps, none on Home
        val maps = app("maps")
        val chat = app("chat")
        val notes = app("notes")
        val f = fixture(listOf(maps, chat, notes))
        setPinThrough(f)

        // When maps and chat are ticked in the picker and Done is pressed
        f.viewModel.onAddApps()
        runCurrent()
        assertEquals(3, f.viewModel.uiState.value.pickerResults.size)
        f.viewModel.onPickerToggle(maps)
        f.viewModel.onPickerToggle(chat)
        f.viewModel.onPickerDone()
        runCurrent()

        // Then they are in the vault, alphabetically, and the list shows
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)
        assertEquals(listOf("chat", "maps"), f.viewModel.uiState.value.apps.map { it.label })
        assertEquals(setOf(chat.vaultKey, maps.vaultKey), f.vault.vaultedKeys.first())
    }

    @Test
    fun `vaulting an app that is on Home asks first and does nothing until confirmed`() = runTest(dispatcher) {
        // Given chat is a Favorite
        val chat = app("chat")
        val f = fixture(listOf(chat))
        `when`(f.placements.countByPackage(chat.packageName, chat.userHandle.hashCode())).thenReturn(1)
        setPinThrough(f)
        f.viewModel.onAddApps()
        runCurrent()
        f.viewModel.onPickerToggle(chat)

        // When Done is pressed
        f.viewModel.onPickerDone()
        runCurrent()

        // Then it asks, and the vault is still empty
        assertEquals(1, f.viewModel.uiState.value.confirmPlacedCount)
        assertEquals(emptySet<String>(), f.vault.vaultedKeys.first())

        // And after a yes the app is vaulted and its Home placements removed
        f.viewModel.onConfirmMove()
        runCurrent()
        assertEquals(setOf(chat.vaultKey), f.vault.vaultedKeys.first())
        verify(f.placements).removeByPackage(chat.packageName, chat.userHandle.hashCode())
        assertEquals(null, f.viewModel.uiState.value.confirmPlacedCount)
    }

    @Test
    fun `removing an app from the vault frees it and offers undo, which hides it again`() = runTest(dispatcher) {
        val chat = app("chat")
        val f = fixture(listOf(chat))
        setPinThrough(f)
        f.vault.add(listOf(chat.vaultKey))
        runCurrent()
        val events = mutableListOf<AppVaultUiEvent>()
        backgroundScope.launch { f.viewModel.events.collect { events += it } }
        runCurrent()

        f.viewModel.onRemoveFromVault(chat)
        runCurrent()

        assertEquals(emptySet<String>(), f.vault.vaultedKeys.first())
        assertEquals(listOf<AppVaultUiEvent>(AppVaultUiEvent.AppRemoved(chat)), events)

        f.viewModel.onUndoRemove(chat)
        runCurrent()
        assertEquals(setOf(chat.vaultKey), f.vault.vaultedKeys.first())
    }

    @Test
    fun `Back steps out of the picker, then asks the vault to close`() = runTest(dispatcher) {
        val f = fixture()
        setPinThrough(f)
        f.viewModel.onAddApps()
        runCurrent()

        assertTrue(f.viewModel.onBack())
        runCurrent()
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)

        assertFalse(f.viewModel.onBack())
    }

    @Test
    fun `changing the PIN replaces it and Back during it returns to the list`() = runTest(dispatcher) {
        val f = fixture()
        setPinThrough(f, "1234")

        f.viewModel.onChangePin()
        runCurrent()
        assertTrue(f.viewModel.onBack())
        runCurrent()
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)

        f.viewModel.onChangePin()
        type(f.viewModel, "9999")
        type(f.viewModel, "9999")
        f.viewModel.lock()
        f.viewModel.onOpened()
        runCurrent()
        type(f.viewModel, "9999")
        assertEquals(VaultStage.LIST, f.viewModel.uiState.value.stage)
    }

    @Test
    fun `forgot PIN after the screen lock is confirmed keeps the apps and asks for a new PIN`() = runTest(dispatcher) {
        // Given a vault with an app and a locked-out pad
        val chat = app("chat")
        val f = fixture(listOf(chat))
        setPinThrough(f)
        f.vault.add(listOf(chat.vaultKey))
        f.viewModel.lock()
        f.viewModel.onOpened()
        runCurrent()
        repeat(VAULT_FREE_ATTEMPTS) { type(f.viewModel, "0000") }

        // When the phone's screen lock is confirmed
        f.viewModel.onDeviceCredentialConfirmed()
        runCurrent()

        // Then the PIN is gone, the app is still vaulted, and a new PIN is asked for
        assertFalse(f.vault.hasPinNow())
        assertEquals(setOf(chat.vaultKey), f.vault.vaultedKeys.first())
        assertEquals(VaultStage.SET_PIN, f.viewModel.uiState.value.stage)
        assertEquals(0, f.viewModel.uiState.value.lockoutSecondsLeft)
    }

    @Test
    fun `turning the vault off empties it and closes it`() = runTest(dispatcher) {
        val chat = app("chat")
        val f = fixture(listOf(chat))
        setPinThrough(f)
        f.vault.add(listOf(chat.vaultKey))
        val events = mutableListOf<AppVaultUiEvent>()
        backgroundScope.launch { f.viewModel.events.collect { events += it } }
        runCurrent()

        f.viewModel.onRequestTurnOff()
        runCurrent()
        assertTrue(f.viewModel.uiState.value.confirmTurnOff)
        f.viewModel.onConfirmTurnOff()
        runCurrent()

        assertEquals(emptySet<String>(), f.vault.vaultedKeys.first())
        assertFalse(f.vault.hasPinNow())
        assertTrue(events.contains(AppVaultUiEvent.Close))
    }

    @Test
    fun `with no screen lock the reset turns the vault off and returns to the intro`() = runTest(dispatcher) {
        val f = fixture()
        setPinThrough(f)
        f.viewModel.lock()
        f.viewModel.onOpened()
        runCurrent()

        f.viewModel.onRequestReset()
        f.viewModel.onConfirmReset()
        runCurrent()

        assertFalse(f.vault.hasPinNow())
        assertEquals(VaultStage.INTRO, f.viewModel.uiState.value.stage)
    }
}
