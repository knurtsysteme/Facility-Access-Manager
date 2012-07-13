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
package de.knurt.fam.core.view.adapter.html;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;

/**
 * factory to produce adapter for a booking
 * 
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@Deprecated
public class HtmlAdapterBookingFactory extends DasHtmlAdapterAbstractFactory<Booking> {

	/**
	 * construct the booking adapter factory. this is an empty constructor.
	 */
	public HtmlAdapterBookingFactory() {
	}

	/**
	 * return a html adapter for the booking.
	 * 
	 * @param current
	 *            user being authenticated
	 * @param mappedObject
	 *            booking being adapted
	 * @return a html adapter for the booking.
	 */
	@Override
	public FamHtmlAdapter<Booking> getInstance(User current, Booking mappedObject) {
		// XXX extract more interfaces!
		if (mappedObject.getClass().equals(TimeBooking.class)) {
			return new HtmlAdapterTimeBooking(current, (TimeBooking) mappedObject);
		} else if (mappedObject.getClass().equals(QueueBooking.class)) {
			return new HtmlAdapterQueueBooking(current, (QueueBooking) mappedObject);
		} else {
			try {
				throw new Exception("unsupported booking class");
			} catch (Exception ex) {
				FamLog.exception(ex, 201204141016l);
				return null;
			}
		}
	}
}
