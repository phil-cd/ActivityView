package de.philcd.activityview

import java.util.*

/**
 * File with helper functions for finding the first day that is visible in an ActivityView and the
 * last visible day (today).
 */

/**
 * Returns the first day that is shown in the ActivityView.
 */
fun firstVisibleDay(numWeeksToShow: Int): Calendar {
    val c = Calendar.getInstance()
    c.firstDayOfWeek = Calendar.MONDAY
    c.add(Calendar.WEEK_OF_YEAR, -(numWeeksToShow - 1))
    c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    c.set(Calendar.HOUR, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}

/**
 * Returns the last day shown in the ActivityView, i.e. today.
 */
fun today(): Calendar {
    val c = Calendar.getInstance()
    c.firstDayOfWeek = Calendar.MONDAY
    c.set(Calendar.HOUR, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c
}