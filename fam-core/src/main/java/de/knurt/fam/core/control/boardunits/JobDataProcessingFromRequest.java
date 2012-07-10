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
 */package de.knurt.fam.core.control.boardunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.heinzelmann.util.nebc.BoardUnit;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;

/**
 * return a job data processing object from a given request
 * 
 * @author Daniel Oltmanns
 * @since 1.6.0 (12/07/2011)
 */
public class JobDataProcessingFromRequest implements BoardUnit<HttpServletRequest, JobDataProcessing> {

	private String parameterName;
	private User user;

	/** {@inheritDoc} */
	@Override
	public JobDataProcessing process(HttpServletRequest datum) {
		JSONObject json = new JSONObjectFromRequest().process(datum);
		JobDataProcessing result = null;
		if (json != null) {
			result = new JobDataProcessing();
			try {
				result.setFacilityKey(json.getString("facilityKey"));
				result.setUsername(user.getUsername());
				JSONArray templates = json.getJSONArray("templates");
				List<Map<String, Object>> jdpts = new ArrayList<Map<String, Object>>(4);
				int i = 0;
				while (i < 4) {
					Map<String, Object> jdpt = new HashMap<String, Object>();
					jdpt.put("behaviour", templates.getJSONObject(i).get("behaviour"));
					jdpt.put("structure", templates.getJSONObject(i).get("structure"));
					jdpt.put("step", i);
					jdpts.add(jdpt);
					i++;
				}
				result.setTemplates(jdpts);
			} catch (Exception e) {
				FamLog.info("bad request (url rewriting?) " + e.getClass() + " - " + this.parameterName + " - " + datum.getParameterMap().entrySet(), 201201191546l);
			}
		}
		return result;
	}

	public JobDataProcessingFromRequest(User user) {
		this.user = user;
	}
}
