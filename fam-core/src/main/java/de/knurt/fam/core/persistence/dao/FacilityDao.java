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
package de.knurt.fam.core.persistence.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.util.time.FacilityAvailabilityMerger;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * dao for all time frames of facilities.
 * 
 * @see FacilityAvailability
 * @author Daniel Oltmanns
 * @since 0.20090424
 */
public abstract class FacilityDao extends AbstractFamDao<FacilityAvailability> {

	private Long idToSet = null;

	/**
	 * return all {@link FacilityAvailability}s for the facility, that overlaps
	 * with the given {@link TimeFrame}. this includes all
	 * {@link FacilityAvailability} s, where the iteration is set and is
	 * overlapping with this iteration. this ignores all parent facilities'
	 * availabilities!
	 * 
	 * @param fromTo
	 *            timeframe to check
	 * @param facilityKey
	 *            the key of the facilities, all saved time frames in databases
	 *            are asked for.
	 * @return all {@link FacilityAvailability}s, that overlaps with the given
	 *         time frame.
	 */
	public List<FacilityAvailability> getFacilityAvailabilitiesIgnoreParents(TimeFrame fromTo, String facilityKey) {
		List<FacilityAvailability> results = new ArrayList<FacilityAvailability>();
		List<FacilityAvailability> candidates = this.getFacilityAvailabilities(facilityKey);
		for (FacilityAvailability candidate : candidates) {
			if (candidate.overlaps(fromTo)) {
				results.add(candidate);
			}
		}
		return results;
	}

	/**
	 * return all {@link FacilityAvailability}s for the facility, that overlaps
	 * with the given {@link TimeFrame}. this includes all
	 * {@link FacilityAvailability} s, where the iteration is set and is
	 * overlapping with this iteration and {@link FacilityAvailability}s set for
	 * parents of given facilityKey.
	 * 
	 * @param fromTo
	 *            timeframe to check
	 * @param facility
	 *            the facility, all saved time frames in databases are asked
	 *            for.
	 * @return all {@link FacilityAvailability}s, that overlaps with the given
	 *         time frame.
	 */
	public List<FacilityAvailability> getFacilityAvailabilitiesFollowingParents(TimeFrame fromTo, Facility facility) {
		List<FacilityAvailability> result = this.getFacilityAvailabilitiesIgnoreParents(fromTo, facility.getKey());
		if (facility.hasParent()) {
			result.addAll(this.getFacilityAvailabilitiesFollowingParents(fromTo, facility.getParentFacility()));
		}
		return result;
	}

	/**
	 * return all facility availabilities set for the given facility. sortation:
	 * by the start of the base time frame.
	 * 
	 * @param facilityKey
	 *            key representing a facility.
	 * @return all facility availabilities set for the given facility.
	 */
	public abstract List<FacilityAvailability> getFacilityAvailabilities(String facilityKey);

	/**
	 * return true, if {@link FacilityAvailability} cannot be saved. that's if:
	 * <ul>
	 * <li>facility key does not exist</li>
	 * <li>information about user set this is missed</li>
	 * <li>
	 * given {@link FacilityAvailability} is a {@link FacilityAvailability} and
	 * it is maybe available.</li>
	 * </ul>
	 * 
	 * @param tf
	 *            to check
	 * @return true, if {@link FacilityAvailability} cannot be saved.
	 */
	@Override
	protected boolean isDataIntegrityViolation(FacilityAvailability tf, boolean onInsert) {
		boolean result = !FacilityConfigDao.getInstance().keyExists(tf.getFacilityKey());
		if (result == false) {
			result = tf.getUserSetThis() == null;
		}
		// throw exception if it is a FacilityAvailability that is maybe
		// available
		if (result == false) {
			try {
				FacilityAvailability tester = tf;
				if (tester.isMaybeAvailable()) {
					result = true;
				}
			} catch (Exception e) {
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected void logAndThrowDataIntegrityViolationException(FacilityAvailability dataholder) throws DataIntegrityViolationException {
		String mess = "insert fail on " + dataholder + ".";
		DataIntegrityViolationException ex = new DataIntegrityViolationException(mess);
		FamLog.logException(FacilityDao.class, ex, mess, 200904241155l);
		throw ex;
	}

	/** {@inheritDoc} */
	@Override
	protected void logInsert(FacilityAvailability dataholder) {
		FamLog.logInfo(FacilityDao.class, "insert " + dataholder + ".", 200904241155l);
	}

	/** {@inheritDoc} */
	@Override
	protected void logUpdate(FacilityAvailability dataholder) {
		FamLog.logInfo(FacilityDao.class, "update " + dataholder + ".", 200911181627l);
	}

	/** {@inheritDoc} */
	@Override
	protected void setIdToNextId(FacilityAvailability dataholder) {
		if (idToSet == null) {
			idToSet = this.getBiggestId();
		}
		dataholder.setFacilityAvailabilityId(++idToSet);
	}

	/**
	 * return the current sudden failure on given facility or null, if nothing
	 * exists.
	 * 
	 * @param d
	 *            facility the sudden failure is for
	 * @return the current sudden failure on given facility or null, if nothing
	 *         exists.
	 */
	public FacilityAvailability getCurrentSuddenFailure(Facility d) {
		FacilityAvailability result = null;
		List<FacilityAvailability> das = FamDaoProxy.facilityDao().getFacilityAvailabilities(d);
		for (FacilityAvailability da : das) {
			if (da.isNotAvailableBecauseOfSuddenFailure() && !da.getBasePeriodOfTime().endsInPast()) {
				result = da;
				break;
			}
		}
		return result;
	}

	/**
	 * stop the current sudden failure on the given facility. if the facility has
	 * no sudden failure or does not exist, do nothing here. on updating the
	 * sudden failure, the user is set to the given user. timestamp set is set
	 * to current time stamp. the end of the base time for the facility
	 * availability is set to now.
	 * 
	 * @param d
	 *            the current sudden failure is stop of
	 * @param userSetThis
	 *            set as the user that stops the current sudden failure
	 */
	public void stopCurrentSuddenFailure(Facility d, User userSetThis) {
		FacilityAvailability da = FamDaoProxy.facilityDao().getCurrentSuddenFailure(d);
		if (da != null) { // bad request
			TimeFrame base = da.getBasePeriodOfTime();
			base.setEnd(Calendar.getInstance());
			da.setBasePeriodOfTime(base);
			da.setUserSetThis(userSetThis);
			da.setTimeStampSet(new Date());
			da.update();
		}
	}

	/**
	 * return the biggest id set in container. if no object / row there, return
	 * 0.
	 * 
	 * @return the biggest id set in container.
	 */
	protected abstract long getBiggestId();

	/**
	 * return all {@link FacilityAvailability}s and its parents of given
	 * facilityKey. the result is unmerged and with parents.
	 * 
	 * @param facilityKey
	 *            of returned {@link FacilityAvailability}s. parents are
	 *            returned as well.
	 * @return all {@link FacilityAvailability}s and its parents of given
	 *         facilityKey.
	 */
	public List<FacilityAvailability> getFacilityAvailabilitiesFollowingParents(String facilityKey) {
		return this.getFacilityAvailabilitiesFollowingParents(FacilityConfigDao.facility(facilityKey));
	}

	/**
	 * return all {@link FacilityAvailability}s and its parents of given
	 * facilityKey. the result is unmerged and with parents.
	 * 
	 * @param facility
	 *            of returned {@link FacilityAvailability}s. parents are
	 *            returned as well.
	 * @return all {@link FacilityAvailability}s and its parents of given
	 *         facilityKey.
	 */
	private List<FacilityAvailability> getFacilityAvailabilitiesFollowingParents(Facility facility) {
		List<FacilityAvailability> result = new ArrayList<FacilityAvailability>(this.getFacilityAvailabilities(facility));
		if (facility.hasParent()) {
			result.addAll(this.getFacilityAvailabilitiesFollowingParents(facility.getParentFacility()));
		}
		return result;
	}

	/**
	 * return all {@link FacilityAvailability}s and its parents of given
	 * facilityKey.
	 * 
	 * all time frames are merged by facilities. the results will have lost the
	 * {@link FacilityAvailability#timeStampSet}.
	 * 
	 * children have higher priority then parents. if the parent say "available"
	 * and the child say "not available", the result will say "not available".
	 * 
	 * @param fromTo
	 *            get only {@link FacilityAvailability}s overlapping this
	 *            {@link TimeFrame}.
	 * @param facilityKey
	 *            of returned {@link FacilityAvailability}s. parents are
	 *            returned as well.
	 * @return all {@link FacilityAvailability}s and its parents of given
	 *         facilityKey.
	 */
	public List<FacilityAvailability> getFacilityAvailabilitiesMergedByFacilities(TimeFrame fromTo, String facilityKey) {
		// get all FacilityAvailabilitys of given facilityKey
		List<FacilityAvailability> mergeThis = FamDaoProxy.facilityDao().getFacilityAvailabilitiesIgnoreParents(fromTo, facilityKey);

		// add priority (in wrong order first)
		List<String> reverseOrderFacilityPriority = new ArrayList<String>();
		reverseOrderFacilityPriority.add(facilityKey);

		// do that with parent and parent's parent
		while (FacilityConfigDao.getInstance().hasParentFacility(facilityKey)) {
			String parentKey = FacilityConfigDao.getInstance().getParentFacilityKey(facilityKey);
			List<FacilityAvailability> dasParents = FamDaoProxy.facilityDao().getFacilityAvailabilitiesIgnoreParents(fromTo, parentKey);
			reverseOrderFacilityPriority.add(parentKey);
			mergeThis.addAll(dasParents);
			facilityKey = parentKey;
		}

		// switch priority order
		List<String> orderedFacilityPriority = new ArrayList<String>();
		for (int i = reverseOrderFacilityPriority.size() - 1; i >= 0; i--) {
			orderedFacilityPriority.add(reverseOrderFacilityPriority.get(i));
		}

		// get result
		return FacilityAvailabilityMerger.getMergedByFacilities(mergeThis, fromTo, orderedFacilityPriority);
	}

	/**
	 * return all {@link FacilityAvailability}s for facility and of given day.
	 * {@link FacilityAvailability}s are merged.
	 * 
	 * @see FacilityDao#getFacilityAvailabilitiesMergedByFacilities(de.knurt.heinzelmann.util.time.TimeFrame,
	 *      java.lang.String)
	 * @param c
	 *            day of interest
	 * @param facility
	 *            of interest
	 * @return all {@link FacilityAvailability}s for facility and of given day.
	 */
	public List<FacilityAvailability> getFacilityAvailabilitiesOfDay(Calendar c, Facility facility) {
		TimeFrame tf = SimpleTimeFrame.getDay(c);
		return this.getFacilityAvailabilitiesMergedByFacilities(tf, facility.getKey());
	}

	/**
	 * return all facility availabilities set for the given facility.
	 * 
	 * @param facility
	 *            given.
	 * @return all facility availabilities set for the given facility.
	 */
	public List<FacilityAvailability> getFacilityAvailabilities(Facility facility) {
		return this.getFacilityAvailabilities(facility.getKey());
	}

	/**
	 * return a list of keys representing the facilities the user is responsible
	 * for. if the user is unknown for responsibilities, return
	 * <code>null</code>:
	 * 
	 * @param user
	 *            a list of keys representing the facilities is returned of.
	 * @return a list of keys representing the facilities the user is
	 *         responsible for.
	 */
	public abstract List<String> getFacilityKeysUserIsResponsibleFor(User user);

	/**
	 * return <code>true</code> if the user has at least responsibility for one
	 * facility. otherwise return <code>false</code>
	 * 
	 * @param user
	 *            to check
	 * @return true if the user has at least responsibility for one facility.
	 */
	public abstract boolean hasResponsibilityForAFacility(User user);

	/**
	 * return a list of bookable facilities, the user is responsible for or an
	 * empty array, if the user does not have any responsibilities for a
	 * bookable facility.
	 * 
	 * @param user
	 *            of interest
	 * @return a list of bookable facilities, the user is responsible for or
	 *         null.
	 */
	public abstract List<Facility> getBookableFacilitiesUserIsResponsibleFor(User user);

	/**
	 * return a list of facilities, the user is responsible for or an empty
	 * array, if the user does not have any responsibilities for a facility.
	 * 
	 * @param user
	 *            of interest
	 * @return a list of facilities, the user is responsible for or null.
	 */
	public abstract List<Facility> getFacilitiesUserIsResponsibleFor(User user);

	/**
	 * set the responsibilities of the given user to the given facilities
	 * 
	 * @param user
	 *            is responsible for the given facilities
	 * @param facilities
	 *            the given user is responsible for
	 * @return true if update succeeded
	 */
	public abstract boolean updateResponsibility(User newUser, List<Facility> facilities);

	/**
	 * return true, if the given user has the responsibility for the given
	 * facility
	 * 
	 * @param user
	 *            to check
	 * @param facility
	 *            to check
	 * @return true, if the given user has the responsibility for the given
	 *         facility
	 */
	public boolean hasResponsibilityForFacility(User user, Facility facility) {
		boolean result = false;
		List<Facility> facilities = this.getFacilitiesUserIsResponsibleFor(user);
		for (Facility candidate : facilities) {
			if (candidate.getKey().equals(facility.getKey())) {
				result = true;
				break;
			}
		}
		return result;
	}

}
