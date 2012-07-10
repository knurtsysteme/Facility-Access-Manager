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

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;

/**
 * A data holder for login inputs
 * 
 * @author Daniel Oltmanns
 * @since 0.20090308 (03/08/2009)
 */
public class Login {

	private String username, password, rememberme;
	private User user;

	/**
	 * return the user matching the login or null, if no user matches.
	 * 
	 * @return the user matching the login or null, if no user matches.
	 */
	public User getUser() {
		if (this.user == null) {
			User candidate = UserFactory.me().getUserWithUsername(this.username);
			candidate.setPassword(this.password);
			if (candidate.isAuth()) {
				this.user = FamDaoProxy.userDao().getUserFromUsername(this.username);
			}
		}
		return this.user;
	}

	/**
	 * set the mailOrUsername input
	 * 
	 * @param mailOrUsername
	 *            input
	 */
	public void setUsername(String username) {
		this.username = username.matches("\\W*") ? null : username;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * input of remember me
	 * 
	 * @return input of remember me
	 */
	public String getRememberme() {
		return rememberme;
	}

	/**
	 * set input of input remember me
	 * 
	 * @param rememberme
	 *            input
	 */
	public void setRememberme(String rememberme) {
		this.rememberme = rememberme;
	}

	/**
	 * return true, if the user wants to be remembered
	 * 
	 * @return true, if the user wants to be remembered
	 */
	public boolean userWantsRememberMeCookie() {
		return this.getRememberme() != null && this.getRememberme().equals("") == false;
	}

	/**
	 * return true, if login fails. otherwise true login fails, if there is no
	 * user with given password and given username or email address.
	 * 
	 * @return true, if login fails. otherwise true;
	 */
	public boolean fail() {
		return this.getUser() == null || !this.getUser().hasVarifiedActiveAccount();
	}

	/**
	 * set password input
	 * 
	 * @param password
	 *            input
	 */
	public void setPassword(String password) {
		this.password = password.matches("\\W*") ? "" : password;
	}

	/**
	 * nothing set here
	 */
	public Login() {
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}
