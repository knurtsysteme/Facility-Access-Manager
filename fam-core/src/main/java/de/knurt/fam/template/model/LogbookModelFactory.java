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
package de.knurt.fam.template.model;

import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.core.persistence.dao.LogbookEntryDao;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.view.html.LogbookEntriesTrHtml;
import de.knurt.fam.core.view.html.LogbookOverviewTrHtml;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * produce the model for tutorials
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/21/2010)
 */
@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
public class LogbookModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		if (this.isQueryForLogbookEntriesOfLogbook(templateResource.getRequest())) {
			String logbookKey = templateResource.getRequest().getParameter(QueryKeys.QUERY_KEY_LOGBOOK);
			result.put("entryView", true);
			result.put("logbookKey", logbookKey);
			result.put("logbook_trhtml", new LogbookEntriesTrHtml(logbookKey, this.getShowFromEntry(templateResource.getRequest(), logbookKey), this.getShowToEntry(templateResource.getRequest(), logbookKey)));
			result.put("logbook_name", LogbookConfigDao.getInstance().getLabel(logbookKey));
			result.put("logbook_description", LogbookConfigDao.getInstance().getDescription(logbookKey));
			result.put("logbook_pageshownfromtooptions", this.getPageFromToNavi(templateResource.getRequest(), logbookKey));
			result.put("logbook_link2post", RedirectResolver.getLogbookMakePostURLWithQueryString(logbookKey));
			result.put("logbook_postingsuccess", this.getShowSuccessfullPosting(templateResource.getRequest()));
		} else { // query for overview
			result.put("entryView", false);
			result.put("logbook_trhtml", LogbookOverviewTrHtml.getInstance());
		}
		return result;
	}

	private int getCurrentPageNumber(HttpServletRequest rq, String logbookKey) {
		String result = rq.getParameter(QueryKeys.QUERY_KEY_PAGENO);
		if (result == null || Integer.parseInt(result) > this.getTotalPageNumber(rq, logbookKey)) {
			result = QueryStringBuilder.QUERY_LOGBOOK_DEFAULT_VALUE_PAGENO;
		}
		return Math.abs(Integer.parseInt(result));
	}

	/**
	 * returns the little page selection navigation. XXX not a controller
	 * element -> put in content / view
	 * 
	 * @param rq
	 * @param logbookKey
	 * @return
	 */
	private String getPageFromToNavi(HttpServletRequest rq, String logbookKey) {
		int actpno = this.getCurrentPageNumber(rq, logbookKey);
		int totpno = this.getTotalPageNumber(rq, logbookKey);
		String result = "";
		if (LogbookConfigDao.getInstance().getEntryCount(logbookKey) > 0) {

			// create numbers to show as link
			ArrayList<Integer> showNumbersCandidates = new ArrayList<Integer>();
			showNumbersCandidates.add(1);
			showNumbersCandidates.add(2);
			showNumbersCandidates.add(actpno - 1);
			showNumbersCandidates.add(actpno);
			showNumbersCandidates.add(actpno + 1);
			showNumbersCandidates.add(totpno - 1);
			showNumbersCandidates.add(totpno);

			String tmp = "";
			// create previous page link
			String text = "previous"; // INTLANG
			if (actpno > 1) { // previous page exists
				tmp = this.getQueryString(logbookKey, (actpno - 1) + "", rq, logbookKey).getAsHtmlLinkHref();
				result += "<a href=\"" + tmp + "\">" + text + "</a>";
			} else {
				result += text;
			}
			result += "-";

			// create links
			ArrayList<Integer> setLinks = new ArrayList<Integer>();
			Integer before = null;
			for (Integer showNumbersCandidate : showNumbersCandidates) {
				if (setLinks.contains(showNumbersCandidate) || showNumbersCandidate <= 0 || showNumbersCandidate > totpno) {
					continue;
				}
				if (before != null && before + 1 < showNumbersCandidate) {
					result += "...";
				}
				before = showNumbersCandidate;
				if (showNumbersCandidate == actpno) {
					result += "<strong>" + actpno + "</strong>";
				} else {
					tmp = this.getQueryString(logbookKey, showNumbersCandidate + "", rq, logbookKey).getAsHtmlLinkHref();
					result += "<a href=\"" + tmp + "\">" + showNumbersCandidate + "</a>";
				}
				setLinks.add(showNumbersCandidate);
				result += "-";
			}
			// create next page link
			text = "next"; // INTLANG
			if (actpno != totpno) { // next page exists
				tmp = this.getQueryString(logbookKey, (actpno + 1) + "", rq, logbookKey).getAsHtmlLinkHref();
				result += "<a href=\"" + tmp + "\">" + text + "</a>";
			} else {
				result += text;
			}
		}
		return result;
	}

	private QueryString getQueryString(String key, String pageno, HttpServletRequest rq, String logbookKey) {
		return QueryStringBuilder.getLogbookQueryString(key, pageno, this.getCountOfEntriesPerPage(rq, logbookKey) + "");
	}

	/**
	 * return the number of the first entry of the overview.
	 * 
	 * @see LogbookEntriesTrHtml
	 * @see LogbookEntryDao#get(String, int, int)
	 * @param rq
	 *            got
	 * @return the number of the first entry of the overview.
	 */
	private int getShowFromEntry(HttpServletRequest rq, String logbookKey) {
		int pageno = this.getCurrentPageNumber(rq, logbookKey);
		int entrysize = this.getCountOfEntriesPerPage(rq, logbookKey);
		int result = (pageno - 1) * entrysize;
		if (result < 0) { // someone manipulated url
			result = 0;
		}

		return result;
	}

	private boolean getShowSuccessfullPosting(HttpServletRequest rq) {
		boolean result = false;
		String success = rq.getParameter(QueryKeys.QUERY_KEY_POST_REQUEST_SUCCEEDED);
		if (success != null && success.equals(QueryKeys.QUERY_DEFAULT_VALUE_POST_REQUEST_SUCCEEDED)) {
			result = true;
		}
		return result;
	}

	/**
	 * return the number of the last entry of the overview.
	 * 
	 * @see LogbookEntriesTrHtml
	 * @see LogbookEntryDao#get(String, int, int)
	 * @param rq
	 *            got
	 * @return the number of the last entry of the overview.
	 */
	private int getShowToEntry(HttpServletRequest rq, String logbookKey) {
		return this.getShowFromEntry(rq, logbookKey) + this.getCountOfEntriesPerPage(rq, logbookKey);
	}

	private int getCountOfEntriesPerPage(HttpServletRequest rq, String logbookKey) {
		int result = RequestInterpreter.getCountOfEntriesPerPage(rq);
		if (result > LogbookConfigDao.getInstance().getEntryCount(logbookKey)) {
			result = LogbookConfigDao.getInstance().getEntryCount(logbookKey);
		}
		return result;
	}

	private int getTotalPageNumber(HttpServletRequest rq, String logbookKey) {
		int pagesize = this.getCountOfEntriesPerPage(rq, logbookKey);
		int allentriessize = LogbookConfigDao.getInstance().getEntryCount(logbookKey);
		int result = 1;
		if (pagesize > 0) {
			result = Math.round(allentriessize / pagesize);
			if (allentriessize % pagesize != 0) {
				result++;
			}
		}
		return result;
	}

	/**
	 * return true, if it is not a query for the overview page.
	 * 
	 * @param rq
	 *            the request
	 * @return true, if it is not a query for the overview page.
	 */
	private boolean isQueryForLogbookEntriesOfLogbook(HttpServletRequest rq) {
		String logbookKey = rq.getParameter(QueryKeys.QUERY_KEY_LOGBOOK);
		return logbookKey != null && LogbookConfigDao.getInstance().keyExists(logbookKey);
	}
}
