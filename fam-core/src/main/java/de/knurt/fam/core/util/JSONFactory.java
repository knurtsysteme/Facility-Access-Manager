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

import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;

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

	public JSONObject getUser(User user) throws JSONException {
		JSONObject result = new JSONObject();
		Address address = user.getMainAddress();
		if (address != null) {
			result.put("city", address.getCity());
			result.put("street", address.getStreet());
			result.put("streetno", address.getStreetno());
			result.put("zipcode", address.getZipcode());
			result.put("country", address.getCountry());
		} else {
			result.put("city", "");
			result.put("street", "");
			result.put("streetno", "");
			result.put("zipcode", "");
			result.put("country", "");
		}

		result.put("birthdate", user.getBirthdateFormValue());
		result.put("account_expires", user.getAccountExpiresFormValue());
		result.put("departmentkey", user.getDepartmentKey());
		result.put("departmentlabel", user.getDepartmentLabel());
		result.put("fname", user.getFname());
		result.put("intendedResearch", user.getIntendedResearch());
		result.put("username", user.getUsername());
		result.put("sname", user.getSname());
		result.put("mail", user.getMail());
		String male = user.getMale() == null ? "" : (user.getMale() ? "1" : "0");
		result.put("male", male);
		result.put("pass", user.getPassword());
		result.put("phone1", user.getPhone1());
		result.put("phone2", user.getPhone2());
		result.put("title", user.getTitle());
		result.put("company", user.getCompany());
		result.put("roleid", user.getRoleId());
		result.put("rolelabel", user.getRoleLabel());
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
