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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jcouchdb.document.Attachment;
import org.json.JSONException;
import org.json.JSONObject;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.couchdb.FamCouchDBDao;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.FamShoppingCart;
import de.knurt.fam.core.model.persist.document.Job;
import de.knurt.fam.core.model.persist.document.JobDataProcessing;
import de.knurt.fam.core.util.bu.FileOfUser;
import de.knurt.heinzelmann.util.nebc.bu.JSONObject2Properties;
import de.knurt.heinzelmann.util.query.HttpServletRequestConverter;

/**
 * post job surveys. if there is a key "attachments" and the value of
 * "attachments" is an JSONArray with files exist for the user auth, it is
 * interpreted and inserted as {@link Attachment}
 * 
 * @author Daniel Oltmanns
 * @since 1.7.0 (03/06/2012)
 */
public class JobSurveyPostController extends JSONController2 {

	private boolean succ = false;
	private String reason = null;
	private Booking booking = null;
	private int step;
	private JSONObject jobSurvey = null;
	private JobDataProcessing jdp = null;
	private User user = null;
	private JSONObject inputs = null;

	public JobSurveyPostController(HttpServletRequest request) {
		try {
			inputs = HttpServletRequestConverter.me().getJSONObject(request);
			user = SessionAuth.user(request);
			fou = new FileOfUser(user);
			boolean insertJob = this.setParameter() && this.setBooking();
			if (insertJob) {
				this.setAndInsertNotice();
				this.setAndInsertJob();
			}
		} catch (Exception e) {
			// idiot or none inputs
			FamLog.exception(e, 201203071247l);
			succ = false;
			reason = "invalid request";
			this.rollback();
		}
	}

	private void setAndInsertNotice() {
		try {
			if (inputs.getJSONObject("main").getString("notice").trim().isEmpty() == false) {
				booking.setNotice(inputs.getJSONObject("main").getString("notice"));
				booking.update();
			}
		} catch (Exception e) {
			// OK!
		}
	}

	/**
	 * return true on a valid request
	 */
	private boolean setParameter() {
		boolean result = false;
		if (user == null) {
			reason = "lost session";
		} else {
			try {
				JSONObject main = inputs.getJSONObject("main");
				if (inputs == null || main.get("step") == null || main.get("idJobDataProcessing") == null || main.get("idJobDataProcessing").toString().isEmpty() || inputs.get("jobSurvey") == null) {
					reason = "invalid inputs";
				}
				step = Integer.parseInt(main.getString("step"));
				if (step < 0 || step > 4) {
					reason = "invalid job step";
				}
				jobSurvey = inputs.getJSONObject("jobSurvey");
				jdp = FamCouchDBDao.getInstance().getOne(main.getString("idJobDataProcessing"), JobDataProcessing.class);

				try {
					if (jdp == null) {
						FamLog.error("invalid job data processing id", 201203061218l);
						reason = "invalid job data processing id";
					} else if (jobSurvey == null) {
						FamLog.error("invalid job data processing id", 201203071118l);
						reason = "invalid job data processing id";
					} else {
						// here everything is set as needed!
						result = true;
					}
				} catch (Exception e2) {
					FamLog.exception(e2, 201203061217l);
					reason = "invalid job data processing id";
				}
			} catch (NumberFormatException e) {
				FamLog.exception(e, 201203061213l);
				reason = "invalid job step";
			} catch (JSONException e) {
				FamLog.exception(e, 201203061212l);
				reason = "invalid inputs";
			} catch (Exception e) {
				FamLog.exception(e, 201203061211l);
				reason = "unknown reason";
			}
		}
		return result;
	}

	private boolean isRequestForBooking() {
		boolean result = false;
		try {
			FamShoppingCart cs = user.getShoppingCart();
			result = cs != null && cs.getArticles().size() > 0 && (Booking) cs.getArticle(inputs.getJSONObject("main").getString("v")) != null;
		} catch (Exception e) { // not a request for booking
			result = false;
		}
		return result;
	}

	private boolean setBooking() {
		if (this.isRequestForBooking()) {
			boolean hasBeenPurchased = false;
			if (user != null && step == 0) {
				FamShoppingCart sc = user.getShoppingCart();
				try {
					booking = (Booking) sc.getArticle(inputs.getJSONObject("main").getString("v"));
					hasBeenPurchased = booking.purchase();
				} catch (Exception e) { // wrong object for this id
					succ = false;
					reason = "someone snatched the time slot from under your nose. Please try again with different time";
					FamLog.logException(this.getClass(), e, "session", 201203071231l);
				}
			} else if (user != null) { // step > 0
				succ = false;
				reason = "lost session";
				FamLog.error("session", 201203301314l);
			}
			if (!hasBeenPurchased) {
				succ = false;
				reason = "someone snatched the time slot from under your nose. Please try again with different time";
			}
			return hasBeenPurchased;
		} else {
			boolean bookingIsGivenAndIsValid = false;
			try {
				int jobid = Integer.parseInt(inputs.getJSONObject("main").getString("jobid"));
				Booking candidate = FamDaoProxy.bookingDao().getBookingWithId(jobid);
				if (candidate != null && (candidate.getUsername().equals(user.getUsername()) || user.hasResponsibility4Facility(candidate.getFacility()))) {
					bookingIsGivenAndIsValid = true;
					booking = candidate;
				}
			} catch (Exception e) {
				// OK!
			}
			return bookingIsGivenAndIsValid;
		}
	}

	private FileOfUser fou = null;

	private void setAndInsertJob() {
		boolean hasAttachments = jobSurvey.has("attachments");
		boolean errorOnInserAttachments = false;
		Job job = new Job();
		job.setStep(step);
		job.setUsername(user.getUsername());
		job.setIdJobDataProcessing(jdp.getId());
		job.setJobSurvey(new JSONObject2Properties().process(jobSurvey));
		job.setJobId(booking.getId());

		// try to insert many files
		if (hasAttachments) {
			List<File> attachments = new ArrayList<File>();
			try {
				for (int i = 0; i < jobSurvey.getJSONArray("attachments").length(); i++) {
					String filename = jobSurvey.getJSONArray("attachments").getString(i);
					File attachment = fou.process(filename);
					if (attachment != null) {
						attachments.add(attachment);
					} else {
						errorOnInserAttachments = true;
						reason = String.format("file %s does not exist anymore. please click reload, add attachment and retry.", filename);
						break;
					}
				}
			} catch (JSONException e) {
				try {
					// attachments is not an array - just one file!
					File attachment = fou.process(jobSurvey.getString("attachments"));
					attachments.add(attachment);
				} catch (JSONException e2) {
					// okay, something went wrong!
					FamLog.exception(e2, 201204201633l);
				}
			}
			job.addAttachments(attachments);
		}

		if (!hasAttachments || hasAttachments && !errorOnInserAttachments) {
			succ = job.insertOrUpdate();
		}
		if (!succ) {
			if (reason == null)
				reason = "unknown error while saving the job data";
			this.rollback();
		}

	}

	private void rollback() {
		if (booking != null && this.isRequestForBooking()) {
			if (!booking.delete()) {
				FamLog.error("could not delete booking " + booking.getId(), 201204231058l);
				booking.cancel(new Cancelation(user, Cancelation.REASON_UNKNOWN_ERROR));
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public JSONObject getJSONObject() {
		JSONObject result = new JSONObject();
		try {
			result.put("succ", succ);
			if (succ) {
				result.put("jobId", booking.getId());
			} else {
				result.put("reason", reason);
			}
		} catch (JSONException e) {
			FamLog.exception(e, 201203061026l);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void onIOException(IOException ex) {
		FamLog.exception(ex, 201203061024l);
	}

}
