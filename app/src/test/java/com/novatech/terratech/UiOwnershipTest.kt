package com.novatech.terratech

import com.novatech.terratech.core.presentation.state.forUser
import com.novatech.terratech.monitoring.domain.entity.Field
import com.novatech.terratech.monitoring.presentation.state.MonitoringState
import com.novatech.terratech.profile.domain.entity.FarmProfile
import com.novatech.terratech.profile.presentation.state.ProfileState
import org.junit.Assert.*
import org.junit.Test

class UiOwnershipTest {
  @Test
  fun switchingAccountCannotRenderThePreviousOwnersFieldsOrProfile() {
    val old =
      MonitoringState(
        userId = 1,
        fields = listOf(Field(1, 1, "Private field", 5000.0, "Loam", -12.0, -77.0, "Potato")),
      )
    val profile =
      ProfileState(
        userId = 1,
        profile = FarmProfile(1, 1, "Ana", "a@example.com", "Private farm", "999", "Lima", 10000.0),
      )
    assertTrue(old.forUser(2).fields.isEmpty())
    assertNull(profile.forUser(2).profile)
    assertTrue(old.forUser(2).busy)
    assertTrue(profile.forUser(2).busy)
    assertEquals("Private field", old.forUser(1).fields.single().name)
    assertEquals("Private farm", profile.forUser(1).profile!!.fundoName)
  }
}
