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

import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;

/**
 * a low end password encoding
 * 
 * @author Daniel Oltmanns
 * @since 0.20090409 (04/09/2009)
 */
public abstract class FamEncoderControl {

	/** construct me */
	protected FamEncoderControl() {
	}


	/**
	 * return the password of the given user encoded. it is not checked, if
	 * user's password is already encoded. the password is encoded with salt.
	 * 
	 * @see #getSalt(de.knurt.fam.core.model.persist.User)
	 * @see User#getPassword()
	 * @param user
	 *            the passworded is encoded of
	 * @return the password of the given user encoded.
	 */
	public String encodePassword(User user) {
		if (user != null) {
			String passwordUsed = user.getPassword().replaceAll("[^(a-zA-Z0-9_\\-\\.\\+,#)]", "");
			if(!passwordUsed.equals(user.getPassword())) {
				FamLog.warn(user.getUsername() + " insert password with not allowed char. password changed", 201207130853l);
			}
			return FamEncoder.getInstance().getEncoder().encodePassword(passwordUsed, this.getSalt(user));
		} else {
			return null;
		}
	}

	/**
	 * return true, if the given password is valid for the given user.
	 * 
	 * @see #isPasswordValid(java.lang.String, java.lang.String,
	 *      java.lang.Object)
	 * @param user
	 *            to check the password from
	 * @param rawPass
	 *            to check for given user
	 * @return true, if the given password is valid for the given user.
	 */
	public boolean isPasswordValid(User user, String rawPass) {
		return this.isPasswordValid(user.getPassword(), rawPass, this.getSalt(user));
	}

	/**
	 * return true, if the given password is valid for the given user.
	 * 
	 * @see MessageDigestPasswordEncoder#isPasswordValid(java.lang.String,
	 *      java.lang.String, java.lang.Object)
	 * @param encPass
	 *            the encoded password
	 * @param rawPass
	 *            to check for given user
	 * @param salt
	 *            used for decoding
	 * @return true, if the given password is valid for the given user.
	 */
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {
		return FamEncoder.getInstance().getEncoder().isPasswordValid(encPass, rawPass, salt);
	}

	/**
	 * return salt for encoding passwords of the given user.
	 * 
	 * @param user
	 *            salt is generated of
	 * @return salt for encoding passwords of the given user.
	 */
	protected abstract Object getSalt(User user);
}
