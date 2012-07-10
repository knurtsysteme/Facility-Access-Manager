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
package de.knurt.fam.core.content.html;

import de.knurt.fam.core.control.persistence.dao.config.KnownDepartmentConfigDao;
import de.knurt.fam.core.model.config.Department;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * generate html for departments configured.
 * 
 * @author Daniel Oltmanns
 * @since 1.20 (08/19/2010)
 */
public class DepartmentHtml {
	/** one and only instance of DepartmentHtml */
	private volatile static DepartmentHtml me;

	/** construct DepartmentHtml */
	private DepartmentHtml() {
	}

	/**
	 * return the one and only instance of DepartmentHtml
	 * 
	 * @return the one and only instance of DepartmentHtml
	 */
	public static DepartmentHtml getInstance() {
		if (me == null) { // no instance so far
			synchronized (DepartmentHtml.class) {
				if (me == null) { // still no instance so far
					me = new DepartmentHtml(); // the one and only
				}
			}
		}
		return me;
	}

	public HtmlElement getSelect() {
		return this.getSelect("");
	}

	public HtmlElement getSelect(String selectedKey) {
		if (KnownDepartmentConfigDao.getInstance() == null) {
			return HtmlFactory.get("span");
		} else {
			HtmlElement result = HtmlFactory.get_select("department_key").id("department_id");
			for (Department department : KnownDepartmentConfigDao.getInstance().getDepartments()) {
				HtmlElement option = HtmlFactory.get_option(department.getKey(), department.getTitle(), department.getKey().equals(selectedKey));
				result.add(option);
			}
			return result;
		}
	}
}
