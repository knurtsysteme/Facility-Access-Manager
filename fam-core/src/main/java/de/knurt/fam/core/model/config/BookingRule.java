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

import java.util.List;
import java.util.Map;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.view.html.factory.FamFormFactory;

/**
 * rules for booking a facility
 * 
 * @author Daniel Oltmanns
 * @since 0.20090924
 */
public interface BookingRule {

	/**
	 * return the facility specific booking rule for the user.
	 * 
	 * @param user
	 *            the facility specific booking rule is returned for
	 * @return the facility specific booking rule for the user.
	 */
	public SpecificRights4RoleOnFacility getSpecificRights4UserOnFacility(User user);

	/**
	 * return a set of rules forced for the given role. if the role is not
	 * configured for this facility or null is given, return the default.
	 * 
	 * @return a set of rules forced for the given role. if the role is not
	 *         configured for this facility or null is given, return the default.
	 */
	public SetOfRulesForARole getSetOfRulesForARole(User user);

	/**
	 * return a set of rules forced for users where specific rules are not
	 * configured for.
	 * 
	 * @return a set of rules forced for users where specific rules are not
	 *         configured for.
	 */
	public SetOfRulesForARole getDefaultSetOfRulesForARole();

	/**
	 * set a set of rules forced for users where specific rules are not
	 * configured for.
	 */
	public void setDefaultSetOfRulesForARole(SetOfRulesForARole defaultSetOfRulesForARole);

	/**
	 * set a map of a set of rules forced by a specific role. the role key is
	 * given in the map key. the rules is dedicated to the value.
	 * 
	 * @param setsOfRulesForARole
	 *            to set
	 */
	public void setSetsOfRulesForARole(Map<String, SetOfRulesForARole> setsOfRulesForARole);

	/**
	 * return the label for the given units of time. return it with leading
	 * unit.
	 * 
	 * @param units
	 *            time units used to generate the label
	 * @return the label for the given units of time.
	 */
	public String getTimeLabel(int units);

	/**
	 * return the name for the given capacity units. return it
	 * <strong>without</strong> leading unit.
	 * 
	 * @param units
	 *            capacity units used to generate the label
	 * @return the label for the given capacity units.
	 */
	public String getCapacityUnitName(int units);

	/**
	 * return the key for the booking rule. the key for the booking rule is
	 * typicaly the key of the facility.
	 * 
	 * @see Facility#getKey()
	 * @return the key for the booking rule.
	 */
	public String getKey();

	/**
	 * set the key for the booking rule.
	 * 
	 * @see #getKey()
	 * @param key
	 *            to set
	 */
	public void setKey(String key);

	/**
	 * return the smalles minutes bookable. every facility must be booked in
	 * steps of n minutes. return this minutes of one step.
	 * 
	 * @return the smalles minutes bookable.
	 */
	public int getSmallestMinutesBookable();

	/**
	 * return true, if the session can be started manually
	 * 
	 * @return true, if the session can be started manually
	 */
	public boolean isSessionStartable();

	/**
	 * set the minutes of one step as smalles bookable minutes.
	 * 
	 * @see #getSmallestMinutesBookable()
	 * @param smallestMinutesBookable
	 *            value to set
	 */
	public void setSmallestMinutesBookable(int smallestMinutesBookable);

	/**
	 * return the facility, the booking rule is for.
	 * 
	 * @return the facility, the booking rule is for.
	 */
	public FacilityBookable getFacility();

	/**
	 * set the facility, the booking rule is for.
	 * 
	 * @param facility
	 *            the booking rule is for.
	 */
	public void setFacility(FacilityBookable facility);

	/**
	 * return the name of the smallest time label. if the smalles time label has
	 * a special meaning, this might be "one shool hour", "one decade" etc.
	 * almost always this is minutes. this is why this case is assumed, when
	 * <code>null</code> returned.
	 * 
	 * @see FamFormFactory#getUnspecifiedTimeInput()
	 * @return the name of the smallest time label.
	 */
	public String getSmallestTimeLabelEqualsOneXKey();

	/**
	 * set the name of the smallest time label.
	 * 
	 * @param smallestTimeLabelEqualsOneXKey
	 *            the name of the smallest time label.
	 * @see #getSmallestTimeLabelEqualsOneXKey()
	 */
	public void setSmallestTimeLabelEqualsOneXKey(String smallestTimeLabelEqualsOneXKey);

	/**
	 * return the time, when a booking must start. the time can be set in full
	 * hours only. if you set this e.g. to 14, all bookings must start at 2pm or
	 * {@link #getSmallestMinutesBookable()} minutes away from that. if this is
	 * <code>null</code>, use any other rule.
	 * 
	 * @return the time, when a booking must start.
	 */
	public Integer getMustStartAt();

	/**
	 * set hour for "must start".
	 * 
	 * @see #getMustStartAt()
	 * @param mustStartAt
	 *            to set
	 */
	public void setMustStartAt(Integer mustStartAt);

	/**
	 * set special booking rules for special roles or null for no special rules.
	 * 
	 * @see #getSpecificRights4UserOnFacility(de.knurt.fam.core.model.persist.User)
	 * @param specificRights4Role
	 *            list of booking rules for user on this facility
	 */
	public void setSpecificRights4UserOnFacility(List<SpecificRights4RoleOnFacility> specificRights4Role);

	/**
	 * return the strategy, this booking rule can be used with.
	 * 
	 * @see BookingStrategy
	 * @return the strategy, this booking rule can be used with.
	 */
	public int getBookingStrategy();

	/**
	 * return the minimal capacity units to book as text. return it with leading
	 * unit. same as
	 * <code>getCapacityUnitName(getMinBookableCapacityUnits)</code>
	 * 
	 * @see #getCapacityUnitName(int)
	 * @return the minimal capacity units to book as text.
	 */
	public String getCapacityLabelOfMin(User user);

	/**
	 * return the maximal capacity units to book as text. return it with leading
	 * unit. same as
	 * <code>getCapacityUnitName(getMaxBookableCapacityUnits)</code>
	 * 
	 * @see #getCapacityUnitName(int)
	 * @return the maximal capacity units to book as text.
	 */
	public String getCapacityLabelOfMax(User user);

	/**
	 * return the minimal time label to book. return it with leading unit. same
	 * as
	 * <code>getMinBookableTimeUnits + " " + getCapacityUnitName(getMinBookableTimeUnits)</code>
	 * 
	 * @see #getCapacityUnitName(int)
	 * @return the minimal time label to book.
	 */
	public String getTimeLabelOfMin(User user);

	/**
	 * return the maximal time label to book. return it with leading unit. same
	 * as
	 * <code>getMaxBookableTimeUnits + " " + getTimeLabel(getMaxBookableTimeUnits)</code>
	 * 
	 * @see #getCapacityUnitName(int)
	 * @return the maximal time label to book.
	 */
	public String getTimeLabelOfMax(User user);

	/**
	 * same as {@link #getSetOfRulesForARole(User)
	 * #getMinBookableCapacityUnits(User)}
	 */
	public int getMinBookableCapacityUnits(User user);

	/**
	 * same as {@link #getSetOfRulesForARole(User)
	 * #getMaxBookableCapacityUnits(User)}
	 */
	public int getMaxBookableCapacityUnits(User user);

	/**
	 * same as {@link #getSetOfRulesForARole(User)
	 * #getMinBookableTimeUnits(User)}
	 */
	public int getMinBookableTimeUnits(User user);

	/**
	 * same as {@link #getSetOfRulesForARole(User)
	 * #getMaxBookableTimeUnits(User)}
	 */
	public int getMaxBookableTimeUnits(User user);
}
