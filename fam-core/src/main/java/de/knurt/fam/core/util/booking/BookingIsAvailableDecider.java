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
package de.knurt.fam.core.util.booking;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * decide, if a {@link Booking} is available or not.
 * 
 * @author Daniel Oltmanns
 * @since 1.4.3 (07/22/2011)
 */
public interface BookingIsAvailableDecider {

	public boolean isAvailableForInsertion(TimeBooking bookingRequest);

	public boolean isAvailableForInsertion(QueueBooking bookingRequest);

	public boolean isAvailableForInsertion(TimeBooking bookingRequest, TimeFrame supposedTimeFrame);
}
