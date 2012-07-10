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
package de.knurt.fam.core.control.persistence.dao;

import de.knurt.fam.core.model.persist.document.FamBaseDocument;

/**
 * dao for documents. Mostly a dao for a db like CouchDB or Mongo.
 * 
 * @see FamBaseDocument
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (05/23/2012)
 */
public interface FamDocumentDao {

	public boolean createDocument(FamBaseDocument document);

	public boolean updateDocument(FamBaseDocument document, FamBaseDocument existing);

	/**
	 * set created to actual date, insert document into database and return true
	 * on success.
	 * 
	 * @param document
	 *            to insert
	 * @return true, if inserting succeeded
	 */
	public boolean updateDocument(FamBaseDocument document);

}
