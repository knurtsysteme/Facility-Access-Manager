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
import java.util.List;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report news about new users
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public class NewsSourceForNewUser implements NewsSource {

	/** {@inheritDoc} */
	@Override
	public List<NewsItem> getNews(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		List<User> users = FamDaoProxy.userDao().getUsersRegistrationIsIn(from);
		for (User user : users) {
			if (from.contains(user.getRegistration())) {
				NewsItem ni = new NewsItemDefault();
				if (to.hasRight(8, null)) { // user has right to view personal
											// information
					ni.setDescription(String.format("Inserted new user: %s", user.getFullName())); // INTLANG
				} else {
					ni.setDescription("Inserted a new user"); // INTLANG
				}
				ni.setEventStarts(user.getRegistration());
				if (to.hasRight2ViewPage("users")) {
					ni.setLinkToFurtherInformation(TemplateHtml.href("users"));
				}
				result.add(ni);
			}
		}
		return result;
	}
}