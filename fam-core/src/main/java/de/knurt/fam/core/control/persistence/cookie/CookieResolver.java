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
package de.knurt.fam.core.control.persistence.cookie;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.knurt.fam.core.aspects.security.encoder.FamCookiePassEncoderControl;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.heinzelmann.util.CookieUtils;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * A simple low end cookie resolver, that sets cookies for one year. The class
 * provides static methods for all cookie interactions of das.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090405 (04/05/2009)
 */
public class CookieResolver {

	/** one and only instance of me */
	private volatile static CookieResolver me;

	/** construct me */
	private CookieResolver() {
	}

	/**
	 * return the one and only instance of CookieResolver
	 * 
	 * @return the one and only instance of CookieResolver
	 */
	public static CookieResolver getInstance() {
		if (me == null) { // no instance so far
			synchronized (CookieResolver.class) {
				if (me == null) { // still no instance so far
					me = new CookieResolver(); // the one and only
				}
			}
		}
		return me;
	}

	private final static String REM_ME_HASH = "a";
	private final static String REM_ME_KEY = "b";
	private final static String TEMPLATE_RESOURCE_NAME = "c";
	private final static String QUERY_STRING = "d";
	private final static int ONE_HOUR = 60 * 60;
	private final static int ONE_YEAR = ONE_HOUR * 24 * 365;

	public void addTemplateResourceAfterLogin(HttpServletResponse response, HttpServletRequest request, TemplateResource templateResource) {
		if (templateResource != null && templateResource.getName() != null) {
			Cookie c = new Cookie(TEMPLATE_RESOURCE_NAME, templateResource.getName());
			c.setMaxAge(ONE_HOUR);
			response.addCookie(c);

			Cookie d;
			if (request.getMethod().equals("GET")) {
				d = new Cookie(QUERY_STRING, QueryStringFactory.getInstance().get(request).toString());
			} else {
				d = new Cookie(QUERY_STRING, "");
			}
			d.setMaxAge(ONE_HOUR);
			response.addCookie(d);
		}
	}

	public String getTemplateResourceName(HttpServletRequest request) {
		return CookieUtils.getCookieValue(request, TEMPLATE_RESOURCE_NAME);
	}

	public String getQueryString(HttpServletRequest request) {
		return CookieUtils.getCookieValue(request, QUERY_STRING);
	}

	/**
	 * add a cookie for "remember me" option
	 * 
	 * @param response
	 *            the cookie is added to
	 * @param user
	 *            added to the cookie
	 */
	public void addCookieRememberMe(HttpServletResponse response, User user) {
		addCookie(response, REM_ME_HASH, FamCookiePassEncoderControl.getInstance().encodePassword(user));
		addCookie(response, REM_ME_KEY, user.getUsername());
	}

	/**
	 * return true, if cookies are active. otherwise false.
	 * 
	 * @param request
	 *            given
	 * @return true, if cookies are active. otherwise false.
	 */
	public static boolean cookiesAreActive(HttpServletRequest request) {
		return request.getCookies() != null;
	}

	/**
	 * return the user saved in cookie or null, if no user is saved
	 * 
	 * @param rq
	 *            the user are saved in
	 * @return user saved in cookie or null, if no user is saved
	 */
	public User getUser(HttpServletRequest rq) {
		User result = null;
		User found = null;
		// search a user with username in cookie
		String username = CookieUtils.getCookieValue(rq, REM_ME_KEY);
		if (username != null && username.equals("") == false) { // username set
			// in cookie
			String passEncrypted = CookieUtils.getCookieValue(rq, REM_ME_HASH);
			if (passEncrypted != null && passEncrypted.equals("") == false) { // pass
				// set
				// in
				// cookie
				User candidate = UserFactory.me().blank();
				candidate.setUsername(username);
				found = FamDaoProxy.getInstance().getUserDao().getOneLike(candidate);
				// check, if found cookie-user is auth
				String cookieValueShouldBe = FamCookiePassEncoderControl.getInstance().encodePassword(found);
				if (found != null && cookieValueShouldBe.equals(passEncrypted)) { // its
					// the
					// right
					// pass
					result = found;
				}
			}
		}
		return result;
	}

	private static void addCookie(HttpServletResponse response, String key, String value) {
		Cookie c = new Cookie(key, value);
		c.setMaxAge(ONE_YEAR);
		response.addCookie(c);
	}
}
