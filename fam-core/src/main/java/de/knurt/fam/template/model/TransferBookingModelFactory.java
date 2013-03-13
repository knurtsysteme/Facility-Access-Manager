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
package de.knurt.fam.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * model factory for model of transferbooking.html
 * 
 * @author Daniel Oltmanns
 * @since 1.8.1 (03/12/2013)
 */
public class TransferBookingModelFactory {

  public Properties getProperties(TemplateResource templateResource) {
    Properties result = new Properties();
    Booking booking = RequestInterpreter.getBooking(templateResource.getRequest());
    User auth = templateResource.getAuthUser();
    if (booking != null && auth != null && booking.getUsername().equals(auth.getUsername())) {
      result.put("booking", booking);
      FacilityBookable facility = booking.getFacility();
      result.put("facility", facility);
      // all users being able to receive the booking
      List<User> receivers = new ArrayList<User>();
      List<User> candidates = FamDaoProxy.userDao().getAllUsersWithAccount();
      for(User candidate : candidates) {
        if(candidate.isAllowedToAccess(facility) && !candidate.is(auth)) receivers.add(candidate);
      }
      result.put("users", receivers);
    }
    return result;
  }
}
