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

import de.knurt.fam.connector.FamConnector;

/**
 * define users for a single authentication.
 * 
 * @see DirectPageAccess
 * @author Daniel Oltmanns
 * @since 0.20100123 (01/23/2010)
 */
public class DirectPageAccessContainer {

	private Properties viewNameUserPass = null;
	/** one and only instance of me */
	private volatile static DirectPageAccessContainer me;

	/** construct me */
	private DirectPageAccessContainer() {
	}

	/**
	 * return the one and only instance of me
	 * 
	 * @return the one and only instance of me
	 */
	public static DirectPageAccessContainer getInstance() {
		if (me == null) { // no instance so far
			synchronized (DirectPageAccessContainer.class) {
				if (me == null) { // still no instance so far
					me = new DirectPageAccessContainer(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return a combination of pages and <code>viewNameUserPass</code>.<br />
	 * e.g. if the home page shall be accessable without any login for a user
	 * foo with the password bar, then define a map:<br />
	 * <code>home => foo:bar</code><br />
	 * the delemiter for user and password is <code>:</code>.
	 * 
	 * changed on 11/10/2011: only statistics are accessable!
	 * 
	 * @return viewNameUserPass a combination of pages and
	 *         <code>viewNameUserPass</code>
	 */
	protected final Properties getViewNameUserPass() {
		if (this.viewNameUserPass == null) {
			this.viewNameUserPass = new Properties();
			String statistics_username = FamConnector.getGlobalProperty("statistics_username");
			String statistics_password = FamConnector.getGlobalProperty("statistics_password");
			if (statistics_password != null && statistics_username != null) {
				this.viewNameUserPass.put("statistics", statistics_username + ":" + statistics_password);
			}
		}
		return this.viewNameUserPass;
	}
}
