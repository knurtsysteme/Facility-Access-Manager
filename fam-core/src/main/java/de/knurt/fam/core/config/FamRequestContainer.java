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
package de.knurt.fam.core.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * a container for the request.
 * this must be injected with request scope. it resolves then everything, that
 * is request specific (like locale for request) and cannot be resolved by
 * spring resolvers.
 * @author Daniel Oltmanns
 * @since 0.20090429 (04/29/2009)
 */
// XXX if this is not init before all other beans, you get the default locale
public class FamRequestContainer {

    /**
     * configure all variables for the request
     * @param rq got from client
     */
    public static void config(HttpServletRequest rq) {
        synchronized (FamRequestContainer.class) {
            // get config
            WebApplicationContext context = RequestContextUtils.getWebApplicationContext(rq);
            FamRequestContainer singleMe = ((FamRequestContainer) context.getBean("dasRequestContainer"));

            // set locale
            CookieLocaleResolver resol = ((CookieLocaleResolver) context.getBean("localeResolver"));
            resol.setDefaultLocale(getDefaultLocale());
            Locale setLocale = resol.resolveLocale(rq);
            singleMe.setLocale(setLocale);
        }
    }
    private Locale locale;

    private static Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }

    private Locale getLocale() {
        if (this.locale == null) {
            this.locale = getDefaultLocale();
        }
        return this.locale;
    }

    /**
     * return the {@link Locale} the user wants and the system supports.
     * @return the {@link Locale} the user wants and the system supports.
     */
    public static Locale locale() {
        return getInstance().getLocale();
    }
    /** one and only instance of me */
    private volatile static FamRequestContainer me;

    /** construct me */
    private FamRequestContainer() {
    }

    /**
     * return the one and only instance of FamRequestContainer
     * @return the one and only instance of FamRequestContainer
     */
    public static FamRequestContainer getInstance() {
        if (me == null) { // no instance so far
            synchronized (FamRequestContainer.class) {
                if (me == null) { // still no instance so far
                    me = new FamRequestContainer(); // the one and only
                }
            }
        }
        return me;
    }

    /**
     * @param locale the locale to set
     */
    private void setLocale(Locale locale) {
        this.locale = locale;
    }
}
