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

import java.util.Properties;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * controller for editing a facility availability
 * 
 * @author Daniel Oltmanns
 * @since 0.20091112 (11/12/2009)
 */
public class EditFacilityAvailabilityModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		FacilityAvailability da = RequestInterpreter.getExistingFacilityAvailabilityWithId(templateResource.getRequest());

		if (da != null && da.getUsernameSetThis().equals(templateResource.getAuthUser().getUsername())) {
			result.put("name_notice", QueryKeys.QUERY_KEY_TEXT_NOTICE);
			QueryString qs = QueryStringBuilder.getQueryString(da.getFacility());
			qs.put(QueryKeys.QUERY_KEY_CALENDAR_VIEW, QueryKeys.OVERVIEW);
			result.put("href2systemfacilityoverview", TemplateHtml.href("systemfacilityavailability") + qs.toString());
			qs.put(QueryKeys.QUERY_KEY_AVAILABLILITY, da.getId());
			result.put("formaction", TemplateHtml.href("editfacilityavailability") + qs);
			result.put("text_notice", da.getNotice());
		} else { // evil
			RedirectResolver.redirectClient(RedirectTarget.PROTECTED_HOME, templateResource);
		}
		return result;
	}
}
