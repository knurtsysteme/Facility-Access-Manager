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
package de.knurt.fam.core.content.adapter.html;

import java.util.Calendar;

import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;

/**
 * html adapter for a {@link FacilityAvailability}. generate information html for
 * the object being adapted.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@Deprecated
public class HtmlAdapterFacilityAvailability extends FamHtmlAdapter<FacilityAvailability> {

	private FacilityAvailability da;

	/**
	 * construct me
	 * 
	 * @param actual
	 *            user being authenticated
	 * @param da
	 *            availability being adapted
	 */
	protected HtmlAdapterFacilityAvailability(User actual, FacilityAvailability da) {
		super(da);
		this.da = da;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return da.getFacility().getLabel();
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return FamText.facilityAvailability(da);
	}

	/**
	 * return the output of
	 * {@link FamDateFormat#getDateFormattedWithTime(de.knurt.heinzelmann.util.time.TimeFrame, boolean)}
	 * . given time frame is the base period of time for the facility
	 * availability.
	 * 
	 * @return the output of
	 *         {@link FamDateFormat#getDateFormattedWithTime(de.knurt.heinzelmann.util.time.TimeFrame, boolean)}
	 *         .
	 */
	public String getBaseTimeFrame() {
		String result = FamDateFormat.getDateFormattedWithTime(da.getBasePeriodOfTime(), true);
		return result;
	}

	/**
	 * @return the notice
	 */
	public String getNotice() {
		return da.getNotice();
	}

	/**
	 * return only the time of the base time frame if the booking is on the same
	 * day. otherwise return the full date.
	 * 
	 * @return only the time of the base time frame if the booking is on the
	 *         same day. otherwise return the full date.
	 */
	public String getBaseTimeFrameTime() {
		if (da.getBasePeriodOfTime().getCalendarStart().get(Calendar.DAY_OF_YEAR) == da.getBasePeriodOfTime().getCalendarEnd().get(Calendar.DAY_OF_YEAR)) {
			return FamDateFormat.getTimeFormatted(da.getBasePeriodOfTime());
		} else {
			return this.getBaseTimeFrame();
		}
	}
}
