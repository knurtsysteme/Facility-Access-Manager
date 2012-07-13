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
package de.knurt.fam.core.model.persist.document;

import de.knurt.fam.core.persistence.dao.FamDaoProxy;

/**
 * soa (or "terms of agreement") pojo
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.20 (08/13/2010)
 */
public class SoaDocument extends FamBaseDocument implements FamDocument {

	private String content, title;

	public FamDocumentType getType() {
		return FamDocumentType.SOA;
	}

	public boolean insertOrUpdate() {
		return FamDaoProxy.docDao().createDocument(this);
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}
