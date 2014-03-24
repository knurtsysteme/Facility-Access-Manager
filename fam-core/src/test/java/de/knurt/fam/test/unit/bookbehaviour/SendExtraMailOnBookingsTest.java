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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.mail.UserMailSender;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class SendExtraMailOnBookingsTest extends FamIBatisTezt {

  @Test
  public void sendExtraMailOnBookingSchoolbusses() {
    this.clearDatabase();
    // assert booking rules of the default test-facility bookable (bus1) has 2 extra emails configured
    int assertExtraEmails = 2;
    Booking b = TeztBeanSimpleFactory.getNewValidBooking();
    assertEquals("selftest", "bus1", b.getFacilityKey());
    assertNotNull("selftest", b.getBookingRule().getExtraMailsOnBooking());
    assertEquals("selftest", assertExtraEmails, b.getBookingRule().getExtraMailsOnBooking().length);
    // book it and expect, that both got an email
    b.setBooked();
    int countMailsSendBefore = UserMailSender.SEND_WITHOUT_METER;
    assertTrue(b.insert());
    int countMailsSendAfter = UserMailSender.SEND_WITHOUT_METER;
    assertEquals(countMailsSendBefore + assertExtraEmails, countMailsSendAfter);
  }
}
