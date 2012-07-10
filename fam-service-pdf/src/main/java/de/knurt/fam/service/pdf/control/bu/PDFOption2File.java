package de.knurt.fam.service.pdf.control.bu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import de.knurt.fam.service.pdf.control.FileFactoryUtil;
import de.knurt.fam.service.pdf.control.ebc.BoardUnit;
import de.knurt.fam.service.pdf.model.PDFOption;
import de.knurt.fam.service.pdf.model.TextContent;

/**
 * generate a pdf file from pdf option
 * 
 * @author Daniel Oltmanns
 * @since 1.4.0 (05/31/2011)
 */

public class PDFOption2File implements BoardUnit<PDFOption, File> {

	@Override
	public File process(PDFOption datum) {
		File result = new File(FileFactoryUtil.filename(datum.getCustomId()));
		if (this.createFile(datum, result)) {
			return result;
		} else {
			return FileFactoryUtil.me().getSorryFile();
		}
	}

	private boolean createFile(PDFOption datum, File resultFile) {
		boolean result = false;
		try {
			PdfReader pdfReader = new PdfReader(datum.getTemplateurl());
			PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(resultFile.getAbsoluteFile()));
			for (TextContent po : datum.getTextContents()) {
				PdfContentByte content = pdfStamper.getOverContent(po.getPageNumber());

				BaseFont basefont = new TextContent2BaseFont().process(po);
				Font font = new TextContent2Font().process(po);
				int align = po.getAlignment();
				int llx = po.getLowerLeftX();
				int lly = po.getLowerLeftY();
				int urx = po.getUpperRightX();
				int ury = po.getUpperRightY();
				float leading = po.getLeading();

				content.setFontAndSize(basefont, po.getFontSize());

				content.beginText();

				Paragraph paragraph = new Paragraph(po.getText(), font);
				paragraph.setAlignment(align);
				paragraph.setLeading(leading);

				ColumnText ct = new ColumnText(content);
				ct.setSimpleColumn(llx, lly, urx, ury, leading, align);
				ct.addElement(paragraph);
				ct.go();

				content.endText();

			}
			pdfStamper.close();
			result = true;
		} catch (IOException e) {
			Logger.getRootLogger().info(resultFile.getAbsoluteFile() + " | " + e.getMessage() + " | " + datum.getTemplateurl() + " | 201106071250");
		} catch (DocumentException e) {
			Logger.getRootLogger().info("201106071249");
		}
		return result;
	}

}
