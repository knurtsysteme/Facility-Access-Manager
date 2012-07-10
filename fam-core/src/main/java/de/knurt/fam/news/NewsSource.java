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

import java.util.List;

import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report a list of {@link NewsItem} objects concerning a specific point of
 * time. this is the source for a {@link NewsCollector}.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public interface NewsSource {

	/**
	 * return the news for the given time frame interested in. return
	 * <code>null</code> or empty list when nothing happened.
	 * 
	 * @param from
	 *            interested in
	 * @param subscriber
	 *            news are going to (the auth person in most cases)
	 * @return the news for the given time frame interested in
	 */
	public List<NewsItem> getNews(TimeFrame from, User subscriber);

}
