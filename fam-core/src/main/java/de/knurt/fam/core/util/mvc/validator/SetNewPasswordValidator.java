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

import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.heinzelmann.util.validation.PasswordValidator;

/**
 * validation for a {@link User} that set a new password. checks, if a user has
 * put in the required fields
 * 
 * @author Daniel Oltmanns
 * @since 0.20090317 (03/17/2009)
 */
public class SetNewPasswordValidator implements Validator {

	private PasswordValidator passwordValidation;

	public boolean supports(Class<?> clazz) {
		return clazz.equals(Registration.class);
	}

	public void validate(Object target, Errors errors) {
		JmValidationUtils.rejectIfPasswordIsUnsafe(errors, "password", "page.register.input.password.error.unsafe", null, "Password is too weak", this.getPasswordValidation());
	}

	/**
	 * @return the passwordValidation
	 */
	public PasswordValidator getPasswordValidation() {
		return passwordValidation;
	}

	/**
	 * @param passwordValidation
	 *            the passwordValidation to set
	 */
	@Required
	public void setPasswordValidation(PasswordValidator passwordValidation) {
		this.passwordValidation = passwordValidation;
	}
}
