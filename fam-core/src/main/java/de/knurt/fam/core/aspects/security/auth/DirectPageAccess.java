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

import java.util.Properties;

/**
 * control a single authentication for one single user.
 * 
 * some pages may have one specific user that is authenticated auth of the box.
 * resolve this specific success
 * 
 * @see DirectPageAccessContainer
 * @author Daniel Oltmanns
 * @since 0.20100123 (01/23/2010)
 */
public class DirectPageAccess {

	/** one and only instance of me */
	private volatile static DirectPageAccess me;

	/** construct me */
	private DirectPageAccess() {
	}

	/**
	 * return the one and only instance of me
	 * 
	 * @return the one and only instance of me
	 */
	public static DirectPageAccess getInstance() {
		if (me == null) { // no instance so far
			synchronized (DirectPageAccess.class) {
				if (me == null) { // still no instance so far
					me = new DirectPageAccess(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return true, if the given login has direct access to the page with the
	 * given <code>viewName</code>. otherwise return false
	 * 
	 * @param viewName
	 *            of the page to check
	 * @param username
	 *            of the login to check
	 * @param rawPassword
	 *            of the login to check
	 * @return true, if the given login has direct access to the page with the
	 *         given <code>viewName</code>.
	 */
	public boolean isAuth(String viewName, String username, String rawPassword) {
		Properties accesses = DirectPageAccessContainer.getInstance().getViewNameUserPass();
		boolean result = false;
		if (!accesses.isEmpty() && accesses.containsKey(viewName)) {
			result = accesses.get(viewName).equals(username + ":" + rawPassword);
		}
		return result;
	}
}
