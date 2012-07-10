/*
 * Copyright 2009-2012 by KNURT Systeme (http://www.knurt.de)
 *
 * Licensed under the Creative Commons License Attribution-NonCommercial-ShareAlike 3.0 Unported;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://creativecommons.org/licenses/by-nc-sa/3.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.knurt.fam.core.util.time;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

/**
 * simple calendar util functions
 * 
 * @author Daniel Oltmanns
 * @since 1.5.0 (09/13/2011)
 */
public class CalendarUtil {

	/** one and only instance of CalendarUtil */
	private volatile static CalendarUtil me;

	/** construct CalendarUtil */
	private CalendarUtil() {
	}

	/**
	 * return the one and only instance of CalendarUtil
	 * 
	 * @return the one and only instance of CalendarUtil
	 */
	public static CalendarUtil getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CalendarUtil.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CalendarUtil();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of CalendarUtil
	 */
	public static CalendarUtil me() {
		return getInstance();
	}

	/**
	 * return the number of days between the given to calendar instances. the
	 * result is always a positive long value.
	 * 
	 * @param c1
	 *            first calendar instance
	 * @param c2
	 *            second calendar instance
	 * @return the number of days between the given to calendar instances
	 */
	public long daysBetween(Calendar c1, Calendar c2) {
		long result = 0l;
		while (!DateUtils.isSameDay(c1, c2)) {
			result++;
			if (c1.before(c2)) {
				c1.add(Calendar.DAY_OF_YEAR, 1);
			} else {
				c2.add(Calendar.DAY_OF_YEAR, 1);
			}
		}
		return result;
	}

	public long daysBetween(Calendar c, Date d) {
		Calendar dc = Calendar.getInstance();
		dc.setTime(d);
		return this.daysBetween(c, dc);
	}
}
