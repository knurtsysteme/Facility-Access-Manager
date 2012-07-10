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

import java.util.List;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.UserDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.auth.Authenticatable;
import de.knurt.heinzelmann.util.auth.UserAuthentication;

/**
 * authentificate the candidate with the internal database.
 * this authentificates uses
 * @see User
 * @author Daniel Oltmanns
 * @since 0.20090303 (03/03/2009)
 */
public class StandardAuthentication implements UserAuthentication {

    /**
     * return true, if candidate is stored in the database.
     * @see User#isExcluded() 
     * @see UserDao#getOneLike(de.knurt.fam.core.model.persist.Storeable)
     * @param candidate must be a user, that is authenticatable.
     *  this user must have a password and a username or e-mail address.
     * @return true, if given candidate is auth
     */
    public boolean isAuth(Authenticatable candidate, String cleanPass) {
        return this.isAuth((User) candidate, cleanPass);
    }

    /**
     * return true, if candidate is stored in the database.
     * @see User#isExcluded()
     * @see UserDao#getOneLike(de.knurt.fam.core.model.persist.Storeable)
     * @param candidate must be a user, that is authenticatable.
     *  this user must have a password and a username or e-mail address.
     * @param cleanPass not encoded password
     * @return true, if given candidate is auth
     */
    public boolean isAuth(User candidate, String cleanPass) {
        boolean result = false;
        if (this.isAuthenticatable(candidate)) {
            List<User> users = FamDaoProxy.userDao().getObjectsLike(candidate);
            result = users.size() == 1;
            if (result) {
                result = users.get(0).hasVarifiedActiveAccount();
            }
        }
        return result;
    }
    /** one and only instance of me */
    private volatile static StandardAuthentication me;

    /** construct me */
    private StandardAuthentication() {
    }
    public static StandardAuthentication me() {
    	return getInstance();
    }

    /**
     * return the one and only instance of StandardAuthentication
     * @return the one and only instance of StandardAuthentication
     */
    public static StandardAuthentication getInstance() {
        if (me == null) { // no instance so far
            synchronized (StandardAuthentication.class) {
                if (me == null) { // still no instance so far
                    me = new StandardAuthentication(); // the one and only
                }
            }
        }
        return me;
    }

    private boolean isAuthenticatable(User candidate) {
        return 	candidate.getPassword() != null &&
                candidate.getPassword().equals("") == false &&
                candidate.getUsername() != null &&
                candidate.getUsername().equals("") == false;
    }
}

