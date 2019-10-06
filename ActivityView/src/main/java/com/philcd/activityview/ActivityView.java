package com.philcd.activityview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

public class ActivityView extends View {

    private int dp6;

    /**
     * Paint used to draw fields that are enabled.
     */
    private Paint paintEnabled;

    /**
     * Paint used to draw fields that are disabled.
     */
    private Paint paintDisabled;

    /**
     * Paint used for the text showing the months.
     */
    private Paint paintText;

    private int textHeight;

    /**
     * Calendar of the first day that gets shown in the ActivityView.
     */
    private Calendar calendarStart;

    /**
     * Calendar of the last day that gets shown in the ActivityView.
     */
    private Calendar calendarEnd;

    private boolean[] enabled;

    public ActivityView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ActivityView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initCalendar();

        // init enabled array
        enabled = new boolean[getDaysCount()];

        int colorEnabled = context.getColor(R.color.enabled);
        int colorDisabled = context.getColor(R.color.disabled);
        int colorText = context.getColor(R.color.text);

        if(attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.ActivityView,
                    0, 0);

            try {
                colorEnabled = a.getColor(R.styleable.ActivityView_color_enabled, colorEnabled);
                colorDisabled = a.getColor(R.styleable.ActivityView_color_disabled, colorDisabled);
                colorText = a.getColor(R.styleable.ActivityView_color_text, colorText);
            } finally {
                a.recycle();
            }
        }


        dp6 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6,getResources().getDisplayMetrics());

        paintEnabled = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintEnabled.setStyle(Paint.Style.FILL);
        paintEnabled.setColor(colorEnabled);

        paintDisabled = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDisabled.setStyle(Paint.Style.FILL);
        paintDisabled.setColor(colorDisabled);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(colorText);
        int spSize = 14;
        float scaledSizeInPixels = spSize * getResources().getDisplayMetrics().scaledDensity;
        paintText.setTextSize(scaledSizeInPixels);
    }

    /**
     * Sets calendarStart and calendarEnd.
     * These are the calendars for the first day that gets shown in the ActivityView
     * and the last day that gets shown in the ActivityView.
     */
    private void initCalendar() {
        calendarStart = Calendar.getInstance();
        calendarStart.setFirstDayOfWeek(Calendar.MONDAY);
        calendarStart.add(Calendar.WEEK_OF_YEAR, -51);
        calendarStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        calendarEnd = Calendar.getInstance();
        calendarEnd.setFirstDayOfWeek(Calendar.MONDAY);

        resetCalendarTime(calendarStart);
        resetCalendarTime(calendarEnd);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int widthItems = (int)(width*0.7);
        int widthSpaces = width-widthItems;

        double widthItem = (widthItems/52.0);
        double spaceItem = (widthSpaces/51.0);

        int height = (int)(7*widthItem+6*spaceItem);

        Rect bounds = new Rect();
        paintText.getTextBounds("Ttpqyjg", 0, 9, bounds);

        textHeight = bounds.bottom-bounds.top + dp6;

        height += textHeight;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // get width
        int width = canvas.getWidth();
        // example: 5 Weeks: W-W-W-W-W (5x W plus 4x space)

        // 70 percent of the width should be used for the fields/items
        int widthItems = (int)(width*0.7);
        // 30 percent of the width should be used for the spaces between the fields/items
        int widthSpaces = width-widthItems;

        // we have 52 fields and 51 spaces
        double widthItem = (widthItems/52.0);
        double spaceItem = (widthSpaces/51.0);

        // get current day of week
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int daysThisWeek = getDaysThisWeek();

        // set calendar to first date that gets shown in the ActivityView
        calendar.add(Calendar.WEEK_OF_YEAR, -51);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // nextMonthIn variable is used to determine if the current month should
        // be displayed as a text
        // needed because not every month should be displayed as a text above the ActivityView
        // nextMonthIn = 0 means: display month text on next 1st of a month
        // nextMonthIn = 1 means: display month text on the 1st of the month after the next one
        int nextMonthIn = 0;

        // 52 weeks should be displayed
        // 51 full weeks and then the current week
        for(int week = 0; week < 52; week++) {
            int dayMax = 7; // normal week is a full 7-day week
            if(week == 51) {
                // current week / last week that gets displayed
                dayMax = daysThisWeek;
            }

            // posX for all items of this week
            float posX = (float) ((week * widthItem) + (week * spaceItem));

            // iterate through days of week
            for (int day = 0; day < dayMax; day++) {
                // posY for this item
                float posY = textHeight + (float) ((day * widthItem) + (day * spaceItem));

                if(calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                    if(nextMonthIn == 0) {
                        canvas.drawText(getMonthString(calendar.get(Calendar.MONTH)), posX, textHeight - dp6, paintText);
                        nextMonthIn = 1;
                    } else {
                        nextMonthIn--;
                    }
                }

                // check if the current field should be enabled or not
                int currentField = (week*7) + day;

                if(enabled[currentField]) {
                    canvas.drawRect(posX, posY, (float) (posX + widthItem), (float) (posY + widthItem), paintEnabled);
                } else {
                    canvas.drawRect(posX, posY, (float) (posX + widthItem), (float) (posY + widthItem), paintDisabled);
                }

                // go to next day
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
    }

    /**
     * Converts number of month to corresponding string.
     * @param i Number between 0 and 11. 0 stands for January, 11 for December.
     * @return String corresponding to the given month number.
     */
    private String getMonthString(int i) {
        i++; // because otherwise there's a month 0
        switch(i) {
            case 1:
                return getContext().getString(R.string.month_1);
            case 2:
                return getContext().getString(R.string.month_2);
            case 3:
                return getContext().getString(R.string.month_3);
            case 4:
                return getContext().getString(R.string.month_4);
            case 5:
                return getContext().getString(R.string.month_5);
            case 6:
                return getContext().getString(R.string.month_6);
            case 7:
                return getContext().getString(R.string.month_7);
            case 8:
                return getContext().getString(R.string.month_8);
            case 9:
                return getContext().getString(R.string.month_9);
            case 10:
                return getContext().getString(R.string.month_10);
            case 11:
                return getContext().getString(R.string.month_11);
            case 12:
                return getContext().getString(R.string.month_12);
            default:
                return getContext().getString(R.string.month_1);
        }
    }

    /**
     * Enables or disabled the field in the ActivityView by the given date from the calendar.
     * @param c Calendar with the given date that should be enabled or disabled in the ActivityView.
     * @param enabled Wether field should be enabled or disabled.
     * @return False if given calendar is null or not between start and end of the ActivityView. True if a field got enabled or disabled.
     */
    public boolean setEnabled(Calendar c, boolean enabled) {
        if(c == null) return false;

        resetCalendarTime(c);
        if(c.before(calendarStart)) return false;
        if(c.after(calendarEnd)) return false;

        int daysBetween = getDaysBetween(calendarStart, c);
        this.enabled[daysBetween] = enabled;
        return true;
    }

    /**
     * Counts the days between two calendars.
     * c1 has to be before or equal to c2.
     * If c1 and c2 got the same date, 0 gets returned.
     * If c1 is one day before c2, 1 gets returned.
     * @param c1 First calendar, needs to be before c2 or equal to c2.
     * @param c2 Second calendar, needs to be after c1 or equal to c1.
     * @return Count of days between the given calendars.
     */
    private int getDaysBetween(Calendar c1, Calendar c2) {
        if(!c2.after(c1)) return 0;

        int days = 0;
        while(c1.before(c2)) {
            days++;
            c1.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }

    /**
     * Returns the count of days that get shown in the ActivityView.
     * Includes the first and last day.
     * Since the ActivityView shows 51 full weeks and the current one,
     * we count 51 full weeks and add the days of the current/last week.
     * @return Count of days that get shown in the ActivityView including the first and last day.
     */
    public int getDaysCount() {
        return 51*7 + getDaysThisWeek();
    }

    /**
     * Gets the count of days in the current week (or the last week that gets shown in the ActivityView).
     * @return Count of days in the current week (or the last week that gets shown in the ActivityView).
     */
    private int getDaysThisWeek() {
        int dayOfWeek = calendarEnd.get(Calendar.DAY_OF_WEEK);
        switch(dayOfWeek) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            case Calendar.SUNDAY:
                return 7;
            default:
                return 1;
        }
    }

    /**
     * Sets time of day to 00:00.
     * @param c Calendar with time set to 00:00.
     */
    private void resetCalendarTime(Calendar c) {
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Returns the calendar of the first day that gets shown in the ActivityView.
     * @return Calendar of the first day that gets shown in the ActivityView.
     */
    public Calendar getCalendarStart() {
        return this.calendarStart;
    }

    /**
     * Returns the calendar of the last day that gets shown in the ActivityView.
     * @return Calendar of the last day that gets shown in the ActivityView.
     */
    public Calendar getCalendarEnd() {
        return this.calendarEnd;
    }
}

