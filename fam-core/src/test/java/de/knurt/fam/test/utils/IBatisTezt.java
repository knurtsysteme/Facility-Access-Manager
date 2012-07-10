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
package de.knurt.fam.test.utils;

import java.util.Properties;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.junit.Before;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

/**
 * @author Daniel Oltmanns
 * @since 0.20090829
 * @version 0.20100207
 */
public abstract class IBatisTezt implements DatabaseTezt {

    public abstract String[] getTablesToDeleteAfterTest();

    public abstract Properties getTestConnection();

    public IBatisTezt() {
    }

    public abstract SqlMapClientDaoSupport getSqlMapClientDaoSupport();

    @Before
    @Override
    public synchronized void clearDatabase() {
        for (String table2delete : this.getTablesToDeleteAfterTest()) {
            SimpleJdbcTemplateSingleton.getSimpleJdbcTemplate(this.getSqlMapClientDaoSupport(), this.getTestConnection()).update("DELETE FROM " + table2delete);
        }
    }
}
class SimpleJdbcTemplateSingleton {

    private volatile static SimpleJdbcTemplate jdbcTemplate = null;

    /** construct me */
    private SimpleJdbcTemplateSingleton() {
    }

    /**
     * return the one and only instance of RolePool
     * @return the one and only instance of RolePool
     */
    public static SimpleJdbcTemplate getSimpleJdbcTemplate(SqlMapClientDaoSupport sqlMcds, Properties testConnection) {
        if (jdbcTemplate == null) { // no instance so far
            synchronized (SimpleJdbcTemplate.class) {
                if (jdbcTemplate == null) { // still no instance so far
                    try {
                        sqlMcds.setDataSource(BasicDataSourceFactory.createDataSource(testConnection));
                        jdbcTemplate = new SimpleJdbcTemplate(sqlMcds.getDataSource());
                    } catch (Exception ex) {
                        System.err.println("####################################" + ex);
                        System.exit(0);
                    }
                }
            }
        }
        return jdbcTemplate;
    }
}