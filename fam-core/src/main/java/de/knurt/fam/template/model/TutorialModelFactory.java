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

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 * produce the model for tutorials
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/21/2010)
 */
public class TutorialModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		result.put("n", this.getRequestedTutorial(templateResource.getRequest()));
		return result;
	}

	private int getRequestedTutorial(HttpServletRequest rq) {
		int result = -1;
		try {
			result = Integer.parseInt(rq.getParameter("n"));
		} catch (NumberFormatException e) {
			// OK!
		}
		return result;
	}

}
