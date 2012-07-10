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
 * password for staying logged in the long run is saved in a cookie.
 * encode this password for a specific user.
 * @author Daniel Oltmanns
 * @since 0.20090409 (04/09/2009)
 */
public class FamCookiePassEncoderControl extends FamEncoderControl {

    /** one and only instance of me */
    private volatile static FamCookiePassEncoderControl me;

    /** construct me */
    private FamCookiePassEncoderControl() {
    }

    /**
     * return the one and only instance of FamCookiePassEncoderControl
     * @return the one and only instance of FamCookiePassEncoderControl
     */
    public static FamCookiePassEncoderControl getInstance() {
        if (me == null) { // no instance so far
            synchronized (FamCookiePassEncoderControl.class) {
                if (me == null) { // still no instance so far
                    me = new FamCookiePassEncoderControl(); // the one and only
                }
            }
        }
        return me;
    }

    /**
     * return salt for encoding password
     * @param user the salt is generated of
     * @return salt for encoding password
     */
    @Override
    protected Object getSalt(User user) {
        return user.getMail();
    }
}
