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
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.connector.FamSystemUpdateNotifier;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.JSONFactory;
import de.knurt.fam.core.view.html.FacilityOverviewHtml;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.news.NewsItem;
import de.knurt.fam.plugin.DefaultPluginResolver;
import de.knurt.fam.template.util.QuicksandHtml;
import de.knurt.fam.template.util.TemplateHtml;

/**
 * produce the model for specific pages
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/15/2010)
 */
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
			result.putAll(new CoreHomeModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("adminhome")) {
			result.put("system", FamSystemUpdateNotifier.currentVersion());
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
		} else if (templateResource.getName().equals("book2") || templateResource.getName().equals("book")) {
			result.putAll(new Book2ModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("news")) {
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
			// TODO #22 if there is only one facility show it directly
			result.putAll(new FacilityAvailabilityModelFactory().getProperties(templateResource));
		} else if (templateResource.getName().equals("mybookings")) {
			result.put("bookings", templateResource.getAuthUser().getBookings());
			result.put("current_sessions", FamDaoProxy.bookingDao().getCurrentSessions(templateResource.getAuthUser()));
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
