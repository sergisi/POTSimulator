package Helpers;

import java.util.Date;

public final class HelperTime {

	public static long GetMillisecondsSeconds(Date start) {

		Date now = new Date();

        return now.getTime() - start.getTime();
	}
	
	

	public static String GetFormattedInterval(Date start) {

		String result = "";
		long interval = GetMillisecondsSeconds(start);

		if(interval > 60000)
		{
			result = (interval / 60000) + "min " + ((interval % 60000)/1000) + "s";
		}
		else if (interval > 1000) {

			result = (interval / 1000) + "s " + (interval % 1000) + "ms";
		} else {
			result = interval + "ms";
		}

		return (result);
	}

}
