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

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.model.persist.User;

/**
 * a rule for booking a facility is defined here. every bookable facility has at
 * least one rule for booking. the rule says, who can book the facility when, for
 * how long and how many units are allowed.
 * 
 * @author Daniel Oltmanns
 * @see FacilityBookable
 * @since 0.20090614
 */
public abstract class AbstractBookingRule implements BookingRule {

	private List<SpecificRights4RoleOnFacility> bookingRule4Roles;
	private FacilityBookable facility;
	private String key;
	private Map<String, SetOfRulesForARole> setsOfRulesForARole;
	private SetOfRulesForARole defaultSetOfRulesForARole;

	public SpecificRights4RoleOnFacility getSpecificRights4UserOnFacility(User user) {
		SpecificRights4RoleOnFacility result = null;
		if (this.bookingRule4Roles != null) {
			for (SpecificRights4RoleOnFacility candidate : this.bookingRule4Roles) {
				if (user.getRoleId().equals(candidate.getRole().getKey())) {
					result = candidate;
					break;
				}
			}
		}
		return result;
	}

	public void setDefaultSetOfRulesForARole(SetOfRulesForARole defaultSetOfRulesForARole) {
		this.defaultSetOfRulesForARole = defaultSetOfRulesForARole;
	}

	public SetOfRulesForARole getDefaultSetOfRulesForARole() {
		return this.defaultSetOfRulesForARole;
	}

	public SetOfRulesForARole getSetOfRulesForARole(User user) {
		if (user != null && this.setsOfRulesForARole != null && this.setsOfRulesForARole.containsKey(user.getRoleId())) {
			return this.setsOfRulesForARole.get(user.getRoleId());
		} else {
			return this.defaultSetOfRulesForARole;
		}
	}

	public void setSetsOfRulesForARole(Map<String, SetOfRulesForARole> setsOfRulesForARole) {
		this.setsOfRulesForARole = setsOfRulesForARole;
	}

	public String getCapacityLabelOfMin(User user) {
		return this.getSetOfRulesForARole(user).getMinBookableCapacityUnits() + " " + this.getCapacityUnitName(this.getSetOfRulesForARole(user).getMinBookableCapacityUnits());
	}

	public String getCapacityLabelOfMax(User user) {
		return this.getSetOfRulesForARole(user).getMaxBookableCapacityUnits() + " " + this.getCapacityUnitName(this.getSetOfRulesForARole(user).getMaxBookableCapacityUnits());
	}

	public String getTimeLabelOfMax(User user) {
		return this.getTimeLabel(this.getSetOfRulesForARole(user).getMaxBookableTimeUnits());
	}

	public String getTimeLabelOfMin(User user) {
		return this.getTimeLabel(this.getSetOfRulesForARole(user).getMinBookableTimeUnits());
	}

	public String getTimeLabel(int units) {
		String result = "";
		if (this.getSmallestTimeLabelEqualsOneXKey() != null) {
			result = units + " " + this.getLabel("label.time." + this.getSmallestTimeLabelEqualsOneXKey() + ".%s", units);
		} else {
			int unitTmpCount = units * this.getSmallestMinutesBookable();
			// XXX use de.knurt.util.text.DurationAdapter
			if (unitTmpCount == 1) {
				result = unitTmpCount + " minute"; // INTLANG
			} else if (unitTmpCount < 60) {
				result = unitTmpCount + " minutes"; // INTLANG
			} else if (unitTmpCount == 60) {
				result = "1 hour"; // INTLANG
			} else { // more then 60 minutes
				int hours = (int) Math.floor(unitTmpCount / 60);
				int minutes = unitTmpCount % 60;
				if (hours >= 24) {
					int days = (int) Math.floor(hours / 24);
					if (days == 1) {
						result = "1 day"; // INTLANG
					} else {
						result = days + " days"; // INTLANG
					}
					hours = hours % 24;
				}
				if (hours == 1) {
					result += hours + " hour"; // INTLANG
				} else if (hours > 1) {
					result += hours + " hours"; // INTLANG
				}
				if (minutes == 1) {
					result += " " + minutes + " minute";
				} else if (minutes > 1) {
					result += " " + minutes + " minutes";
				}
			}
		}
		return result;
	}

	private String getLabel(String keyformat, int units) {
		String keyformated = "";
		if (units == 1) {
			keyformated = String.format(keyformat, "singular");
		} else {
			keyformated = String.format(keyformat, "plural");
		}
		return FamText.message(keyformated);
	}

	public String getCapacityUnitName(int units) {
		return this.getLabel("label.capacity." + this.getKey() + ".%s", units);
	}

	/**
	 * the key for getting names and labels. if <code>null</code>, this will be
	 * the facility itself.
	 * 
	 * @return the key
	 */

	public String getKey() {
		if (key == null) {
			this.key = this.getFacility().getKey();
		}
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	@Required
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the facility
	 */

	public FacilityBookable getFacility() {
		return facility;
	}


	/**
	 * @param facility
	 *            the facility to set
	 */

	public void setFacility(FacilityBookable facility) {
		this.facility = facility;
	}

	/**
	 * @param bookingRule4Roles
	 *            the bookingRule4Roles to set
	 */

	public void setSpecificRights4UserOnFacility(List<SpecificRights4RoleOnFacility> bookingRule4Roles) {
		this.bookingRule4Roles = bookingRule4Roles;
	}



	public int getMaxBookableCapacityUnits(User user) {
		return this.getSetOfRulesForARole(user).getMaxBookableCapacityUnits();
	}

	public int getMaxBookableTimeUnits(User user) {
		return this.getSetOfRulesForARole(user).getMaxBookableTimeUnits();
	}

	public int getMinBookableCapacityUnits(User user) {
		return this.getSetOfRulesForARole(user).getMinBookableCapacityUnits();
	}

	public int getMinBookableTimeUnits(User user) {
		return this.getSetOfRulesForARole(user).getMinBookableTimeUnits();
	}

}
