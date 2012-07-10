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

import de.knurt.fam.core.model.persist.booking.QueueBooking;

/**
 * get information about the rules, of how a queue based booking shall be made.
 * 
 * @author Daniel Oltmanns
 * @see QueueBooking
 * @since 24.09.2009
 */
public interface QueueBasedBookingRule {

	/**
	 * return the units that are processed in an hour. this value should be
	 * compute from experience.
	 * 
	 * @return the units that are processed in an hour.
	 */
	public Integer getUnitsPerHourProcessed();

	/**
	 * return the number of units that can be processed in the facility at
	 * once. e.g. if you have a shopping counter it's 1. in a oven may be 10
	 * pizzas.
	 * 
	 * @return the number of units that can be processed in the facility at
	 *         once.
	 */
	public int getMaxPossibleUnitsProcessedAtOnce();

	/**
	 * return the length of the queue that results a booking stop. return 0 for
	 * infinite.
	 * 
	 * @return the length of the queue that results a booking stop.
	 */
	public int getMaxQueueLength();

	/**
	 * return number of units in queue now.
	 * 
	 * @return number of units in queue now.
	 */
	public int getActualQueueLength();

	/**
	 * return the position, a user shall get an reminder mail if the unit is not
	 * there. if no mail shall be send (because unit does not come from user or
	 * it simply shall not be send), return <code>null</code>.
	 * 
	 * @return the position, a user shall get an reminder mail if the unit is
	 *         not there.
	 */
	public Integer reminderMailAtPosition();

	/**
	 * return the position, user's booking shall be canceled if the unit is not
	 * there. if no cancelation shall be made (because unit does not come from
	 * user or it simply shall not be made), return <code>null</code>.
	 * 
	 * @return the position, user's booking shall be canceled if the unit is not
	 *         there.
	 */
	public Integer cancelBookingAtPosition();

	/**
	 * set queue length + 1
	 */
	public void incrementQueue();

	/**
	 * set queue length - 1
	 */
	public void reduceQueue();

	/**
	 * return the units per hour processed as asserted before real life.
	 * 
	 * @return the units per hour processed as asserted before real life.
	 */
	public Integer getAssertUnitsPerHourProcessed();

	/**
	 * set the units per hour processed as asserted before real life.
	 * 
	 * @param assertUnitsPerHourProcessed
	 *            units per hour processed as asserted before real life.
	 */
	public void setAssertUnitsPerHourProcessed(Integer assertUnitsPerHourProcessed);
}
