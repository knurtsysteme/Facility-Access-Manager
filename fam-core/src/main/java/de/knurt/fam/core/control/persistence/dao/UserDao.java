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
package de.knurt.fam.core.control.persistence.dao;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.model.persist.Address;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.UserMail;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.UserFactory;
import de.knurt.heinzelmann.util.auth.RandomPasswordFactory;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * a dao resolving all about {@link User}s. resolve {@link UserMail}s as well.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090323
 */
public abstract class UserDao extends AbstractFamDao<User> {

	/**
	 * return the user that have the given unique username.
	 * 
	 * @param username
	 *            representing a user
	 * @return the user that have the given unique username.
	 */
	public abstract User getUserFromUsername(String username);

	/** {@inheritDoc} */
	@Override
	protected void logAndThrowDataIntegrityViolationException(User user) {
		String mess = "insert fail on " + user + ".";
		DataIntegrityViolationException ex = new DataIntegrityViolationException(mess);
		FamLog.logException(UserDao.class, ex, mess, 200903270956l);
		throw ex;
	}

	/** {@inheritDoc} */
	@Override
	protected void logInsert(User user) {
		FamLog.logInfo(UserDao.class, "insert " + user + ".", 200903270957l);
	}

	/** {@inheritDoc} */
	@Override
	protected void logUpdate(User user) {
		FamLog.logInfo(UserDao.class, "update " + user + ".", 200911181630l);
	}

	/** {@inheritDoc} */
	@Override
	protected void setIdToNextId(User user) {
		user.setUserId(this.getAll().size() + 1);
	}

	private String getNewUsername(User user) {
		String result = "";
		if (user != null) {
			String fname = this.prepareUsername(user.getFname());
			String sname = this.prepareUsername(user.getSname());
			if (fname != null) {
				if (fname.length() >= 2) {
					result += fname.substring(0, 2);
				} else {
					result += fname;
				}
			}
			if (sname != null) {
				if (sname.length() >= 6) {
					result += sname.substring(0, 6);
				} else {
					result += sname;
				}
			}
		}
		if (result.equals("")) {
			result = "user" + this.prepareUsername(user.getMail());
		}
		return result;
	}

	/**
	 * return a unique username for given user username has 8 chars and is
	 * unique compared with all other users in the database. if the given user
	 * has a username, this is the base to create the unique name. Otherwise the
	 * first and second name of the user is taken. If nothing is set at all, the
	 * hashcode of the user object is taken. If the username is not unique, it
	 * gets a number: pemuelle, pemuell1, pemuell2, [...], pemuelle99.
	 * 
	 * @param user
	 *            a unique username is created for
	 * @return a unique username for user
	 */
	public String getUniqueUsername(User user) {
		String result = "";
		if (user.getUsername() == null || user.getUsername().equals("")) {
			result = this.getNewUsername(user);
		} else {
			result = user.getUsername().replaceAll("[0-9]", "");
		}
		result = this.makeUniqueUsername(result);
		return result;
	}

	// XXX on many equal usernames performance is not acceptable here!
	// TODO #29 kill that crazy stuff
	private String makeUniqueUsername(String username) {
		username = username.trim().toLowerCase();
		User example = UserFactory.me().getUserWithUsername(username);
		int i = 1;
		String baseusername = username;
		while (this.userLikeExists(example) && i < 101) {
			if (i < 10) {
				if (baseusername.length() < 8) {
					username = baseusername + i;
				} else {
					username = baseusername.substring(0, 7) + i;
				}
			} else if (i < 100) {
				if (baseusername.length() < 7) {
					username = baseusername + i;
				} else {
					username = baseusername.substring(0, 6) + i;
				}
			} else {
				username = this.getUniqueUsername(UserFactory.me().blank()); // return
				// a
				// username at random
			}
			example.setUsername(username);
			i++;
		}
		if (i >= 101) {
			// there are already to many users
			char[] possibleChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
			while (this.userLikeExists(example)) {
				String randomUsername = RandomPasswordFactory.me().getPassword(8, possibleChars);
				example = UserFactory.me().getUserWithUsername(randomUsername);
			}
		}
		return username;
	}

	/**
	 * Return true, if another user <code>like</code> exists, that is not the
	 * same user as <code>andIsNotThis</code>.
	 * 
	 * @param like
	 *            example user
	 * @param andIsNotThis
	 *            user that is the same user
	 * @return true, if another user <code>like</code> exists, that is not the
	 *         same user as <code>andIsNotThis</code>.
	 */
	public boolean userLikeExists(User like, User andIsNotThis) {
		int likecount = this.getObjectsLike(like).size();
		boolean result = likecount > 1; // there are several like given
		if (likecount == 1) { // there is exactly one user like given
			User found = this.getOneLike(like); // get the one
			result = andIsNotThis != found; // user is not the same user then
			// given
		}
		return result;
	}

	/**
	 * Return true, if another user <code>like</code> given user exists. If the
	 * found user is the same user like the given, this returns false.
	 * 
	 * @param like
	 *            user as example
	 * @return true, if another user <code>like</code> given user exists.
	 */
	public boolean userLikeExists(User like) {
		return this.userLikeExists(like, like);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isDataIntegrityViolation(User user, boolean onInsert) {
		boolean result = false;
		if (user.getMail() == null) {
			result = true;
			FamLog.debug("users mail is null " + user.getUsername(), 201011120948l);
		} else if (user.getRoleId() == null) {
			result = true;
			FamLog.debug("users roleid is null " + user.getUsername(), 201011120949l);
		} else if (user.getUsedPlattformLang() == null) {
			result = true;
			FamLog.debug("users plattformlang is null " + user.getUsername(), 201011120950l);
		} else if (user.getRegistration() == null) {
			result = true;
			FamLog.debug("users registration is null " + user.getUsername(), 201011120951l);
		} else if (user.getUsername() == null || user.getUsername().equals("")) {
			result = true;
			FamLog.debug("users username is null ", 201011120952l);
		} else if (user.getPassword() == null || user.getPassword().equals("")) {
			result = true;
			FamLog.debug("users password is null " + user.getUsername(), 201011120953l);
		}
		if (result == false && onInsert) {
			User testuser = UserFactory.me().blank();
			testuser.setMail(user.getMail());
			if (this.userLikeExists(testuser, user)) {
				result = true;
				FamLog.debug("users email exists" + user.getMail(), 201011120954l);
			}
			testuser.setMail(null);
			testuser.setUsername(user.getUsername());
			if (this.userLikeExists(testuser, user)) {
				result = true;
				FamLog.debug("users username exists" + user.getUsername(), 201011120955l);
			}
		}
		return result;
	}

	/**
	 * store the given user if and only if it does not violate the data
	 * integrity. after validating the user succeeded, it delegates to
	 * {@link #internInsert(de.knurt.fam.core.model.persist.Storeable)} always
	 * invoke {@link User#encodePassword}, so that only encoded passes are
	 * saved.
	 * 
	 * @param user
	 *            to store
	 * @throws org.springframework.dao.DataIntegrityViolationException
	 *             if storing would violate data integrity
	 */
	@Override
	public synchronized boolean insert(User user) throws DataIntegrityViolationException {
		user.encodePassword();
		return super.insert(user);
	}

	/**
	 * update the given user
	 * 
	 * @param user
	 *            to be updated
	 * @throws DataIntegrityViolationException
	 *             if the user is not storeable (like it is when user has no
	 *             name or mail address).
	 */
	@Override
	public synchronized boolean update(User user) throws DataIntegrityViolationException {
		user.encodePassword();
		return super.update(user);
	}

	/**
	 * insert the given mail into the database.
	 * 
	 * @param mail
	 *            the given mail into the database.
	 */
	public abstract boolean insert(UserMail mail);

	/**
	 * return all users, that have an account and that are not barred from the
	 * system.
	 * 
	 * @return all users, that have an account and that are not barred from the
	 *         system.
	 */
	public List<User> getNotExcludedUsersWithAccount() {
		List<User> result = new ArrayList<User>();
		List<User> candidates = this.getAll();
		for (User candidate : candidates) {
			if (candidate.hasVarifiedActiveAccount()) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * return all mails, that must be sent now.
	 * 
	 * @return all mails, that must be sent now.
	 */
	public abstract List<UserMail> getUserMailsThatMustBeSendNow();

	/**
	 * update the given mail
	 * 
	 * @param mail
	 *            to be updated
	 */
	public abstract boolean update(UserMail mail);

	/**
	 * return all user emails.
	 * 
	 * @return all user emails.
	 */
	public abstract List<UserMail> getAllUserMails();

	private String prepareUsername(String string) {
		// whatever you do here must be done in register.js in
		// Register.Username.getPresumablyUsernameUncut as well
		if (string == null) {
			return "";
		} else {
			return string.trim().toLowerCase().replaceAll("ü", "ue").replaceAll("ä", "ae").replaceAll("ö", "oe").replaceAll("ß", "ss").replaceAll("[^a-z]", "");
		}
	}

	/**
	 * return the address with the given id.
	 * 
	 * @param id
	 *            of the address
	 * @return the address with the given id.
	 */
	public abstract Address getAddress(Integer id);

	/**
	 * return all users, which accounts are verified yet.
	 * 
	 * @see User#hasVarifiedAccount()
	 * @return all users, which accounts are verified yet.
	 */
	public List<User> getAllUsersWithAccount() {
		List<User> result = new ArrayList<User>();
		List<User> candidates = this.getAll();
		for (User candidate : candidates) {
			if (candidate.hasVarifiedAccount()) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * return all users, which accounts are <strong>not</strong> verified yet.
	 * 
	 * @see User#hasVarifiedAccount()
	 * @return all users, which accounts are <strong>not</strong> verified yet.
	 */
	public List<User> getAllUsersWithoutAccount() {
		List<User> result = new ArrayList<User>();
		List<User> candidates = this.getAll();
		for (User candidate : candidates) {
			if (!candidate.hasVarifiedAccount()) {
				result.add(candidate);
			}
		}
		return result;
	}

	/**
	 * insert a given contact detail.
	 * 
	 * @see ContactDetail
	 * @param contactDetail
	 *            to insert
	 */
	public abstract boolean insert(ContactDetail contactDetail);

	/**
	 * update a given contact detail.
	 * 
	 * @see ContactDetail
	 * @param contactDetail
	 *            to update
	 */
	public abstract boolean update(ContactDetail contactDetail);

	/**
	 * delete a given existing contact detail.
	 * 
	 * @see ContactDetail
	 * @param contactDetail
	 *            to delete
	 */
	public abstract boolean delete(ContactDetail contactDetail);

	/**
	 * return all contact details matching the example.
	 * 
	 * @see ContactDetail
	 * @param example
	 *            for comparing
	 * @return all contact details matching the example.
	 */
	public abstract List<ContactDetail> getAllLike(ContactDetail example);

	public User getUserWithId(int userId) {
		User example = UserFactory.me().blank();
		example.setId(userId);
		return this.getOneLike(example);
	}

	public synchronized boolean delete(List<ContactDetail> contactDetails) {
		boolean result = true;
		boolean tmp = false;
		for (ContactDetail contactDetail : contactDetails) {
			tmp = this.delete(contactDetail);
			if (!tmp)
				result = false;
		}
		return result;
	}

	public UserMail getUserMailWithId(Integer id) {
		UserMail result = null;
		if (id != null) {
			// XXX write sql to avoid performance leaks
			List<UserMail> candidates = this.getAllUserMails();
			for (UserMail candidate : candidates) {
				if (candidate.getId().equals(id)) {
					result = candidate;
				}
			}
		}
		return result;
	}

	public abstract List<User> getUsersAccountExpired(Date day);

	public abstract List<User> getUserAccountExpiresIn(TimeFrame from);

	public abstract List<User> getUsersRegistrationIsIn(TimeFrame from);

	public abstract List<User> getUsersWithRealName(String firstname, String sirname) throws InvalidParameterException;

	public abstract List<User> getUsersWithEMail(String email) throws InvalidParameterException;

	/**
	 * set all personal user data to something anonym and update all relevated
	 * user data to this anonym user. delete all mails of the user and delete
	 * all contactdetails.
	 * 
	 * set firstname and lastname to "Anonym".
	 * 
	 * do not anonymize this statistical information
	 * <ul>
	 * <li> {@link User#getTitle()}</li>
	 * <li> {@link User#getCountry()}</li>
	 * <li> {@link User#getCity()}</li>
	 * <li> {@link User#getMale()}</li>
	 * <li> {@link User#getDepartmentKey()}</li>
	 * <li> {@link User#getDepartmentLabel()}</li>
	 * </ul>
	 * 
	 * only change username of user's {@link LogbookEntry}s and {@link Booking}s
	 * to preserve logical information.
	 * 
	 * if the user <code>auth</code> does not have the right
	 * {@link FamAuth#ANONYMIZE_USER}, do nothing and return <code>false</code>.
	 * 
	 * @param user
	 *            to anonymize
	 * @param auth
	 *            user calling this
	 * @return true if it succeeded.
	 */
	public abstract boolean anonymize(User user, User auth);

	/**
	 * return a list with users that are responsible for the given facility.
	 * 
	 * @param facility
	 *            "for"
	 * @see Facility#getKey()
	 * @return a list with users that are responsible for the given facility.
	 */
	public abstract List<User> getResponsibleUsers(Facility facility);

	/**
	 * return all users with the given role or an empty list if there is no user
	 * with the given role.
	 * 
	 * @param role
	 *            the users have
	 * @return all users with given role
	 */
	public abstract List<User> getUserWithRole(Role role);
}
