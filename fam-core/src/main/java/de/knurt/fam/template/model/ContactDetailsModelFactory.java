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

import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.content.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.KnownDepartmentConfigDao;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.template.util.ContactDetailsRequestHandler;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
/**
 * produce the model for terms of use pages
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/19/2010)
 */
public class ContactDetailsModelFactory {

    @SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	public Properties getProperties(TemplateResource templateResource) {
		Properties result = new Properties();
		User userauth = templateResource.getAuthUser();
		if (userauth != null) {
			if (KnownDepartmentConfigDao.requestDepartments()) {
				result.put("requestDepartment", true);
				result.put("knownDepartments", KnownDepartmentConfigDao.getInstance().getDepartments());
			} else {
				result.put("requestDepartment", false);
				result.put("knownDepartments", null);
			}
			User user2change = this.getUserToChange(templateResource);
			result.put("status", FamText.getUserStatus(user2change));
			result.put("show_status", userauth.isAdmin() ? "t" : "f");
			result.put("user2change", user2change);
			Address usersAddress = user2change.getMainAddress();
			if(usersAddress == null) {
				usersAddress = new Address();
			}
			result.put("address", usersAddress);
			result.put("summary", new HtmlElement("div").setId("contactDetailsSummary_container").add(ContactDetailsRequestHandler.getSummaryTable(user2change)));
			HtmlElement addButton = FamSubmitButtonFactory.getAddButton("Add something else");
			addButton.addClassName("js_show").addClassName("js_add").doNotDisplay();
			result.put("addButton", addButton);
			if (templateResource.getAuthUser().isAdmin()) {
				HtmlElement form = HtmlFactory.get_form("get", TemplateHtml.href("contactdetails"));
				HtmlElement select = HtmlFactory.get_select("user_id");
				List<User> usersWithAccount = FamDaoProxy.userDao().getAllUsersWithAccount();
				for (User userWithAccount : usersWithAccount) {
					boolean selected = userWithAccount.getId().equals(user2change.getId());
					String optionLabel = String.format("%s (Status: %s)", userWithAccount.getFullName(), FamText.getUserStatus(userWithAccount)); // INTLANG
					select.add(HtmlFactory.get_option(userWithAccount.getId(), optionLabel, selected));
				}
				form.add(select);
				form.add(HtmlFactory.get("br"));
				form.add(FamSubmitButtonFactory.getChangeButton("View this user")); // INTLANG
				form.setId("changeUser");
				result.put("changeUser_form", "<h1>" + "View another user" + "</h1>" + form); // INTLANG
				result.put("changeUser_href", HtmlFactory.get_a("#changeUser", "View another user")); // INTLANG
			} else {
				result.put("changeUser_href", "");
				result.put("changeUser_form", "");
			}
			result.put("existingContactDetails", user2change.getContactDetails());
		}
		return result;
	}

	private User getUserToChange(TemplateResource templateResource) {
		User result = ContactDetailsRequestHandler.getUserOfRequest(templateResource.getRequest());
		if (result == null) {
			User example = UserFactory.me().blank();
			example.setId(templateResource.getAuthUser().getId());
			result = FamDaoProxy.userDao().getOneLike(example);
		}
		return result;
	}



}
