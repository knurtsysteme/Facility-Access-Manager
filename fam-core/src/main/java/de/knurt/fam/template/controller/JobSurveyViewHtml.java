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
package de.knurt.fam.template.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Jobs;

/**
 * a html view for job surveys of a specific step and for a specific facility.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.7.0 (02/16/2012)
 * 
 */
public class JobSurveyViewHtml extends JobSurveyViewHtmlAbstract {
	private User user;
	private int jobid = -1;

	/**
	 * construct the data model
	 */
	public JobSurveyViewHtml(User authUser, HttpServletRequest request, HttpServletResponse response) {
		boolean isRequestForTemplate = false;
		try {
			this.jobid = Integer.parseInt(request.getParameter("jobid"));
		} catch (NumberFormatException nfe) {
			isRequestForTemplate = true;
		}
		this.user = authUser;
		boolean useParent = true;
		if (request.getParameter("useParent") != null) {
			try {
				useParent = Boolean.parseBoolean(request.getParameter("useParent"));
			} catch (Exception e) {
			}
		}
		this.setStep(request);

		JobDataProcessing jdp = null;
		if (isRequestForTemplate) {
			Facility facility = FacilityConfigDao.facility(request.getParameter("facility"));
			jdp = CouchDBDao4Jobs.me().getCurrentJobDataProcessing(facility, useParent);
		} else if (this.jobid > 0) {
			// it is a request for a user input
			try {
				jdp = CouchDBDao4Jobs.me().getCurrentJobDataProcessing(FamDaoProxy.bookingDao().getBookingWithId(jobid).getFacility(), useParent);
			} catch (Exception e) {
				// OK!
			}
			this.setJobs(CouchDBDao4Jobs.me().getJobs(jobid));
		}
		if (this.isValidRequest_intern(jdp)) {
			this.setJdp(jdp);
		}
	}

	private boolean isValidRequest_intern(JobDataProcessing jdp) {
		boolean result = jdp != null && jdp.getUsername() != null;
		if (result) {
			if (this.getStep() == 2) {
				// feedback given by operator (and only by operator)
				result = user.hasAdminTasks();
			} else {
				// either an operator views a template or the job must be
				// owned by auth user or a job does not exist
				boolean userOwnedFirstJob = false;
				if (this.getJobs() != null) {
					Job job2check = null;
					for (Job job : this.getJobs()) {
						if (job.getStep() == 0) {
							job2check = job;
							break;
						}
					}
					userOwnedFirstJob = job2check != null && job2check.getUsername().equals(user.getUsername());
				}
				result = this.getJobs() == null || userOwnedFirstJob || user.hasAdminTasks();
			}
		}
		return result;
	}

}
