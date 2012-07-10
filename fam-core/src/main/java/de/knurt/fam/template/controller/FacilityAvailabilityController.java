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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.model.TemplateResource;

/**
 * control the pages, where you can set the general availability in a calendar.
 * delegate between calendar views and overviews and handle the request and its
 * containing date in the right context.
 * 
 * controller for resource named <code>systemfacilityavailability</code>
 * 
 * @author Daniel Oltmanns
 * @since 0.20090421 (04/21/2009)
 */
class FacilityAvailabilityController {

	protected boolean submit(TemplateResource templateResource) {
		boolean result = false;
		Facility facility = RequestInterpreter.getFacility(templateResource.getRequest());
		boolean hasBeenDeleted = false;
		if (facility != null && templateResource.getAuthUser() != null) {
			if (this.suddenFailureIsActive(facility.getKey())) { // ask for stop
				// sudden
				// failure
				FamDaoProxy.facilityDao().stopActualSuddenFailure(facility, templateResource.getAuthUser());
			} else if (this.isRequest4Delete(templateResource.getRequest())) {
				FacilityAvailability da = new FacilityAvailability();
				try {
					Long facilityAvailabilityId = Long.parseLong(RequestInterpreter.getDelete(templateResource.getRequest()));
					da.setFacilityAvailabilityId(facilityAvailabilityId);
					FacilityAvailability got = FamDaoProxy.facilityDao().getOneLike(da);
					if (got != null) {
						try {
							got.delete();
							result = true;
							hasBeenDeleted = true;
						} catch (DataIntegrityViolationException e) {
							FamLog.logException(this.getClass(), e, "delete failed", 201010291108l);
						}
					}

				} catch (Exception e) {
					FamLog.logException(this.getClass(), e, "parsing or something failed", 201010291109l);
				}
			} else if (this.isFinalSentOfAddNewRule(templateResource)) {
				FacilityAvailability insertMe = RequestInterpreter.getCompleteFacilityAvailabilityForInsertion(templateResource.getRequest(), templateResource.getAuthUser());
				if (insertMe != null) {
					try {
						insertMe.insert();
						result = true;
					} catch (DataIntegrityViolationException e) {
						FamLog.logException(this.getClass(), e, "insertion failed", 201010291107l);
					}
				}
			}
		}
		if (result) {
			if (hasBeenDeleted) {
				templateResource.putWritingResultProperty("infoMessage", "A rule has been deleted"); // INTLANG
			} else {
				templateResource.putWritingResultProperty("infoMessage", "A new rule has been added"); // INTLANG
			}
			templateResource.putWritingResultProperty("infobox_id", "successMessage"); // INTLANG
		} else {
			templateResource.putWritingResultProperty("infoMessage", "An error occured"); // INTLANG
			templateResource.putWritingResultProperty("infobox_id", "validationError"); // INTLANG
		}
		return result;
	}

	private boolean isRequest4Delete(HttpServletRequest rq) {
		String toDelete = RequestInterpreter.getDelete(rq);
		return toDelete != null && toDelete.equals("-1") == false;
	}

	private boolean suddenFailureIsActive(String facilityKey) {
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilitiesFollowingParents(facilityKey);
		return this.suddenFailureIsActive(das);
	}

	private boolean suddenFailureIsActive(List<FacilityAvailability> das) {
		boolean result = false;
		for (FacilityAvailability da : das) {
			if (da.isNotAvailableBecauseOfSuddenFailure() && !da.getBasePeriodOfTime().endsInPast()) {
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean isFinalSentOfAddNewRule(TemplateResource templateResource) {
		return RequestInterpreter.getOf(templateResource.getRequest()) != null && RequestInterpreter.getOf(templateResource.getRequest()).equals("4");
	}
}
