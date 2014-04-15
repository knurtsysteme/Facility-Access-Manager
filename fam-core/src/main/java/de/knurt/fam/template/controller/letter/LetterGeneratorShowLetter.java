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
package de.knurt.fam.template.controller.letter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.MathTool;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.view.text.FamDateFormat;
import de.knurt.fam.template.model.TemplateResource;

/**
 * resolve a request for a letter. answer with a pdf file.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.4.0 (06/13/2011)
 * 
 */
public class LetterGeneratorShowLetter {

  private DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");

  /**
   * a pdf file styled as defined in configuration and the content of the request. the style is defined in custom/letter_style.json (no other
   * possibility by now)
   * 
   * @param response a pdf file
   * @param request original
   * @return a pdf file styled as defined in configuration and the content of the request
   */
  public ModelAndView processGeneralLetter(HttpServletResponse response, TemplateResource tr) {
    // get template from local filesystem
    String customid = tr.getAuthUser().getUsername() + "-s";
    PostMethod post = new LetterFromHttpServletRequestToPostMethod(customid).process(tr.getRequest());
    this.processIntern(response, post, customid);
    // set invoiced
    InvoiceBookingResolver lgub = new InvoiceBookingResolver(tr);
    lgub.invoice();
    return null;
  }

  private void processIntern(HttpServletResponse response, PostMethod post, String customid) {
    // ↓ forward response got
    // ↘ force "save as" in browser
    String downloadFilename = this.df.format(new Date()) + "-" + customid + ".pdf";
    response.setHeader("Content-Disposition", "attachment; filename=" + downloadFilename);
    // ↘ it is a pdf
    response.setContentType("application/pdf");
    try {
      ServletOutputStream ouputStream = response.getOutputStream();
      ouputStream.write(post.getResponseBody());
      ouputStream.flush();
      ouputStream.close();
    } catch (IOException e) {
      FamLog.exception(e, 201106131408l);
    }
  }

  public JSONObject getTermsLetterStyle(User target, String customid) {
    VelocityContext context = new VelocityContext();
    context.put("user", target);
    context.put("FamDateFormat", FamDateFormat.class);
    context.put("math", new MathTool());
    context.put("templateurl", FamConnector.getGlobalProperty("service_termspdf__templateurl"));
    context.put("customid", customid);
    return new LetterFromVelocityContextToJSONObject("custom/letter_terms_style.json").process(context);
  }

  public ModelAndView processTerms(HttpServletResponse response, User target) {
    String customid = target.getUsername() + "-terms";
    PostMethod post = new FamServicePDFResolver().process(this.getTermsLetterStyle(target, customid));
    this.processIntern(response, post, customid);
    return null;
  }

}
