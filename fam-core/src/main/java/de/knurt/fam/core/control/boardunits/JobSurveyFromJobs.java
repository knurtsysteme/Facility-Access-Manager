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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.heinzelmann.util.nebc.BoardUnit;

/**
 * return a job data processing object from a given request
 * 
 * @author Daniel Oltmanns
 * @since 1.6.0 (12/07/2011)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class JobSurveyFromJobs implements BoardUnit<List<Job>, Map> {

	/** {@inheritDoc} */
	@Override
	public Map process(List<Job> jobs) {
		Map result = new HashMap();
		if (jobs != null) {
			for (Job job : jobs) {
				result.putAll(job.getJobSurvey());
			}
		}
		return result;
	}
}
