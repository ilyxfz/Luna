package com.luna.cycle.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationTest {
    @Test
    fun translatesNavigationAndSettingsForBothLanguages() {
        assertEquals("Calendrier", localizeLunaText("Calendar", "fr"))
        assertEquals("الوضع الداكن", localizeLunaText("Dark mode", "ar"))
        assertEquals("English", localizeLunaText("English", "en"))
    }

    @Test
    fun translatesDynamicCycleDayAndDaysRemaining() {
        assertEquals("Jour 12", localizeLunaText("Day 12", "fr"))
        assertEquals("خلال 5 أيام", localizeLunaText("In 5 days", "ar"))
        assertEquals("5 jours avant vos prochaines règles", localizeLunaText("5 days until your next period", "fr"))
        assertEquals("طول الدورة المخصص: 28 يومًا", localizeLunaText("Custom cycle length: 28 days", "ar"))
        assertEquals("Conseil de phase Folliculaire", localizeLunaText("Follicular phase tip", "fr"))
    }

    @Test
    fun translatesDynamicConfirmationMessages() {
        assertEquals(
            "Ajout de 3 dates du cycle à votre calendrier.",
            localizeLunaText("Added 3 cycle dates to your calendar.", "fr")
        )
        assertEquals("تمت استعادة 4 تسجيلات.", localizeLunaText("Restored 4 check-ins.", "ar"))
    }
}
