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

import de.knurt.fam.template.model.TemplateResource;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * a stupid container for statistical meta information.
 * 
 * @author Daniel Oltmanns
 * @since 0.20091011
 */
public interface FamStatistic {

	/**
	 * return the label for the statistic.
	 * 
	 * @return the label for the statistic.
	 */
	public String getLabel();

	/**
	 * return the unique id for this statistic. every statistic must have an id.
	 * 
	 * @return the unique id for this statistic.
	 */
	public int getId();
	
	public Properties resolveModelAndView(TemplateResource templateResource, QueryString qs);
}
