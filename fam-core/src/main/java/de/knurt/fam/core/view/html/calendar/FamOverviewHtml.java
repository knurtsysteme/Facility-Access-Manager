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
package de.knurt.fam.core.view.html.calendar;

import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.ui.html.HtmlTableUtils;

/**
 * a calendar overview to show specifics as a list.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090624 (06/24/2009)
 */
public abstract class FamOverviewHtml extends HtmlElement {

	private HtmlTableUtils tableUtils;

	/**
	 * construct the {@link FamOverviewHtml}. the basic tag for all the content
	 * gets the classname "overview"
	 * 
	 * @param tagname
	 *            the name of the basic tag used for that.
	 */
	public FamOverviewHtml(String tagname) {
		super(tagname);
		this.addClassName("overview");
		this.tableUtils = new HtmlTableUtils();
	}

	/**
	 * return a tr for the overview.
	 * 
	 * @return a tr for the overview.
	 */
	protected HtmlElement getTr() {
		HtmlElement tr = HtmlFactory.get("tr");
		tr.addClassName(this.tableUtils.getOddEven());
		tr.addClassName("chooseline");
		return tr;
	}

	/**
	 * return <code>&lt;td class="important"&gt;</code> as {@link HtmlElement}.
	 * this is used to highlight important things in an overview table.
	 * 
	 * @return <code>&lt;td class="important"&gt;</code> as {@link HtmlElement}.
	 */
	protected HtmlElement getImportantTd() {
		HtmlElement td = HtmlFactory.get("td");
		td.addClassName("important");
		return td;
	}

	/**
	 * add the navigation. this is optional, but must explicitly do nothing if
	 * no navigation is wished.
	 */
	protected abstract void addNavi();

	/**
	 * return the headline for a specific facility
	 * 
	 * @param facilityKey
	 *            for the facility
	 * @return the headline for a specific facility
	 */
	protected String getFacilityHeadline(String facilityKey) {
		return FacilityConfigDao.getInstance().getLabel(facilityKey);
	}
}
