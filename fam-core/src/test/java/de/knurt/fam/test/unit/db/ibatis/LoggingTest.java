/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.knurt.fam.test.unit.db.ibatis;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.test.utils.FamIBatisTezt;

/**
 * insert and update is doing in on operation "store" in db4o - so only
 * inserting is not seen as a problem here.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class LoggingTest extends FamIBatisTezt {

	@Test
	public void logInfo() throws FileNotFoundException, IOException {
		try {
			FamLog.logInfo(this.getClass(), "test", 54654654654l);
			assertTrue(true);
		} catch (Exception e) {
			fail("should not have thrown an exception");
		}
	}

	@Test
	public void logException() throws FileNotFoundException, IOException {
		try {
			FamLog.logException(this.getClass(), new Exception("asdf"), "test", 54654654654l);
			assertTrue(true);
		} catch (Exception e) {
			fail("should not have thrown an exception");
		}
	}
}
