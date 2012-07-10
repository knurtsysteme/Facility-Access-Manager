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

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.QueryKeys;

/**
 * reject if there is no {@link User}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090819 (08/19/2009)
 */
public class HasExistingUserValidator extends FamValidator {

	public void validate(Object target, Errors errors) {
		QueryKeys query = (QueryKeys) target;
		if (query.getUser() == null) {
			errors.rejectValue(QueryKeys.QUERY_KEY_USER, "", "");
		}
	}
}
