package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.document.FamDocumentType;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.model.persist.document.SoaActivationPageDocument;
import de.knurt.fam.core.model.persist.document.SoaDocument;

/**
 * all as documents must be compatible with all documents in db. this is tested
 * here.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 16.08.2010
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class AgreementsCompatibilityTest {
	@Test
	public void soaDocument() {
		SoaDocument document = new SoaDocument();
		document.setContent("la");
		assertEquals("la", document.getContent());
		Date date = new Date();
		document.setCreated(date.getTime());
		assertEquals(date.getTime(), document.getCreated().longValue());
	}

	@Test
	public void soaActivationDocument() {
		SoaActivationDocument document = new SoaActivationDocument();
		assertNull(document.getActivatedOn());
		assertNull(document.getDeactivatedOn());
		assertNull(document.getRoleId());
		assertNotNull(document.getSoaActivePages());
		assertEquals(0, document.getSoaActivePages().size());
		assertEquals(FamDocumentType.SOA_ACTIVATION, document.getType());

		SoaActivationPageDocument sapd = new SoaActivationPageDocument();
		assertNull(sapd.getSoaDoc());
		assertFalse(sapd.isForcePrinting());
	}

}
