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
package de.knurt.fam.core.view.html;

import java.util.Date;

import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlException;
import de.knurt.heinzelmann.ui.html.HtmlFactory;
import de.knurt.heinzelmann.ui.html.StrictHtmlFactory;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * operations to return dynamic(!) pictures. these pictures ends all with ".img"
 * and has a controller class, that generates the picture.
 * 
 * @see "de.knurt.fam.core.control.mvc.controller.image"
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
@Deprecated
public class FamImageHtmlGetter {

	/**
	 * return the html code for a dynamic image of das.
	 * 
	 * @param control
	 *            basic name of image to control.
	 * @param altAndTitle
	 *            img alt and title node value
	 * @param query
	 *            string for parameters of the image
	 * @return html code for a dynamic image of das.
	 */
	public static HtmlElement get(String control, String altAndTitle, QueryString query) {
		String src = control + "image.img" + query.getAsHtmlLinkHref();
		HtmlElement result;
		try {
			result = StrictHtmlFactory.getInstance().get_img(src, altAndTitle);
		} catch (HtmlException ex) {
			result = HtmlFactory.get("img").att("src", src).att("alt", altAndTitle).att("title", altAndTitle);
		}
		result.addTitleAttribute(altAndTitle);
		return result;
	}

	/**
	 * return the code for the style to insert a picture dynamicly in the
	 * background.
	 * 
	 * @param control
	 *            basic name of image to control.
	 * @param query
	 *            string for parameters of the image
	 * @return the code for the style to insert a picture dynamicly in the
	 *         background.
	 */
	public static String getBackgroundImage(String control, QueryString query) {
		String format = "background:transparent url(%simage.img%s) no-repeat scroll 0 0;";
		String nocache = (query.isEmpty() ? "?" : "&amp;") + "nocache=" + new Date().getTime(); // hack
																								// browser
																								// cache
		return String.format(format, control, query.getAsHtmlLinkHref() + nocache);
	}

	private FamImageHtmlGetter() {
	}
}
