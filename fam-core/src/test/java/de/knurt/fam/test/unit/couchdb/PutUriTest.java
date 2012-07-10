package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertTrue;

import org.jcouchdb.db.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class PutUriTest {

	@Test
	public void putInvalidValidationError() {
		String body = "{\"bar\" : \"foo\"}";
		Response response = FamCouchDBDao.getInstance().put(body);
		assertTrue(response.getContentAsString().trim().matches("^\\{\"error\":.*"));
	}

	@Test
	public void putValidIn() {
		String body = "{\"content\":\"" + "some content\",\"" + "title\":\"title\",\"created\":1282210173712,\"type\":\"SOA\"}";
		Response response = FamCouchDBDao.getInstance().put(body);
		assertTrue(response.getContentAsString().trim().matches("^\\{\"ok\":true.*"));
	}

	@Test
	public void putInvalidSyntaxError() {
		String body = "\"bar\" : \"foo\"}";
		Response response = FamCouchDBDao.getInstance().put(body);
		assertTrue(response.getContentAsString().trim().matches("^\\{\"error\":.*invalid.*"));
	}
}
