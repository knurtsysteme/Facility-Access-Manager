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

import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report news broken facilities
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (09/13/2011)
 * 
 */
public class NewsSourceForFacilityFailureAlerts implements NewsSource {

	/** {@inheritDoc} */
	@Override
	public List<NewsItem> getNews(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		List<Facility> facilities = FacilityConfigDao.getInstance().getAll();
		for (Facility facility : facilities) {
			if (to.isAllowedToAccess(facility)) {
				FacilityAvailability da = facility.getFacilityStatus().getFacilityAvailability();
				if (da.isNotAvailableBecauseOfBooking() || da.isNotAvailableBecauseOfSuddenFailure() || da.isNotAvailableBecauseOfMaintenance() || da.isNotAvailableInGeneral()) {
					NewsItem ni = new NewsItemDefault();
					String description = "";
					if (da.isNotAvailableBecauseOfBooking()) {
						description = String.format("In use: %s", facility.getLabel()); // INTLANG
						if (to.hasRight2ViewPage("book")) {
							ni.setLinkToFurtherInformation(TemplateHtml.href("book") + QueryStringBuilder.getQueryString(facility));
						}
					} else if (da.isNotAvailableBecauseOfSuddenFailure()) {
						description = String.format("Not usable: %s", facility.getLabel()); // INTLANG
						// TODO @see #18 both links do make sense for e.g.
						// admins
						if (to.hasRight2ViewPage("facilityemergency")) {
							ni.setLinkToFurtherInformation(TemplateHtml.href("facilityemergency"));
						} else if (to.hasRight2ViewPage("book")) {
							ni.setLinkToFurtherInformation(TemplateHtml.href("book") + QueryStringBuilder.getQueryString(facility));
						}
					} else if (da.isNotAvailableBecauseOfMaintenance()) {
						description = String.format("Maintenance: %s", facility.getLabel()); // INTLANG
						if (to.hasRight2ViewPage("book")) {
							ni.setLinkToFurtherInformation(TemplateHtml.href("book") + QueryStringBuilder.getQueryString(facility));
						}
					} else if (da.isNotAvailableInGeneral()) {
						description = String.format("Not available: %s", facility.getLabel()); // INTLANG
						if (to.hasRight2ViewPage("book")) {
							ni.setLinkToFurtherInformation(TemplateHtml.href("book") + QueryStringBuilder.getQueryString(facility));
						}
					}
					if (da.getNotice() != null) {
						description += " Notice: " + da.getNotice(); // INTLANG
					}
					ni.setDescription(description);
					if (da.getStartOfBasePeriodOfTime() == null) {
						ni.setEventStarts(new Date());
					} else {
						ni.setEventStarts(da.getStartOfBasePeriodOfTime());
						if (da.getEndOfBasePeriodOfTime() != null) {
							ni.setEventEnds(da.getEndOfBasePeriodOfTime());
						}
					}
					result.add(ni);
				}
			}
		}
		return result;
	}
}