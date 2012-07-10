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

import de.knurt.fam.core.config.FamRequestContainer;

/**
 * if sometimes the system exists in multilanguages, this is the method to show
 * the dates in that language as well. <br />
 * by now, the application often uses {@link Calendar#getInstance()}, which
 * takes the server locale. <br />
 * anyway: the entire application assumes a western world calendar (like there
 * must be 7 days the week).
 * 
 * @author Daniel Oltmanns
 * @since 0.20090518 (05/19/2009)
 */
public class FamCalendar {

	// XXX this is a good class. anyway, the application uses
	// Calendar.getInstance instead - replace it
	/**
	 * return a {@link Calendar} in the requests locale.
	 * 
	 * @see Calendar#getInstance(java.util.Locale)
	 * @return a {@link Calendar} in the requests locale.
	 */
	public static Calendar getInstance() {
		Calendar result = Calendar.getInstance(FamRequestContainer.locale());
		result.set(Calendar.MILLISECOND, 0);
		result.set(Calendar.SECOND, 0);
		return result;
	}

	private FamCalendar() {
	}
}
