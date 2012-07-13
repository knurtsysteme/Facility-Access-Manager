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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.encoder.FamEncoder;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.termsofuse.TermsOfUsePage;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;
import de.knurt.heinzelmann.util.validation.AssertOrException;

/**
 * resolve acceptance for terms of agreement.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/18/2009)
 */
public class TermsOfUseResolver {

	private List<TermsOfUsePage> termsOfUsePages = new ArrayList<TermsOfUsePage>();

	private User user = null;

	/**
	 * to show terms for or that is working on the terms (in most cases the auth
	 * user)
	 * 
	 * @param user
	 *            to show terms for
	 */
	public TermsOfUseResolver(User user) {
		AssertOrException.assertTrue(user != null, "user must not be null [201205231141]");
		this.user = user;
		List<TermsOfUsePage> customPages = FamDaoProxy.soaDao().getActiveTermsOfUsePages(user);
		int pagetotalno = customPages.size() + 1;
		if (this.showBasePageFirst()) {
			termsOfUsePages.add(this.getBasicPage(0));
		}
		termsOfUsePages.addAll(customPages);
		if (!this.showBasePageFirst()) {
			termsOfUsePages.add(this.getBasicPage(pagetotalno - 1));
		}
	}

	/**
	 * return true, if the default base page is configured as first page
	 * 
	 * @return true, if the default base page is configured as first page
	 */
	public boolean showBasePageFirst() {
		boolean result = true;
		try {
			result = FamConnector.getGlobalProperty("soa_base_page").equalsIgnoreCase("first");
		} catch (NullPointerException e) {
			FamLog.warn("Please define soa_base_page in your fam_global.conf", 201206271134l);
		}
		return result;
	}

	/**
	 * discard the acceptance for all users that have to force the given new
	 * agreements. this results in reaccepting the agreements.
	 * 
	 * @param newSoaActivationDocument
	 *            the new soas the role is taken from. must be activated and not
	 *            deactivated already.
	 */
	public void discardAcceptanceForUsers(SoaActivationDocument newSoaActivationDocument) {
		AssertOrException.assertTrue(this.user.isAdmin(), this.user + " is not an admin [201205231140]");
		AssertOrException.assertTrue(newSoaActivationDocument.getActivatedOn() != null, "given soa activation is not activated [201009271406]");
		AssertOrException.assertTrue(newSoaActivationDocument.getDeactivatedOn() == null, "given soa activation is deactivated [201009271407]");
		List<User> usersWithRole = FamDaoProxy.userDao().getUserWithRole(RoleConfigDao.getInstance().getRole(newSoaActivationDocument.getRoleId()));
		for (User user : usersWithRole) {
			user.setAcceptedStatementOfAgreement(false);
			user.update();
		}
	}

	/**
	 * deactivate every ativation document with the same role that is not
	 * already deactivated.
	 * 
	 * @param newSoaActivationDocument
	 *            set as a new activation document for a role
	 */
	public void deactivateAgreementsFor(SoaActivationDocument newSoaActivationDocument) {
		AssertOrException.assertTrue(newSoaActivationDocument.getActivatedOn() != null, "201205231142");
		AssertOrException.assertTrue(newSoaActivationDocument.getDeactivatedOn() == null, "201205231143");
		List<SoaActivationDocument> all_sads = FamDaoProxy.soaDao().getSoaActivationDocumentsForDeactivation(newSoaActivationDocument);
		for (SoaActivationDocument sad : all_sads) {
			sad.setDeactivatedOn(new Date().getTime());
			sad.update();
		}
	}

	/**
	 * get a secret, so that only the user registered can follow its terms of
	 * agreement.
	 * 
	 * @param newUser
	 *            to show the terms
	 * @return a secret string
	 */
	public String getSecret() {
		return FamEncoder.getInstance().encodeSomething(user.getDepartmentKey() + user.getFname(), user.getMail());
	}

	public boolean isRightUser(String secret) {
		return secret != null && this.getSecret().equals(secret);
	}

	public TermsOfUsePage getPageForUser(int pageno) {
		return this.termsOfUsePages.get(pageno);
	}

	private TermsOfUsePage getBasicPage(int pageno) {
		TermsOfUsePage result = new TermsOfUsePage();
		// ↘ leave empty, so that termsofusepage_main.html is used
		result.setHtmlContent("");
		result.setPageno(pageno);
		result.setForcePrinting(false);
		return result;
	}

	public String getLinkForUser(int pageno, String goToAfterPost) {
		return TemplateHtml.me().getHref("termsofuse") + "?" + this.getQueryString(pageno, goToAfterPost).getAsQueryParams(false);
	}

	public QueryString getQueryString(int pageno, String goToAfterPost) {
		QueryString result = QueryStringFactory.get(QueryKeys.QUERY_KEY_ROLE, user.getRoleId());
		result.put(QueryKeys.QUERY_KEY_TO, goToAfterPost);
		result.put(QueryKeys.QUERY_KEY_USER, user.getUsername());
		result.put(QueryKeys.QUERY_KEY_PAGENO, pageno);
		result.put(QueryKeys.QUERY_KEY_SECRET, this.getSecret());
		return result;
	}

	public boolean isLastPage(int pageno) {
		return this.termsOfUsePages.size() <= pageno + 1;
	}

	public int getPageCount() {
		return this.termsOfUsePages.size();
	}

}
