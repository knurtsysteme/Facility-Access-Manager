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
package de.knurt.fam.core.util.mvc;

import static de.knurt.fam.core.util.mvc.QueryKeys.QUERY_KEY_LOGBOOK;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * resolve redirects.
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/23/2010)
 */
public class RedirectResolver {
	/** one and only instance of AsRedirectResolver */
	private volatile static RedirectResolver me;

	/** construct AsRedirectResolver */
	private RedirectResolver() {
	}

	/**
	 * return the address to contact a specific logbook. this is the relative
	 * address (without "http://...") assuming the main directory of the
	 * project.
	 * 
	 * @param key
	 *            representing a logbook
	 * @return the address to contact a specific logbook.
	 */
	public final static String getLogbookMakePostURLWithQueryString(String key) {
		QueryString qs = new QueryString();
		qs.put(QUERY_KEY_LOGBOOK, key);
		return TemplateHtml.me().getHref("logbookmakepost") + qs;
	}

	/**
	 * return the one and only instance of {@link RedirectResolver}
	 * 
	 * @return the one and only instance of {@link RedirectResolver}
	 */
	public static RedirectResolver getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (RedirectResolver.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new RedirectResolver();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of {@link RedirectResolver}
	 */
	public static RedirectResolver me() {
		return getInstance();
	}

	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) {
		return this.getRedirect("login");
	}

	public ModelAndView home(HttpServletRequest request, HttpServletResponse response) {
		return this.getRedirect("home");
	}

	/**
	 * return a redirect resulting in a client side redirect.
	 */
	public ModelAndView getRedirect(RedirectTarget target) {
		return this.getRedirect(getRedirectTargetToResourceName(target));
	}

	private static String getRedirectTargetToResourceName(RedirectTarget target) {
		switch (target) {
		case REGISTER:
			return "register";
		case LOGIN:
			return "corehome";
		case PUBLIC_HOME:
			return "home";
		case PROTECTED_HOME:
			return "corehome";
		case SYSTEM_FACILITY_AVAILABILITY_OVERVIEW:
			return "systemfacilityavailabilityoverview";
		case ADMIN_JOBS_MANAGER:
			return "jobsmanager";
		case PUBLIC_REGISTER:
			return "register";
		case EDIT_SOA:
			return "editsoa";
		default: // go to public home
			return "home";
		}
	}

	public ModelAndView getRedirect(String resourceName) {
		return this.getRedirect(resourceName, null);
	}

	/**
	 * use {@link #redirect(RedirectTarget, QueryString)} instead
	 */
	@Deprecated
	public static ModelAndView redirect(String resourceName, QueryString queryString) {
		return me().getRedirect(resourceName, queryString);
	}

	/**
	 * use {@link #redirect(RedirectTarget, QueryString)} instead
	 */
	@Deprecated
	public static ModelAndView redirect(String resourceName) {
		return me().getRedirect(resourceName);
	}

	private ModelAndView getRedirect(String resourceName, QueryString queryString) {
		return new ModelAndView("redirect:" + redirectLink(resourceName, queryString));
	}

	/**
	 * redirect link for a resource. same as TemplateHtml#href(resourceName)
	 * 
	 * @param resourceName
	 * @return
	 */
	public static String redirectLink(String resourceName) {
		return TemplateHtml.href(resourceName);
	}

	public static String redirectLink(String resourceName, QueryString queryString) {
		return redirectLink(resourceName) + (queryString == null ? "" : queryString.getAsHtmlLinkHref(false));
	}

	public static void redirectClient(String resourceNameOfRedirect, TemplateResource templateResourceNow, Properties model, QueryString queryString) {
		model.put("url", redirectLink(resourceNameOfRedirect, queryString));
		templateResourceNow.setTemplateFile("page_redirect.html");
	}

	private static void redirectClient(String resourceName, TemplateResource templateResourceNow) {
		templateResourceNow.putWritingResultProperty("url", redirectLink(resourceName));
		templateResourceNow.setTemplateFile("page_redirect.html");
	}

	public static void redirectClient(RedirectTarget target, TemplateResource templateResourceNow) {
		redirectClient(getRedirectTargetToResourceName(target), templateResourceNow);
	}

	public static ModelAndView unknownErrorPage(long l) {
		QueryString queryString = QueryStringFactory.get("id", l + "");
		return redirect("unknownerror", queryString);
	}

	public static ModelAndView redirect(String resourceName, String queryString) {
		String result = redirectLink(resourceName);
		if (queryString != null && queryString.length() > 3 && queryString.startsWith("?")) {
			result += queryString;
		}
		return new ModelAndView("redirect:" + result);
	}

	public static ModelAndView redirect(RedirectTarget target, QueryString queryString) {
		return redirect(getRedirectTargetToResourceName(target), queryString);
	}

	public static ModelAndView redirect(RedirectTarget target) {
		return redirect(target, new QueryString());
	}

}
