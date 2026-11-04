package com.facetlauncher.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.facetlauncher.app.R
import com.facetlauncher.app.data.model.CalendarInfo

/** Where a Work Profile calendar sits among the work calendars: its 1-based [number] of [count], for naming it when there is more than one. */
fun workCalendarPosition(calendar: CalendarInfo, all: List<CalendarInfo>): Pair<Int, Int> {
    val work = all.filter { it.isWork }
    return (work.indexOfFirst { it.id == calendar.id } + 1) to work.size
}

/**
 * What a calendar is called in a picker. A Work Profile calendar has no name of its own (Android hides it from the personal side), so it is
 * "Work calendar", numbered ([workNumber] of [workCount], see [workCalendarPosition]) when there is more than one.
 */
@Composable
fun calendarTitle(calendar: CalendarInfo, workNumber: Int, workCount: Int): String = when {
    !calendar.isWork -> calendar.displayName
    workCount > 1 -> stringResource(R.string.calendar_work_calendar_n, workNumber)
    else -> stringResource(R.string.calendar_work_calendar)
}

/** The line under [calendarTitle]: the account, or for a Work Profile calendar "Work" and a few of its event titles, which are all that identify it. */
@Composable
fun calendarSubtitle(calendar: CalendarInfo): String = when {
    !calendar.isWork -> calendar.accountName
    calendar.sampleTitles.isEmpty() -> stringResource(R.string.facet_scope_work)
    else -> stringResource(R.string.facet_scope_work) + " · " + calendar.sampleTitles.joinToString(", ")
}
