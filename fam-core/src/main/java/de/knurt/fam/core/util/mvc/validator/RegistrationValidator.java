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

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.Department;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.heinzelmann.util.validation.PasswordValidator;

/**
 * validation for a {@link User} registrating to the system via registration
 * form. reject if a user has not put in a required field.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090317 (03/17/2009)
 */
public class RegistrationValidator implements Validator {

	private PasswordValidator passwordValidation;

	@Override
  public boolean supports(Class<?> clazz) {
		return clazz.equals(Registration.class);
	}

	@Override
  public void validate(Object target, Errors errors) {
		Registration registration = (Registration) target;
		User candidate = registration.getUser();
		try {
			if (MandatoryUserFieldValidator.getInstance().isMandatory(candidate, "male")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "male", "page.register.input.male.error.required", "Please specify your gender");
			}
			if (MandatoryUserFieldValidator.getInstance().isMandatory(candidate, "title")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "page.register.input.title.error.required", "Please specify your title");
			}
			if (!MandatoryUserFieldValidator.getInstance().isSufficient(candidate, "birthdate")) {
				errors.rejectValue("birthdate", "page.register.input.birthdate.error.required", "Not a valid birthday.");
			}
			if (MandatoryUserFieldValidator.getInstance().isMandatory(candidate, "fname")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "fname", "page.register.input.fname.error.required", "First Name is required");
			}
			if (MandatoryUserFieldValidator.getInstance().isMandatory(candidate, "intendedResearch")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "intendedResearch", "page.register.input.intendedResearch.error.required", "Intended research project is required");
			}
			if (MandatoryUserFieldValidator.getInstance().isMandatory(candidate, "sname")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "sname", "page.register.input.sname.error.required", "Last Name is required");
			}
			JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "mail", "page.register.input.mail.error.required", "e-mail address is required");
			JmValidationUtils.rejectIfNotEmail(errors, "mail", "page.register.input.mail.error.notvalid", "e-mail address is not valid");
			JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "page.register.input.password.error.required", "Passwords empty or not the same");
			JmValidationUtils.rejectIfPasswordIsUnsafe(errors, "password", "page.register.input.password.error.unsafe", null, "Password is too weak", this.getPasswordValidation());

			if (registration.missedCheckedSoABox()) {
				errors.rejectValue("acceptedStatementOfAgreement", "page.register.input.acceptedStatementOfAgreement.error.required", "Please accept out Terms of Use Agreement");
			}
			if (registration.mailExists()) {
				errors.rejectValue("mail", "page.register.input.mail.error.exists", "Your e-mail address exists already");
			}
			if (registration.getStreet() != null) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "street", "page.errorflag", "1");
			}
			if (!MandatoryUserFieldValidator.getInstance().isSufficient(candidate, "phone1")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone1", "page.errorflag", "1");
			}
			if (!MandatoryUserFieldValidator.getInstance().isSufficient(candidate, "phone2")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone2", "page.errorflag", "1");
			}
			if (MandatoryUserFieldValidator.getInstance().isMandatory(candidate, "company")) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "company", "page.errorflag", "1");
			}
			if (registration.getCountry() != null && registration.getCountry().equals("-1")) {
				errors.rejectValue("country", "page.errorflag", "-1");
			}
			if (registration.getCountry() == null) {
				errors.rejectValue("country", "page.errorflag", "-1");
			}

			if (registration.getDepartmentKey() != null) {
				// ↖ department is asked for
				if (registration.getDepartmentKey().equals("-1")) {
					// ↖ known department not selected
					errors.rejectValue("departmentKey", "page.errorflag", "-1");
				} else if (registration.getDepartmentKey().equals(Department.UNKNOWN_KEY)) {
					// ↖ other department selected
					JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "departmentLabel", "page.errorflag", "Please specify");
				}
			}

			if (registration.getCity() != null) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "city", "page.errorflag", "1");
			}
			if (registration.getZipcode() != null) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "zipcode", "page.errorflag", "1");
			}
			if (registration.getStreetno() != null) {
				JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "streetno", "page.errorflag", "1");
			}
		} catch (InvalidRoleIdException e) {
			FamLog.exception(e, 201011151039l);
			errors.rejectValue("roleid", "page.errorflag", "-1");
		}

		// final error message
		if (errors.getErrorCount() > 0) {
			errors.reject("page.register.input.error", "Please correct your input");
		}
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
