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
package de.knurt.fam.core.aspects.security.encoder;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;

/**
 * if user forgot password, a tmp access is created.
 * encode the password for this access and a specific user.
 * @author Daniel Oltmanns
 * @since 0.20090409 (04/09/2009)
 */
public class FamTmpAccessEncoderControl extends FamEncoderControl {

    /** one and only instance of me */
    private volatile static FamTmpAccessEncoderControl me;

    /** construct me */
    private FamTmpAccessEncoderControl() {
    }

    /**
     * return the one and only instance of FamTmpAccessEncoderControl
     * @return the one and only instance of FamTmpAccessEncoderControl
     */
    public static FamTmpAccessEncoderControl getInstance() {
        if (me == null) { // no instance so far
            synchronized (FamTmpAccessEncoderControl.class) {
                if (me == null) { // still no instance so far
                    me = new FamTmpAccessEncoderControl(); // the one and only
                }
            }
        }
        return me;
    }

    /**
     * return true, if given encoded password is valid.
     * the encoded password is part of the url, sent to the user via email.
     * @param encPass the encoded password.
     * @return true, if given encoded password is valid. otherwise false.
     */
    public boolean isPasswordValid(String encPass) {
        return this.getUser(encPass) != null;
    }

    /**
     * return the user with the given encoded password.
     * @param encPass the encoded password.
     * @return user, if given encoded password is valid. otherwise null.
     */
    public User getUser(String encPass) {
        User result = null;
        if (encPass != null) {
            String username = encPass.substring(encPass.lastIndexOf("_") + 1);
            User example = UserFactory.me().blank();
            example.setUsername(username);
            User stored = FamDaoProxy.getInstance().getUserDao().getOneLike(example);
            if (stored != null) { // username exist
                if (this.encodePassword(stored).equals(encPass)) { // code is valid
                    result = stored;
                }
            }
        }
        return result;
    }

    /**
     * encode the password and set the username at the end.
     * @param user the password is for.
     * @return an encoded password for tmp access
     */
    @Override
    public String encodePassword(User user) {
        String result = super.encodePassword(user);
        result += "_" + user.getUsername();
        return result;
    }

    /**
     * return salt for encoding password
     * @param user the password is for
     * @return salt for encoding password
     */
    @Override
    protected Object getSalt(User user) {
        return user.getUsername() + "555" + user.getPassword();
    }
}
