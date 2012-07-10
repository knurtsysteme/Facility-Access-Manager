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
package de.knurt.fam.core.util.mvc.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import de.knurt.fam.core.util.mvc.ForgottenPassword;

/**
 * Validation for a User
 * 
 * Checks, if a user has put in the required fields
 * 
 * @author Daniel Oltmanns
 * @since 0.20090327 (03/27/2009)
 */
public class ForgottenPasswordValidator implements Validator {

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return clazz.equals(ForgottenPassword.class);
	}

	public void validate(Object target, Errors errors) {
		ForgottenPassword fp = (ForgottenPassword) target;
		if (fp.fail()) {
			errors.reject("page.forgottenpassword.input.unknown", "This is is unknown");
		}
	}
}
