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

import java.util.Date;
import java.util.Observable;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.jcouchdb.db.Database;
import org.jcouchdb.db.Response;
import org.jcouchdb.db.ServerImpl;
import org.svenson.JSONParseException;
import org.svenson.JSONParser;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.FamBaseDocument;
import de.knurt.fam.core.persistence.dao.FamDocumentDao;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * dao for couchdb
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.20 (08/13/2010)
 */
public class FamCouchDBDao extends Observable implements FamDocumentDao {

	// private Database database;

	/** one and only instance of CouchDBDao */
	private volatile static FamCouchDBDao me;

	/** construct CouchDBDao */
	private FamCouchDBDao() {
	}

	/**
	 * return the one and only instance of CouchDBDao
	 * 
	 * @return the one and only instance of CouchDBDao
	 */
	public static FamCouchDBDao getInstance() {
		if (me == null) { // no instance so far
			synchronized (FamCouchDBDao.class) {
				if (me == null) { // still no instance so far
					me = new FamCouchDBDao(); // the one and only
				}
			}
		}
		return me;
	}

	@Override
  public synchronized boolean createDocument(FamBaseDocument document) {
		boolean result = true;
		document.setCreated(new Date().getTime());
		try {
			database().createDocument(document);
			document.setHasBeenCreated();
	    setChanged();
			notifyObservers(document);
		} catch (Exception e) {
			FamLog.exception(e, 201203071217l);
			result = false;
		}
		return result;
	}

	public JSONParser getJSONParserUsed() {
		return database().getJsonConfig().getJsonParser();
	}

	/**
	 * put the given body without any validation into the database under a
	 * random uuid.
	 * 
	 * @param body
	 * @return the response for the put request
	 */
	public Response put(String body) {
		return database().getServer().put("/" + FamConnector.getGlobalProperty("couchdb_dbname") + "/" + this.getUuid(), body);
	}

	public byte[] getAttachment(String docId, String attachmentId) {
		return database().getAttachment(docId, attachmentId);
	}

	private String getUuid() {
		return database().getServer().getUUIDs(1).get(0);
	}

	public String getContentAsString(String uri) {
		Response response = response(uri);
		String result = "";
		try {
			result = response.getContentAsString().trim();
		} finally {
			if (response != null)
				response.destroy();
		}
		return result;
	}

	public <R> R getContentAsBean(String uri, QueryString queryString, Class<R> cls) throws JSONParseException {
		Response response = response(uri + "?" + queryString.getAsQueryParams(false));
		R result = null;
		try {
			result = response.getContentAsBean(cls);
		} finally {
			if (response != null)
				response.destroy();
		}
		return result;
	}

	public String getContentAsString(String uri, QueryString queryString) {
		return this.getContentAsString(uri + "?" + queryString.getAsQueryParams(false));
	}

	/**
	 * return one document as the result class given
	 * 
	 * @param <R>
	 *            class document converted to
	 * @param docId
	 *            search for
	 * @param resultClass
	 *            document converted to
	 * @return on document as the result class object or null
	 * @see Database#getDocument(Class, String);
	 */
	public <R> R getOne(String docId, Class<R> resultClass) {
		return database().getDocument(resultClass, docId);
	}

	/**
	 * alias for {@link #getOne(String, Class)}
	 */
	public <R> R getContentAsBean(String docId, Class<R> resultClass) {
		return this.getOne(docId, resultClass);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean updateDocument(FamBaseDocument documentToUpdate, FamBaseDocument existingDocument) {
		// update doc
		documentToUpdate.setId(existingDocument.getId());
		documentToUpdate.setRevision(existingDocument.getRevision());
		return this.updateDocument(documentToUpdate);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean updateDocument(FamBaseDocument document) {
		boolean result = true;
		document.setCreated(new Date().getTime());
		try {
			database().updateDocument(document);
		} catch (Exception e) {
			FamLog.exception(e, 201203071218l);
			result = false;
		}
		if (result) {
			FamLog.info("update document: " + document.getId(), 201203080925l);
		}
		return result;
	}

	protected static Database database() {
		String host = FamConnector.getGlobalProperty("couchdb_ip");
		int port = Integer.parseInt(FamConnector.getGlobalProperty("couchdb_port"));
		String name = FamConnector.getGlobalProperty("couchdb_dbname");
		return new Database(new ServerImpl(host, port), name);
	}

	protected static Database admindatabase(User auth) {
		if (auth.isAdmin()) {
			String username = FamConnector.getGlobalProperty("couchdb_username");
			String userpass = FamConnector.getGlobalProperty("couchdb_userpass");
			String ip = FamConnector.getGlobalProperty("couchdb_ip");
			String host = String.format("%s:%s@%s", username, userpass, ip);
			int port = Integer.parseInt(FamConnector.getGlobalProperty("couchdb_port"));
			String name = FamConnector.getGlobalProperty("couchdb_dbname");
			ServerImpl s = new ServerImpl(host, port);
			AuthScope authScope = new AuthScope(host, port, "_admin");
			Credentials credentials = new UsernamePasswordCredentials(username, userpass);
			s.setCredentials(authScope, credentials);
			return new Database(s, name);
		} else {
			FamLog.error("this must be called by an admin", 201205041254l);
			return null;
		}

	}

	public String databaseName() {
		return database().getName();
	}

	public long documentCount() {
		return database().getStatus().getDocumentCount();
	}

	/**
	 * return the response object for the given uri. DO NOT FORGET TO DESTROY
	 * THE RESPONSE!!!
	 */
	protected static Response response(String uri) {
		return database().getServer().get("/" + FamConnector.getGlobalProperty("couchdb_dbname") + "/" + uri);
	}

}
