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
package de.knurt.fam.core.util.termsofuse;

import org.jcouchdb.document.BaseDocument;

/**
 * a pojo for a terms of use page. this is used with couchdb and
 * http://127.0.0.1:5984/.../_design/as/_view/terms_of_use_pages_active
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/21/2009)
 */
public class TermsOfUsePage extends BaseDocument {
	private String htmlContent, title, roleId;
	private int pageno;
	private boolean forcePrinting;

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public int getPageno() {
		return pageno;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isForcePrinting() {
		return forcePrinting;
	}

	public void setForcePrinting(boolean forcePrinting) {
		this.forcePrinting = forcePrinting;
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}

	public void setPageno(int pageno) {
		this.pageno = pageno;
	}

	public TermsOfUsePage() {
	}

}
