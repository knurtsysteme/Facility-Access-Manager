package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.model.persist.document.SoaActivationPageDocument;
import de.knurt.fam.core.model.persist.document.SoaDocument;
import de.knurt.fam.template.util.TermsOfUseResolver;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class DeactivateAgreementsTest {
	@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	@Test
	public void insertNewForExtern() {
		SoaActivationDocument externOld = this.insertAndGet("extern");
		SoaActivationDocument internOld = this.insertAndGet("intern");
		SoaActivationDocument externNew = this.insertAndGet("extern");

		assertNull(externOld.getDeactivatedOn());
		assertNull(internOld.getDeactivatedOn());
		assertNull(externNew.getDeactivatedOn());

		assertTrue(externOld.isActive());
		assertTrue(internOld.isActive());
		assertTrue(externNew.isActive());

		assertNotNull(externOld.getId());
		assertNotNull(internOld.getId());
		assertNotNull(externNew.getId());

		new TermsOfUseResolver(TeztBeanSimpleFactory.getAdmin()).deactivateAgreementsFor(externNew);

		List<SoaActivationDocument> all_sads = FamDaoProxy.soaDao().getAllSoaActivation();
		assertTrue(all_sads.size() >= 3);
		boolean[] foundAll = { false, false, false };
		for (SoaActivationDocument sad : all_sads) {
			if (sad.getId().equals(internOld.getId())) {
				// intern is still active
				foundAll[0] = true;
				assertTrue(sad.isActive());
			}
			if (sad.getId().equals(externOld.getId())) {
				// extern is not active anymore
				foundAll[1] = true;
				assertFalse(sad.isActive());
				assertTrue(sad.getDeactivatedOn() > sad.getActivatedOn());
			}
			if (sad.getId().equals(externNew.getId())) {
				// new one is active
				foundAll[2] = true;
				assertTrue(sad.isActive());
			}
		}
		// found everything
		assertTrue(foundAll[0]);
		assertTrue(foundAll[1]);
		assertTrue(foundAll[2]);
	}

	@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	@Test
	public void insertNewForIntern() {
		SoaActivationDocument internOld = this.insertAndGet("intern");
		SoaActivationDocument externOld = this.insertAndGet("extern");
		SoaActivationDocument internNew = this.insertAndGet("intern");

		new TermsOfUseResolver(TeztBeanSimpleFactory.getAdmin()).deactivateAgreementsFor(internNew);

		List<SoaActivationDocument> all_sads = FamDaoProxy.soaDao().getAllSoaActivation();
		assertTrue(all_sads.size() >= 3);
		boolean[] foundAll = { false, false, false };
		for (SoaActivationDocument sad : all_sads) {
			if (sad.getId().equals(internOld.getId())) {
				// intern is still active
				foundAll[0] = true;
				assertFalse(sad.isActive());
				assertTrue(sad.getDeactivatedOn() > sad.getActivatedOn());
			}
			if (sad.getId().equals(externOld.getId())) {
				// extern is not active anymore
				foundAll[1] = true;
				assertTrue(sad.isActive());
			}
			if (sad.getId().equals(internNew.getId())) {
				// new one is active
				foundAll[2] = true;
				assertTrue(sad.isActive());
			}
		}
		// found everything
		assertTrue(foundAll[0]);
		assertTrue(foundAll[1]);
		assertTrue(foundAll[2]);
	}

	@SuppressWarnings("deprecation") // TODO #11 kill uses of deprecations
	@Test
	public void checkFields() {
		SoaActivationDocument internOld = this.insertAndGet("intern");
		SoaActivationDocument externOld = this.insertAndGet("extern");
		SoaActivationDocument internNew = this.insertAndGet("intern");

		new TermsOfUseResolver(TeztBeanSimpleFactory.getAdmin()).deactivateAgreementsFor(internNew);

		List<SoaActivationDocument> all_sads = FamDaoProxy.soaDao().getAllSoaActivation();
		assertTrue(all_sads.size() >= 3);
		boolean[] foundAll = { false, false, false };
		for (SoaActivationDocument sad : all_sads) {
			if (sad.getId().equals(internOld.getId())) {
				// intern is still active
				foundAll[0] = true;
				assertEquals(sad.getActivatedOn(), internOld.getActivatedOn());
			}
			if (sad.getId().equals(externOld.getId())) {
				// extern is not active anymore
				foundAll[1] = true;
				assertTrue(sad.isActive());
				assertEquals(sad.getActivatedOn(), externOld.getActivatedOn());
			}
			if (sad.getId().equals(internNew.getId())) {
				// new one is active
				foundAll[2] = true;
				assertTrue(sad.isActive());
				assertEquals(sad.getActivatedOn(), internNew.getActivatedOn());
			}
		}
		// found everything
		assertTrue(foundAll[0]);
		assertTrue(foundAll[1]);
		assertTrue(foundAll[2]);
	}

	private SoaActivationDocument insertAndGet(String roleId) {
		SoaActivationDocument oldSoaActivationDocumentForExtern = new SoaActivationDocument();
		oldSoaActivationDocumentForExtern.setRoleId(roleId);
		oldSoaActivationDocumentForExtern.setActivatedOn(new Date().getTime() - 10000);
		SoaActivationPageDocument sapd = new SoaActivationPageDocument();
		SoaDocument soc = new SoaDocument();
		soc.setTitle("title");
		soc.setContent("content");
		sapd.setSoaDoc(soc);
		oldSoaActivationDocumentForExtern.addPage(sapd);
		oldSoaActivationDocumentForExtern.insertOrUpdate();
		return oldSoaActivationDocumentForExtern;
	}
}
