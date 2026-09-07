package com.lumenlauncher.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileEntityTest {

    @Test
    fun `resolveOverride returns the global value when there is no profile`() {
        // Given no profile at all
        val profile: ProfileEntity? = null

        // When resolving with any overriding/profileValue lambdas
        val result = profile.resolveOverride({ true }, { "profile" }, "global")

        // Then the global value wins, since there's nothing to override with
        assertEquals("global", result)
    }

    @Test
    fun `resolveOverride returns the profile value when overriding is true`() {
        // Given a profile whose override flag is on
        val profile = ProfileEntity(name = "Work", position = 0, overrideClock = true)

        // When resolving against that flag
        val result = profile.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, com.lumenlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE)

        // Then the profile's own stored value wins
        assertEquals(profile.clockTemplateId, result)
    }

    @Test
    fun `resolveOverride returns the global value when overriding is false`() {
        // Given a profile whose override flag is off
        val profile = ProfileEntity(name = "Work", position = 0, overrideClock = false)

        // When resolving against that flag
        val result = profile.resolveOverride({ it.overrideClock }, { it.clockTemplateId }, com.lumenlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE)

        // Then the global default wins, even though the profile has its own stored value
        assertEquals(com.lumenlauncher.app.data.model.ClockTemplateId.ROBOTO_FLEX_WIDE, result)
    }
}
