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

import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;

/**
 * get html representation for a logbook, that are table rows.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090412 (04/12/2009)
 */
@Deprecated
public class LogbookOverviewTrHtml extends LogbookHtml {

	/** one and only instance of me */
	private volatile static LogbookOverviewTrHtml me;

	/** construct me */
	private LogbookOverviewTrHtml() {
	}

	/**
	 * return the one and only instance of RolePool
	 * 
	 * @return the one and only instance of RolePool
	 */
	public static LogbookOverviewTrHtml getInstance() {
		if (me == null) { // no instance so far
			synchronized (LogbookOverviewTrHtml.class) {
				if (me == null) { // still no instance so far
					me = new LogbookOverviewTrHtml(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return the representation here. these are rows of a html table,
	 * representing different logbooks.
	 * 
	 * @return rows of a html table.
	 */
	@Override
	public String getHtml() {
		Formatter result = new Formatter();
		String toFormat = this.getToFormat();
		for (String key : LogbookConfigDao.getInstance().getKeys()) {
			String oddeven = this.getOddEven();
			String label = this.getLabel(key);
			String desc = LogbookConfigDao.getInstance().getDescription(key);
			String entryCount = LogbookConfigDao.getInstance().getEntryCount(key) + "";
			String makePostQueryString = RedirectResolver.getLogbookMakePostURLWithQueryString(key);

			String view = "";
			if (LogbookConfigDao.getInstance().getEntryCount(key) > 0) {
				view = ", <a href=\"" + QueryStringBuilder.getLogbookQueryString(key).getAsHtmlLinkHref() + "\">View</a>"; // INTLANG
			}

			String lastEntryFrom = this.getNewestEntry(key);
			result.format(toFormat, oddeven, label, desc, makePostQueryString, view, entryCount, lastEntryFrom);
		}
		return result.toString();
	}

	/**
	 * return label. as link, if there are entries and otherwise as plain text.
	 * 
	 * @param key
	 *            of the logbook
	 * @return label
	 */
	private String getLabel(String key) {
		return String.format("<h3>%s</h3>", LogbookConfigDao.getInstance().getLabel(key));
	}

	/**
	 * return string to format
	 * 
	 * <pre>
	 * <tr>
	 *  <th>Logbook</th>
	 *  <th>Entries</th>
	 *  <th>Last entry from</th>
	 * </tr>
	 * </pre>
	 * 
	 * @return string to format
	 */
	@Override
	protected String getToFormat() {
		String result = "<tr class=\"%s\">"; // 0 odd | even
		result += "<td>";
		result += "<p>%s</p>"; // 1 label
		result += "<p class=\"small\">%s</p>"; // 2 description
		// ↘ INTLANG 3 make post link view
		result += "<p class=\"small\"><a href=\"%s\">Make Post</a>%s</p>";
		result += "</td>";
		result += "<td class=\"small\">%s</td>"; // 4 number of entries
		result += "<td class=\"small\">%s</td>"; // 5 last entry info
		result += "</tr>";
		return result;
	}
}
