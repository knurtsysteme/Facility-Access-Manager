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
package de.knurt.fam.core.model.config.statistics;

import java.util.Properties;

import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * this is for statistics using pic.jsp as a view. it is a picture embedded in
 * an img-tag.
 * 
 * @author Daniel Oltmanns
 * @since 11.10.2009
 */
public abstract class FamStatisticPic extends FamStatisticAbstract {
	@Override
  public Properties resolveModelAndView(TemplateResource templateResource, QueryString qs) {
		Properties result = new Properties();
		templateResource.setTemplateFile("page_full_pic.html");
		result.put("imgsrc", this.getBaseSrcName() + ".img" + qs.getAsHtmlLinkHref());
		int refreshMinutes = -1;
		try {
			refreshMinutes = Integer.parseInt(qs.get(QueryKeys.QUERY_KEY_MINUTE_OF_HOUR));
		} catch (NumberFormatException e) {
		}
		result.put("refreshMinutes", refreshMinutes);
		return result;
	}

	/**
	 * return the base for the value of the src attribute of the img tag. e.g.,
	 * if the statistical view is reachable on url:
	 * <code>http://www.foo.com/mystatistic.img?a=b&c=d</code> return
	 * "mystatistic" then.
	 * 
	 * @return the base for the value of the src attribute of the img tag.
	 */
	public abstract String getBaseSrcName();
}
