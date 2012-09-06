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

import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * targets to redirect to
 * 
 * @see InternalResourceViewResolver
 * @author Daniel Oltmanns
 * @since 1.3.0 (07/10/2010)
 */
public enum RedirectTarget {
	REGISTER, LOGIN, PUBLIC_HOME, PROTECTED_HOME, SYSTEM_FACILITY_AVAILABILITY_OVERVIEW, ADMIN_JOBS_MANAGER, PUBLIC_REGISTER, EDIT_SOA, BOOK_FACILITY
}
