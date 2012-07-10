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

/**
 * A data holder for login inputs
 * @author Daniel Oltmanns
 * @since 0.20090308 (03/08/2009)
 */
public class ForgottenPassword {

    private String mailOrUsername, password;

    /**
     * return true, if requesting this fails.
     * it fails, if the user is unknown or banned.
     * @return true, if requesting this fails.
     */
    public boolean fail() {
        return this.getUser() == null || this.getUser().isExcluded();
    }

    /**
     * return the user matching the login or null, if no user matches.
     * @return the user matching the login or null, if no user matches.
     */
    public User getUser() {
        // XXX use UserFactory here
        User example = new User();
        example.setMail(this.getMailOrUsername());
        User result = FamDaoProxy.getInstance().getUserDao().getOneLike(example);
        if (result == null) {
            example.setMail(null);
            example.setUsername(this.getMailOrUsername());
            result = FamDaoProxy.getInstance().getUserDao().getOneLike(example);
        }
        if (result != null && !result.hasVarifiedActiveAccount()) {
            result = null;
        }
        return result;
    }

    /**
     * set the mailOrUsername input
     * @param mailOrUsername input
     */
    public void setMailOrUsername(String mailOrUsername) {
        this.mailOrUsername = mailOrUsername;
    }

    /**
     * set password input
     * @param password input
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * nothing set here
     */
    public ForgottenPassword() {
    }

    /**
     * @return the mailOrUsername
     */
    public String getMailOrUsername() {
        return mailOrUsername;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
