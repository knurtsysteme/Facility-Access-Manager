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
package de.knurt.fam.core.aspects.security.auth;

import de.knurt.fam.core.model.persist.User;

/**
 * on many authentications per request, all attributes of being still auth must
 * be check again. that does not make sense more then one time per request.
 * 
 * do handle a request user set by the session. <strong>the application context scope must be
 * defined as "request".</strong>
 * 
 * @see SessionAuth#getUser()
 * @author Daniel Oltmanns
 * @since 1.4.4 (07/27/2011)
 */
class RequestAuth {
	private User requestUser = null;

	protected void setRequestUser(User requestUser) {
		this.requestUser = requestUser;
	}

	protected User getRequestUser() {
		return requestUser;
	}
}
