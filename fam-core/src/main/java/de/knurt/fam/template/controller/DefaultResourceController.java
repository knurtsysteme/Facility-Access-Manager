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
package de.knurt.fam.template.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.template.controller.image.availability.AvailabilityLegendImageController;
import de.knurt.fam.template.controller.image.availability.OneMonthDayBookingsImageController;
import de.knurt.fam.template.controller.image.availability.OneMonthDayImageController;
import de.knurt.fam.template.controller.image.availability.OneWeekDayBookingsImageController;
import de.knurt.fam.template.controller.image.availability.OneWeekDayImageController;
import de.knurt.fam.template.controller.image.availability.TimeLineOfDayImageController;
import de.knurt.fam.template.controller.image.statistics.SimplePngImageController;
import de.knurt.fam.template.controller.image.statistics.StatisticDisplayOfWeekImageController;
import de.knurt.fam.template.controller.json.AdminInitPasswordController;
import de.knurt.fam.template.controller.json.BookingNoticeUpdateController;
import de.knurt.fam.template.controller.json.CouchPutController;
import de.knurt.fam.template.controller.json.DeleteUserFromUsersManagerController;
import de.knurt.fam.template.controller.json.DirectBookingRequest2Controller;
import de.knurt.fam.template.controller.json.DirectBookingRequestController;
import de.knurt.fam.template.controller.json.GetBookingsController;
import de.knurt.fam.template.controller.json.GetDetailsOfBookingController;
import de.knurt.fam.template.controller.json.GetEventsController;
import de.knurt.fam.template.controller.json.GetFacilityDetailsController;
import de.knurt.fam.template.controller.json.GetJobController;
import de.knurt.fam.template.controller.json.GetUserController;
import de.knurt.fam.template.controller.json.InsertUserController;
import de.knurt.fam.template.controller.json.JSONController;
import de.knurt.fam.template.controller.json.JobSurveyGetterController;
import de.knurt.fam.template.controller.json.JobSurveyPreviewController;
import de.knurt.fam.template.controller.json.JobSurveyPreviewSessionStoreController;
import de.knurt.fam.template.controller.json.JobSurveyPublishController;
import de.knurt.fam.template.controller.json.PrecheckUserInsertionController;
import de.knurt.fam.template.controller.json.PublicDocController;
import de.knurt.fam.template.controller.json.RegisterGetUsernameController;
import de.knurt.fam.template.controller.json.UpdateUserFromContactDetailsController;
import de.knurt.fam.template.controller.json.UpdateUserFromUsersManagerController;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.util.TemplateConfig;

/**
 * this implements the default behaviour of the Facility Access Manager in
 * working with data input and output. as a default, this uses the
 * {@link DefaultAnswerFactory} for outputs. howevery, you can inject your own.
 * 
 * every post request has a own method.
 * 
 * this is the class to override on writing plugins or module specific
 * behaviour.
 * 
 * @author Daniel Oltmanns
 * @since 1.3.0 (10/06/2010)
 * @version 1.3.0 (13/11/2010)
 */
public class DefaultResourceController implements ResourceController {

	/** {@inheritDoc} */
	@Override
	public ModelAndView handleGetRequests(TemplateResource templateResource, HttpServletResponse response, HttpServletRequest request) {
		ModelAndView result = null;
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
		} catch (Exception e) {
			try {
				response.sendError(404);
			} catch (IOException e1) {
				FamLog.exception(e1, 201011141515l);
			}
		}
		if (templateResource.getSuffix().equals("html")) {
			result = TemplateConfig.me().getAnswerFactory().answerHTML(templateResource, response, request, pw);
		} else if (templateResource.getSuffix().equals("css")) {
			result = TemplateConfig.me().getAnswerFactory().answerCSS(templateResource, response, request, pw);
		} else if (templateResource.getSuffix().equals("js")) {
			result = TemplateConfig.me().getAnswerFactory().answerJS(templateResource, response, request, pw);
		} else if (templateResource.getSuffix().equals("json")) {
			result = TemplateConfig.me().getAnswerFactory().answerJSON(templateResource, response, request, pw);
		} else {
			result = TemplateConfig.me().getAnswerFactory().answerUnknown(templateResource, response, request, pw);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public ModelAndView handleJSONRequest(String resourceName, HttpServletResponse response, HttpServletRequest request) {
		boolean hasAuthUser = SessionAuth.authUser(request);
		User user = SessionAuth.user(request);
		if (resourceName.equals("couchput") && hasAuthUser) {
			return new CouchPutController().handleRequest(request, response);
		} else if (resourceName.equals("publicdoc") && hasAuthUser) {
			return new PublicDocController().handleRequest(request, response);
		} else {
			JSONController controller = null;
			if (resourceName.equals("getbookings") && hasAuthUser) {
				controller = new GetBookingsController(user);
			} else if (resourceName.equals("job") && hasAuthUser) {
				controller = new GetJobController(user);
			} else if (resourceName.equals("insertuser") && hasAuthUser) {
				controller = new InsertUserController();
			} else if (resourceName.equals("precheckuserinsertion") && hasAuthUser) {
				controller = new PrecheckUserInsertionController();
			} else if (resourceName.equals("directbookingrequest") && hasAuthUser) {
				controller = new DirectBookingRequestController();
			} else if (resourceName.equals("directbookingrequest2") && hasAuthUser) {
				controller = new DirectBookingRequest2Controller();
			} else if (resourceName.equals("bookingnoticeupdate") && hasAuthUser) {
				controller = new BookingNoticeUpdateController();
			} else if (resourceName.equals("registergetusername") && hasAuthUser) {
				controller = new RegisterGetUsernameController();
			} else if (resourceName.equals("savecontactdetail") && hasAuthUser) {
				controller = new UpdateUserFromContactDetailsController();
			} else if (resourceName.equals("updateuser") && hasAuthUser && user.isAdmin()) {
				controller = new UpdateUserFromUsersManagerController();
			} else if (resourceName.equals("deleteuser") && hasAuthUser && user.isAdmin()) {
				controller = new DeleteUserFromUsersManagerController(user);
			} else if (resourceName.equals("getuser") && hasAuthUser && user.hasAdminTasks()) {
				controller = new GetUserController();
			} else if (resourceName.equals("getdetailsofbooking") && hasAuthUser) {
				controller = new GetDetailsOfBookingController(user);
			} else if (resourceName.equals("admininitpass") && hasAuthUser && user.isAdmin()) {
				controller = new AdminInitPasswordController();
			} else if (resourceName.equals("jobsurveypublish") && hasAuthUser && user.hasAdminTasks()) {
				controller = new JobSurveyPublishController(user);
			} else if (resourceName.equals("jobsurveypreview") && hasAuthUser && user.hasAdminTasks()) {
				controller = new JobSurveyPreviewController(user);
			} else if (resourceName.equals("jobsurveypreviewsessionstore") && hasAuthUser && user.hasAdminTasks()) {
				controller = new JobSurveyPreviewSessionStoreController(user);
			} else if (resourceName.equals("getevents") && hasAuthUser) {
				controller = new GetEventsController(user);
			} else if (resourceName.equals("getfacilitydetails") && hasAuthUser) {
				controller = new GetFacilityDetailsController(user);
			} else if (resourceName.equals("jobsurveygetter") && hasAuthUser) {
				controller = new JobSurveyGetterController();
			}
			return controller != null ? controller.handleRequest(request, response) : null;
		}
	}

	/** {@inheritDoc} */
	@Override
	public ModelAndView handleImageRequest(String resourceName, HttpServletResponse response, HttpServletRequest request) {
		SimplePngImageController controller = null;
		if (resourceName.equals("onemonthdaybookingsimage")) {
			controller = new OneMonthDayBookingsImageController();
		} else if (resourceName.equals("availabilitylegendimage")) {
			controller = new AvailabilityLegendImageController();
		} else if (resourceName.equals("oneweekdaybookingsimage")) {
			controller = new OneWeekDayBookingsImageController();
		} else if (resourceName.equals("statisticdisplayofweekimage") && (RequestInterpreter.containsDirectAccess("statistics", request) || SessionAuth.authUser(request))) {
			controller = new StatisticDisplayOfWeekImageController();
		} else if (resourceName.equals("onemonthdayimage")) {
			controller = new OneMonthDayImageController();
		} else if (resourceName.equals("oneweekdayimage")) {
			controller = new OneWeekDayImageController();
		} else if (resourceName.equals("timelineofdayimage")) {
			controller = new TimeLineOfDayImageController();
		}
		try {
			return controller != null ? controller.handleRequest(request, response) : null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
