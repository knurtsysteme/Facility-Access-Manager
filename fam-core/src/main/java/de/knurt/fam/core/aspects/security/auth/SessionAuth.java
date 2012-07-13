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

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.support.RequestContextUtils;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.persistence.cookie.CookieResolver;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.news.NewsItem;
import de.knurt.heinzelmann.util.CookieUtils;
import de.knurt.heinzelmann.util.shopping.Purchasable;
import de.knurt.heinzelmann.util.shopping.ShoppingCart;

/**
 * this is a session bean that must be part of the spring context and must have
 * the scope "session". it holds the user for a session and handle the
 * authentification over cookies as well.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090331 (03/31/2009)
 */
public class SessionAuth {

	private List<NewsItem> newsItems = null;
	private Date newsItemsLastUpdate = null;

	public void setNewsItems(List<NewsItem> newsItems) {
		this.newsItemsLastUpdate = new Date();
		this.newsItems = newsItems;
	}

	public List<NewsItem> getNewsItems() {
		return newsItems;
	}

	/**
	 * return true, if the request has an user that is authenticated.
	 * 
	 * @param rq
	 *            got
	 * @return true, if the request has an user that is authenticated.
	 */
	public static boolean authUser(HttpServletRequest rq) {
		return getInstance(rq).hasAuthUser(rq);
	}

	/**
	 * return me out of the application context. this assumes, i have the
	 * session-scope
	 * 
	 * @param rq
	 *            got
	 * @return me out of the application context.
	 */
	public static SessionAuth getInstance(HttpServletRequest rq) {
		SessionAuth result = ((SessionAuth) RequestContextUtils.getWebApplicationContext(rq).getBean("sessionauth"));
		result.requestAuth = ((RequestAuth) RequestContextUtils.getWebApplicationContext(rq).getBean("requestauth"));
		return result;
	}

	private RequestAuth requestAuth = null;

	/**
	 * add an article to the shopping cart of the user.
	 * 
	 * @param rq
	 *            got
	 * @param article
	 *            being added to the shopping cart of the user
	 */
	public static void addToUsersShoppingCart(HttpServletRequest rq, Purchasable article) {
		ShoppingCart sc = user(rq).getShoppingCart();
		sc.addArticle(article);
	}

	private User user;

	/**
	 * return the user. check same user in database and set important things for
	 * the session like: excluded, accepted soa
	 * 
	 * @return the user
	 */
	private User getUser() {
		if (this.requestAuth.getRequestUser() == null) {
			if (this.user != null && this.user.getUsername() != null) {
				User userNow = FamDaoProxy.userDao().getUserFromUsername(this.user.getUsername());
				if (userNow != null) {
					this.user.setExcluded(userNow.isExcluded());
					this.user.setAcceptedStatementOfAgreement(userNow.isAcceptedStatementOfAgreement());
					this.user.setAttributesOf(userNow);
				}
			}
			this.requestAuth.setRequestUser(this.user);
			return this.user;
		} else {
			return this.requestAuth.getRequestUser();
		}
	}

	/**
	 * return auth user or null, if there is no user auth. be careful using this
	 * on pages, where the user is not auth. the page will be crash then with a
	 * null pointer exception!
	 * 
	 * @param rq
	 *            request
	 * @return auth user
	 */
	public static User user(HttpServletRequest rq) {
		SessionAuth sa = getInstance(rq);
		User result = null;
		if (sa.hasAuthUser(rq)) {
			result = sa.getUser();
		}
		return result;
	}

	/**
	 * kill this session
	 * 
	 * @param rq
	 *            got
	 * @param rs
	 *            give
	 */
	public void kill(HttpServletRequest rq, HttpServletResponse rs) {
		if (this.user != null) {
			this.user = null;
		}
		CookieUtils.deleteAll(rq, rs);
	}

	private boolean hasAuthUser() {
		return this.getUser() != null && this.getUser().hasVarifiedActiveAccount();
	}

	/**
	 * return true, if a user is saved in this session or in given cookies. if
	 * no user is saved, the cookies are searched for a user auth. and if a user
	 * is saved in the session, he is set here.
	 * 
	 * @param rq
	 *            current request
	 * @return true, if a user is saved in this session or in given cookies
	 */
	public boolean hasAuthUser(HttpServletRequest rq) {
		boolean result = this.hasAuthUser();
		if (result == false) {
			this.user = CookieResolver.getInstance().getUser(rq);
			result = this.hasAuthUser();
		}
		return result;
	}

	/**
	 * set the owner of this session. this is the method to be used after user
	 * has been authenticated.
	 * 
	 * @param user
	 *            owner of this session
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * return the date of the last update of the news items
	 * @return the date of the last update of the news items
	 */
	public Date getNewsItemsLastUpdate() {
		return newsItemsLastUpdate;
	}

}