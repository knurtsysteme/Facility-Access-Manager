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
package de.knurt.fam.test.unit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.fam.core.util.booking.TimeBookingRequest;
import de.knurt.fam.core.util.bu.PdfLetterFromBooking;
import de.knurt.fam.template.controller.letter.FamServicePDFResolver;
import de.knurt.fam.template.controller.letter.LetterGeneratorShowLetter;
import de.knurt.fam.test.utils.FamIBatisTezt;
import de.knurt.fam.test.utils.TeztBeanSimpleFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class LetterTest extends FamIBatisTezt {

  public LetterTest() {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Test
  public void terms() {
    this.clearDatabase();
    LetterGeneratorShowLetter genera = new LetterGeneratorShowLetter();
    HttpServletResponse response = new MockHttpServletResponse();
    User userTarget = TeztBeanSimpleFactory.getAdmin();
    try {
      genera.processTerms(response, userTarget);
      assertTrue("no exception here", true);
    } catch (Exception e) {
      fail("should not throw " + e);
    }
  }
  
  private String getIndendedResearch2000CharsLong() {
    String result = "";
    while(result.trim().length() < 2000) result += "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr,  sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo. ";
    return result.trim().substring(0, 1999) + "!";
  }

  @Test
  public void writeTermsToFile() throws URIException {
    LetterGeneratorShowLetter genera = new LetterGeneratorShowLetter();
    User target = UserFactory.getInstance().getJoeBloggs();
    // TODO raus - mpi spezifisch
    target.addCustomField("principal_investigator_title_id_unknown", "Mr.");
    target.addCustomField("principal_investigator_fname_id_unknown", "Peter");
    target.addCustomField("principal_investigator_sname_id_unknown", "Investigator");
    target.addCustomField("taskdesc_id_unknown", "My task is to foo, but we need nearly 80 characters here, more foo to test and we have more then 80 - future save!");
    target.setIntendedResearch(this.getIndendedResearch2000CharsLong());
    target.addCustomField("hasRights", "1");
    target.addCustomField("trademarkrights_id_unknown", "My trademark is named foo, and we do not expect longer names here");
    target.addCustomField("principal_investigator_issecret_id_unknown", "1");
    target.addCustomField("partner_id_unknown", "Der Partner stellt zur Verfügung: Foo und Bar");
    String customid = target.getUsername() + "-terms";
    PostMethod post = new FamServicePDFResolver().process(genera.getTermsLetterStyle(target, customid));
    assertEquals(post.getStatusCode() + "@" + post.getURI(), post.getStatusCode(), 200);
    String checkFileName = System.getProperty("java.io.tmpdir") + "/test-terms.pdf";
    File checkFile = new File(checkFileName);
    if (checkFile.exists()) checkFile.delete();
    try {
      checkFile.createNewFile();
      FileUtils.writeByteArrayToFile(checkFile, post.getResponseBody());
      assertTrue(checkFile.length() > 1042);
      System.out.println("find the result in " + checkFile);
    } catch (Exception e) {
      fail("should not throw " + e + ". file closed on idiot windows?");
    }

  }

  @Test
  public void getLetterForFacility() {
    this.clearDatabase();
    // create TimeBooking
    TimeBookingRequest br = TeztBeanSimpleFactory.getBookingRequest();
    TimeBooking booking = TeztBeanSimpleFactory.getNewValidBooking(br);
    booking.setSeton(new Date());
    booking.setBooked();
    booking.insert();
    JSONObject letter = new PdfLetterFromBooking(TeztBeanSimpleFactory.getAdmin()).process(booking);
    assertNotNull(letter);
    assertTrue(letter.length() > 0);
  }
}
