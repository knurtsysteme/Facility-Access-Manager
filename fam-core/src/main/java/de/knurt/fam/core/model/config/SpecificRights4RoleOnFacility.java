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

import org.springframework.beans.factory.annotation.Required;

/**
 * a container for rules depending on the role of the user.
 * every user has a role with default rights. if a facility has exceptions
 * from this defaults, it is defined here.
 * @author Daniel Oltmanns
 * @see BookingRule
 * @since 0.20090914
 */
public class SpecificRights4RoleOnFacility {

    private Role role;
    private int[] divestedRights;
    private int[] extendedRights;

    private SpecificRights4RoleOnFacility() {
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * @return the divestRights
     */
    public int[] getDivestedRights() {
        return divestedRights;
    }

    /**
     * @param divestRights the divestRights to set
     */
    @Required
    public void setDivestedRights(int[] divestRights) {
        this.divestedRights = divestRights;
    }

    /**
     * @return the extendedRights
     */
    public int[] getExtendedRights() {
        return extendedRights;
    }

    /**
     * @param extendedRights the extendedRights to set
     */
    @Required
    public void setExtendedRights(int[] extendedRights) {
        this.extendedRights = extendedRights;
    }

    /**
     * @param role the role to set
     */
    @Required
    public void setRole(Role role) {
        this.role = role;
    }


    
}
