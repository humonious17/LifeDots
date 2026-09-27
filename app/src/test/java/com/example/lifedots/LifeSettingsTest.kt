package com.example.lifedots

import com.example.lifedots.preferences.LifeProgress
import com.example.lifedots.preferences.LifeSettings
import com.example.lifedots.preferences.ProgressSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class LifeSettingsTest {

    @Test
    fun testDefaultLifeSettings() {
        val settings = LifeSettings()
        assertEquals(80, settings.lifeExpectancyYears)
        assertEquals("MEMENTO MORI", settings.titleText)
        assertTrue(settings.showTitle)
        assertTrue(settings.showYearLabels)
        assertTrue(settings.splitHalves)
        assertTrue(settings.showQuote)
        assertEquals("SENECA", settings.quoteAuthor)
    }

    @Test
    fun testCalculateProgressForNewborn() {
        val now = Calendar.getInstance()
        val birthDate = now.timeInMillis
        val settings = LifeSettings(birthDate = birthDate, lifeExpectancyYears = 80)
        val progress = settings.calculateProgress()

        assertEquals(0, progress.ageYears)
        assertEquals(0, progress.weekInCurrentYear)
        assertEquals(0, progress.currentDotIndex)
        assertEquals(4160, progress.totalWeeks) // 80 * 52
        assertEquals(0, progress.weeksLived)
        assertEquals(4159, progress.weeksRemaining)
        assertEquals(0.0f, progress.percentLived, 0.01f)
    }

    @Test
    fun testCalculateProgressForKnownAge() {
        val now = Calendar.getInstance()
        // Set birthday exactly 25 years ago today
        val birthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, now.get(Calendar.YEAR) - 25)
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val settings = LifeSettings(birthDate = birthCal.timeInMillis, lifeExpectancyYears = 80)
        val progress = settings.calculateProgress()

        assertEquals(25, progress.ageYears)
        assertEquals(0, progress.weekInCurrentYear)
        val expectedDot = 25 * 52
        assertEquals(expectedDot, progress.currentDotIndex)
        assertEquals(expectedDot, progress.weeksLived)
        assertEquals(4160 - expectedDot - 1, progress.weeksRemaining)
        val expectedPercent = (expectedDot.toFloat() / 4160f) * 100f
        assertEquals(expectedPercent, progress.percentLived, 0.1f)
    }

    @Test
    fun testCalculateProgressBirthdayLaterThisYear() {
        val now = Calendar.getInstance()
        // Birthday in December if now is not December, or next month
        val birthMonth = if (now.get(Calendar.MONTH) < 11) Calendar.DECEMBER else Calendar.DECEMBER
        // Born 20 years ago, but month is December (if today is before Dec 31)
        val birthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, now.get(Calendar.YEAR) - 20)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
        }

        val settings = LifeSettings(birthDate = birthCal.timeInMillis, lifeExpectancyYears = 80)
        val progress = settings.calculateProgress()

        // If today is not Dec 31, age is 19
        if (now.get(Calendar.MONTH) != Calendar.DECEMBER || now.get(Calendar.DAY_OF_MONTH) != 31) {
            assertEquals(19, progress.ageYears)
        }
        assertTrue(progress.currentDotIndex >= 0)
        assertTrue(progress.currentDotIndex <= progress.totalWeeks)
    }

    @Test
    fun testProgressFormattingWithLifeProgress() {
        val lifeProgress = LifeProgress(
            ageYears = 25,
            weekInCurrentYear = 10,
            currentDotIndex = 1310,
            totalWeeks = 4160,
            weeksLived = 1310,
            weeksRemaining = 2849,
            percentLived = 31.49f
        )

        // Default: showPercentage = true, showRemainingDays = true, showDaysPassed = false, decimalPlaces = 1
        val settings = ProgressSettings()
        val formatted = settings.formatText(0, 0, lifeProgress)
        assertEquals("31.5% • 2849 weeks left", formatted)
    }

    @Test
    fun testProgressFormattingLifePercentageOnly() {
        val lifeProgress = LifeProgress(
            ageYears = 25,
            weekInCurrentYear = 10,
            currentDotIndex = 1310,
            totalWeeks = 4160,
            weeksLived = 1310,
            weeksRemaining = 2849,
            percentLived = 31.49f
        )

        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = false,
            showDaysPassed = false,
            decimalPlaces = 1
        )
        val formatted = settings.formatText(0, 0, lifeProgress)
        assertEquals("31.5% lived", formatted)
    }

    @Test
    fun testProgressFormattingLifeRemainingOnly() {
        val lifeProgress = LifeProgress(
            ageYears = 25,
            weekInCurrentYear = 10,
            currentDotIndex = 1310,
            totalWeeks = 4160,
            weeksLived = 1310,
            weeksRemaining = 2849,
            percentLived = 31.49f
        )

        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = true,
            showDaysPassed = false
        )
        val formatted = settings.formatText(0, 0, lifeProgress)
        assertEquals("2849 weeks remaining", formatted)
    }

    @Test
    fun testProgressFormattingLifeSingleWeekRemaining() {
        val lifeProgress = LifeProgress(
            ageYears = 79,
            weekInCurrentYear = 50,
            currentDotIndex = 4158,
            totalWeeks = 4160,
            weeksLived = 4158,
            weeksRemaining = 1,
            percentLived = 99.95f
        )

        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = true,
            showDaysPassed = false
        )
        val formatted = settings.formatText(0, 0, lifeProgress)
        assertEquals("1 week remaining", formatted)
    }

    @Test
    fun testProgressFormattingLifeDaysPassedOnly() {
        val lifeProgress = LifeProgress(
            ageYears = 25,
            weekInCurrentYear = 10,
            currentDotIndex = 1310,
            totalWeeks = 4160,
            weeksLived = 1310,
            weeksRemaining = 2849,
            percentLived = 31.49f
        )

        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = false,
            showDaysPassed = true
        )
        val formatted = settings.formatText(0, 0, lifeProgress)
        assertEquals("Week 1311 of 4160", formatted)
    }

    @Test
    fun testProgressFormattingLifeAllEnabled() {
        val lifeProgress = LifeProgress(
            ageYears = 25,
            weekInCurrentYear = 10,
            currentDotIndex = 1310,
            totalWeeks = 4160,
            weeksLived = 1310,
            weeksRemaining = 2849,
            percentLived = 31.49f
        )

        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = true,
            showDaysPassed = true,
            decimalPlaces = 1
        )
        val formatted = settings.formatText(0, 0, lifeProgress)
        assertEquals("Wk 1311/4160 • 31.5% • 2849 weeks left", formatted)
    }
}
