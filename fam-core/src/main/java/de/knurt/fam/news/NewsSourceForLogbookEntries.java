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

import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.query.QueryStringFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * report news about new logbook entries
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (04/30/2012)
 */
public class NewsSourceForLogbookEntries implements NewsSource {

  private boolean sinceLastLogin = false;

  /**
   * construct news source.
   * 
   * @param sinceLastLogin if true, the requested timeframe is set back to users last login
   */
  public NewsSourceForLogbookEntries(boolean sinceLastLogin) {
    this.sinceLastLogin = sinceLastLogin;
  }

  /**
   * return the logbook entries as news. if {@link #sinceLastLogin} is true, the start of the requested timeframe is set back to users last login.
   */
  @Override
  public List<NewsItem> getNews(TimeFrame from, User to) {
    if (this.sinceLastLogin) {
      Date start = to.getLastLogin();
      if (start == null) {
        start = to.getRegistration();
      }
      if (start != null) {
        from.setStart(start);
      }
    }
    List<NewsItem> result = new ArrayList<NewsItem>();
    List<LogbookEntry> entries = this.getLogbookEntries(from, to);
    for (LogbookEntry entry : entries) {
      NewsItem ni = new NewsItemDefault();
      ni.setEventStarts(entry.getDate());
      ni.setDescription(this.getDescription(entry));
      String href = TemplateHtml.getInstance().getHref("logbook", QueryStringFactory.get(QueryKeys.QUERY_KEY_LOGBOOK, entry.getLogbookId()));
      ni.setLinkToFurtherInformation(href);
      result.add(ni);
    }
    return result;
  }

  private String getDescription(LogbookEntry entry) {
    Logbook lb = LogbookConfigDao.getInstance().get(entry.getLogbookId());
    String loogbookName = lb.getLabel();
    // ↘ INTLANG
    return String.format("New logbook-entry in „%s“: %s", loogbookName, entry.getHeadline());
  }

  private List<LogbookEntry> getLogbookEntries(TimeFrame from, User to) {
    return FamDaoProxy.logbookEntryDao().getEntriesMadeIn(from);
  }

}