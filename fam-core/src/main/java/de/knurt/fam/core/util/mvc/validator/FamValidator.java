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

import org.springframework.validation.Validator;

import de.knurt.fam.core.util.mvc.QueryKeys;

/**
 * a {@link Validator} supports {@link QueryKeys}. this is used for nearly 100
 * % of all queries in the Facility Access Manager.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090818 (08/18/2009)
 */
abstract class FamValidator implements Validator {

	/**
	 * set {@link QueryKeys} as support
	 * 
	 * @param clazz
	 *            being supported
	 * @return true, if given class is supported
	 */
	public boolean supports(Class<?> clazz) {
		return clazz.equals(QueryKeys.class);
	}
}
