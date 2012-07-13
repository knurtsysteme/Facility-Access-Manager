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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.heinzelmann.util.query.HttpServletRequestConverter;

/**
 * update a user given as json object. json object has olduser and newuser.
 * 
 * @link users.js
 * @author Daniel Oltmanns
 * @since 1.3.0 (11/14/2010)
 */
public class UpdateUserFromUsersManagerController extends JSONController {

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest request, HttpServletResponse response) {
		JSONObject result = new JSONObject();
		boolean succ = false;
		boolean reopened_account_mail_sent = false;
		JSONObject updatejson = null;
		try {
			updatejson = HttpServletRequestConverter.me().getJSONObject(request);
		} catch (JSONException e) {
			FamLog.exception(e, 2010111457l);
			try {
				result.put("message", "error reading json 2010111457");
			} catch (JSONException e1) {
				FamLog.exception(e, 2010111455l);
			}
		} catch (IOException e) {
			FamLog.exception(e, 2010111454l);
			try {
				result.put("message", "error reading request 2010111454");
			} catch (JSONException e1) {
				FamLog.exception(e1, 2010111453l);
			}
		}
		try {
			if (updatejson != null) {
				// ↖ request is right
				User olduser = UserFactory.me().getUser(updatejson.getJSONObject("olduser"));
				if (olduser != null && olduser.getUsername() != null && olduser.getId() != null) {
					User existingUser = FamDaoProxy.userDao().getUserFromUsername(olduser.getUsername());
					if (existingUser != null && existingUser.getId().equals(olduser.getId())) {
						User newuser = UserFactory.me().getUser(updatejson.getJSONObject("newuser"));
						if (newuser != null) {
							reopened_account_mail_sent = this.sendMailIfAccountReopend(existingUser, newuser);
							// ↖ old user exist and has right id
							// ↓ set new values and update
							existingUser.setMale(newuser.getMale());
							existingUser.setTitle(newuser.getTitle());
							existingUser.setFname(newuser.getFname());
							existingUser.setIntendedResearch(newuser.getIntendedResearch());
							existingUser.setSname(newuser.getSname());
							existingUser.setCompany(newuser.getCompany());
							existingUser.setBirthdate(newuser.getBirthdate());
							existingUser.setAccountExpires(newuser.getAccountExpires());
							existingUser.setPhone1(newuser.getPhone1());
							existingUser.setPhone2(newuser.getPhone2());
							existingUser.setDepartmentLabel(newuser.getDepartmentLabel());
							existingUser.setRoleId(newuser.getRoleId());
							existingUser.setMail(newuser.getMail());
							if (newuser.getMainAddress() != null) {
								existingUser.setMainAddress(newuser.getMainAddress());
							}
							existingUser.setExcluded(newuser.isExcluded());
							existingUser.setMainAddress(newuser.getMainAddress());
							if (newuser.getPassword() != null && newuser.getPassword().trim().length() >= 8 && newuser.getPassword().trim().length() <= 20) {
								existingUser.setCleanPassword(newuser.getPassword());
							}
							if (existingUser.update()) {
								JSONArray responsibilities = updatejson.getJSONObject("newuser").getJSONArray("responsibilities");
								List<Facility> facilities = new ArrayList<Facility>(responsibilities.length());
								for (int i = 0; i < responsibilities.length(); i++) {
									Facility f = FacilityConfigDao.facility(responsibilities.getJSONObject(i).getString("value"));
									facilities.add(f);
								}
								if (FamDaoProxy.facilityDao().updateResponsibility(existingUser, facilities)) {
									// ↓ set and add new contact details
									List<ContactDetail> cds = UserFactory.me().getContactDetails(updatejson.getJSONObject("newuser"));
									if (cds != null) {
										existingUser.updateContactDetails(cds);
									}
									succ = true;
								}
							}
						} else {
							result.put("message", "please report error 201011141103l");
							FamLog.error("bad request: " + updatejson.toString(), 201011141103l);
						}
					} else {
						result.put("message", "please report error 201011141104l");
						FamLog.error("bad request: " + existingUser + " " + updatejson.toString(), 201011141104l);
					}
				} else {
					result.put("message", "please report error 201011141136l");
					FamLog.error("bad request", 201011141136l);
				}
			} else {
				result.put("message", "please report error 201011141105l");
				FamLog.error("bad request", 201011141105l);
			}
		} catch (JSONException e) {
			FamLog.exception(e, 2010111456l);
			try {
				result.put("message", "please report error 2010111456l");
			} catch (JSONException e1) {
				FamLog.exception(e1, 201011141131l);
			}
		}
		try {
			result.put("succ", succ);
			result.put("reopened_account_mail_sent", reopened_account_mail_sent);
		} catch (JSONException e) {
			FamLog.exception(e, 2010111452l);
		}
		return result;
	}

	private boolean sendMailIfAccountReopend(User existingUser, User newuser) {
		boolean result = false;
		Date existingAccountExpired = existingUser.getAccountExpires();
		if (existingAccountExpired != null && existingAccountExpired.before(new Date()) && newuser.getAccountExpires().after(new Date())) {
			OutgoingUserMailBox.sendMail_expiredAccountReopened(newuser);
			result = true;
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201011141106l);
	}

}
