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
package de.knurt.fam.core.util.mvc.validator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;

/**
 * this is a validator out of the box. it is used to decide which input for
 * which user is mandatory.
 * 
 * @author Daniel Oltmanns
 * @since 0.20100131 (01/31/2009)
 */
public class MandatoryUserFieldValidator implements MandatoryFieldValidator<User> {

	/**
	 * return true, if the field with the given <code>fieldname</code> is
	 * mandatory for the given user.
	 * 
	 * @param user
	 *            to check the mandatory for.
	 * @param key
	 *            to check
	 * @return true, if the field with the given <code>fieldname</code>
	 */
	public boolean isMandatory(User user, String key) throws InvalidRoleIdException {
		boolean result = false;
		String key2check = key;
		if (key.equals("departmentLabel")) {
			key2check = "department";
		}
		// ↘ role of user is known
		if (this.mandatoryRolesAndFields.containsKey(user.getRoleId())) {
			// ↘ iterate all known keys
			for (String mandatoryKey : this.mandatoryRolesAndFields.get(user.getRoleId())) {
				// ↘ key asked for exists and is mandatory then
				if (mandatoryKey.equals(key2check)) {
					result = true;
					break;
				}
			}
		} else {
			throw new InvalidRoleIdException(user);
		}
		return result;
	}

	/** one and only instance of me */
	private volatile static MandatoryUserFieldValidator me;
	private Map<String, String[]> mandatoryRolesAndFields;

	/** construct me */
	private MandatoryUserFieldValidator() {
	}

	/**
	 * return the one and only instance of MandatoryUserFieldValidator
	 * 
	 * @return the one and only instance of MandatoryUserFieldValidator
	 */
	public static MandatoryUserFieldValidator getInstance() {
		if (me == null) { // no instance so far
			synchronized (MandatoryUserFieldValidator.class) {
				if (me == null) { // still no instance so far
					me = new MandatoryUserFieldValidator(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * set a combination of role ids and fieldnames, that are required for the
	 * given role id.
	 * 
	 * @param mandatoryRolesAndFields
	 *            to set
	 */
	@Required
	public void setMandatoryRolesAndFields(Map<String, String[]> mandatoryRolesAndFields) {
		this.mandatoryRolesAndFields = mandatoryRolesAndFields;
	}

	public boolean isSufficient(User user, String key) throws InvalidRoleIdException {
		boolean result = false;
		key = this.getKey2Check(key);
		if (this.isMandatory(user, key)) {
			if (key.equals("birthdate")) {
				result = user.getBirthdate() != null;
			} else if (key.equals("male")) {
				result = user.getMale() != null;
			} else if (key.equals("statementOfAgreementAccepted")) {
				result = user.isAcceptedStatementOfAgreement() == true;
			} else if (key.equals("title")) {
				result = this.isNotNullAndNotEmpty(user.getTitle());
			} else if (key.equals("sname")) {
				result = this.isNotNullAndNotEmpty(user.getSname());
			} else if (key.equals("fname")) {
				result = this.isNotNullAndNotEmpty(user.getFname());
			} else if (key.equals("intendedResearch")) {
				result = this.isNotNullAndNotEmpty(user.getIntendedResearch());
			} else if (key.equals("company")) {
				result = this.isNotNullAndNotEmpty(user.getCompany());
			} else if (key.equals("department") || key.equals("departmentLabel")) {
				result = this.isNotNullAndNotEmpty(user.getDepartmentLabel());
			} else if (key.equals("oneofphones")) {
				result = this.isNotNullAndNotEmpty(user.getPhone1());
				if (!result) {
					result = this.isNotNullAndNotEmpty(user.getPhone2());
				}
			} else if (key.equals("address")) { // it is an address part
				result = user.getMainAddress() != null;
				if (result) {
					Address add = user.getMainAddress();
					result = this.isNotNullAndNotEmpty(add.getStreet()) && this.isNotNullAndNotEmpty(add.getCity()) && this.isNotNullAndNotEmpty(add.getZipcode()) && this.isNotNullAndNotEmpty(add.getCountry()) && this.isNotNullAndNotEmpty(add.getStreetno());
				}
			}
		} else { // not mandatory
			result = true;
		}
		return result;
	}

	private boolean isNotNullAndNotEmpty(String toCheck) {
		return toCheck != null && !toCheck.trim().isEmpty();
	}

	/**
	 * return true, if only one of the needed fields is unsufficient or if the
	 * role of the user is unknown.
	 * 
	 * information of admins is always sufficient.
	 * 
	 * @param user
	 *            to check
	 * @return true, if only one of the needed fields is unsufficient
	 */
	public Boolean isSufficient(User user) throws InvalidRoleIdException {
		boolean result = true;
		if (!user.isAdmin()) {
			if (this.mandatoryRolesAndFields.containsKey(user.getRoleId())) { // role
				// of user is known
				for (String mandatoryKey : this.mandatoryRolesAndFields.get(user.getRoleId())) { // all
					// known keys
					result = this.isSufficient(user, mandatoryKey);
					if (result == false) { 
						// done! - it's insufficient
						break;
					}
				}
			} else {
				throw new InvalidRoleIdException(user);
			}
		}
		return result;
	}

	/**
	 * in this implementation, there are different keys with kind of subkeys.
	 * like the given key is "street", the key to check is "address" by now.
	 * this will return the "parent key"
	 * 
	 * @param key
	 * @return
	 */
	private String getKey2Check(String key) {
		String result = key;
		if (key.equals("street") || key.equals("streetno") || key.equals("city") || key.equals("country") || key.equals("zipcode")) {
			result = "address";
		} else if (key.equals("phone1") || key.equals("phone2")) {
			result = "oneofphones";
		}
		return result;
	}
}
