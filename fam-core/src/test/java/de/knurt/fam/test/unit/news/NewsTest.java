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
package de.knurt.fam.test.unit.news;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.news.NewsCollector;
import de.knurt.fam.news.NewsCollectorDefault;
import de.knurt.fam.news.NewsItem;
import de.knurt.fam.news.NewsItemDefault;
import de.knurt.fam.news.NewsSource;
import de.knurt.fam.news.NewsSourceForLogbookEntries;
import de.knurt.fam.news.NewsSourceForNewUser;
import de.knurt.fam.news.NewsSourceForSessionProcessed;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class NewsTest extends FamIBatisTezt {

	@Test
	public void constructNewsItem() {
		try {
			NewsItem ni = new NewsItemDefault();
			ni.setEventEnds(new Date());
			assertNotNull(ni);
			assertNotNull(ni.getEventEnds());
			assertTrue("successed", true);
		} catch (Exception e) {
			fail("should not throw exception");
		}
	}

	@Test
	public void constructNewsReporter() {
		try {
			NewsSource nr = new NewsSourceForNewUser();
			assertNotNull(nr);
			@SuppressWarnings("unused")
			List<NewsItem> nis = nr.getNews(SimpleTimeFrame.getToday(), TeztBeanSimpleFactory.getAdmin());
			assertTrue("successed", true);
		} catch (Exception e) {
			fail("should not throw exception");
		}
	}

	@Test
	public void sessionProcessedNewsReporter() {
		this.clearDatabase();
		User u = TeztBeanSimpleFactory.getAdmin();
		u.insert();
		NewsSource nr = new NewsSourceForSessionProcessed();
		assertNotNull(nr);
		List<NewsItem> nis = nr.getNews(SimpleTimeFrame.getToday(), u);
		int nis_size_before = nis.size();

		// insert a processed session
		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.getSessionTimeFrame().add(Calendar.YEAR, 1);
		booking.setBooked();
		booking.setUsername(u.getUsername());
		booking.insert();
		booking.processSession();
		Job document = new Job();
		document.setJobId(booking.getId());
		document.setUsername(u.getUsername());
		document.setStep(2);
		document.setIdJobDataProcessing("foo");
		document.setJobSurvey(new JSONObject());
		document.insertOrUpdate();

		// the test
		nis = nr.getNews(SimpleTimeFrame.getToday(), u);
		assertEquals(nis_size_before + 1, nis.size());
	}

	@Test
	public void logbookNewsReporter() {
		try {
			NewsSource nr = new NewsSourceForLogbookEntries(false);
			assertNotNull(nr);
			List<NewsItem> nis = nr.getNews(SimpleTimeFrame.getToday(), TeztBeanSimpleFactory.getAdmin());
			int nis_size_before = nis.size();
	    int assertNewLogbookEntries = 0;

			LogbookEntry logbook = TeztBeanSimpleFactory.getNewValidLogbookEntry();
			assertNewLogbookEntries++; // because a new user was inserted
			logbook.setLogbookId(TeztBeanSimpleFactory.LOGBOOK_ID1);
			logbook.setDate(new Date());
			logbook.insert();
      assertNewLogbookEntries++; // because a new logbook was inserted

			assertNotNull(logbook.getDate());

			nis = nr.getNews(SimpleTimeFrame.getToday(), TeztBeanSimpleFactory.getAdmin());
			assertEquals(nis_size_before + assertNewLogbookEntries, nis.size());

			// assert same date as lobook entry
			NewsItem back = nis.get(nis.size() - 1);
			assertEquals(back.getEventStarts().toString(), logbook.getDate().toString());

			// update entry to past
			Calendar last_year = Calendar.getInstance();
			last_year.add(Calendar.YEAR, -1);
			logbook.setDate(last_year.getTime());
			logbook.update();

			nis = nr.getNews(SimpleTimeFrame.getToday(), TeztBeanSimpleFactory.getAdmin());
			assertEquals(nis_size_before + 1, nis.size()); // + 1 because of adminLogbook still there

			assertTrue("successed", true);
		} catch (Exception e) {
			fail("should not throw exception");
		}
	}

	@Test
	public void constructNewsCollector() {
		try {
			NewsCollector nc = new NewsCollectorDefault();
			assertNotNull(nc);
			@SuppressWarnings("unused")
			NewsSource ns = (NewsCollector) nc;
			nc.add(new NewsSourceForNewUser());
			assertTrue("successed", true);
		} catch (Exception e) {
			fail("should not throw exception");
		}
	}

}
