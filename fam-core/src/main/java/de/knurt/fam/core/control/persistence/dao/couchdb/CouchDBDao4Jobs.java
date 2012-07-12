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
package de.knurt.fam.core.control.persistence.dao.couchdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jcouchdb.db.Response;
import org.jcouchdb.document.DocumentInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.svenson.JSONParseException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.control.persistence.dao.FamJobsDao;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.util.booking.JobDataProcessingVelocityParser;
import de.knurt.heinzelmann.util.query.QueryString;
import de.knurt.heinzelmann.util.query.QueryStringFactory;

/**
 * dao for jobs of the fam
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.6.0 (11/23/20111)
 */
public class CouchDBDao4Jobs implements FamJobsDao {

	/** one and only instance of CouchDBDao4Jobs */
	private volatile static CouchDBDao4Jobs me;

	/** construct CouchDBDao4Jobs */
	private CouchDBDao4Jobs() {
	}

	/**
	 * return the one and only instance of CouchDBDao4Jobs
	 * 
	 * @return the one and only instance of CouchDBDao4Jobs
	 */
	public static CouchDBDao4Jobs getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CouchDBDao4Jobs.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CouchDBDao4Jobs();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of CouchDBDao4Jobs
	 */
	public static CouchDBDao4Jobs me() {
		return getInstance();
	}

	/**
	 * return the current {@link JobDataProcessing} instance for the given
	 * facility or <code>null</code> if nothing is defined
	 * 
	 * @see couchdb <a href="http://localhost:5984/fam/_design/as/_list/newest_with_facility_key/all_job_data_processing?facilityKey=bus1"
	 *      >http://localhost:5984/fam/_design/as/_list/newest_with_facility_key
	 *      /all_job_data_processing?facilityKey=bus1</a>
	 * @param useParent
	 *            if <code>true</code> and nothing is defined for given
	 *            facility, use parent facility and go on to root facility. only
	 *            if even the root facility has nothing defined return
	 *            <code>null</code>.
	 */
	@Override
	public JobDataProcessing getCurrentJobDataProcessing(Facility facility, boolean useParent) {
		QueryString queryString = QueryStringFactory.get("facilityKey", facility.getKey());
		String uri = "_design/as/_list/newest_with_facility_key/all_job_data_processing";
		try {
			return FamCouchDBDao.getInstance().getContentAsBean(uri, queryString, JobDataProcessing.class);
		} catch (JSONParseException e) {
			if (useParent && facility.hasParent()) {
				return this.getCurrentJobDataProcessing(facility.getParentFacility(), useParent);
			} else {
				return null;
			}
		}
	}

	/**
	 * return the current {@link JobDataProcessing} instance for the given
	 * facility or <code>null</code> if nothing is defined
	 * 
	 * @see couchdb <a href="http://localhost:5984/fam/_design/as/_list/newest_with_facility_key/all_job_data_processing?facilityKey=bus1"
	 *      >http://localhost:5984/fam/_design/as/_list/newest_with_facility_key
	 *      /all_job_data_processing?facilityKey=bus1</a>
	 * @param useParent
	 *            if <code>true</code> and nothing is defined for given
	 *            facility, use parent facility and go on to root facility. only
	 *            if even the root facility has nothing defined return
	 *            <code>null</code>.
	 */
	public JSONObject getCurrentJobDataProcessingAsJSONObject(Facility facility, List<Job> jobs, boolean useParent, boolean parseVelocity) {
		QueryString queryString = QueryStringFactory.get("facilityKey", facility.getKey());
		String uri = "_design/as/_list/newest_with_facility_key/all_job_data_processing";
		JSONObject result = null;
		try {
			String jsonResult = FamCouchDBDao.getInstance().getContentAsString(uri, queryString);
			result = new JSONObject(jsonResult);
			if (parseVelocity) {
				Facility facilityGot = FacilityConfigDao.facility(result.getString("facilityKey"));
				JobDataProcessingVelocityParser parser = new JobDataProcessingVelocityParser(facilityGot, jobs);
				parser.parse(result);
			}
		} catch (JSONParseException e) {
		} catch (JSONException e) {
		}
		if (result == null && useParent && facility.hasParent()) {
			return this.getCurrentJobDataProcessingAsJSONObject(facility.getParentFacility(), jobs, useParent, parseVelocity);
		} else {
			return result;
		}
	}

	/** {@inheritDoc} */
	@Override
	public Job getJob(int jobId, int step) {
		Job result = null;
		QueryString queryString = QueryStringFactory.get("jobId", jobId + "");
		queryString.put("step", step);
		String uri = "_design/as/_list/of_step_with_job_id/all_job";
		try {
			result = FamCouchDBDao.getInstance().getContentAsBean(uri, queryString, Job.class);
		} catch (JSONParseException e) {
			// okay! just no Job found ...
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public List<Job> getJobs(Booking booking) {
		return this.getJobs(booking.getId());
	}

	/** {@inheritDoc} */
	@Override
	public List<Job> getJobs(int jobid) {
		List<Job> result = new ArrayList<Job>();
		int step = 0;
		do {
			Job tmp = this.getJob(jobid, step);
			if (tmp != null) {
				result.add(tmp);
			}
			step++;
		} while (step <= 4);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public JobDataProcessing getJobDataProcessing(String id) {
		return FamCouchDBDao.getInstance().getContentAsBean(id, JobDataProcessing.class);
	}

	/** {@inheritDoc} */
	@Override
	public JobDataProcessing getJobDataProcessing(Job job) {
		return this.getJobDataProcessing(job.getIdJobDataProcessing());
	}

	/** {@inheritDoc} */
	@Override
	public List<Job> getJobs(User user, boolean withFeedback) {
		List<Job> result = new ArrayList<Job>();
		QueryString queryString = QueryStringFactory.get("username", user.getUsername() + "");
		String uri = withFeedback ? "_design/as/_list/jobs_of_user_and_feedback/all_job" : "_design/as/_list/of_user/all_job";
		uri += "?" + queryString.getAsQueryParams(false);
		Response response = FamCouchDBDao.response(uri);
		try {
			@SuppressWarnings("unchecked")
			List<Map> jobs = response.getContentAsList();
			for (Map<?, ?> job : jobs) {
				result.add(FamCouchDBDao.getInstance().getContentAsBean(job.get("_id").toString(), Job.class));
			}
		} catch (ClassCastException e) {
			FamLog.exception(e, 201205031134l);
		} finally {
			if (response != null) {
				response.destroy();
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean deleteJobs(User auth, User user, boolean withFeedback) {
		boolean result = FamAuth.hasRight(auth, FamAuth.DELETE_USERS_DATA, null);
		if (result) {
			List<Job> jobs = this.getJobs(user, withFeedback);
			if (jobs.size() > 0) {
				try {
					List<DocumentInfo> infos = FamCouchDBDao.admindatabase(auth).bulkDeleteDocuments(jobs, true);
					for (DocumentInfo info : infos) {
						if (info.getError() != null) {
							FamLog.error(info.getReason() + " / " + info.getId() + " / " + info.getRevision() + " / " + info.getError(), 201205041202l);
							result = false;
						}
					}
				} catch (NullPointerException e) {
					FamLog.exception("called by " + auth.getUsername() + " - ", e, 201205031337l);
				} catch (Exception e) {
					FamLog.exception(e, 201205031258l);
				}
			}
		}
		return result;
	}
}
