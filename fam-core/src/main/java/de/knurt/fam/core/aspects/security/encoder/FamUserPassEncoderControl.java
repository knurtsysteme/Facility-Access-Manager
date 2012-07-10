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

import de.knurt.fam.core.model.persist.User;

/**
 * encode the main password of a user.
 * @author Daniel Oltmanns
 * @since 0.20090409 (04/09/2009)
 */
public class FamUserPassEncoderControl extends FamEncoderControl {

    /** one and only instance of me */
    private volatile static FamUserPassEncoderControl me;

    /** construct me */
    private FamUserPassEncoderControl() {
    }

    /**
     * return the one and only instance of FamUserPassEncoderControl
     * @return the one and only instance of FamUserPassEncoderControl
     */
    public static FamUserPassEncoderControl getInstance() {
        if (me == null) { // no instance so far
            synchronized (FamUserPassEncoderControl.class) {
                if (me == null) { // still no instance so far
                    me = new FamUserPassEncoderControl(); // the one and only
                }
            }
        }
        return me;
    }

    /**
     * encode the password.
     * only encode, if user password is not already encoded!
     * @param user the password shall be encoded.
     * @return the encoded password.
     */
    @Override
    public String encodePassword(User user) {
        String result = user.getPassword();
        if (user.isPasswordEncoded() == false) {
            result = super.encodePassword(user);
        }
        return result;
    }

    /**
     * no salt here.
     * @param user the encoded password is for
     * @return null for not using a salt here
     */
    @Override
    protected Object getSalt(User user) {
        return null;
    }
}
