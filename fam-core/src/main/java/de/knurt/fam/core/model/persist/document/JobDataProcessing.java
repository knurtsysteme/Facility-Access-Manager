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
package de.knurt.fam.core.model.persist.document;

import java.util.List;
import java.util.Map;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.booking.JobDataProcessingVelocityParser;
import de.knurt.heinzelmann.util.shopping.Purchasable;

/**
 * a job survey of a facility defining what is recorded for a job ({@link Job}
 * on this facility.
 * 
 * @see Job
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.6.0 (23/11/2011)
 */
public class JobDataProcessing extends FamBaseDocument implements FamDocument, Purchasable {

	private String facilityKey, username;
	private List<Map<String, Object>> templates;

	public FamDocumentType getType() {
		return FamDocumentType.JOB_DATA_PROCESSING;
	}

	public boolean insertOrUpdate() {
		return FamDaoProxy.docDao().createDocument(this);
	}

	public String getFacilityKey() {
		return facilityKey;
	}

	public void setFacilityKey(String facilityKey) {
		this.facilityKey = facilityKey;
	}

	public List<Map<String, Object>> getTemplates() {
		return templates;
	}

	public void setTemplates(List<Map<String, Object>> templates) {
		this.templates = templates;
	}

	/**
	 * return the username of creating this document
	 * 
	 * @return the username of creating this document
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBehaviour(int step, List<Job> jobs) {
		return this.getStringValueOfStep(step, false, "behaviour", jobs);
	}

	private String getStringValueOfStep(int step, boolean parseValocity, String behOrStr, List<Job> jobs) {
		try {
			if (this.getTemplates().get(step).get(behOrStr) == org.json.JSONObject.NULL || this.getTemplates().get(step).get(behOrStr).toString().equals("{}")) {
				return null;
			} else {
				String result = this.getTemplates().get(step).get(behOrStr).toString();
				if (parseValocity) {
					result = new JobDataProcessingVelocityParser(FacilityConfigDao.facility(this.getFacilityKey()), jobs).getParsed(result);
				}
				return result;
			}
		} catch (NullPointerException e) {
			return null;
		} catch (Exception e) {
			// i do not know exception
			FamLog.exception(e, 201112161008l);
			return null;
		}
	}

	public String getStructure(int step, boolean parseValocity, List<Job> jobs) {
		return this.getStringValueOfStep(step, parseValocity, "structure", jobs);
	}

	@Override
	public String getArticleNumber() {
		return "jobdataprocessing_" + this.username + "_" + this.getFacilityKey();
	}

	@Override
	public boolean purchase() {
		return false;
	}

	public String getStructure(int step, boolean parseValocity) {
		return this.getStructure(step, parseValocity, null);
	}

	public String getBehaviour(int step) {
		return this.getBehaviour(step, null);
	}

}
