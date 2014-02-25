package de.knurt.fam.test.web;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.jcouchdb.db.Database;
import org.jcouchdb.db.Response;

import de.knurt.fam.connector.FamConnector;
import de.knurt.fam.core.view.text.FamDateFormat;

public class PrepareDatabase {
  private String url = null;
  private String username = null;
  private String password = null;
  private String test_couchdb_id1 = null;

  private Database couchdb;

  public String doAndGetFeedback(HttpServletRequest request) {
    String result = "";
    if (FamConnector.isDev()) {

      result = "<ul>";
      result += "<li>Base parameters:<ul>";
      result += String.format("<li>mysql url: '%s'</li>", this.url);
      result += String.format("<li>mysql username: '%s'</li>", this.username);
      result += String.format("<li>mysql password: '%s'</li>", this.password);
      result += String.format("<li>couchdb name: '%s'</li>", this.couchdb.getName());
      result += "</ul></li>";

      // get and check database connection
      Connection connection = null;
      Statement stmt = null;
      try {
        // Load the JDBC driver
        String driverName = "org.gjt.mm.mysql.Driver"; // MySQL MM
        // JDBC
        Class.forName(driverName);
        connection = DriverManager.getConnection(url, username, password);
        stmt = connection.createStatement();

        if (request.getParameter("confirm") != null && request.getParameter("confirm").equals("1")) {

          try {
            String customFields = "{\"hasRights\":\"0\",\"ceo_sname\":\"Val: a\",\"ceo_fname\":\"Val: b\",\"taskdesc\":\"Val: c \",\"partner\":\"Val: d\",\"ceo_title\":\"Val: e\"}";
            String[] sqls = { "DELETE FROM user", "DELETE FROM address", "DELETE FROM booking", "DELETE FROM facility_responsibility",

            "INSERT INTO `address` VALUES (1,1,'24989','Muehlenstr','3','Dollerup','de')", "INSERT INTO `address` VALUES (2,1,'24989','Muehlenstr','3','Dollerup','de')", "INSERT INTO `address` VALUES (3,1,'24989','Muehlenstr','3','Dollerup','de')", "INSERT INTO `address` VALUES (4,1,'24989','Muehlenstr','3','Dollerup','de')",

            "INSERT INTO `user` VALUES (null,'daoltman','2010-08-11 12:42:25',NULL,'1976-06-17',1,0,1,'en','937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244','123','123','KNURT Systeme','Daniel','Mr.','info@knurt.de','Oltmanns','admin',1,1,'unknown','Test Department',NULL,'intended research project',false,'" + customFields + "')", "INSERT INTO `user` VALUES (null,'daoltma1','2010-08-11 12:47:43',NULL,'1976-06-17',1,0,1,'en','937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244','123','123','KNURT Systeme','Daniel','Mr.','nobody01@knurt.de','Oltmanns','extern',2,1,'unknown','Test Department',NULL,'intended research project',false,'" + customFields + "')",
                "INSERT INTO `user` VALUES (null,'daoltma2','2010-08-11 12:48:48',NULL,'1976-06-17',1,0,1,'en','937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244','123','123','KNURT Systeme','Daniel','Mr.','nobody02@knurt.de','Oltmanns','operator',3,1,'unknown','Test Department',NULL,'intended research project',false,'" + customFields + "')", "INSERT INTO `user` VALUES (null,'daoltma3','2010-08-11 12:49:37',NULL,'1976-06-17',1,0,1,'en','937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244','123','123','KNURT Systeme','Daniel','Mr.','nobody03@knurt.de','Oltmanns','intern',4,1,'unknown','Test Department',NULL,'intended research project',false,'" + customFields + "')",

                "INSERT INTO facility_responsibility(username, facility_key) VALUES(\"daoltma2\", \"indoor\")", "INSERT INTO facility_responsibility(username, facility_key) VALUES(\"daoltma2\", \"sportsHall\")", "INSERT INTO facility_responsibility(username, facility_key) VALUES(\"daoltma2\", \"ballBath\")" };
            for (String sql : sqls) {
              result += "<li>exec: <code>" + sql + "</code></li>";
              stmt.execute(sql);
            }
            result += this.prepareCouchDB();
            result += "<li id=\"finished\">finished</li>";
          } catch (SQLException e) {
          }
        } else if (request.getParameter("confirm") != null && request.getParameter("confirm").equals("262")) {
          // ticket 262

          Calendar today = Calendar.getInstance();
          today.add(Calendar.DAY_OF_YEAR, -1);
          String accountExpiresNew = FamDateFormat.getCustomDate(today.getTime(), "yyyy-MM-dd");
          String sql = String.format("UPDATE `user` SET account_expires = '%s' WHERE username = 'daoltma1'", accountExpiresNew);
          result = String.format("<li>Set account expiration date of test user extern to %s</li>", accountExpiresNew);
          try {
            stmt.execute(sql);
          } catch (SQLException e) {
          }
        } else if (request.getParameter("confirm") != null && request.getParameter("confirm").equals("setABookingSessionIsNow")) {
          // set a booking for extern where the session is active now
          Calendar timeStartC = Calendar.getInstance();
          timeStartC.add(Calendar.DAY_OF_YEAR, -10);
          Calendar timeEndC = Calendar.getInstance();
          timeEndC.add(Calendar.DAY_OF_YEAR, 10);
          String timeStart = this.getTimestamp(timeStartC.getTime());
          String timeEnd = this.getTimestamp(timeEndC.getTime());
          String sqlTempl = "INSERT INTO booking (id, username, seton, status_id, status_seton, facilityKey, capacityUnits, time_end, time_start, cancelation_username, cancelation_reason, cancelation_seton, notice, idBookedInBookingStrategy, processed) 	VALUES (null, 'daoltma1', '%1$s', 2, '%1$s', 'bus1', 1, '%2$s', '%1$s', null, null, null, null, 1, 0)";
          String sql = String.format(sqlTempl, timeStart, timeEnd);
          result = String.format("<li>Inserted booking: %s</li>", sql);
          try {
            stmt.execute(sql);
          } catch (SQLException e) {
          }
        } else if (request.getParameter("confirm") != null && request.getParameter("confirm").equals("340")) {
          // ticket 340
          File directory = new File(FamConnector.fileExchangeDir() + File.separator + "users" + File.separator + TestPropertiesGetter.me().getTestProperties().getProperty("molybdenum.admin"));
          if (!directory.exists()) {
            directory.mkdir();
          }
          File[] dfs = directory.listFiles();
          for (File df : dfs) {
            df.delete();
          }
          for (String abc : new String[] { "a", "b", "c", "d", "e", "f" }) {
            for (String suffix : new String[] { "pdf", "xls", "odt", "doc", "jpg", "png" }) {
              File nf = new File(directory.getAbsolutePath() + File.separator + abc + "." + suffix);
              try {
                nf.createNewFile();
              } catch (IOException e) {
              }
            }
          }
        }
      } catch (ClassNotFoundException e) {
        result += "<li>Could not find the database driver</li>";
      } catch (SQLException e) {
        result += "<li>Could not connect to the database</li>";
      }
      result += "</ul>";
    } else {
      result = "YOU ARE NOT A DEV SYSTEM!";
    }
    return result;
  }

  private String getTimestamp(Date date) {
    return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", date);
  }

  private String prepareCouchDB() {
    String jsonstring = String.format("{\"type\": \"TEST_MOLYBDENUM\", \"created\": \"" + new Date().getTime() + "\"}");
    String uri = "/" + this.couchdb.getName() + "/" + this.test_couchdb_id1;
    Response existingDoc = this.couchdb.getServer().get(uri);
    if (existingDoc.isOk()) {
      return String.format("<li id=\"couchdb_id1\">%s</li>", existingDoc.getContentAsString());
    } else {
      Response answer = this.couchdb.getServer().put(uri, jsonstring);
      return String.format("<li id=\"couchdb_id1\">%s</li>", answer.getContentAsString());
    }
  }

  /** one and only instance of PrepareDatabase */
  private volatile static PrepareDatabase me;

  /** construct PrepareDatabase */
  private PrepareDatabase() {
    Properties p = TestPropertiesGetter.me().getTestProperties();
    url = p.getProperty("db.url");
    username = p.getProperty("db.username");
    password = p.getProperty("db.password");

    String host = FamConnector.getGlobalProperty("couchdb_ip");
    int port = Integer.parseInt(FamConnector.getGlobalProperty("couchdb_port"));
    String name = FamConnector.getGlobalProperty("couchdb_dbname");
    couchdb = new Database(host, port, name);

    test_couchdb_id1 = p.getProperty("test.couchdb.id1");
  }

  /**
   * return the one and only instance of PrepareDatabase
   * 
   * @return the one and only instance of PrepareDatabase
   */
  public static PrepareDatabase getInstance() {
    if (me == null) { // no instance so far
      synchronized (PrepareDatabase.class) {
        if (me == null) { // still no instance so far
          me = new PrepareDatabase(); // the one and only
        }
      }
    }
    return me;
  }
}
