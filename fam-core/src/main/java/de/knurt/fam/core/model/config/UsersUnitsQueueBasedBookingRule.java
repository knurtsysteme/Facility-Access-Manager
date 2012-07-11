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

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;

/**
 * define a booking rule, where user's units queue for a facility.
 * 
 * @author Daniel Oltmanns
 * @since 24.09.2009
 */
public class UsersUnitsQueueBasedBookingRule extends AbstractBookingRule implements QueueBasedBookingRule {

	private Integer actualQueueLength = null;
	private Integer assertUnitsPerHourProcessed = null;

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public int getSmallestMinutesBookable() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 */

	public void setSmallestMinutesBookable(int smallestMinutesBookable) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public String getSmallestTimeLabelEqualsOneXKey() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @param smallestTimeLabelEqualsOneXKey
	 *            the smallest time label to set
	 */

	public void setSmallestTimeLabelEqualsOneXKey(String smallestTimeLabelEqualsOneXKey) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public Integer getMustStartAt() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 */

	public void setMustStartAt(Integer mustStartAt) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public Integer getUnitsPerHourProcessed() {
		return this.assertUnitsPerHourProcessed;
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public int getMaxPossibleUnitsProcessedAtOnce() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * return the maximal length of a queue or something smaller equal 0 for
	 * infinite. by now, this return 0 for infinite.
	 * 
	 * @return 0
	 */
	public int getMaxQueueLength() {
		return 0; // TODO #14 a queue can have a maximum length: di and unit tests
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public int getActualQueueLength() {
		this.initQueue();
		return this.actualQueueLength;
	}

	/**
	 * return true, if the queue has been init.
	 * 
	 * @return
	 */
	private boolean initQueue() {
		boolean result = false;
		if (this.actualQueueLength == null) {
			this.actualQueueLength = FamDaoProxy.bookingDao().getActualQueueLength(this.getFacility());
			result = true;
		}
		return result;
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public Integer reminderMailAtPosition() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * throw UnsupportedOperationException until it's supported
	 * 
	 * @return -
	 */

	public Integer cancelBookingAtPosition() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public int getBookingStrategy() {
		return BookingStrategy.QUEUE_BASED;
	}

	public void incrementQueue() {
		if (!this.initQueue()) { // queue has been init and has actual value
			// already
			this.actualQueueLength++;
		}
	}

	public void reduceQueue() {
		if (!this.initQueue()) { // queue has been init and has actual value
			// already
			this.actualQueueLength--;
		}
	}

	/**
	 * @return the assertUnitsPerHourProcessed
	 */

	public Integer getAssertUnitsPerHourProcessed() {
		return assertUnitsPerHourProcessed;
	}

	/**
	 * @param assertUnitsPerHourProcessed
	 *            the assertUnitsPerHourProcessed to set
	 */
	@Required
	public void setAssertUnitsPerHourProcessed(Integer assertUnitsPerHourProcessed) {
		this.assertUnitsPerHourProcessed = assertUnitsPerHourProcessed;
	}

	public boolean isSessionStartable() {
		return true;
	}
}
