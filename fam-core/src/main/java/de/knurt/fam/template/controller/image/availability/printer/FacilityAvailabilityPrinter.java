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
package de.knurt.fam.template.controller.image.availability.printer;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.util.time.FacilityAvailabilityMerger;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * print a given set of availabilities using a specific artist for that.
 * 
 * @see FacilityAvailabilityArtist
 * @see FacilityAvailability#getAvailable()
 * @author Daniel Oltmanns
 * @since 0.20090518 (05/18/2009)
 */
public class FacilityAvailabilityPrinter {

	private List<FacilityAvailability> dasGeneralNotAvailables;
	private List<FacilityAvailability> dasBookedNotAvailables;
	private List<FacilityAvailability> dasMaintenanceNotAvailables;
	private List<FacilityAvailability> dasMaybeAvailables;
	private List<FacilityAvailability> dasMustNotStartHere;
	private List<FacilityAvailability> dasFailureNotAvailables;
	private List<FacilityAvailability> dasCompletelyAvailables;
	private FacilityAvailabilityArtist daa;

	/**
	 * construct a printer for given availablities, artist and time frame.
	 * 
	 * @param das
	 *            set of availabilities to print out
	 * @param daa
	 *            artist using for printings
	 * @param askFor
	 *            time frame being printed out
	 */
	public FacilityAvailabilityPrinter(List<FacilityAvailability> das, FacilityAvailabilityArtist daa, TimeFrame askFor) {
		// set all unset completely available
		// create timeframe with availables
		FacilityAvailability exampleGrounding = new FacilityAvailability("", askFor.getCalendarStart(), askFor.getCalendarEnd());
		exampleGrounding.setTimeStampSet(-1); // set to ice age
		if (askFor.endsInPast()) {
			exampleGrounding.setNotAvailableInGeneral();
		} else {
			exampleGrounding.setCompletelyAvailable();
		}
		das.add(exampleGrounding);
		das = FacilityAvailabilityMerger.getMergedByTimeStampSet(das, askFor);

		this.dasGeneralNotAvailables = new ArrayList<FacilityAvailability>();
		this.dasBookedNotAvailables = new ArrayList<FacilityAvailability>();
		this.dasMaintenanceNotAvailables = new ArrayList<FacilityAvailability>();
		this.dasFailureNotAvailables = new ArrayList<FacilityAvailability>();
		this.dasMaybeAvailables = new ArrayList<FacilityAvailability>();
		this.dasMustNotStartHere = new ArrayList<FacilityAvailability>();
		this.dasCompletelyAvailables = new ArrayList<FacilityAvailability>();
		this.daa = daa;
		for (FacilityAvailability da : das) {
			if (da.isNotAvailableInGeneral()) {
				this.dasGeneralNotAvailables.add(da);
			} else if (da.isMaybeAvailable()) {
				this.dasMaybeAvailables.add(da);
			} else if (da.isNotAvailableBecauseOfBooking()) {
				this.dasBookedNotAvailables.add(da);
			} else if (da.mustNotStartHere()) {
				this.dasMustNotStartHere.add(da);
			} else if (da.isNotAvailableBecauseOfMaintenance()) {
				this.dasMaintenanceNotAvailables.add(da);
			} else if (da.isNotAvailableBecauseOfSuddenFailure()) {
				this.dasFailureNotAvailables.add(da);
			} else if (da.isCompletelyAvailable()) {
				this.dasCompletelyAvailables.add(da);
			}
		}
	}

	/**
	 * print out all facility availabilities using a method of the constructed
	 * {@link FacilityAvailabilityArtist} that matches the availability. concrete
	 * use ...
	 * <ul>
	 * <li>...
	 * {@link FacilityAvailabilityArtist#outCompletelyAvailable(de.knurt.fam.core.model.persist.FacilityAvailability)}
	 * if {@link FacilityAvailability#isCompletelyAvailable()}</li>
	 * <li>...
	 * {@link FacilityAvailabilityArtist#outMaybeAvailable(de.knurt.fam.core.model.persist.FacilityAvailability)}
	 * if {@link FacilityAvailability#isMaybeAvailable()}</li>
	 * <li>...
	 * {@link FacilityAvailabilityArtist#outNotAvailableBooking(de.knurt.fam.core.model.persist.FacilityAvailability)}
	 * if {@link FacilityAvailability#isNotAvailableBecauseOfBooking()}</li>
	 * <li>...
	 * {@link FacilityAvailabilityArtist#outNotAvailableMaintenance(de.knurt.fam.core.model.persist.FacilityAvailability)}
	 * if {@link FacilityAvailability#isNotAvailableBecauseOfMaintenance()}</li>
	 * <li>...
	 * {@link FacilityAvailabilityArtist#outNotAvailableFailure(de.knurt.fam.core.model.persist.FacilityAvailability)}
	 * if {@link FacilityAvailability#isNotAvailableBecauseOfSuddenFailure()}</li>
	 * <li>...
	 * {@link FacilityAvailabilityArtist#outNotAvailableFailure(de.knurt.fam.core.model.persist.FacilityAvailability)}
	 * if {@link FacilityAvailability#mustNotStartHere()}</li>
	 * </ul>
	 */
	public void out() {
		// print everything available - because of what is not set is available
		for (FacilityAvailability da : this.dasCompletelyAvailables) {
			this.daa.outCompletelyAvailable(da);
		}
		for (FacilityAvailability da : this.dasMaybeAvailables) {
			this.daa.outMaybeAvailable(da);
		}
		for (FacilityAvailability da : this.dasBookedNotAvailables) {
			this.daa.outNotAvailableBooking(da);
		}
		for (FacilityAvailability da : this.dasGeneralNotAvailables) {
			this.daa.outNotAvailableInGeneral(da);
		}
		for (FacilityAvailability da : this.dasMaintenanceNotAvailables) {
			this.daa.outNotAvailableMaintenance(da);
		}
		for (FacilityAvailability da : this.dasFailureNotAvailables) {
			this.daa.outNotAvailableFailure(da);
		}
		for (FacilityAvailability da : this.dasMustNotStartHere) {
			this.daa.outMustNotStartHere(da);
		}
	}
}
