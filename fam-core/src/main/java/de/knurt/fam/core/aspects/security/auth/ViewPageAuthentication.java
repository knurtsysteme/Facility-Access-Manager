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
package de.knurt.fam.core.aspects.security.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.persist.User;

/**
 * this authenticates the user against a viewName.
 * 
 * all views with restricted access are configured here with a list of rights,
 * the user need (or at least one of it).
 * 
 * user, that are excluded, did not get the right to any page. administrators
 * have unrestricted access.
 * 
 * WARNING: if the viewName is not listed here, the view is without any
 * restriction and can be seen by everyone (as far as there is no restriction at
 * another place, namely the base authentication)!
 * 
 * @author Daniel Oltmanns
 * @since 0.20090821 (08/21/2009)
 */
public class ViewPageAuthentication {

	/** one and only instance of me */
	private volatile static ViewPageAuthentication me;

	/** construct me */
	private ViewPageAuthentication() {
	}

	private Map<String, int[]> needOneOf;

	/**
	 * return the one and only instance of ViewPageAuthentication
	 * 
	 * @return the one and only instance of ViewPageAuthentication
	 */
	public static ViewPageAuthentication getInstance() {
		if (me == null) { // no instance so far
			synchronized (ViewPageAuthentication.class) {
				if (me == null) { // still no instance so far
					me = new ViewPageAuthentication(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return true, if the given user is allowed to view the page with the given
	 * viewName.
	 * 
	 * @param user
	 *            being checked
	 * @param viewName
	 *            check, if the user has the right to view this
	 * @return true, if the given user is allowed to view the page with the
	 *         given viewName.
	 */
	public static boolean hasIt(User user, String viewName) {
		return getInstance().hasItIntern(user, viewName);
	}

	/**
	 * set the map mapping viewnames to rights. each view name gets a list of
	 * righst, where the user must have one of the get the view authentication.
	 * 
	 * @param needOneOf
	 *            map mapping viewnames to rights.
	 */
	@Required
	public void setNeedOneOf(Map<String, int[]> needOneOf) {
		this.needOneOf = needOneOf;
	}

	private boolean hasItIntern(User user, String viewName) {
		boolean result = user.hasVarifiedActiveAccount();
		if (result) {
			result = user.isAdmin();
			if (!result) {
				int[] needOneOfRights = this.needOneOf.get(viewName);
				if (needOneOfRights == null) { // unknown page - allow it!
					result = true;
				} else {
					for (int right : needOneOfRights) {
						if (user.hasRight(right, null)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}
}
