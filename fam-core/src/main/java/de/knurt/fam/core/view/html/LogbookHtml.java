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
package de.knurt.fam.core.view.html;

import java.util.Formatter;

import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.heinzelmann.ui.html.HtmlTableUtils;

/**
 * get html representation for a logbook, that are table rows.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090412 (04/12/2009)
 */
public abstract class LogbookHtml {

  private HtmlTableUtils tableUtils;

  /**
   * default constructor setting {@link HtmlTableUtils} internly
   */
  protected LogbookHtml() {
    this.tableUtils = new HtmlTableUtils();
  }

  /**
   * return {@link HtmlTableUtils#getOddEven()}
   * 
   * @return {@link HtmlTableUtils#getOddEven()}
   */
  protected String getOddEven() {
    return this.tableUtils.getOddEven();
  }

  /**
   * return html of <code>&lt;T&gt;</code>
   * 
   * @return html of <code>&lt;T&gt;</code>
   */
  public abstract String getHtml();

  /**
   * return a format string used to display the content of the logbook.
   * 
   * @see String#format(java.lang.String, java.lang.Object[])
   * @return a format string used to display the content of the logbook.
   */
  protected abstract String getToFormat();

  /**
   * return {@link #getHtml()}
   * 
   * @return {@link #getHtml()}
   */
  @Override
  public String toString() {
    return this.getHtml();
  }

  /**
   * return the newest entry of the given logbook.
   * 
   * @param logbookKey key representing a logbook
   * @return the newest entry of the given logbook.
   */
  protected String getNewestEntry(String logbookKey) {
    return this.getEntryInfo(FamDaoProxy.logbookEntryDao().getNewestEntry());
  }

  /**
   * return the information of a enty. this is: who set what when?
   * 
   * @param entry the information is generated from
   * @return the information of a enty.
   */
  protected String getEntryInfo(LogbookEntry entry) {
    String result = null;
    Formatter resultFormatter = new Formatter();
    if (entry == null) {
      resultFormatter.format("-", "");
    } else {
      User user = FamDaoProxy.userDao().getUserFromUsername(entry.getOfUserName());
      String userinfo = user == null ? "unknown user" : user.getFullName();
      String date = FamDateFormat.getDateFormatted(entry.getDate());
      date = date.replace(" ", "&nbsp;");
      String tags = "";
      int i = 0;
      for (String tag : entry.getTags()) {
        if (i == 0) {
          tags += "<strong>";
        }
        tags += tag;
        if (i == 0) {
          tags += "</strong>";
        }
        i++;
        if (i < entry.getTags().size()) {
          tags += ", ";
        }
      }
      // INTLANG
      String toFormat = "<p class=\"small\">%s</p><p class=\"small\">Date:&nbsp;%s</p><p class=\"small\">%s</p>";
      resultFormatter.format(toFormat, userinfo, date, tags);
    }
    result = resultFormatter.toString();
    resultFormatter.close();
    return result;
  }
}
