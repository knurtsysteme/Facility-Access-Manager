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
 */package de.knurt.fam.core.aspects.security.auth;

import de.knurt.heinzelmann.util.validation.PasswordValidator;

/**
 * check if a password is valid for fam.
 * 
 * inject this bean to configure:
 * 
 * <pre>
 * &lt;bean id="passwordValidation" class="de.knurt.fam.core.aspects.security.auth.FamPasswordValidation" factory-method="getInstance"&gt;
 *     &lt;property name="minLength" value="8" /&gt;
 *     &lt;property name="maxLength" value="20" /&gt;
 *     &lt;property name="minDigits" value="0" /&gt;
 *     &lt;property name="minUpper" value="0" /&gt;
 *     &lt;property name="minLower" value="0" /&gt;
 *     &lt;property name="minChars" value="0" /&gt;
 *     &lt;property name="minNonChars" value="0" /&gt;
 *     &lt;property name="minSpecial" value="0" /&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.3.0 (11/27/2010)
 * 
 */
public class FamPasswordValidation extends PasswordValidator {
	/** one and only instance of FamPasswordValidation */
	private volatile static FamPasswordValidation me;

	/** construct FamPasswordValidation */
	private FamPasswordValidation() {
		super();
	}

	/**
	 * return the one and only instance of FamPasswordValidation
	 * 
	 * @return the one and only instance of FamPasswordValidation
	 */
	public static FamPasswordValidation getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (FamPasswordValidation.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new FamPasswordValidation();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of FamPasswordValidation
	 */
	public static FamPasswordValidation me() {
		return getInstance();
	}
	
	/** {@inheritDoc} */
	@Override
	public Boolean isValid(Object password) {
		Boolean result = super.isValid(password);
		if(result == true) {
			result = password.toString().replaceAll("[^A-Za-z0-9_\\-\\.\\+,#]", "").length() == password.toString().length();
		}
		return result;
	}
}
