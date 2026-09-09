package com.lumenlauncher.app.domain

import com.lumenlauncher.app.data.model.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class SelectPreviewAppsUseCaseTest {

    private val useCase = SelectPreviewAppsUseCase()

    private fun app(packageName: String, label: String = packageName) =
        AppInfo(packageName = packageName, activityName = ".Main", label = label, icon = null)

    @Test
    fun `OS-preferred packages come first, ahead of fallback and always-considered apps`() {
        // Given the OS resolved a browser default, and the installed list also happens to
        // contain a fallback-category app (Gmail) and an unrelated app
        val installed = listOf(app("com.example.aaa"), app("com.google.android.gm"), app("com.android.chrome"))

        // Then the OS-resolved pick leads, ahead of Gmail (fallback/always-considered) and the plain remainder
        val result = useCase(installed, preferredPackages = listOf("com.android.chrome"), count = 3)

        assertEquals(listOf("com.android.chrome", "com.google.android.gm", "com.example.aaa"), result.map { it.packageName })
    }

    @Test
    fun `a fallback category package fills in when OS resolution didn't cover that category`() {
        // Given no OS-resolved default at all, but a fallback browser candidate is installed
        val installed = listOf(app("com.example.aaa"), app("com.android.chrome"))

        // Then the fallback candidate is still picked ahead of the plain remainder
        val result = useCase(installed, preferredPackages = emptyList(), count = 2)

        assertEquals(listOf("com.android.chrome", "com.example.aaa"), result.map { it.packageName })
    }

    @Test
    fun `Play Store is always considered even when absent from preferred and fallback packages`() {
        // Given Play Store isn't an OS-resolved default and isn't part of any fallback-category
        // candidate list (unlike Gmail, which is also the Mail category's own fallback pick)
        val installed = listOf(app("com.example.aaa"), app("com.android.vending"))

        // Then it still leads ahead of the unrelated app, purely via the always-considered tier
        val result = useCase(installed, preferredPackages = emptyList(), count = 2)

        assertEquals(listOf("com.android.vending", "com.example.aaa"), result.map { it.packageName })
    }

    @Test
    fun `an app matched by multiple tiers is not duplicated`() {
        // Given Gmail, which appears in both the mail-category fallback and the always-considered list
        val installed = listOf(app("com.google.android.gm"), app("com.example.aaa"))

        val result = useCase(installed, preferredPackages = emptyList(), count = 5)

        assertEquals(listOf("com.google.android.gm", "com.example.aaa"), result.map { it.packageName })
    }

    @Test
    fun `packages absent from the installed list are skipped, not padded`() {
        // Given an OS-preferred package that (unusually) isn't in the installed list
        val installed = listOf(app("com.example.aaa"), app("com.example.bbb"))

        val result = useCase(installed, preferredPackages = listOf("com.android.chrome"), count = 2)

        assertEquals(listOf("com.example.aaa", "com.example.bbb"), result.map { it.packageName })
    }

    @Test
    fun `falls back to the installed list's own order when nothing in any tier matches`() {
        val installed = listOf(app("com.example.aaa"), app("com.example.bbb"), app("com.example.ccc"))

        val result = useCase(installed, preferredPackages = emptyList(), count = 2)

        assertEquals(listOf("com.example.aaa", "com.example.bbb"), result.map { it.packageName })
    }

    @Test
    fun `never returns more than the requested count`() {
        val installed = listOf(app("com.android.chrome"), app("com.google.android.gm"), app("com.example.aaa"))

        val result = useCase(installed, preferredPackages = listOf("com.android.chrome"), count = 1)

        assertEquals(listOf("com.android.chrome"), result.map { it.packageName })
    }

    @Test
    fun `empty installed list yields an empty preview`() {
        assertEquals(emptyList<AppInfo>(), useCase(emptyList(), preferredPackages = listOf("com.android.chrome"), count = 5))
    }
}
