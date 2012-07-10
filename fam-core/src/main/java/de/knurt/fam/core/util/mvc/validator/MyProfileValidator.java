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

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.Registration;

/**
 * validation for an existing {@link User} reworking its profile.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090616 (06/16/2009)
 */
public class MyProfileValidator implements Validator {

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return clazz.equals(Registration.class);
	}

	public void validate(Object target, Errors errors) {
		Registration registration = (Registration) target;
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "male", "page.register.input.male.error.required", "Please specify your gender");
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "page.register.input.title.error.required", "Please specify your title");
		if (registration.getBirtdateAsDate() == null) {
			errors.rejectValue("birthdate", "page.register.input.birthdate.error.required", "Not a valid birthday.");
		}
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "fname", "page.register.input.fname.error.required", "First Name is required");
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "sname", "page.register.input.sname.error.required", "Last Name is required");
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "intendedResearch", "page.register.input.intendedResearch.error.required", "Intended research project is required");
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "mail", "page.register.input.mail.error.required", "e-mail address is required");
		JmValidationUtils.rejectIfNotEmail(errors, "mail", "page.register.input.mail.error.notvalid", "e-mail address is not valid");

		if (registration.mailExists()) {
			errors.rejectValue("mail", "page.register.input.mail.error.exists", "Your e-mail address exists already");
		}
		if (registration.missedCheckedSoABox()) {
			errors.rejectValue("acceptedStatementOfAgreement", "page.register.input.acceptedStatementOfAgreement.error.required", "Please accept out Terms of Use Agreement");
		}
		if (errors.getErrorCount() > 0) {
			errors.reject("page.register.input.error", "Please correct your input");
		}
	}
}
