package com.facetlauncher.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facetlauncher.app.R
import com.facetlauncher.app.data.AutomationPermissionRepository
import com.facetlauncher.app.data.CalendarPermissionRepository
import com.facetlauncher.app.data.ContactPermissionRepository
import com.facetlauncher.app.data.DeviceStateRepository
import com.facetlauncher.app.data.NotificationAccessRepository
import com.facetlauncher.app.data.SettingsRepository
import com.facetlauncher.app.data.UsageAccessRepository
import com.facetlauncher.app.data.model.AutomationPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Every permission Facet uses, in one list — none of them have a grant-change callback
 * ([CalendarPermissionRepository]/[ContactPermissionRepository] are normal runtime permissions
 * that could change from outside the app via system Settings; [UsageAccessRepository] is a
 * special-access permission with no runtime dialog at all), so [refresh] is called on every
 * `onResume`, the same pattern [UsageAccessExplanationViewModel] already established.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val calendarPermissionRepository: CalendarPermissionRepository,
    private val contactPermissionRepository: ContactPermissionRepository,
    private val usageAccessRepository: UsageAccessRepository,
    private val notificationAccessRepository: NotificationAccessRepository,
    private val automationPermissionRepository: AutomationPermissionRepository,
    private val deviceStateRepository: DeviceStateRepository,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    /** Last seen Bluetooth/location grants, to notice a change made outside the app (system Settings). */
    private var lastDeviceGrants: Pair<Boolean, Boolean>? = null

    val uiState: StateFlow<PermissionsUiState> = combine(settingsRepository.settings, refreshTick) { settings, _ ->
        PermissionsUiState(
            permissions = listOf(
                PermissionRowState(
                    kind = PermissionKind.CALENDAR,
                    title = context.getString(R.string.permission_calendar_title),
                    subtitle = context.getString(R.string.permission_calendar_subtitle),
                    isGranted = calendarPermissionRepository.isGranted(),
                    hasRequestedBefore = settings.calendarPermissionRequested,
                ),
                PermissionRowState(
                    kind = PermissionKind.CONTACTS,
                    title = context.getString(R.string.permission_contacts_title),
                    subtitle = context.getString(R.string.permission_contacts_subtitle),
                    isGranted = contactPermissionRepository.isGranted(),
                    hasRequestedBefore = settings.contactsPermissionRequested,
                ),
                PermissionRowState(
                    kind = PermissionKind.USAGE_ACCESS,
                    title = context.getString(R.string.permission_usage_access_title),
                    subtitle = context.getString(R.string.permission_usage_access_subtitle),
                    isGranted = usageAccessRepository.isGranted(),
                ),
                PermissionRowState(
                    kind = PermissionKind.NOTIFICATION_ACCESS,
                    title = context.getString(R.string.permission_notification_access_title),
                    subtitle = context.getString(R.string.permission_notification_access_subtitle),
                    isGranted = notificationAccessRepository.isGranted(),
                ),
                PermissionRowState(
                    kind = PermissionKind.BLUETOOTH,
                    title = context.getString(R.string.permission_bluetooth_title),
                    subtitle = context.getString(R.string.permission_bluetooth_subtitle),
                    isGranted = automationPermissionRepository.isGranted(AutomationPermission.BLUETOOTH_CONNECT),
                    hasRequestedBefore = settings.bluetoothPermissionRequested,
                ),
                PermissionRowState(
                    kind = PermissionKind.LOCATION,
                    title = context.getString(R.string.permission_location_title),
                    subtitle = context.getString(R.string.permission_location_subtitle),
                    isGranted = automationPermissionRepository.isGranted(AutomationPermission.LOCATION),
                    hasRequestedBefore = settings.locationPermissionRequested,
                ),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PermissionsUiState())

    fun refresh() {
        refreshTick.value++
        notifyIfDeviceGrantsChanged()
    }

    /** Re-subscribes the Wi-Fi and Bluetooth sources, which check their permission when they subscribe, if a grant changed. */
    private fun notifyIfDeviceGrantsChanged() {
        val current = automationPermissionRepository.isGranted(AutomationPermission.BLUETOOTH_CONNECT) to
            automationPermissionRepository.isGranted(AutomationPermission.LOCATION)
        val previous = lastDeviceGrants
        lastDeviceGrants = current
        if (previous != null && previous != current) deviceStateRepository.onPermissionsChanged()
    }

    fun markCalendarPermissionRequested() {
        viewModelScope.launch { settingsRepository.setCalendarPermissionRequested(true) }
    }

    fun markContactsPermissionRequested() {
        viewModelScope.launch { settingsRepository.setContactsPermissionRequested(true) }
    }

    /** Called with the system dialog's result; also wakes the automation sources so a fresh grant takes effect at once. */
    fun markBluetoothPermissionRequested() {
        viewModelScope.launch { settingsRepository.setBluetoothPermissionRequested(true) }
        deviceStateRepository.onPermissionsChanged()
    }

    fun markLocationPermissionRequested() {
        viewModelScope.launch { settingsRepository.setLocationPermissionRequested(true) }
        deviceStateRepository.onPermissionsChanged()
    }
}
