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
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.FamRandomPasswordFactory;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;

/**
 * set the user a new password
 * 
 * @author Daniel Oltmanns
 * @since 1.3.1 (12/03/2010)
 */
public class AdminInitPasswordController extends JSONController {

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		try {
			result.put("succ", false);
			int userId = Integer.parseInt(rq.getParameter("user_id"));
			User user = FamDaoProxy.userDao().getUserWithId(userId);
			if (user == null) {
				result.put("message", "user with id " + userId + " not found");
			} else {
				String newpass = FamRandomPasswordFactory.me().getNew();
				user.setCleanPassword(newpass);
				user.update();
				UserMail mail = OutgoingUserMailBox.sendMail_adminInitPassword(user, newpass);
				if (mail.getWasSentDate() == null) {
					result.put("message", "password set successfully to " + newpass + " but sending email failed for some reason");
				} else {
					result.put("message", "email with new password has been sent on " + mail.getWasSentDate());
					result.put("succ", true);
				}
			}
		} catch (NullPointerException e) {
			FamLog.exception(e, 201012031040l);
		} catch (NumberFormatException e) {
			FamLog.exception(e, 201012031041l);
		} catch (JSONException e) {
			FamLog.exception(e, 201012031042l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201012031043l);
	}
}
