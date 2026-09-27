package com.example.lifedots

import com.example.lifedots.preferences.CustomYearSettings
import com.example.lifedots.preferences.ProgressSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class CustomYearSettingsTest {

    private fun createCalendar(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun testDefaultSettings() {
        val settings = CustomYearSettings()
        assertFalse(settings.enabled)
        assertTrue(settings.endDate > settings.startDate)
    }

    @Test
    fun testAcademicYearPresets() {
        val (start, end) = CustomYearSettings.createAcademicYearRange()
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(Calendar.SEPTEMBER, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(Calendar.MAY, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(startCal.get(Calendar.YEAR) + 1, endCal.get(Calendar.YEAR))
    }

    @Test
    fun testSchoolYearPresets() {
        val (start, end) = CustomYearSettings.createSchoolYearRange()
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(Calendar.AUGUST, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(Calendar.JUNE, endCal.get(Calendar.MONTH))
        assertEquals(30, endCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(startCal.get(Calendar.YEAR) + 1, endCal.get(Calendar.YEAR))
    }

    @Test
    fun testCalendarYearPresets() {
        val (start, end) = CustomYearSettings.createCalendarYearRange()
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(Calendar.JANUARY, startCal.get(Calendar.MONTH))
        assertEquals(1, startCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(Calendar.DECEMBER, endCal.get(Calendar.MONTH))
        assertEquals(31, endCal.get(Calendar.DAY_OF_MONTH))

        assertEquals(startCal.get(Calendar.YEAR), endCal.get(Calendar.YEAR))
    }

    @Test
    fun testAcademicYearTotalDaysNonLeap() {
        // Sep 1, 2026 to May 31, 2027 (2027 is non-leap year)
        // Sep(30) + Oct(31) + Nov(30) + Dec(31) + Jan(31) + Feb(28) + Mar(31) + Apr(30) + May(31) = 273 days
        val start = createCalendar(2026, Calendar.SEPTEMBER, 1).timeInMillis
        val end = createCalendar(2027, Calendar.MAY, 31).timeInMillis

        val settings = CustomYearSettings(enabled = true, startDate = start, endDate = end)
        val progressOnStart = settings.calculateProgress(start)

        assertEquals(273, progressOnStart.totalDays)
        assertEquals(1, progressOnStart.dayIndex)
        assertEquals(272, progressOnStart.daysRemaining)
        assertTrue(progressOnStart.isTodayInRange)
        assertFalse(progressOnStart.isBeforeStart)
        assertFalse(progressOnStart.isAfterEnd)
    }

    @Test
    fun testAcademicYearProgressOnEndDate() {
        val start = createCalendar(2026, Calendar.SEPTEMBER, 1).timeInMillis
        val end = createCalendar(2027, Calendar.MAY, 31).timeInMillis

        val settings = CustomYearSettings(enabled = true, startDate = start, endDate = end)
        val progressOnEnd = settings.calculateProgress(end)

        assertEquals(273, progressOnEnd.totalDays)
        assertEquals(273, progressOnEnd.dayIndex)
        assertEquals(0, progressOnEnd.daysRemaining)
        assertEquals(100.0f, progressOnEnd.percentage, 0.01f)
        assertTrue(progressOnEnd.isTodayInRange)
    }

    @Test
    fun testAcademicYearMidwayProgress() {
        val start = createCalendar(2026, Calendar.SEPTEMBER, 1).timeInMillis
        val end = createCalendar(2027, Calendar.MAY, 31).timeInMillis

        // Sep 28 is the 28th day
        val now = createCalendar(2026, Calendar.SEPTEMBER, 28, 14, 0).timeInMillis

        val settings = CustomYearSettings(enabled = true, startDate = start, endDate = end)
        val progress = settings.calculateProgress(now)

        assertEquals(273, progress.totalDays)
        assertEquals(28, progress.dayIndex)
        assertEquals(245, progress.daysRemaining)
        // 28 / 273 * 100 = 10.256%
        assertEquals(10.26f, progress.percentage, 0.01f)
        assertTrue(progress.isTodayInRange)
    }

    @Test
    fun testProgressBeforeStartDate() {
        val start = createCalendar(2026, Calendar.SEPTEMBER, 1).timeInMillis
        val end = createCalendar(2027, Calendar.MAY, 31).timeInMillis

        // August 15 (before start)
        val before = createCalendar(2026, Calendar.AUGUST, 15).timeInMillis

        val settings = CustomYearSettings(enabled = true, startDate = start, endDate = end)
        val progress = settings.calculateProgress(before)

        assertTrue(progress.isBeforeStart)
        assertFalse(progress.isTodayInRange)
        assertFalse(progress.isAfterEnd)
        assertEquals(0, progress.dayIndex)
        assertEquals(273, progress.daysRemaining)
    }

    @Test
    fun testProgressAfterEndDate() {
        val start = createCalendar(2026, Calendar.SEPTEMBER, 1).timeInMillis
        val end = createCalendar(2027, Calendar.MAY, 31).timeInMillis

        // June 15 (after end)
        val after = createCalendar(2027, Calendar.JUNE, 15).timeInMillis

        val settings = CustomYearSettings(enabled = true, startDate = start, endDate = end)
        val progress = settings.calculateProgress(after)

        assertTrue(progress.isAfterEnd)
        assertFalse(progress.isTodayInRange)
        assertFalse(progress.isBeforeStart)
        assertEquals(273, progress.dayIndex)
        assertEquals(0, progress.daysRemaining)
        assertEquals(100.0f, progress.percentage, 0.01f)
    }

    @Test
    fun testCustomYearProgressFormattingWithProgressSettings() {
        val start = createCalendar(2026, Calendar.SEPTEMBER, 1).timeInMillis
        val end = createCalendar(2027, Calendar.MAY, 31).timeInMillis

        val settings = CustomYearSettings(enabled = true, startDate = start, endDate = end)
        val now = createCalendar(2026, Calendar.SEPTEMBER, 28).timeInMillis
        val progress = settings.calculateProgress(now)

        // Day count only
        val dayCountSettings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = false,
            showDaysPassed = true
        )
        val text1 = dayCountSettings.formatText(progress.dayIndex, progress.totalDays)
        assertEquals("Day 28 of 273", text1)

        // Day count + remaining days + percentage
        val fullSettings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = true,
            showDaysPassed = true,
            decimalPlaces = 1
        )
        val text2 = fullSettings.formatText(progress.dayIndex, progress.totalDays)
        assertEquals("Day 28/273 • 10.3% • 245 days left", text2)
    }
}
