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
package de.knurt.fam.template.controller;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.model.TemplateResource;

/**
 * controller for editing a facility availability
 * 
 * @author Daniel Oltmanns
 * @since 0.20091112 (11/12/2009)
 */
class EditFacilityAvailabilityController {

	public boolean submit(TemplateResource templateResource) {
		boolean result = false;
		FacilityAvailability da = RequestInterpreter.getExistingFacilityAvailabilityWithId(templateResource.getRequest());
		String notice = RequestInterpreter.getNotice(templateResource.getRequest());
		da.setNotice(notice);
		try {
			da.update();
			result = true;
		} catch (DataIntegrityViolationException e) {
			FamLog.exception(e, 201010291856l);
		}
		templateResource.putWritingResultProperty("text_notice", notice);
		return result;
	}
}
