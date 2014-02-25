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
package de.knurt.fam.template.model;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.model.config.Logbook;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.QueryStringBuilder;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.template.util.TemplateHtml;
import de.knurt.heinzelmann.util.query.QueryString;

/**
 * produce the model for tutorials
 * 
 * @see TemplateContentProperties#getTemplateModel(TemplateResource)
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/21/2010)
 */
public class LogbookMakePostModelFactory {

  public Properties getProperties(TemplateResource templateResource) {
    Properties result = new Properties();
    WritingResultProperties wrp = templateResource.getWritingResultProperties();
    if (wrp != null) {
      result.put("is_post", true);
      if ((Boolean) wrp.get("succ") == true) {
        QueryString qs = QueryStringBuilder.getLogbookQueryString(wrp.getProperty("key"));
        qs.put(QueryKeys.QUERY_KEY_POST_REQUEST_SUCCEEDED, QueryKeys.QUERY_DEFAULT_VALUE_POST_REQUEST_SUCCEEDED);
        RedirectResolver.redirectClient("logbook", templateResource, result, qs);
      }
      result.putAll(wrp);
    } else {
      result.put("is_post", false);
      result.put("logbook", this.getEmptyLogbookEntry());
    }
    String logbookKey = this.getLogbookKey(templateResource.getRequest());
    if (logbookKey == null) {
      logbookKey = LogbookConfigDao.getInstance().getAll().get(0).getKey();
    }
    if (LogbookConfigDao.getInstance().keyExists(logbookKey) && LogbookConfigDao.getInstance().isVisibleFor(logbookKey, templateResource.getAuthUser())) {
      Logbook logbook = LogbookConfigDao.getInstance().getConfiguredInstance(logbookKey);
      result.put("logbook_label", logbook.getLabel());
      result.put("logbook_description", logbook.getDescription());
      result.put("logbook_query", QueryStringBuilder.getLogbookQueryString(logbookKey));
      result.put("logbook_musttags", logbook.getTags());
      result.put("logbook_logbookId", logbookKey);
      result.put("logbook_logbookId_queryKey", QueryKeys.QUERY_KEY_LOGBOOK);
      result.put("language", FamRequestContainer.locale().getDisplayLanguage(FamRequestContainer.locale()));
      result.put("mainaddress", TemplateHtml.href("logbookmakepost"));
    } else {
      RedirectResolver.redirectClient(RedirectTarget.PROTECTED_HOME, templateResource);
    }
    return result;
  }

  private LogbookEntry getEmptyLogbookEntry() {
    LogbookEntry result = new LogbookEntry();
    result.setTags(null);
    result.setHeadline("");
    result.setContent("");
    return result;
  }

  private String getLogbookKey(HttpServletRequest rq) {
    return rq.getParameter(QueryKeys.QUERY_KEY_LOGBOOK);
  }

}
