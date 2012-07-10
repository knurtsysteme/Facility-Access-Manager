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

import de.knurt.fam.core.model.persist.FacilityAvailability;

/**
 * print out {@link FacilityAvailability}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090904 (09/04/2009)
 */
public interface FacilityAvailabilityArtist {

	/**
	 * print out the given facility availability as it is if it is not available
	 * in general.
	 * 
	 * @see FacilityAvailability#isNotAvailableInGeneral()
	 * @param da
	 *            to print out.
	 */
	public void outNotAvailableInGeneral(FacilityAvailability da);

	/**
	 * print out the given facility availability as it is if it is not available
	 * because of maintenance.
	 * 
	 * @see FacilityAvailability#isNotAvailableBecauseOfMaintenance()
	 * @param da
	 *            to print out.
	 */
	public void outNotAvailableMaintenance(FacilityAvailability da);

	/**
	 * print out the given facility availability as it is if it is not available
	 * because of a failure.
	 * 
	 * @see FacilityAvailability#isNotAvailableBecauseOfSuddenFailure()
	 * @param da
	 *            to print out.
	 */
	public void outNotAvailableFailure(FacilityAvailability da);

	/**
	 * print out the given facility availability as it is if it is maybe
	 * available.
	 * 
	 * @see FacilityAvailability#isMaybeAvailable()
	 * @param da
	 *            to print out.
	 */
	public void outMaybeAvailable(FacilityAvailability da);

	/**
	 * print out the given facility availability as it is if it is not available
	 * because of it is booked up.
	 * 
	 * @see FacilityAvailability#isNotAvailableBecauseOfBooking()
	 * @param da
	 *            to print out.
	 */
	public void outNotAvailableBooking(FacilityAvailability da);

	/**
	 * print out the given facility availability as it is if it is available.
	 * 
	 * @see FacilityAvailability#isCompletelyAvailable()
	 * @param da
	 *            to print out.
	 */
	public void outCompletelyAvailable(FacilityAvailability da);

	/**
	 * print out the given facility availability as it is if it must not start
	 * here
	 * 
	 * @see FacilityAvailability#mustNotStartHere()
	 * @param da
	 *            to print out
	 */
	public void outMustNotStartHere(FacilityAvailability da);
}
