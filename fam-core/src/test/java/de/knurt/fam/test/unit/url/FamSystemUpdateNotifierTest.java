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
package de.knurt.fam.test.unit.url;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.connector.FamSystemUpdateNotifier;

/**
 * test if the update notifier works correctly.
 * this can only succeed if you are online.
 */
public class FamSystemUpdateNotifierTest {

	/**
	 * if this test fails, check the update uri defined in
	 * {@link FamSystemUpdateNotifier}
	 */
	@Test
	public void methodsExists() {
		assertTrue(FamConnector.getGlobalPropertyAsBoolean("system.check_update"));
		Properties av = FamSystemUpdateNotifier.actualVersion();
		assertNotNull(av);
		assertEquals(av.get("update_active"), true);
		assertNotNull(av.get("is_actual"));
		assertNotNull(av.get("update_url"));
		assertEquals(av.get("error"), false);
		assertNotNull(av.get("actual_version"));
		assertNotNull(av.get("download_page"));
		assertEquals(av.get("is_actual").getClass(), Boolean.class);
		assertEquals(av.get("actual_version").getClass(), String.class);
		assertTrue(av.get("download_page").toString(), av.get("download_page").toString().startsWith("http://facility-access-manager.com"));
		assertTrue(av.get("update_url").toString(), av.get("update_url").toString().startsWith("http://facility-access-manager.com"));
	}
}