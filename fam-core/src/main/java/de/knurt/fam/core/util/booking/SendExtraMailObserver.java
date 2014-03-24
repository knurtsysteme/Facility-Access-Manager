// © 2014 by IT-Power GmbH (http://www.it-power.org)
package de.knurt.fam.core.util.booking;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.QueueBooking;
import de.knurt.fam.core.util.mail.UserMailSender;

/**
 * observe bookings dao and send mail on bookings to configured e-mail addresses to inform about new bookings.
 * 
 * @see BookingRule#getExtraMailsOnBooking()
 * @see Booking#hasJustBeenInserted()
 * 
 * @author daniel.oltmanns@it-power.org
 * @since 03/24/2014
 */
public class SendExtraMailObserver implements Observer {

  private String from = "noreply@facility-access-manager.com";

  public SendExtraMailObserver(String from) {
    this.from = from;
  }

  public void setObservable(Observable observable) {
    observable.addObserver(this);
  }

  private Email getEMail(String to, Booking booking) {
    // email message
    MultiPartEmail email = new MultiPartEmail();
    try {
      // basics
      email.addTo(to);
      email.setFrom(this.from);
      email.setSubject("A new booking for \"" + booking.getFacility().getLabel() + "\""); // INTLANG
      String message = booking.getUsername() + " booked it on " + booking.getSeton(); // INTLANG
      if (booking.isTimeBased()) {
        message += " for " + booking.getSessionTimeFrame(); // INTLANG
      } else { // queue besed
        message += " at the current queue position " + ((QueueBooking) booking).getCurrentQueuePosition(); // INTLANG
      }
      email.setMsg(message);
    } catch (EmailException e) {
      FamLog.exception(e, 201403240938l);
      email = null;
    }
    return email;
  }

  @Override
  public void update(Observable arg0, Object booking) {
    if (this.isBooking(booking)) {
      Booking b = (Booking) booking;
      if (b.hasJustBeenInserted()) {
        this.insernUpdate(b);
      }
    }

  }

  private void insernUpdate(Booking b) {
    String[] extraEmailsOnBooking = b.getFacility().getBookingRule().getExtraMailsOnBooking();
    for (String extraEmailOnBooking : extraEmailsOnBooking) {
      Email mail = this.getEMail(extraEmailOnBooking, b);
      UserMailSender.sendWithoutUserBox(mail);
    }

  }

  private boolean isBooking(Object booking) {
    Booking b = null;
    try {
      b = (Booking) booking;
    } catch (Exception e) {
    }
    return b != null;
  }

}