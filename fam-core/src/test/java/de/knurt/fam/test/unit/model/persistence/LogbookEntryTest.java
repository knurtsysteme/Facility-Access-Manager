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
package de.knurt.fam.test.unit.model.persistence;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import de.knurt.fam.core.model.persist.LogbookEntry;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
public class LogbookEntryTest {

	/**
     *
     */
	@Test
	public void testConstructionAndSetting() {
		LogbookEntry le = new LogbookEntry();

		String testString = "foo";
		Locale testLocale = Locale.getDefault();
		Date testdate = new Date();
		ArrayList<String> testtags = new ArrayList<String>();
		testtags.add("a");
		testtags.add("b");
		testtags.add("c");

		le.setLogbookId(testString);
		le.setLanguage(testLocale);
		le.setOfUserName(testString);
		le.setContent(testString);
		le.setHeadline(testString);
		le.setDate(testdate);
		le.setTags(testtags);

		assertEquals(testString, le.getLogbookId());
		assertEquals(testString, le.getOfUserName());
		assertEquals(testLocale, le.getLanguage());
		assertEquals(testString, le.getContent());
		assertEquals(testString, le.getHeadline());
		assertEquals(testdate, le.getDate());
		assertEquals(testtags, le.getTags());

	}
}