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
package de.knurt.fam.template.controller.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.nebc.bu.JSONObjectFromRequest;

/**
 * delete a user from user manager with different options
 * 
 * @author Daniel Oltmanns
 * @since 1.8.0 (02/05/2012)
 */
public class DeleteUserFromUsersManagerController extends JSONController {

	private final static int TRASH = 0;
	private final static int TRASH_AND_ANONYM = 2;
	private final static int TRASH_AND_ANONYM_AND_DELETE_DATA = 3;
	private final static int TOTAL_DESTROY = 1;
	private JSONArray messages = null;
	private User auth = null;

	/**
	 * return the array of messages created
	 * 
	 * @return the array of messages created
	 */
	public JSONArray getMessages() {
		return messages;
	}

	/**
	 * construct the deletion manager
	 * 
	 * @param auth
	 *            user that is calling this
	 */
	public DeleteUserFromUsersManagerController(User auth) {
		this.auth = auth;
		this.messages = new JSONArray();
	}

	/**
	 * add a message to the user
	 * 
	 * @param content
	 *            of the message
	 * @param succ
	 *            true if it is a success message, false if it is a failing
	 *            message
	 */
	protected void addMessage(String content, boolean succ) {
		JSONObject newMessage = new JSONObject();
		try {
			newMessage.put(succ ? "1" : "0", content);
		} catch (JSONException e) {
			FamLog.exception(e, 201205070842l);
		}
		this.messages.put(newMessage);
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject(HttpServletRequest request, HttpServletResponse response) {
		boolean succ = false;
		JSONObject result = new JSONObject();
		JSONObject json = new JSONObjectFromRequest().process(request);
		int type = TRASH;
		try {
			try {
				type = json.getInt("type");
			} catch (JSONException e) {
				this.addMessage("invalid deletion type (201205021307)", false); // INTLANG
			}
		} catch (NumberFormatException e) {
			this.addMessage("invalid deletion type (201205021306)", false); // INTLANG
		}
		if (type < TRASH || type > TRASH_AND_ANONYM_AND_DELETE_DATA) {
			this.addMessage("invalid deletion type (201205091256)", false); // INTLANG
		} else {
			try {
				User user2delete = FamDaoProxy.userDao().getUserFromUsername(json.getString("user_username"));
				if (user2delete == null) {
					this.addMessage("could not found a user (201205021303)", false); // INTLANG
				} else if (user2delete.getId() != Integer.parseInt(json.getString("user_id"))) {
					this.addMessage("invalid request for a user (201205021300)", false); // INTLANG
				} else {
					// valid request for a user, user found and matching id
					switch (type) {
					case TRASH:
						succ = this.putUserToTrash(user2delete);
						break;
					case TRASH_AND_ANONYM:
						succ = this.putUserToTrashAndAnonym(user2delete);
						break;
					case TRASH_AND_ANONYM_AND_DELETE_DATA:
						succ = this.putUserToTrashAndAnonymizeAndDeleteJobs(user2delete);
						break;
					case TOTAL_DESTROY:
						succ = this.totalDestroy(user2delete);
						break;
					}
				}
			} catch (JSONException e1) {
				this.addMessage("invalid request for a user (201205021301)", false); // INTLANG
			} catch (NumberFormatException e2) {
				this.addMessage("invalid request for a user (201205021302)", false); // INTLANG
			}
		}
		try {
			result.put("succ", succ);
			result.put("messages", messages);
		} catch (JSONException e) {
			FamLog.exception(e, 201205021259l);
		}
		return result;
	}

	/**
	 * Completly delete user
	 * 
	 * Irreversible deletion of everything concerning the user. The user and
	 * user's data is not part of the system anymore. You even cannot find him
	 * here as anonymized user.
	 * 
	 * @param user
	 *            to delete
	 * @return true, if deletion has been successful
	 */
	protected boolean totalDestroy(User user) {
		boolean result = false;
		if (user.is(this.auth)) {
			this.addMessage(String.format("You cannot delete yourself", user.getFullName()), false); // INTLANG
		} else if (user.isAdmin()) {
			this.addMessage(String.format("%s is an admin and cannot be deleted", user.getFullName()), false); // INTLANG
		} else if (FamAuth.hasAllRights(this.auth, new Integer[] { FamAuth.DELETE_USER, FamAuth.ANONYMIZE_USER, FamAuth.DELETE_USERS_DATA }, null)) {
			try {
				result = this.deleteJobs(user);
				if (result) {
					result = user.delete();
				} else {
					this.addMessage("user not deleted because of previous errors", false); // INTLANG
				}
			} catch (Exception e) {
				FamLog.exception(e, 201205030850l);
			}
		} else {
			this.addMessage("You do not have the right to do that", false); // INTLANG
		}
		if (result) {
			this.addMessage(String.format("Completly deleted %s", user.getFullName()), true); // INTLANG
		}
		return result;
	}

	private boolean deleteJobs(User user) {
		boolean result = FamDaoProxy.jobsDao().deleteJobs(this.auth, user, true);
		if (!result) {
			FamLog.error("could not delete jobs", 201205031301l);
			this.addMessage("Could not delete jobs", false); // INTLANG
		}
		return result;
	}

	/**
	 * Put user to trash, anonymize irreversible and delete all user's data
	 * 
	 * Like above but user's data are deleted as well. Which means the
	 * anonymized user has no Job Surveys, no bookings, no logbook-entries
	 * anymore.
	 * 
	 * @param user
	 *            to delete
	 * @return true, if deletion has been successful
	 */
	protected boolean putUserToTrashAndAnonymizeAndDeleteJobs(User user) {
		boolean result = false;
		if (user.is(this.auth)) {
			this.addMessage(String.format("You cannot put yourself into trash", user.getFullName()), false); // INTLANG
		} else if (user.isAdmin()) {
			this.addMessage(String.format("%s is an admin and cannot be put into trash", user.getFullName()), false); // INTLANG
		} else if (user.isAnonym()) {
			this.addMessage("this user is already anonym", false); // INTLANG
		} else if (FamAuth.hasAllRights(this.auth, new Integer[] { FamAuth.EXCLUDE_USERS, FamAuth.ANONYMIZE_USER, FamAuth.DELETE_USERS_DATA }, null)) {
			String fullNameBefore = user.getFullName();
			user.exclude();
			result = user.update();
			if (result) {
				this.addMessage("Exclude " + fullNameBefore, true); // INTLANG
			} else {
				this.addMessage("Fail to exclude " + fullNameBefore, false); // INTLANG
			}
			if (result) {
				result = this.deleteJobs(user);
				if (result) {
					this.addMessage("Jobs deleted.", true); // INTLANG
				} else {
					this.addMessage("Fail to delete jobs of " + fullNameBefore, false); // INTLANG
				}
			}
			if (result) {
				result = user.anonymize(this.auth);
				if (result) {
					this.addMessage(String.format("Anonymized %s. New username %s.", fullNameBefore, user.getUsername()), true); // INTLANG
				} else {
					this.addMessage("Fail to anonymize " + fullNameBefore, false); // INTLANG
				}
			}
		} else {
			this.addMessage("You do not have the right to do that", false); // INTLANG
		}
		return result;
	}

	/**
	 * Put user to trash and anonymize irreversible
	 * 
	 * Like above but all personal data of the user gets anonymize. That means
	 * all personal data like username, real name, adress et cetera are set to a
	 * "anonym"-value. Sending a bill or writing user an e-mail gets impossible.
	 * Only values interesting for statistics and user's data (like Job Surveys,
	 * Logbook-Entries, Bookings) are unchanged.
	 * 
	 * @param user
	 *            to delete
	 * @return true, if deletion has been successful
	 */
	protected boolean putUserToTrashAndAnonym(User user) {
		boolean result = false;
		if (user.is(this.auth)) {
			this.addMessage(String.format("You cannot put yourself into trash", user.getFullName()), false); // INTLANG
		} else if (user.isAdmin()) {
			this.addMessage(String.format("%s is an admin and cannot be put into trash", user.getFullName()), false); // INTLANG
		} else if (user.isAnonym()) {
			this.addMessage("this user is already anonym", false); // INTLANG
		} else if (FamAuth.hasAllRights(this.auth, new Integer[] { FamAuth.EXCLUDE_USERS, FamAuth.ANONYMIZE_USER }, null)) {
			String fullNameBefore = user.getFullName();
			user.exclude();
			result = user.update();
			if (result) {
				this.addMessage("Exclude " + fullNameBefore, true); // INTLANG
				result = user.anonymize(this.auth);
				if (result) {
					this.addMessage(String.format("Anonymized %s. New username %s.", fullNameBefore, user.getUsername()), true); // INTLANG
				} else {
					this.addMessage("Fail to anonymize " + fullNameBefore, false); // INTLANG
				}
			} else {
				this.addMessage("Fail to exclude " + fullNameBefore, false); // INTLANG
			}
		} else {
			this.addMessage("You do not have the right to do that", false); // INTLANG
		}
		return result;
	}

	/**
	 * Put user to trash
	 * 
	 * Other user cannot see daoltma1 anymore. You and all other administrators
	 * will still see daoltma1 in this User-Manager and you are able to restore
	 * daoltma1.
	 * 
	 * @see User#exclude()
	 * @param user
	 *            to delete
	 * @return true, if deletion has been successful
	 */
	protected boolean putUserToTrash(User user) {
		boolean result = false;
		if (user.is(this.auth)) {
			this.addMessage(String.format("You cannot put yourself into trash", user.getFullName()), false); // INTLANG
		} else if (user.isAdmin()) {
			this.addMessage(String.format("%s is an admin and cannot be put into trash", user.getFullName()), false); // INTLANG
		} else if (FamAuth.hasRight(this.auth, FamAuth.EXCLUDE_USERS, null)) {
			try {
				user.exclude();
				result = user.update();
				if (result) {
					this.addMessage(String.format("Put %s to trash", user.getFullName()), true); // INTLANG
				} else {
					this.addMessage(String.format("Put %s to trash failed", user.getFullName()), false); // INTLANG
				}
			} catch (Exception e) {
				FamLog.exception(e, 201205030826l);
			}
		} else {
			this.addMessage("You do not have the right to put user into trash", false); // INTLANG
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onException(IOException ex) {
		FamLog.exception(ex, 201205021238l);
	}

}
