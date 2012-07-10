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

/**
 * rules for booking a facility forced to user with a specific role. a simple
 * bean with getters and setters that are required.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (09/01/2010)
 */
public class SimpleSetOfRulesForARoleBean implements SetOfRulesForARole {

	private int minBookableTimeUnits, maxBookableTimeUnits, minBookableCapacityUnits, maxBookableCapacityUnits;
	private int reminderMailMinutesBeforeStarting = -1; // no mail

	public int getMinBookableTimeUnits() {
		return minBookableTimeUnits;
	}

	@Required
	public void setMinBookableTimeUnits(int minBookableTimeUnits) {
		this.minBookableTimeUnits = minBookableTimeUnits;
	}

	public int getMaxBookableTimeUnits() {
		return maxBookableTimeUnits;
	}

	@Required
	public void setMaxBookableTimeUnits(int maxBookableTimeUnits) {
		this.maxBookableTimeUnits = maxBookableTimeUnits;
	}

	public int getMinBookableCapacityUnits() {
		return minBookableCapacityUnits;
	}

	@Required
	public void setMinBookableCapacityUnits(int minBookableCapacityUnits) {
		this.minBookableCapacityUnits = minBookableCapacityUnits;
	}

	public int getMaxBookableCapacityUnits() {
		return maxBookableCapacityUnits;
	}

	@Required
	public void setMaxBookableCapacityUnits(int maxBookableCapacityUnits) {
		this.maxBookableCapacityUnits = maxBookableCapacityUnits;
	}

	public int getReminderMailMinutesBeforeStarting() {
		return reminderMailMinutesBeforeStarting;
	}

	@Required
	public void setReminderMailMinutesBeforeStarting(int reminderMailMinutesBeforeStarting) {
		this.reminderMailMinutesBeforeStarting = reminderMailMinutesBeforeStarting;
	}

}
