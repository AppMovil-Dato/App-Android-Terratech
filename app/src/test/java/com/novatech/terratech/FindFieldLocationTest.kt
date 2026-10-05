package com.novatech.terratech

import com.novatech.terratech.monitoring.application.usecase.FindFieldLocation
import com.novatech.terratech.monitoring.domain.repository.LocationSearch
import com.novatech.terratech.monitoring.domain.valueobject.Coordinates
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FindFieldLocationTest {
  @Test
  fun searchTrimsValidQueriesAndSkipsInvalidOnes() = runTest {
    val queries = mutableListOf<String>()
    val find =
      FindFieldLocation(
        object : LocationSearch {
          override suspend fun find(query: String): Coordinates? {
            queries += query
            return Coordinates.of(-14.0, -75.0)
          }
        }
      )
    assertNull(find(" "))
    assertNull(find("x"))
    assertNull(find("x".repeat(201)))
    assertEquals(Coordinates.of(-14.0, -75.0), find(" Ica "))
    assertEquals(listOf("Ica"), queries)
  }
}
