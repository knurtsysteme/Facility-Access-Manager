/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.knurt.fam.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.service.pdf.control.FileFactoryUtil;
import de.knurt.fam.service.pdf.control.bu.HttpServletRequest2File;
import de.knurt.fam.service.pdf.control.bu.PDFOption2File;
import de.knurt.fam.service.pdf.model.PDFOption;
import de.knurt.fam.service.pdf.model.TextContent;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/config.xml" })
public class FamPdfFileTest {

	public FamPdfFileTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void outputDirectory() {
		File outputDirectory = FileFactoryUtil.outputDirectory();
		assertTrue(outputDirectory.exists());
		assertTrue(outputDirectory.canRead());
		assertTrue(outputDirectory.canWrite());
		assertTrue(outputDirectory.isDirectory());
	}

	/**
	 * filename must be [timestamp]-[serial number]-[id].pdf e.g.:
	 * 20110530111502-3-daoltman.pdf (for the 3rd document generated on
	 * 05/30/2011 11:15 0045ms)
	 */
	@Test
	public void filename() {
		String filename = FileFactoryUtil.filename("foo");
		assertNotNull(filename);
		assertTrue(filename.matches(FileFactoryUtil.outputDirectory() + File.separator + "[0-9]{14}-[0-9]+-foo\\.pdf"));

		// is unique
		int filesBefore = FileFactoryUtil.outputDirectory().listFiles().length;
		int i = 0;
		while (i++ < 20) {
			try {
				assertTrue(new File(FileFactoryUtil.filename("foo")).createNewFile());
			} catch (IOException e) {
				assertTrue(false);
			}
		}
		assertEquals(filesBefore + 20, FileFactoryUtil.outputDirectory().listFiles().length);
	}

	@Test
	public void request2file() {
		MockHttpServletRequest mockrq = new MockHttpServletRequest();
		// // EasyMock.expect(mockrq.getAttribute("bla")).andReturn("blub");
		File f = new HttpServletRequest2File().process(mockrq);
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.canRead());
		assertTrue(f.canWrite());
	}

	@Test
	public void pdfoption2file_noContent() {
		PDFOption options = new PDFOption();
		File f = new PDFOption2File().process(options);
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.canRead());
		assertTrue(f.canWrite());
	}

	@Test
	public void pdfoption2file_withParagraph() {
		PDFOption options = new PDFOption();
		TextContent p = new TextContent();
		options.add(p);
		File f = new PDFOption2File().process(options);
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.canRead());
		assertTrue(f.canWrite());
	}

	@Test
	public void pdfoption2file_withStyledParagraph() {
		PDFOption options = new PDFOption();

		TextContent p = new TextContent();
		p.setText(this.getTestText());
		p.putStyle("font-family", "Courier");
		p.putStyle("font-size", 104);
		p.putStyle("text-align", "left");
		p.putStyle("left", 100);
		p.putStyle("bottom", 800);
		p.putStyle("color", "#990000");
		p.putStyle("font-style", "italic");
		p.putStyle("font-weight", "bold");
		p.putStyle("text-decoration", "line-through");

		options.add(p);
		File f = new PDFOption2File().process(options);
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.canRead());
		assertTrue(f.canWrite());
	}

	/**
	 * assert size 595.0x842.0
	 */
	@Test
	public void pdfoption2file_asNeeded4MPI() {
		PDFOption options = new PDFOption();
		options.setCustomId("example");

		int left = 56;
		int textWidth = 375;
		int left2 = 440;
		int fontsize = 10;

		TextContent recipient = new TextContent();
		recipient.setText("To\nLorem ipsum\nconsetetur sadipscing");
		recipient.putStyle("font-family", "Helvetica");
		recipient.putStyle("font-size", fontsize);
		recipient.putStyle("line-height", 13);
		recipient.putStyle("left", left);
		recipient.putStyle("bottom", 615);
		recipient.putStyle("width", 220);
		recipient.putStyle("height", 70);
		options.add(recipient);

		TextContent telephone = new TextContent();
		telephone.setText("Tel.: 555 2 05 43 54");
		telephone.putStyle("font-family", "Helvetica");
		telephone.putStyle("font-size", fontsize);
		telephone.putStyle("line-height", 13);
		telephone.putStyle("left", left2);
		telephone.putStyle("bottom", 615);
		telephone.putStyle("width", 220);
		telephone.putStyle("height", 70);
		options.add(telephone);

		TextContent headline = new TextContent();
		headline.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt");
		headline.putStyle("font-family", "Helvetica");
		headline.putStyle("font-size", fontsize);
		headline.putStyle("font-weight", "bold");
		headline.putStyle("font-style", "italic");
		headline.putStyle("left", left);
		headline.putStyle("bottom", 600);
		headline.putStyle("width", textWidth);
		headline.putStyle("height", 25);
		options.add(headline);

		TextContent date = new TextContent();
		date.setText("07.06.2011");
		date.putStyle("font-family", "Helvetica");
		date.putStyle("font-size", fontsize);
		date.putStyle("left", left2);
		date.putStyle("bottom", 600);
		date.putStyle("width", 90);
		date.putStyle("height", 25);
		options.add(date);

		TextContent billaddress = new TextContent();
		billaddress.setText("Lorem ipsum dolor\nconsetetur sadipscing elitr\nnonumy eirmod");
		billaddress.putStyle("font-family", "Helvetica");
		billaddress.putStyle("font-size", fontsize);
		billaddress.putStyle("line-height", 16);
		billaddress.putStyle("left", left);
		billaddress.putStyle("bottom", 470);
		billaddress.putStyle("width", textWidth);
		billaddress.putStyle("height", 100);
		options.add(billaddress);

		TextContent maintext = new TextContent();
		maintext
				.setText("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore:\n\n"
						+ "et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores\n"
						+ "et ea rebum. Stet clita kasd gubergren\n"
						+ "et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");
		maintext.putStyle("font-family", "Helvetica");
		maintext.putStyle("font-size", fontsize);
		maintext.putStyle("line-height", 16);
		maintext.putStyle("left", left);
		maintext.putStyle("bottom", 55);
		maintext.putStyle("width", textWidth);
		maintext.putStyle("height", 425);
		options.add(maintext);

		TextContent city = new TextContent();
		city.setText("Lorem, 11.11.2011");
		city.putStyle("font-family", "Helvetica");
		city.putStyle("font-size", fontsize);
		city.putStyle("left", left);
		city.putStyle("bottom", 170);
		city.putStyle("width", textWidth);
		city.putStyle("height", 25);
		options.add(city);

		TextContent signer = new TextContent();
		signer.setText("Lorem ipsum");
		signer.putStyle("font-family", "Helvetica");
		signer.putStyle("font-size", fontsize);
		signer.putStyle("left", left);
		signer.putStyle("bottom", 105);
		signer.putStyle("width", textWidth);
		signer.putStyle("height", 25);
		options.add(signer);

		File f = new PDFOption2File().process(options);
		assertNotNull(f);
	}

	private String getTestText() {
		String result = "";
		for (int i = 0; i < 20; i++) {
			result += "This is a test text for a long paragraph. ";
		}
		return result;
	}
}
