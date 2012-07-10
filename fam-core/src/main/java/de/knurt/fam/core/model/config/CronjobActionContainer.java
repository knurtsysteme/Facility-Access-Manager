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
package de.knurt.fam.core.model.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import de.knurt.fam.core.aspects.logging.FamLog;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;

/**
 * delegate a {@link CronjobAction} to its {@link CronjobActionResolver}.
 * cronjob actions are defined in <code>cronjobs.xml</code>. there are cronjob actions
 * with ids 1, 2 and 3 that must be there. you can add more actions over plugins.
 * 
 * @author Daniel Oltmanns
 * @since 0.20090913
 */
public class CronjobActionContainer {
	/** one and only instance of CronjobActionContainer */
	private volatile static CronjobActionContainer me;

	/** construct CronjobActionContainer */
	private CronjobActionContainer() {
	}

	/**
	 * return the one and only instance of CronjobActionContainer
	 * 
	 * @return the one and only instance of CronjobActionContainer
	 */
	public static CronjobActionContainer getInstance() {
		if (me == null) {
			// ↖ no instance so far
			synchronized (CronjobActionContainer.class) {
				if (me == null) {
					// ↖ still no instance so far
					// ↓ the one and only me
					me = new CronjobActionContainer();
				}
			}
		}
		return me;
	}

	/**
	 * short for {@link #getInstance()}
	 * 
	 * @return the one and only instance of CronjobActionContainer
	 */
	public static CronjobActionContainer me() {
		return getInstance();
	}

	/**
	 * if it is time for the given cronjob, check the id and delegate it to a
	 * specific resolver specified on this action of the cronjob.
	 * 
	 * @param ca
	 *            action to resolve.
	 */
	public void resolveAll() {
		List<Integer> cronjobsResolvedTimes = new ArrayList<Integer>();
		for (CronjobAction cronjob : this.cronjobActions) {
			if (lastExecutionOlderThen(cronjob.resolveEvery())) {
				cronjob.resolve();
				if (!cronjobsResolvedTimes.contains(cronjob.resolveEvery())) {
					cronjobsResolvedTimes.add(cronjob.resolveEvery());
				}
			}
		}
		for (int cronjobsResolvedTime : cronjobsResolvedTimes) {
			FamDaoProxy.keyValueDao().put(getKeyCronjobLastExec(cronjobsResolvedTime), SDF.format(new Date()));
		}
	}

	/**
	 * return the date of the last execution for the given time in minutes
	 * 
	 * @param time
	 *            in minutes the execution is made frequently
	 * @return the date of the last execution for the given time in minutes
	 */
	public static Date getLastExecution(int time) {
		Date result = null;
		String cronjob_last_execution = FamDaoProxy.keyValueDao().value(getKeyCronjobLastExec(time));
		if (cronjob_last_execution != null) {
			// assert format e.g. 2011-05-11 11:24:44
			try {
				result = SDF.parse(cronjob_last_execution);
			} catch (ParseException e) {
				FamLog.exception(cronjob_last_execution, e, 201201111237l);
			}
		}
		return result;
	}

	/**
	 * return true, if last execution of cronjob is older then given time in
	 * minutes. otherwise false.
	 * 
	 * @param time
	 *            in minutes
	 * @return true, if last execution of cronjob is older then given time in
	 *         minutes.
	 */
	public static boolean lastExecutionOlderThen(int time) {
		Date le = getLastExecution(time);
		if (le == null) {
			return true;
		} else {
			Calendar lec = Calendar.getInstance();
			lec.add(Calendar.MINUTE, -time + 1); // +1 because do not fail because of milis!
			return le.before(lec.getTime());
		}
	}

	private static final String getKeyCronjobLastExec(int time) {
		return "cronjob_last_execution_" + time;
	};

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * register a new resolver here.
	 * 
	 * @param id
	 * @param cronjobActionResolver
	 */
	public void register(CronjobAction cronjobAction) {
		cronjobActions.add(cronjobAction);
	}

	/**
	 * unregister the resolver with given id
	 * 
	 * @param id
	 *            of resolver to unregister
	 */
	public void unregister(CronjobAction cronjobAction) {
		cronjobActions.remove(cronjobAction);
	}

	private List<CronjobAction> cronjobActions = new ArrayList<CronjobAction>();

	@Required
	public void setCronjobActions(List<CronjobAction> cronjobActions) {
		this.cronjobActions = cronjobActions;
	}

	public List<CronjobAction> getAllCronjobActions() {
		return this.cronjobActions;
	}
}
