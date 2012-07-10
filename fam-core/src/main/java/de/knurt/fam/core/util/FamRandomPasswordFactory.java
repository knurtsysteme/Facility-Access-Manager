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
package de.knurt.fam.core.util;

import de.knurt.heinzelmann.util.auth.RandomPasswordFactory;

/**
 * create new passwords at random for fam. on default ({@link Strength#NORMAL})
 * a string with numbers, small and big letters and special characters are generated.
 * 
 * on {@link Strength#EASY} only big letters and numbers are used where the
 * first half are letters and the second half numbers. to avoid mistakes,
 * "1" and "I" are not used as well as "0" and "O".
 * 
 * the length of the password can be configured separately with {@link #setLength(short)}
 * 
 * @see RandomPasswordFactory
 * @author Daniel Oltmanns
 * @since 1.3.1 (12/03/2010)
 */
public class FamRandomPasswordFactory {

	private short length = 13;
	private Strength strengthUsed = Strength.NORMAL;

	/**
	 * step of strength used for ramdom passwords
	 * 
	 * @author Daniel Oltmanns <info@knurt.de>
	 * @since 25.07.2011
	 * @version 25.07.2011
	 * 
	 */
	public enum Strength {
		EASY, NORMAL
	};

	/**
	 * length of string used for the random password
	 * 
	 * @param length
	 *            length of string used for the password
	 */
	public void setLength(short length) {
		this.length = length;
	}

	/**
	 * used strength
	 * 
	 * @see Strength
	 * @param strengthUsed
	 *            used strength
	 */
	public void setStrengthUsed(Strength strengthUsed) {
		this.strengthUsed = strengthUsed;
	}

	private final char[] bigLetters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	private final char[] numbers = { '2', '3', '4', '5', '6', '7', '8', '9' };

	/**
	 * return a new password at random as configured in
	 * {@link #setLength(short)} and {@link #setStrengthUsed(Strength)}
	 * 
	 * @return a new password at random as configured in
	 *         {@link #setLength(short)} and {@link #setStrengthUsed(Strength)}
	 */
	public String getNew() {
		String result = "";
		if (this.strengthUsed == Strength.NORMAL) {
			result = RandomPasswordFactory.me().getPassword(this.length);
		} else {
			short firstLength = (short) (this.length / 2);
			result = RandomPasswordFactory.me().getPassword(firstLength, this.bigLetters);
			result += RandomPasswordFactory.me().getPassword(this.length - firstLength, this.numbers);
		}
		return result;
	}

	/** one and only instance of FamRandomPasswordFactory */
	private volatile static FamRandomPasswordFactory me;

	/** construct FamRandomPasswordFactory */
	private FamRandomPasswordFactory() {
	}

	/**
	 * return the one and only instance of FamRandomPasswordFactory
	 * 
	 * @return the one and only instance of FamRandomPasswordFactory
	 */
	public static FamRandomPasswordFactory getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (FamRandomPasswordFactory.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new FamRandomPasswordFactory();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of FamRandomPasswordFactory
	 */
	public static FamRandomPasswordFactory me() {
		return getInstance();
	}
}
