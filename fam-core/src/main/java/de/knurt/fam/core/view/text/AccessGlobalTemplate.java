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
package de.knurt.fam.core.view.text;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.aspects.logging.FamLog;

/**
 * get templates with velocity in template-directory/global/...
 * 
 * @author Daniel Oltmanns
 * @since 2.00-SNAPSHOT (07/21/2010)
 */
public class AccessGlobalTemplate {

  /** one and only instance of me */
  private volatile static AccessGlobalTemplate me;

  /** construct me */
  private AccessGlobalTemplate() {
    String templateDirectory = FamConnector.templateDirectory();
    Properties props = new Properties();
    props.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
    props.setProperty("resource.loader", "file");
    props.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
    props.setProperty("file.resource.loader.path", templateDirectory);
    props.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
    props.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
    props.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective");
    try {
      Velocity.init(props);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * return the one and only instance of AccessTemplate
   * 
   * @return the one and only instance of AccessTemplate
   */
  public static AccessGlobalTemplate getInstance() {
    if (me == null) { // no instance so far
      synchronized (AccessGlobalTemplate.class) {
        if (me == null) { // still no instance so far
          me = new AccessGlobalTemplate(); // the one and only
        }
      }
    }
    return me;
  }

  public String getContent(String fileResourceName, Properties props, VelocityContext context) {
    String result = null;
    try {
      for (Object key : props.keySet()) {
        context.put(key.toString(), props.get(key));
      }
      Template template = Velocity.getTemplate(fileResourceName);
      Writer writer = new StringWriter();
      template.merge(context, writer);
      result = writer.toString();
    } catch (ResourceNotFoundException e) {
      FamLog.logException(this.getClass(), e, "no resource found", 201010271533l);
    } catch (ParseErrorException e) {
      FamLog.logException(this.getClass(), e, "could not parse it", 201010271532l);
    } catch (MethodInvocationException e) {
      FamLog.logException(this.getClass(), e, "template uses unknown or defective method", 201106211156l);
    } catch (Exception e) {
      FamLog.logException(this.getClass(), e, "unknown", 201010271531l);
    }
    return result;
  }

  public String getContent(String fileResourceName, Properties props) {
    return this.getContent(fileResourceName, props, new VelocityContext());
  }

  public String getContent(String fileResourceName, VelocityContext context) {
    return this.getContent(fileResourceName, new Properties(), context);
  }

}
