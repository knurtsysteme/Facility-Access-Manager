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
package de.knurt.fam.template.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.EmailValidator;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.config.Department;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.KnownDepartmentConfigDao;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.core.util.mvc.validator.InvalidRoleIdException;
import de.knurt.fam.core.util.mvc.validator.MandatoryUserFieldValidator;
import de.knurt.fam.core.view.html.factory.FamFormFactory;
import de.knurt.fam.core.view.html.factory.FamSubmitButtonFactory;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * like {@link RequestInterpreter} this interprets requests, but especially
 * for the contact detail page.
 * 
 * @author Daniel Oltmanns
 * @since 0.20100130 (01/30/2009)
 */
public class ContactDetailsRequestHandler {

	/**
	 * return the user that is in the request or null, if no user found.
	 * 
	 * @param rq
	 *            request got
	 * @return the user that is in the request or null, if no user found.
	 */
	public static User getUserOfRequest(HttpServletRequest rq) {
		User result = null;
		Integer user_id = null;
		try {
			user_id = Integer.parseInt(rq.getParameter("user_id"));
		} catch (NumberFormatException e) {
		}
		if (user_id != null) {
			User example = UserFactory.getInstance().blank();
			example.setId(user_id);
			result = FamDaoProxy.userDao().getOneLike(example);
		}
		return result;
	}

	/**
	 * return the value with given key in given request, if it is set and not
	 * empty.
	 * 
	 * @param rq
	 *            request got
	 * @param key
	 *            interested in
	 * @return the value with given key in given request, if it is set and not
	 *         empty.
	 */
	public static String getValue(HttpServletRequest rq, String key) {
		String result = rq.getParameter(key) == null ? null : rq.getParameter(key).trim();
		return result == null || result.isEmpty() ? null : result;
	}

	/**
	 * return true, if the request is valid. the request is valid, if the user
	 * wants to change its own contact details or if the user is an
	 * administrator. invalid request is missing param that must be there or
	 * modify another user without to have the right to OR modify another user
	 * without to have the right to.
	 * 
	 * @param rq
	 *            request got
	 * @return true, if the request is valid.
	 */
	public static boolean isValidUpdateRequest(HttpServletRequest rq) {
		boolean result = userHasRightToViewAndModifyContactDetails(rq);
		if (result) {
			result = getUserOfRequest(rq) != null;
		}
		if (result) {
			String newMail = ContactDetailsRequestHandler.getValue(rq, "mail");
			if (newMail != null && !EmailValidator.getInstance().isValid(newMail)) {
				result = false;
			}

		}
		return result;
	}

	@Deprecated
	public static HtmlElement getSummaryTable(User user) {

		HtmlElement tbody = HtmlFactory.get("tbody");
		String tmp = "";

		// fname
		tmp = "";
		if (user.getFname() != null && !user.getFname().isEmpty()) {
			tmp = user.getFname();
		}
		buildSummaryRow(user, tbody, "First name", tmp, "fname"); // INTLANG

		// sname
		tmp = "";
		if (user.getSname() != null && !user.getSname().isEmpty()) {
			tmp = user.getSname();
		}
		buildSummaryRow(user, tbody, "Last Name", tmp, "sname"); // INTLANG

		// address table
		Address address = user.getMainAddress();
		if (address != null) {
			String tmpTmp = new HtmlAdapterAddress(address).getFullAsText("<br />");
			if (!tmpTmp.isEmpty()) {
				tmp = tmpTmp;
			}
		}
		buildSummaryRow(user, tbody, "Address", tmp, "address"); // INTLANG

		// email
		tmp = user.getMail();
		buildSummaryRow(user, tbody, "E-Mail", tmp, "mail"); // INTLANG

		// title
		tmp = "";
		if (user.getTitle() != null && !user.getTitle().isEmpty()) {
			tmp = user.getTitle();
		}
		buildSummaryRow(user, tbody, "Title", tmp, "title"); // INTLANG

		// gender
		tmp = "";
		if (user.getMale() != null) {
			tmp = user.getMale() ? "Male" : "Female"; // INTLANG
		}
		buildSummaryRow(user, tbody, "Gender", tmp, "male"); // INTLANG

		// birthdate
		tmp = "";
		if (user.getBirthdate() != null) {
			tmp = FamDateFormat.getDateFormatted(user.getBirthdate());
		}
		buildSummaryRow(user, tbody, "Day of birth", tmp, "birthdate"); // INTLANG

		// company
		tmp = "";
		if (user.getCompany() != null && !user.getCompany().isEmpty()) {
			tmp = user.getCompany();
		}
		buildSummaryRow(user, tbody, "Institution", tmp, "company"); // INTLANG

		// department
		tmp = "";
		if (user.getDepartmentLabel() != null && !user.getDepartmentLabel().isEmpty()) {
			tmp = user.getDepartmentLabel();
		}
		buildSummaryRow(user, tbody, "Department", tmp, "departmentLabel"); // INTLANG

		// landline
		tmp = "";
		if (user.getPhone1() != null && !user.getPhone1().isEmpty()) {
			tmp = user.getPhone1();
		}
		buildSummaryRow(user, tbody, "Landline", tmp, "phone1"); // INTLANG

		// mobile
		tmp = "";
		if (user.getPhone2() != null && !user.getPhone2().isEmpty()) {
			tmp = user.getPhone2();
		}
		buildSummaryRow(user, tbody, "Mobile", tmp, "phone2"); // INTLANG

		// free contact details
		for (ContactDetail cd : user.getContactDetails()) {
			buildSummaryRow(user, tbody, cd.getTitle(), cd.getDetail(), "cd_" + cd.getId());
		}

		// intendedResearch
		tmp = "";
		if (user.getIntendedResearch() != null && !user.getIntendedResearch().isEmpty()) {
			tmp = user.getIntendedResearch().replaceAll(System.getProperty("line.separator"), "<br />");
		}
		buildSummaryRow(user, tbody, "Intended Research Projekt", tmp, "intendedResearch"); // INTLANG

		return tbody;
	}

	private static void buildSummaryRow(User user, HtmlElement table, String label, String value, String actionName) {
		if (value.isEmpty()) {
			HtmlElement shownValue = new HtmlElement("span");
			try {
				if (MandatoryUserFieldValidator.getInstance().isSufficient(user, actionName)) {
					shownValue.add("unknown"); // INTLANG
				} else {
					shownValue.addClassName("missed").add("please add"); // INTLANG
				}
			} catch (InvalidRoleIdException e) {
				FamLog.exception(e, 201011151041l);
				shownValue.add("error 201011151041");
			}
			table.add(HtmlFactory.getInstance().get_tr(label, shownValue).add(getActionTd(actionName, 1, user, false)));
		} else { // user has address
			HtmlElement shownValue = new HtmlElement("p").add(value);
			try {
				if (!MandatoryUserFieldValidator.getInstance().isSufficient(user, actionName)) {
					shownValue.add(new HtmlElement("br")).add(new HtmlElement("span").addClassName("missed").add("please complete")); // INTLANG
				}
			} catch (InvalidRoleIdException e) {
				FamLog.exception(e, 201011151042l);
				shownValue.add("error 201011151042");
			}
			table.add(HtmlFactory.get("tr").add(HtmlFactory.get("td").add(label)).add(HtmlFactory.get("td").add(shownValue)).add(getActionTd(actionName, 1, user, true)));
		}
	}

	private static HtmlElement getActionTd(String queryKeyOf, int rowcount, User user, boolean forExisting) {
		HtmlElement result = HtmlFactory.get("td");
		// build delete form
		if (forExisting && !queryKeyOf.equals("mail")) {
			QueryString deleteQueryString = QueryStringBuilder.getDeleteQueryString();
			deleteQueryString.put(QueryKeys.QUERY_KEY_OF, queryKeyOf);
			deleteQueryString.put("hasBeenSent", "true");
			deleteQueryString.put("user_id", user.getId());
			HtmlElement deleteButton = FamSubmitButtonFactory.getDeleteButton();
			HtmlElement deleteForm = FamFormFactory.getForm(deleteQueryString, deleteButton);
			deleteForm.setAttribute("action", TemplateHtml.href("contactdetails"));
			result.add(deleteForm);
		}

		// build edit button (only for javascript version)
		QueryString editQueryString = QueryStringBuilder.getDeleteQueryString();
		editQueryString.put(QueryKeys.QUERY_KEY_OF, queryKeyOf);
		editQueryString.put("hasBeenSent", "true");
		editQueryString.put("user_id", user.getId());
		HtmlElement editButton = null;
		if (forExisting || queryKeyOf.equals("mail")) {
			editButton = FamSubmitButtonFactory.getEditButton();
		} else {
			editButton = FamSubmitButtonFactory.getAddButton();
		}
		editButton.addClassName("js_edit");
		HtmlElement editForm = FamFormFactory.getForm(editQueryString, editButton);
		editForm.setAttribute("action", TemplateHtml.href("contactdetails"));
		editForm.setAttribute("onSubmit", "javascript: return false;");
		editForm.doNotDisplay();
		editForm.addClassName("js_show");
		result.add(editForm);

		// build result td
		result.setAttribute("rowspan", rowcount);
		result.addClassName("action");
		return result;
	}

	/**
	 * return the date from a date input. input must have format ddMMyyyy (or
	 * dd.MM.yyyy etc.).
	 * 
	 * @param birthdate
	 *            as put in
	 * @return the date from a date input
	 */
	public static Date getDate(String birthdate) {
		Date result = null;
		if (birthdate != null) {
			String input = birthdate.trim().replaceAll("[^0-9]", "");
			try {
				DateFormat df = new SimpleDateFormat("ddMMyyyy");
				df.setLenient(false);
				result = df.parse(input);
			} catch (ParseException ex) {
			}
		}
		return result;

	}

	private ContactDetailsRequestHandler() {
	}

	public static String getDepartmentLabel(String departmentLabel, String departmentKey) {
		Department d = null;
		if (departmentLabel == null || departmentLabel.trim().isEmpty()) {
			d = KnownDepartmentConfigDao.getInstance().get(departmentKey);
		}
		if (d != null) {
			return d.getTitle();
		} else {
			return departmentLabel;
		}
	}

	public static boolean userHasRightToViewAndModifyContactDetails(HttpServletRequest rq) {
		User changeuser = getUserOfRequest(rq);
		if (changeuser != null) {
			User authuser = SessionAuth.user(rq);
			return changeuser.getUsername().equals(authuser.getUsername()) || authuser.isAdmin();
		} else {
			return true;
		}
	}

	public static Date correctBirthdate(Date birthdate) {
		Calendar now = Calendar.getInstance();
		// older then 0?
		if (birthdate != null) {
			if (birthdate.after(now.getTime())) {
				birthdate = null;
			}
		}
		if (birthdate != null) {
			// younger then 200?
			now.roll(Calendar.YEAR, -200);
			if (birthdate.before(now.getTime())) {
				birthdate = null;
			}
		}
		return birthdate;
	}

	public static Date correctBirthdate(String birthdate) {
		return correctBirthdate(getDate(birthdate));
	}
}
