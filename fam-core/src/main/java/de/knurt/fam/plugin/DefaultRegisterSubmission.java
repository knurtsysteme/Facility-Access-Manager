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
package de.knurt.fam.plugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.util.TemplateConfig;
import de.knurt.heinzelmann.util.CookieUtils;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * default behaviour on sending a successfully validated request from
 * registration page successfully.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/09/2010)
 */
class DefaultRegisterSubmission implements RegisterSubmission {

	/**
	 * set the username and put the user into the database. excluded goes
	 * <code>false</code>, so that the user can directly start to use the
	 * system. the user is forwarded directly into the system.
	 */
	public ModelAndView handle(TemplateResource templateResource, Registration registration, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView result = null;
		User newUser = registration.getUser();
		newUser.setStandardUser();
		newUser.setUniqueUsernameForInsertion();
		newUser.setExcluded(false);
		boolean succ = false;
		try {
			if (FamDaoProxy.getInstance().getUserDao().insert(newUser)) {
				CookieUtils.deleteAll(request, response);
				OutgoingUserMailBox.insert_Registration(newUser);
				result = TemplateConfig.me().getResourceController().handleGetRequests(templateResource, response, request);
				succ = true;
			}
		} catch (Exception e) {
			FamLog.exception(e, 201207021029l);
		}
		if (!succ) {
			// this only happens if email exists
			QueryString queryString = QueryStringFactory.get("recheck", "true");
			queryString.put("emailExists", "true");
			FamLog.info("could not put in user! " + newUser.getMail() + " " + newUser.getFullName(), 201207021017l);
			result = RedirectResolver.redirect(RedirectTarget.PUBLIC_REGISTER, queryString);
		}
		return result;
	}

}
