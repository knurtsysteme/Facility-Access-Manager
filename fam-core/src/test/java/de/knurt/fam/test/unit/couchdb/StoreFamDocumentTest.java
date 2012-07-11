package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.persist.document.FamDocument;
import de.knurt.fam.core.model.persist.document.FamDocumentType;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.model.persist.document.SoaActivationPageDocument;
import de.knurt.fam.core.model.persist.document.SoaDocument;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class StoreFamDocumentTest {
	@Test
	public void constructAndStore_fails() {
		FamDocument document = new SoaDocument();
		assertEquals(FamDocumentType.SOA, document.getType());
		assertNull(document.getId());
		assertNull(document.getRevision());
		assertNull(document.getCreated());
		assertFalse(document.insertOrUpdate());
	}

	@Test
	public void constructAndStore_succ() {
		SoaDocument document = new SoaDocument();
		assertEquals(FamDocumentType.SOA, document.getType());
		assertNull(document.getId());
		assertNull(document.getRevision());
		assertNull(document.getCreated());
		document.setTitle("title");
		document.setContent("content");
		document.insertOrUpdate();
		assertNotNull(document.getId());
		assertNotNull(document.getRevision());
		assertNotNull(document.getCreated());
	}

	@Test
	public void couchDbDaoInsertAndGet() {
		SoaDocument document = new SoaDocument();
		document.setTitle("title");
		document.setContent("content");
		long docSizesBefore = FamCouchDBDao.getInstance().documentCount();
		FamCouchDBDao.getInstance().createDocument(document);
		assertEquals(docSizesBefore + 1, FamCouchDBDao.getInstance().documentCount());
	}

	@Test
	public void storeASoaActivationFails_missesRoleId() {
		SoaActivationDocument sad = new SoaActivationDocument();
		sad.setActivatedOn(new Date().getTime() - 100);
		SoaActivationPageDocument sapd = new SoaActivationPageDocument();
		SoaDocument soc = new SoaDocument();
		soc.setCreated(new Date().getTime());
		soc.setTitle("title");
		soc.setContent("content");
		sapd.setSoaDoc(soc);
		sad.addPage(sapd);
		long docSizesBefore = FamCouchDBDao.getInstance().documentCount();

		boolean insertSucc = sad.insertOrUpdate();

		assertFalse(insertSucc);
		assertEquals(docSizesBefore, FamCouchDBDao.getInstance().documentCount());

	}

	@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	@Test
	public void storeASoaActivation() {
		SoaActivationDocument sad = new SoaActivationDocument();
		sad.setActivatedOn(new Date().getTime() - 100);
		sad.setRoleId("extern");
		SoaActivationPageDocument sapd = new SoaActivationPageDocument();
		SoaDocument soc = new SoaDocument();
		soc.setCreated(new Date().getTime());
		soc.setTitle("title");
		soc.setContent("content");
		sapd.setSoaDoc(soc);
		sad.addPage(sapd);

		long docSizesBefore = FamCouchDBDao.getInstance().documentCount();
		assertTrue(sad.insertOrUpdate());
		assertNotNull(sad.getId());
		List<SoaActivationDocument> docs = FamDaoProxy.soaDao().getAllSoaActivation();
		boolean sadIsWithIt = false;
		for (SoaActivationDocument got : docs) {
			if (got.getId().equals(sad.getId())) {
				sadIsWithIt = true;
				break;
			}
		}
		assertTrue(sadIsWithIt);
		assertEquals(docSizesBefore + 1, FamCouchDBDao.getInstance().documentCount());
	}

	@Test
	public void bug_classCastException() {
		SoaActivationDocument sad = new SoaActivationDocument();
		sad.setActivatedOn(new Date().getTime() - 100);
		sad.setRoleId("extern");
		SoaActivationPageDocument sapd = new SoaActivationPageDocument();
		SoaDocument soc = new SoaDocument();
		soc.setCreated(new Date().getTime());
		soc.setTitle("title");
		soc.setContent("content");
		sapd.setSoaDoc(soc);
		sad.addPage(sapd);

		// get back sad
		assertNull(sad.getId());
		sad.insertOrUpdate();
		assertNotNull(sad.getId());
		SoaActivationDocument got = FamCouchDBDao.getInstance().getOne(sad.getId(), SoaActivationDocument.class);
		assertEquals(ArrayList.class, got.getSoaActivePages().getClass());
		assertEquals(1, got.getSoaActivePages().size());
	}
}
