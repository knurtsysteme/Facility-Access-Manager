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

import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Soa;
import de.knurt.fam.core.view.html.factory.FamSubmitButtonFactory;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * produce the model for terms of use pages
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/19/2010)
 */
public class EditSoaModelFactory {
	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		if (templateResource.getRequest().getMethod().equals("GET")) {
			// ↓ some easy values
			result.put("button_new", FamSubmitButtonFactory.getAddButton("Insert this Terms of Use Agreement")); // INTLANG
			result.put("overview", this.getOverviewTable());
			result.put("role_selection", this.getRoleSelection()); // INTLANG
			result.put("jhistory", this.getJHistoryTable());
			result.put("jcurrent", this.getJCurrent());

			// ↓ forward jsonvar
			if (templateResource.getRequest().getParameter("jsonvar") != null) {
				// ↖ redirected from post
				result.put("jsonvar", templateResource.getRequest().getParameter("jsonvar"));
			}

			// ↓ set selection for agreements
			QueryString queryString = QueryStringFactory.get("selectid", "existing_agreement_id");
			queryString.put("extraoptionhtml", "<option value=\"-1\" selected=\"selected\">Do not use a draft</option>"); // INTLANG
			result.put("existing_agreements_select", this.getExistingAgreementsSelect(queryString));

			// ↓ set selection for activation of agreements
			queryString = QueryStringFactory.get("selectid", "jactivation_agreements_selection_id");
			queryString.put("selectname", "jactivation_agreements_selection");
			queryString.put("selectspecialoptions", "size=\"5\" multiple=\"multiple\"");
			result.put("jactivation_agreements_selection", this.getExistingAgreementsSelect(queryString));

			// ↓ set step buttons
			int[] steps = { 1, 2, 3, 4, 5 };
			for (int step : steps) {
				HtmlElement button;
				if (step != 1) {
					button = FamSubmitButtonFactory.getBackButton("Back to step " + (step - 1)); // INTLANG
					button.id("button_prev_step" + step);
					result.put("button_prev_step" + step, button);
				}
				if (step != 5) {
					button = FamSubmitButtonFactory.getNextButton("Next to step " + (step + 1)); // INTLANG
					button.id("button_next_step" + step);
					result.put("button_next_step" + step, button);
				}
			}
			result.put("button_activation", FamSubmitButtonFactory.getChangeButton("Set this as new configuration").att("type", "").id("jactivationform_send_button")); // INTLANG
		}
		return result;
	}

	private String getJCurrent() {
		return CouchDBDao4Soa.getInstance().getListOfCurrentSoaActionvationsAsHtml();
	}

	private String getJHistoryTable() {
		return CouchDBDao4Soa.getInstance().getListOfHistorySoaActionvationsAsHtml();
	}

	private HtmlElement getRoleSelection() {
		HtmlElement result = HtmlFactory.get("select").id("role_selection_id").name("role");
		for (Role role : RoleConfigDao.getInstance().getAll()) {
			if (!role.getKey().equals("admin")) {
				result.add(HtmlFactory.get("option", role.getLabel()).name(role.getKey()));
			}
		}
		return result;
	}

	private String getExistingAgreementsSelect(QueryString queryString) {
		return CouchDBDao4Soa.getInstance().getListOfSoasAsHtmlSelect(queryString);
	}

	private String getOverviewTable() {
		return CouchDBDao4Soa.getInstance().getListOfSoasAsHtmlOverview();
	}

}
