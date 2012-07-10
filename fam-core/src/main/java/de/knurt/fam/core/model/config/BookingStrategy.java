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

/**
 * by now just some flags to know, how to book a facility.
 * 
 * @see BookingRule#getBookingStrategy
 * @author Daniel Oltmanns
 * @since 09/25/2009
 */
public class BookingStrategy {

	/**
	 * the facility can be booked time based. this is mainly booking over a
	 * calendar.
	 */
	public final static int TIME_BASED = 1;

	/**
	 * the facility can be booked queue based. this is to queue for a facility.
	 */
	public final static int QUEUE_BASED = 2;

}
