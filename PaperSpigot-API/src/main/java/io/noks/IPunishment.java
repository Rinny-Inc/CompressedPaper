package io.noks;

import java.util.Calendar;
import java.util.Date;

public interface IPunishment {
	default Date parseDuration(String durationString) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        char unit = durationString.charAt(durationString.length() - 1);
        int amount = Integer.parseInt(durationString.substring(0, durationString.length() - 1));

        switch (unit) {
            case 's':
                cal.add(Calendar.SECOND, amount);
                break;
            case 'm':
                cal.add(Calendar.MINUTE, amount);
                break;
            case 'h':
                cal.add(Calendar.HOUR, amount);
                break;
            case 'd':
                cal.add(Calendar.DAY_OF_MONTH, amount);
                break;
            case 'w':
                cal.add(Calendar.WEEK_OF_YEAR, amount);
                break;
            case 'M':
                cal.add(Calendar.MONTH, amount);
                break;
            case 'y':
                cal.add(Calendar.YEAR, amount);
                break;
            default:
                //throw new IllegalArgumentException("Invalid duration unit: " + unit);
            	return null;
        }
        return cal.getTime();
    }
	
	default boolean isValidDuration(String durationString) {
        return durationString.matches("\\d+[smhdwMy]");
    }
}
