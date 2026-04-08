package com.payoda.smartlock.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    public static SimpleDateFormat DATE_TIME_NOTIFICATION_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static String YYYYMMDD = "yyyy-MM-dd";
    public static String DDMMYYYY = "dd-MM-yyyy";

    public static String getDate(String dateTime) {
        return getDate(dateTime, null);
    }

    public static final SimpleDateFormat utcFormatWithoutMilli = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public static String getDate(String dateTime, SimpleDateFormat dt) {
        try {
            if (dt == null)
                dt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            dateTime = dt1.format(dt.parse(dateTime));
        } catch (Exception e) {
            Logger.e(e);
        }
        return dateTime;
    }

    public static String getDateAndTime(String dateTime, SimpleDateFormat dt) {
        try {
            if (dt == null)
                dt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault());
            dateTime = dt1.format(dt.parse(dateTime));
        } catch (Exception e) {
            Logger.e(e);
        }
        return dateTime;
    }

    public static String getTime(String dateTime) {
        return getTime(dateTime, null);
    }

    public static String getTime(String dateTime, SimpleDateFormat dt) {
        try {
            if (dt == null)
                dt = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            SimpleDateFormat dt1 = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
            dateTime = dt1.format(dt.parse(dateTime));
        } catch (Exception e) {
            Logger.e(e);
        }
        return dateTime;
    }

    public static String getHistoryTime(String dateTime) {
        try {

            SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat dt1 = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
            dateTime = dt1.format(dt.parse(dateTime));
        } catch (Exception e) {
            Logger.e(e);
        }
        return dateTime;
    }

    public static boolean isBetweenDate(String startDate, String endDate) {
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fromDate = dt.parse(startDate);
            Date toDate = dt.parse(endDate);
            Date calendar = Calendar.getInstance().getTime();
            String currentDate = dt.format(calendar);
            Date todayDate = dt.parse(currentDate);
            return (todayDate.after(fromDate) || currentDate.equalsIgnoreCase(startDate)) && (todayDate.before(toDate) || currentDate.equalsIgnoreCase(endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isBetweenTime(String startTime, String endTime) {

        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";

        boolean valid = false;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(new Date());

        if (startTime.matches(reg) && endTime.matches(reg) && currentTime.matches(reg)) {
            try {

                //Start Time
                Date inTime = new SimpleDateFormat("HH:mm:ss").parse(startTime);
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(inTime);

                //Current Time
                Date checkTime = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
                Calendar calendar3 = Calendar.getInstance();
                calendar3.setTime(checkTime);

                //End Time
                Date finTime = new SimpleDateFormat("HH:mm:ss").parse(endTime);
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(finTime);


                if (endCal.getTime().before(startCal.getTime())) {
                    endCal.add(Calendar.DATE, 1);
                }

                Date actualTime = calendar3.getTime();

                if (startCal.getTime().before(endCal.getTime()) || startCal.getTime().compareTo(endCal.getTime()) == 0) {
                    Logger.d("Start Time b4 End Time");
                    if ((actualTime.after(startCal.getTime()) || actualTime.compareTo(startCal.getTime()) == 0)) {
                        Logger.d("Actual Time After Start Time");
                        if (actualTime.before(endCal.getTime())) {
                            Logger.d("Actual Time B4 End Time");
                            valid = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return valid;
        } else {
            Logger.i("Not a valid time, expecting HH:MM:SS format");
        }

        return valid;
    }

    public static boolean isBetweenDate(String startDate, String endDate, String serverTime) {

        Logger.d("%%% UTC Start Date " + startDate);
        Logger.d("%%% UTC End Date " + endDate);
        Logger.d("%%% UTC Server Time " + serverTime);

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fromDate = dt.parse(startDate);
            Date toDate = dt.parse(endDate);
            //String currentDate = dt.format(serverTime);
            String[] dateValue = serverTime.split(" ");
            if (dateValue.length != 2)
                return false;
            String currentDate = dateValue[0];
            Date todayDate = dt.parse(serverTime);
            return (todayDate.after(fromDate) || currentDate.equalsIgnoreCase(startDate))
                    && (todayDate.before(toDate) || currentDate.equalsIgnoreCase(endDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isBetweenTime(String startTime, String endTime, String serverTime) {

        Logger.d("*** UTC Start Time " + startTime);
        Logger.d("*** UTC End Time " + endTime);
        Logger.d("*** UTC Server Time " + serverTime);

        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
        boolean valid = false;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String[] dateValue = serverTime.split(" ");
        if (dateValue.length != 2)
            return false;
        String currentTime = dateValue[dateValue.length - 1];
        if (startTime.matches(reg) && endTime.matches(reg) && currentTime.matches(reg)) {
            try {

                //Start Time
                Date inTime = new SimpleDateFormat("HH:mm:ss").parse(startTime);
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(inTime);

                //Current Time
                Date checkTime = new SimpleDateFormat("HH:mm:ss").parse(currentTime);
                Calendar currentCal = Calendar.getInstance();
                currentCal.setTime(checkTime);

                //End Time
                Date finTime = new SimpleDateFormat("HH:mm:ss").parse(endTime);
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(finTime);

                if (endCal.getTime().before(startCal.getTime())) {
                    endCal.add(Calendar.DATE, 1);
                }


                Date actualTime = currentCal.getTime();

                if (startCal.getTime().before(endCal.getTime()) || startCal.getTime().compareTo(endCal.getTime()) == 0) {
                    Logger.d("Start Time b4 End Time");
                    if ((actualTime.after(startCal.getTime()) || actualTime.compareTo(startCal.getTime()) == 0)) {
                        Logger.d("Actual Time After Start Time");
                        if (actualTime.before(endCal.getTime())) {
                            Logger.d("Actual Time B4 End Time");
                            valid = true;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return valid;
        } else {
            Logger.i("Not a valid time, expecting HH:MM:SS format");
        }
        return valid;
    }

    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(calendar.getTime());
    }

    public static String getNextDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(calendar.getTime());
    }

    public static String getLocalDateFromGMT(String dateAndTimeString) {
        try {

            // Notification Adapter
            Logger.d("### UTC Date " + dateAndTimeString);

            // Create a SimpleDateFormat for the correct input format (UTC)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date utcDate = sdf.parse(dateAndTimeString);
            SimpleDateFormat reqFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa", Locale.getDefault());
            TimeZone defaultTimeZone = TimeZone.getTimeZone("GMT");
            reqFormat.setTimeZone(defaultTimeZone);
            dateAndTimeString = reqFormat.format(utcDate);

            Logger.d("### Local Date " + dateAndTimeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateAndTimeString;
    }

    public static String getLocalDateFromGMT(String dateAndTimeString, String timeString) {
        try {

            // History adapter

            dateAndTimeString = dateAndTimeString + " " + timeString;
            Logger.d("### UTC Date " + dateAndTimeString);

            // Create a SimpleDateFormat for the correct input format (UTC)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date utcDate = sdf.parse(dateAndTimeString);
            SimpleDateFormat reqFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa", Locale.getDefault());
            TimeZone defaultTimeZone = TimeZone.getTimeZone("GMT");
            reqFormat.setTimeZone(defaultTimeZone);
            dateAndTimeString = reqFormat.format(utcDate);

            Logger.d("### Local Date " + dateAndTimeString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dateAndTimeString;
    }

    public static String getGMTTimeFromLocal(String dateString, String timeString) {
        dateString = dateString + " " + timeString;
        try {
            Logger.d("### Local date " + dateString);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getDefault());

            SimpleDateFormat utcDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcDateFormat.parse(dateString);

            String formattedDate = utcDateFormat.format(date);
            Logger.d("### UTC Date " + formattedDate);
            return formattedDate;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;


    }

    public static String YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static String DDMMYY_HHMMSS = "dd-MM-yy\0 HH:mm:ss\0";

    public static String getLocalDateFromGMT(String dateString, String timeString, String requiredFormat) {
        try {
            dateString = dateString + " " + timeString;
            Logger.d("### UTC Date " + dateString);

            // Create a SimpleDateFormat with the desired format and local time zone
            SimpleDateFormat reqFormat = new SimpleDateFormat(requiredFormat, Locale.getDefault());
            reqFormat.setTimeZone(TimeZone.getDefault()); // Use the default time zone

            // Format the input date and time without adjusting for timezone
            dateString = reqFormat.format(reqFormat.parse(dateString));
            Logger.d("### Local Date " + dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    public static String getCurrentGMTTime(String requiredFormat) {

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat(requiredFormat);
        Logger.d("### getCurrentGMTTime = " + sdf.format(calendar.getTime()));
        return sdf.format(calendar.getTime());
    }

    public static String getLocalTimeZoneId() {
        TimeZone localTimeZone = TimeZone.getDefault();
        String localTimeZoneId = localTimeZone.getID();
        return localTimeZoneId;
    }

}
