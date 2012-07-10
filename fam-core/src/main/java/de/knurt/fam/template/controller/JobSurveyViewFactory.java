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
 */package de.knurt.fam.template.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.knurt.fam.core.model.persist.User;

/**
 * simple factory to 
 * produce {@link JobSurveyView}s in context of the request.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (04/20/2012)
 * 
 */
final class JobSurveyViewFactory {

	public static JobSurveyView getJobSurveyView(User user, HttpServletRequest request, HttpServletResponse response) {
		if(request.getParameter("docid") != null) {
			return new JobSurveyViewAttachment(user, request, response);
		} else {
			return new JobSurveyViewHtml(user, request, response);
		}
	}


}