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
package de.knurt.fam.core.control.persistence.dao.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.model.config.SpecificRights4RoleOnFacility;
import de.knurt.fam.core.model.persist.User;

/**
 * a data holder for all defined roles and specific roles
 * 
 * the roles all have to be injected
 * 
 * @author Daniel Oltmanns
 * @since 0.20090324 (03/24/2009)
 */
public class RoleConfigDao extends AbstractConfigDao<Role> {

	private volatile static RoleConfigDao me;
	private Map<String, Role> configuredInstances;

	/** {@inheritDoc} */
	@Override
	protected Map<String, Role> getConfiguredInstances() {
		return this.configuredInstances;
	}

	/**
	 * return true, if the given roleId exists
	 * 
	 * @param roleId
	 *            to check
	 * @return true, if the given roleId exists
	 */
	public boolean roleIdExists(String roleId) {
		return this.configuredInstances.containsKey(roleId);
	}

	private String standardId, adminId;
	private String[] usernamesOfAdmins;

	private RoleConfigDao() {
	}

	/**
	 * return the key representing the admin role. this is usualy "admin".
	 * 
	 * @return the key representing the admin role.
	 */
	public String getAdminId() {
		return adminId;
	}

	/**
	 * return the one and only instance of RoleConfigDao
	 * 
	 * @return the one and only instance of RoleConfigDao
	 */
	public static RoleConfigDao getInstance() {
		if (me == null) { // no instance so far
			synchronized (RoleConfigDao.class) {
				if (me == null) { // still no instance so far
					me = new RoleConfigDao(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return all available roleIds
	 * 
	 * @see User#roleId
	 * @return all available roleIds
	 */
	public Set<String> getRoleIds() {
		return this.configuredInstances.keySet();
	}

	/**
	 * return true, if the user have a special right on a specific facility.
	 * 
	 * @param user
	 *            which right is checked
	 * @param forwhat
	 *            one of defined rights
	 * @param onFacility
	 *            {@link Facility} the user may have extended or divested rights.
	 *            if facility is not bookable, all child facilities are checked. if
	 *            there is only one child facility, that has the requested right,
	 *            return true. if there is not even one facility, return false.
	 * @see SpecificRights4RoleOnFacility
	 * @return true, if the user have a special right
	 * @see FamAuth
	 */
	public boolean hasRight(User user, int forwhat, Facility onFacility) {
		boolean result = false;
		Role userRole = this.configuredInstances.get(user.getRoleId());
		if (userRole != null) {
			// check right in general
			for (int right : userRole.getRights()) {
				if (right == forwhat) {
					result = true;
					break;
				}
			}
			// check right on facility
			if (onFacility != null) {
				if (onFacility.isBookable()) {
					SpecificRights4RoleOnFacility toCheck = ((FacilityBookable) onFacility).getBookingRule().getSpecificRights4UserOnFacility(user);
					if (toCheck != null) { // special rules exist
						if (result == true) { // user has right in general ...
							for (int divestedRight : toCheck.getDivestedRights()) {
								if (divestedRight == forwhat) {
									result = false; // ... but not on this
									// facility
								}
							}
						} else { // user does not have right in general ...
							for (int extendedRight : toCheck.getExtendedRights()) {
								if (extendedRight == forwhat) {
									result = true;
									// ... but does have it on this facility
								}
							}
						}
					}
				} else {
					// ↖ a none bookable facility
					List<Facility> children = FacilityConfigDao.getInstance().getChildrenFacilities(onFacility);
					result = false;
					for (Facility child : children) {
						if (this.hasRight(user, forwhat, child)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param configuredInstances
	 *            to set
	 */
	@Required
	@Override
	public void setConfiguredInstances(Map<String, Role> configuredInstances) {
		this.configuredInstances = configuredInstances;
	}

	/**
	 * @return the standardId
	 */
	public String getStandardId() {
		return standardId;
	}

	/**
	 * return roleId of the given user without a roleId by now. return the
	 * adminId, if the username is defined as admin. otherwise return the
	 * stadardId. does not return any other ids but admin and standard, even if
	 * given user has a third id already.
	 * 
	 * @param user
	 *            without a role id where the roleId shall be returned
	 * @return the roleId of the given user without a roleId by now
	 */
	public String getRoleId(User user) {
		String result = this.standardId;
		for (String username : this.usernamesOfAdmins) {
			if (username.equals(user.getUsername())) {
				result = this.adminId;
				break;
			}
		}
		return result;
	}

	/**
	 * return true, if the user is an admin.<br />
	 * the user is admin if:<br />
	 * 1. his role equals the admin id<br />
	 * 2. he is not excluded from the system<br />
	 * 3. his username is set as admin
	 * 
	 * @param user
	 *            to check
	 * @return true, if the user is an admin.
	 */
	public boolean isAdmin(User user) {
		boolean result = false;
		if (user.getRoleId() != null && user.getUsername() != null) {
			if (user.isExcluded() == null || user.isExcluded() == false) {
				if (user.getRoleId().equals(this.getAdminId())) {
					for (String admin : this.usernamesOfAdmins) {
						if (admin.equals(user.getUsername())) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	private List<User> admins = null;

	/**
	 * return all admins of the system
	 * 
	 * @return all admins of the system
	 */
	public List<User> getAdmins() {
		if (this.admins == null) {
			this.admins = new ArrayList<User>();
			for (String username : this.usernamesOfAdmins) {
				User admin = FamDaoProxy.userDao().getUserFromUsername(username);
				if (admin != null && !admin.isExcluded()) { // admin has logged
					// in and has active
					// account
					this.admins.add(admin);
				}
			}
		}
		return this.admins;
	}

	/**
	 * @param standardId
	 *            the standardId to set
	 */
	@Required
	public void setStandardId(String standardId) {
		this.standardId = standardId;
	}

	/**
	 * @param usernamesOfAdmins
	 *            the usernamesOfAdmins to set
	 */
	@Required
	public void setUsernamesOfAdmins(String[] usernamesOfAdmins) {
		this.usernamesOfAdmins = usernamesOfAdmins;
	}

	/**
	 * return usernames representing all admins.
	 * 
	 * @return usernames representing all admins.
	 */
	public String[] getUsernamesOfAdmins() {
		return usernamesOfAdmins;
	}

	/**
	 * 
	 * @param adminId
	 *            the adminId to set
	 */
	@Required
	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	/**
	 * return the role for the given id
	 * 
	 * @param roleId
	 *            the role is returned of
	 * @see #getConfiguredInstance(java.lang.String)
	 * @return the role for the given id
	 */
	public Role getRole(String roleId) {
		return this.getConfiguredInstance(roleId);
	}

	public boolean isAdmin(String role) {
		return role.equals(this.getAdminId());
	}

	public Role getStandard() {
		return this.getRole(this.getStandardId());
	}
}
