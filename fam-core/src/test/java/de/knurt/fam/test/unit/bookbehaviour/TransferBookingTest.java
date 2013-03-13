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
package de.knurt.fam.test.unit.bookbehaviour;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * test to transfer a booking to another user.
 * 
 * @author Daniel Oltmanns <daniel.oltmanns@it-power.org>
 * @since 03/08/2013
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class TransferBookingTest extends FamIBatisTezt {

  private void transferBooking(Booking booking) {
    // insert job survey to booking
    Job document = new Job();
    document.setJobId(booking.getId());
    document.setUsername("foo");
    document.setStep(0);
    document.setIdJobDataProcessing("foo");
    document.setJobSurvey(new JSONObject());
    assertTrue(document.insertOrUpdate());
    Job job = CouchDBDao4Jobs.me().getJob(booking.getId(), 0);

    booking = FamDaoProxy.bookingDao().getAll().get(0);
    User receiver = TeztBeanSimpleFactory.getNewUniqueValidUser("1303081351");

    // THE TRANSFER TO TEST
    assertTrue(booking.transferTo(receiver));

    job = CouchDBDao4Jobs.me().getJob(booking.getId(), 0);

    // booking has username of receiver
    assertEquals(receiver.getUsername(), booking.getUsername());
    // job has username of receiver
    assertEquals(receiver.getUsername(), job.getUsername());
    // booking in database
    List<Booking> bs = receiver.getBookings();
    assertEquals(1, bs.size());
  }

  @Test
  public void transferTimeBooking() {
    this.clearDatabase();
    Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
    TeztBeanSimpleFactory.getAdmin().insert();
    booking.setUsername(TeztBeanSimpleFactory.getAdmin().getUsername());
    booking.setBooked();
    booking.insert();
    this.transferBooking(booking);
  }

  @Test
  public void canceledBookingNotTranferable() {
    this.clearDatabase();
    Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
    String oldUsername = booking.getUsername();
    booking.cancel();
    User receiver = TeztBeanSimpleFactory.getNewUniqueValidUser("1303081351");
    assertFalse(booking.transferTo(receiver));
    assertEquals(booking.getUsername(), oldUsername);
  }

  @Test
  public void bookingNotTranferableToInvalidUser() {
    this.clearDatabase();
    Booking booking = TeztBeanSimpleFactory.getNewValidBooking();
    booking.cancel();
    assertFalse(booking.transferTo(new User()));
  }

  @Test
  public void transferQueueBooking() {
    this.clearDatabase();
    TeztBeanSimpleFactory.getAdmin().insert();
    Booking booking = TeztBeanSimpleFactory.getNewValidQueueBooking(TeztBeanSimpleFactory.getAdmin(), TeztBeanSimpleFactory.getBookableQueueFacility());
    booking.setBooked();
    booking.insert();
    this.transferBooking(booking);
  }

}
