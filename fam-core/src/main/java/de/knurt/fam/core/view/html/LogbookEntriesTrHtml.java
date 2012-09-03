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
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * get html representation for a logbook, that are table rows.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090412 (04/12/2009)
 */
@Deprecated
public class LogbookEntriesTrHtml extends LogbookHtml {

	private String logbookKey;
	private int from;
	private int to;

	/**
	 * set given things
	 * 
	 * @param logbookKey
	 *            key for the logbook
	 * @param from
	 *            entries shown from
	 * @param to
	 *            entries shown to
	 */
	public LogbookEntriesTrHtml(String logbookKey, int from, int to) {
		this.logbookKey = logbookKey;
		this.from = from;
		this.to = to;
	}

	/**
	 * return the representation here. these are rows of a html table,
	 * representing logbook entries.
	 * 
	 * @return rows of a html table.
	 */
	@Override
	public String getHtml() {
		Formatter result = new Formatter();
		String toFormat = this.getToFormat();
		for (LogbookEntry le : FamDaoProxy.getInstance().getLogbookEntryDao().get(this.logbookKey, this.from, this.to)) {
			String oddeven = this.getOddEven();
			String headline = le.getHeadline();
			String content = le.getContent().replaceAll("\n", "<br />");
			String tags = this.getTagsTd(le);
			String user = this.getUserTd(le);
			result.format(toFormat, oddeven, this.getDateCol(le), headline, content, tags, user);
		}
		if (result.toString().equals("")) {
			return this.getNoEntryLine();
		} else {
			return result.toString();
		}
	}

	private String getDateCol(LogbookEntry le) {
		return HtmlFactory.get("span").hide().add(le.getDate().getTime()) + FamDateFormat.getDateFormattedWithTime(le.getDate());
	}

	private String getUserTd(LogbookEntry entry) {
		if (entry == null) {
			return "";
		} else {
			User user = FamDaoProxy.userDao().getUserFromUsername(entry.getOfUserName());
			return user == null ? "unknown user" : user.getFullName(); // INTLANG
		}
	}

	private String getTagsTd(LogbookEntry entry) {
		String result = "";
		if (entry != null) {
			int i = 0;
			for (String tag : entry.getTags()) {
				if (i == 0) {
					result += "<strong>";
				}
				result += tag;
				if (i == 0) {
					result += "</strong>";
				}
				i++;
				if (i < entry.getTags().size()) {
					result += ", ";
				}
			}
		}
		return result.toString();
	}

	/**
	 * return the format string to format a row of a table.<br />
	 * table is starting like this 
	 * 
	 * @see String#format(java.lang.String, java.lang.Object[])
	 * @return the format string to format a row of a table.
	 */
	@Override
	protected String getToFormat() {
		return "<tr class=\"%s\">" + "<td class=\"small\">%s</td>" +
				"<td>" + 
					"<p><strong>%s</strong></p>" + // headline
					"<p class=\"small\">%s</p>" +
				"</td>" + // desc
				"<td class=\"small\">%s</td>" + // user
				"<td class=\"small\">%s</td>" + // tags
				"</tr>";

	}

	private String getNoEntryLine() {
		return "<tr><td colspan=\"4\">No entry yet</td></tr>"; // INTLANG
	}
}
