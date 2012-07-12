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
package de.knurt.fam.core.content.adapter.html;

import java.util.List;

import de.knurt.fam.core.content.html.factory.FamFormFactory;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.template.util.HtmlAdapterAddress;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * adapt user for nice looking html.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@Deprecated
public class HtmlAdapterUser extends FamHtmlAdapter<User> {

	private User user;

	/**
	 * construct me
	 * 
	 * @param user
	 *            to adapt
	 */
	public HtmlAdapterUser(User user) {
		super(user);
		this.user = user;
	}

	/**
	 * return a form to verify a user account. this is for admins activating
	 * user accounts.
	 * 
	 * @return a form to verify a user account.
	 */
	public HtmlElement getFormVerifyAccountUser() {
		QueryString hiddenInputs = new QueryString();
		hiddenInputs.put(QueryKeys.QUERY_KEY_USER, this.user.getUsername());
		String altYes = "activate account for " + this.user.getFullName();
		String altNo = "delete user " + this.user.getFullName();
		return FamFormFactory.getYesNoForm(hiddenInputs, null, altYes, altNo, "");
	}

	/**
	 * return form to change a users something. same as
	 * {@link FamFormFactory#getFormChangeUser(de.knurt.fam.core.model.persist.User)}
	 * 
	 * @see FamFormFactory#getFormChangeUser(de.knurt.fam.core.model.persist.User)
	 * @return form to change a users something.
	 */
	public HtmlElement getFormChangeUser() {
		return FamFormFactory.getFormChangeUser(this.user);
	}

	/**
	 * return a information if the account of the adapted user is active.
	 * 
	 * @return a information if the account of the adapted user is active.
	 */
	public HtmlElement getAccountStatusActive() {
		HtmlElement result = HtmlFactory.get("p");
		result.add(FamText.getUserStatus(this.user));
		return result;
	}

	/**
	 * return a list of facilities, the user is responsible for. this does not
	 * look at the role of the user and if he has the right to administrate this
	 * facilities.
	 * 
	 * @return a list of facilities, the user is responsible for.
	 */
	public HtmlElement getResponsibleForFacilities() {
		List<String> facilityKeys = FamDaoProxy.facilityDao().getFacilityKeysUserIsResponsibleFor(this.user);
		if (facilityKeys != null) {
			HtmlElement result = HtmlFactory.get("ul");
			for (String facilityKey : facilityKeys) {
				result.add(HtmlFactory.get("li").add(FacilityConfigDao.getInstance().getLabel(facilityKey)));
			}
			result.addClassName("asList");
			return result;
		} else {
			return HtmlFactory.get("p", "");
		}
	}

	/**
	 * return the full name of the user
	 * 
	 * @see User#getFullName()
	 * @return the fullname
	 */
	public String getFullname() {
		return user.getFullName();
	}

	/**
	 * return the username
	 * 
	 * @see User#getUsername()
	 * @return the username
	 */
	public String getUsername() {
		return user.getUsername();
	}

	/**
	 * return the username as mailto-link and username as content.
	 * 
	 * @return the username as mailto-link and username as content.
	 */
	public HtmlElement getUsernameAsMail() {
		return HtmlFactory.get_a_mailto(this.user.getMail(), this.user.getUsername());
	}

	/**
	 * return the full user as content with username in brackets and mail linked
	 * in a mailto-link.
	 * 
	 * @return the full username as content with username in brackets and mail
	 *         linked in a mailto-link.
	 */
	public HtmlElement getFullUserAsHtml() {
		String recipient = String.format("%s (%s)", this.user.getFullName(), this.user.getUsername());
		return HtmlFactory.get_a_mailto(this.user.getMail(), recipient);
	}

	/**
	 * return the mail address of the user
	 * 
	 * @return the mail
	 */
	public String getMail() {
		return user.getMail();
	}

	/**
	 * return the full user as content and mail linked in a mailto-link.
	 * 
	 * @return the full user as content and mail linked in a mailto-link.
	 */
	public HtmlElement getMailWithFullNameAsHtml() {
		return HtmlFactory.get_a_mailto(this.user.getMail(), this.user.getFullName());
	}

	/**
	 * return a summary of contact details for the user being adapted. this does
	 * not look at the rights of the current logged in user. contact details are
	 * name of user, phone numbers and email addresses.
	 * 
	 * @return a summary of contact details for the user being adapted.
	 */
	public HtmlElement getContactAsHtml() {
		HtmlElement table = new HtmlAdapterAddress(this.user.getMainAddress()).getFullAsHtml();
		if (table == null) {
			table = super.getHtmlTable();
		}
		table.add(HtmlFactory.getInstance().get_tr("mail", HtmlFactory.get_a_mailto(this.user.getMail(), this.user.getMail()))); // INTLANG
		if (this.user.hasPhone1()) {
			table.add(HtmlFactory.getInstance().get_tr("phone number (landline)", this.user.getPhone1())); // INTLANG
		}
		if (this.user.getPhone2() != null && this.user.getPhone2().trim().isEmpty() == false) {
			table.add(HtmlFactory.getInstance().get_tr("phone number (mobile)", this.user.getPhone2())); // INTLANG
		}
		return table;
	}

	/**
	 * return the date of the registration of the user with time.
	 * 
	 * @return the date of the registration of the user with time.
	 */
	public String getDatereg() {
		return FamDateFormat.getDateFormattedWithTime(user.getRegistration());
	}

	/**
	 * return the date of the last login of the user with time.
	 * 
	 * @return the date of the last login of the user with time.
	 */
	public String getDatelastlogin() {
		return user.getLastLogin() == null ? "no login by now" : FamDateFormat.getDateFormattedWithTime(user.getLastLogin()); // INTLANG
	}

	/**
	 * return the id of the role of the user
	 * 
	 * @return the id of the role of the user
	 */
	public String getRole() {
		return user.getRoleId();
	}

	/**
	 * return a user with full name and username and a link as given.
	 * 
	 * @param link2format
	 *            a link set as href. the given link is called with
	 *            {@link String#format(String, Object...)} where
	 *            <code>Object...</code> is users' id.
	 * @return a user with full name and username and a link as given.
	 */
	public HtmlElement getUserLink(String link2format) {
		String recipient = String.format("%s (%s)", this.user.getFullName(), this.user.getUsername());
		String href = String.format(link2format, this.user.getId());
		return HtmlFactory.get_a(href, recipient);
	}
}
