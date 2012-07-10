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
package de.knurt.fam.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
public class AssertSomehowEquals {

	/**
	 * 
	 * @param b1
	 * @param b2
	 */
	public static void test(Booking b1, Booking b2) {
		assertEquals(b1.getId(), b2.getId());
		assertEquals(b1.getClass(), b2.getClass());
		assertEquals(b1.getBookingStatus().getStatus(), b2.getBookingStatus().getStatus());
		assertEquals(b1.getUsername(), b2.getUsername());
		assertEquals(b1.getCapacityUnits(), b2.getCapacityUnits());
		if (b1.getCancelation() == null) {
			assertNull(b2.getCancelation());
		} else {
			assertEquals(b1.getCancelation().getDateCanceled() + "", b2.getCancelation().getDateCanceled() + "");
			assertEquals(b1.getCancelation().getReason(), b2.getCancelation().getReason());
			assertEquals(b1.getCancelation().getUsername(), b2.getCancelation().getUsername());
		}
		assertEquals(b1.getFacilityKey(), b2.getFacilityKey());
		assertEquals(b1.getSessionTimeFrame() + "", b2.getSessionTimeFrame() + "");
		assertEquals(b1.getSeton() + "", b2.getSeton() + "");
	}

	/**
	 * 
	 * @param a
	 * @param b
	 */
	public static void test(User a, User b) {
		assertEquals(a.getId(), b.getId());
		assertEquals(a.getUsername(), b.getUsername());
		assertEquals(a.getMail(), b.getMail());
		assertEquals(a.getPassword(), b.getPassword());
		assertEquals(a.getAccountExpires(), b.getAccountExpires());
		assertEquals(a.isMale(), b.isMale());
		assertEquals(a.isExcluded(), b.isExcluded());
		if (a.getAddresses() == null || b.getAddresses() == null) {
			assertNull(a.getAddresses());
			assertNull(b.getAddresses());
		} else {
			assertEquals(a.getAddresses().size(), b.getAddresses().size());
		}
		assertEquals(a.getUsedPlattformLangAsString(), b.getUsedPlattformLangAsString());
		assertEquals(a.getPhone1(), b.getPhone1());
		assertEquals(a.getPhone2(), b.getPhone2());
		assertEquals(a.getTitle(), b.getTitle());
		assertEquals(a.getFullName(), b.getFullName());
		assertEquals(a.getFname(), b.getFname());
		assertEquals(a.getSname(), b.getSname());
		assertEquals(a.getIntendedResearch(), b.getIntendedResearch());
		assertEquals(a.getDepartmentKey(), b.getDepartmentKey());
		assertEquals(a.getBirthdate() + "", b.getBirthdate() + "");
		assertEquals(a.getCompany(), b.getCompany());
		assertEquals(a.getLastLogin() + "", b.getLastLogin() + "");
		assertEquals(a.getRegistration() + "", b.getRegistration() + "");
		assertEquals(a.getRoleId(), b.getRoleId());
	}

	/**
	 * 
	 * @param l1
	 * @param l2
	 */
	public static void test(LogbookEntry l1, LogbookEntry l2) {
		assertEquals(l1.getContent(), l2.getContent());
		assertEquals(l1.getDate() + "", l2.getDate() + "");
		assertEquals(l1.getHeadline(), l2.getHeadline());
		assertEquals(l1.getLanguageAsString(), l2.getLanguageAsString());
		assertEquals(l1.getOfUserName(), l2.getOfUserName());
		assertEquals(l1.getLogbookId(), l2.getLogbookId());
		assertEquals(l1.getTagsAsCsv(), l2.getTagsAsCsv());
		assertEquals(l1.getId(), l2.getId());
	}
}
