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

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.statistics.FamStatistic;
import de.knurt.fam.core.model.config.statistics.FamStatisticContainer;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * controller to delegate differenct statistic views.
 * 
 * @author Daniel Oltmanns
 * @since 0.20091009 (10/09/2009)
 */
public class StatisticsModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		FamStatistic statisticAskedFor = RequestInterpreter.getStatistic(templateResource.getRequest());
		if (statisticAskedFor == null) {
			return this.handleOverviewRequest(templateResource);
		} else {
			return this.handleStatisticRequest(templateResource, statisticAskedFor);
		}
	}

	private Properties handleStatisticRequest(TemplateResource templateResource, FamStatistic statisticAskedFor) {
		Properties result = new Properties();
		QueryString qs = new QueryString();
		qs.add(templateResource.getRequest());
		qs.put("nocache", new Date().getTime() + "");
		result.put("statistic", statisticAskedFor);
		result.putAll(statisticAskedFor.resolveModelAndView(templateResource, qs));
		return result;
	}

	private Properties handleOverviewRequest(TemplateResource templateResource) {
		Properties result = new Properties();
		result.put("direct_access", RequestInterpreter.containsDirectAccess(templateResource.getName(), templateResource.getRequest()));

		// add options for id
		String options = "";
		for (FamStatistic statistic : FamStatisticContainer.getInstance().getAvailableStatistics()) {
			options += String.format("<option value=\"%s\">%s</option>", statistic.getId(), statistic.getLabel());
		}
		result.put("options_id", options);
		result.put("name_id", QueryKeys.QUERY_KEY_OF);

		// add options for facility
		options = "";
		for (Facility facility : FacilityConfigDao.getInstance().getCollectionOfAllConfigured()) {
			if (facility.isBookable()) {
				options += String.format("<option value=\"%s\">%s</option>", facility.getKey(), facility.getLabel());
			}
		}
		result.put("options_facility", options);
		result.put("name_facility", QueryKeys.QUERY_KEY_FACILITY);

		// add options for week
		options = "";
		Calendar today = Calendar.getInstance();
		Calendar pointer = Calendar.getInstance();
		pointer.set(Calendar.DAY_OF_YEAR, 1);
		int yearOfOptions = pointer.get(Calendar.YEAR);
		HtmlElement tmp = null;
		while (pointer.get(Calendar.YEAR) == yearOfOptions) {
			tmp = HtmlFactory.get("option");
			if (today.get(Calendar.WEEK_OF_YEAR) == pointer.get(Calendar.WEEK_OF_YEAR)) {
				tmp.att("selected");
				tmp.add("Todays week"); // INTLANG
				tmp.att("value", -1); // current
			} else {
				tmp.add(pointer.get(Calendar.WEEK_OF_YEAR) + "");
				tmp.att("value", pointer.getTimeInMillis());
			}
			options += tmp.toString();
			pointer.add(Calendar.WEEK_OF_YEAR, 1);
		}
		result.put("name_week", QueryKeys.QUERY_KEY_DAY);
		result.put("options_week", options);

		// add options for time start
		options = "";
		int hour = 0;
		while (hour < 24) {
			tmp = HtmlFactory.get("option");
			if (hour == 0) {
				tmp.att("selected");
				tmp.add("Start of day"); // INTLANG
			} else {
				tmp.add(FamDateFormat.getShortTimeFormatted(hour));
			}
			tmp.att("value", hour);
			options += tmp.toString();
			hour++;
		}
		result.put("name_timestart", QueryKeys.QUERY_KEY_FROM);
		result.put("options_timestart", options);

		// add options for time end
		options = "";
		hour = 1;
		while (hour <= 24) {
			tmp = HtmlFactory.get("option");
			if (hour == 24) {
				tmp.att("selected");
				tmp.add("End of day"); // INTLANG
			} else {
				tmp.add(FamDateFormat.getShortTimeFormatted(hour));
			}
			tmp.att("value", hour);
			options += tmp.toString();
			hour++;
		}
		result.put("name_timeend", QueryKeys.QUERY_KEY_TO);
		result.put("options_timeend", options);
		result.put("name_refresh", QueryKeys.QUERY_KEY_MINUTE_OF_HOUR);
		return result;
	}

}
