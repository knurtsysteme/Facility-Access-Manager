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
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.query.QueryStringFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;
import de.knurt.heinzelmann.util.validation.AssertOrException;

/**
 * report news about session processed with a feedback of the operator.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (06/05/2012)
 */
public class NewsSourceForSessionProcessed implements NewsSource {

	@Override
	public List<NewsItem> getNews(TimeFrame from, User subscriber) {
		List<NewsItem> result = new ArrayList<NewsItem>();
		List<Booking> candidates = FamDaoProxy.bookingDao().getAllUncanceledAndProcessed(subscriber);
		for (Booking candidate : candidates) {
			Job job = this.getJobOfNews(from, subscriber, candidate);
			if (job != null) {
				result.add(this.getAsNews(job));
			}
		}
		return result;
	}

	private NewsItem getAsNews(Job job) {
		NewsItem result = new NewsItemDefault();
		result.setEventStarts(new Date(job.getCreated()));
		String desc = String.format("You got feedback for your job #%s", job.getId()); // INTLANG
		result.setDescription(desc);
		String href = TemplateHtml.getInstance().getHref("viewfeedback", QueryStringFactory.get(QueryKeys.QUERY_KEY_BOOKING, job.getJobId() + ""));
		result.setLinkToFurtherInformation(href);
		return result;
	}

	/**
	 * return a job if the booking is interested for the new. check if operator
	 * left a feedback and the feedback is in time requested.
	 */
	private Job getJobOfNews(TimeFrame from, User subscriber, Booking booking) {
		AssertOrException.assertTrue(booking.isProcessed());
		AssertOrException.assertFalse(booking.isCanceled());
		Job result = null;
		Job job = FamDaoProxy.jobsDao().getJob(booking.getId(), Job.STEP_OPERATOR_FEEDBACK);
		if(job != null) {
			Long created = job.getCreated();
			if (job.getCreated() != null && created >= from.getStart() && created <= from.getEnd()) {
				result = job;
			}
		}
		return result;
	}

}
