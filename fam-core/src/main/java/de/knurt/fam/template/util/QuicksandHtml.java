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
package de.knurt.fam.template.util;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.template.model.TemplateResource;

/**
 * generate the quicksand navigation on the home page.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (07/30/2010)
 */
public class QuicksandHtml {

	public static List<String[]> getJuiTabs(TemplateResource templateResource) {
		User user = templateResource.getAuthUser();
		List<String[]> jui_tabs = new ArrayList<String[]>();
		jui_tabs.add(new String[] { "All Services", "quicksand_button_show_all", "show_all" }); // INTLANG
		if (templateResource.getVisibility() == TemplateResource.Visibility.ADMIN) {
			if (user.hasRight2ViewPage("systemfacilityavailability")) {
				jui_tabs.add(new String[] { "Facilities", "quicksand_button_manage_facilities", "manage_facilities" }); // INTLANG
			}
			if (user.hasRight2ViewPage("users")) {
				jui_tabs.add(new String[] { "Users", "quicksand_button_administrate_system", "administrate_user" }); // INTLANG
			}
			if (user.hasAdminTasks() || user.hasRight2ViewPage("systemmodifyapplications") || user.hasRight2ViewPage("jobsmanager")) {
				jui_tabs.add(new String[] { "Bookings", "quicksand_button_administrate_system", "administrate_bookings" }); // INTLANG
			}
			if (user.hasRight2ViewPage("editsoa")) {
				jui_tabs.add(new String[] { "System", "quicksand_button_administrate_system", "administrate_system" }); // INTLANG
			}
			jui_tabs.add(new String[] { "Information", "quicksand_button_spot_on_information", "spot_on_information" }); // INTLANG
		} else {
			jui_tabs.add(new String[] { "Access Facilities", "quicksand_button_access_facilities", "access_facilities" }); // INTLANG
			jui_tabs.add(new String[] { "My Account", "quicksand_button_access_my_account", "access_my_account" }); // INTLANG
			jui_tabs.add(new String[] { "Help", "quicksand_button_spot_on_information", "spot_on_information" }); // INTLANG
		}
		return jui_tabs;
	}

	public static List<String[]> getClickItems(TemplateResource templateResource) {
		User user = templateResource.getAuthUser();
		// ↘ generate clickitems ...
		List<String[]> clickitems = new ArrayList<String[]>();
		// ↘ ... for "access facilities"
		List<String> dataTypes = new ArrayList<String>();
		if (templateResource.getVisibility() == TemplateResource.Visibility.ADMIN) {
			dataTypes.removeAll(dataTypes);
			dataTypes.add("manage_facilities");
			if (user.hasRight2ViewPage("systemfacilityavailability")) {
				clickitems.add(new String[] { "quicksand_calendar_icon", "icons/calendar_icon.gif", getQuicksandClasses(dataTypes), "systemfacilityavailabilityoverview" });
			}
			if (user.hasRight2ViewPage("facilityemergency")) {
				clickitems.add(new String[] { "quicksand_alert_icon", "icons/alert_icon.gif", getQuicksandClasses(dataTypes), "facilityemergency" });
			}
			if (user.hasRight2ViewPage("configjobsurvey")) {
				clickitems.add(new String[] { "quicksand_configjobsurvey_icon", "icons/configjobsurvey_icon.gif", getQuicksandClasses(dataTypes), "configjobsurvey" });
			}

			dataTypes.removeAll(dataTypes);
			dataTypes.add("administrate_user");
			if (user.hasRight2ViewPage("users")) {
				clickitems.add(new String[] { "quicksand_people_icon", "icons/people.gif", getQuicksandClasses(dataTypes), "users" });
			}
			dataTypes.removeAll(dataTypes);
			dataTypes.add("administrate_bookings");
			if (user.hasAdminTasks()) {
				clickitems.add(new String[] { "quicksand_see_applications_icon", "icons/see_applications_icon.gif", getQuicksandClasses(dataTypes), "systemmodifyapplications" });
			}
			if (user.hasRight2ViewPage("jobsmanager")) {
				clickitems.add(new String[] { "quicksand_jobsmanager_icon", "icons/jobsmanager.gif", getQuicksandClasses(dataTypes), "jobsmanager" });
			}

			dataTypes.removeAll(dataTypes);
			dataTypes.add("administrate_system");
			if (user.hasAdminTasks() && user.hasRight2ViewPage("editsoa")) {
				clickitems.add(new String[] { "quicksand_verifypeople_icon", "icons/edit-find-replace.png", getQuicksandClasses(dataTypes), "editsoa" });
			}
			if (user.hasAdminTasks() && user.hasRight2ViewPage("lettergenerator")) {
				clickitems.add(new String[] { "quicksand_lettergenerator_icon", "icons/mail-message-new.png", getQuicksandClasses(dataTypes), "lettergenerator" });
			}

			dataTypes.removeAll(dataTypes);
			dataTypes.add("spot_on_information");
			if (user.hasRight2ViewPage("systembookings")) {
				clickitems.add(new String[] { "quicksand_bookings_icon", "icons/bookings_icon.gif", getQuicksandClasses(dataTypes), "systembookings" });
			}
			if (user.hasRight2ViewPage("statistics")) {
				clickitems.add(new String[] { "quicksand_admin_show_statistics_icon", "icons/statistics.gif", getQuicksandClasses(dataTypes), "statistics" });
			}
			if (user.hasRight2ViewPage("systemlistofusermails")) {
				clickitems.add(new String[] { "quicksand_letter_icon", "icons/letter_icon.gif", getQuicksandClasses(dataTypes), "systemlistofusermails" });
			}
			if (user.hasRight2ViewPage("systemlistofconfiguredfacilities")) {
				clickitems.add(new String[] { "quicksand_admin_icon", "icons/admin_icon.gif", getQuicksandClasses(dataTypes), "systemlistofconfiguredfacilities" });
			}
			if (user.hasRight2ViewPage("systemlistofrolesandrights")) {
				clickitems.add(new String[] { "quicksand_rolesrights_icon", "icons/rolesandrights_icon.gif", getQuicksandClasses(dataTypes), "systemlistofrolesandrights" });
			}
			if (user.hasAdminTasks()) {
				clickitems.add(new String[] { "quicksand_meta_icon", "icons/manreadbook_icon.gif", getQuicksandClasses(dataTypes), "systemmeta" });
			}
		} else {
			// ↘ ... for "Access"
			dataTypes.add("access_facilities");
			clickitems.add(new String[] { "quicksand_access_facilities_icon", "icons/access_facilities_icon.gif", getQuicksandClasses(dataTypes), "book2" });
			clickitems.add(new String[] { "quicksand_cancelation_icon", "icons/appointment-new.png", getQuicksandClasses(dataTypes), "mybookings" });
			clickitems.add(new String[] { "quicksand_filemanager_icon", "icons/system-file-manager.png", getQuicksandClasses(dataTypes), "filemanager" });
			
			// ↘ ... for "Access" and "Spot on information"
			dataTypes.add("spot_on_information");
			clickitems.add(new String[] { "quicksand_logbooks_icon", "icons/logbooks_icon.gif", getQuicksandClasses(dataTypes), "logbook" });

			// ↘ ... for "Spot on information"
			dataTypes.removeAll(dataTypes);
			dataTypes.add("spot_on_information");
			if (FamConnector.getGlobalPropertyAsBoolean("show_home_on_quicksand")) {
				clickitems.add(new String[] { "quicksand_information_icon", "icons/information_icon.gif", getQuicksandClasses(dataTypes), "home" });
			}
			if (FamConnector.getGlobalPropertyAsBoolean("show_terms_on_quicksand")) {
				clickitems.add(new String[] { "quicksand_terms_icon", "icons/paragraf.png", getQuicksandClasses(dataTypes), "termsofuse" });
			}
			clickitems.add(new String[] { "quicksand_blackboeard_icon", "icons/blackboeard_icon.gif", getQuicksandClasses(dataTypes), "tutorial" });
			
			// ↘ ... for "Access"
			dataTypes.removeAll(dataTypes);
			dataTypes.add("access_my_account");
			clickitems.add(new String[] { "quicksand_my_profile_icon", "icons/my_profile_icon.gif", getQuicksandClasses(dataTypes), "contactdetails" });
			clickitems.add(new String[] { "quicksand_changepassword_icon", "icons/ryanlerch_sword_battleaxe_shield.png", getQuicksandClasses(dataTypes), "changepassword" });
		}

		return clickitems;
	}

	private static String getQuicksandClasses(List<String> dataTypes) {
		String result = "";
		for (String dataType : dataTypes) {
			result += dataType + " ";
		}
		return result + "show_all";
	}

}
