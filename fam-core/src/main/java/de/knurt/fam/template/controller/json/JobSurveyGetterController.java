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
package de.knurt.fam.template.controller.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;

/**
 * return job surveys.
 * 
 * @author Daniel Oltmanns
 * @since 1.7.0 (01/19/2012)
 */
public class JobSurveyGetterController extends JSONController {

	public JobSurveyGetterController() {
	}

	private JSONObject getJSONObject(JSONObject rq) {
		JSONObject result = new JSONObject();
		boolean succ = false;
		boolean has_job_data_processing = false;
		try {
			boolean useParent = rq.has("useParent") ? rq.getBoolean("useParent") : true;
			boolean parseVelocity = rq.has("parseVelocity") ? rq.getBoolean("parseVelocity") : true;
			String facilityKey = rq.getString("facilityKey");
			if (rq != null && facilityKey != null && FacilityConfigDao.facility(facilityKey) != null) {
				result = CouchDBDao4Jobs.me().getActualJobDataProcessingAsJSONObject(FacilityConfigDao.facility(facilityKey), null, useParent, parseVelocity);
				if (result == null) {
					FamLog.error("did not find " + facilityKey, 201201191632l);
					result = new JSONObject();
				} else {
					succ = true;
					has_job_data_processing = true;
				}
			}
			result.put("succ", succ);
			result.put("has_job_data_processing", has_job_data_processing);
		} catch (JSONException e) {
			FamLog.exception(e, 201201191556l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		return this.getJSONObject(new JSONObjectFromRequest().process(rq));
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201201191600l);
	}

}
