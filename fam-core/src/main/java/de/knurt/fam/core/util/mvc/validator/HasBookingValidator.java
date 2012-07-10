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

import de.knurt.fam.core.util.mvc.QueryKeys;

/**
 * reject if not a true false value with time.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090815 (08/15/2009)
 */
public class HasBookingValidator extends FamValidator {

	public void validate(Object target, Errors errors) {
		QueryKeys query = (QueryKeys) target;
		if (query.getBookingKey() == null) {
			errors.rejectValue(QueryKeys.QUERY_KEY_BOOKING, "", "");
		}
	}
}
