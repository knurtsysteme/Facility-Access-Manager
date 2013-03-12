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

import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.util.mvc.LogbookEntryForm;

/**
 * Validation for a {@link LogbookEntry}. check if a user has put in the
 * required fields for a entry.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090327 (03/27/2009)
 */
public class LogbookEntryValidator implements Validator {

	@Override
  public boolean supports(Class<?> clazz) {
		return clazz.equals(LogbookEntryForm.class);
	}

	@Override
  public void validate(Object target, Errors errors) {
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "headline", "page.logbookentry.input.headline.required", "Headline is required");
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "content", "page.logbookentry.input.content.required", "Content is required");
		JmValidationUtils.rejectIfEmptyOrWhitespace(errors, "musttag", "page.logbookentry.input.musttag.required", "Category is required");
		if (errors.getErrorCount() > 0) {
			errors.reject("page.logbookentry.input.error", "Please correct your input");
		}
	}
}
