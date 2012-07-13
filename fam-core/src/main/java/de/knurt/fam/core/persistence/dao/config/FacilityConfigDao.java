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
package de.knurt.fam.core.persistence.dao.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.view.text.FamText;

/**
 * DAO do access facilities configured.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090324 (03/24/2009)
 */
public class FacilityConfigDao extends AbstractConfigDao<Facility> {

	private volatile static FacilityConfigDao me;

	/**
	 * return the key representing given facility.
	 * 
	 * @param facility
	 *            given
	 * @return the key representing given facility.
	 */
	public static String key(Facility facility) {
		return getInstance().getKey(facility);
	}

	/**
	 * return true, if the given key is representing a configured facility.
	 * 
	 * @param facilityKey
	 *            to check
	 * @return true, if the given key is representing a configured facility.
	 */
	public static boolean isKey(String facilityKey) {
		return getInstance().keyExists(facilityKey);
	}

	private FacilityBookable unknownFacility;

	/**
	 * return the unknown bookable facility. if there are bookings for
	 * facilities, that have been removed after this booking, there must be a
	 * facility to still handle that bookings. this is the facility to use in
	 * that case.
	 * 
	 * @return the unknown bookable facility.
	 */
	public static FacilityBookable getUnknownBookableFacility() {
		return getInstance().unknownFacility;
	}

	/**
	 * return the bookable facility for the given key or null, if key is null or
	 * not a bookable facility.
	 * 
	 * @param facilityKey
	 *            for the bookable facility to return
	 * @return the bookable facility for the given key or null, if key is null
	 *         or not a bookable facility.
	 */
	public static FacilityBookable bookableFacility(String facilityKey) {
		return (FacilityBookable) facility(facilityKey);
	}

	/**
	 * return the default booking rules for the given facility.
	 * 
	 * @param facilityKey
	 *            representing a facility
	 * @return the default booking rules for the given facility.
	 */
	public static BookingRule bookingRule(String facilityKey) {
		return bookableFacility(facilityKey).getBookingRule();
	}

	/**
	 * return the facility represented by the given key.
	 * 
	 * @param facilityKey
	 *            representing a facility
	 * @return the facility represented by the given key.
	 */
	public static Facility facility(String facilityKey) {
		Facility result = getInstance().getConfiguredInstance(facilityKey);
		return result == null ? getInstance().unknownFacility : result;
	}

	/**
	 * return the label of this facility. this is used for presenting the
	 * facility to a user.
	 * 
	 * @param facility
	 *            the label is returned of
	 * @return the label of this facility.
	 */
	public static String label(Facility facility) {
		return getInstance().getLabel(key(facility));
	}

	/**
	 * return the label of the given facility.
	 * 
	 * @param facilityKey
	 *            representing a facility.
	 * @return the label of the given facility.
	 */
	public static String label(String facilityKey) {
		return label(facility(facilityKey));
	}

	private Properties labels;
	private Map<String, Facility> configuredInstances;

	private FacilityConfigDao() {
	}

	/**
	 * return the one and only instance of RoleConfigDao
	 * 
	 * @return the one and only instance of RoleConfigDao
	 */
	public static FacilityConfigDao getInstance() {
		if (me == null) { // no instance so far
			synchronized (FacilityConfigDao.class) {
				if (me == null) { // still no instance so far
					me = new FacilityConfigDao(); // the one and only
				}
			}
		}
		return me;
	}

	/**
	 * return all facilities that can be booked.
	 * 
	 * @return all facilities that can be booked.
	 */
	public List<FacilityBookable> getBookableFacilities() {
		List<FacilityBookable> result = new ArrayList<FacilityBookable>();
		for (Facility facility : this.getCollectionOfAllConfigured()) {
			if (this.isBookable(facility)) {
				result.add((FacilityBookable) facility);
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public String getKey(Facility facility) {
		String result = super.getKey(facility);
		if (result.isEmpty()) {
			result = "unknownFacility";
		}
		return result;
	}

	/**
	 * return {@link Facility}s and do it like
	 * {@link #getChildrenKeys(java.lang.String)} does.
	 * 
	 * @param rootFacility
	 *            as parent
	 * @return {@link Facility}s and do it like
	 *         {@link #getChildrenKeys(java.lang.String)} does.
	 */
	public List<Facility> getChildrenFacilities(Facility rootFacility) {
		return this.getChildrenFacilities(rootFacility, false);
	}

	/**
	 * return {@link Facility}s and do it like
	 * {@link #getChildrenKeys(java.lang.String)} does.
	 * 
	 * @param rootFacility
	 *            as parent
	 * @param allGenerations
	 *            if true, return children's children (all generations). if
	 *            false return only direct children.
	 * @return {@link Facility}s and do it like
	 *         {@link #getChildrenKeys(java.lang.String)} does.
	 */
	public List<Facility> getChildrenFacilities(Facility rootFacility, boolean allGenerations) {
		List<Facility> result = new ArrayList<Facility>();
		List<String> childrenKeys = this.getChildrenKeys(rootFacility.getKey());
		for (String childrenKey : childrenKeys) {
			result.add(facility(childrenKey));
			if (allGenerations) {
				result.addAll(this.getChildrenFacilities(facility(childrenKey), allGenerations));
			}
		}
		return result;
	}

	/**
	 * return true, if the given facility can be booked. if the given facility
	 * is the unknown facility, return true as well.
	 * 
	 * @param facility
	 *            to check
	 * @return true, if the given facility can be booked.
	 */
	public boolean isBookable(Facility facility) {
		boolean result = facility != null;
		if (result && facility.getKey().equals(this.unknownFacility.getKey())) {
			result = true;
		} else {
			if (result) {
				result = this.keyExists(facility.getKey());
			}
			if (result) {
				try {
					@SuppressWarnings("unused")
					Facility bd = (FacilityBookable) facility;
				} catch (ClassCastException e) {
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * return true, if the facility is bookable.
	 * 
	 * @param facilityKey
	 *            key of the facility
	 * @return true, if the facility is bookable.
	 */
	public boolean isBookable(String facilityKey) {
		return this.isBookable(this.configuredInstances.get(facilityKey));
	}

	/**
	 * return true, if given facility is bookable.
	 * 
	 * @param facilityKey
	 *            representing given facility
	 * @return true, if given facility is bookable.
	 */
	public static boolean bookable(String facilityKey) {
		return facilityKey != null && getInstance().isBookable(facilityKey);
	}

	/**
	 * return keys of direct children of facility with given key. this does not
	 * contain the given key.
	 * 
	 * @param parentKey
	 *            parent key of returned children keys.
	 * @return keys of direct children of facility with given key.
	 */
	public List<String> getChildrenKeys(String parentKey) {
		ArrayList<String> result = new ArrayList<String>();
		for (String key : this.getKeys()) {
			Facility parentOfKey = this.configuredInstances.get(key).getParentFacility();
			if (parentOfKey != null && parentOfKey.equals(this.configuredInstances.get(parentKey))) {
				result.add(key);
			}
		}
		return result;
	}

	/**
	 * return all labels of all facilities. the message base is the beginning of
	 * a property key. all messages are in a properties files for
	 * internationalization that can be found in
	 * {@link "de.knurt.fam.core.view.text"}. this one is the file
	 * "facilities.properties".
	 * 
	 * @return all labels of all facilities
	 */
	public Properties getLabels() {
		if (this.labels == null) {
			this.labels = new Properties();
			for (String key : this.configuredInstances.keySet()) {
				labels.put(key, FamText.getInstance().getMessage(key + ".label"));
			}
		}
		return this.labels;
	}

	/**
	 * return the label of the given facility.
	 * 
	 * @param key
	 *            representing a facility.
	 * @return the label of the given facility.
	 */
	public String getLabel(String key) {
		if (this.keyExists(key) == false) {
			return FamText.getInstance().getMessage("unknownFacility.label");
		} else {
			return this.getLabels().get(key).toString();
		}
	}

	/**
	 * return all keys representing facilities that have no parents (are root
	 * facilities).
	 * 
	 * @return all keys representing facilities that have no parents (are root
	 *         facilities).
	 */
	public String getRootKey() {
		String result = null;
		for (String key : this.getKeys()) {
			if (this.configuredInstances.get(key).hasParent() == false) {
				result = key;
				break;
			}
		}
		return result;
	}

	/**
	 * return true, if the given facility has a parent (and is not a root
	 * facility).
	 * 
	 * @param facilityKey
	 *            representing a facility
	 * @return true, if the given facility has a parent (and is not a root
	 *         facility).
	 */
	public boolean hasParentFacility(String facilityKey) {
		return this.getParentFacilityKey(facilityKey) != null;
	}

	/**
	 * return the key representing the parent of the given facility. return
	 * null, if facility is root facility.
	 * 
	 * @param child
	 *            representing the given facility
	 * @return the key representing the parent of the given facility.
	 */
	public String getParentFacilityKey(String child) {
		String result = null;
		for (String key : this.getKeys()) {
			if (this.getChildrenKeys(key).contains(child)) {
				result = key;
			}
		}
		return result;
	}

	/**
	 * set all configured facilities here.
	 * 
	 * @param configuredInstances
	 *            all facilities.
	 */
	@Required
	@Override
	public void setConfiguredInstances(Map<String, Facility> configuredInstances) {
		this.configuredInstances = configuredInstances;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Facility> getConfiguredInstances() {
		return this.configuredInstances;
	}

	/**
	 * return all {@link FacilityBookable} that are children of given root
	 * facility.
	 * 
	 * @param rootFacilityKey
	 *            key of given root facility
	 * @return all {@link FacilityBookable} that are children of given root
	 *         facility.
	 */
	public List<FacilityBookable> getBookableChildrenFacilities(String rootFacilityKey) {
		List<FacilityBookable> result = new ArrayList<FacilityBookable>();
		List<Facility> candidates = this.getChildrenFacilities(this.getConfiguredInstance(rootFacilityKey));
		for (Facility candidate : candidates) {
			String candidateKey = candidate.getKey();
			if (bookable(candidateKey)) {
				result.add(bookableFacility(candidateKey));
			}
		}
		return result;
	}

	/**
	 * set an unknown facility. if a facility is booked and deleted out of the
	 * configuration, this is the facility used to show in the application.
	 * 
	 * @param unknownFacility
	 *            the Facility that is unknown to set
	 */
	@Required
	public void setUnknownFacility(FacilityBookable unknownFacility) {
		this.unknownFacility = unknownFacility;
	}

	public Facility getRootFacility() {
		return this.getConfiguredInstance(this.getRootKey());
	}

	public static Facility[] facilities(String[] keys) {
		if (keys == null) {
			return new Facility[0];
		} else {
			Facility[] result = new Facility[keys.length];
			int i = 0;
			for (String key : keys) {
				result[i++] = facility(key);
			}
			return result;
		}
	}

	public static List<FacilityBookable> bookablefacilities(List<String> keys) {
		List<FacilityBookable> result = new ArrayList<FacilityBookable>();
		for (String key : keys) {
			Facility candidate = FacilityConfigDao.facility(key);
			if (candidate.isBookable()) {
				result.add((FacilityBookable) candidate);
			}
		}
		return result;
	}
}
