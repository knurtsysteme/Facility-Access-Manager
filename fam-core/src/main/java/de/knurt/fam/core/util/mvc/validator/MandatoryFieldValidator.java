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

/**
 * this is a validator out of the box. it is used to decide which input for
 * which object is mandatory.
 * 
 * @param <T>
 *            an object, mandatory is checked with.
 * @author Daniel Oltmanns
 * @since 0.20100131 (01/31/2009)
 */
public interface MandatoryFieldValidator<T> {

	/**
	 * return true, if the field with the given <code>fieldname</code> is
	 * mandatory for the given object. the given object typically is a user.
	 * 
	 * @param object
	 *            to check the mandatory for.
	 * @param fieldname
	 *            to check. may be a key in general
	 * @return true, if the field with the given <code>fieldname</code>
	 * @throws InvalidRoleIdException 
	 * @throws UnsupportedOperationException
	 *             throws exception, if the fieldname is unknown
	 */
	public boolean isMandatory(T object, String fieldname) throws InvalidRoleIdException;

	/**
	 * return true, if the given fieldname is not mandatory or it is not
	 * mandatory but the given object has the given fieldname sufficiently.
	 * 
	 * @param object
	 *            to check
	 * @param fieldname
	 *            to check
	 * @return true, if the given object has sufficiently the given fieldname.
	 */
	public boolean isSufficient(T object, String fieldname) throws InvalidRoleIdException;
}
