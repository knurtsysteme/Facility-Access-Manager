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

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.persistence.dao.FamDaoProxy;

/**
 * soa (or "terms of agreement") pojo
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.20 (08/16/2010)
 */
public class SoaActivationDocument extends FamBaseDocument implements FamDocument {

	private Long activatedOn, deactivatedOn;
	private String roleId;
	private List<SoaActivationPageDocument> soaActivePages = new ArrayList<SoaActivationPageDocument>();

	public FamDocumentType getType() {
		return FamDocumentType.SOA_ACTIVATION;
	}

	public boolean insertOrUpdate() {
		return FamDaoProxy.docDao().createDocument(this);
	}

	public Long getActivatedOn() {
		return activatedOn;
	}

	public void setActivatedOn(Long activatedOn) {
		this.activatedOn = activatedOn;
	}

	public Long getDeactivatedOn() {
		return deactivatedOn;
	}

	public void setDeactivatedOn(Long deactivatedOn) {
		this.deactivatedOn = deactivatedOn;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public List<SoaActivationPageDocument> getSoaActivePages() {
		return soaActivePages;
	}

	public void setSoaActivePages(List<SoaActivationPageDocument> soaActivePages) {
		this.soaActivePages = soaActivePages;
	}

	public void addPage(SoaActivationPageDocument sapd) {
		this.soaActivePages.add(sapd);
	}

	/**
	 * return, if deactivatedOn is null and activatedOn is not. do not save this
	 * as field in document (starting with <code>util</code>.
	 * 
	 * @return
	 */
	public boolean isActive() {
		return this.deactivatedOn == null && this.activatedOn != null;
	}

	public void update() {
		FamDaoProxy.docDao().updateDocument(this);
	}

}
