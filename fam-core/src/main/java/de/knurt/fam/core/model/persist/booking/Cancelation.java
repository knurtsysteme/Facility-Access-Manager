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
package de.knurt.fam.core.model.persist.booking;

import java.util.Date;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;

/**
 * Container for cancelations. a cancelation contains the reason, the user that
 * canceled it and a date of cancelation (that is always the construction date).
 * 
 * @see Booking
 * @author Daniel Oltmanns
 * @since 0.20090711
 */
public class Cancelation {

  /**
   * reason for cancelation. the owner of the item being canceled, self-canceled
   * it
   */
  public final static String REASON_FREE_BY_USER = "canceled by user"; // INTLANG
  /**
   * reason for cancelation. the system has an unknown error and canceled it
   */
  public final static String REASON_UNKNOWN_ERROR = "canceled by the system because of an unknown error"; // INTLANG
  /**
   * reason for cancelation. anotherone with stronger rights has override this.
   */
  public final static String REASON_BOOKED_BY_ANOTHER = "another one got this time slot"; // INTLANG
  /**
   * reason for cancelation. the item being booked is not available anymore.
   */
  public final static String REASON_NOT_AVAILABLE_IN_GENERAL = "the facility is not available anymore (because of closing times, maintenence or accidents)"; // INTLANG
  /**
   * reason for cancelation. no reason given. this is used, if the reason is
   * unclear.
   */
  public final static String REASON_NO_REASON = "no reason given for this cancelation"; // INTLANG

  /**
   * construct a Cancelation
   * 
   * @param user being responsible for cancelation
   * @param reason for cancelation
   */
  public Cancelation(User user, String reason) {
    this.reason = reason;
    this.username = user.getUsername();
    this.dateCanceled = new Date();
  }

  private Cancelation(String username, String reason, Date dateCanceled) {
    this.reason = reason;
    this.username = username;
    this.dateCanceled = dateCanceled;
  }

  /**
   * return a cancelation used for orm. do not use this for any purposes then
   * for mapping.
   * 
   * @param username of user being responsible for cancelation
   * @param reason for cancelation
   * @param dateCanceled cancelation made
   * @return a cancelation used for orm.
   */
  public static Cancelation getCancelationForMapping(String username, String reason, Date dateCanceled) {
    return new Cancelation(username, reason, dateCanceled);
  }

  private String reason, username;
  private Date dateCanceled;

  /**
   * return the reason for the cancelation.
   * 
   * @return the reason for the cancelation.
   */
  public String getReason() {
    return reason;
  }

  /**
   * return the user being responsible for this cancelation.
   * 
   * @return the user being responsible for this cancelation.
   */
  public User getUser() {
    return FamDaoProxy.userDao().getUserFromUsername(this.username);
  }

  /**
   * set the reason for the cancelation.
   * 
   * @param reason for the cancelation.
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * return username of user being responsible for this cancelation.
   * 
   * @return username of user being responsible for this cancelation.
   */
  public String getUsername() {
    return username;
  }

  /**
   * set username of user being responsible for this cancelation.
   * 
   * @param username of user being responsible for this cancelation.
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * return the date, it cancelation made
   * 
   * @return the date cancelation made
   */
  public Date getDateCanceled() {
    return dateCanceled;
  }

  /**
   * set date, cancelation made
   * 
   * @param dateCanceled date, cancelation made
   */
  public void setDateCanceled(Date dateCanceled) {
    this.dateCanceled = dateCanceled;
  }
}
