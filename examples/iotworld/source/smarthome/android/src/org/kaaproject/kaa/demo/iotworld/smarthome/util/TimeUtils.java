package org.kaaproject.kaa.demo.iotworld.smarthome.util;

public class TimeUtils {

    public static String milliSecondsToTimer(long milliseconds) {
        return milliSecondsToTimer(milliseconds, false);
    }

    public static String milliSecondsToTimer(long milliseconds, boolean extended) {
        StringBuilder timerStringBuilder = new StringBuilder();
        StringBuilder secondsStringBuilder = new StringBuilder();

        // Convert total duration into time in milliseconds.
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours.
        if (hours > 0) {
            timerStringBuilder.append(hours);
            if (extended) {
                timerStringBuilder.append(" h");
            } else {
                timerStringBuilder.append(":");
            }
        }

        // Prepend 0 to seconds if there is one digit.
        if (seconds < 10) {
            secondsStringBuilder.append("0");
        }

        secondsStringBuilder.append(seconds);
        if (extended) {
            secondsStringBuilder.append(" s");
        }

        if (extended) {
            if (hours > 0) {
                timerStringBuilder.append(" ");
            }
            timerStringBuilder.append(minutes);
            timerStringBuilder.append(" m");
        } else {
            timerStringBuilder.append(minutes);
            timerStringBuilder.append(":");
        }
        if (seconds > 0 || !extended) {
            if (extended) {
                timerStringBuilder.append(" ");
                timerStringBuilder.append(secondsStringBuilder);
            } else {
                timerStringBuilder.append(secondsStringBuilder);
            }
        }
        // Return the timer string.
        return timerStringBuilder.toString();
    }

    public static String secondsToTimer(int seconds, boolean extended) {
        return milliSecondsToTimer(seconds * 1000, extended);
    }

}
