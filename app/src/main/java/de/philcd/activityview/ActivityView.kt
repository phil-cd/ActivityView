package de.philcd.activityview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import java.util.*
import java.util.Calendar.*
import java.util.concurrent.TimeUnit

const val MIN_NUM_WEEKS_TO_SHOW_MONTH_LABELS = 5

val monthLabels = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
    "Aug", "Sep", "Oct", "Nov", "Dec"
)

/**
 * Composable representing the full ActivityView with activity boxes and month labels.
 *
 * @param activities Map of dates and activity counts
 * @param numWeeksToShow Number of weeks that should be shown
 * @param noActivityColor Color for days with no activity
 * @param minColor Color for days with the lowest possible activity count
 * @param maxColor Color for days with the highest possible activity count
 * @param boxShape Shape of the activity boxes
 * @param monthLabelFontSize The font size of the month labels.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActivityView(
    activities: MutableMap<Calendar, Int>,
    numWeeksToShow: Int = 52,
    noActivityColor: Color = Color(236, 236, 236, 255),
    minColor: Color = Color(194, 245, 185, 255),
    maxColor: Color = Color(65, 216, 60, 255),
    boxShape: Shape = RectangleShape,
    monthLabelFontSize: TextUnit = 16.sp
) {
    val firstDay = firstVisibleDay(numWeeksToShow)
    val numDaysVisible = getNumberOfVisibleDays(firstDay, today())

    val activityBoxes = getActivityViewBoxes(
        activities,
        firstDay,
        numDaysVisible,
        noActivityColor,
        minColor,
        maxColor,
        boxShape
    )
    val monthLabelTexts = getMonthLabelTexts(monthLabelFontSize)

    Layout(
        contents = monthLabelTexts + activityBoxes
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight

        val monthLabelMeasurables = measurables.subList(0, 12)
        val activityBoxMeasurables = measurables.subList(12, measurables.size)

        // width = numWeeksToShow * boxWidth + (numWeeksToShow-1) * space
        // width = numWeeksToShow * boxWidth + (numWeeksToShow-1) * 0.2 * boxWidth
        // width = (numWeeksToShow + (numWeeksToShow-1) * 0.2) * boxWidth
        // boxWidth = width / (numWeeksToShow + (numWeeksToShow-1) * 0.2)

        val boxWidth = (width / (numWeeksToShow + (numWeeksToShow - 1) * 0.2)).toInt()
        val space = (boxWidth * 0.2).toInt()

        val activityBoxPlaceables = activityBoxMeasurables.map { m ->
            m.first().measure(Constraints(maxWidth = boxWidth, maxHeight = boxWidth))
        }

        val textPlaceables = monthLabelMeasurables.map { m -> m.first().measure(constraints) }
        val textHeight = textPlaceables.first().height

        var monthToWeekIndexList =
            createMonthToWeekIndexList(firstVisibleDay(numWeeksToShow), today())
        monthToWeekIndexList = if (numWeeksToShow < MIN_NUM_WEEKS_TO_SHOW_MONTH_LABELS) {
            mutableListOf()
        } else {
            removeOverlappingMonthLabels(monthToWeekIndexList, boxWidth, space, textPlaceables)
        }

        layout(width, height) {
            // place month labels
            monthToWeekIndexList.forEach { (month, weekIndex) ->
                val xTextCenter = (weekIndex + 2) * (boxWidth + space)
                val textPlaceable = textPlaceables[month]
                val x = xTextCenter - textPlaceable.width / 2
                textPlaceable.place(x, 0)
            }

            // place activity boxes
            for (i in 0 until numDaysVisible) {
                val x = ((i / 7) * (boxWidth + space))
                val y = ((i % 7) * (boxWidth + space)) + textHeight
                activityBoxPlaceables[i].place(x, y)
            }
        }
    }
}

/**
 * Composable representing a single activity box.
 *
 * @param activities Map of dates and activity counts
 * @param activityDate Date of the activity box
 * @param noActivityColor Color for days with no activity
 * @param minColor Color for days with the lowest possible activity count
 * @param maxColor Color for days with the highest possible activity count
 * @param boxShape Shape of the activity box
 */
@Composable
fun ActivityViewBox(
    activities: MutableMap<Calendar, Int>,
    activityDate: Calendar,
    noActivityColor: Color,
    minColor: Color,
    maxColor: Color,
    boxShape: Shape
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(boxShape)
            .background(
                getActivityColor(
                    activities,
                    activityDate,
                    noActivityColor,
                    minColor,
                    maxColor
                )
            )
    )
}

/**
 * Creates a list of the composables for the activity boxes.
 *
 * @param activities Map of dates and activity counts
 * @param firstDay First day that is visible in the ActivityView
 * @param numDays Number of days that are visible in the ActivityView
 * @param noActivityColor Color for days with no activity
 * @param minColor Color for days with the lowest possible activity count
 * @param maxColor Color for days with the highest possible activity count
 * @param boxShape Shape of the activity boxes
 * @return List of the composables for the activity boxes.
 */
fun getActivityViewBoxes(
    activities: MutableMap<Calendar, Int>,
    firstDay: Calendar,
    numDays: Int,
    noActivityColor: Color,
    minColor: Color,
    maxColor: Color,
    boxShape: Shape
): MutableList<@Composable @UiComposable () -> Unit> {
    val boxes = mutableListOf<@Composable @UiComposable () -> Unit>()

    // iterate over all visible days to create one box per day
    for (i in 0 until numDays) {
        // get date
        val activityDate = firstDay.clone() as Calendar
        activityDate.add(DAY_OF_YEAR, i)

        // create box for this day
        boxes.add @Composable {
            ActivityViewBox(
                activities,
                activityDate,
                noActivityColor,
                minColor,
                maxColor,
                boxShape
            )
        }
    }

    return boxes
}

/**
 * Returns the list of all month label texts (January to December).
 *
 * @param monthLabelFontSize The font size of the month labels.
 * @return List of all month label texts (January to December).
 */
fun getMonthLabelTexts(monthLabelFontSize: TextUnit): List<@Composable @UiComposable () -> Unit> {
    return monthLabels.map { monthLabel ->
        @Composable { Text(text = monthLabel, fontSize = monthLabelFontSize) }
    }
}

/**
 * Creates a list that contains pairs that map a month to the index of the week in which the month
 * occurs for the first time in the ActivityView.
 *
 * @param firstDay The first day that is visible in the ActivityView. Needs to be a Monday.
 * @param lastDay the last day that is visible in the ActivityView
 * @return a list that contains pairs that map a month to the index of the week in which the month
 * occurs for the first time in the ActivityView.
 */
fun createMonthToWeekIndexList(
    firstDay: Calendar,
    lastDay: Calendar
): MutableList<Pair<Int, Int>> {
    var monthToWeekIndexList: MutableList<Pair<Int, Int>> = mutableListOf()

    if (firstDay.get(DAY_OF_WEEK) != MONDAY)
        throw IllegalArgumentException("Argument firstDay must be a Monday.")

    if (firstDay > lastDay)
        throw IllegalArgumentException("Argument firstDay should not be after lastDay.")

    var currentDay = firstDay.clone() as Calendar
    var weekIndex = 0
    // add first month to the list (and always store the latest month that was added to the list)
    var previousMonth = currentDay.get(MONTH)
    monthToWeekIndexList.add(Pair(previousMonth, weekIndex))
    while (currentDay <= lastDay) {
        if (previousMonth != currentDay.get(MONTH)) {
            // month has changed
            monthToWeekIndexList.add(Pair(currentDay.get(MONTH), weekIndex))
            previousMonth = currentDay.get(MONTH)
        }

        // go to next day
        currentDay.add(DAY_OF_WEEK, 1)

        // if next day is a monday, increase weekIndex
        if (currentDay.get(DAY_OF_WEEK) == MONDAY) weekIndex++
    }

    return monthToWeekIndexList
}

/**
 * Removes month labels that would overlap with other month labels. The result contains every
 * x-th month label, where x is the smallest number that ensures that no month labels overlap.
 *
 * @param monthToWeekIndexList a list that contains pairs of months and the week index where the month
 * label should be placed in the ActivityView
 * @param boxWidth width of a single activity box
 * @param space space between two activity boxes
 * @param textPlaceables a list of placeables for the month labels
 * @return a list that contains every x-th item of the given list, where x is the smallest number
 * that ensures that no month labels overlap
 */
fun removeOverlappingMonthLabels(
    monthToWeekIndexList: MutableList<Pair<Int, Int>>,
    boxWidth: Int,
    space: Int,
    textPlaceables: List<Placeable>
): MutableList<Pair<Int, Int>> {
    // remove first and last element (they might not be fully visible)
    if (monthToWeekIndexList.size <= 2) return mutableListOf()
    val initialList = monthToWeekIndexList.subList(1, monthToWeekIndexList.size - 1)
    var filteredList = initialList.toMutableList()

    // we start checking if every month label can be fully displayed without overlapping
    // if that does not work, we check if every second label could be displayed and so on
    var showEveryXthText = 1
    while (showEveryXthText < monthToWeekIndexList.size - 2) {
        // test if texts could be shown without overlap
        var previousTextEndX = 0
        var overlap = false
        filteredList.forEach { (month, weekIndex) ->
            // add 2 to weekIndex to (at least approximately) center the text in the month
            val xTextCenter = (weekIndex + 2) * (boxWidth + space)
            val textPlaceable = textPlaceables[month]
            val x = xTextCenter - textPlaceable.width / 2
            if (x <= previousTextEndX) overlap = true
            previousTextEndX = x + textPlaceable.width
        }
        if (overlap) {
            showEveryXthText++
            filteredList = initialList.filterIndexed { index, pair ->
                index % showEveryXthText == 0
            } as MutableList<Pair<Int, Int>>
        } else {
            break
        }
    }
    return filteredList
}

/**
 * Returns the color that should be used for drawing the activity on the given [activityDate].
 * If there is no activity on this date, [noActivityColor] is returned.
 * Otherwise, the color is chosen between [minColor] and [maxColor] based on the activity count.
 *
 * @param activities Map of dates and activity counts
 * @param activityDate Date for which the color should be returned
 * @param noActivityColor Color that is returned if there is no activity on the given date
 * @param minColor Color that is returned for the lowest possible activity count
 * @param maxColor Color that is returned for the highest possible activity count
 * @return Color that should be used for drawing the activity on the given [activityDate] (depends
 * on the activity count on that day).
 */
fun getActivityColor(
    activities: MutableMap<Calendar, Int>,
    activityDate: Calendar,
    noActivityColor: Color,
    minColor: Color,
    maxColor: Color
): Color {
    // max number of activities on one day (here, maxColor is used)
    val maxNumActivities = activities.values.max().toFloat()

    // find the given activityDate in the map
    activities.forEach { (cal, value) ->
        if (cal.compareTo(activityDate) == 0) {
            // activityDate found => return the color based on the activity count
            return if (value > 0) {
                Color(
                    ColorUtils.blendARGB(
                        minColor.toArgb(),
                        maxColor.toArgb(),
                        value.toFloat() / maxNumActivities
                    )
                )
            } else {
                noActivityColor
            }
        }
    }
    return noActivityColor
}

/**
 * Returns the number of days that are visible in the ActivityView starting from [firstDay] and
 * ending on [lastDay].
 *
 * @param firstDay The first day that should be visible in the ActivityView.
 * @param lastDay The last day that should be visible in the ActivityView.
 * @return The number of days that are visible in the ActivityView.
 */
fun getNumberOfVisibleDays(firstDay: Calendar, lastDay: Calendar): Int {
    return TimeUnit.MILLISECONDS.toDays(lastDay.timeInMillis - firstDay.timeInMillis).toInt() + 1
}