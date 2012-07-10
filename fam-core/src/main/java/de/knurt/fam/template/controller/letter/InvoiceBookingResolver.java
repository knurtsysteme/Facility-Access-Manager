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
package de.knurt.fam.template.controller.letter;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.model.TemplateResource;

/**
 * resolve setting a booking as invoiced depending on the request. if the
 * parameter <code>invoiced</code> is given and not empty AND there is a booking
 * in the request.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 */
public class InvoiceBookingResolver {

	private Booking booking = null;

	public InvoiceBookingResolver(TemplateResource tr) {
		if (tr.getRequest().getParameter("invoiced") != null && tr.getRequest().getParameter("invoiced").isEmpty() == false) {
			this.booking = RequestInterpreter.getBooking(tr.getRequest());
		}
	}

	public boolean invoice() {
		if (this.booking == null) {
			return false;
		} else {
			this.booking.invoice();
			return true;
		}
	}

}
