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

/**
 * rules for booking a facility forced to user with a specific role
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (09/01/2010)
 */
public interface SetOfRulesForARole {

	/**
	 * return units of smallest minutes bookable that must be booked at least.
	 * every facility can be booked for at least n minutes. this n minutes cannot
	 * be set directly in the system, but the units of smallest minutes
	 * bookable.
	 * 
	 * @see #getSmallestMinutesBookable()
	 * @return units of smallest minutes bookable that must be booked at least.
	 */
	public int getMinBookableTimeUnits();

	/**
	 * set the units of smallest minutes bookable. this is for the minimum
	 * minutes that can be booked.
	 * 
	 * @see #getMinBookableTimeUnits()
	 * @param minBookableTimeUnits
	 *            the units of smallest minutes bookable.
	 */
	public void setMinBookableTimeUnits(int minBookableTimeUnits);

	/**
	 * return units of smallest minutes bookable that must can be booked at
	 * maximum. every facility can be booked for maximum n minutes. this n minutes
	 * cannot be set directly in the system, but the units of smallest minutes
	 * bookable.
	 * 
	 * @see #getSmallestMinutesBookable()
	 * @return units of smallest minutes bookable that can be booked at maximum.
	 */
	public int getMaxBookableTimeUnits();

	/**
	 * set the units of maximum minutes bookable. this is for the maximum
	 * minutes that can be booked.
	 * 
	 * @see #getMaxBookableTimeUnits()
	 * @param maxBookableTimeUnits
	 *            the units of maximal minutes bookable.
	 */
	public void setMaxBookableTimeUnits(int maxBookableTimeUnits);

	/**
	 * return the units of facilities, that must be booked at once. on facilities
	 * with more then one capacity unit, there may be a minimum count of units
	 * to book. for example: if the facility is 24 eggs, you may want set 6 eggs
	 * at minimum. if the facility is the unit self, return 1.
	 * 
	 * @see FacilityBookable#getCapacityUnits()
	 * @return the units of facilities, that must be booked at once.
	 */
	public int getMinBookableCapacityUnits();

	/**
	 * the minimum bookable capactiy units to set.
	 * 
	 * @see #getMinBookableCapacityUnits()
	 * @param minBookableCapacityUnits
	 *            to set
	 */
	public void setMinBookableCapacityUnits(int minBookableCapacityUnits);

	/**
	 * return the units of facilities, that can be booked at once. on facilities
	 * with more then one capacity unit, there may be a maximum count of units
	 * to book. for example: if the facility is 24 eggs, you may want set 12
	 * eggs at maximum. if the facility is the unit self, return 1.
	 * 
	 * @see FacilityBookable#getCapacityUnits()
	 * @return the units of facilities, that can be booked maximal at once.
	 */
	public int getMaxBookableCapacityUnits();

	/**
	 * the max bookable capacity units to set.
	 * 
	 * @see #getMaxBookableCapacityUnits()
	 * @param maxBookableCapacityUnits
	 *            to set
	 */
	public void setMaxBookableCapacityUnits(int maxBookableCapacityUnits);

	/**
	 * return the minutes the reminder mail must be send to the booker. before a
	 * session starts, a reminder mail can be send to the booker. return the
	 * minutes the reminder mail shall be sent before the session starts or
	 * <code>null</code>, if no mail shall be sent.
	 * 
	 * @see CronjobAction
	 * @return the minutes the reminder mail must be send to the booker.
	 */
	public int getReminderMailMinutesBeforeStarting();

	/**
	 * set the minutes for the reminder mail.
	 * 
	 * @see #getReminderMailMinutesBeforeStarting()
	 * @param reminderMailMinutesBeforeStarting
	 *            to set
	 */
	public void setReminderMailMinutesBeforeStarting(int reminderMailMinutesBeforeStarting);
}
