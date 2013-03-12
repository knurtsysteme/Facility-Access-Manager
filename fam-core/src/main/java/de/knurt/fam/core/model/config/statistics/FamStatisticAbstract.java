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

import org.springframework.beans.factory.annotation.Required;

/**
 * statistic, where label and id is injected
 * 
 * @author Daniel Oltmanns
 * @since 11.10.2009
 */
public abstract class FamStatisticAbstract implements FamStatistic {

	private String label;
	private int id;

	/**
	 * @return the label
	 */

	@Override
  public String getLabel() {
		return label;
	}

	@Override
  public int getId() {
		return this.id;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	@Required
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	@Required
	public void setId(int id) {
		this.id = id;
	}

}
