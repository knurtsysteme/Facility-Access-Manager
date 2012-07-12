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
package de.knurt.fam.template.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * controller for see in rework the input of a session for operator side.
 * 
 * class named <code>WorkOnJobsModelFactory</code> before
 * 
 * @see SimpleSession
 * @author Daniel Oltmanns
 * @since 1.3.0 (11/21/2010)
 */
public class JobsManagerModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		List<FacilityBookable> usersFacilities;
		if(templateResource.getAuthUser().isAdmin()) {
			usersFacilities = FacilityConfigDao.getInstance().getBookableFacilities();
		} else {
			usersFacilities = FacilityConfigDao.bookablefacilities(templateResource.getAuthUser().getFacilityKeysUserIsResponsibleFor());
		}
		FacilityBookable facility = this.getFacilityBookable(templateResource);
		if (facility == null) {
			// no facility given in request. put out first facility of
			// responsibility
			if (usersFacilities != null && usersFacilities.size() > 0) {
				for (Facility facilityCandidate : usersFacilities) {
					if (facilityCandidate.isBookable()) {
						facility = (FacilityBookable) facilityCandidate;
						break;
					}
				}
			}
		}
		if (facility == null) {
			// bad request
			RedirectResolver.redirectClient(RedirectTarget.PUBLIC_HOME, templateResource);
		} else {
			// good request
			result = this.getGeneralProperties(facility);
			Booking booking = RequestInterpreter.getBooking(templateResource.getRequest());
			if (booking != null && booking.isCanceled()) {
				// ↖ in javascript, you can cancel a booking and process it
				// ↖ then. just reload the page
				RedirectResolver.redirectClient(RedirectTarget.ADMIN_JOBS_MANAGER, templateResource);
			} else {
				// set queue based flag
				boolean queueBased = facility.getBookingRule().getBookingStrategy() == BookingStrategy.QUEUE_BASED;
				result.put("is_queue_based", queueBased);
				// set jobs
				result.put("bookings", queueBased ? this.getQueueBookingsCurrentJobs(templateResource, facility) : this.getTimeBookingsCurrentJobs(templateResource, facility));
				// set facilitly
				result.put("facility", facility);
				// set if past is requested
				result.put("isRequest4Past", this.isRequest4Past(templateResource));
				// set if past is requested
				result.put("hasResponsibility", templateResource.getAuthUser().hasResponsibility4Facility(facility));
			}
		}
		result.put("users_facilities", usersFacilities);
		return result;
	}

	private List<TimeBooking> getTimeBookingsCurrentJobs(TemplateResource templateResource, FacilityBookable bd) {
		TimeBooking example = TimeBooking.getEmptyExampleBooking();
		example.setFacilityKey(bd.getKey());
		List<TimeBooking> timeBookings = new ArrayList<TimeBooking>();
		boolean isRequest4Past = this.isRequest4Past(templateResource);
		List<Booking> candidates = FamDaoProxy.bookingDao().getObjectsLike(example);
		for (Booking candidate : candidates) {
			if (((!isRequest4Past && !candidate.isProcessed()) || (isRequest4Past && candidate.isProcessed())) && !candidate.isCanceled() && !candidate.isApplication()) {
				timeBookings.add((TimeBooking) candidate);
			}
		}

		Collections.sort(timeBookings);
		if (isRequest4Past) {
			Collections.reverse(timeBookings);
		}
		return timeBookings;
	}

	private List<QueueBooking> getQueueBookingsCurrentJobs(TemplateResource templateResource, FacilityBookable facility) {
		List<QueueBooking> queueBookings = null;
		boolean isRequest4Past = this.isRequest4Past(templateResource);
		if (isRequest4Past) {
			QueueBooking example = QueueBooking.getBooking4Query();
			example.setFacilityKey(facility.getKey());
			queueBookings = new ArrayList<QueueBooking>();
			List<Booking> candidates = FamDaoProxy.bookingDao().getObjectsLike(example);
			for (Booking candidate : candidates) {
				if (candidate.isProcessed() && !candidate.isCanceled()) {
					queueBookings.add((QueueBooking) candidate);
				}
			}
		} else {
			queueBookings = FamDaoProxy.bookingDao().getCurrentQueue(facility);
		}
		Collections.sort(queueBookings);
		if (isRequest4Past) {
			Collections.reverse(queueBookings);
		}
		return queueBookings;
	}

	private boolean isRequest4Past(TemplateResource templateResource) {
		String past = templateResource.getRequest().getParameter("past");
		return past != null && past.equals("1");
	}

	private int getUnitsInUseNow(FacilityBookable bd) {
		TimeFrame test = new SimpleTimeFrame();
		test.addEnd(Calendar.MINUTE, 1);
		return FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, test).size();
	}

	private Properties getGeneralProperties(FacilityBookable facility) {
		Properties result = new Properties();
		if (facility != null) {
			QueryString qs = QueryStringBuilder.getQueryString(facility);
			qs.put("past", "1");
			result.put("alreadyProcessedQueryString", qs.getAsHtmlLinkHref(true));
			int unitsInUseNow = this.getUnitsInUseNow(facility);
			if (unitsInUseNow >= 0) {
				result.put("showUnitsUsedNow", "t");
				result.put("unitsUsedNow", unitsInUseNow);
			} else {
				result.put("showUnitsUsedNow", "f");
				result.put("unitsUsedNow", "");
			}
		}
		return result;
	}

	private FacilityBookable getFacilityBookable(TemplateResource templateResource) {
		return RequestInterpreter.getBookableFacility(templateResource.getRequest());
	}
}
