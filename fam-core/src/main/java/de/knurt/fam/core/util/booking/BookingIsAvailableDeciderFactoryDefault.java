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

import de.knurt.fam.core.model.config.FacilityBookable;

/**
 * produce {@link BookingIsAvailableDecider} for the default behavior of the
 * fam.
 * 
 * @author Daniel Oltmanns
 * @since 1.4.3 (07/22/2011)
 */
public class BookingIsAvailableDeciderFactoryDefault implements BookingIsAvailableDeciderFactory {

	/** {@inheritDoc} */
	@Override
	public BookingIsAvailableDecider get(FacilityBookable facilityBookingIsFor) {
		return new BookingIsAvailableDeciderDefault();
	}

	/** one and only instance of BookingIsAvailableDeciderFactoryDefault */
	private volatile static BookingIsAvailableDeciderFactoryDefault me;

	/** construct BookingIsAvailableDeciderFactoryDefault */
	private BookingIsAvailableDeciderFactoryDefault() {
	}

	/**
	 * return the one and only instance of
	 * BookingIsAvailableDeciderFactoryDefault
	 * 
	 * @return the one and only instance of
	 *         BookingIsAvailableDeciderFactoryDefault
	 */
	public static BookingIsAvailableDeciderFactoryDefault getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (BookingIsAvailableDeciderFactoryDefault.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new BookingIsAvailableDeciderFactoryDefault();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of
	 *         BookingIsAvailableDeciderFactoryDefault
	 */
	public static BookingIsAvailableDeciderFactoryDefault me() {
		return getInstance();
	}

}
