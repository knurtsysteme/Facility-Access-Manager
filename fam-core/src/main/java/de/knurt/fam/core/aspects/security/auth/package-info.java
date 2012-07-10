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

/**
 * utilities for base and extended authentication of the system.
 * there are two ways of authenticate a user in the Facility Access Manager.
 * the user must be authenticated in general. this is the log in with
 * username and password. after this, the user has rights to view
 * and use different pages. this is the second step resolved here.
 * @see <a href="./doc-files/classes_auth.png">class diagram</a>
 * @see <a href="./doc-files/activities_login.png">activities login</a>
 * @since 0.20090310 (03/10/2009)
 */
package de.knurt.fam.core.aspects.security.auth;

