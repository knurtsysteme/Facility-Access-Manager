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
package de.knurt.fam.news;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report news about applications on facilities to operator auth
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (09/13/2011)
 * 
 */
public class NewsSourceForApplicationsOnFacilities implements NewsSource {

	/** {@inheritDoc} */
	@Override
	public List<NewsItem> getNews(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		List<Booking> applications = FamDaoProxy.bookingDao().getAllUncanceledApplicationsNotMade();
		int counter = 0;
		for (Booking application : applications) {
			if (to.hasResponsibility4Facility(application.getFacility())) {
				counter++;
			}
		}
		if (counter > 0) {
			NewsItem ni = new NewsItemDefault();
			ni.setEventStarts(new Date());
			String isAre = counter > 1 ? "are" : "is";
			String facilityFacilities = to.getFacilityKeysUserIsResponsibleFor().size() > 1 ? "facilities" : "facility";
			ni.setDescription(String.format("There %s %s booking requests for your %s", isAre, counter, facilityFacilities));
			ni.setLinkToFurtherInformation(TemplateHtml.href("systemmodifyapplications"));
			result.add(ni);
		}
		return result;
	}

}