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

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;

/**
 * a html view for job survey of a specific step and a article number the
 * jobsurvey has (and is stored in users session before).
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.7.0 (02/16/2012)
 * 
 */
public class JobSurveyViewPreviewHtml extends JobSurveyViewHtmlAbstract {
	public JobSurveyViewPreviewHtml(User authUser, HttpServletRequest request, HttpServletResponse response) {
		this.setStep(request);
		try {
			String articleno = request.getParameter("articleno");
			JobDataProcessing jdp = (JobDataProcessing) authUser.getShoppingCart().getArticle(articleno);
			if (jdp != null) {
				this.setJdp(jdp);
			}
		} catch (Exception e) {
			FamLog.debug("invalid request", 201202170849l);
		}
	}

}
