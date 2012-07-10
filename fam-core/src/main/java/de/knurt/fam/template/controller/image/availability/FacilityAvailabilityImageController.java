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
package de.knurt.fam.template.controller.image.availability;

import java.util.List;

import de.knurt.fam.core.model.persist.FacilityAvailability;

/**
 * a controller controlling visualization of the availability of facilities.
 * @see FacilityAvailability
 * @since 0.20090702 (07/02/2009)
 * @author Daniel Oltmanns <info@knurt.de>
 */
interface FacilityAvailabilityImageController {

    /**
     * return the facility availabilites the shall be shown
     * @return the facility availabilites the shall be shown
     */
    List<FacilityAvailability> getFacilityAvailabilitiesToShow();
}
