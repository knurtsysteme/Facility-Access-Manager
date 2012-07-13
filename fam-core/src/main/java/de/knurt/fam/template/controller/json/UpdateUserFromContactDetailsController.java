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
package de.knurt.fam.template.controller.json;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.EmailValidator;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.util.ContactDetailsRequestHandler;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * controller for ajax request to change a contact detail. answer with: a status
 * id for
 * <ul>
 * <li><code>OK</code>: input saved</li>
 * <li><code>BAD_REQUEST</code>: if basic parameters are missed</li>
 * <li><code>WRONG_INPUTS</code>: if user set something wrong, mostly an empty
 * input, but may also be a wrong date or email format</li>
 * </ul>
 * only if status is <code>OK</code>, answer with:
 * <ul>
 * <li><code>summaryTable</code></li>: the complete new table with details
 * <li><code>fullName</code></li>: the new name of the user (if first or last
 * name changed)
 * <li><code>updatedId</code></li>: if there was added a new contact detail,
 * there must be an input for that to edit shown then.
 * </ul>
 * 
 * @link contactdetails.js
 * @see ContactDetailsController
 * @author Daniel Oltmanns
 * @since 0.20100130 (01/30/2010)
 */
public class UpdateUserFromContactDetailsController extends JSONController {

	/**
	 * return an available possible booking.
	 * 
	 * there are two different places where this is called: 1. the
	 * "request booking" link in the navigation of the calendar 2. the drag and
	 * drop action in same calender
	 * 
	 * the response depends on which place causes the request.
	 * 
	 * @param rq
	 *            request
	 * @param rs
	 *            response
	 * @return an available possible booking.
	 */
	@Override
	public JSONObject getJSONObject(HttpServletRequest rq, HttpServletResponse rs) {
		JSONObject result = new JSONObject();
		try {
			if (ContactDetailsRequestHandler.isValidUpdateRequest(rq)) {
				Identificable objectChanged = this.updateValue(rq);
				int status = objectChanged == null ? BAD_REQUEST : (objectChanged.getClass().equals(WrongInput.class) ? WRONG_INPUTS : OK);
				result.put("succ", status);
				if (status == OK) {
					result.put("summaryTable", this.getSummaryTable(rq));
					User user = ContactDetailsRequestHandler.getUserOfRequest(rq);
					result.put("fullName", user.getFullName());
					result.put("updatedId", objectChanged.getId());
					result.put("showCompleteMessage", !user.hasUnsufficientContactDetails());
				}
			} else {
				result.put("succ", "0");
			}
		} catch (JSONException ex) {
			this.onException(ex);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201205071225l);
	}

	private void onException(Exception ex) {
		FamLog.exception("creating json failed", ex, 201001301336l);
	}

	private static final int OK = 1;
	private static final int BAD_REQUEST = 3;
	private static final int WRONG_INPUTS = 2;

	private Identificable updateValue(HttpServletRequest rq) {
		Identificable result = null;
		User user = ContactDetailsRequestHandler.getUserOfRequest(rq);
		String of = RequestInterpreter.getOf(rq);
		if (of != null) {
			if (of.equals("company")) {
				user.setCompany(ContactDetailsRequestHandler.getValue(rq, "company"));
				user.update();
				result = user;
			} else if (of.equals("phone1")) {
				user.setPhone1(ContactDetailsRequestHandler.getValue(rq, "phone1"));
				user.update();
				result = user;
			} else if (of.equals("fname")) {
				user.setFname(ContactDetailsRequestHandler.getValue(rq, "fname"));
				user.update();
				result = user;
			} else if (of.equals("intendedResearch")) {
				user.setIntendedResearch(ContactDetailsRequestHandler.getValue(rq, "intendedResearch"));
				user.update();
				result = user;
			} else if (of.equals("sname")) {
				user.setSname(ContactDetailsRequestHandler.getValue(rq, "sname"));
				user.update();
				result = user;
			} else if (of.equals("title")) {
				user.setTitle(ContactDetailsRequestHandler.getValue(rq, "title"));
				user.update();
				result = user;
			} else if (of.equals("mail")) {
				String newMail = ContactDetailsRequestHandler.getValue(rq, "mail");
				User example = UserFactory.me().blank();
				example.setMail(newMail);
				if (EmailValidator.getInstance().isValid(newMail) && !FamDaoProxy.userDao().userLikeExists(example)) {
					user.setMail(newMail);
					user.update();
					result = user;
				}
			} else if (of.equals("departmentLabel") || of.equals("departmentKey")) {
				String departmentKey = ContactDetailsRequestHandler.getValue(rq, "departmentKey");
				String departmentLabel = ContactDetailsRequestHandler.getValue(rq, "departmentLabel");
				departmentLabel = ContactDetailsRequestHandler.getDepartmentLabel(departmentLabel, departmentKey);
				user.setDepartmentLabel(departmentLabel);
				user.setDepartmentKey(departmentKey);
				user.update();
				result = user;
			} else if (of.equals("birthdate")) {
				Date birthdate = ContactDetailsRequestHandler.getDate(ContactDetailsRequestHandler.getValue(rq, "birthdate"));
				user.setBirthdate(birthdate);
				user.update();
				result = user;
			} else if (of.equals("male")) {
				String male = ContactDetailsRequestHandler.getValue(rq, "male");
				if (male != null) {
					user.setMale(male.equals("1") ? true : false);
				} else {
					user.setMale(null);
				}
				user.update();
				result = user;
			} else if (of.equals("phone2")) {
				user.setPhone2(ContactDetailsRequestHandler.getValue(rq, "phone2"));
				user.update();
				result = user;
			} else if (of.equals("address")) {

				Address changeaddress = user.getMainAddress();
				if (changeaddress == null) {
					changeaddress = new Address();
				}
				changeaddress.setStreet(ContactDetailsRequestHandler.getValue(rq, "street"));
				changeaddress.setStreetno(ContactDetailsRequestHandler.getValue(rq, "streetno"));
				changeaddress.setZipcode(ContactDetailsRequestHandler.getValue(rq, "zipcode"));
				changeaddress.setCity(ContactDetailsRequestHandler.getValue(rq, "city"));
				changeaddress.setCountry(ContactDetailsRequestHandler.getValue(rq, "country"));
				user.setMainAddress(changeaddress);
				user.update();
				result = user;
			} else if (of.startsWith("cd_")) {
				if (of.equals("cd_new")) {
					String newTitle = ContactDetailsRequestHandler.getValue(rq, "title_new");
					String newDetail = ContactDetailsRequestHandler.getValue(rq, "detail_new");

					// change unspecific contact details
					if (newTitle != null && newDetail != null) {
						newTitle = newTitle.trim();
						newDetail = newDetail.trim();
						if (!newTitle.isEmpty() && !newDetail.isEmpty()) {
							ContactDetail cd = new ContactDetail();
							cd.setDetail(newDetail);
							cd.setTitle(newTitle);
							cd.setUsername(user.getUsername());
							cd.insert();
							result = cd;
						} else {
							result = new WrongInput();
						}
					} else {
						result = new WrongInput();
					}
				} else {
					try {
						Integer cdId = Integer.parseInt(of.substring(3));
						String newTitle = ContactDetailsRequestHandler.getValue(rq, "title_" + cdId);
						String newDetail = ContactDetailsRequestHandler.getValue(rq, "detail_" + cdId);
						if (newTitle != null && newDetail != null) {
							for (ContactDetail cdExist : user.getContactDetails()) {
								if (cdExist.getId().intValue() == cdId) {
									cdExist.setDetail(newDetail);
									cdExist.setTitle(newTitle);
									cdExist.update();
									result = cdExist;
									break;
								}
							}
						}
						if (result == null) {
							result = new WrongInput();
						}
					} catch (NumberFormatException e) {
					}
				}
			}
			if (SessionAuth.authUser(rq) && SessionAuth.user(rq).getUsername().equals(user.getUsername())) {
				SessionAuth.getInstance(rq).setUser(user);
			}
		}
		return result;
	}

	@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	private HtmlElement getSummaryTable(HttpServletRequest rq) {
		User user = ContactDetailsRequestHandler.getUserOfRequest(rq);
		return ContactDetailsRequestHandler.getSummaryTable(user);
	}
}

final class WrongInput implements Identificable {

	public Integer getId() {
		return -1;
	}

	public void setId(Integer id) {
	}
}
