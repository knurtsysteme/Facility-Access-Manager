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
 */package de.knurt.fam.core.control.boardunits;

import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * return the form template the user filled out and the job survey matches to.
 * 
 * @author Daniel Oltmanns
 * @since 1.7.0 (03/08/2012)
 */

public class JobDataProcessingOfJob implements BoardUnit<Job, JobDataProcessing> {

	/**
	 * return the form template the user filled out and the job survey matches
	 * to.
	 */
	@Override
	public JobDataProcessing process(Job datum) {
		JobDataProcessing result = null;
		if (datum.getIdJobDataProcessing() != null) {
			result = FamCouchDBDao.getInstance().getOne(datum.getIdJobDataProcessing(), JobDataProcessing.class);
		}
		return result;
	}
}
