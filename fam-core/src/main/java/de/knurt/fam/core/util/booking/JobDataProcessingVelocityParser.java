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
package de.knurt.fam.core.util.booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.jcouchdb.document.Attachment;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.boardunits.JobSurveyFromJobs;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.core.view.text.FamText;
import de.knurt.fam.template.util.TemplateConfig;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;
import de.knurt.heinzelmann.util.velocity.VelocityStringRenderUtil;

/**
 * util to parse velocity for {@link JobDataProcessing} objects
 * 
 * @see JobDataProcessing
 * @author Daniel Oltmanns
 * @since 1.7.0 (01/20/2012)
 */
public class JobDataProcessingVelocityParser {

	public JobDataProcessingVelocityParser(Facility facility, List<Job> jobs) {
		context = new VelocityContext();
		context.put("facility", facility);
		context.put("util", TemplateConfig.me().getUtilities());
		context.put("FamDateFormat", FamDateFormat.class);
		context.put("Math", Math.class);
		context.put("FamText", FamText.class);
		context.put("jobs", jobs);
		context.put("job", new JobSurveyFromJobs().process(jobs));
		List<Properties> attachments = this.getJobFiles(jobs);
		context.put("attachments", attachments);
	}

	// @SuppressWarnings("unchecked")
	private List<Properties> getJobFiles(List<Job> jobs) {
		List<Properties> result = new ArrayList<Properties>();
		if (jobs != null) {
			for (Job job : jobs) {
				Map<String, Attachment> attachments = job.getAttachments();
				if (attachments != null) {
					for (String key : attachments.keySet()) {
						Properties jobOfStep = new Properties();
						jobOfStep.put("step", job.getStep());
						jobOfStep.put("name", key);
						jobOfStep.put("size", attachments.get(key).getLength() + "");
						QueryString qs = QueryStringFactory.get("docid", job.getId());
						qs.put("name", key);
						jobOfStep.put("url", TemplateHtml.me().getHref("jobsurvey", qs));
						result.add(jobOfStep);
					}
				}
			}
		}
		return result;
	}

	public String getParsed(String template) {
		return VelocityStringRenderUtil.getInstance().getRendered(template, context);
	}

	private VelocityContext context = null;

	/**
	 * parse the given json object representing a {@link JobDataProcessing}
	 * object. this is what couchdb does answer.
	 * 
	 * @see CouchDBDao4Jobs#getCurrentJobDataProcessingAsJSONObject(Facility,
	 *      boolean, boolean)
	 * @param jobDataProcessingRepresentation
	 *            {@link JSONObject} representing a {@link JobDataProcessing}
	 * @return true, if the given {@link JSONObject} representing a
	 *         {@link JobDataProcessing} parsed successfuly
	 */
	public boolean parse(JSONObject jobDataProcessingRepresentation) {
		boolean result = false;
		try {
			if (jobDataProcessingRepresentation.has("templates") && !jobDataProcessingRepresentation.isNull("templates")) {
				int i = 0;
				while (i < jobDataProcessingRepresentation.getJSONArray("templates").length()) {
					jobDataProcessingRepresentation.getJSONArray("templates").getJSONObject(i).put("structure", this.getParsed(jobDataProcessingRepresentation.getJSONArray("templates").getJSONObject(i).getString("structure")));
					jobDataProcessingRepresentation.getJSONArray("templates").getJSONObject(i).put("behaviour", jobDataProcessingRepresentation.getJSONArray("templates").getJSONObject(i).getString("behaviour"));
					i++;
				}
			}
			result = true;
		} catch (JSONException e) {
			FamLog.exception(jobDataProcessingRepresentation.toString(), e, 201201200957l);
		}
		return result;
	}

}
