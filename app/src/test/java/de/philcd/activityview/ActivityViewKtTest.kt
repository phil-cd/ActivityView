package de.philcd.activityview

import org.junit.Test

import org.junit.Assert.*
import java.util.Calendar
import java.util.Calendar.*

class ActivityViewKtTest {

    /**
     * Test uses a date as firstDay that is not a Monday.
     * However, this is a requirement. Therefore, an IllegalArgumentException should be thrown.
     */
    @Test
    fun createMonthToWeekIndexList_firstDay_not_Monday() {
        val firstDay = Calendar.getInstance()
        // month 9 = October
        firstDay.set(2022, 9, 4)
        // verify that this is a tuesday
        assertEquals(TUESDAY, firstDay.get(DAY_OF_WEEK))

        val lastDay = Calendar.getInstance()
        lastDay.set(2022, 10, 8)

        assertThrows("IllegalArgumentException should be thrown if firstDay is not a Monday.", IllegalArgumentException::class.java) {
            createMonthToWeekIndexList(firstDay, lastDay)
        }
    }

    /**
     * Test uses a lastDay which is not after firstDay.
     * Therefore, an IllegalArgumentException should be thrown.
     */
    @Test
    fun createMonthToWeekIndexList_firstDay_after_lastDay() {
        val firstDay = Calendar.getInstance()
        firstDay.set(2022, 9, 3)

        val lastDay = Calendar.getInstance()
        lastDay.set(2022, 9, 2)

        assertThrows("IllegalArgumentException should be thrown if firstDay is after lastDay.", IllegalArgumentException::class.java) {
            createMonthToWeekIndexList(firstDay, lastDay)
        }
    }

    /**
     * Tests the method with firstDay equals to lastDay.
     */
    @Test
    fun createMonthToWeekIndexList_one_day() {
        val firstDay = Calendar.getInstance()
        firstDay.set(2022, 9, 3)

        val list = createMonthToWeekIndexList(firstDay, firstDay)

        // month does not change between firstDay and lastDay
        // therefore, the list should have one entry for month 9 with week index 0
        assertEquals("List size should be 1", 1, list.size)

        val (month, weekIndex) = list[0]
        assertEquals(9, month)
        assertEquals(0, weekIndex)
    }

    /**
     * Test with more than one day but still the month does not change between firstDay and
     * lastDay. Last day is set to 31th as an edge case.
     */
    @Test
    fun createMonthToWeekIndexList_one_month() {
        val firstDay = Calendar.getInstance()
        firstDay.set(2022, 9, 3)

        val lastDay = Calendar.getInstance()
        lastDay.set(2022, 9, 31)

        val list = createMonthToWeekIndexList(firstDay, lastDay)

        // month does not change between firstDay and lastDay
        // therefore, the list should have one entry for month 9 with week index 0
        assertEquals("List size should be 1", 1, list.size)

        val month = list[0].first
        val weekIndex = list[0].second
        assertEquals(9, month)
        assertEquals(weekIndex, 0)
    }

    /**
     * Test with one month change between firstDay and lastDay.
     */
    @Test
    fun createMonthToWeekIndexList_two_months() {
        val firstDay = Calendar.getInstance()
        firstDay.set(2022, 9, 3)

        val lastDay = Calendar.getInstance()
        lastDay.set(2022, 10, 20)

        val list = createMonthToWeekIndexList(firstDay, lastDay)

        // month changes once, we should have an entry for month 9 and one for month 10
        assertEquals("List size should be 2", 2, list.size)

        val (firstMonth, firstWeekIndex) = list[0]

        assertEquals(9, firstMonth)
        assertEquals(0, firstWeekIndex)

        val (secondMonth, secondWeekIndex) = list[1]

        assertEquals(10, secondMonth)
        assertEquals(4, secondWeekIndex)
    }
}