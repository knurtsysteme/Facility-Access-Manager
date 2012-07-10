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
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.JSONFactory;

/**
 * return a user with <code>user_id</code>
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (11/13/2010)
 * @version 1.3.0 (11/13/2010)
 */
public class GetUserController extends JSONController {

	/**
	 * return an available possible booking.
	 * 
	 * there are two different places where this is called: 1. the
	 * "request booking" link in the navigation of the calendar 2. the drag and
	 * drop action in same calender
	 * 
	 * the response depends on which place causes the request.
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return an available possible booking.
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		try {
			result.put("succ", false);
			int userId = Integer.parseInt(rq.getParameter("user_id"));
			User user = FamDaoProxy.userDao().getUserWithId(userId);
			result.put("user", JSONFactory.me().getUser(user));
			result.put("succ", true);
		} catch (NullPointerException e) {
			FamLog.exception(e, 201011131147l);
		} catch (NumberFormatException e) {
			FamLog.exception(e, 201011131148l);
		} catch (JSONException e) {
			FamLog.exception(e, 201011131149l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201011131144l);
	}
}
