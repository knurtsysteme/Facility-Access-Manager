package de.knurt.fam.test.unit.db.ibatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

/**
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class ContactDetailTest extends FamIBatisTezt {

	@Test
	public void insertContactDetail() {
		this.clearDatabase();
		User user = TeztBeanSimpleFactory.getNewValidUser();
		user.insert();
		assertNotNull(user.getId());
		List<ContactDetail> contactDetails = user.getContactDetails();
		assertEquals(0, contactDetails.size());

		// add contact detail to the user
		ContactDetail cd = new ContactDetail();
		cd.setUsername(user.getUsername());
		cd.setTitle("title");
		cd.setDetail("detail");
		cd.insert();
		assertNotNull(cd.getId());
		contactDetails = user.getContactDetails();
		assertEquals(1, contactDetails.size());

		ContactDetail contactDetail = contactDetails.get(0);
		assertEquals("title", contactDetail.getTitle());
		assertEquals("detail", contactDetail.getDetail());
		assertEquals(user.getUsername(), contactDetail.getUsername());
	}
}
