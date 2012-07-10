package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.couchdb.CouchDBDao4Jobs;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;
import de.knurt.heinzelmann.util.nebc.bu.File2ByteArray;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class StoreJobSurveyTest extends FamIBatisTezt {

	String id = FamDateFormat.getLangIndependantShortDate();

	@Test
	public synchronized void storeAndGetAllJobs() {
		int i = 20;
		while (i-- > 0) {
			this.clearDatabase();

			TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
			booking.setBooked();
			booking.insert();

			Job document = new Job();
			document.setJobId(booking.getId());
			document.setUsername("foo");
			document.setStep(0);
			document.setIdJobDataProcessing("foo");
			document.setJobSurvey(new JSONObject());
			long docSizesBefore = FamCouchDBDao.getInstance().documentCount();
			assertTrue(FamCouchDBDao.getInstance().createDocument(document));
			assertEquals(docSizesBefore + 1, FamCouchDBDao.getInstance().documentCount());
		}
	}

	@Test
	public void storeAndGetJobAttachment() throws IOException {
		this.clearDatabase();
		User admin = TeztBeanSimpleFactory.getAdmin();

		File testFileOriginal = new File(System.getProperty("user.dir") + File.separator + ".." + File.separator + "fam-web" + File.separator + "src" + File.separator + "main" + File.separator + "webapp" + File.separator + "demo" + File.separator + "invoice_demo_nursery_school.pdf");
		assertTrue(testFileOriginal.canRead());

		File directory = new File(FamConnector.fileExchangeDir() + File.separator + "users" + File.separator + admin.getUsername());
		if (!directory.exists()) {
			directory.mkdir();
			directory.setWritable(true);
		}
		assertTrue(directory.canWrite());

		File testFile = new File(directory.getAbsolutePath() + File.separator + "test.pdf");
		if (testFile.exists() == false || testFile.canRead() == false || testFile.length() != testFileOriginal.length()) {
			InputStream in = new FileInputStream(testFileOriginal);
			OutputStream out = new FileOutputStream(testFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}

		assertEquals(testFileOriginal.length(), testFile.length());

		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		booking.insert();

		Job document = new Job();
		document.setJobId(booking.getId());
		document.setUsername(admin.getUsername());
		document.setStep(0);
		document.setIdJobDataProcessing("foo");
		document.setJobSurvey(new JSONObject());

		List<File> attachments = new ArrayList<File>(1);
		attachments.add(testFile);
		document.addAttachments(attachments);
		assertTrue(document.insertOrUpdate());

		// get it back
		byte[] got_back = FamCouchDBDao.getInstance().getAttachment(document.getId(), testFile.getName());
		assertEquals(got_back.length, testFile.length());
		assertEquals(got_back.length, testFileOriginal.length());

		Byte[] testBs = new File2ByteArray().process(testFile);
		Byte[] testBs2 = new File2ByteArray().process(testFileOriginal);
		for (int i = 0; i < testBs.length; i++) {
			assertEquals(testBs[i].byteValue(), got_back[i]);
			assertEquals(testBs2[i].byteValue(), got_back[i]);
		}
	}

	@Test
	public synchronized void storeAndUpdateJob() {
		this.clearDatabase();

		long docSizesBefore = FamCouchDBDao.getInstance().documentCount();

		int step = 1;

		TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
		booking.setBooked();
		booking.insert();

		Job document = new Job();
		document.setJobId(booking.getId());
		document.setUsername("foo");
		document.setStep(step);
		document.setIdJobDataProcessing("foo");
		document.setJobSurvey(new JSONObject());
		assertTrue(FamCouchDBDao.getInstance().createDocument(document));

		Job back = CouchDBDao4Jobs.me().getJob(booking.getId(), step);
		assertNotNull(back);
		long createdBefore = back.getCreated();
		assertEquals(back.getIdJobDataProcessing(), "foo");
		assertEquals(docSizesBefore + 1, FamCouchDBDao.getInstance().documentCount());

		document.setIdJobDataProcessing("bar");
		document.insertOrUpdate();
		back = CouchDBDao4Jobs.me().getJob(booking.getId(), step);
		assertNotNull(back);
		assertTrue(createdBefore != back.getCreated());
		assertEquals(back.getIdJobDataProcessing(), "bar");
		assertEquals(docSizesBefore + 1, FamCouchDBDao.getInstance().documentCount());
	}

	@Test
	public void storeAndGetJobSimpleJobSurvey() {
		this.clearDatabase();
		try {
			int step = 0;

			TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
			booking.setBooked();
			booking.insert();

			Job job = new Job();
			job.setUsername(TeztBeanSimpleFactory.getAdmin().getUsername());
			job.setStep(step);
			job.setJobSurvey(new JSONObject());
			job.setJobId(booking.getId());
			JobDataProcessing jdp = TeztBeanSimpleFactory.getNewValidJobDataProcessing();
			jdp.insertOrUpdate();
			job.setIdJobDataProcessing(jdp.getId());
			assertTrue(job.insertOrUpdate());

			Job back = CouchDBDao4Jobs.me().getJob(booking.getId(), step);
			assertNotNull(back);
		} catch (Exception e) {
			fail("should not throw exception " + e);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void storeAndGetJob() {
		this.clearDatabase();
		try {
			int step = 0;
			Job job = new Job();
			job.setStep(step);
			JSONObject jobsurvey = new JSONObject();

			JSONObject jobsurvey2 = new JSONObject();
			jobsurvey2.put("input 2", "value 2");
			jobsurvey2.put("input 3", "value 3");

			JSONObject jobsurvey3 = new JSONObject();
			jobsurvey3.put("input 4", "value 4");
			jobsurvey3.put("input 5", "value 5");

			JSONArray innerarray = new JSONArray();
			innerarray.put("hallo 1");
			innerarray.put("hallo 2");

			JSONArray innerarray2 = new JSONArray();
			innerarray2.put("hallo 3");
			innerarray2.put("hallo 4");

			innerarray.put(innerarray2);
			innerarray.put(jobsurvey3);

			jobsurvey.put("input 0", "value 0");
			jobsurvey.put("input 1", "value 1");
			jobsurvey.put("innerobj", jobsurvey2);
			jobsurvey.put("innerarray", innerarray);

			job.setJobSurvey(jobsurvey);

			assertEquals(job.getJobSurvey().get("input 0").toString(), "value 0");
			assertEquals(job.getJobSurvey().get("input 1").toString(), "value 1");
			Map inner = (Map) job.getJobSurvey().get("innerobj");
			assertEquals(inner.get("input 2"), "value 2");

			job.setUsername(TeztBeanSimpleFactory.getAdmin().getUsername());
			TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking();
			booking.setBooked();
			booking.insert();
			job.setJobId(booking.getId());
			JobDataProcessing jdp = TeztBeanSimpleFactory.getNewValidJobDataProcessing();
			jdp.insertOrUpdate();
			job.setIdJobDataProcessing(jdp.getId());
			job.insertOrUpdate();

			Job back = CouchDBDao4Jobs.me().getJob(booking.getId(), step);
			assertNotNull(back);
			assertNotNull(back.getJobSurvey());
			assertEquals(back.getJobSurvey().get("input 0").toString(), "value 0");
			assertEquals(back.getJobSurvey().get("input 1").toString(), "value 1");
			Map innerback = (Map) back.getJobSurvey().get("innerobj");
			assertEquals(innerback.get("input 2"), "value 2");
		} catch (JSONException e) {
			fail("should not throw exception " + e);
		} catch (Exception e) {
			fail("should not throw exception " + e);
		}
	}
}
