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

import org.jcouchdb.document.Attachment;

import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.Job;

/**
 * response with a download of a job survey attachment
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (04/20/2012)
 * 
 */
class JobSurveyViewAttachment implements JobSurveyView {

	private String filename = null;
	private byte[] view = null;
	private Attachment attachment = null;
	private Job job = null;
	private User user = null;
	private HttpServletResponse response = null;

	public JobSurveyViewAttachment(User user, HttpServletRequest request, HttpServletResponse response) {
		this.user = user;
		this.response = response;
		try {
			String docid = request.getParameter("docid");
			filename = request.getParameter("name");
			job = FamCouchDBDao.getInstance().getOne(docid, Job.class);
			attachment = job.getAttachments().get(filename);
			view = FamCouchDBDao.getInstance().getAttachment(docid, filename);
		} catch (Exception e) {
			// ok - it is not valid
		}
	}

	/** {@inheritDoc} */
    @Override
	public byte[] getView() {
		this.response.setHeader("Content-Disposition", "attachment; filename=" + this.filename);
		this.response.setContentType(attachment.getContentType());
		return view;
	}

	/** {@inheritDoc} */
    @Override
	public boolean isValidRequest() {
		return view != null && attachment != null && job != null && user != null && (job.getUsername().equals(user.getUsername()) || user.hasAdminTasks());
	}
}
