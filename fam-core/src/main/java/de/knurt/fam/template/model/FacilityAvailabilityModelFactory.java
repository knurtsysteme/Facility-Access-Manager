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

import static de.knurt.fam.core.model.persist.FacilityAvailability.BOOKED_NOT_AVAILABLE;
import static de.knurt.fam.core.model.persist.FacilityAvailability.BOOKING_MUST_NOT_START_HERE;
import static de.knurt.fam.core.model.persist.FacilityAvailability.COMPLETE_AVAILABLE;
import static de.knurt.fam.core.model.persist.FacilityAvailability.GENERAL_NOT_AVAILABLE;
import static de.knurt.fam.core.model.persist.FacilityAvailability.MAINTENANCE_NOT_AVAILABLE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.content.html.calendar.AvailabilityOverviewHtml;
import de.knurt.fam.core.content.html.calendar.FamMonthAvailabilityHtml;
import de.knurt.fam.core.content.html.calendar.FamMonthHtml;
import de.knurt.fam.core.content.html.calendar.FamWeekAvailabilityHtml;
import de.knurt.fam.core.content.html.calendar.FamWeekHtml;
import de.knurt.fam.core.content.html.calendar.factory.AvailabilityInputHtmlFactory;
import de.knurt.fam.core.content.html.calendar.factory.FamCalendarHtmlFactory;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.util.time.CalendarViewResolver;
import de.knurt.fam.core.util.time.FamCalendar;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.adapter.ComparableCollectionsAdapter;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * control the pages, where you can set the general availability in a calendar.
 * delegate between calendar views and overviews and handle the request and its
 * containing date in the right context.
 * 
 * model factory for resource named <code>systemfacilityavailability</code>
 * 
 * @author Daniel Oltmanns
 * @since 0.20090421 (04/21/2009)
 */
@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
public class FacilityAvailabilityModelFactory {

	private HtmlElement getTimeInput(HttpServletRequest request) {
		HtmlElement result = HtmlFactory.get("div");
		Integer interval = RequestInterpreter.getInterval(request);
		if (interval != null) {
			HtmlElement pStart = HtmlFactory.get("p");
			HtmlElement labeldiv = HtmlFactory.get("span", "Start time:"); // INTLANG
			pStart.add(labeldiv + HtmlFactory.get("br").toString());
			HtmlElement pEnd = HtmlFactory.get("p");
			labeldiv = HtmlFactory.get("span", "End time:"); // INTLANG
			pEnd.add(labeldiv + HtmlFactory.get("br").toString());
			Calendar today = Calendar.getInstance();
			if (interval.intValue() == FacilityAvailability.ONE_TIME) {
				pStart.add(FamCalendarHtmlFactory.getDateSelect(today, 365, QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_DAY, 0, true));
				pEnd.add(FamCalendarHtmlFactory.getDateSelect(today, 365, QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_DAY, 1, true));
			} else if (interval.intValue() == FacilityAvailability.EACH_YEAR) {
				Calendar firstDayOfYear = Calendar.getInstance();
				firstDayOfYear.set(Calendar.DAY_OF_YEAR, 1);
				pStart.add(FamCalendarHtmlFactory.getDateSelect(firstDayOfYear, 365, QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_DAY, today.get(Calendar.DAY_OF_YEAR), false));
				pEnd.add(FamCalendarHtmlFactory.getDateSelect(firstDayOfYear, 365, QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_DAY, today.get(Calendar.DAY_OF_YEAR) + 1, false));
			} else if (interval.intValue() == FacilityAvailability.EACH_MONTH) {
				int i = 1;
				Calendar firstDayOfMonth = Calendar.getInstance();
				firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
				List<String> optiontexts = new ArrayList<String>();
				while (i <= 28) {
					optiontexts.add(FamDateFormat.getNumeralFormat(i));
					i++;
				}
				pStart.add(FamCalendarHtmlFactory.getDateSelect(firstDayOfMonth, QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_DAY, today.get(Calendar.DAY_OF_MONTH) - 1, optiontexts));
				pEnd.add(FamCalendarHtmlFactory.getDateSelect(firstDayOfMonth, QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_DAY, today.get(Calendar.DAY_OF_MONTH), optiontexts));
			} else if (interval.intValue() == FacilityAvailability.EACH_WEEK) {
				Calendar weekday = FamCalendar.getInstance();
				weekday.set(Calendar.DAY_OF_WEEK, weekday.getFirstDayOfWeek());
				int pointer = 0;
				List<String> optiontexts = new ArrayList<String>();
				while (pointer < 7) {
					optiontexts.add(weekday.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, FamRequestContainer.locale()));
					weekday.add(Calendar.DAY_OF_YEAR, 1);
					pointer++;
				}
				weekday.add(Calendar.DAY_OF_YEAR, -7);
				pStart.add(FamCalendarHtmlFactory.getDateSelect(weekday, QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_DAY, today.get(Calendar.DAY_OF_WEEK) - 1, optiontexts));
				pEnd.add(FamCalendarHtmlFactory.getDateSelect(weekday, QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_DAY, today.get(Calendar.DAY_OF_WEEK), optiontexts));
			} else { // each day or each hour
				QueryString qs = QueryStringFactory.get(QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_DAY, QueryKeys.getEncodeStringOfDate(today));
				pStart.add(qs.getAsHtmlInputsTypeHidden());
				List<String> optiontexts = new ArrayList<String>(2);
				optiontexts.add("same day"); // INTLANG
				optiontexts.add("next day"); // INTLANG
				pEnd.add(FamCalendarHtmlFactory.getDateSelect(today, QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_DAY, 0, optiontexts));
			}
			pStart.add(FamCalendarHtmlFactory.getTimeSelectCompact(QueryKeys.QUERY_KEY_FROM + QueryKeys.QUERY_KEY_HOUR_OF_DAY, 0));
			pEnd.add(FamCalendarHtmlFactory.getTimeSelectCompact(QueryKeys.QUERY_KEY_TO + QueryKeys.QUERY_KEY_HOUR_OF_DAY, 0));
			result.add(pStart);
			result.add(pEnd);
		}
		return result;
	}

	private HtmlElement getSummary(HttpServletRequest request) {
		HtmlElement result = HtmlFactory.get("ul").addClassName("asList");
		HtmlElement warningSpan = HtmlFactory.get("span").addClassName("warning");

		// summary availability
		Integer availability = RequestInterpreter.getAvailability(request);
		// is valid availability
		if (availability != null && (availability == COMPLETE_AVAILABLE || availability == GENERAL_NOT_AVAILABLE || availability == BOOKED_NOT_AVAILABLE || availability == MAINTENANCE_NOT_AVAILABLE || availability == BOOKING_MUST_NOT_START_HERE)) {
			result.add(HtmlFactory.get("li").add(HtmlFactory.get("strong").add("Availability")).add(": ").add(FamText.facilityAvailability(availability))); // INTLANG;
		}

		// summary interval
		Integer interval = RequestInterpreter.getInterval(request);
		if (interval != null) {
			result.add(HtmlFactory.get("li").add(HtmlFactory.get("strong").add("Interval")).add(": ").add(FamText.message("calendar.iteration." + interval))); // INTLANG;
		}

		// summary notice
		String notice = RequestInterpreter.getNotice(request);
		if (notice != null) {
			result.add(HtmlFactory.get("li").add(HtmlFactory.get("strong").add("Notice")).add(": ").add(notice)); // INTLANG;
		}

		if (availability != null && availability.intValue() != FacilityAvailability.COMPLETE_AVAILABLE) {
			FacilityAvailability da = RequestInterpreter.getCompleteFacilityAvailabilityForInsertion(request, SessionAuth.user(request));
			if (da != null) {

				TimeFrame baseTimeFrame = da.getBasePeriodOfTime();
				if (baseTimeFrame != null) {
					String timeText = interval == FacilityAvailability.ONE_TIME ? "Coming into effect" : "First time coming into effect"; // INTLANG
					result.add(HtmlFactory.get("li").add(HtmlFactory.get("strong").add(timeText)).add(": ").add(baseTimeFrame));

					// summarize bookings being canceled
					List<Facility> facilities = new ArrayList<Facility>();
					facilities.add(da.getFacility());
					List<Booking> bookings = FamDaoProxy.bookingDao().getAll(facilities);
					int negativeAnswers = 0;
					for (Booking booking : bookings) {
						if (!booking.isCanceled() && !booking.sessionAlreadyBegun() && booking.getIdBookedInBookingStrategy() != BookingStrategy.QUEUE_BASED && da.applicableTo(booking.getSessionTimeFrame())) {
							negativeAnswers++;
						}
					}

					result.add(HtmlFactory.get("li").add(HtmlFactory.get("strong").add("Number of letters of refusal sent with this input")).add(": ").add(negativeAnswers == 0 ? "no letter" : warningSpan.setContent(negativeAnswers + " letter(s)").toString())); // INTLANG;

					// warning on nothing set or left
					long durationOfBaseTime = baseTimeFrame.getDuration();
					long durationOfAnHour = 1000l * 60 * 60;
					boolean willBlockResource = false;
					boolean nothingSet = durationOfBaseTime <= 0;
					if (!nothingSet && interval != FacilityAvailability.ONE_TIME && availability != FacilityAvailability.COMPLETE_AVAILABLE) {
						if ((interval == FacilityAvailability.EACH_YEAR && durationOfBaseTime >= durationOfAnHour * 24 * 365) || (interval == FacilityAvailability.EACH_MONTH && durationOfBaseTime >= durationOfAnHour * 24 * 365 / 12)
								|| (interval == FacilityAvailability.EACH_WEEK && durationOfBaseTime >= durationOfAnHour * 24 * 7) || (interval == FacilityAvailability.EACH_DAY && durationOfBaseTime >= durationOfAnHour * 24)
								|| (interval == FacilityAvailability.EACH_HOUR && durationOfBaseTime >= durationOfAnHour)) {
							willBlockResource = true;
						}
					}
					if (willBlockResource) {
						result.add(HtmlFactory.get("li").add(warningSpan.setContent("If setting this, the facility will never ever be available!"))); // INTLANG;
					}
					if (nothingSet) {
						result.add(HtmlFactory.get("li").add(warningSpan.setContent("The duration of your setting is 0!"))); // INTLANG;
					}
				}
			}
		}
		return result;
	}

	private FamWeekHtml getDasWeekHtml(Calendar cal, String facilityKey, HttpServletRequest request) {
		boolean showRedGreenOnly = RequestInterpreter.hasAjaxFlag(request);
		return new FamWeekAvailabilityHtml(cal, facilityKey, new AvailabilityInputHtmlFactory(facilityKey, cal), showRedGreenOnly, true);
	}

	private FamMonthHtml getDasMonthHtml(Calendar cal, String facilityKey, HttpServletRequest request) {
		boolean showRedGreenOnly = RequestInterpreter.hasAjaxFlag(request);
		return new FamMonthAvailabilityHtml(cal, new AvailabilityInputHtmlFactory(facilityKey, cal), "onemonthday", facilityKey, showRedGreenOnly, true);
	}

	private String getOverviewHtml(TemplateResource templateResource, Calendar cal, String facilityKey) {
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(facilityKey);
		if (this.suddenFailureIsActive(das)) {
			return "";
		} else {
			ComparableCollectionsAdapter.getInstance().setModusToAll(das, FacilityAvailability.COMPARE_BY_DATE_SET);
			Collections.sort(das);
			return new AvailabilityOverviewHtml(cal, facilityKey, das, templateResource.getAuthUser()).toString();
		}
	}

	private boolean suddenFailureIsActive(String facilityKey) {
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(facilityKey);
		return this.suddenFailureIsActive(das);
	}

	private boolean suddenFailureIsActive(List<FacilityAvailability> das) {
		boolean result = false;
		for (FacilityAvailability da : das) {
			if (da.isNotAvailableBecauseOfSuddenFailure() && !da.getBasePeriodOfTime().endsInPast()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * return the calendar view requested.
	 * 
	 * @param rq
	 *            request got
	 * @return the calendar view requested
	 */
	private String getCalendarView(HttpServletRequest rq) {
		String calView = rq.getParameter(QueryKeys.QUERY_KEY_CALENDAR_VIEW);
		if (calView == null || QueryStringBuilder.isValidCalendarView(calView) == false) {
			Facility d = RequestInterpreter.getFacility(rq);
			if (d == null) {
				calView = CalendarViewResolver.getInstance().getDefaultCalendarView(FacilityConfigDao.getUnknownBookableFacility());
			} else {
				calView = CalendarViewResolver.getInstance().getDefaultCalendarView(d);
			}
		}
		return calView;
	}

	private Calendar getCalendar(HttpServletRequest rq) {
		if (rq.getParameter(QueryKeys.QUERY_KEY_SHOW_DETAILS) != null) {
			return RequestInterpreter.getCalendarStart(rq.getParameter(QueryKeys.QUERY_KEY_SHOW_DETAILS));
		} else {
			return RequestInterpreter.getCalendar(rq);
		}
	}

	/**
	 * return the calendar matching the given request and facility.
	 * 
	 * @param rq
	 *            request got
	 * @param facilityKey
	 *            representing a facility
	 * @return the calendar matching the given request and facility.
	 */
	private String getCalendarHtml(TemplateResource templateResource, String facilityKey) {
		Calendar cal = this.getCalendar(templateResource.getRequest());
		String result = "";
		String calView = this.getCalendarView(templateResource.getRequest());
		if (calView.equals(QueryKeys.WEEK)) {
			FamWeekHtml calhtml = this.getDasWeekHtml(cal, facilityKey, templateResource.getRequest());
			result = calhtml.toString();
		} else if (calView.equals(QueryKeys.MONTH)) { // month view
			FamMonthHtml calhtml = this.getDasMonthHtml(cal, facilityKey, templateResource.getRequest());
			result = calhtml.toString();
		} else { // over view
			result = this.getOverviewHtml(templateResource, cal, facilityKey);
		}
		return result;
	}

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		String facilityKey = templateResource.getRequest().getParameter(QueryKeys.QUERY_KEY_FACILITY);
		boolean is_overview = this.getCalendarView(templateResource.getRequest()).equals(QueryKeys.OVERVIEW);
		Facility facility = RequestInterpreter.getFacility(templateResource.getRequest());
		if (facility == null) {
			// ↖ wrong url!
			RedirectResolver.redirectClient(RedirectTarget.SYSTEM_FACILITY_AVAILABILITY_OVERVIEW, templateResource);
		} else {
			result.put("queue_based", facility.isBookable() && FacilityConfigDao.bookingRule(facilityKey).getBookingStrategy() == BookingStrategy.QUEUE_BASED);
			result.put("facility_name", FacilityConfigDao.getInstance().getLabel(facilityKey));
			result.put("content", this.getCalendarHtml(templateResource, facilityKey));
			QueryString link_overview = QueryStringFactory.get(QueryKeys.QUERY_KEY_CALENDAR_VIEW, QueryKeys.OVERVIEW);
			link_overview.put(QueryKeys.QUERY_KEY_FACILITY, facilityKey);
			result.put("link_hidden_inputs", link_overview.getAsHtmlInputsTypeHidden());
			result.put("text_facilitykey", facilityKey);
			result.put("name_facilitykey", QueryKeys.QUERY_KEY_FACILITY);
			if (is_overview) {
				result.put("link_overview", link_overview.getAsHtmlLinkHref() + "#newrule");
				result.put("link_overview_text", "Add a new rule"); // INTLANG
				result.put("add_or_edit", "add");
			} else {
				result.put("link_overview", link_overview.getAsHtmlLinkHref());
				result.put("link_overview_text", "Edit availability rules"); // INTLANG
				result.put("add_or_edit", "edit");
			}
			link_overview.put(QueryKeys.QUERY_KEY_CALENDAR_VIEW, QueryKeys.WEEK);
			result.put("link_weekview", link_overview.getAsHtmlLinkHref());
			link_overview.put(QueryKeys.QUERY_KEY_CALENDAR_VIEW, QueryKeys.MONTH);
			result.put("link_monthview", link_overview.getAsHtmlLinkHref());
			// has many units
			boolean hasChildren = facility.hasChildren();
			int units = facility.isBookable() ? ((FacilityBookable) facility).getCapacityUnits() : 1;
			result.put("flag_hasmanyunits", units > 1);
			result.put("flag_haschildren", hasChildren);
			if (hasChildren) {
				result.put("number_children", facility.getChildren().size());
			} else {
				result.put("number_children", "");
			}
			if (units > 1) {
				result.put("string_nameofunits", FamText.capacityUnits((FacilityBookable) facility));
			} else {
				result.put("string_nameofunits", "");
			}

			// set edit form
			boolean suddenFailureIsActive = this.suddenFailureIsActive(facilityKey);
			if (is_overview && !suddenFailureIsActive) { // show "add a rule"
				result.put("show_edit", true);
				result.put("show_suddenFailureIsActive", false);
				result.put("querystring", QueryStringBuilder.getBigCalendarQueryString(facilityKey, RequestInterpreter.getCalendar(templateResource.getRequest()), QueryKeys.OVERVIEW));

				// get actual step
				int actualStep = 1;
				try {
					actualStep = Integer.parseInt(RequestInterpreter.getOf(templateResource.getRequest()));
				} catch (NumberFormatException e) {
				} // ← stay step 1

				QueryString hiddenInputs = null;
				// ↘ it is the final sending of add new rule
				if (actualStep >= 4) {
					hiddenInputs = new QueryString();
					hiddenInputs.put(QueryKeys.QUERY_KEY_FACILITY, facilityKey);
					hiddenInputs.put(QueryKeys.QUERY_KEY_CALENDAR_VIEW, QueryKeys.OVERVIEW);
					actualStep = 1;
				} else {
					hiddenInputs = QueryStringFactory.getInstance().get(templateResource.getRequest());
				}
				hiddenInputs.put(QueryKeys.QUERY_KEY_OF, actualStep + 1);
				// ↘ set flag: nothing shall be deleted when submit form
				hiddenInputs.put(QueryKeys.QUERY_KEY_DELETE, "-1");

				switch (actualStep) {
				case 1:
					// choose of availability
					result.put("name_available", QueryKeys.QUERY_KEY_AVAILABLILITY);
					result.put("value_available_nomaintenance", FacilityAvailability.MAINTENANCE_NOT_AVAILABLE);
					result.put("text_available_nomaintenance", FamText.facilityAvailability(FacilityAvailability.MAINTENANCE_NOT_AVAILABLE));
					result.put("value_available_noingeneral", FacilityAvailability.GENERAL_NOT_AVAILABLE);
					result.put("text_available_noingeneral", FamText.facilityAvailability(FacilityAvailability.GENERAL_NOT_AVAILABLE));
					result.put("value_available_mustnotstarthere", FacilityAvailability.BOOKING_MUST_NOT_START_HERE);
					result.put("text_available_mustnotstarthere", FamText.facilityAvailability(FacilityAvailability.BOOKING_MUST_NOT_START_HERE));
					result.put("value_available_available", FacilityAvailability.COMPLETE_AVAILABLE);
					result.put("text_available_available", FamText.facilityAvailability(FacilityAvailability.COMPLETE_AVAILABLE));

					// choose interval
					result.put("name_interval", QueryKeys.QUERY_KEY_ITERATION);
					result.put("value_interval_onetime", FacilityAvailability.ONE_TIME);
					result.put("value_interval_yearly", FacilityAvailability.EACH_YEAR);
					result.put("value_interval_monthly", FacilityAvailability.EACH_MONTH);
					result.put("value_interval_weekly", FacilityAvailability.EACH_WEEK);
					result.put("value_interval_daily", FacilityAvailability.EACH_DAY);
					break;
				case 2:
					result.put("text_info", this.getSummary(templateResource.getRequest()));
					result.put("select_time", this.getTimeInput(templateResource.getRequest()));
					result.put("name_notice", QueryKeys.QUERY_KEY_TEXT_NOTICE);
					break;
				case 3:
					result.put("text_info", this.getSummary(templateResource.getRequest()));
					break;
				}

				// set actual step
				result.put("actual_step", actualStep);

				result.put("hiddenInputs", hiddenInputs.getAsHtmlInputsTypeHidden());
			} else if (suddenFailureIsActive) {
				result.put("show_edit", false);
				result.put("show_suddenFailureIsActive", true);
				result.put("text_facilitykey", facilityKey);
				result.put("name_facilitykey", QueryKeys.QUERY_KEY_FACILITY);
				result.put("value_yes", QueryKeys.YES);
				result.put("name_yes", QueryKeys.QUERY_KEY_YES_NO);
				result.put("querystring", QueryStringBuilder.getBigCalendarQueryString(facilityKey, RequestInterpreter.getCalendar(templateResource.getRequest()), QueryKeys.OVERVIEW));
			} else { // it is not the overview / editing view
				result.put("show_edit", false);
				result.put("show_suddenFailureIsActive", false);
			}
		}
		return result;

	}
}