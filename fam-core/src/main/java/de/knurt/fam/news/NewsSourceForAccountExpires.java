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
import java.util.Calendar;
import java.util.List;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.time.CalendarUtil;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report news about accounts expiring to admins
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (09/13/2011)
 * 
 */
public class NewsSourceForAccountExpires implements NewsSource {

	private final static int DAYS_IN_FUTURE = 10;

	/** {@inheritDoc} */
	@Override
	public List<NewsItem> getNews(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		TimeFrame clone = from.clone();
		clone.addEnd(Calendar.DAY_OF_YEAR, DAYS_IN_FUTURE);
		List<User> users = FamDaoProxy.userDao().getUserAccountExpiresIn(clone);
		for (User user : users) {
			if (user.getAccountExpires() != null) {
				long days = CalendarUtil.me().daysBetween(from.getCalendarStart(), user.getAccountExpires());
				NewsItem ni = new NewsItemDefault();
				String description = this.getDescription(user, days);
				ni.setDescription(description);
				ni.setEventStarts(user.getAccountExpires());
				if (to.hasRight2ViewPage("users")) {
					ni.setLinkToFurtherInformation(TemplateHtml.href("users"));
				}
				result.add(ni);
			}
		}
		return result;
	}

	private String getDescription(User user, long days) {
		return String.format("Account of %s expires", user.getFullName()); // INTLANG
	}

}