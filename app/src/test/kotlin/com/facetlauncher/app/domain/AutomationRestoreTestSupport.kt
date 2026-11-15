package com.facetlauncher.app.domain

import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer

/** The summary of a restore whose backup held no rules. */
val EMPTY_AUTOMATION_RESTORE = AutomationRestoreSummary(inBackup = true, restoredCount = 0, skipped = emptyList(), pausedCount = 0, needsPermissionCount = 0)

/**
 * A [RestoreAutomationRulesUseCase] for import tests that aren't about rules: every call answers [EMPTY_AUTOMATION_RESTORE]
 * and appends its arguments (backup rules, facet id by UUID, active facet id) to [calls].
 */
fun noAutomationRestore(calls: MutableList<List<Any?>> = mutableListOf()): RestoreAutomationRulesUseCase =
    mock(
        RestoreAutomationRulesUseCase::class.java,
        Answer { invocation ->
            calls += invocation.arguments.toList() // Mockito already leaves out a suspend call's continuation
            EMPTY_AUTOMATION_RESTORE
        },
    )
