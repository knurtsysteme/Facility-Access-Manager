package de.knurt.fam.test.unit.couchdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.document.SoaActivationDocument;
import de.knurt.fam.core.model.persist.document.SoaActivationPageDocument;
import de.knurt.fam.core.model.persist.document.SoaDocument;
import de.knurt.fam.core.persistence.dao.couchdb.CouchDBDao4Soa;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.termsofuse.TermsOfUsePage;
import de.knurt.fam.template.util.TermsOfUseResolver;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class GetActiveTermsOfUsePagesTest {
	@Test
	public void storeASoaActivation() {
		SoaActivationDocument sad = new SoaActivationDocument();
		sad.setActivatedOn(new Date().getTime() - 100);
		sad.setRoleId("extern");
		int i = 0;
		do {
			SoaActivationPageDocument sapd = new SoaActivationPageDocument();
			SoaDocument soc = new SoaDocument();
			soc.setCreated(new Date().getTime());
			soc.setTitle("title" + i);
			soc.setContent("content" + i);
			sapd.setSoaDoc(soc);
			sad.addPage(sapd);
			i++;
		} while (i < 10);
		assertTrue(sad.insertOrUpdate());
		new TermsOfUseResolver(TeztBeanSimpleFactory.getAdmin()).deactivateAgreementsFor(sad);

		User user = UserFactory.me().blank();
		user.setRoleId("extern");
		List<TermsOfUsePage> touPages = CouchDBDao4Soa.getInstance().getActiveTermsOfUsePages(user);
		assertNotNull(touPages);
		assertEquals(10, touPages.size());

		// check getting the right order
		i = 0;
		for (TermsOfUsePage touPage : touPages) {
			assertEquals("title" + i, touPage.getTitle());
			assertEquals("content" + i, touPage.getHtmlContent());
			assertEquals(i, touPage.getPageno());
			i++;
		}
	}

}
