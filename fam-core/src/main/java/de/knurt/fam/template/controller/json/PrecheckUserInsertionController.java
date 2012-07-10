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
import java.security.InvalidParameterException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.heinzelmann.util.query.HttpServletRequestConverter;

/**
 * precheck, if user exists.
 * 
 * @author Daniel Oltmanns
 * @since 1.5.3 (02/11/2011)
 */
public class PrecheckUserInsertionController extends JSONController {

	/**
	 * simply return false, because of a user can always be inserted if the
	 * email does not exists (which is a seperate check). this method is to
	 * override to implement other rules,
	 */
	public boolean userExists(JSONObject user) {
		return false;
	}

	private boolean emailExists(JSONObject user) {
		boolean result = false;
		try {
			result = FamDaoProxy.userDao().getUsersWithEMail(user.getString("mail")).size() > 0;
		} catch (JSONException e) {
			FamLog.exception("error reading json", e, 201111020943l);
			result = true;
		} catch (InvalidParameterException e) {
			FamLog.exception("sql injection " + user.toString(), e, 201204260846l);
			result = true;
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest request, HttpServletResponse response) {
		JSONObject result = new JSONObject();
		JSONObject user = null;
		try {
			boolean insertionImpossible = false;
			user = HttpServletRequestConverter.me().getJSONObject(request);
			result.put("user_exists", this.userExists(user));
			if (this.emailExists(user)) {
				insertionImpossible = true;
				result.put("insertion_impossible_message", this.getInsertionImpossibleMessage());
			}
			result.put("insert_anyway_message", this.getInsertAnywayMessage());
			result.put("insertion_impossible", insertionImpossible);
		} catch (JSONException e) {
			FamLog.exception("error reading json", e, 201111020915l);
			try {
				result.put("message", "error reading json 201111020914");
			} catch (JSONException e1) {
				FamLog.exception("okay ...", e1, 201111020913l);
			}
		} catch (IOException e) {
			FamLog.exception("error reading request", e, 201111020912l);
			try {
				result.put("message", "error reading request 201111020911");
			} catch (JSONException e1) {
				FamLog.exception("okay ...", e1, 201111020910l);
			}
		}
		return result;
	}

	private String getInsertionImpossibleMessage() {
		return "Not possible to insert User. Email invalid or may already exist.";
	}

	protected String getInsertAnywayMessage() {
		return "User with Firstname and Lastname already exists! Insert anyway?";
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201111020909l);
	}

}
