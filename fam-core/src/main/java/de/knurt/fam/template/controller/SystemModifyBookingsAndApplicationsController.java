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
package de.knurt.fam.template.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * controller for view and cancel bookings.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090810 (08/10/2009)
 */
class SystemModifyBookingsAndApplicationsController {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = null;
		User user = SessionAuth.user(request);
		if (user != null) {
			try {
				Integer id2cancel = Integer.parseInt(request.getParameter(QueryKeys.QUERY_KEY_BOOKING));

				// get the booking
				TimeBooking example = TimeBooking.getEmptyExampleBooking();
				example.setId(id2cancel);
				Booking shallBeChanged = FamDaoProxy.bookingDao().getOneLike(example);

				// get the reason
				String textNotice = RequestInterpreter.getNotice(request);

				// cancel it
				if (shallBeChanged != null) {
					if (shallBeChanged.isApplication() && this.getTrueFalse(request) == true) {
						shallBeChanged.confirmApplication(textNotice);
					} else { // is cancelation of booking
						if (textNotice.trim().isEmpty()) {
							textNotice = Cancelation.REASON_NO_REASON;
						}
						shallBeChanged.cancel(new Cancelation(user, textNotice));
					}
				}
			} catch (Exception e) {
			}
		}
		return mav;
	}

	public Boolean getTrueFalse(HttpServletRequest request) {
		Boolean result = null;
		String y = request.getParameter(QueryKeys.QUERY_KEY_TRUE_FALSE);
		if (y != null) {
			if (y.equals(QueryKeys.TRUE)) {
				result = true;
			} else {
				result = false;
			}
		}
		return result;
	}

}
