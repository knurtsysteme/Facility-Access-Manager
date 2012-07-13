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
package de.knurt.fam.core.persistence.dao.couchdb;

import java.util.ArrayList;
import java.util.List;

import org.jcouchdb.db.Response;
import org.jcouchdb.document.ValueAndDocumentRow;
import org.jcouchdb.document.ValueRow;
import org.jcouchdb.document.ViewAndDocumentsResult;
import org.jcouchdb.document.ViewResult;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.model.persist.document.SoaActivationPageDocument;
import de.knurt.fam.core.persistence.dao.FamSoaDao;
import de.knurt.fam.core.util.termsofuse.TermsOfUsePage;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * dao for couchdb
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.20 (08/21/2010)
 */
public class CouchDBDao4Soa implements FamSoaDao {

	/** one and only instance of CouchDBDao */
	private volatile static CouchDBDao4Soa me;

	/** construct CouchDBDao */
	private CouchDBDao4Soa() {
	}

	/**
	 * return the one and only instance of CouchDBDao
	 * 
	 * @return the one and only instance of CouchDBDao
	 */
	public static CouchDBDao4Soa getInstance() {
		if (me == null) { // no instance so far
			synchronized (CouchDBDao4Soa.class) {
				if (me == null) { // still no instance so far
					me = new CouchDBDao4Soa(); // the one and only
				}
			}
		}
		return me;
	}

	public SoaActivationDocument getSoaActivationDocument(String docId) {
		return FamCouchDBDao.getInstance().getOne(docId, SoaActivationDocument.class);
	}

	public String getListOfCurrentSoaActionvationsAsHtml() {
		QueryString queryString = QueryStringFactory.get("descending", "true");
		queryString.put("format", "html");
		return FamCouchDBDao.getInstance().getContentAsString("_design/as/_list/terms_of_use_pages/terms_of_use_pages_active", queryString);
	}

	public String getListOfHistorySoaActionvationsAsHtml() {
		QueryString queryString = QueryStringFactory.get("descending", "true");
		queryString.put("format", "html");
		return FamCouchDBDao.getInstance().getContentAsString("_design/as/_list/terms_of_use_pages/terms_of_use_pages_history", queryString);
	}

	public String getListOfSoasAsHtmlSelect(QueryString parameters) {
		parameters.put("descending", "true");
		parameters.put("format", "html");
		parameters.put("render", "select");
		return FamCouchDBDao.getInstance().getContentAsString("_design/as/_list/soas/all_soa", parameters);
	}

	public String getListOfSoasAsHtmlOverview() {
		QueryString queryString = QueryStringFactory.get("descending", "true");
		queryString.put("format", "html");
		queryString.put("render", "overview");
		return FamCouchDBDao.getInstance().getContentAsString("_design/as/_list/soas/all_soa", queryString);
	}

	public List<SoaActivationPageDocument> getSoasForUser(User user) {
		// TODO #13 do not use FamCouchDBDao#getAll
		List<SoaActivationDocument> soas = this.getAllSoaActivation();
		List<SoaActivationPageDocument> result = null;
		for (SoaActivationDocument soa : soas) {
			if (soa.isActive() && soa.getRoleId().equals(user.getRoleId())) {
				result = soa.getSoaActivePages();
				break;
			}
		}
		return result;
	}

	/** {@inheritDoc}} */
	@Override
	public List<TermsOfUsePage> getActiveTermsOfUsePages(User user) {
		List<TermsOfUsePage> result = new ArrayList<TermsOfUsePage>();
		ViewResult<TermsOfUsePage> pages = FamCouchDBDao.database().query("/_design/as/_view/terms_of_use_pages_active", TermsOfUsePage.class, null, null, null);
		for (ValueRow<TermsOfUsePage> page : pages.getRows()) {
			if (page.getValue().getRoleId().equals(user.getRoleId())) {
				result.add(page.getValue());
			}
		}
		return result;
	}

	public String getAgreement(String soaId) {
		return FamCouchDBDao.getInstance().getContentAsString("/_design/as/_show/agreement/" + soaId);
	}

	/** {@inheritDoc}} */
	@Override
	public List<SoaActivationDocument> getSoaActivationDocumentsForDeactivation(SoaActivationDocument newSoaActivationDocument) {
		// TODO #13 do not use FamCouchDBDao#getAll
		List<SoaActivationDocument> result = new ArrayList<SoaActivationDocument>();
		List<SoaActivationDocument> all_sads = this.getAllSoaActivation();
		for (SoaActivationDocument sad : all_sads) {
			if (sad.getRoleId().equals(newSoaActivationDocument.getRoleId())) {
				if (sad.isActive() && !sad.getId().equals(newSoaActivationDocument.getId())) {
					result.add(sad);
				}
			}
		}
		return result;
	}

	/** {@inheritDoc}} */
	@Override
	public List<SoaActivationDocument> getAllSoaActivation() {
		String uri = "_design/as/_view/all_soa_activation";
		List<SoaActivationDocument> result = new ArrayList<SoaActivationDocument>();
		ViewAndDocumentsResult<SoaActivationDocument, SoaActivationDocument> value = FamCouchDBDao.database().query(uri, SoaActivationDocument.class, SoaActivationDocument.class, null, null, null);
		for (ValueAndDocumentRow<SoaActivationDocument, SoaActivationDocument> row : value.getRows()) {
			result.add(row.getDocument());
		}
		return result;
	}

	/** {@inheritDoc}} */
	@Override
	public JSONObject getRealSoaActivationPageDocument(String docId) {
		JSONObject result = null;
		Response responseForRealSoaDocument = FamCouchDBDao.response(docId);
		if (responseForRealSoaDocument.isOk()) {
			try {
				result = new JSONObject(responseForRealSoaDocument.getContentAsString());
			} catch (JSONException e) {
				FamLog.exception(e, 201205041026l);
			}
		}
		return result;
	}

	/** {@inheritDoc}} */
	@Override
	public Response put(JSONObject jo) {
		return FamCouchDBDao.getInstance().put(jo.toString());
	}

}
