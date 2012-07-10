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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.heinzelmann.util.query.HttpServletRequestConverter;

/**
 * insert a user given as json object
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (11/07/2010)
 */
public class InsertUserController extends JSONController {

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest request, HttpServletResponse response) {
		JSONObject result = new JSONObject();
		boolean succ = false;
		JSONObject user = null;
		try {
			user = HttpServletRequestConverter.me().getJSONObject(request);
		} catch (JSONException e) {
			FamLog.exception("error reading json", e, 201011071024l);
			try {
				result.put("message", "error reading json 201011071024");
			} catch (JSONException e1) {
				FamLog.exception("okay ...", e1, 201011141051l);
			}
		} catch (IOException e) {
			FamLog.exception("error reading request", e, 201011071025l);
			try {
				result.put("message", "error reading request 201011071025");
			} catch (JSONException e1) {
				FamLog.exception("okay ...", e1, 201011141050l);
			}
		}
		if (user != null) {
			User newUser = null;
			try {
				newUser = UserFactory.me().getRegistration(user).getUser();
			} catch (JSONException e) {
				FamLog.exception("error getting user from json", e, 201011071025l);
			}
			if (newUser != null) {
				try {
					try {
						newUser.setStandardUser();
						if (user.get("roleid") != null) {
							// ↖ an explicit role id is given (without a
							// department key) - set this
							newUser.setRoleId(user.getString("roleid"));
						}
						newUser.setUniqueUsernameForInsertion();
						newUser.setExcluded(false); // verify account
						succ = FamDaoProxy.getInstance().getUserDao().insert(newUser);
						if (succ) {
							JSONArray responsibilities = user.getJSONArray("responsibilities");
							List<Facility> facilities = new ArrayList<Facility>(responsibilities.length());
							for (int i = 0; i < responsibilities.length(); i++) {
								Facility f = FacilityConfigDao.facility(responsibilities.getJSONObject(i).getString("value"));
								facilities.add(f);
							}
							succ = FamDaoProxy.facilityDao().updateResponsibility(newUser, facilities);
						}
						if (succ) {
							OutgoingUserMailBox.insert_Registration(newUser);
						}
					} catch (DataIntegrityViolationException e) {
						FamLog.info("put in invalid user", 201011071117l);
						result.put("message", "Error 201011071117! Reason: Invalid user. Original: " + e.getMessage());
					} catch (Exception e) {
						FamLog.exception(e, 201011071211l);
					}
				} catch (JSONException e) {
					FamLog.exception(e, 201011071232l);
				}
			}
		}
		try {
			result.put("succ", succ);
		} catch (JSONException e) {
			FamLog.exception("failure on adding a boolean - strange", e, 201011071119l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201011071059l);
	}

}
