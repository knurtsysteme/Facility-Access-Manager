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
package de.knurt.fam.core.persistence.dao.ibatis;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FacilityDao;
import de.knurt.fam.core.persistence.dao.config.FacilityConfigDao;

/**
 * dao for every {@link LogbookEntry} stored in sql
 * 
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
public class FacilityDao4ibatis extends FacilityDao {

	/** {@inheritDoc} */
	@Override
	protected synchronized boolean internInsert(FacilityAvailability dataholder) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().insert("facility.availability.insert", dataholder);
		} catch (Exception e) {
			FamLog.exception(e, 201205071125l);
		}
		return result;

	}

	/** {@inheritDoc} */
	@Override
	protected synchronized boolean internUpdate(FacilityAvailability dataholder) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().update("facility.availability.update", dataholder);
		} catch (Exception e) {
			FamLog.exception(e, 201205071126l);
		}
		return result;

	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean delete(FacilityAvailability deleteThis) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().delete("facility.availability.delete", deleteThis, 1);
			result = true;
		} catch (Exception e) {
			FamLog.exception(e, 201204231012l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public List<FacilityAvailability> getObjectsLike(FacilityAvailability example) {
		return FamSqlMapClientDaoSupport.sqlMap().queryForList("facility.availability.select.where", example);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public List<FacilityAvailability> getAll() {
		return FamSqlMapClientDaoSupport.sqlMap().queryForList("facility.availability.select.all");
	}

	/** {@inheritDoc} */
	@Override
	public List<FacilityAvailability> getFacilityAvailabilities(String facilityKey) {
		FacilityAvailability da = new FacilityAvailability();
		da.setFacilityKey(facilityKey.trim());
		List<FacilityAvailability> result = this.getObjectsLike(da);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	protected long getBiggestId() {
		Integer result = (Integer) FamSqlMapClientDaoSupport.sqlMap().queryForObject("facility.availability.select.idmax");
		return result == null ? 0 : result.longValue();
	}

	/** {@inheritDoc} */
	@Override
	public List<Facility> getBookableFacilitiesUserIsResponsibleFor(User user) {
		List<Facility> result = new ArrayList<Facility>();
		List<Facility> facilities = this.getFacilitiesUserIsResponsibleFor(user);
		for (Facility facility : facilities) {
			if (facility.isBookable()) {
				result.add(facility);
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getFacilityKeysUserIsResponsibleFor(User user) {
		if (FamConnector.sqlConfigured()) {
			return FamSqlMapClientDaoSupport.sqlMap().queryForList("facility.responsibility.select.of.user", user);
		} else {
			return new ArrayList<String>(0);
		}
	}

	/** {@inheritDoc} */
	@Override
	public List<Facility> getFacilitiesUserIsResponsibleFor(User user) {
		List<String> keys = this.getFacilityKeysUserIsResponsibleFor(user);
		List<Facility> result = new ArrayList<Facility>(keys.size());
		for (String key : keys) {
			result.add(FacilityConfigDao.facility(key));
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasResponsibilityForAFacility(User user) {
		return this.getFacilitiesUserIsResponsibleFor(user).size() > 0;
	}

	@Override
	public boolean updateResponsibility(User user, List<Facility> facilities) {
		boolean result = false;
		try {
			FamSqlMapClientDaoSupport.sqlMap().update("facility.responsibility.update.remove", user);
			for (Facility facility : facilities) {
				Properties params = new Properties();
				params.put("username", user.getUsername());
				params.put("facility_key", facility.getKey());
				FamSqlMapClientDaoSupport.sqlMap().insert("facility.responsibility.insert", params);
			}
			result = true;
		} catch (Exception e) {
			FamLog.exception(e, 201205181313l);
		}
		return result;
	}
}