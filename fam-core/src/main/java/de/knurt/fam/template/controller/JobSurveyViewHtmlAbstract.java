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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;

/**
 * a html view for job survey. set the {@link JobDataProcessing} in the
 * constructor. it is simply a valid request, if it is not null.
 * 
 * answer with a complete html-page and include some libs (jquery by now).
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.7.0 (02/17/2012)
 * 
 */
abstract class JobSurveyViewHtmlAbstract implements JobSurveyView {
	private JobDataProcessing jdp = null;
	private List<Job> jobs = null;
	private int step = 0;

	protected void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	protected List<Job> getJobs() {
		return jobs;
	}

	protected void setJdp(JobDataProcessing jdp) {
		this.jdp = jdp;
	}

	protected void setStep(int step) {
		this.step = step;
	}

	protected int getStep() {
		return step;
	}

	/** {@inheritDoc} */
    @Override
	public byte[] getView() {
		String result = "";
		if (this.jdp != null) {
			result = this.jdp.getStructure(step, true, jobs);
			result = result == null ? "" : result;
		}
		return result.getBytes();
	}

	public String getBehaviour() {
		String result = "";
		if (this.jdp != null) {
			result = this.jdp.getBehaviour(step, jobs);
			result = result == null ? "" : result;
		}
		return result;
	}

	/** {@inheritDoc} */
    @Override
	public boolean isValidRequest() {
		return this.jdp != null;
	}

	protected void setStep(HttpServletRequest request) {
		if (request.getParameter("step") != null) {
			try {
				int tmp = Integer.parseInt(request.getParameter("step"));
				if (3 >= tmp && tmp >= 0) {
					this.setStep(tmp);
				}
			} catch (NumberFormatException e) {
			}
		}
	}

}
