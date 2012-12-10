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
import de.knurt.fam.core.util.mvc.Login;

/**
 * validation for a {@link User} logging in. Reject if the login fails.
 * 
 * @see Login#fail()
 * @author Daniel Oltmanns
 * @since 0.20090327 (03/27/2009)
 */
public class LoginValidator implements Validator {

	public boolean supports(Class<?> clazz) {
		return clazz.equals(Login.class);
	}

	public void validate(Object target, Errors errors) {
		Login login = (Login) target;
		if (login.fail()) {
			errors.reject("page.login.input.notauth", "<span style=\"font-weight:bold;\">Login failed.</span><br /><span>Either your username or password is wrong or your account is not active yet. If you just have registered, please be patient until we have activated your account.</span>"); // INTLANG
		}
	}
}
