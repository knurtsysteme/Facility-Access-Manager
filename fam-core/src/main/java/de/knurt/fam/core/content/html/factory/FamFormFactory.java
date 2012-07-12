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
package de.knurt.fam.core.content.html.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.content.text.FamText;
import de.knurt.fam.core.control.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.heinzelmann.ui.html.HtmlButtonFactory;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * produce buttons for different areas of the application without any label.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090624 (06/24/2009)
 */
public class FamFormFactory {

	/**
	 * return a html-select with some time inputs. for each of 1, 2, 3, 4, 5,
	 * 10, 20 an option is generated for minutes, hours, days and weeks. all
	 * values are set in minutes. so we have the option: 1 minute, 2 minutes,
	 * ..., 10 minutes, 20 minutes, 1 hour, 2 hours, ..., 20 hours, ..., 20
	 * weeks.
	 * 
	 * @return a html-select with some time inputs.
	 */
	public static HtmlElement getUnspecifiedTimeInput() {
		Integer[] timesteps = { 1, 2, 3, 4, 5, 10, 20 };
		HtmlElement select_time = HtmlFactory.get_select(QueryKeys.QUERY_KEY_UNITS_TIME);
		// minutes
		for (Integer timestep : timesteps) {
			select_time.add(HtmlFactory.get_option(timestep, FamText.getTimeInput(timestep), false));
		}

		// hours
		for (Integer timestep : timesteps) {
			select_time.add(HtmlFactory.get_option(timestep * 60, FamText.getTimeInput(timestep * 60), timestep == 1));
		}

		// days
		for (Integer timestep : timesteps) {
			select_time.add(HtmlFactory.get_option(timestep * 60 * 24, FamText.getTimeInput(timestep * 60 * 24), false));
		}

		// weeks
		for (Integer timestep : timesteps) {
			select_time.add(HtmlFactory.get_option(timestep * 60 * 24 * 7, FamText.getTimeInput(timestep * 60 * 24 * 7), false));
		}

		return select_time;
	}

	/**
	 * return a form containing the hidden inputs and the given contents.
	 * 
	 * @param hiddenInputs
	 *            added into the form
	 * @param contents
	 *            of the form
	 * @return a form containing the hidden inputs and the given contents.
	 */
	public static HtmlElement getForm(QueryString hiddenInputs, String contents) {
		HtmlElement result = getForm();
		result.add("<p>" + hiddenInputs.getAsHtmlInputsTypeHidden() + "</p>");
		result.add("<p>" + contents + "</p>");
		return result;
	}

	/**
	 * return a delete button as form. the delete button contains
	 * <code>qs</code> as hidden input. the button has one the given size.
	 * 
	 * @param qs
	 *            query string for the hidden input. the hidden input commonly
	 *            should explain, what to delete.
	 * @param size
	 *            one of the delete image size
	 * @return a delete button as form.
	 */
	public static HtmlElement getDeleteButtonAsForm(QueryString qs, int size) {
		qs.put(QueryKeys.QUERY_KEY_DELETE, "true");
		HtmlElement form = HtmlFactory.get("form");
		form.addClassName("deleteButton");
		form.setAttribute("action", "");
		form.setAttribute("method", "post");
		HtmlElement button = FamSubmitButtonFactory.getButton("Delete");
		button.setAttribute("title", "cancel!"); // INTLANG
		button.setAttribute("type", "submit");
		return HtmlButtonFactory.getButtonAsForm(qs, form, button);
	}

	/**
	 * return a disabled delete button as form, if disabled is true. if form is
	 * disabled, it is just a pseudo form.
	 * 
	 * @see #getDeleteButtonAsForm(de.knurt.heinzelmann.util.query.QueryString,
	 *      int)
	 * @param qs
	 *            in case of <code>disabled == true</code> it is the query
	 *            string for the hidden input. the hidden input commonly should
	 *            explain, what to delete. if form is disabled, this is ignored.
	 * @param size
	 *            one of the delete image size
	 * @param disabled
	 *            if true, the form is a pseudo form. then
	 *            {@link FamSubmitButtonFactory#getDisabledButtonWithPic(java.lang.Object)}
	 *            is used.
	 * @return a disabled delete button as form, if disabled is true.
	 */
	public static HtmlElement getDeleteButtonAsForm(QueryString qs, int size, boolean disabled) {
		if (disabled) {
			HtmlElement result = HtmlFactory.get("button", "delete");
			result.addTitleAttribute("delete"); // INTLANG
			result.att("disabled");
			return result;
		} else {
			return getDeleteButtonAsForm(qs, size);
		}
	}

	/**
	 * return a delete button for admins. for admins, there are more options
	 * then just deleting something. by now, there is always a reason to do that
	 * - so a reason text input field exists.
	 * 
	 * @param qs
	 *            string being part of the form. must contain value of
	 *            {@link QueryKeys#QUERY_KEY_BOOKING}
	 * @param size
	 *            for the deleting button
	 * @param disabled
	 *            true, if the deleting button is nothing as a disabled button.
	 * @return a delete button for admins.
	 */
	public static HtmlElement getAdminDeleteButtonAsForm(QueryString qs, int size, boolean disabled) {
		HtmlElement result = getDeleteButtonAsForm(qs, size, disabled);
		if (!disabled) {
			// rebuild content of form
			ArrayList<Object> contents = new ArrayList<Object>();
			contents.add(getInputReasonForCancelation(Integer.parseInt(qs.get(QueryKeys.QUERY_KEY_BOOKING)), "Reason for cancelation (optional):")); // INTLANG
			contents.addAll(result.getContents());
			result.setContents(contents);
		}
		return result;
	}

	/**
	 * return text input field for the individual input of a cancelation reason.
	 * 
	 * @param idOfBooking
	 *            the cancelation is for
	 * @param label
	 *            for the input field.
	 * @return text input field for the individual input of a cancelation
	 *         reason.
	 */
	public static String getInputReasonForCancelation(int idOfBooking, String label) {
		// create reason text input
		String reasonhtml = "<label for=\"reason_%s\">" + label + ":</label><br /><input type=\"text\" name=\"%s\" id=\"reason_%s\" value=\"\" />";
		return String.format(reasonhtml, idOfBooking, QueryKeys.QUERY_KEY_TEXT_NOTICE, idOfBooking);
	}

	/**
	 * create 2 forms. one for a yes input. another for a no input. the main
	 * different of the forms are the key {@link QueryKeys#QUERY_KEY_YES_NO} set
	 * to {@link QueryKeys#YES} and {@link QueryKeys#NO}. you can set further
	 * hidden values for both and html elements setting before the yes no
	 * buttons.
	 * 
	 * @param hiddenInputs
	 *            set as hidden input on both forms
	 * @param activate
	 *            decide the disabled buttons. if value is ...
	 *            <ul>
	 *            <li>... <code>true</code>: enable "yes", disable "no"</li>
	 *            <li>... <code>false</code>: disable "yes", enable "no"</li>
	 *            <li>... <code>null</code>: enable "yes" and "no"</li>
	 *            </ul>
	 *            never disable both
	 * @param altYes
	 *            text for the alt attribute of the image tag vor yes button
	 * @param altNo
	 *            text for the alt attribute of the image tag vor no button
	 * @param more_html
	 *            further form content setting over the yes no buttons
	 * @return two yes no forms
	 */
	public static HtmlElement getYesNoForm(QueryString hiddenInputs, Boolean activate, String altYes, String altNo, String more_html) {
		// base
		HtmlElement form = getForm();
		HtmlElement table = HtmlFactory.get("table");
		table.addClassName("yesNoForms");
		HtmlElement tr = HtmlFactory.get("tr");

		// set current value
		boolean yesIsActive = activate == null ? true : activate.booleanValue();
		boolean noIsActive = activate == null ? true : !activate.booleanValue();

		// yes cell
		HtmlElement yes_td = HtmlFactory.get("td");
		yes_td.addClassName("yesForm");
		HtmlElement yes_button = HtmlFactory.get("button", "yes");
		yes_button.setAttribute("type", "submit");
		yes_button.addTitleAttribute(altYes);
		if (yesIsActive) {
			yes_button.setAttribute("name", QueryKeys.QUERY_KEY_YES_NO);
			yes_button.setAttribute("value", QueryKeys.YES);
			yes_button.setId(hiddenInputs.hashCode() + "_yes_id");
			yes_td.add(yes_button);
		} else {
			yes_button.att("disabled");
			yes_td.add(yes_button);
		}

		// no cell
		HtmlElement no_td = HtmlFactory.get("td");
		no_td.addClassName("noForm");
		HtmlElement no_button = HtmlFactory.get("button", "no");
		no_button.addTitleAttribute(altNo);
		if (noIsActive) {
			no_button.setAttribute("name", QueryKeys.QUERY_KEY_YES_NO);
			no_button.setAttribute("value", QueryKeys.NO);
			no_button.setId(hiddenInputs.hashCode() + "_no_id");
			no_td.add(no_button);
		} else {
			no_button.att("disabled");
			no_td.add(no_button);
		}

		// put it all together
		form.add(hiddenInputs.getAsHtmlInputsTypeHidden());
		form.add(more_html);
		tr.add(yes_td);
		tr.add(no_td);
		table.add(tr);
		form.add(table);
		return form;
	}

	private static HtmlElement getForm() {
		HtmlElement result = HtmlFactory.get("form");
		result.setAttribute("action", "");
		result.setAttribute("method", "post");
		return result;
	}

	/**
	 * return a form to change user's role and to barre user. in case of user is
	 * an administrator, just return text "not changeable".
	 * 
	 * @param user
	 *            the form is for
	 * @return a form to change user's role and to barre user.
	 */
	public static HtmlElement getFormChangeUser(User user) {
		if (user.isAdmin()) { // not changeable
			return HtmlFactory.get("p", "admin role not changeable"); // INTLANG
		} else {
			HtmlElement result = getForm();
			String idbase = user.getUsername() + "_id";

			// select role
			Properties roleoptions = new Properties();
			List<Role> roles = RoleConfigDao.getInstance().getAll();
			for (Role role : roles) {
				if (!role.getKey().equals(RoleConfigDao.getInstance().getAdminId())) {
					roleoptions.setProperty(role.getKey(), role.getLabel());
				}
			}
			HtmlElement rolelabel = HtmlFactory.get_label(QueryKeys.QUERY_KEY_ROLE + idbase, "Change role:"); // INTLANG
			HtmlElement roleselect = HtmlFactory.get_select_with_options(QueryKeys.QUERY_KEY_ROLE, roleoptions, user.getRoleLabel());
			roleselect.setId(QueryKeys.QUERY_KEY_ROLE + idbase);

			// hidden input
			QueryString qs = new QueryString();
			qs.put(QueryKeys.QUERY_KEY_USER, user.getUsername());

			// option exclude
			HtmlElement deletelabel = HtmlFactory.get_label(QueryKeys.QUERY_KEY_DELETE + idbase, "barred"); // INTLANG
			HtmlElement deletecheckbox = HtmlFactory.get_input_checkbox(QueryKeys.QUERY_KEY_DELETE, user.isExcluded());
			deletecheckbox.setId(QueryKeys.QUERY_KEY_DELETE + idbase);

			// submit button
			HtmlElement submitbutton = FamSubmitButtonFactory.getNextButton("submit");
			submitbutton.setId("submit" + idbase);

			// put it together
			HtmlElement rolep = HtmlFactory.get("p");
			rolep.add(rolelabel);
			rolep.add("<br />");
			rolep.add(roleselect);

			HtmlElement deletep = HtmlFactory.get("p");
			deletep.add(deletecheckbox + "&nbsp;" + deletelabel);

			HtmlElement submitp = HtmlFactory.get("p");
			submitp.add(submitbutton);

			result.add(qs.getAsHtmlInputsTypeHidden());
			result.add(rolep);
			result.add(deletep);
			result.add(submitp);

			return result;
		}
	}

	/**
	 * return a form containing the hidden inputs and the given contents.
	 * 
	 * @see #getForm(de.knurt.heinzelmann.util.query.QueryString,
	 *      java.lang.String)
	 * @param hiddenInputs
	 *            added into the form
	 * @param content
	 *            of the form
	 * @return a form containing the hidden inputs and the given contents.
	 */
	public static HtmlElement getForm(QueryString hiddenInputs, HtmlElement content) {
		return getForm(hiddenInputs, content.toString());
	}
}
