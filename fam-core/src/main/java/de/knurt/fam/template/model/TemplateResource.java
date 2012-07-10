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
package de.knurt.fam.template.model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.cookie.CookieResolver;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.FileUploadController;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.util.TemplateConfig;
import de.knurt.heinzelmann.util.CookieUtils;

/**
 * a bean haveing a name a filename and a suffix for eache resource. this
 * depends on how urlrewrite is mapping it. as a default, mapping works:
 * 
 * <pre>
 * http://www.my-address.com/[name]/[filename].[suffix]
 * </pre>
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/06/2010)
 */
public class TemplateResource {

	public static enum Visibility {
		PUBLIC, PROTECTED, ADMIN
	};

	private TemplateResource(HttpServletRequest request, WritingResultProperties writingResultProperties) {
		this.request = request;
		this.writingResultProperties = writingResultProperties;
	}

	private String name, filename, suffix, templateFile = "page.html";
	private User authUser;
	private boolean invalidSession = false;
	private boolean invalidAuth = false;
	private boolean lostSession = false;
	private boolean loggedOut = false;
	private boolean unknownUser = false;
	private boolean accountExpired = false;
	private final HttpServletRequest request;
	private WritingResultProperties writingResultProperties;

	public SessionAuth getSession() {
		return SessionAuth.getInstance(this.request);
	}

	public String getTemplateFile() {
		return templateFile;
	}

	public WritingResultProperties getWritingResultProperties() {
		return writingResultProperties;
	}

	/**
	 * return true, if the session is set to invalid.
	 * this is cause of missing inputs - first of all accepting the terms.
	 * @return true, if the session is set to invalid.
	 */
	public boolean isInvalidSession() {
		return invalidSession;
	}

	public boolean isUnknownUser() {
		return unknownUser;
	}

	public boolean isAccountExpired() {
		return accountExpired;
	}

	public boolean isInvalidAuth() {
		return invalidAuth;
	}

	public boolean isLostSession() {
		return lostSession;
	}

	public boolean isLoggedOut() {
		return loggedOut;
	}

	public User getAuthUser() {
		return authUser;
	}

	/**
	 * return the visibility of the page on content pages. for other pages (like
	 * for images, css or js files) return {@link Visibility#PUBLIC}.
	 * 
	 * @see Visibility
	 * @see #isRequestForContent()
	 * @return the visibility of the page on content pages.
	 */
	public Visibility getVisibility() {
		Visibility result = Visibility.PUBLIC;
		if (this.isRequestForContent()) {
			// it's a request for html or json
			String v = TemplateConfig.me().getContentProperties().getCustomConfigPage(this.name).getAttributeValue("visibility");
			if (v.equals("admin")) {
				result = Visibility.ADMIN;
			} else if (v.equals("protected")) {
				result = Visibility.PROTECTED;
			} else { // if (v.equals("public")) {
				result = Visibility.PUBLIC;
			}
		}
		return result;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public String getName() {
		return name;
	}

	public String getFilename() {
		return filename;
	}

	public String getSuffix() {
		return suffix;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return String.format("name: %s, suffix: %s, filename: %s, visibility: %s", this.getName(), this.getSuffix(), this.getFilename(), this.getVisibility());
	}

	public static TemplateResource getTemplateResource(HttpServletResponse response, HttpServletRequest request, String filename, String resourceName, String suffix) {
		return getTemplateResource(response, request, filename, resourceName, suffix, null);
	}

	private static void setCookiePageAfterLogin(TemplateResource tr, HttpServletResponse response, HttpServletRequest request) {
		if (!tr.name.equals("logout")) {
			CookieResolver.getInstance().addTemplateResourceAfterLogin(response, request, tr);
		}
	}

	private static void killCookiesOnLogoutPage(TemplateResource tr, HttpServletResponse response, HttpServletRequest request) {
		if (tr.name.equals("logout")) {
			CookieUtils.deleteAll(request, response);
		}
	}

	public static TemplateResource getTemplateResource(HttpServletResponse response, HttpServletRequest request, String filename, String resourceName, String suffix, WritingResultProperties writingResultProperties) {
		TemplateResource result = new TemplateResource(request, writingResultProperties);
		// set basics template resource
		result.authUser = SessionAuth.user(request);
		result.filename = filename;
		result.name = resourceName;
		result.suffix = suffix;

		// set basics session vars
		boolean hasAuthUser = SessionAuth.authUser(request);
		User authUser = hasAuthUser ? SessionAuth.user(request) : null;

		// reload the configuration if you have an auth user and a reload of it
		// is requested
		if (result.configurationReloadIsRequested()) {
			TemplateConfig.me().getContentProperties().reload();
			FileUploadController.reinitOptions();
		}

		// correct only on html stuff
		if (result.suffix.equals("html")) {
			// set invalid session
			if (!hasAuthUser || !authUser.hasRight2ViewPage(result.name) || ((authUser.isAcceptedStatementOfAgreement() != true || authUser.hasUnsufficientContactDetails()) && !authUser.isAdmin())) {
				result.invalidSession = true;
			}
			killCookiesOnLogoutPage(result, response, request);

			// correct vars on specific pages
			try {
				// check auth
				switch (result.getVisibility()) {
				case PROTECTED:
					if (!result.name.equals("login") && !hasAuthUser && !RequestInterpreter.containsDirectAccess(resourceName, request)) {
						// ↖ login failed
						setCookiePageAfterLogin(result, response, request);
						result.invalidAuth = request.getMethod().equals("POST") && result.name.equals("corehome");
						result.loggedOut = result.name.equals("logout");
						result.lostSession = !result.name.equals("corehome") && !result.name.equals("login");
						result.name = "login";
					}
					break;
				case ADMIN:
					if (result.name.equals("admin") == false && (!hasAuthUser || !authUser.hasAdminTasks()) && !RequestInterpreter.containsDirectAccess(resourceName, request)) {
						// ↖ login failed
						setCookiePageAfterLogin(result, response, request);
						result.invalidAuth = request.getMethod().equals("POST");
						result.lostSession = !result.invalidAuth;
						result.loggedOut = result.name.equals("logout");
						result.name = "admin";
						result.invalidSession = true;
					}
					break;
				}

				// more detailed message on invalid auth - ticket #262
				if (result.invalidAuth) {
					String username = request.getParameter("username");
					User user = null;
					if (username != null) {
						user = FamDaoProxy.userDao().getUserFromUsername(username);
					}
					;
					result.unknownUser = user == null;
					if (!result.unknownUser) {
						result.accountExpired = user.isAccountExpired();
					}
				}

				// set a redirect in case of invalid session
				if (hasAuthUser && authUser.isAcceptedStatementOfAgreement() != true) {
					result.name = "termsofuse";
				} else if (hasAuthUser && authUser.hasUnsufficientContactDetails()) {
					result.name = "contactdetails";
				} else if (hasAuthUser && result.invalidSession) {
					result.name = "login";
				}

				// correct pages not shown when user is auth ...
				if (hasAuthUser && authUser.isAdmin()) {
					String[] doNotShowPages = { "admin" };
					// do not show this pages
					// when someone has admin
					// auth
					for (String doNotShowPage : doNotShowPages) {
						if (result.name.equals(doNotShowPage)) {
							result.name = "adminhome";
							break;
						}
					}
				}
				if (hasAuthUser) {
					String[] doNotShowPages = { "register", "registersent", "login" };
					// do not show this pages when someone has protected auth
					for (String doNotShowPage : doNotShowPages) {
						if (result.name.equals(doNotShowPage)) {
							result.name = "corehome";
							break;
						}
					}
				}

			} catch (NullPointerException e) {
				// ↖ url hack
				result.name = "home";
			}
		}
		if (result.newsReloadIsRequested()) {
			result.getSession().setNewsItems(null);
		}
		return result;
	}

	public void setTemplateFile(String templateFile) {
		this.templateFile = templateFile;
	}

	public boolean hasAuthUser() {
		return this.getAuthUser() != null;
	}

	public void putWritingResultProperty(Object key, Object value) {
		if (writingResultProperties == null) {
			writingResultProperties = new WritingResultProperties();
		}
		writingResultProperties.put(key, value);
	}

	/**
	 * return true, if this is a request for content.
	 * 
	 * @return true, if this is a request for content.
	 */
	public boolean isRequestForContent() {
		return this.suffix.equals("html") || this.suffix.equals("json");
	}

	public boolean configurationReloadIsRequested() {
		return this.hasAuthUser() && request.getParameter("reload") != null && request.getParameter("reload").equals("config");
	}

	/**
	 * return true, if the minute in the cookie has a different of five to the
	 * minute now. this is unexactly in case of reloading page in 60 minutes
	 * again, but exactly enough for this.
	 */
	private boolean newsReloadIsRequested() {
		boolean result = false;
		int minuteLastUpdate = -1;
		if (this.hasAuthUser()) {
			result = this.name.equals("corehome");
			if (!result) {
				try {
					minuteLastUpdate = Integer.parseInt(CookieUtils.getCookieValue(this.request, "LastNewsUpdate"));
				} catch (Exception e) {
					// NOP or NumberFormatException or ???Exception means:
					// there is no cookie value!
					result = true;
				}
			}
			if (!result) {
				try {
					int minuteNow = Integer.parseInt(FamDateFormat.getCustomDate("m"));
					result = Math.abs(minuteNow - minuteLastUpdate) >= 5;
				} catch (NumberFormatException e) {
					FamLog.exception(e, 201204270903l);
				}
			}
		}
		return result;
	}

}
