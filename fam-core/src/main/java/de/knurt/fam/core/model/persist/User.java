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
package de.knurt.fam.core.model.persist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.aspects.security.auth.FamAuth;
import de.knurt.fam.core.aspects.security.auth.ViewPageAuthentication;
import de.knurt.fam.core.aspects.security.encoder.FamUserPassEncoderControl;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.Role;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.model.persist.booking.FamShoppingCart;
import de.knurt.fam.core.persistence.dao.BookingDao;
import de.knurt.fam.core.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.persistence.dao.UserDao;
import de.knurt.fam.core.persistence.dao.config.RoleConfigDao;
import de.knurt.fam.core.util.mvc.validator.InvalidRoleIdException;
import de.knurt.fam.core.util.mvc.validator.MandatoryUserFieldValidator;
import de.knurt.fam.core.util.mvc.validator.RegistrationValidator;
import de.knurt.fam.template.util.ContactDetailsRequestHandler;
import de.knurt.heinzelmann.util.adapter.ViewableObject;
import de.knurt.heinzelmann.util.auth.Authenticatable;
import de.knurt.heinzelmann.util.query.Identificable;

/**
 * a data holder for a person using the system
 * 
 * @author Daniel Oltmanns
 * @since 0.20090303 (03/03/2009)
 */
public class User implements Storeable, Authenticatable, ViewableObject, Identificable {

	/*
	 * this is for future version, if a user may be language specific
	 */
	private Date registration, lastLogin, birthdate, accountExpires;
	private Boolean male, excluded;
	private boolean passwordEncoded, statementOfAgreementAccepted, anonym = false;
	private Locale usedPlattformLang;
	private String password, phone1, phone2, company, fname, title, mail, sname, username, departmentKey, departmentLabel, intendedResearch;
	private String roleId;
	private Address mainAddress;
	private ArrayList<Address> addresses;
	private Integer userId;
	private Map<String, Integer> directBookingCredits;
	private FamShoppingCart shoppingCart = new FamShoppingCart();

	public String getIntendedResearch() {
		return intendedResearch;
	}

	public void setIntendedResearch(String intendedResearch) {
		this.intendedResearch = intendedResearch;
	}

	public String getDepartmentKey() {
		return departmentKey;
	}

	public String getPhone() {
		String result = "";
		if (phone1 != null && !phone1.equals("")) {
			result = phone1;
		}
		if (phone2 != null && !phone2.equals("")) {
			if (!result.equals("")) {
				result += " / ";
			}
			result += phone2;
		}
		return result;
	}

	public void setDepartmentKey(String departmentKey) {
		this.departmentKey = departmentKey;
	}

	public String getDepartmentLabel() {
		return departmentLabel;
	}

	public void setDepartmentLabel(String departmentLabel) {
		this.departmentLabel = departmentLabel;
	}

	/**
	 * return the list of all bookings the user made.
	 * 
	 * @see BookingDao#getAllBookingsOfUser(de.knurt.fam.core.model.persist.User)
	 * @return the list of all bookings the user made.
	 */
	public List<Booking> getBookings() {
		return FamDaoProxy.bookingDao().getAllBookingsOfUser(this);
	}

	/**
	 * return the name of the role the user have
	 * 
	 * @return the name of the role the user have
	 */
	public String getRoleLabel() {
		Role r = RoleConfigDao.getInstance().getRole(this.getRoleId());
		return r.getLabel();
	}

	@Override
  public void setId(Integer id) {
		this.setUserId(id);
	}

	/**
	 * forwarding {@link #setPassword(java.lang.String)} without getting angry
	 * aber sql "password" making orm difficult.
	 * 
	 * @see #setPassword(java.lang.String)
	 * @param pass
	 *            to set
	 */
	public void setPazzword(String pass) {
		this.setPassword(pass);
	}

	/**
	 * return the id representing the main address or <code>null</code> if not
	 * mainadress is set.
	 * 
	 * @return the id representing the main address or <code>null</code> if not
	 *         mainadress is set.
	 */
	public Integer getMainAddressId() {
		return this.mainAddress == null ? null : this.mainAddress.getId();
	}

	/**
	 * return true, if needed information missed. needed information are things
	 * like accepting the statement of agreement, the title for the user, is
	 * user male or female etc. this is used to decide, if information must be
	 * entered up by the user and commonly return false if user logged in the
	 * first time authenticating via an ldap server. it is <strong>not</strong>
	 * a replacement for {@link RegistrationValidator}.
	 * 
	 * administrators never have unsufficient information independent of
	 * existing information.
	 * 
	 * @return true, if needed information missed or user is admin.
	 */
	public Boolean hasUnsufficientContactDetails() {
		try {
			return !MandatoryUserFieldValidator.getInstance().isSufficient(this);
		} catch (InvalidRoleIdException e) {
			FamLog.exception(e, 201011151038l);
			return true;
		}
	}

	/**
	 * return shopping cart of the user.
	 * 
	 * @return shopping cart of the user.
	 */
	public FamShoppingCart getShoppingCart() {
		return this.shoppingCart;
	}

	/**
	 * return true, if the user is an admin.
	 * 
	 * @see RoleConfigDao#isAdmin(de.knurt.fam.core.model.persist.User)
	 * @return true, if the user is an admin.
	 */
	public Boolean isAdmin() {
		return RoleConfigDao.getInstance().isAdmin(this);
	}

	/**
	 * return true, if the user is allowed to book a facility without
	 * application.
	 * 
	 * @param onFacility
	 *            the facility to check this right for
	 * @see FamAuth#DIRECT_BOOKING
	 * @return true, if the user is allowed to book a facility without
	 *         application.
	 */
	public Boolean isAllowedToBookWithoutApplication(Facility onFacility) {
		return FamAuth.hasRight(this, FamAuth.DIRECT_BOOKING, onFacility);
	}

	/**
	 * return true, if the user is allowed to override applications.
	 * 
	 * @param onFacility
	 *            the facility to check this right for
	 * @see FamAuth#OVERRIDE_APPLICATIONS
	 * @return true, if the user is allowed to override applications.
	 */
	public Boolean isAllowedToOverrideApplications(Facility onFacility) {
		return FamAuth.hasRight(this, FamAuth.OVERRIDE_APPLICATIONS, onFacility);
	}

	/**
	 * set all transferable attributes of given user to this user.
	 * 
	 * @param user
	 */
	public void setAttributesOf(User user) {
		assert user != null;
		this.setBirthdate(user.getBirthdate());
		this.setMale(user.isMale());
		this.setPhone1(user.getPhone1());
		this.setPhone2(user.getPhone2());
		this.setCompany(user.getCompany());
		this.setFname(user.getFname());
		this.setTitle(user.getTitle());
		this.setMail(user.getMail());
		this.setSname(user.getSname());
		this.setMainAddress(user.getMainAddress());
		this.setDepartmentKey(user.getDepartmentKey());
		this.setDepartmentLabel(user.getDepartmentLabel());
	}

	/**
	 * set the key representing the role of the user.
	 * 
	 * @see Role
	 * @param roleId
	 *            the key representing the role of the user.
	 */
	public void setRoleId(String roleId) {
		if (RoleConfigDao.getInstance().roleIdExists(roleId)) {
			this.roleId = roleId;
		} else {
			FamLog.error(User.class, "try to set a role id that is not configured", 200904111505l);
		}
	}

	/**
	 * return the key representing the role of the user.
	 * 
	 * @see Role
	 * @return the key representing the role of the user.
	 */
	public String getRoleId() {
		return roleId;
	}

	/**
	 * nothing set here
	 */
	public User() {
	}

	/**
	 * encode password of the user. if and only if it is not already encoded and
	 * if and only if a password for the user is set.
	 */
	public void encodePassword() {
		if (this.password != null && this.passwordEncoded == false) {
			this.password = FamUserPassEncoderControl.getInstance().encodePassword(this);
			this.passwordEncoded = true;
		}
	}

	/**
	 * return the full name containing title, first name and sir name
	 * 
	 * @return the full name containing title, first name and sir name
	 */
	public String getFullName() {
		String result = this.getFname() + " " + this.getSname();
		if (this.getTitle() != null) {
			result = this.getTitle() + " " + result;
		}
		return result.trim();
	}

	/**
	 * set this user with standard configuration. this is:
	 * <ul>
	 * <li>standard language is english</li>
	 * <li>standard is user is neither excluded nor not excluded from system
	 * (means no decision so far)</li>
	 * <li>registration of the user is yet</li>1
	 * <li>role is the standard role</li>
	 * </ul>
	 * 
	 * @see RoleConfigDao#getRoleId(de.knurt.fam.core.model.persist.User)
	 */
	public void setStandardUser() {
		this.excluded = null;
		this.usedPlattformLang = new Locale("EN");
		this.registration = new Date();
		this.setRoleId(RoleConfigDao.getInstance().getStandardId());
	}

	/**
	 * set the locale of the user with given language.
	 * 
	 * @see Locale#getLanguage()
	 * @param lang
	 *            language as lowercase ISO 639 code.
	 */
	public void setUsedPlattformLangAsString(String lang) {
		this.usedPlattformLang = new Locale(lang);
	}

	/**
	 * set the address with the given id as main address. this assumes, that an
	 * address with the given id is stored in the database. if the given id is
	 * not present, set <code>null</code> as main address.
	 * 
	 * @see UserDao#getAddress(java.lang.Integer)
	 * @param id
	 *            representing the main address and be stored in the database.
	 */
	public void setMainAddressWithId(Integer id) {
		this.mainAddress = FamDaoProxy.userDao().getAddress(id);
	}

	/**
	 * set the next unique username as the standard username
	 * 
	 * @see UserDao#getUniqueUsername(de.knurt.fam.core.model.persist.User)
	 */
	public void setUniqueUsernameForInsertion() {
		this.username = FamDaoProxy.getInstance().getUserDao().getUniqueUsername(this);
	}

	/**
	 * return true, if user is male
	 * 
	 * @return the male
	 */
	public Boolean isMale() {
		return male;
	}

	/**
	 * return true, if user is male. false, if user is female. null if sex is
	 * unknown.
	 * 
	 * @return sex of the user
	 */
	public Boolean getMale() {
		return male;
	}

	/**
	 * set true for male, false for female, null for unkonwn.
	 * 
	 * @param male
	 *            true for male, false for female, null for unkonwn.
	 */
	public void setMale(Boolean male) {
		this.male = male;
	}

	/**
	 * get password of the user. the password might be uncrypted or crypted.
	 * 
	 * @return the uncrypted or crypted.password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * set the password leaving given password unchanged
	 * 
	 * @param password
	 *            to set, must be encrypted
	 */
	public void setPassword(String password) {
		this.password = password == null ? null : password.trim();
	}

	/**
	 * set a clean password. set passwordEncoded to false and password to the
	 * given password.
	 * 
	 * @param cleanPassword
	 */
	public void setCleanPassword(String cleanPassword) {
		this.passwordEncoded = false;
		this.password = cleanPassword;
	}

	/**
	 * date of birth of the user
	 * 
	 * @return the birthdate
	 */
	public Date getBirthdate() {
		return birthdate;
	}

	/**
	 * return birthdate as value yyyy-mm-dd
	 * 
	 * @return birthdate as value yyyy-mm-dd
	 */
	public String getBirthdateFormValue() {
		if (this.getBirthdate() == null) {
			return "";
		} else {
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			df.setLenient(false);
			return df.format(this.getBirthdate());
		}
	}

	/**
	 * set date of birth of the user. person must be older then 0 and younger
	 * then 200 years. otherwise, birthdate is set to <code>null</code>.
	 * 
	 * @see ContactDetailsRequestHandler#correctBirthdate(Date)
	 * @param birthdate
	 *            the date of birth of the user
	 */
	public void setBirthdate(Date birthdate) {
		this.birthdate = ContactDetailsRequestHandler.correctBirthdate(birthdate);
	}

	/**
	 * return the first name.
	 * 
	 * @return the first name
	 */
	public String getFname() {
		return fname;
	}

	/**
	 * set the first name
	 * 
	 * @param fname
	 *            the first name
	 */
	public void setFname(String fname) {
		this.fname = fname == null ? null : fname.trim();
	}

	/**
	 * email address
	 * 
	 * @return the email address
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * set email address
	 * 
	 * @param mail
	 *            the email address to set
	 */
	public void setMail(String mail) {
		this.mail = mail != null ? mail.toLowerCase() : null;
	}

	/**
	 * return second name.
	 * 
	 * @return the second name
	 */
	public String getSname() {
		return sname;
	}

	/**
	 * set second name
	 * 
	 * @param sname
	 *            the second name to set
	 */
	public void setSname(String sname) {
		this.sname = sname == null ? null : sname.trim();
	}

	/**
	 * return main address
	 * 
	 * @return the main address
	 */
	public Address getMainAddress() {
		return mainAddress;
	}

	/**
	 * set main address
	 * 
	 * @param mainAddress
	 *            the main address to set
	 */
	public void setMainAddress(Address mainAddress) {
		this.mainAddress = mainAddress;
	}

	/**
	 * return all addresses of the user. by now, there is no chance to put in
	 * more then one address in the application. this is only for compatibility
	 * reason, if this is needed in future versions.
	 * 
	 * @return all addresses of the user.
	 */
	public List<Address> getAddresses() {
		return addresses;
	}

	/**
	 * set all addresses of the user. by now, there is no chance to put in more
	 * then one address in the application. this is only for compatibility
	 * reason, if this is needed in future versions.
	 * 
	 * @param addresses
	 *            of the user.
	 */
	public void setAddresses(List<Address> addresses) {
		this.setAddresses(addresses);
	}

	/**
	 * return a phone number
	 * 
	 * @return the phone number
	 */
	public String getPhone1() {
		return phone1;
	}

	/**
	 * set a phone number
	 * 
	 * @param phone1
	 *            a phone number
	 */
	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	/**
	 * title of user
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * set title of user
	 * 
	 * @param title
	 *            to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * return true, if user is banned from the system. false, if user is not
	 * banned and <code>null</code>, if there is no decision by now (user
	 * account is not active).
	 * 
	 * @return true, if user is banned from the system. false, if user is not
	 *         banned and <code>null</code>, if there is no decision by now
	 *         (user account is not active).
	 */
	public Boolean isExcluded() {
		return excluded;
	}

	/**
	 * set true, if user is banned from the system. false, if user is not banned
	 * and <code>null</code>, if there is no decision by now (user account is
	 * not active).
	 * 
	 * @param excluded
	 *            set true, if user is banned from the system. false, if user is
	 *            not banned and <code>null</code>, if there is no decision by
	 *            now (user account is not active).
	 */
	public void setExcluded(Boolean excluded) {
		this.excluded = excluded;
	}

	/**
	 * return true, if user accepted the statement of agreement. otherwise
	 * false.
	 * 
	 * @return true, if user accepted the statement of agreement. otherwise
	 *         false.
	 */
	public Boolean isAcceptedStatementOfAgreement() {
		return statementOfAgreementAccepted;
	}

	/**
	 * set true, if user accepted the statement of agreement. otherwise false.
	 * 
	 * @param acceptedStatementOfAgreement
	 *            true, if user accepted the statement of agreement. otherwise
	 *            false.
	 */
	public void setAcceptedStatementOfAgreement(Boolean acceptedStatementOfAgreement) {
		this.statementOfAgreementAccepted = acceptedStatementOfAgreement;
	}

	/**
	 * return company name
	 * 
	 * @return the company name
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * set company name
	 * 
	 * @param company
	 *            the company name to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * return a second phone number (mobile)
	 * 
	 * @return a second phone number (mobile)
	 */
	public String getPhone2() {
		return phone2;
	}

	/**
	 * set a second phone number (mobile)
	 * 
	 * @param phone2
	 *            a second phone number (mobile)
	 */
	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	/**
	 * return date, user registered.
	 * 
	 * @return date, user registered.
	 */
	public Date getRegistration() {
		return registration;
	}

	/**
	 * set date, user registered.
	 * 
	 * @param registration
	 *            date, user registered.
	 */
	public void setRegistration(Date registration) {
		this.registration = registration;
	}

	/**
	 * return date of the last login
	 * 
	 * @return the lastLogin date of last login
	 */
	public Date getLastLogin() {
		return lastLogin;
	}

	/**
	 * set date of last login
	 * 
	 * @param lastLogin
	 *            the date of last login to set
	 */
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * return used plattform language and locale.
	 * 
	 * @return the used plattform language and locale.
	 * @see FamRequestContainer#locale()
	 */
	public Locale getUsedPlattformLang() {
		return usedPlattformLang;
	}

	/**
	 * return the username of the user
	 * 
	 * @return the username of the user
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * set username
	 * 
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * return true, if the user is auth. the user is auth, if at least one of
	 * the authentifications authentificates the user. calling this, the user
	 * must have an none encoded password. you can call this method only one
	 * time for each object though.
	 * 
	 * @return true, if this user is auth
	 */
	@Override
  public boolean isAuth() {
		if (this.isPasswordEncoded()) {
			throw new AssertionError("password must be clean");
		}
		String mayCleanPass = this.password;
		this.encodePassword();
		return FamAuth.isAuth(this, mayCleanPass);
	}

	/**
	 * return true, if the user has the right <code>forwhat</code>
	 * 
	 * @param onFacility
	 *            the facility to check this right for
	 * @see de.knurt.fam.core.aspects.security.auth.FamAuth
	 * @param forwhat
	 *            one of the class constants in <code>FamAuth</code>
	 * @return true, if the user has the right <code>forwhat</code>
	 */
	public Boolean hasRight(int forwhat, Facility onFacility) {
		return FamAuth.hasRight(this, forwhat, onFacility);
	}

	/**
	 * return true, if the password is already encoded.
	 * 
	 * @return the passwordEncoded, true, if the password is already encoded.
	 */
	public Boolean isPasswordEncoded() {
		return passwordEncoded;
	}

	/**
	 * set true, if the password is already encoded.
	 * 
	 * @param passwordEncoded
	 *            set true, if the password is already encoded.
	 */
	public void setPasswordEncoded(Boolean passwordEncoded) {
		this.passwordEncoded = passwordEncoded;
	}

	@Override
	/** {@inheritDoc} */
	public boolean insert() throws DataIntegrityViolationException {
		return FamDaoProxy.getInstance().getUserDao().insert(this);
	}

	@Override
	/** {@inheritDoc} */
	public boolean update() throws DataIntegrityViolationException {
		return FamDaoProxy.getInstance().getUserDao().update(this);
	}

	/**
	 * return the id. this is the primary key in relational databases.
	 * 
	 * @return the userId id to return
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * set id for this user. this is the primary key in relational databases.
	 * 
	 * @param userId
	 *            the id of user to set.
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * return the lowercase ISO 639 code of the plattform language used by the
	 * user.
	 * 
	 * @return the lowercase ISO 639 code of the plattform language used by the
	 *         user.
	 */
	public String getUsedPlattformLangAsString() {
		return this.getUsedPlattformLang() == null ? null : this.getUsedPlattformLang().toString();
	}

	@Override
	public String toString() {
		return super.toString() + "[" + this.username + "]";
	}

	/**
	 * short for <code>setExcluded(true)</code>.
	 * 
	 * @see #setExcluded(java.lang.Boolean)
	 */
	public void exclude() {
		this.setExcluded(true);
	}

	/**
	 * delete the user.
	 * 
	 * @see UserDao#delete(de.knurt.fam.core.model.persist.Storeable)
	 */
	public boolean delete() {
		return FamDaoProxy.userDao().delete(this);
	}

	/**
	 * return true, if a first phone number is set.
	 * 
	 * @return true, if a first phone number is set.
	 */
	public Boolean hasPhone1() {
		return this.getPhone1() != null && this.getPhone1().trim().isEmpty() == false;
	}

	/**
	 * return true, if a second phone number is set.
	 * 
	 * @return true, if a second phone number is set.
	 */
	public Boolean hasPhone2() {
		return this.getPhone2() != null && this.getPhone2().trim().isEmpty() == false;
	}

	/**
	 * return true, if the user is an administrator or is responsible for at
	 * least one facility.
	 * 
	 * @see #isAdmin()
	 * @see #hasResponsibilities4Facilities()
	 * @see #hasResponsibility4Facility(de.knurt.fam.core.model.config.Facility)
	 * @return true, if the user is an administrator or is responsible for at
	 *         least one facility.
	 */
	public boolean hasAdminTasks() {
		boolean result = this.isAdmin() || this.hasResponsibilities4Facilities();
		if (!result) {
			// TODO #15 connect with all admin pages
			result = this.hasRight2ViewPage("statistics");
		}
		return result;
	}

	/**
	 * return the keys representing the facilities the user is responsible for.
	 * 
	 * @see Facility#getKey()
	 * @return the keys representing the facilities the user is responsible for.
	 */
	public List<String> getFacilityKeysUserIsResponsibleFor() {
		return FamDaoProxy.facilityDao().getFacilityKeysUserIsResponsibleFor(this);
	}

	/**
	 * return true, if the user is responsible for at least one facility.
	 * 
	 * @see #getFacilityKeysUserIsResponsibleFor()
	 * @return true, if the user is responsible for at least one facility.
	 */
	public Boolean hasResponsibilities4Facilities() {
		return this.getFacilityKeysUserIsResponsibleFor().size() > 0;
	}

	/**
	 * return true, if the user has the right to view the page with the given
	 * viewname.
	 * 
	 * @see ViewPageAuthentication#hasIt(de.knurt.fam.core.model.persist.User,
	 *      java.lang.String)
	 * @param viewName
	 *            to check
	 * @return true, if the user has the right to view the page with the given
	 *         viewname.
	 */
	public Boolean hasRight2ViewPage(String viewName) {
		return ViewPageAuthentication.hasIt(this, viewName);
	}

	/**
	 * return the number of direct booking credits for a specific facility the
	 * user has. this is for future versions of the system and can be used to
	 * get credits for canceled bookings of the user.
	 * 
	 * @return the number of direct booking credits for a specific facility the
	 *         user has.
	 */
	public Map<String, Integer> getDirectBookingCredits() {
		return directBookingCredits;
	}

	/**
	 * set the number of direct booking credits for a specific facility the user
	 * has. this is for future versions of the system and can be used to get
	 * credits for canceled bookings of the user.
	 * 
	 * @param directBookingCredits
	 *            the number of direct booking credits for a specific facility
	 *            the user has.
	 */
	public void setDirectBookingCredits(Map<String, Integer> directBookingCredits) {
		this.directBookingCredits = directBookingCredits;
	}

	@Override
  public Integer getId() {
		return this.getUserId();
	}

	/**
	 * return true, if user is not excluded and has a varified account.
	 * 
	 * @return true, if user is not excluded and has a varified account.
	 */
	public boolean hasVarifiedActiveAccount() {
		return this.hasVarifiedAccount() && !this.isExcluded() && !this.isAccountExpired();
	}

	/**
	 * return true, if user has a varified account. a verified acount is an
	 * account, the user has applied for but the account has not been verified.
	 * 
	 * or in other words: return <code>true</code>, if the database column
	 * <code>excluded</code> is not <code>null</code>
	 * 
	 * @return true, if user is has a varified account.
	 */
	public boolean hasVarifiedAccount() {
		return this.isExcluded() != null;
	}

	/**
	 * return true, if user has a responsibility on the given facility.
	 * 
	 * @param facility
	 *            to check
	 * @return true, if user has a responsibility on the given facility.
	 */
	public boolean hasResponsibility4Facility(Facility facility) {
		return FamDaoProxy.facilityDao().hasResponsibilityForFacility(this, facility);
	}

	/**
	 * return the initialing (first char of first and second name with ".").
	 * 
	 * @return the initialing (first char of first and second name with ".").
	 */
	public String getInitialing() {
		Character firstChar = !this.fname.isEmpty() && this.fname.toCharArray().length > 0 ? this.fname.toCharArray()[0] : this.username.toCharArray()[0];
		Character secondChar = !this.sname.isEmpty() && this.sname.toCharArray().length > 0 ? this.sname.toCharArray()[0] : this.username.toCharArray()[2];
		return firstChar.toString().toUpperCase() + "." + secondChar.toString().toUpperCase() + ".";
	}

	/**
	 * return the contact details of the user.
	 * 
	 * @return the contact details of the user.
	 */
	public List<ContactDetail> getContactDetails() {
		ContactDetail example = new ContactDetail();
		example.setUsername(this.username);
		try {
			return FamDaoProxy.userDao().getAllLike(example);
		} catch (CannotGetJdbcConnectionException e) {
			return null;
		}
	}

	public void setBirthdate(String birthdate) {
		this.setBirthdate(ContactDetailsRequestHandler.getDate(birthdate));
	}

	public void setBirthdateNull() {
		this.birthdate = null;

	}

	/**
	 * delete all old contact details and replace it with new contact details
	 * given. orm action - replacing taking place in database. the username of
	 * the given contact details are set to this user (overriding any other user
	 * set).
	 * 
	 * @param newContactDetails
	 *            to set in the db
	 */
	public void updateContactDetails(List<ContactDetail> newContactDetails) {
		FamDaoProxy.userDao().delete(this.getContactDetails());
		for (ContactDetail newContactDetail : newContactDetails) {
			newContactDetail.setUsername(this.getUsername());
			FamDaoProxy.userDao().insert(newContactDetail);
		}
	}

	/**
	 * set the date, the account of this user expires.
	 */
	public void setAccountExpires(Date accountExpires) {
		this.accountExpires = accountExpires;
	}

	/**
	 * return the date, the account of this user expires.
	 * 
	 * @return the date, the account of this user expires.
	 */
	public Date getAccountExpires() {
		return accountExpires;
	}

	/**
	 * return true, if the account's expiration of the validity is not given
	 * anymore. the account expires, if the value <code>accountExpires</code> is
	 * <strong>not</strong> <code>null</code> and before now. if the value
	 * <code>accountExpires</code> is null, the account never expires.
	 * 
	 * @see #getAccountExpires()
	 * @return true, if the account's expiration of the validity is not given
	 *         anymore.
	 */
	public boolean isAccountExpired() {
		Date exp = this.getAccountExpires();
		Date now = new Date();
		return exp != null && (exp.equals(now) || exp.before(now));
	}

	public void setAccountExpires(String accountExpires) {
		this.setAccountExpires(ContactDetailsRequestHandler.getDate(accountExpires));

	}

	public String getAccountExpiresFormValue() {
		if (this.getAccountExpires() == null) {
			return "";
		} else {
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			df.setLenient(false);
			return df.format(this.getAccountExpires());
		}
	}

	public String getAccountExpiresFormatted() {
		return this.getAccountExpiresFormValue();
	}

	public void setAccountNeverExpires() {
		this.accountExpires = null;
	}

	/**
	 * shortcut for setting the value of main address. if user has no main
	 * address yet, create new empty one.
	 * 
	 * @see #getMainAddress()
	 */
	public void setZipcode(String zipcode) {
		if (this.mainAddress == null) {
			this.mainAddress = new Address();
		}
		this.mainAddress.setZipcode(zipcode);
	}

	/**
	 * shortcut for setting the value of main address if user has no main
	 * address yet, create new empty one.
	 * 
	 * @see #getMainAddress()
	 */
	public void setStreet(String street) {
		if (this.mainAddress == null) {
			this.mainAddress = new Address();
		}
		this.mainAddress.setStreet(street);
	}

	/**
	 * shortcut for setting the value of main address if user has no main
	 * address yet, create new empty one.
	 * 
	 * @see #getMainAddress()
	 */
	public void setStreetno(String streetno) {
		if (this.mainAddress == null) {
			this.mainAddress = new Address();
		}
		this.mainAddress.setStreetno(streetno);
	}

	/**
	 * shortcut for setting the value of main address if user has no main
	 * address yet, create new empty one.
	 * 
	 * @see #getMainAddress()
	 */
	public void setCity(String city) {
		if (this.mainAddress == null) {
			this.mainAddress = new Address();
		}
		this.mainAddress.setCity(city);
	}

	/**
	 * shortcut for setting the value of main address if user has no main
	 * address yet, create new empty one.
	 * 
	 * @see #getMainAddress()
	 */
	public void setCountry(String country) {
		if (this.mainAddress == null) {
			this.mainAddress = new Address();
		}
		this.mainAddress.setCountry(country);
	}

	/**
	 * return the value of the main address. return <code>null</code>, if main
	 * address is <code>null</code>.
	 * 
	 * @see #getMainAddress()
	 * @return the value of the main address.
	 */
	public String getZipcode() {
		return this.mainAddress == null ? null : this.mainAddress.getZipcode();
	}

	/**
	 * return the value of the main address. return <code>null</code>, if main
	 * address is <code>null</code>.
	 * 
	 * @see #getMainAddress()
	 * @return the value of the main address.
	 */
	public String getStreet() {
		return this.mainAddress == null ? null : this.mainAddress.getStreet();
	}

	/**
	 * return the value of the main address. return <code>null</code>, if main
	 * address is <code>null</code>.
	 * 
	 * @see #getMainAddress()
	 * @return the value of the main address.
	 */
	public String getStreetno() {
		return this.mainAddress == null ? null : this.mainAddress.getStreetno();
	}

	/**
	 * return the value of the main address. return <code>null</code>, if main
	 * address is <code>null</code>.
	 * 
	 * @see #getMainAddress()
	 * @return the value of the main address.
	 */
	public String getCity() {
		return this.mainAddress == null ? null : this.mainAddress.getCity();
	}

	/**
	 * return the value of the main address. return <code>null</code>, if main
	 * address is <code>null</code>.
	 * 
	 * @see #getMainAddress()
	 * @return the value of the main address.
	 */
	public String getCountry() {
		return this.mainAddress == null ? null : this.mainAddress.getCountry();
	}

	/**
	 * return a String of the street with street number.
	 * 
	 * @see Address#getStreetWithStreetno()
	 * @return a String of the street with street number
	 */
	public String getStreetWithStreetno() {
		return this.mainAddress == null ? null : this.mainAddress.getStreetWithStreetno();
	}

	/**
	 * alias for {@link #getPhone1()}
	 */
	public String getPhoneHome() {
		return this.getPhone1();
	}

	/**
	 * alias for {@link #getPhone2()}
	 */
	public String getPhoneMobile() {
		return this.getPhone2();
	}

	public void setStreetWithStreetno(String streetWithStreetno) {
		if (this.mainAddress == null) {
			this.mainAddress = new Address();
		}
		this.mainAddress.setStreetWithStreetno(streetWithStreetno);
	}

	/**
	 * @see #setPhone1(String)
	 */
	public void setPhoneHome(String phoneHome) {
		this.setPhone1(phoneHome);
	}

	/**
	 * @see #setPhone2(String)
	 */
	public void setPhoneMobile(String mobile) {
		this.setPhone2(mobile);
	}

	public boolean isAllowedToAccess(Facility facility) {
		return this.hasRight(7, facility);
	}

	public List<Facility> getFacilitiesUserIsResponsibleFor() {
		return FamDaoProxy.facilityDao().getFacilitiesUserIsResponsibleFor(this);
	}

	/**
	 * return true if this is the user given.
	 * 
	 * @param user
	 *            to check
	 * @return true if this is the user given.
	 */
	public boolean is(User user) {
		return this.is(user.getUsername());
	}

	/**
	 * {@link #setAnonym(boolean)} to result of
	 * {@link UserDao#anonymize(User, User)}
	 * 
	 * @param auth
	 *            calling this
	 * @return true if anonymization succeeded
	 */
	public synchronized boolean anonymize(User auth) {
		this.anonym = FamDaoProxy.userDao().anonymize(this, auth);
		return this.anonym;
	}

	public boolean isAnonym() {
		return anonym;
	}

	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}

	/**
	 * return true if this is the user with the given username.
	 * 
	 * @param username
	 *            to check
	 * @return true if this is the user with the given username.
	 */
	public boolean is(String username) {
		return this.getUsername().equals(username);
	}
}
