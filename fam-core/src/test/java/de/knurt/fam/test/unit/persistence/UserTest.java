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
package de.knurt.fam.test.unit.persistence;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.knurt.fam.core.model.persist.User;
import de.knurt.fam.core.util.UserFactory;
/**
 *
 * @author Daniel Oltmanns <info@knurt.de>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/test-dependencies.xml" })
public class UserTest {

    /**
     *
     */
    public UserTest() {
    }


    @Test
    public void getBirthdateFormValue() {
        User user = UserFactory.me().blank();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 11);
        c.set(Calendar.MONTH, 3);
        c.set(Calendar.YEAR, 1999);
        user.setBirthdate(c.getTime());
        assertEquals("11.04.1999", user.getBirthdateFormValue());
    }


}