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
package de.knurt.fam.template.util;

import java.util.Date;

import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * help sortation of tables. typicaly add an invisible span with a timestamp in
 * velocity templates.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 06/14/2011
 */
public class HtmlTableSortationUtil {
	/** one and only instance of TableSortationUtil */
	private volatile static HtmlTableSortationUtil me;

	/** construct TableSortationUtil */
	private HtmlTableSortationUtil() {
	}

	/**
	 * return the one and only instance of TableSortationUtil
	 * 
	 * @return the one and only instance of TableSortationUtil
	 */
	public static HtmlTableSortationUtil getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (HtmlTableSortationUtil.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new HtmlTableSortationUtil();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of TableSortationUtil
	 */
	public static HtmlTableSortationUtil me() {
		return getInstance();
	}

	public HtmlElement span(Date date) {
		HtmlElement result = new HtmlElement("span");
		result.hide();
		result.add(FamDateFormat.getCustomDate(date, "yyyyMMddHHmmss"));
		return result;
	}

	public HtmlElement span(TimeFrame timeframe) {
		return this.span(timeframe.getDateStart());
	}

}
