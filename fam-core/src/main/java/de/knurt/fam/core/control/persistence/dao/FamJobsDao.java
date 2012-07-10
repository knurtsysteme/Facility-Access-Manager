package de.knurt.fam.core.control.persistence.dao;

import java.util.List;

import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;

/**
 * a dao for jobs and job data processing.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.8.0 (04/05/2012)
 */
public interface FamJobsDao {
	/**
	 * return all jobs from the user.
	 * 
	 * @param user
	 *            the user
	 * @param withFeedback
	 *            if true, return all jobs that are feedback to user's input as
	 *            well
	 * @return all jobs from the user
	 */
	List<Job> getJobs(User user, boolean withFeedback);

	/**
	 * delete all jobs got from {@link #getJobs(User, boolean)}. return true if
	 * something deleted or no jobs are there. return false if no jobs deleted.
	 * always delete all jobs or nothing.
	 * 
	 * if auth has no right {@link FamAuth#DELETE_USERS_DATA} do nothing and
	 * return false
	 * 
	 * @param user
	 *            that is requesting this - must be admin
	 * @param user
	 *            jobs are deleted of
	 * @param withFeedback
	 * @return
	 */
	boolean deleteJobs(User auth, User user, boolean withFeedback);

	/**
	 * return the actual {@link JobDataProcessing} for the given
	 * {@link Facility}. if useParent is true and no {@link JobDataProcessing}
	 * is found for the given {@link Facility}, try to return the
	 * {@link JobDataProcessing} for the parent {@link Facility}.
	 * 
	 * @see Facility#getParentFacility()
	 * @param facility
	 *            requested
	 * @param useParent
	 *            use parent facility if true and nothing found
	 * @return the actual {@link JobDataProcessing} for the given
	 *         {@link Facility} or parent {@link Facility}
	 */
	JobDataProcessing getActualJobDataProcessing(Facility facility, boolean useParent);

	/**
	 * return the jobs of the given job id. this may be an empty list on no jobs
	 * stored so far.
	 * 
	 * @param jobid
	 *            of the requested jobs
	 * @return the jobs of the given job id.
	 */
	List<Job> getJobs(int jobid);

	/**
	 * return the job where the given id and step is given. return null if
	 * nothing found.
	 * 
	 * @param jobId
	 * @param step
	 * @return
	 */
	Job getJob(int jobId, int step);

	/**
	 * return all jobs for the booking. return empty list if nothing found.
	 * assume that the id of the booking equals to the id of the job.
	 * 
	 * @param booking
	 *            jobs are for
	 * @return all jobs for the booking
	 */
	List<Job> getJobs(Booking booking);

	/**
	 * return the {@link JobDataProcessing} with the given id
	 * 
	 * @param id
	 *            given
	 * @return the {@link JobDataProcessing} with the given id
	 */
	JobDataProcessing getJobDataProcessing(String id);

	/**
	 * return the {@link JobDataProcessing} for the given {@link Job}
	 * 
	 * @param job
	 *            given
	 * @return the {@link JobDataProcessing} for the given {@link Job}
	 */
	JobDataProcessing getJobDataProcessing(Job job);
}