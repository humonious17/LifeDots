package com.example.lifedots

import com.example.lifedots.preferences.ProgressPosition
import com.example.lifedots.preferences.ProgressSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressSettingsTest {

    @Test
    fun testDefaultProgressFormatting() {
        // By default: showPercentage = true, showRemainingDays = true, showDaysPassed = false, decimalPlaces = 1
        val settings = ProgressSettings()
        // Day 100 of 365 -> percentage = (100 / 365) * 100 = 27.397% -> "27.4%", remaining = 265
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("27.4% • 265 days left", text)
    }

    @Test
    fun testPercentageOnly() {
        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = false,
            showDaysPassed = false,
            decimalPlaces = 1
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("27.4% completed", text)
    }

    @Test
    fun testPercentageWithZeroDecimals() {
        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = false,
            showDaysPassed = false,
            decimalPlaces = 0
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("27% completed", text)
    }

    @Test
    fun testPercentageWithTwoDecimals() {
        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = false,
            showDaysPassed = false,
            decimalPlaces = 2
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("27.40% completed", text)
    }

    @Test
    fun testRemainingDaysOnly() {
        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = true,
            showDaysPassed = false
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("265 days remaining", text)
    }

    @Test
    fun testSingleRemainingDay() {
        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = true,
            showDaysPassed = false
        )
        val text = settings.formatText(dayOfYear = 364, totalDays = 365)
        assertEquals("1 day remaining", text)
    }

    @Test
    fun testDaysPassedOnly() {
        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = false,
            showDaysPassed = true
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("Day 100 of 365", text)
    }

    @Test
    fun testAllThreeEnabled() {
        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = true,
            showDaysPassed = true,
            decimalPlaces = 1
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("Day 100/365 • 27.4% • 265 days left", text)
    }

    @Test
    fun testLeapYear() {
        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = true,
            showDaysPassed = false,
            decimalPlaces = 1
        )
        // Day 183 of 366 (leap year) -> 50.0%, 183 days left
        val text = settings.formatText(dayOfYear = 183, totalDays = 366)
        assertEquals("50.0% • 183 days left", text)
    }

    @Test
    fun testLastDayOfYear() {
        val settings = ProgressSettings(
            showPercentage = true,
            showRemainingDays = true,
            showDaysPassed = false,
            decimalPlaces = 1
        )
        val text = settings.formatText(dayOfYear = 365, totalDays = 365)
        assertEquals("100.0% • 0 days left", text)
    }

    @Test
    fun testFallbackWhenAllTogglesOff() {
        val settings = ProgressSettings(
            showPercentage = false,
            showRemainingDays = false,
            showDaysPassed = false,
            decimalPlaces = 1
        )
        val text = settings.formatText(dayOfYear = 100, totalDays = 365)
        assertEquals("27.4% • 265 days left", text)
    }
}
