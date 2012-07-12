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
package de.knurt.fam.test.unit.db.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.ibatis.BookingAdapterParameter;
import de.knurt.fam.core.control.persistence.dao.ibatis.FamSqlMapClientDaoSupport;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.booking.CurrentFacilityStatus;
import de.knurt.fam.test.utils.AssertSomehowEquals;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;

/**
 *
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class FacilityStatusTest extends FamIBatisTezt {

    /**
     *
     */
    @Test
    public void everyFacilityHasAStatus() {
        Facility facility = TeztBeanSimpleFactory.getFacility1();
        CurrentFacilityStatus ds = facility.getFacilityStatus();
        assertNotNull(ds);
        assertEquals(CurrentFacilityStatus.class, ds.getClass());
    }

    /**
     *
     */
    @Test
    public void everyFacilityStatusHasAFacilityAvailability() {
        this.clearDatabase();
        Facility facility = TeztBeanSimpleFactory.getFacility1();
        CurrentFacilityStatus ds = facility.getFacilityStatus();
        FacilityAvailability da = ds.getFacilityAvailability();
        assertNotNull(da);
        assertEquals(FacilityAvailability.class, da.getClass());
        assertTrue(da.isCompletelyAvailable());
    }

    /**
     *
     */
    @Test
    public void facilityAvailabilityBooked() {
        this.clearDatabase();
        TimeBooking b = TeztBeanSimpleFactory.getNewValidBooking();
        b.setStartEnd(SimpleTimeFrame.getToday());
        b.setBooked();

        // brutal store - its not available because of starting in past
        BookingAdapterParameter adapter = new BookingAdapterParameter(b);
        FamSqlMapClientDaoSupport.sqlMap().insert("Booking.insert", adapter);

        // selftest
        assertEquals(1, FamDaoProxy.bookingDao().getUncanceledBookingsAndApplicationsIn(b.getFacility(), SimpleTimeFrame.getToday()).size());
        assertEquals(1, FamDaoProxy.bookingDao().getAllUncanceledBookingsAndApplicationsOfToday(b.getFacility()).size());

        Facility facility = TeztBeanSimpleFactory.getFacility1();
        CurrentFacilityStatus ds = facility.getFacilityStatus();
        FacilityAvailability da = ds.getFacilityAvailability();
        assertNotNull(da);
        assertEquals(FacilityAvailability.BOOKED_NOT_AVAILABLE, da.getAvailable().intValue());
        assertTrue(da.isNotAvailableBecauseOfBooking());
    }

    /**
     *
     */
    @Test
    public void facilityAvailabilityMaintenance() {
        this.clearDatabase();
        FacilityAvailability daPut = new FacilityAvailability();
        daPut.setBasePeriodOfTime(SimpleTimeFrame.getToday());
        daPut.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
        User testuser = TeztBeanSimpleFactory.getNewValidUser();
        testuser.setUsername("testuser");
        testuser.insert();
        daPut.setUserSetThis(testuser);
        daPut.setAvailable(FacilityAvailability.MAINTENANCE_NOT_AVAILABLE);
        daPut.insert();
        assertEquals(1, FamDaoProxy.facilityDao().getAll().size());

        Facility facility = TeztBeanSimpleFactory.getFacility1();
        CurrentFacilityStatus ds = facility.getFacilityStatus();
        FacilityAvailability daGot = ds.getFacilityAvailability();
        assertNotNull(daGot);
        assertEquals(FacilityAvailability.MAINTENANCE_NOT_AVAILABLE, daGot.getAvailable().intValue());
        assertTrue(daGot.isNotAvailableBecauseOfMaintenance());
        AssertSomehowEquals.test(testuser, daGot.getUserSetThis());
    }

    /**
     *
     */
    @Test
    public void facilityAvailabilityGeneralWithParent() {
        this.clearDatabase();
        FacilityAvailability daPut = new FacilityAvailability();
        daPut.setBasePeriodOfTime(SimpleTimeFrame.getToday());
        daPut.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_PARENT);
        User testuser = TeztBeanSimpleFactory.getNewValidUser();
        testuser.setUsername("testuser");
        testuser.insert();
        daPut.setUserSetThis(testuser);
        daPut.setAvailable(FacilityAvailability.MAINTENANCE_NOT_AVAILABLE);
        daPut.insert();
        assertEquals(1, FamDaoProxy.facilityDao().getAll().size());

        Facility facility = TeztBeanSimpleFactory.getFacility1();
        CurrentFacilityStatus ds = facility.getFacilityStatus();
        FacilityAvailability daGot = ds.getFacilityAvailability();
        assertNotNull(daGot);
        assertEquals(1, FamDaoProxy.facilityDao().getAll().size());
        assertEquals(FacilityAvailability.MAINTENANCE_NOT_AVAILABLE, daGot.getAvailable().intValue());
        assertTrue(daGot.isNotAvailableBecauseOfMaintenance());
        AssertSomehowEquals.test(testuser, daGot.getUserSetThis());
    }
}
