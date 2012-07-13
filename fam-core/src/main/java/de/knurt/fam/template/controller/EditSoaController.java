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
package de.knurt.fam.template.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jcouchdb.db.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.model.persist.document.SoaDocument;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Soa;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.template.util.TermsOfUseResolver;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * control the edit soa page
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/13/2010)
 */
@Controller
class EditSoaController {

	// XXX cut me into pieces!
	public ModelAndView handleRequest(HttpServletRequest rq, HttpServletResponse rs) {
		ModelAndView result = null;
		User user = SessionAuth.user(rq);
		if (rq.getMethod().equals("POST") && user != null && user.isAdmin()) {
			if (this.isValidRequestFromNewAgreement(rq)) {
				SoaDocument document = this.insertDoc(rq);
				QueryString qs = QueryStringFactory.get("jsonvar", this.getJsonVarOnNewAgreementSucc(document));
				result = RedirectResolver.redirect(RedirectTarget.EDIT_SOA, qs);
			} else if (this.isValidRequestFromNewAgreementActivation(rq)) {
				boolean insertSucc = true;
				SoaActivationDocument newSoaActivationDocument = null;
				// XXX this is all dao stuff!!!!
				Response response = null;
				try {
					// ↓ get map of foreign keys given and put in real documents
					JSONObject jo = new JSONObject(rq.getParameter("body"));
					JSONArray oldArray = jo.getJSONArray("soaActivePages");
					JSONArray newArray = new JSONArray();
					int i = 0;
					boolean getAllSoasSucceeded = true;
					while (i < oldArray.length()) {
						JSONObject mappedSoaActivationPageDocument = oldArray.getJSONObject(i);

						JSONObject realSoaActivationPageDocument = new JSONObject();
						// ↘ java_link 201008191220
						realSoaActivationPageDocument.put("forcePrinting", mappedSoaActivationPageDocument.get("forcePrinting"));
						JSONObject soaDoc = FamDaoProxy.soaDao().getRealSoaActivationPageDocument(mappedSoaActivationPageDocument.get("soaId").toString());
						if(soaDoc == null) {
							getAllSoasSucceeded = false;
							break;
						} else {
							realSoaActivationPageDocument.put("soaDoc", soaDoc);
							newArray.put(realSoaActivationPageDocument);
						}
						i++;
					}
					if (getAllSoasSucceeded) {
						jo.put("soaActivePages", newArray);

						// put in new json string
						response = FamDaoProxy.soaDao().put(jo);
					}
				} catch (JSONException e) {
					insertSucc = false;
				}

				// execute consequences (like deleting accepted soas)
				if (response == null) {
					insertSucc = false;
				} else {
					Object objectid = response.getContentAsMap().get("id");
					if (objectid == null || objectid.toString().trim().isEmpty()) {
						insertSucc = false;
					} else {
						newSoaActivationDocument = CouchDBDao4Soa.getInstance().getSoaActivationDocument(objectid.toString());
						if (newSoaActivationDocument == null) {
							insertSucc = false;
						} else {
							TermsOfUseResolver tour = new TermsOfUseResolver(user);
							tour.discardAcceptanceForUsers(newSoaActivationDocument);
							tour.deactivateAgreementsFor(newSoaActivationDocument);
						}
					}
				}

				if (insertSucc) {
					QueryString qs = QueryStringFactory.get("jsonvar", this.getJsonVarOnNewAgreementActivationSucc(rq, newSoaActivationDocument));
					result = RedirectResolver.redirect(RedirectTarget.EDIT_SOA, qs);
				} else {
					QueryString qs = QueryStringFactory.get("jsonvar", this.getJsonVarOnNewAgreementActivationFail(rq));
					result = RedirectResolver.redirect(RedirectTarget.EDIT_SOA, qs);
				}
			} else {
				// ↖ invalid request
				QueryString qs = QueryStringFactory.get("jsonvar", this.getJsonVarOnInsertingFailOrUrlHacking(rq));
				result = RedirectResolver.redirect(RedirectTarget.EDIT_SOA, qs);
			}
		}
		return result;
	}

	private String getJsonVarOnNewAgreementActivationSucc(HttpServletRequest rq, SoaActivationDocument sad) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("succ", true);
			jo.put("doc_id", sad.getId());
			jo.put("animate_row", "page_" + sad.getId());
			jo.put("select", 3);
			jo.put("show_message", "New agreements have been activated"); // INTLANG
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "var FamStatus = " + jo.toString() + ";";
	}

	/**
	 * this may happen when the validation of couchdb document fails.
	 * 
	 * @param rq
	 * @param response
	 * @return
	 */
	private String getJsonVarOnNewAgreementActivationFail(HttpServletRequest rq) {
		JSONObject jo = new JSONObject();
		try {
			jo.put("succ", false);
			jo.put("sentfromform", "jactivationform");
			jo.put("doc_id", "null");
			jo.put("show_message", INSERTION_FAILED + "bad request [intern code: 201008181402]");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "var FamStatus = " + jo.toString() + ";";
	}

	private String getJsonVarOnInsertingFailOrUrlHacking(HttpServletRequest rq) {
		JSONObject jo = new JSONObject();
		try {
			if (rq.getParameter("sentfromform").equals("jnewform")) {
				jo.put("succ", false);
				jo.put("sentfromform", "jnewform");
				jo.put("doc_id", "null");
				jo.put("select", 1);
				String message = INSERTION_FAILED;
				if (rq.getParameter("title") == null || rq.getParameter("title").trim().isEmpty()) {
					message += "Please give the agreement a title."; // INTLANG
				}
				if (rq.getParameter("content") == null || rq.getParameter("content").trim().isEmpty()) {
					message += "<br />Agreement has no content."; // INTLANG
				}
				jo.put("show_message", message);
			} else if (rq.getParameter("sentfromform").equals("jactivationform")) {
				jo.put("succ", false);
				jo.put("sentfromform", "jactivationform");
				jo.put("doc_id", "null");
				jo.put("show_message", INSERTION_FAILED + "unknown error [intern code: 201008181400 - missed body]");
				jo.put("select", 1);
			} else { // hacked sentfromform
				jo.put("succ", false);
				jo.put("sentfromform", "unknown");
				jo.put("select", 1);
				jo.put("doc_id", "null");
				jo.put("show_message", INSERTION_FAILED + "invalid request [201008181359]");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "var FamStatus = " + jo.toString() + ";";
	}

	private String getJsonVarOnNewAgreementSucc(SoaDocument document) {
		JSONObject jo = new JSONObject();
		try {
			if (document == null) {
				// ↖ insert doc failed
				// ↓ let javascript know failing
				jo.put("succ", false);
				jo.put("sentfromform", "jnewform");
				jo.put("select", 1);
				jo.put("doc_id", "null");
				jo.put("show_message", INSERTION_FAILED + "Document has just been modified"); // INTLANG
			} else {
				// ↖ insert doc succeeded
				// ↓ let javascript know succeeding
				jo.put("succ", true);
				jo.put("sentfromform", "jnewform");
				jo.put("doc_id", document.getId());
				jo.put("animate_row", "edit_soa_overview_" + document.getId());
				jo.put("show_message", "Document has been inserted"); // INTLANG
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "var FamStatus = " + jo.toString() + ";";
	}

	private static final String INSERTION_FAILED = "Insertion failed! Reason: ";

	private SoaDocument insertDoc(HttpServletRequest rq) {
		SoaDocument document = new SoaDocument();
		document.setContent(rq.getParameter("content").trim());
		document.setTitle(rq.getParameter("title"));
		if (document.insertOrUpdate()) {
			return document;
		} else {
			return null;
		}
	}

	private boolean isValidRequestFromNewAgreement(HttpServletRequest rq) {
		boolean result = true;
		if (rq.getParameter("title") == null || rq.getParameter("content") == null || rq.getParameter("title").trim().isEmpty() || rq.getParameter("content").trim().isEmpty()) {
			result = false;
		}
		if (rq.getParameter("sentfromform") == null || !rq.getParameter("sentfromform").equals("jnewform")) {
			result = false;
		}
		return result;
	}

	private boolean isValidRequestFromNewAgreementActivation(HttpServletRequest rq) {
		boolean result = true;
		if (rq.getParameter("body") == null || rq.getParameter("body").trim().isEmpty()) {
			result = false;
		}
		if (rq.getParameter("sentfromform") == null || !rq.getParameter("sentfromform").equals("jactivationform")) {
			result = false;
		}
		return result;
	}
}
