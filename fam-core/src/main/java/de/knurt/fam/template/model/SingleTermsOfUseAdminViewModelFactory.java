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

import org.apache.velocity.VelocityContext;

import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Soa;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.VelocityContextFactory;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.heinzelmann.util.velocity.VelocityStringRenderUtil;

/**
 * controller to view a single soa
 * 
 * @author Daniel Oltmanns
 * @since 1.3.1 (12/03/2010)
 */
public class SingleTermsOfUseAdminViewModelFactory {

	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		String soaId = RequestInterpreter.getOf(templateResource.getRequest());
		if (soaId != null) {
			String template = CouchDBDao4Soa.getInstance().getAgreement(soaId);
			VelocityContext userContext = VelocityContextFactory.me().getUser(UserFactory.me().getJoeBloggs());
			String content = VelocityStringRenderUtil.getInstance().getRendered(template, userContext);
			result.put("termsHtml", content);
		} else {
			result.put("termsHtml", "bad request"); // INTLANG
		}
		return result;
	}
}
