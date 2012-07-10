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
package de.knurt.fam.core.content.html;

import java.util.List;

import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.util.mvc.QueryKeys;

/**
 * create overviews of all or some facilities. an overview might be a list or a
 * tree.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090421 (04/21/2009)
 */
public class FacilityOverviewHtml extends FacilityHtml {

	private static boolean shallBeShownAsLink(Facility facility, List<Facility> facilities2link) {
		boolean result = facility != null && facilities2link != null;
		if (result) {
			boolean facilityShallBeLinked = false;
			for (Facility facility2link : facilities2link) {
				if (facility2link.getKey().equals(facility.getKey())) {
					facilityShallBeLinked = true;
					break;
				}
			}
			result = facilityShallBeLinked;
		}
		return result;
	}

	private static String getSheet(Facility facility, String linkbase, List<Facility> facilities2link) {
		String label = facility.getLabel();
		if (shallBeShownAsLink(facility, facilities2link)) { // shall be shown
			return String.format("<a href=\"%s?%s=%s\" alt=\"%s\">%s</a>", linkbase, QueryKeys.QUERY_KEY_FACILITY, facility.getKey(), label, label);
		} else {
			return label;
		}
	}

	private static String getBranch(Facility facility, String linkbase, List<Facility> facilities2link) {
		String result = "<li>";
		result += getSheet(facility, linkbase, facilities2link);
		List<Facility> childrenFacilities = FacilityConfigDao.getInstance().getChildrenFacilities(facility);
		if (childrenFacilities.size() > 0) {
			for (Facility childFacility : childrenFacilities) {
				result += getTree(childFacility, linkbase, facilities2link);
			}
		}
		result += "</li>";
		return result;
	}

	/**
	 * return html of the tree of one given root facility. every entry in the
	 * key will be a link with a linkbase if and only if it is the given
	 * class2link. the key of the link is {@link QueryKeys#QUERY_KEY_FACILITY},
	 * value is the facilityKey of the facility. an entry will be
	 * <code><a href="linkbase?queryKeyDevic=facilityKey">label</a></code>
	 * 
	 * @param facility
	 *            as root of the tree
	 * @param linkbase
	 *            base of the key of a facility as
	 * @param facilities2link
	 *            list of the facilities, that shall be displayed as link
	 * @return html of all root facilities.
	 */
	public static String getTree(Facility facility, String linkbase, List<Facility> facilities2link) {
		String result = "<ul>";
		result += getBranch(facility, linkbase, facilities2link);
		result += "</ul>";
		return result;
	}

	/**
	 * return html of all root facilities. entry in the trees will be a link if
	 * and only if it is the given class. the key of the link is
	 * {@link QueryKeys#QUERY_KEY_FACILITY}, value is the facilityKey of the
	 * facility. an entry will be
	 * <code><a href="linkbase?queryKeyDevic=facilityKey">label</a></code>
	 * 
	 * @param linkbase
	 *            base of the key of a facility as
	 * @param facilities2link
	 *            a list of the facilities that shall be shown as a link
	 * @return html of all root facilities.
	 */
	public static String getRootTree(String linkbase, List<Facility> facilities2link) {
		return getTree(FacilityConfigDao.facility(FacilityConfigDao.getInstance().getRootKey()), linkbase, facilities2link);
	}
}
