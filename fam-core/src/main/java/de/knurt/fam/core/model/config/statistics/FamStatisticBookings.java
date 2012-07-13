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
package de.knurt.fam.core.model.config.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * the default statistic view.
 * 
 * @author Daniel Oltmanns
 * @since 0.20091011
 */
public class FamStatisticBookings extends FamStatisticAbstract {

	/** {@inheritDoc} */
	@Override
	public Properties resolveModelAndView(TemplateResource templateResource, QueryString qs) {
		Properties result = new Properties();
		result.put("statistic", this);
		result.put("facilityStatistics", this.getFacilityStatistics());
		result.put("rootFacility", FacilityConfigDao.getInstance().getRootFacility());
		return result;
	}

	private Map<Facility, Properties> getFacilityStatistics() {
		List<Booking> bookings = FamDaoProxy.bookingDao().getAll();
		List<FacilityBookable> bds = FacilityConfigDao.getInstance().getBookableFacilities();
		Map<Facility, Properties> result = new HashMap<Facility, Properties>();
		for (FacilityBookable bd : bds) {
			result.put(bd, this.getCleanProperties());
		}
		Properties rootStatistic = this.getCleanProperties();
		result.put(FacilityConfigDao.getInstance().getRootFacility(), rootStatistic);
		for (Booking booking : bookings) {
			Properties totalStatistic = result.get(booking.getFacility());
			this.add(totalStatistic, "total", 1);
			this.add(rootStatistic, "total", 1);
			if (booking.isApplication()) {
				this.add(totalStatistic, "application", 1);
				this.add(rootStatistic, "application", 1);
			}
			if (booking.isCanceled()) {
				this.add(totalStatistic, "canceled", 1);
				this.add(rootStatistic, "canceled", 1);
			}
			else if (booking.sessionAlreadyMade()) {
				this.add(totalStatistic, "made", 1);
				this.add(rootStatistic, "made", 1);
			}
			if (booking.isProcessed()) {
				this.add(totalStatistic, "processed", 1);
				this.add(rootStatistic, "processed", 1);
			}

			result.put(booking.getFacility(), totalStatistic);
		}
		return result;

	}

	private Properties getCleanProperties() {
		Properties result = new Properties();
		result.put("total", 0);
		result.put("application", 0);
		result.put("canceled", 0);
		result.put("made", 0);
		result.put("processed", 0);
		return result;
	}

	private void add(Properties totalStatistic, String key, int amount) {
		totalStatistic.put(key, Integer.parseInt(totalStatistic.get(key).toString()) + amount);
	}
}
