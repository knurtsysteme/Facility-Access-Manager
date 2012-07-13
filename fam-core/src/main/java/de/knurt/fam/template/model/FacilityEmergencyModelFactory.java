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

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.view.html.factory.FamFormFactory;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * control the pages, where you can set a facility emergency.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090810 (08/10/2009)
 */
public class FacilityEmergencyModelFactory {

	private List<Facility> getFacilities(User user) {
		List<Facility> result = new ArrayList<Facility>();
		if (user != null && user.hasAdminTasks()) {
			if (user.isAdmin()) {
				result = FacilityConfigDao.getInstance().getAll();
			} else {
				// ↖ user is operator
				List<String> dks = user.getFacilityKeysUserIsResponsibleFor();
				for (String dk : dks) {
					result.add(FacilityConfigDao.facility(dk));
				}
			}
		}
		return result;
	}

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		List<Facility> facilities = this.getFacilities(templateResource.getAuthUser());
		if (facilities.size() > 0) {
			result.put("facilities", facilities);
			// set ${model.show_anchors2notices} and ${model.anchors2notices}
			boolean show_anchors2notices = false;
			HtmlElement anchors2notices = HtmlFactory.get("ul");
			for (Facility facility : facilities) {
				if (facility.isInWorkingOrderNow() == false) {
					FacilityAvailability da = facility.getFacilityStatus().getFacilityAvailability();
					if (da.getUsernameSetThis().equals(templateResource.getAuthUser().getUsername())) {
						QueryString qs = QueryStringBuilder.getQueryString(facility);
						qs.put(QueryKeys.QUERY_KEY_AVAILABLILITY, da.getId());
						HtmlElement anchor = HtmlFactory.get("a").att("href", "editfacilityavailability.html" + qs.toString()).add("Set notice for alert of " + facility.getLabel()); // INTLANG
						show_anchors2notices = true;
						anchors2notices.add(HtmlFactory.get("li").add(anchor));
					}
				}
			}
			if (show_anchors2notices) {
				result.put("show_anchors2notices", "t");
				result.put("anchors2notices", anchors2notices);
			} else {
				result.put("show_anchors2notices", "f");
				result.put("anchors2notices", "");
			}

			result.put("value_no", QueryKeys.NO);
			result.put("qk_yesno", QueryKeys.QUERY_KEY_YES_NO);
			result.put("qk_facility", QueryKeys.QUERY_KEY_FACILITY);

			// create options for bookable facilities only
			String options_facility = "";
			for (Facility facility : facilities) {
				if (!facility.isBookable()) {
					continue;
				}
				options_facility += String.format("<option value=\"%s\">%s</option>", facility.getKey(), facility.getLabel());
			}
			result.put("options_facility", options_facility);
			result.put("select_timeunits", FamFormFactory.getUnspecifiedTimeInput());
		}
		return result;
	}
}
