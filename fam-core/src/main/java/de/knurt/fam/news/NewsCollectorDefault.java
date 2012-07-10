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
import java.util.Collections;
import java.util.List;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * The default news collector
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public class NewsCollectorDefault implements NewsCollector {

	private List<NewsSource> knownSources = new ArrayList<NewsSource>();

	/** {@inheritDoc} */
	@Override
	public boolean add(NewsSource source) {
		if (!this.knownSources.contains(source)) {
			this.knownSources.add(source);
			return true;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(NewsSource source) {
		if (this.knownSources.contains(source)) {
			this.knownSources.remove(source);
			return true;
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<NewsItem> getNews(TimeFrame from, User to) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		for (NewsSource knownSource : knownSources) {
			try {
				result.addAll(knownSource.getNews(from, to));
			} catch (Exception e) {
				FamLog.exception(knownSource.getClass().toString(), e, 201205181207l);
			}
		}
		Collections.sort(result);
		return result;
	}
}