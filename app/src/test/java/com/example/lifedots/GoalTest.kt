package com.example.lifedots

import com.example.lifedots.preferences.Goal
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class GoalTest {

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
    fun testGoalDueTodayReturnsZeroRegardlessOfTimeOfDay() {
        val target = createCalendar(2026, Calendar.SEPTEMBER, 28, 0, 0).timeInMillis
        val goal = Goal(id = "1", title = "Launch Feature", targetDate = target)

        // Morning
        val morning = createCalendar(2026, Calendar.SEPTEMBER, 28, 9, 30).timeInMillis
        assertEquals(0, goal.calculateDaysRemaining(morning))

        // Afternoon (14:30) - this previously failed with integer division
        val afternoon = createCalendar(2026, Calendar.SEPTEMBER, 28, 14, 30).timeInMillis
        assertEquals(0, goal.calculateDaysRemaining(afternoon))

        // Late night (23:59)
        val night = createCalendar(2026, Calendar.SEPTEMBER, 28, 23, 59).timeInMillis
        assertEquals(0, goal.calculateDaysRemaining(night))
    }

    @Test
    fun testGoalDueTomorrowReturnsOne() {
        // Target is tomorrow at midnight
        val target = createCalendar(2026, Calendar.SEPTEMBER, 29, 0, 0).timeInMillis
        val goal = Goal(id = "2", title = "Ship Tomorrow", targetDate = target)

        // Now is today at 15:00 (difference is only 9 hours, previously gave 0)
        val afternoon = createCalendar(2026, Calendar.SEPTEMBER, 28, 15, 0).timeInMillis
        assertEquals(1, goal.calculateDaysRemaining(afternoon))

        // Now is today at 23:59 (difference is 1 minute, previously gave 0)
        val lateNight = createCalendar(2026, Calendar.SEPTEMBER, 28, 23, 59).timeInMillis
        assertEquals(1, goal.calculateDaysRemaining(lateNight))

        // Now is today at 00:01
        val earlyMorning = createCalendar(2026, Calendar.SEPTEMBER, 28, 0, 1).timeInMillis
        assertEquals(1, goal.calculateDaysRemaining(earlyMorning))
    }

    @Test
    fun testGoalDueInMultipleDays() {
        val target = createCalendar(2026, Calendar.OCTOBER, 5, 0, 0).timeInMillis
        val goal = Goal(id = "3", title = "Next Week", targetDate = target)

        val now = createCalendar(2026, Calendar.SEPTEMBER, 28, 18, 45).timeInMillis
        assertEquals(7, goal.calculateDaysRemaining(now))
    }

    @Test
    fun testGoalDueYesterdayReturnsMinusOne() {
        val target = createCalendar(2026, Calendar.SEPTEMBER, 27, 0, 0).timeInMillis
        val goal = Goal(id = "4", title = "Yesterday's Goal", targetDate = target)

        val now = createCalendar(2026, Calendar.SEPTEMBER, 28, 10, 0).timeInMillis
        assertEquals(-1, goal.calculateDaysRemaining(now))
    }

    @Test
    fun testGoalPastDueMultipleDays() {
        val target = createCalendar(2026, Calendar.SEPTEMBER, 18, 0, 0).timeInMillis
        val goal = Goal(id = "5", title = "10 Days Ago", targetDate = target)

        val now = createCalendar(2026, Calendar.SEPTEMBER, 28, 14, 0).timeInMillis
        assertEquals(-10, goal.calculateDaysRemaining(now))
    }

    @Test
    fun testGoalAcrossMonthBoundary() {
        val target = createCalendar(2026, Calendar.OCTOBER, 1, 0, 0).timeInMillis
        val goal = Goal(id = "6", title = "New Month", targetDate = target)

        val now = createCalendar(2026, Calendar.SEPTEMBER, 30, 22, 0).timeInMillis
        assertEquals(1, goal.calculateDaysRemaining(now))
    }

    @Test
    fun testGoalAcrossYearBoundary() {
        val target = createCalendar(2027, Calendar.JANUARY, 1, 0, 0).timeInMillis
        val goal = Goal(id = "7", title = "New Year", targetDate = target)

        val now = createCalendar(2026, Calendar.DECEMBER, 31, 23, 30).timeInMillis
        assertEquals(1, goal.calculateDaysRemaining(now))
    }
}
