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
package de.knurt.fam.core.persistence.dao.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.config.Department;
import de.knurt.fam.core.model.config.Role;

/**
 * a data holder for all defined roles and specific roles
 * 
 * the roles all have to be injected
 * 
 * @author Daniel Oltmanns
 * @since 0.20090324 (03/24/2009)
 */
public class KnownDepartmentConfigDao {

	private volatile static KnownDepartmentConfigDao me;
	private final List<Department> departments;

	private KnownDepartmentConfigDao(List<Department> departments) {
		this.departments = departments;
	}

	/**
	 * return the one and only instance of RoleConfigDao
	 * 
	 * @return the one and only instance of RoleConfigDao
	 */
	@Required
	public static KnownDepartmentConfigDao getInstance(List<Department> departments) {
		if (me == null) { // no instance so far
			synchronized (KnownDepartmentConfigDao.class) {
				if (me == null) { // still no instance so far
					me = new KnownDepartmentConfigDao(departments); // the one and
					// only
				}
			}
		}
		return me;
	}

	/**
	 * return true, if departements shall be requested. this is, when this been
	 * is configured. (me != null)
	 * 
	 * @return true, when departments shall be requested.
	 */
	public static boolean requestDepartments() {
		return me != null;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public static KnownDepartmentConfigDao getInstance() {
		return me;
	}

	public Role getRole(String key) {
		Role result = null;
		Department d = this.get(key);
		if(d != null) {
			result = d.getRole();
		}
		if (result == null) {
			result = RoleConfigDao.getInstance().getStandard();
		}
		return result;
	}

	public Department get(String key) {
		Department result = null;
		for (Department department : this.getDepartments()) {
			if (department.getKey().equals(key)) {
				result = department;
				break;
			}
		}
		return result;
	}

}
