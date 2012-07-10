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
package de.knurt.fam.core.model.config;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.persist.booking.TimeBooking;

/**
 * rules for booking a facility at a specific time.
 * 
 * @see TimeBooking
 * @author Daniel Oltmanns
 * @since 24.09.2009
 */
public class TimeBasedBookingRule extends AbstractBookingRule {

	private int smallestMinutesBookable;
	private String smallestTimeLabelEqualsOneXKey;
	private Integer mustStartAt;
	private int earliestPossibilityToBookFromNow = 0;

	/**
	 * return earliest time to book or apply for the facility for those who have
	 * to wait.
	 * 
	 * @see de.knurt.fam.core.aspects.security.auth.FamAuth#BOOK_WITHOUT_TIME_BARRIER
	 * @return earliest time to book or apply for the facility for those who
	 *         have to wait.
	 */
	public int getEarliestPossibilityToBookFromNow() {
		return earliestPossibilityToBookFromNow;
	}

	/**
	 * earliest time to book or apply for the facility for those who have to
	 * wait.
	 * 
	 * @see de.knurt.fam.core.aspects.security.auth.FamAuth#BOOK_WITHOUT_TIME_BARRIER
	 * @param earliestPossibilityToBookFromNow
	 */
	public void setEarliestPossibilityToBookFromNow(int earliestPossibilityToBookFromNow) {
		this.earliestPossibilityToBookFromNow = earliestPossibilityToBookFromNow;
	}

	/**
	 * return the smallest time unit to book in minutes. that may be 24*60
	 * minutes for a hired car or 15 minutes for booking a chairoplane.
	 * 
	 * @return the smallestMinutesBookable
	 */

	public int getSmallestMinutesBookable() {
		return smallestMinutesBookable;
	}

	/**
	 * @param smallestMinutesBookable
	 *            the smallestMinutesBookable to set
	 */
	@Required
	public void setSmallestMinutesBookable(int smallestMinutesBookable) {
		this.smallestMinutesBookable = smallestMinutesBookable;
	}

	/**
	 * return a label for the smalles time. if the smallest time unit is not
	 * just minutes but a special meaning, this is the key to set here. a
	 * special meaning might be "school hour" (for 45 minutes), "night" (for
	 * 24*60 minutes in a hotel) et cetera. if this is null, "minutes" will be
	 * choosen.
	 * 
	 * @return the smallestTimeLabelEqualsOneXKey
	 */

	public String getSmallestTimeLabelEqualsOneXKey() {
		return smallestTimeLabelEqualsOneXKey;
	}

	/**
	 * @param smallestTimeLabelEqualsOneXKey
	 *            the smallestTimeLabelEqualsOneXKey to set
	 */
	@Required
	public void setSmallestTimeLabelEqualsOneXKey(String smallestTimeLabelEqualsOneXKey) {
		this.smallestTimeLabelEqualsOneXKey = smallestTimeLabelEqualsOneXKey;
	}

	/**
	 * return minutes of the day the first possible booking can be made. e.g. 0
	 * when only start is possible on full hours. if null, every start is
	 * possible.
	 * 
	 * @return the mustStartAt
	 */

	public Integer getMustStartAt() {
		return mustStartAt;
	}

	/**
	 * @param mustStartAt
	 *            the mustStartAt to set
	 */
	@Required
	public void setMustStartAt(Integer mustStartAt) {
		this.mustStartAt = mustStartAt;
	}

	public int getBookingStrategy() {
		return BookingStrategy.TIME_BASED;
	}

	public boolean isSessionStartable() {
		return false;
	}
}
