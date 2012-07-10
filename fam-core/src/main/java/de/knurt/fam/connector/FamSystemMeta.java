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
package de.knurt.fam.connector;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * validate view names for the Facility Access Manager. Assuming using an
 * {@link InternalResourceViewResolver}.
 * 
 * @see InternalResourceViewResolver
 * @author Daniel Oltmanns
 * @since 0.20091008 (10/08/2009)
 */
public class FamSystemMeta {

    /**
     * label for actual version.<br />
     */
    public final static String ACTUAL_VERSION = "1.8.1";

    /**
     * return the language used for the meta-tag
     *
     * @param rq
     *            got and the language may read from
     * @return the language used for the meta-tag
     */
    public final static String getMetaLanguage(HttpServletRequest rq) {
        return "English"; // XXX static meta-language by now
    }
    private final static Calendar DAY_DEPLOYED = Calendar.getInstance();

    /**
     * return the date of this version formatted.
     *
     * @return the date of this version formatted.
     */
    public static String getDateDeployed() {
        return DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(DAY_DEPLOYED.getTime());
    }

    private FamSystemMeta() {
    }

    /**
     * return true, if the actual configuration is a test system.
     * @see DefaultEnvironment#isDev()
     * @return true, if the actual configuration is a test system.
     */
    public static boolean isPreview() {
        return FamConnector.isPreviewSystem();
    }
}
