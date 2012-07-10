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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.connector.RedirectTarget;
import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.cookie.CookieResolver;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.control.persistence.dao.config.FacilityConfigDao;
import de.knurt.fam.core.control.persistence.dao.config.LogbookConfigDao;
import de.knurt.fam.core.model.config.CronjobActionController;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FileUploadController;
import de.knurt.fam.core.model.persist.ContactDetail;
import de.knurt.fam.core.model.persist.LogbookEntry;
import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.Cancelation;
import de.knurt.fam.core.model.persist.booking.TimeBooking;
import de.knurt.fam.core.util.mvc.LogbookEntryForm;
import de.knurt.fam.core.util.mvc.Login;
import de.knurt.fam.core.util.mvc.QueryKeys;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.Registration;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.fam.plugin.DefaultPluginResolver;
import de.knurt.fam.template.controller.json.JobSurveyPostController;
import de.knurt.fam.template.controller.letter.LetterGeneratorEMailLetter;
import de.knurt.fam.template.controller.letter.LetterGeneratorShowLetter;
import de.knurt.fam.template.model.TemplateResource;
import de.knurt.fam.template.model.WritingResultProperties;
import de.knurt.fam.template.util.TemplateConfig;
import de.knurt.heinzelmann.ui.html.HtmlElement;
import de.knurt.heinzelmann.ui.html.HtmlFactory;

/**
 * delegate get requests to the right content.
 * 
 * [resource]/[filename]/[suffix]/delegate.fam
 * 
 * @author Daniel Oltmanns
 * @since 1.21 (09/28/2010)
 */
@Controller
public final class DelegateResourceController {

	private TemplateResource getTemplateResource(String resourceName, String filename, String suffix, HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplateResource(resourceName, filename, suffix, request, response, null);
	}

	private TemplateResource getTemplateResource(String resourceName, String filename, String suffix, HttpServletRequest request, HttpServletResponse response, WritingResultProperties writingResultProperties) {
		return TemplateResource.getTemplateResource(response, request, filename, resourceName, suffix, writingResultProperties);
	}

	private TemplateResource getTemplateResource(String resource, String filename, HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplateResource(resource, filename, "html", request, response);
	}

	private boolean isValidLocation(String templateResourceName) {
		return TemplateConfig.me().getContentProperties().getCustomConfigPage(templateResourceName) != null;
	}

	private ModelAndView afterLoginSuccess(User loggedIn, String alternativeResourceName, String filename, String suffix, HttpServletResponse response, HttpServletRequest request) {
		loggedIn.setLastLogin(new Date());
		loggedIn.update();
		FamLog.info(loggedIn.getUsername() + " logged in", 201011131403l);
		String templateResourceNameRequested = CookieResolver.getInstance().getTemplateResourceName(request);
		if (templateResourceNameRequested != null && this.isValidLocation(templateResourceNameRequested)) {
			return RedirectResolver.redirect(templateResourceNameRequested, CookieResolver.getInstance().getQueryString(request));
		} else {
			TemplateResource tr = this.getTemplateResource(alternativeResourceName, filename, suffix, request, response);
			if (this.isValid(request, tr)) {
				return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
			} else {
				return RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
			}
		}
	};

	private boolean isValid(HttpServletRequest request, TemplateResource templateResource) {
		boolean result = templateResource != null;
		if (result) {
			String configuredUrl = null;
			switch (templateResource.getVisibility()) {
			case PUBLIC:
				configuredUrl = FamConnector.baseUrlPublic();
				break;
			case PROTECTED:
				configuredUrl = FamConnector.baseUrlProtected();
				break;
			case ADMIN:
				configuredUrl = FamConnector.baseUrlAdmin();
				break;
			}
			boolean proxyIsUsed = FamConnector.getGlobalPropertyAsBoolean("proxy_is_used");
			try {
				if (configuredUrl == null || (templateResource.isRequestForContent() && (!proxyIsUsed && !request.getRequestURL().toString().startsWith(configuredUrl)) || (proxyIsUsed && !FamConnector.baseUrlPublic().split("\\/")[2].toLowerCase().equals(request.getHeader("x-forwarded-host"))))) {
					// if (configuredUrl == null ||
					// !request.getRequestURL().toString().startsWith(configuredUrl))
					// {
					// ↖ no url found or url is not the configured url: neither
					// the configured proxy url nor a none-proxy url
					FamLog.info(configuredUrl + " - " + request.getRequestURL() + " - " + request.getHeader("x-forwarded-host"), 201112182005l);
					result = false;
				}
			} catch (Exception e) {
				FamLog.exception(e, 201112141116l);
				result = false;
			}
		}
		return result;

	}

	@RequestMapping(value = "/{resource}__{filename}__{suffix}__delegate.fam", method = RequestMethod.GET)
	public final ModelAndView handleGetRequests(@PathVariable("resource") String resource, @PathVariable("filename") String filename, @PathVariable("suffix") String suffix, HttpServletResponse response, HttpServletRequest request) {
		if (suffix.equals("html")) {
			this.generalInit(response, "text/html; charset=UTF-8");
		} else if (suffix.equals("css")) {
			this.generalInit(response, "text/css; charset=UTF-8");
		} else if (suffix.equals("js")) {
			this.generalInit(response, "text/javascript; charset=UTF-8");
		}
		if (resource.equals("logout")) {
			SessionAuth.getInstance(request).kill(request, response);
		}
		TemplateResource tr = this.getTemplateResource(resource, filename, suffix, request, response);
		if (this.isValid(request, tr)) {
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		} else {
			return RedirectResolver.me().getRedirect(RedirectTarget.PUBLIC_HOME);
		}
	}

	@RequestMapping(value = "/fileupload__{filename}__{suffix}__delegate.fam")
	public final ModelAndView handleFileUpload(@PathVariable("filename") String filename, @PathVariable("suffix") String suffix, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		TemplateResource tr = this.getTemplateResource("fileupload", filename, suffix, request, response);
		if (tr.hasAuthUser()) {
			return new FileUploadController(tr, response).getModelAndView();
		} else {
			return RedirectResolver.redirect(RedirectTarget.PROTECTED_HOME);
		}
	}

	@RequestMapping(value = "/logbookmakepost__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleLogbookEntry(@ModelAttribute("lef") LogbookEntryForm lef, @PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		String key = request.getParameter(QueryKeys.QUERY_KEY_LOGBOOK);
		WritingResultProperties writingResultProperties = new WritingResultProperties();
		User user = SessionAuth.user(request);
		if (user != null && key != null && LogbookConfigDao.getInstance().keyExists(key)) {
			lef.meltTags();
			LogbookEntry le = (LogbookEntry) lef;
			le.setDate(new Date());
			le.setOfUserName(user.getUsername());
			le.setLanguage(FamRequestContainer.locale());
			try {
				le.insert();
				writingResultProperties.put("succ", true);
			} catch (DataIntegrityViolationException e) {
				writingResultProperties.put("succ", false);
			}
			writingResultProperties.put("logbook", lef);
			writingResultProperties.put("key", key);
		} else {
			writingResultProperties.put("succ", false);
		}
		TemplateResource tr = this.getTemplateResource("logbookmakepost", filename, "html", request, response, writingResultProperties);
		return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
	}

	@RequestMapping(value = "/cronjob__{filename}__html__delegate.fam", method = RequestMethod.GET)
	public final ModelAndView handleGetRequests(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		return new CronjobActionController().handleRequest(request, response);
	}

	private final void generalInit(HttpServletResponse response, String contentType) {
		response.setContentType(contentType);
		DefaultPluginResolver.init();
	}

	@RequestMapping(value = "/systemmodifyapplications__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleSystemModifyApplications(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		ModelAndView result = new SystemModifyBookingsAndApplicationsController().handleRequest(request, response);
		if (result == null) {
			TemplateResource tr = this.getTemplateResource("systemmodifyapplications", "", "html", request, response);
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		} else {
			// must be a redirect
			return result;
		}
	}

	@RequestMapping(value = "/registersent__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleRegisterSubmit(@ModelAttribute("registration") Registration registration, @PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		TemplateResource tr = this.getTemplateResource("registersent", filename, request, response);
		if (this.isValid(request, tr)) {
			return DefaultPluginResolver.me().getRegisterSubmission().handle(tr, registration, response, request);
		} else {
			return RedirectResolver.me().getRedirect(RedirectTarget.PUBLIC_HOME);
		}
	}

	@RequestMapping(value = "/changepassword__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView changepasswordSubmit(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		TemplateResource tr = new ChangePasswordController().execute(filename, response, request);
		if (tr == null) {
			return RedirectResolver.me().home(request, response);
		} else {
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		}
	}

	@RequestMapping(value = "/adminhome__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleLoginAdminSubmit(@ModelAttribute("login") Login login, @PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		if (login.fail() == false && login.getUser().hasAdminTasks()) {
			User user = login.getUser();
			SessionAuth.getInstance(request).setUser(user);
			if (login.userWantsRememberMeCookie()) {
				CookieResolver.getInstance().addCookieRememberMe(response, user);
			}
			return this.afterLoginSuccess(user, "adminhome", filename, "html", response, request);
		} else {
			TemplateResource tr = this.getTemplateResource("adminhome", filename, "html", request, response);
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		}
	}

	@RequestMapping(value = "/termsofuse__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleTermsOfUseAcceptance(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		User auth = SessionAuth.user(request);
		User got = RequestInterpreter.getUser(request);
		if (auth != null && got != null && auth.getId().intValue() == got.getId().intValue()) {
			// ↖ user is auth and allowed to accept it for its own
			auth.setAcceptedStatementOfAgreement(true);
			auth.update();
		}
		TemplateResource tr = this.getTemplateResource("corehome", filename, "html", request, response);
		return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
	}

	@RequestMapping(value = "/lettergenerator__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView lettergeneratorShowPDF(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		String event = request.getParameter("event");
		if (event != null && event.equals("show")) {
			TemplateResource tr = this.getTemplateResource("jobsmanager", filename, "html", request, response);
			return new LetterGeneratorShowLetter().process(response, tr);
		} else {
			// ↖ no or unknown event
			FamLog.error("no or unknown event: " + event, 201106131307l);
			return RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
		}
	}

	@RequestMapping(value = "/lettergenerator__{filename}__json__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView lettergeneratorSendMail(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		String event = request.getParameter("event");
		if (event != null && event.equals("email")) {
			TemplateResource tr = this.getTemplateResource("jobsmanager", filename, "html", request, response);
			return new LetterGeneratorEMailLetter().process(response, tr);
		} else {
			// ↖ no or unknown event
			FamLog.error("no or unknown event: " + event, 201106141017l);
			return RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
		}
	}

	@RequestMapping(value = "/jobsmanager__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView workOnAJob(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		boolean doChanges = false;
		User auth = SessionAuth.user(request);
		Booking booking = RequestInterpreter.getBooking(request);
		if (auth != null && auth.hasAdminTasks() && booking != null && !booking.isProcessed() && (RequestInterpreter.hasSentFlag(request) || RequestInterpreter.hasDeleteFlag(request))) {
			doChanges = auth.hasResponsibility4Facility(booking.getFacility());
		}
		if (doChanges) {
			// ↖ queue booking has been started or stopped
			if (RequestInterpreter.hasSentFlag(request)) {
				if (request.getParameter("action") != null && request.getParameter("action").equals("invoice")) {
					booking.invoice();
				} else {
					booking.processSession();
				}
			} else if (RequestInterpreter.hasDeleteFlag(request)) {
				// ↖ bookinghas been canceled
				Cancelation c = new Cancelation(auth, Cancelation.REASON_NO_REASON);
				booking.cancel(c);
			}
			TemplateResource tr = this.getTemplateResource("jobsmanager", filename, "html", request, response);
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		} else {
			response.setStatus(406); // Not Acceptable
			return null;
		}
	}

	@RequestMapping(value = "/corehome__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleLoginCoreSubmit(@ModelAttribute("login") Login login, @PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		if (login.fail() == false) {
			User user = login.getUser();
			SessionAuth.getInstance(request).setUser(user);
			if (login.userWantsRememberMeCookie()) {
				CookieResolver.getInstance().addCookieRememberMe(response, user);
			}
			return this.afterLoginSuccess(user, "corehome", filename, "html", response, request);
		} else {
			TemplateResource tr = this.getTemplateResource("corehome", filename, "html", request, response);
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		}
	}

	@RequestMapping(value = "/mybookings__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleMybookingsSubmit(@PathVariable("filename") String filename, HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		if (request.getParameter(QueryKeys.QUERY_KEY_DELETE) != null && request.getParameter(QueryKeys.QUERY_KEY_BOOKING) != null) {
			User user = SessionAuth.user(request);
			if (user != null) {
				try {
					Integer id2cancel = Integer.parseInt(request.getParameter(QueryKeys.QUERY_KEY_BOOKING));
					TimeBooking example = TimeBooking.getEmptyExampleBooking();
					example.setId(id2cancel);
					Booking shallBeCanceled = FamDaoProxy.bookingDao().getOneLike(example);
					if (shallBeCanceled != null && user.getUsername().equals(shallBeCanceled.getUsername())) {
						shallBeCanceled.cancel(new Cancelation(user, Cancelation.REASON_FREE_BY_USER));
					}
				} catch (Exception e) {
				}
			}
		}
		TemplateResource tr = this.getTemplateResource("mybookings", filename, "html", request, response);
		return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
	}

	@RequestMapping(value = "/jobsurvey__{filename}__html__delegate.fam", method = RequestMethod.GET)
	public final ModelAndView handleGetJobsurvey(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		User user = SessionAuth.user(request);
		if (user == null) {
			return RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
		} else {
			OutputStream os = null;
			try {
				os = response.getOutputStream();
				JobSurveyView jsv = JobSurveyViewFactory.getJobSurveyView(user, request, response);
				if (jsv.isValidRequest()) {
					IOUtils.write(jsv.getView(), os);
					response.flushBuffer();
				} else {
					FamLog.error("no jobsurvey found!;" + request.getQueryString() + ";" + user.getUsername(), 201203281229l);
					IOUtils.write("please report error 201203281229-" + FamDateFormat.getDateAndTimeShort(), os);
				}
			} catch (IOException e) {
				FamLog.exception(e, 201202160927l);
			} finally {
				IOUtils.closeQuietly(os);
			}
			return null;
		}
	}

	@RequestMapping(value = "/jobsurvey__{filename}__json__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handlePostJobsurvey(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		return new JobSurveyPostController(request).handleRequest(request, response);
	}

	@RequestMapping(value = "/jobsurveypreview__{filename}__html__delegate.fam")
	public final ModelAndView handleJobsurveyPreview(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		User user = SessionAuth.user(request);
		if (user == null) {
			return RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
		} else {
			PrintWriter pw = null;
			try {
				pw = response.getWriter();
				JobSurveyView jsv = new JobSurveyViewPreviewHtml(user, request, response);
				if (jsv.isValidRequest()) {
					IOUtils.write(jsv.getView(), pw);
				} else {
					return RedirectResolver.redirect(RedirectTarget.PUBLIC_HOME);
				}
			} catch (IOException e) {
				FamLog.exception(e, 201202170829l);
			} finally {
				IOUtils.closeQuietly(pw);
			}
			return null;
		}
	}

	@RequestMapping(value = "/editsoa__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleEditsoaPosts(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		ModelAndView result = new EditSoaController().handleRequest(request, response);
		if (result == null) {
			TemplateResource tr = this.getTemplateResource("editsoa", "", "html", request, response);
			return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
		} else {
			// must be a redirect
			return result;
		}
	}

	@RequestMapping(value = "/facilityemergency__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView setSuddenFailure(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		TemplateResource templateResource = this.getTemplateResource("facilityemergency", "", "html", request, response);
		if (templateResource.hasAuthUser()) {
			templateResource.putWritingResultProperty("succ", new FacilityEmergencyController().submit(templateResource));
		}
		return TemplateConfig.me().getResourceController().handleGetRequests(templateResource, response, request);
	}

	@RequestMapping(value = "/systemfacilityavailability__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView setFacilityAvailability(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		TemplateResource templateResource = this.getTemplateResource("systemfacilityavailability", "", "html", request, response);
		if (templateResource.hasAuthUser()) {
			templateResource.putWritingResultProperty("succ", new FacilityAvailabilityController().submit(templateResource));
		}
		return TemplateConfig.me().getResourceController().handleGetRequests(templateResource, response, request);
	}

	@RequestMapping(value = "/editfacilityavailability__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView editFacilityAvailability(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		TemplateResource templateResource = this.getTemplateResource("editfacilityavailability", "", "html", request, response);
		if (templateResource.hasAuthUser()) {
			templateResource.putWritingResultProperty("succ", new EditFacilityAvailabilityController().submit(templateResource));
		}
		return TemplateConfig.me().getResourceController().handleGetRequests(templateResource, response, request);
	}

	@RequestMapping(value = "/contactdetails__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView deleteContactDetail(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		User authuser = SessionAuth.user(request);
		TemplateResource tr = this.getTemplateResource("contactdetails", "", "html", request, response);
		if (authuser != null && RequestInterpreter.hasDeleteFlag(request)) {
			String of = RequestInterpreter.getOf(request);
			if (of.equals("company")) {
				authuser.setCompany(null);
			} else if (of.equals("address")) {
				authuser.setMainAddress(null);
			} else if (of.equals("phone2")) {
				authuser.setPhone2(null);
			} else if (of.equals("phone1")) {
				authuser.setPhone1(null);
			} else if (of.equals("fname")) {
				authuser.setFname(null);
			} else if (of.equals("intendedResearch")) {
				authuser.setIntendedResearch(null);
			} else if (of.equals("sname")) {
				authuser.setSname(null);
			} else if (of.equals("male")) {
				authuser.setMale(null);
			} else if (of.equals("title")) {
				authuser.setTitle(null);
			} else if (of.equals("birthdate")) {
				authuser.setBirthdateNull();
			} else if (of.equals("departmentLabel")) {
				authuser.setDepartmentLabel(null);
			} else if (of.startsWith("cd_")) {
				int cd_id = -1;
				try {
					cd_id = Integer.parseInt(of.substring(3));
				} catch (NumberFormatException e) {
				} // do nothing
				for (ContactDetail cd : authuser.getContactDetails()) {
					if (cd.getId() == cd_id) {
						cd.delete(); // this must be user's contact
						break;
					}
				}
			}
			authuser.update();
		}
		return TemplateConfig.me().getResourceController().handleGetRequests(tr, response, request);
	}

	@RequestMapping(value = "/{resource}__{filename}__json__delegate.fam")
	public final ModelAndView handleJSONRequest(@PathVariable("resource") String resource, HttpServletResponse response, HttpServletRequest request) {
		return TemplateConfig.me().getResourceController().handleJSONRequest(resource, response, request);
	}

	@RequestMapping(value = "/{resource}__{filename}__png__delegate.fam", method = RequestMethod.GET)
	public final ModelAndView handleImageRequest(@PathVariable("resource") String resource, HttpServletResponse response, HttpServletRequest request) {
		return TemplateConfig.me().getResourceController().handleImageRequest(resource, response, request);
	}

	/**
	 * answer a file tree of facilities compatible with jquery file tree plugin.
	 * if a user is given, all facilities that are not bookable by the user or
	 * that has no bookable child is not part of the tree. if no user is given,
	 * show entire tree.
	 * 
	 * XXX must be post because of jquery file tree plugin - but get would be
	 * more restful
	 * 
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/jqueryfacilitytree__{filename}__html__delegate.fam", method = RequestMethod.POST)
	public final ModelAndView handleJSjqueryfacilitytree(HttpServletResponse response, HttpServletRequest request) {
		this.generalInit(response, "text/html; charset=UTF-8");
		User user = SessionAuth.user(request);
		PrintWriter pw = null;
		try {
			pw = response.getWriter();
			HtmlElement result = HtmlFactory.get("ul").cla("jqueryFileTree").style("display", "none");
			List<Facility> rootFacilities = new ArrayList<Facility>();
			String rawKeys = request.getParameter("dir");
			if (rawKeys != null) {
				rawKeys = rawKeys.replaceAll("/", "");
				for (String key : rawKeys.split(", ")) {
					rootFacilities.add(FacilityConfigDao.facility(key));
				}
			}
			for (Facility rootFacility : rootFacilities) {
				if (rootFacility != null) {
					List<Facility> facilities = FacilityConfigDao.getInstance().getChildrenFacilities(rootFacility);
					for (Facility facility : facilities) {
						if (user != null && !user.hasRight(FamAuth.BOOKING, facility)) {
							continue;
						}
						HtmlElement li = HtmlFactory.get("li");
						li.cla(facility.isBookable() ? "facility" : "directory collapsed");
						HtmlElement a = HtmlFactory.get("a").att("rel", facility.getKey() + "/").att("href", "#").add(facility.getLabel());
						li.add(a);
						result.add(li);
					}
				}
			}
			IOUtils.write(result.toString(), pw);
		} catch (IOException ex) {
			FamLog.exception(ex, 201204191245l);
		} finally {
			IOUtils.closeQuietly(pw);
		}
		return null;
	}

}
