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
package de.knurt.fam.core.model.config;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.booking.CurrentFacilityStatus;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.heinzelmann.util.adapter.ViewableObject;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;

/**
 * a facility in general. this uses a properties file to get all the message
 * values like description and label etc. this is resolved via {@link FamText}
 * and {@link FacilityConfigDao} and has nothing to do with this class!
 * 
 * @author Daniel Oltmanns
 * @since 0.20090303
 */
public class Facility implements ViewableObject {

	private Facility parentFacility;

	/**
	 * return the label cut after 3 chars.
	 * 
	 * @see #getLabel()
	 * @return the label cut after 3 chars.
	 */
	public String getShortLabel() {
		String result = this.getLabel();
		if (result.length() > 3) {
			result = result.substring(0, 3) + "...";
		}
		return result;
	}

	/**
	 * A facility. Must always be injected!
	 */
	protected Facility() {
	}

	/**
	 * return the key representing this facility.
	 * 
	 * @return the key representing this facility.
	 */
	public String getKey() {
		return FacilityConfigDao.key(this);
	}

	/**
	 * return the label of this facility. this is used for presenting the
	 * facility to a user.
	 * 
	 * @return the label of this facility.
	 */
	public String getLabel() {
		return FacilityConfigDao.label(this);
	}

	/**
	 * return true, if the facility has a parent. return false, if this is a
	 * root-facility.
	 * 
	 * @return true, if the facility has a parent.
	 */
	public boolean hasParent() {
		return this.parentFacility != null;
	}

	/**
	 * return the parent facility
	 * 
	 * @return the parent facility to set
	 */
	public Facility getParentFacility() {
		return parentFacility;
	}

	/**
	 * set the parent facility
	 * 
	 * @param parentFacility
	 *            the parent facility to set
	 */
	public void setParentFacility(Facility parentFacility) {
		this.parentFacility = parentFacility;
	}

	/**
	 * return the current status.
	 * 
	 * @return the current status.
	 */
	public CurrentFacilityStatus getFacilityStatus() {
		return new CurrentFacilityStatus(this);
	}

	/**
	 * return true, if this facility is bookable.
	 * 
	 * @return true, if this facility is bookable.
	 */
	public boolean isBookable() {
		return FacilityConfigDao.bookable(this.getKey());
	}

	/**
	 * return true, if facility has children.
	 * 
	 * @return true, if facility has children.
	 */
	public boolean hasChildren() {
		return FacilityConfigDao.getInstance().getChildrenKeys(this.getKey()).size() > 0;
	}

	/**
	 * return children of facility.
	 * 
	 * @param allGenerations
	 *            if true, return children's children (all generations). if
	 *            false return only direct children.
	 * @return children of facility.
	 */
	public List<Facility> getChildren(boolean allGenerations) {
		return FacilityConfigDao.getInstance().getChildrenFacilities(this, allGenerations);
	}

	/**
	 * return children of facility. same as <code>getChildren(false)</code>
	 * 
	 * @see #getChildren(boolean)
	 * @return children of facility.
	 */
	public List<Facility> getChildren() {
		return this.getChildren(false);
	}

	/**
	 * return true, if now there is no failure on the facility.
	 * 
	 * @see CurrentFacilityStatus
	 * @return true, if now there is no failure on the facility.
	 */
	public boolean isInWorkingOrderNow() {
		return this.getFacilityStatus().getFacilityAvailability().isFailure() == false;
	}

	public String getAvailabilityNotice() {
		String result = null;
		FacilityAvailability da = this.getFacilityStatus().getFacilityAvailability();
		if (da != null) {
			if (da.getNotice() != null && !da.getNotice().isEmpty()) {
				result = da.getNotice();
			}
		}
		return result;
	}

	/**
	 * return the minutes until this facility is ready-to-use again. return 0,
	 * if the facility is ready to use.
	 * 
	 * @return the minutes until this facility is ready-to-use again.
	 */
	public int getIsReadyToUseIn() {
		int minutes = 0;
		FacilityAvailability da = this.getFacilityStatus().getFacilityAvailability();
		if (da != null && da.getBasePeriodOfTime() != null) {
			minutes = (int) (new SimpleTimeFrame(Calendar.getInstance(), da.getBasePeriodOfTime().getCalendarEnd()).getDuration() / 60000);
		}
		return minutes;
	}

	/**
	 * return true, if this facility is unknown and not part of the current
	 * configuration. an unknown facility is created, if a facility is deleted
	 * from configuration. then, there may exist still some bookings for the
	 * facility - and then, this is the unknown facility.
	 * 
	 * @see FacilityConfigDao#getUnknownBookableFacility()
	 * @return true, if this facility is unknown.
	 */
	public boolean isUnknown() {
		return FacilityConfigDao.getUnknownBookableFacility().getKey().equals(this.getKey()) || FacilityConfigDao.isKey(this.getKey()) == false;
	}

  public FacilityBookable getAsBookable() {
    return FacilityConfigDao.bookableFacility(this.getKey());
  }
}
