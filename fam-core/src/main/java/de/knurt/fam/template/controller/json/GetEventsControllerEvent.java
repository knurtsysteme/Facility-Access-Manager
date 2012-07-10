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
package de.knurt.fam.template.controller.json;

import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * for intern use with {@link GetEventsController}.
 * 
 * @author Daniel Oltmanns
 * @since 1.6.0 (10/12/2011)
 */
public class GetEventsControllerEvent {
	private String label;
	private TimeFrame timeFrame;
	/**
	 * 1: available; 2: party available; 3: booked up; 4: time beyond working
	 * hours or yesterdays; 5: maintenance; 6: sudden failures; 7: impossible to
	 * start a session here
	 */
	private int number;

	public void setLabel(String label) {
		this.label = label;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getLabel() {
		return label;
	}

	public int getNumber() {
		return number;
	}

	public void setTimeFrame(TimeFrame timeFrame) {
		this.timeFrame = timeFrame;
	}

	public TimeFrame getTimeFrame() {
		return timeFrame;
	}
}