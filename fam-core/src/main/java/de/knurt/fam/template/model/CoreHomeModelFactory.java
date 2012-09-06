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

import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.template.util.QuicksandHtml;

/**
 * produce the model for the home page of the protected area
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.9.0 (09/03/2012)
 */
public class CoreHomeModelFactory {
	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		result.put("jui_tabs", QuicksandHtml.getJuiTabs(templateResource));
		result.put("quicksand_clickitems", QuicksandHtml.getClickItems(templateResource));
		result.put("current_sessions", FamDaoProxy.bookingDao().getCurrentSessions(templateResource.getAuthUser()));
		return result;
	}

}
