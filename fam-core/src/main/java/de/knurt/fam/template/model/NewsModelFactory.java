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
package de.knurt.fam.template.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.news.NewsCollector;
import de.knurt.fam.news.NewsCollectorDefault;
import de.knurt.fam.news.NewsItem;
import de.knurt.fam.news.NewsItemDefault;
import de.knurt.fam.news.NewsSource;
import de.knurt.fam.news.NewsSourceForAccountExpires;
import de.knurt.fam.news.NewsSourceForApplicationsOnFacilities;
import de.knurt.fam.news.NewsSourceForFacilityFailureAlerts;
import de.knurt.fam.news.NewsSourceForLogbookEntries;
import de.knurt.fam.news.NewsSourceForNewUser;
import de.knurt.fam.news.NewsSourceForSessionProcessed;
import de.knurt.fam.news.NewsSourceForYourBookings;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;

/**
 * generate the model for the news of today
 * 
 * @see NewsCollector
 * @see NewsSource
 * @author Daniel Oltmanns
 * @since 1.5.0 (07/29/2011)
 */
public class NewsModelFactory {

	public List<NewsItem> getNewsItems(TemplateResource templateResource) {
		List<NewsItem> result = templateResource.getSession().getNewsItems();
		if (result == null) {
			NewsCollector nc = new NewsCollectorDefault();
			User auth = templateResource.getAuthUser();
			if (auth != null) {
				try {
					if (auth.isAdmin()) {
						nc.add(new NewsSourceForNewUser());
						nc.add(new NewsSourceForAccountExpires());
					}
					nc.add(new NewsSourceForYourBookings());
					nc.add(new NewsSourceForApplicationsOnFacilities());
					nc.add(new NewsSourceForFacilityFailureAlerts());
					nc.add(new NewsSourceForLogbookEntries(true));
					nc.add(new NewsSourceForSessionProcessed());
				} catch (Exception e) {
					FamLog.exception(e, 201109131057l);
				}
				result = nc.getNews(SimpleTimeFrame.getToday(), auth);
			} else {
				result = new ArrayList<NewsItem>(0); // return empty list
			}
			templateResource.getSession().setNewsItems(result);
		}
		return result;
	}

	public static List<NewsItem> getErrorNewsItem() {
		List<NewsItem> result = new ArrayList<NewsItem>(1);
		NewsItem ni = new NewsItemDefault();
		ni.setDescription("please report error in generating news items"); // INTLANG
		ni.setEventStarts(new Date());
		result.add(ni);
		return result;
	}

}
