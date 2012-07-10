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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.bu.File2Attachment;
import de.knurt.heinzelmann.util.nebc.bu.JSONObject2Map;
import de.knurt.heinzelmann.util.validation.AssertOrException;

/**
 * a job of a booking.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.6.0 (23/11/2011)
 */
public class Job extends FamBaseDocument implements FamDocument {

	public FamDocumentType getType() {
		return FamDocumentType.JOB;
	}

	@SuppressWarnings("unchecked")
	private Map jobSurvey = null;
	private int jobId = 0;
	private String username = null;
	private String idJobDataProcessing = null;
	private int step = 0;

	/**
	 * step marking the user request
	 */
	public final static int STEP_USER_REQUEST = 0;

	/**
	 * step marking the user request in the review
	 */
	public final static int STEP_USER_REQUEST_REVIEW = 1;

	/**
	 * step marking the operator feedback
	 */
	public final static int STEP_OPERATOR_FEEDBACK = 2;

	/**
	 * step marking the operator feedback in the review
	 */
	public final static int STEP_OPERATOR_FEEDBACK_REVIEW = 3;

	public Job() {
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getStep() {
		return step;
	}

	public boolean insertOrUpdate() {
		AssertOrException.assertTrue(jobId != 0);
		AssertOrException.assertTrue(step >= 0 && step <= 4);
		AssertOrException.notNull(idJobDataProcessing);
		AssertOrException.notNull(username);
		Job existing = FamDaoProxy.jobsDao().getJob(jobId, step);
		if (existing != null) {
			return FamDaoProxy.docDao().updateDocument(this, existing);
		} else {
			return FamDaoProxy.docDao().createDocument(this);
		}
	}

	/**
	 * return the user inputs.
	 */
	@SuppressWarnings("unchecked")
	public Map getJobSurvey() {
		return jobSurvey;
	}

	@SuppressWarnings("unchecked")
	public void setJobSurvey(Map jobSurvey) {
		this.jobSurvey = jobSurvey;
	}

	public void addAttachments(List<File> attachments) {
		for (File attachment : attachments) {
			this.addAttachment(attachment.getName(), new File2Attachment().process(attachment));
		}
	}

	/**
	 * return the job id. the job id of a job is equal to the id of the booking
	 * made for the job.
	 * 
	 * @see Booking#getId()
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * for mapping reasons
	 * 
	 * @see #getJobId()
	 * @param jobId
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	/**
	 * username filled in the job requirements.
	 * 
	 * @see User#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * the id of the form template ({@link JobDataProcessing}) filled out. this
	 * is the id of the couchdb document as well.
	 * 
	 * @see #getJobDataProcessing
	 * @see JobDataProcessing#getId()
	 */
	public String getIdJobDataProcessing() {
		return idJobDataProcessing;
	}

	public void setIdJobDataProcessing(String idJobDataProcessing) {
		this.idJobDataProcessing = idJobDataProcessing;
	}

	public void setJobSurvey(JSONObject jobsurvey) {
		this.setJobSurvey(new JSONObject2Map().process(jobsurvey));
	}

}
