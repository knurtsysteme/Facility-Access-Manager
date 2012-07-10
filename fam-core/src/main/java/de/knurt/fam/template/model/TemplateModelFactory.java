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

import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_CALENDAR_VIEW;
import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_SHOW_DETAILS;
import static de.knurt.fam.core.util.mvc.QueryKeys.WEEK;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.connector.FamSystemUpdateNotifier;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.FamCalendarConfiguration;
import de.knurt.fam.core.content.html.FacilityOverviewHtml;
import de.knurt.fam.core.content.html.calendar.FamMonthAvailabilityHtml;
import de.knurt.fam.core.content.html.calendar.FamMonthHtml;
import de.knurt.fam.core.content.html.calendar.FamWeekAvailabilityBookingsHtml;
import de.knurt.fam.core.content.html.calendar.FamWeekHtml;
import de.knurt.fam.core.content.html.calendar.factory.BookFacilityInputHtmlFactory;
import de.knurt.fam.core.content.html.calendar.factory.FamCalendarHtmlFactory;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.control.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.model.config.BookingStrategy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.UsersUnitsQueueBasedBookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.util.JSONFactory;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.util.time.CalendarViewResolver;
import de.knurt.fam.news.NewsItem;
import de.knurt.fam.plugin.DefaultPluginResolver;
import de.knurt.fam.template.util.QuicksandHtml;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * produce the model for specific pages
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/15/2010)
 */
@SuppressWarnings("deprecation")
// TODO #361 kill uses of deprecations
public class TemplateModelFactory {
	private TemplateResource templateResource;

	public TemplateModelFactory(TemplateResource templateResource) {
		this.templateResource = templateResource;
	}

	/**
	 * generate the model by delegating to view dependend controller
	 * 
	 * @return properties for the view in {@link #templateResource}
	 */
	public Properties getProperties() {
		Properties result = new Properties();
		result.put("invalid_session", this.templateResource.isInvalidSession());
		if (this.templateResource.hasAuthUser()) {
			List<NewsItem> newsItems = this.getNewsItems();
			result.put("newsitems", newsItems);
			result.put("newsitems_last_update", this.templateResource.getSession().getNewsItemsLastUpdate());
			result.put("newsitems_count", newsItems.size());
		}
		if (templateResource.getName().equals("users")) {
			result.put("users", FamDaoProxy.userDao().getAll());
			result.put("facilities", FacilityConfigDao.getInstance().getAll());
			result.put("roles", RoleConfigDao.getInstance().getAll());
			try {
				result.put("jsonvar", String.format("var Facilities = %s", JSONFactory.me().getFacilities(FacilityConfigDao.getInstance().getAll())));
			} catch (JSONException e) {
				FamLog.exception(e, 201011131321l);
				result.put("jsonvar", String.format("var Facilities = null"));
			}
		} else if (templateResource.getName().equals("plugins")) {
			result.put("plugins", DefaultPluginResolver.me().getPlugins());
		} else if (templateResource.getName().equals("corehome")) {
			result.put("jui_tabs", QuicksandHtml.getJuiTabs(templateResource));
			result.put("quicksand_clickitems", QuicksandHtml.getClickItems(templateResource));
		} else if (templateResource.getName().equals("adminhome")) {
			result.put("system", FamSystemUpdateNotifier.actualVersion());
			result.put("jui_tabs", QuicksandHtml.getJuiTabs(templateResource));
			result.put("quicksand_clickitems", QuicksandHtml.getClickItems(templateResource));
		} else if (templateResource.getName().equals("systembookings")) {
			List<Booking> bookings = new ArrayList<Booking>();
			User user = templateResource.getAuthUser();
			if (user != null) {
				// prepare candidates
				List<Booking> candidates;
				if (user.isAdmin()) {
					candidates = FamDaoProxy.bookingDao().getAll();
				} else {
					if (FamDaoProxy.facilityDao().hasResponsibilityForAFacility(user)) {
						List<Facility> facilities = FamDaoProxy.facilityDao().getBookableFacilitiesUserIsResponsibleFor(user);
						candidates = FamDaoProxy.bookingDao().getAll(facilities);
					} else {
						candidates = new ArrayList<Booking>();
					}
				}
				for (Booking candidate : candidates) {
					if (!candidate.isCanceled() && !candidate.sessionAlreadyMade() && !candidate.getFacility().isUnknown()) {
						bookings.add(candidate);
					}
				}
			}
			result.put("bookings", bookings);
		} else if (templateResource.getName().equals("systemlistofusermails")) {
			result.put("mails", FamDaoProxy.userDao().getAllUserMails());
		} else if (templateResource.getName().equals("systemlistofrolesandrights")) {
			result.put("roles", RoleConfigDao.getInstance().getAll());
			result.put("availablerights", this.getAvailableRights());
		} else if (templateResource.getName().equals("contactdetails")) {
			result.putAll(new ContactDetailsModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("singletermsofuseadminview")) {
			result.putAll(new SingleTermsOfUseAdminViewModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("agreementen") || templateResource.getName().equals("agreementde")) {
			result.putAll(new AgreementModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("editsoa")) {
			result.putAll(new EditSoaModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("logbookmakepost")) {
			result.putAll(new LogbookMakePostModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("logbook")) {
			result.putAll(new LogbookModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("lettergenerator")) {
			result.putAll(new LettergeneratorModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("jobsmanager")) {
			result.putAll(new JobsManagerModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("editfeedback")) {
			result.putAll(new RequestedBookingModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("viewfeedback")) {
			result.putAll(new RequestedBookingModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("systemlistofconfiguredfacilities")) {
			result.put("facilities", FacilityConfigDao.getInstance().getAll());
		} else if (templateResource.getName().equals("book2")) {
			result.putAll(new Book2ModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("news")) {
		} else if (templateResource.getName().equals("book")) {
			FacilityBookable bd = RequestInterpreter.getBookableFacility(this.templateResource.getRequest());
			if (bd == null) {
				// TODO #135 if there is only one facility show it directly
				// ↓ create overview with first root facility
				result.put("client_redirect", TemplateHtml.me().getHref("book2"));
				result.put("actualQueueLength", "");
				result.put("bookOrApplyFor", "");
				result.put("hiddenInput", "");
				result.put("expectedYourTurnAt", "");
				result.put("jsoncalendarmetrics", "");
				result.put("rightsSummary", "");
				result.put("content", "");
				result.put("facility", FacilityConfigDao.getUnknownBookableFacility());
			} else {
				// ↖ found a facility
				result.put("facility", bd);
				if (bd.getBookingStrategy() == BookingStrategy.QUEUE_BASED) {
					result.put("booking_strategy_is_queue_based", true);
					UsersUnitsQueueBasedBookingRule br = (UsersUnitsQueueBasedBookingRule) bd.getBookingRule();
					QueueBooking qb = new QueueBooking(this.templateResource.getAuthUser(), bd);
					SessionAuth.addToUsersShoppingCart(this.templateResource.getRequest(), qb);
					result.put("actualQueueLength", br.getActualQueueLength());
					result.put("bookOrApplyFor", qb.isBooked() ? "Book" : "Apply for");
					result.put("hiddenInput", QueryStringBuilder.getArticleNumber(qb).getAsHtmlInputsTypeHidden());
					result.put("expectedYourTurnAt", FamDateFormat.getDateFormattedWithTime(qb.getExpectedSessionStart()));
				} else {
					// ↖ time based facility
					result.put("booking_strategy_is_queue_based", false);
					JSONObject calendarMetrics = new JSONObject();
					try {
						calendarMetrics.put("startMinutesOfDay", FamCalendarConfiguration.hourStart() * 60);
						calendarMetrics.put("endMinutesOfDay", FamCalendarConfiguration.hourStop() * 60);
					} catch (JSONException e) {
						FamLog.logException(this.getClass(), e, "error creating json", 201010160922l);
					}
					result.put("jsoncalendarmetrics", calendarMetrics);
					result.put("rightsSummary", this.getRightsSummary(this.templateResource.getAuthUser(), bd));

					// calendar html
					String calendarHtml = "";
					Calendar cal;
					if (this.templateResource.getRequest().getParameter(QUERY_KEY_SHOW_DETAILS) != null) {
						cal = RequestInterpreter.getCalendarStart(this.templateResource.getRequest().getParameter(QUERY_KEY_SHOW_DETAILS));
					} else {
						cal = RequestInterpreter.getCalendar(this.templateResource.getRequest());
					}
					String calView = this.templateResource.getRequest().getParameter(QUERY_KEY_CALENDAR_VIEW);
					if (calView == null || QueryStringBuilder.isValidCalendarView(calView) == false) {
						Facility d = RequestInterpreter.getFacility(this.templateResource.getRequest());
						if (d == null) {
							calView = CalendarViewResolver.getInstance().getDefaultCalendarView(FacilityConfigDao.getUnknownBookableFacility());
						} else {
							calView = CalendarViewResolver.getInstance().getDefaultCalendarView(d);
						}
					}
					FamCalendarHtmlFactory htmlFactory = new BookFacilityInputHtmlFactory(RequestInterpreter.getBookingWishFromRequest(cal, bd.getKey(), this.templateResource.getRequest(), this.templateResource.getAuthUser()));
					boolean showRedGreenOnly = RequestInterpreter.hasAjaxFlag(this.templateResource.getRequest());
					if (calView.equals(WEEK)) {
						FamWeekHtml calhtml = new FamWeekAvailabilityBookingsHtml(cal, bd.getKey(), htmlFactory, showRedGreenOnly, true);
						calendarHtml = calhtml.toString();
					} else { // month view
						FamMonthHtml calhtml = new FamMonthAvailabilityHtml(cal, htmlFactory, "onemonthdaybookings", FacilityConfigDao.facility(bd.getKey()), showRedGreenOnly, true);
						calendarHtml = calhtml.toString();
					}
					result.put("content", calendarHtml);
				}
			}
		} else if (templateResource.getName().equals("bookfacilitiesdone")) {
			result.putAll(new BookFacilitiesDoneModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("statistics")) {
			result.putAll(new StatisticsModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("tutorial")) {
			result.putAll(new TutorialModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("viewrequest")) {
			result.putAll(new RequestedBookingModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("editrequest")) {
			result.putAll(new RequestedBookingModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("viewsystemconfiguration")) {
			result.putAll(new ViewSystemConfigurationModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("filemanager")) {
			result.putAll(new FileManagerModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("systemmodifyapplications")) {
			result.putAll(new SystemModifyApplicationsModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("facilityemergency")) {
			result.putAll(new FacilityEmergencyModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("eventsofday")) {
			result.putAll(new EventsOfDayModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("editfacilityavailability")) {
			result.putAll(new EditFacilityAvailabilityModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("termsofuse")) {
			result.putAll(new TermsOfUseModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("configjobsurvey")) {
			result.putAll(new ConfigJobSurveyModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("systemfacilityavailabilityoverview")) {
			Facility root = FacilityConfigDao.getInstance().getRootFacility();
			String baseUrl = TemplateHtml.href("systemfacilityavailability");
			result.put("facilitieslist", FacilityOverviewHtml.getTree(root, baseUrl, getFacilitiesUserCanBook(this.templateResource.getAuthUser())));
			result.put("jsonfacilities", getJSONFacilities(root.getKey()));
			result.put("jsonvar", String.format("var FacilityOverviewTreeUrlBase = '%s'", baseUrl));
		} else if (templateResource.getName().equals("systemfacilityavailability")) {
			// TODO #135 if there is only one facility show it directly
			result.putAll(new FacilityAvailabilityModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("mybookings")) {
			result.put("bookings", templateResource.getAuthUser().getBookings());
		}
		if (templateResource.getWritingResultProperties() != null) {
			result.putAll(templateResource.getWritingResultProperties());
		}
		result.put("templateResource", templateResource);
		return result;
	}

	private List<NewsItem> getNewsItems() {
		List<NewsItem> result = null;
		if (this.templateResource.hasAuthUser()) {
			try {
				result = new NewsModelFactory().getNewsItems(templateResource);
			} catch (Exception e) {
				// ↖ avoid news blocking the system
				FamLog.exception(e, 201107200854l);
				result = NewsModelFactory.getErrorNewsItem();
			}
		} else {
			result = new ArrayList<NewsItem>(0);
		}
		return result;
	}

	private HtmlElement getRightsSummary(User user, FacilityBookable bd) {
		HtmlElement result = HtmlFactory.get("ul").addClassName("asList");

		String tmpMessage = user.hasRight(FamAuth.DIRECT_BOOKING, bd) ? "<strong>You are allowed to book</strong> this facility without an application." : "<strong>You have to apply</strong> for using this facility."; // INTLANG
		result.add(HtmlFactory.get("li").add(tmpMessage));

		if (bd.getCapacityUnits() > 1) {
			tmpMessage = String.format("You have to request <strong>at least %s</strong> and <strong>%s at most</strong>.", bd.getBookingRule().getCapacityLabelOfMin(user), bd.getBookingRule().getCapacityLabelOfMax(user));
			result.add(HtmlFactory.get("li").add(tmpMessage));
		}

		if (FamAuth.getEarliestPossibilityToBookFromNow(user, bd) > 0) {
			Calendar earliest = FamAuth.getEarliestCalendarToBookFromNow(user, bd);
			tmpMessage = String.format("This facility is <strong>not available before %s</strong>", FamDateFormat.getDateFormattedWithTime(earliest));
			result.add(HtmlFactory.get("li").add(tmpMessage));
		}

		tmpMessage = String.format("Your request has to be between <strong>%s</strong> and <strong>%s</strong>.", bd.getBookingRule().getTimeLabelOfMin(user), bd.getBookingRule().getTimeLabelOfMax(user));
		result.add(HtmlFactory.get("li").add(tmpMessage));

		return result;
	}

	protected static JSONObject getJSONFacilities(String facilityKey) {
		List<Facility> facilities = new ArrayList<Facility>(1);
		facilities.add(FacilityConfigDao.facility(facilityKey));
		return getJSONFacilities(facilities);
	}

	protected static JSONObject getJSONFacilities(List<Facility> facilities) {
		JSONObject result = new JSONObject();
		JSONArray keys = new JSONArray();
		for (Facility facility : facilities) {
			keys.put(facility.getKey());
		}
		try {
			result.put("keys", keys);
		} catch (JSONException e) {
			FamLog.exception("error creating json", e, 201010160922l);
		}
		return result;
	}

	protected static List<Facility> getFacilitiesUserCanBook(User user) {
		// create facilities to link
		List<FacilityBookable> candidates = FacilityConfigDao.getInstance().getBookableFacilities();
		List<Facility> result = new ArrayList<Facility>();
		for (FacilityBookable bd : candidates) {
			if (user.hasRight(FamAuth.BOOKING, bd)) {
				result.add(bd);
			}
		}
		return result;
	}

	private List<String> getAvailableRights() {
		List<String> result = new ArrayList<String>();
		int i = 0;
		while (FamText.getInstance().messageExists("right." + i)) {
			result.add(i + ": " + FamText.message("right." + i));
			i++;
		}
		return result;
	}

}
