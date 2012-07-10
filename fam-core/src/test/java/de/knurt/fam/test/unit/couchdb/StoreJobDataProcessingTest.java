package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class StoreJobDataProcessingTest {

	@Test
	public void insertAndGetJobDataProcessingWithTemplates() {

		Map<String, Object> jdpt0 = new HashMap<String, Object>();
		jdpt0.put("behaviour", "b0");
		jdpt0.put("step", 0);
		jdpt0.put("structure", "s0");

		Map<String, Object> jdpt1 = new HashMap<String, Object>();
		jdpt1.put("behaviour", "b1");
		jdpt1.put("step", 1);
		jdpt1.put("structure", "s1");

		List<Map<String, Object>> jdpts = new ArrayList<Map<String, Object>>(2);
		jdpts.add(jdpt0);
		jdpts.add(jdpt1);

		JobDataProcessing document = TeztBeanSimpleFactory.getNewValidJobDataProcessing();
		document.setTemplates(jdpts);
		document.insertOrUpdate();
		JobDataProcessing doc = CouchDBDao4Jobs.me().getActualJobDataProcessing(FacilityConfigDao.facility(document.getFacilityKey()), false);
		assertNotNull(doc.getTemplates());
		assertEquals(2, doc.getTemplates().size());
		assertEquals(doc.getBehaviour(0), "b0");
		assertEquals(doc.getStructure(0, false), "s0");
		assertEquals(doc.getBehaviour(1), "b1");
		assertEquals(doc.getStructure(1, false), "s1");
	}
	@Test
	public void couchDbDaoInsertAndGetJobDataProcessing() {
		JobDataProcessing document = TeztBeanSimpleFactory.getNewValidJobDataProcessing();
		long docSizesBefore = FamCouchDBDao.getInstance().documentCount();
		assertTrue(document.insertOrUpdate());
		assertEquals(docSizesBefore + 1, FamCouchDBDao.getInstance().documentCount());
	}


	@Test
	public void getActual() {
		JobDataProcessing document = TeztBeanSimpleFactory.getNewValidJobDataProcessing();
		document.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE);
		String username = "user_" + new Date().getTime();
		document.setUsername(username);
		document.setCreated(new Date().getTime());
		document.insertOrUpdate();

		// insert a newer one with different facility key
		JobDataProcessing document2 = TeztBeanSimpleFactory.getNewValidJobDataProcessing();
		document2.setFacilityKey(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2);
		String username2 = "user2_" + new Date().getTime();
		document2.setUsername(username2);
		document2.setCreated(new Date().getTime() + 50000l);
		document2.insertOrUpdate();
		// getting the first one above
		JobDataProcessing doc = CouchDBDao4Jobs.me().getActualJobDataProcessing(FacilityConfigDao.facility(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE), false);
		assertEquals(doc.getUsername(), username);
		JobDataProcessing doc2 = CouchDBDao4Jobs.me().getActualJobDataProcessing(FacilityConfigDao.facility(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_2), false);
		assertEquals(doc2.getUsername(), username2);
	}

	@Test
	public void allConnectionsClosed() {
		int i = 0;
		while (i++ < 50) {
			CouchDBDao4Jobs.me().getActualJobDataProcessing(FacilityConfigDao.facility(TeztBeanSimpleFactory.KEY_FACILITY_BOOKABLE_QUEUE), true);
		}
		assertEquals("there must be a timeout if this is not true", i, 51);
	}
}
