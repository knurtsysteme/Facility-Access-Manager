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
import java.util.List;
import java.util.Properties;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.util.mvc.RedirectResolver;

/**
 * deliver a model for a page where operators can create and change things to be
 * recorded for a job
 * 
 * @author Daniel Oltmanns
 * @since 1.6.0 (11/23/2011)
 */
public class ConfigJobSurveyModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		List<Facility> facilities = new ArrayList<Facility>();
		if (templateResource.getAuthUser().isAdmin()) {
			facilities = FacilityConfigDao.getInstance().getAll();
		} else {
			facilities = templateResource.getAuthUser().getFacilitiesUserIsResponsibleFor();
		}
		if (facilities.size() > 0) {
			result.put("facilities", facilities);
			String requestingFacility = templateResource.getRequest().getParameter("facility_choosen");
			Facility facilityChoosen = requestingFacility == null ? facilities.get(0) : FacilityConfigDao.facility(requestingFacility);
			result.put("facility_choosen", facilityChoosen);
			JobDataProcessing jdp = CouchDBDao4Jobs.me().getCurrentJobDataProcessing(facilityChoosen, true);
			result.put("has_job_data_processing", jdp != null);
			if (jdp != null) {
				result.put("job_data_processing", jdp);
			}
		} else {
			RedirectResolver.redirectClient(RedirectTarget.PROTECTED_HOME, templateResource);
		}
		return result;
	}

}