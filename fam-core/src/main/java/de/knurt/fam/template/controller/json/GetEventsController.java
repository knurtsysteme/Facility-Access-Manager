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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;
import de.knurt.heinzelmann.util.time.TimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrameFactory;

/**
 * return events of the month for a given day that is part of the month and
 * bookable facility that must be booked time based.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.1 (04/07/2011)
 */
public class GetEventsController extends JSONController {

	public GetEventsController(User user) {
		this.user = user;
	}

	private User user = null;

	/**
	 * return an available possible booking.
	 * 
	 * there are two different places where this is called: 1. the
	 * "request booking" link in the navigation of the calendar 2. the drag and
	 * drop action in same calender
	 * 
	 * the response depends on which place causes the request.
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return an available possible booking.
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		boolean succ = true;

		// get data
		JSONObject data = null;
		try {
			data = new JSONObjectFromRequest().process(rq);
			if (data == null) {
				succ = false;
				FamLog.info("data not found", 201104071047l);
			}
		} catch (Exception e) {
			succ = false;
			FamLog.info("data not found", 201104071047l);
		}

		// get facility from request
		FacilityBookable facility = null;
		if (succ) {
			try {
				String facilityKey = data.getString("facility");
				if (facilityKey != null && FacilityConfigDao.isKey(facilityKey)) {
					facility = (FacilityBookable) FacilityConfigDao.getInstance().getConfiguredInstance(facilityKey);
					if (facility.isUnknown()) {
						succ = false;
						FamLog.info("requested unknown facility", 201109260828l);
					}
				} else {
					succ = false;
				}
			} catch (JSONException e) {
				succ = false;
				FamLog.info("facility not found", 201104071011l);
			} catch (ClassCastException e) {
				succ = false;
				FamLog.info("requested unbookable facility", 201109230917l);
			}
		}

		// get capacity_units from request
		int capacityUnits = -1;
		if (succ) {
			try {
				capacityUnits = Integer.parseInt(data.getString("capacity_units"));
			} catch (JSONException e) {
				succ = false;
				FamLog.info("capacity units not found", 201104130939l);
			}
		}

		// capacity units in bounce? check!
		BookingRule br = null;
		if (succ) {
			br = FacilityConfigDao.bookingRule(facility.getKey());
			if (br == null || capacityUnits < br.getMinBookableCapacityUnits(user) || capacityUnits > br.getMaxBookableCapacityUnits(user)) {
				succ = false;
			}
		}

		// refuse facilities that are not booked time based
		if (succ && br.getBookingStrategy() != BookingStrategy.TIME_BASED) {
			succ = false;
		}

		// get calendar
		Calendar dayInMonth = Calendar.getInstance();
		// ↘ as a convention we are starting on mondays!!!
		dayInMonth.setFirstDayOfWeek(2);
		if (succ && facility != null) {
			// set year
			try {
				int year = data.getInt("year");
				dayInMonth.set(Calendar.YEAR, year);
			} catch (JSONException e) {
				succ = false;
				FamLog.info("key year not found", 201104071025l);
			}
			// set month
			try {
				int month = data.getInt("month");
				dayInMonth.set(Calendar.MONTH, month);
			} catch (JSONException e) {
				succ = false;
				FamLog.info("key month not found", 201104071026l);
			}
			// set day_of_month
			try {
				int day_of_month = data.getInt("day_of_month");
				dayInMonth.set(Calendar.DAY_OF_MONTH, day_of_month);
			} catch (JSONException e) {
				succ = false;
				FamLog.info("key month not found", 201104071027l);
			}
		}

		// set success result
		try {
			result.put("succ", succ);
			if (succ) {
				result.put("events", this.getEventsOfMonthWithFullWeeks(dayInMonth, facility, br, capacityUnits));
			} else {
				result.put("error", "invalid data format"); // INTLANG
			}
		} catch (JSONException e) {
			FamLog.exception(e, 201104071014l);
		}
		return result;
	}

	/**
	 * return events of a month.
	 * 
	 * @param dayInMonth
	 *            that is part of that month
	 * @param facility
	 *            events are for
	 * @param br
	 *            rules user try to book with
	 * @param capacityUnits
	 *            requested
	 * @return events of a month
	 */
	private JSONArray getEventsOfMonthWithFullWeeks(Calendar dayInMonth, FacilityBookable facility, BookingRule br, int capacityUnits) {
		JSONArray events = new JSONArray();
		TimeFrame requestedMonth = new TimeFrameFactory(dayInMonth).getMonthWithFullWeeks();
		List<FacilityAvailability> generalAvailabilities = FamDaoProxy.facilityDao().getFacilityAvailabilitiesMergedByFacilities(requestedMonth, facility.getKey());
		List<Booking> bookings = FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(facility, requestedMonth);
		try {

			// create pointer for loop
			Calendar monthPointer = (Calendar) requestedMonth.getCalendarStart().clone();

			// steps to increment in minutes
			int oneHour = 60; // for the calendar take 60 minutes for every
			// facility!

			// loop while it is the same month (with full weeks) as given
			while (requestedMonth.contains(monthPointer.getTime())) {
				// start in "minutes of day"
				int start = monthPointer.get(Calendar.MINUTE) + monthPointer.get(Calendar.HOUR_OF_DAY) * 60;
				JSONObject event = new JSONObject();
				event.put("start", start);
				event.put("end", start + oneHour);
				TimeFrame tf = new TimeFrameFactory(monthPointer).getDuration(Calendar.MINUTE, oneHour);
				GetEventsControllerEvent pEvent = this.getGetEventsControllerEvent(tf, facility, br, capacityUnits, generalAvailabilities, bookings);
				event.put("event", pEvent.getNumber());
				event.put("label", pEvent.getLabel());
				event.put("timeFrameLabel", FamDateFormat.getCustomTimeFrame(pEvent.getTimeFrame(), "E MM/dd/yyyy HH:mm", "HH:mm", " – "));
				event.put("month", monthPointer.get(Calendar.MONTH));
				event.put("year", monthPointer.get(Calendar.YEAR));
				event.put("day_of_month", monthPointer.get(Calendar.DAY_OF_MONTH));

				// increment month and destroy summer and winter time
				int hourBefore = monthPointer.get(Calendar.HOUR_OF_DAY);
				monthPointer.add(Calendar.MINUTE, oneHour);
				if (hourBefore == monthPointer.get(Calendar.HOUR_OF_DAY)) {
					// ↖ clock moved back to winter time
					// ↘ skip doubled hour
					monthPointer.add(Calendar.MINUTE, oneHour);
				} else if (hourBefore == monthPointer.get(Calendar.HOUR_OF_DAY) - 2) {
					// ↖ clock moved forward to summer time
					// ↘ enlarge last hour
					event.put("end", start + oneHour + oneHour);
				}

				// now put event
				events.put(event);
			}

		} catch (JSONException e) {
			FamLog.exception(e, 201104070854l);
		}
		return events;
	}

	protected GetEventsControllerEvent getGetEventsControllerEvent(TimeFrame timeFrame, FacilityBookable facility, BookingRule br, int capacityUnitsRequested, List<FacilityAvailability> generalAvailabilities, List<Booking> bookings) {
		GetEventsControllerEvent result = new GetEventsControllerEvent();
		boolean somethingFound = false;

		// check generaly availability
		for (FacilityAvailability da : generalAvailabilities) {
			if (da.overlaps(timeFrame) && !da.isCompletelyAvailable()) {
				String label = "";
				if (da.isNotAvailableBecauseOfSuddenFailure()) {
					label = "Not Available because of a sudden failure!"; // INTLANG
				} else if (da.isNotAvailableBecauseOfMaintenance()) {
					label = "Not Available because of a maintenance!"; // INTLANG
				} else if (da.mustNotStartHere()) {
					label = "Your booking must not start here."; // INTLANG
				} else { // opening hours
					label = "Not Available in general!"; // INTLANG
				}
				String notice = da.getNotice();
				if (notice != null && !notice.isEmpty()) {
					label += " Notice: " + notice;
				}
				result.setNumber(da.getAvailable());
				result.setLabel(label);
				somethingFound = true;
				break;
			}
		}

		// time frame is past or user has to wait for booking
		if (!somethingFound) {
			Calendar earlistPossibilityToBookFromNow = FamAuth.getEarliestCalendarToBookFromNow(this.user, facility);
			if (timeFrame.getCalendarStart().before(earlistPossibilityToBookFromNow)) {
				String eptbfn = FamDateFormat.getDateFormattedWithTime(earlistPossibilityToBookFromNow);
				result.setLabel(String.format("Please request after: %s", eptbfn)); // INTLANG
				result.setNumber(FacilityAvailability.GENERAL_NOT_AVAILABLE);
				somethingFound = true;
			}
		}

		// if still available, check booking situation
		if (!somethingFound && bookings != null) {
			// ↖ bookable facility and no maintenance count statuses
			int capacityUnitsBooked = 0;
			List<Booking> overlappingBookings = new ArrayList<Booking>();
			for (Booking b : bookings) {
				if (b.overlaps(timeFrame)) {
					capacityUnitsBooked += b.getCapacityUnits();
					overlappingBookings.add(b);
				}
			}
			if (capacityUnitsBooked > 0) {
				String label = "";
				if (((FacilityBookable) facility).getCapacityUnits() >= capacityUnitsBooked + capacityUnitsRequested) {
					label = String.format("Still %s units available! ", ((FacilityBookable) facility).getCapacityUnits() - capacityUnitsBooked); // INTLANG
					result.setNumber(FacilityAvailability.MAYBE_AVAILABLE);
				} else {
					label = "Booked Out! "; // INTLANG
					result.setNumber(FacilityAvailability.BOOKED_NOT_AVAILABLE);
				}
				if (overlappingBookings.size() == 1) {
					String userinfo = "";
					if (this.user.hasRight(FamAuth.VIEW_PERSONAL_INFORMATION, facility)) {
						User tmpu = overlappingBookings.get(0).getUser();
						userinfo = String.format("%s (%s , %s)", tmpu.getFullName(), tmpu.getMail(), tmpu.getPhone());
					} else {
						userinfo = overlappingBookings.get(0).getUsername();
					}
					label += String.format("Booked by %s.", userinfo); // INTLANG
					String notice = overlappingBookings.get(0).getNotice();
					if (notice != null && !notice.isEmpty()) {
						label += " Notice: " + notice; // INTLANG
					}
				} else {
					label += String.format("There are %s bookings.", overlappingBookings.size()); // INTLANG
				}
				result.setLabel(label); // INTLANG
				somethingFound = true;
			}
		}
		if (!somethingFound) {
			result.setLabel("Available!"); // INTLANG
			result.setNumber(FacilityAvailability.COMPLETE_AVAILABLE);
		}

		// add the time the label is for
		result.setTimeFrame(timeFrame);

		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201104070855l);
	}
}