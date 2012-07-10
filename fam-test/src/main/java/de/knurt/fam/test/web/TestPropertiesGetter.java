package de.knurt.fam.test.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import de.knurt.fam.connector.FamConnector;

public class TestPropertiesGetter {
	/** one and only instance of TestPropertiesGetter */
	private volatile static TestPropertiesGetter me;
	private Properties testProperties = new Properties();

	/** construct TestPropertiesGetter */
	private TestPropertiesGetter() {
		try {
			PropertyResourceBundle tmps = new PropertyResourceBundle(new FileInputStream(System.getProperty("catalina.home") + "/webapps/fam-test/WEB-INF/classes/test.properties"));
			for(String key : tmps.keySet()) {
				testProperties.put(key, tmps.getString(key));
			}
			testProperties.put("db.url", FamConnector.getGlobalProperty("sql_url"));
			testProperties.put("db.username", FamConnector.getGlobalProperty("sql_username"));
			testProperties.put("db.password", FamConnector.getGlobalProperty("sql_password"));
			testProperties.put("molybdenum.fam-test-url", FamConnector.baseUrlPublic() + "../fam-test");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * return the one and only instance of TestPropertiesGetter
	 * 
	 * @return the one and only instance of TestPropertiesGetter
	 */
	public static TestPropertiesGetter getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (TestPropertiesGetter.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new TestPropertiesGetter();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of TestPropertiesGetter
	 */
	public static TestPropertiesGetter me() {
		return getInstance();
	}


	public Properties getTestProperties() {
		return testProperties;
	}
}
