package de.knurt.fam.template.controller;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.heinzelmann.util.time.TimeFrame;

class FacilityEmergencyController {

	protected boolean submit(TemplateResource templateResource) {
		assert templateResource.hasAuthUser();
		try {
			Facility d = RequestInterpreter.getFacility(templateResource.getRequest());
			if (RequestInterpreter.isYes(templateResource.getRequest())) { // facility is available again
				FamDaoProxy.facilityDao().stopCurrentSuddenFailure(d, templateResource.getAuthUser());
			} else if (RequestInterpreter.isNo(templateResource.getRequest())) { // facility failed
				this.insertOrUpdateSuddenFailure(d, templateResource.getAuthUser(), templateResource.getRequest());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void insertOrUpdateSuddenFailure(Facility d, User userSetThis, HttpServletRequest request) {
		Integer timeUnits = RequestInterpreter.getTimeUnits(request);
		if (timeUnits != null) {
			FacilityAvailability da = FamDaoProxy.facilityDao().getCurrentSuddenFailure(d);
			if (da == null) { // insert new
				Calendar start = Calendar.getInstance();
				Calendar end = Calendar.getInstance();
				end.add(Calendar.MINUTE, timeUnits);
				da = new FacilityAvailability(d.getKey(), start, end);
				da.setNotAvailableBecauseOfSuddenFailure();
				da.setUserSetThis(userSetThis);
				da.setTimeStampSet(new Date());
				da.insert();
			} else { // update existing
				Calendar newEnd = Calendar.getInstance();
				newEnd.add(Calendar.MINUTE, timeUnits);
				TimeFrame basePeriodOfTime = da.getBasePeriodOfTime();
				basePeriodOfTime.setEnd(newEnd);
				da.setBasePeriodOfTime(basePeriodOfTime);
				da.setUserSetThis(userSetThis);
				da.setTimeStampSet(new Date());
				da.update();
			}
		}
	}
}