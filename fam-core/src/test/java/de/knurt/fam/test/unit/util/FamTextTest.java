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

}
