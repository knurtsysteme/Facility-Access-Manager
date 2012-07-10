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
 */package de.knurt.fam.core.util.booking;

import org.springframework.beans.factory.annotation.Required;

/**
 * return the {@link BookingIsAvailableDeciderFactory} as configured.
 * 
 * @author Daniel Oltmanns
 * @since 1.4.3 (07/22/2011)
 */
public class BookingIsAvailableDeciderProxy {

	private BookingIsAvailableDeciderFactory deciderFactory = null;

	public BookingIsAvailableDeciderFactory getDeciderFactory() {
		return deciderFactory;

	}

	@Required
	public void setDeciderFactory(BookingIsAvailableDeciderFactory deciderFactory) {
		this.deciderFactory = deciderFactory;
	}

	public BookingIsAvailableDeciderFactory getBookingIsAvailableDeciderFactory() {
		return this.deciderFactory;
	}

	/** one and only instance of BookingIsAvailableDeciderProxy */
	private volatile static BookingIsAvailableDeciderProxy me;

	/** construct BookingIsAvailableDeciderProxy */
	private BookingIsAvailableDeciderProxy() {
	}

	/**
	 * return the one and only instance of BookingIsAvailableDeciderProxy
	 * 
	 * @return the one and only instance of BookingIsAvailableDeciderProxy
	 */
	public static BookingIsAvailableDeciderProxy getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (BookingIsAvailableDeciderProxy.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new BookingIsAvailableDeciderProxy();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of BookingIsAvailableDeciderProxy
	 */
	public static BookingIsAvailableDeciderProxy me() {
		return getInstance();
	}

}
