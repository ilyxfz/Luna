package com.luna.cycle.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CycleAnalyticsTest {
    @Test
    fun movingAverageUsesMostRecentConfiguredCycles() {
        val logs = listOf(
            period("2026-06-01"),
            period("2026-06-29"),
            period("2026-07-27"),
            period("2026-08-24")
        )

        val history = analyzeCycleHistory(logs, LocalDate.parse("2026-08-24"), 0, 3)

        assertEquals(28, history.averageCycleLength)
        assertEquals(3, history.cycleLengths.size)
    }

    @Test
    fun flagsUnusuallyShortOrLongCycles() {
        val logs = listOf(
            period("2026-05-01"),
            period("2026-05-18"),
            period("2026-06-29")
        )

        val history = analyzeCycleHistory(logs, LocalDate.parse("2026-06-29"), 28, 3)

        assertEquals(listOf(17, 42), history.irregularCycleLengths)
    }

    @Test
    fun identifiesWaterAndSleepAssociationsWithoutClaimingCausation() {
        val logs = buildList {
            repeat(8) { index ->
                val symptom = if (index < 4) "Cramps" else ""
                add(
                    CycleLog(
                        date = LocalDate.of(2026, 1, index + 1).toString(),
                        symptoms = symptom,
                        waterGlasses = if (index < 4) 1 else 5,
                        sleepQuality = if (index < 4) 1 else 5
                    )
                )
            }
        }

        val correlations = wellbeingCorrelations(logs)

        assertTrue(correlations.any { it.symptom == "Cramps" && it.factor == "Lower water intake" })
        assertTrue(correlations.any { it.symptom == "Cramps" && it.factor == "Lower sleep quality" })
    }

    private fun period(date: String) = CycleLog(date = date, flow = "Medium")
}
