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

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;

/**
 * encode something.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/21/2010)
 */
public class FamEncoder {

	private MessageDigestPasswordEncoder encoder;

	/**
	 * set the encoder that is used for encoding the password.
	 * 
	 * @param encoder
	 *            used for encoding the password.
	 */
	@Required
	public void setEncoder(MessageDigestPasswordEncoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * return the encoder used for encoding passwords.
	 * 
	 * @return the encoder used for encoding passwords.
	 */
	public MessageDigestPasswordEncoder getEncoder() {
		return encoder;
	}

	/** one and only instance of me */
	private volatile static FamEncoder me;

	/** construct me */
	private FamEncoder() {
	}

	/**
	 * return the one and only instance of FamUserPassEncoderControl
	 * 
	 * @return the one and only instance of FamUserPassEncoderControl
	 */
	public static FamEncoder getInstance() {
		if (me == null) { // no instance so far
			synchronized (FamEncoder.class) {
				if (me == null) { // still no instance so far
					me = new FamEncoder(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * encode the password. only encode, if user password is not already
	 * encoded!
	 * 
	 * @param user
	 *            the password shall be encoded.
	 * @return the encoded password.
	 */
	public String encodeSomething(String something, String salt) {
		return this.encoder.encodePassword(something, salt);
	}

}
