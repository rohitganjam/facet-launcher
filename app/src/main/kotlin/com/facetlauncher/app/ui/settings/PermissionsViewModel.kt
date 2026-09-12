package com.facetlauncher.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Every permission Facet uses, in one list — none of the three have a grant-change callback
 * ([CalendarPermissionRepository]/[ContactPermissionRepository] are normal runtime permissions
 * that could change from outside the app via system Settings; [UsageAccessRepository] is a
 * special-access permission with no runtime dialog at all), so [refresh] is called on every
 * `onResume`, the same pattern [UsageAccessExplanationViewModel] already established.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val contactPermissionRepository: ContactPermissionRepository,
    private val usageAccessRepository: UsageAccessRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    val uiState: StateFlow<PermissionsUiState> = combine(settingsRepository.settings, refreshTick) { settings, _ ->
        PermissionsUiState(
            permissions = listOf(
                PermissionRowState(
                    kind = PermissionKind.CALENDAR,
                    title = "Calendar",
                    subtitle = "Shows today's events on the clock.",
                    isGranted = calendarPermissionRepository.isGranted(),
                    hasRequestedBefore = settings.calendarPermissionRequested,
                ),
                PermissionRowState(
                    kind = PermissionKind.CONTACTS,
                    title = "Contacts",
                    subtitle = "Lets Drawer search show matching contacts with quick call/message/WhatsApp actions.",
                    isGranted = contactPermissionRepository.isGranted(),
                    hasRequestedBefore = settings.contactsPermissionRequested,
                ),
                PermissionRowState(
                    kind = PermissionKind.USAGE_ACCESS,
                    title = "Usage access",
                    subtitle = "Powers the Recents and Most used app lists.",
                    isGranted = usageAccessRepository.isGranted(),
                ),
                PermissionRowState(
                    kind = PermissionKind.NOTIFICATION_ACCESS,
                    title = "Notification access",
                    subtitle = "Shows a dot or count badge on apps with active notifications.",
                    isGranted = notificationAccessRepository.isGranted(),
                ),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PermissionsUiState())

    fun refresh() {
        refreshTick.value++
    }

    fun markCalendarPermissionRequested() {
        viewModelScope.launch { settingsRepository.setCalendarPermissionRequested(true) }
    }

    fun markContactsPermissionRequested() {
        viewModelScope.launch { settingsRepository.setContactsPermissionRequested(true) }
    }
}
