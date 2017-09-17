package ml.nextuniverse.bans.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by TheDiamondPicks on 30/07/2017.
 */
public class DateFormatter {
    public static String format(Timestamp time) {
        long difference = time.getTime() - new Date().getTime();
        if (difference != 0L) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(difference);

            Calendar dif = Calendar.getInstance();
            c.setTimeInMillis(0);

            int days = 0;

            if (c.get(Calendar.YEAR) != dif.get(Calendar.YEAR))
                days = 365 * (c.get(Calendar.YEAR) - dif.get(Calendar.YEAR));
            days = days + (c.get(Calendar.DAY_OF_YEAR) - dif.get(Calendar.DAY_OF_YEAR));
            if (days != 0) {
                if (days == 1)
                    return days + " day";
                else
                    return days + " days";
            }
            else {
                int hours;
                hours = (c.get(Calendar.HOUR_OF_DAY) - dif.get(Calendar.HOUR_OF_DAY));
                if (hours != 0) {
                    if (hours == 1)
                        return hours + " hour";
                    else
                        return hours + " hours";
                }
                else {
                    int minutes;
                    minutes = (c.get(Calendar.MINUTE) - dif.get(Calendar.MINUTE));
                    if (minutes != 0) {
                        if (minutes == 1)
                            return minutes + " minute";
                        else
                            return minutes + " minutes";
                    }
                    else {
                        int seconds;
                        seconds = (c.get(Calendar.SECOND) - dif.get(Calendar.SECOND));
                        if (seconds != 0) {
                            if (seconds == 1)
                                return seconds + " second";
                            else
                                return seconds + " seconds";
                        }
                        else {
                            return "A few seconds";
                        }
                    }
                }
            }

        }
        else {
            return "A few seconds";
        }

    }
}
