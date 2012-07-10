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


/**
 * soa (or "terms of agreement") pojo
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.20 (08/16/2010)
 */
public class SoaActivationPageDocument {
	
	// VERY IMPORTANT: if you need more fields, search for java_link 201008191220 and js_link 201008191221 and insert that field into the mapping as well.
	private SoaDocument soaDoc;
	private boolean forcePrinting = false;


	public SoaDocument getSoaDoc() {
		return soaDoc;
	}

	public void setSoaDoc(SoaDocument soaDoc) {
		this.soaDoc = soaDoc;
	}

	public boolean isForcePrinting() {
		return forcePrinting;
	}

	public void setForcePrinting(boolean forcePrinting) {
		this.forcePrinting = forcePrinting;
	}

}
