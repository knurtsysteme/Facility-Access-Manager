package de.knurt.fam.test.web;

import de.knurt.fam.template.util.TemplateHtml;

public class MolybdenumAction {
	/** one and only instance of MolybdenumAction */
	private volatile static MolybdenumAction me;

	/** construct MolybdenumAction */
	private MolybdenumAction() {
	}

	/**
	 * return the one and only instance of MolybdenumAction
	 * 
	 * @return the one and only instance of MolybdenumAction
	 */
	public static MolybdenumAction getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (MolybdenumAction.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new MolybdenumAction();
				}
			}
		}
		return me;
	}

	public String reset() {
		String result = "";
		result += String.format(Molybdenum.command2format, "open", TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum.fam-test-url") + "/prepare_molybdenum.jsp", "", "reset databases: open url");
		result += String.format(Molybdenum.command2format, "clickAndWait", "id=reset", "", "reset databases: do it");
		return result;
	}
	
	public String ticket_262() {
		String result = "";
		result += String.format(Molybdenum.command2format, "open", TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum.fam-test-url") + "/prepare_molybdenum.jsp", "", "change expiration date");
		result += String.format(Molybdenum.command2format, "clickAndWait", "id=ticket_262", "", "do it");
		return result;
	}
	public String ticket_340() {
		String result = "";
		result += String.format(Molybdenum.command2format, "open", TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum.fam-test-url") + "/prepare_molybdenum.jsp", "", "create test files");
		result += String.format(Molybdenum.command2format, "clickAndWait", "id=ticket_340", "", "do it");
		return result;
	}
	public String setABookingSessionIsNow() {
		String result = "";
		result += String.format(Molybdenum.command2format, "open", TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum.fam-test-url") + "/prepare_molybdenum.jsp", "", "create test files");
		result += String.format(Molybdenum.command2format, "clickAndWait", "id=setABookingSessionIsNow", "", "do it");
		return result;
	}

	public String logout() {
		return this.open("logout");
	}

	public String getTestProp(String key) {
		return TestPropertiesGetter.me().getTestProperties().getProperty(key);
	}

	public String open(String resourceName) {
		return String.format(Molybdenum.command2format, "open", TemplateHtml.me().getHref(resourceName), "", "open resource " + resourceName);
	}

	public String open(String resourceName, String queryParams) {
		return String.format(Molybdenum.command2format, "open", TemplateHtml.me().getHref(resourceName) + queryParams, "", "open resource " + resourceName);
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of MolybdenumAction
	 */
	public static MolybdenumAction me() {
		return getInstance();
	}

	public String login(String role, String password) {
		String username = TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum." + role);
		String result = "";
		result += String.format(Molybdenum.command2format, "type", "id=username", username, "type in admin username");
		result += String.format(Molybdenum.command2format, "type", "id=password", password, "type in admin password");
		result += String.format(Molybdenum.command2format, "clickAndWait", "css=button[type=submit]", "", "send login form");
		return result;
	}

	public String login(String role) {
		return this.login(role, TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum.password"));
	}
}