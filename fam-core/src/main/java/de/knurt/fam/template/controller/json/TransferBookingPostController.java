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
package de.knurt.fam.template.controller.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mail.OutgoingUserMailBox;
import de.knurt.fam.core.util.mvc.RequestInterpreter;

/**
 * transfer booking
 * 
 * @author Daniel Oltmanns
 * @since 1.9.0 (03/13/2013)
 */
public class TransferBookingPostController extends JSONController2 {

  private boolean succ = false;
  private String errormessage = null;
  private static final String SUCCMESSAGE = "Thank you for end your current session!"; // INTLANG
  private User user = null;

  public TransferBookingPostController(HttpServletRequest request) {
    user = SessionAuth.user(request);
    if (user == null) {
      errormessage = "Your session expired. Please reload page and log in again."; // INTLANG
    } else {
      Booking booking = RequestInterpreter.getBooking(request);
      if (user.is(booking.getUser())) {
        User receiver = RequestInterpreter.getUser(request);
        if (receiver == null) {
          errormessage = "Invalid request.";
        } else {
          succ = booking.transferTo(receiver);
          if (succ) OutgoingUserMailBox.insert_BookingTransfer(user, receiver, booking);
          else errormessage = "Unknown error [201303131219]";
        }
      } else {
        errormessage = "Your session expired. Please reload page and log in again."; // INTLANG
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public JSONObject getJSONObject() {
    JSONObject result = new JSONObject();
    try {
      result.put("succ", succ);
      if (succ) {
        result.put("succmessage", SUCCMESSAGE);
      } else {
        result.put("errormessage", errormessage);
      }
    } catch (JSONException e) {
      FamLog.exception(e, 201209031219l);
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public void onIOException(IOException ex) {
    FamLog.exception(ex, 201303131037l);
  }

}
