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

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

/**
 * container to inject all statistics known by the system.
 * 
 * @author Daniel Oltmanns
 * @since 0.20091012
 */
public class FamStatisticContainer {

	/** one and only instance of me */
	private volatile static FamStatisticContainer me;

	/** construct me */
	private FamStatisticContainer() {
	}

	/**
	 * return the one and only instance of me
	 * 
	 * @return the one and only instance of me
	 */
	public static FamStatisticContainer getInstance() {
		if (me == null) { // no instance so far
			synchronized (FamStatisticContainer.class) {
				if (me == null) { // still no instance so far
					me = new FamStatisticContainer(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return the statistic with given id or null, if this id is not present.
	 * 
	 * @param id
	 *            of statistic returned
	 * @return the statistic with given id or null, if this id is not present.
	 */
	public FamStatistic getStatistic(int id) {
		FamStatistic result = null;
		for (FamStatistic candidate : this.getAvailableStatistics()) {
			if (candidate.getId() == id) {
				result = candidate;
			}
		}
		return result;
	}

	private List<FamStatistic> availableStatistics;

	/**
	 * @param availableStatistics
	 *            the availableStatistics to set
	 */
	@Required
	public void setAvailableStatistics(List<FamStatistic> availableStatistics) {
		this.availableStatistics = availableStatistics;
	}

	/**
	 * @return the availableStatistics
	 */
	public List<FamStatistic> getAvailableStatistics() {
		return availableStatistics;
	}

}
