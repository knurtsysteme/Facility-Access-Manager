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
package de.knurt.fam.template.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamPasswordValidation;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.model.WritingResultProperties;

/**
 * change the password of an user signed up
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (11/27/2010)
 */
class ChangePasswordController {

	public TemplateResource execute(String filename, HttpServletResponse response, HttpServletRequest request) {
		TemplateResource tr = null;
		User auth = SessionAuth.user(request);
		if (auth == null) {
			// someone is calling this with an post request.
			FamLog.error("session time out or url attack!!! " + request.getRemoteAddr(), 201011271207l);
		} else {
			WritingResultProperties writingResults = this.setNewPassword(auth, request);
			return TemplateResource.getTemplateResource(response, request, filename, "changepassword", "html", writingResults);
		}
		return tr;
	}

	private WritingResultProperties setNewPassword(User auth, HttpServletRequest request) {
		WritingResultProperties result = new WritingResultProperties();
		String oldpass = request.getParameter("oldpass");
		if (this.isOldPasswordValid(auth, oldpass)) {
			result.put("wrong_passold", false);
			Registration helper = new Registration();
			helper.setPass1(request.getParameter("pass1"));
			helper.setPass2(request.getParameter("pass2"));
			String newpass = helper.getPassword();
			if (FamPasswordValidation.me().isValid(newpass)) {
				result.put("wrong_passnew", false);
				auth.setCleanPassword(newpass);
				auth.update();
				result.put("updated_succ", true);
			} else {
				result.put("wrong_passnew", true);
				result.put("updated_succ", false);
			}
		} else {
			result.put("wrong_passold", true);
			result.put("updated_succ", false);
		}
		return result;
	}

	private boolean isOldPasswordValid(User auth, String oldpass) {
		User candidate = UserFactory.me().getUserWithUsername(auth.getUsername());
		candidate.setPassword(oldpass);
		return candidate.isAuth();
	}
}