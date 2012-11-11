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
package de.knurt.fam.test.unit.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.knurt.fam.core.view.text.FamText;

public class FamTextTest {

	@Test
	public void getTimeInput() {
		assertEquals("1 hour", FamText.getTimeInput(60));
		assertEquals("2 hours", FamText.getTimeInput(120));
		assertEquals("1 hour and 58 minutes", FamText.getTimeInput(60 + 58));
		assertEquals("3 days, 2 hours and 58 minutes", FamText.getTimeInput(3*24*60 + 2*60 + 58));
		assertEquals("3 days and 2 hours", FamText.getTimeInput(3*24*60 + 2*60));
	}
	
	@Test
	public void getExtendesValueOrNull() {
		assertEquals("Please drive 2 m", FamText.valueOrAlt("2", "error", null, "Please drive ", " m"));
	}
	@Test
	public void getExtendesValueOrNull2() {
		assertEquals("foo", FamText.valueOrAlt("foo", "error", null, null, null));
	}
	@Test
	public void getExtendesValueOrNullWithNoValue() {
		List<String> noValues = new ArrayList<String>();
		noValues.add("-");
		assertEquals("error", FamText.valueOrAlt("-", "error", noValues, "Please drive ", "m"));
	}

}
