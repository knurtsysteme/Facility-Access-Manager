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
package de.knurt.fam.core.model.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.adapter.ViewableObject;

/**
 * role of a user. this is nothing but a container for rights.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090303
 */
public class Role implements ViewableObject {

	private int[] rights;

	/**
	 * A role of a user. Must always be injected!
	 */
	private Role() {
	}

	/**
	 * @return the rights
	 */
	public int[] getRights() {
		return rights;
	}

	/**
	 * @return the rights
	 */
	public String[] getRightsDescription() {
		String[] result = new String[rights.length];
		int i = 0;
		while (i < rights.length) {
			result[i] = FamText.message("right." + rights[i]);
			i++;
		}
		return result;
	}

	/**
	 * return the key, representing this role.
	 * 
	 * @see RoleConfigDao#getKey(java.lang.Object)
	 * @return the key, representing this role.
	 */
	public String getKey() {
		return RoleConfigDao.getInstance().getKey(this);
	}

	/**
	 * @param rights
	 *            the rights to set
	 */
	@Required
	public void setRights(int[] rights) {
		this.rights = rights;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return FamText.message("role.desc." + this.getKey());
	}

	/**
	 * @return the description
	 */
	public String getLabel() {
		return FamText.message("role.label." + this.getKey());
	}

	/**
	 * return a list of all users having that role
	 * 
	 * @return a list of all users having that role
	 */
	public List<User> getUsers() {
		User example = new User();
		example.setRoleId(this.getKey());
		return FamDaoProxy.userDao().getObjectsLike(example);
	}

}
