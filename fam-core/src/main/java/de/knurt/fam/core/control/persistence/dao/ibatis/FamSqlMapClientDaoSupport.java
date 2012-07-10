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
package de.knurt.fam.core.control.persistence.dao.ibatis;

import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

/**
 * central dataholder for the {@link FamSqlMapClientDaoSupport}
 * @author Daniel Oltmanns
 * @since 0.20090828
 */
public class FamSqlMapClientDaoSupport extends SqlMapClientDaoSupport {

    /** one and only instance of me */
    private volatile static FamSqlMapClientDaoSupport me;

    /**
     * return the sql map.
     * this is the central point for getting access to the database via ibatis.
     * @return the sql map.
     */
    public static SqlMapClientTemplate sqlMap() {
        return getInstance().getSqlMapClientTemplate();
    }

    /** construct me */
    private FamSqlMapClientDaoSupport() {
    }

    /**
     * return the one and only instance of DasSqlMapClientDaoSupport
     * @return the one and only instance of DasSqlMapClientDaoSupport
     */
    public static FamSqlMapClientDaoSupport getInstance() {
        if (me == null) { // no instance so far
            synchronized (FamSqlMapClientDaoSupport.class) {
                if (me == null) { // still no instance so far
                    me = new FamSqlMapClientDaoSupport(); // the one and only
                }
            }
        }
        return me;
    }
}
