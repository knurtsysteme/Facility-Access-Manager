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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.UserDao;
import de.knurt.fam.core.model.persist.User;

/**
 * controller for ajax request to get username. print json with the key
 * "username" and unique username as value. request must contain
 * "supposedUsername", created via javascript.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090311 (03/11/2009)
 */
public class RegisterGetUsernameController extends JSONController {

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.logException(this.getClass(), ex, "creating json fails", 200904031036l);
	}

	private String getUsernameFromRequest(HttpServletRequest rq) {
		String supposedUsername = rq.getParameter("supposedUsername");
		// XXX use UserFactory here
		User example = new User();
		example.setUsername(supposedUsername);
		example.setUniqueUsernameForInsertion();
		return example.getUsername();
	}

	/**
	 * return username as key and unique username as value
	 * 
	 * @see UserDao#getUniqueUsername(de.knurt.fam.core.model.persist.User)
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return username as key and unique username as value
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		try {
			result.put("username", this.getUsernameFromRequest(rq));
		} catch (JSONException ex) {
			FamLog.exception(ex, 201204141013l);
		}
		return result;
	}
}
