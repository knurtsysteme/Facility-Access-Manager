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

import java.util.List;

import org.apache.commons.validator.EmailValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import de.knurt.heinzelmann.util.validation.PasswordValidator;

/**
 * Extend Spring's ValidationUtils with email validation
 * 
 * @see org.springframework.validation.ValidationUtils
 * @author Daniel Oltmanns
 * @since 0.1 03/17/2009
 */
public class JmValidationUtils extends ValidationUtils {

	/**
	 * Reject the given field if it's empty or is one of the given wrong values
	 * 
	 * @param errors
	 *            the Errors instance to register errors on
	 * @param field
	 *            the field name to check
	 * @param errorCode
	 *            error code, interpretable as message key
	 * @param errorArgs
	 *            the error arguments, for argument binding via MessageFormat
	 *            (can be null)
	 * @param defaultMessage
	 *            fallback default message
	 * @param wrongValues
	 *            list of values that are rejected
	 */
	public static void rejectIfEmptyOrOneOfValues(Errors errors, String field, String errorCode, Object[] errorArgs, String defaultMessage, List<String> wrongValues) {
		Object value = errors.getFieldValue(field);
		if (value == null || wrongValues.contains(value.toString())) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}

	/**
	 * Call
	 * <code>JmValidationUtils.rejectIfEmptyOrOneOfValues(errors, field, errorCode, null, defaultMessage, wrongValues);</code>
	 * 
	 * @see JmValidationUtils#rejectIfNotEmail(org.springframework.validation.Errors,
	 *      java.lang.String, java.lang.String, java.lang.Object[],
	 *      java.lang.String)
	 * @param errors
	 *            the Errors instance to register errors on
	 * @param field
	 *            the field name to check
	 * @param errorCode
	 *            error code, interpretable as message key
	 * @param defaultMessage
	 *            fallback default message
	 * @param wrongValues
	 *            list of values that are rejected
	 */
	public static void rejectIfEmptyOrOneOfValues(Errors errors, String field, String errorCode, String defaultMessage, List<String> wrongValues) {
		JmValidationUtils.rejectIfEmptyOrOneOfValues(errors, field, errorCode, null, defaultMessage, wrongValues);
	}

	/**
	 * Reject the given field with the given error code (and default message) if
	 * the value is not a valie email address
	 * 
	 * @param errors
	 *            the Errors instance to register errors on
	 * @param field
	 *            the field name to check
	 * @param errorCode
	 *            error code, interpretable as message key
	 * @param errorArgs
	 *            the error arguments, for argument binding via MessageFormat
	 *            (can be null)
	 * @param defaultMessage
	 *            fallback default message
	 */
	public static void rejectIfNotEmail(Errors errors, String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		Object value = errors.getFieldValue(field);
		if (value == null || EmailValidator.getInstance().isValid(value.toString()) == false) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}

	/**
	 * Call
	 * <code>JmValidationUtils.rejectIfNotEmail(errors, field, errorCode, null, null);</code>
	 * 
	 * @see JmValidationUtils#rejectIfNotEmail(org.springframework.validation.Errors,
	 *      java.lang.String, java.lang.String, java.lang.Object[],
	 *      java.lang.String)
	 * @param errors
	 *            the Errors instance to register errors on
	 * @param field
	 *            the field name to check
	 * @param errorCode
	 *            error code, interpretable as message key
	 */
	public static void rejectIfNotEmail(Errors errors, String field, String errorCode) {
		JmValidationUtils.rejectIfNotEmail(errors, field, errorCode, null, null);
	}

	/**
	 * Call
	 * <code>JmValidationUtils.rejectIfNotEmail(errors, field, errorCode, null, defaultMessage);</code>
	 * 
	 * @see JmValidationUtils#rejectIfNotEmail(org.springframework.validation.Errors,
	 *      java.lang.String, java.lang.String, java.lang.Object[],
	 *      java.lang.String)
	 * @param errors
	 *            the Errors instance to register errors on
	 * @param field
	 *            the field name to check
	 * @param errorCode
	 *            error code, interpretable as message key
	 * @param defaultMessage
	 *            fallback default message
	 */
	public static void rejectIfNotEmail(Errors errors, String field, String errorCode, String defaultMessage) {
		JmValidationUtils.rejectIfNotEmail(errors, field, errorCode, null, defaultMessage);
	}

	/**
	 * Reject a password field, if input is not safe enough This does not check,
	 * if a password is valid (because of invalid characters, pass1 != pass2
	 * etc.)
	 * 
	 * @param errors
	 *            the Errors instance to register errors on
	 * @param field
	 *            the field name to check
	 * @param errorCode
	 *            error code, interpretable as message key
	 * @param errorArgs
	 *            the error arguments, for argument binding via MessageFormat
	 *            (can be null)
	 * @param defaultMessage
	 *            fallback default message
	 * @param minLength
	 *            minimal length of the password to be safe
	 * @param minDigits
	 *            minimal count of digits of the password to be safe
	 * @param minUpper
	 *            minimal count of upper cased chars of the password to be safe
	 * @param minLower
	 *            minimal count of lower cased chars of the password to be safe
	 * @param minSpecial
	 *            minimal count of special chars of the password to be safe
	 */
	public static void rejectIfPasswordIsUnsafe(Errors errors, String field, String errorCode, Object[] errorArgs, String defaultMessage, PasswordValidator pv) {
		if (pv.isValid(errors.getFieldValue(field)) == false) {
			errors.rejectValue(field, errorCode, errorArgs, defaultMessage);
		}
	}
}