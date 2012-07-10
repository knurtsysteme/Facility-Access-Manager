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
package de.knurt.fam.core.config;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Required;

/**
 * configuration of a calendar in general. here you can configure, what is the
 * default time starting a calendar, so the day is not shown from 0 a.m. to 12
 * p.m. and the default steps, a time can entered.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090516 (05/16/2009)
 */
public class FamCalendarConfiguration {

	/**
	 * return default minutes of one step. these are the global minutes used for
	 * generating pictures or time steps or whatever concerning one step of a
	 * time slot.
	 * 
	 * @return default minutes of one step.
	 */
	public static int smallestMinuteStep() {
		return getInstance().getSmallestMinuteStep();
	}

	private int hourStart, hourStop, smallestMinuteStep;
	/** one and only instance of me */
	private volatile static FamCalendarConfiguration me;

	/**
	 * return true, if hour of given calendar is in hour start and stop.
	 * ignoring the date of the given calendar.
	 * 
	 * @param cal
	 *            calendar to check
	 * @return true, if hour of given calendar is in hour start and stop.
	 */
	public boolean isIn(Calendar cal) {
		return cal.get(Calendar.HOUR_OF_DAY) > this.getHourStop() && cal.get(Calendar.HOUR_OF_DAY) <= this.getHourStart();
	}

	/** construct me */
	private FamCalendarConfiguration() {
	}

	/**
	 * return the one and only instance of FamCalendarConfiguration
	 * 
	 * @return the one and only instance of FamCalendarConfiguration
	 */
	public static FamCalendarConfiguration getInstance() {
		if (me == null) { // no instance so far
			synchronized (FamCalendarConfiguration.class) {
				if (me == null) { // still no instance so far
					me = new FamCalendarConfiguration(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * @return the hourStart
	 */
	private int getHourStart() {
		return hourStart;
	}

	/**
	 * set the global hour, when all visualizations of calendars of the system
	 * shall be start. this may be overridden in other areas but shall be used,
	 * if nothing else is set.
	 * 
	 * @param hourStart
	 *            the hour where all visualizations of calendars in the system
	 *            start.
	 */
	@Required
	public void setHourStart(int hourStart) {
		this.hourStart = hourStart;
	}

	/**
	 * @return the hourStop
	 */
	private int getHourStop() {
		return hourStop;
	}

	/**
	 * set the global hour, when all visualizations of calendars of the system
	 * shall be end. this may be overridden in other areas but shall be used, if
	 * nothing else is set.
	 * 
	 * @param hourStop
	 *            the hour where all visualizations of calendars in the system
	 *            end.
	 */
	@Required
	public void setHourStop(int hourStop) {
		this.hourStop = hourStop;
	}

	/**
	 * return the global hour, when all visualizations of calendars of the
	 * system shall be start. this may be overridden in other areas but shall be
	 * used, if nothing else is set.
	 * 
	 * @return the global hour, visualizations of calenders shall start
	 */
	public static int hourStart() {
		return getInstance().getHourStart();
	}

	/**
	 * return the global hour, when all visualizations of calendars of the
	 * system shall be end. this may be overridden in other areas but shall be
	 * used, if nothing else is set.
	 * 
	 * @return the global hour, visualizations of calenders shall stop
	 */
	public static int hourStop() {
		return getInstance().getHourStop();
	}

	/**
	 * @return the smallestMinuteStep
	 */
	private int getSmallestMinuteStep() {
		return smallestMinuteStep;
	}

	/**
	 * @param smallestMinuteStep
	 *            the smallestMinuteStep to set
	 */
	@Required
	public void setSmallestMinuteStep(int smallestMinuteStep) {
		this.smallestMinuteStep = smallestMinuteStep;
	}
}
