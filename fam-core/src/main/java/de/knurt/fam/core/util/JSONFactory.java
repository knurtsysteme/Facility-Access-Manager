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
package de.knurt.fam.core.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;

/**
 * adapt fam objects to json
 * 
 * @see User
 * @author Daniel Oltmanns
 * @since 1.3.0 (08/08/2010)
 */
public class JSONFactory {
	/** one and only instance of UserFactory */
	private volatile static JSONFactory me;

	/** construct UserFactory */
	private JSONFactory() {
	}

	/**
	 * return the one and only instance of UserFactory
	 * 
	 * @return the one and only instance of UserFactory
	 */
	public static JSONFactory getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (JSONFactory.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new JSONFactory();
				}
			}
		}
		return me;
	}
	
	private static String emptyOnNull(String input) {
	  return input == null ? "" : input;
	}

	public JSONObject getUser(User user) throws JSONException {
		JSONObject result = new JSONObject();
		Address address = user.getMainAddress();
		if (address != null) {
			result.put("city", emptyOnNull(address.getCity()));
			result.put("street", emptyOnNull(address.getStreet()));
			result.put("streetno", emptyOnNull(address.getStreetno()));
			result.put("zipcode", emptyOnNull(address.getZipcode()));
			result.put("country", emptyOnNull(address.getCountry()));
		} else {
			result.put("city", "");
			result.put("street", "");
			result.put("streetno", "");
			result.put("zipcode", "");
			result.put("country", "");
		}

		result.put("birthdate", emptyOnNull(user.getBirthdateFormValue()));
		result.put("account_expires", emptyOnNull(user.getAccountExpiresFormValue()));
		result.put("departmentkey", emptyOnNull(user.getDepartmentKey()));
		result.put("departmentlabel", emptyOnNull(user.getDepartmentLabel()));
		result.put("fname", emptyOnNull(user.getFname()));
		result.put("intendedResearch", emptyOnNull(user.getIntendedResearch()));
		result.put("username", emptyOnNull(user.getUsername()));
		result.put("sname", emptyOnNull(user.getSname()));
		result.put("mail", emptyOnNull(user.getMail()));
		String male = user.getMale() == null ? "" : (user.getMale() ? "1" : "0");
		result.put("male", male);
		result.put("pass", emptyOnNull(user.getPassword()));
		result.put("phone1", emptyOnNull(user.getPhone1()));
		result.put("phone2", emptyOnNull(user.getPhone2()));
		result.put("title", emptyOnNull(user.getTitle()));
		result.put("company", emptyOnNull(user.getCompany()));
		result.put("roleid", emptyOnNull(user.getRoleId()));
		result.put("rolelabel", emptyOnNull(user.getRoleLabel()));
		result.put("customFields", user.getCustomFields());
		List<ContactDetail> contactDetails = user.getContactDetails();
		if (contactDetails != null) {
			result.put("contactDetails", this.getContactDetails(contactDetails));
		} else {
			result.put("contactDetails", new JSONArray());
		}

		result.put("responsible4facilities", this.getFacilitiesUserIsResponsibleFor(user));
		result.put("id", user.getId());
		result.put("registrationdate", user.getRegistration());
		result.put("lastlogindate", user.getLastLogin());
		result.put("excluded", user.isExcluded());
		result.put("anonym", user.isAnonym());
		result.put("is_admin", user.isAdmin());
		result.put("accepted_statement_of_agreement", user.isAcceptedStatementOfAgreement());
		return result;
	}

	private JSONArray getFacilitiesUserIsResponsibleFor(User user) throws JSONException {
		JSONArray result = new JSONArray();
		if (user.getFacilityKeysUserIsResponsibleFor() != null) {
			for (String facilityKey : user.getFacilityKeysUserIsResponsibleFor()) {
				result.put(JSONFactory.me().getFacility(FacilityConfigDao.facility(facilityKey)));
			}
		}
		return result;
	}

	private JSONArray getContactDetails(List<ContactDetail> contactDetails) throws JSONException {
		JSONArray result = new JSONArray();
		for (ContactDetail contactDetail : contactDetails) {
			JSONObject detail = new JSONObject();
			detail.put("title", contactDetail.getTitle());
			detail.put("detail", contactDetail.getDetail());
			result.put(detail);
		}
		return result;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of UserFactory
	 */
	public static JSONFactory me() {
		return getInstance();
	}

	public JSONArray getFacilities(List<Facility> facilities) throws JSONException {
		JSONArray result = new JSONArray();
		for (Facility facility : facilities) {
			result.put(this.getFacility(facility));
		}
		return result;
	}

	public JSONObject getFacility(Facility facility) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("key", facility.getKey());
		result.put("label", facility.getLabel());
		result.put("parent", facility.getParentFacility() == null ? null : facility.getParentFacility().getKey());
		result.put("labelshort", facility.getShortLabel());
		return result;
	}
}
