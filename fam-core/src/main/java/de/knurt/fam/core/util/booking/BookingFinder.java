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
package de.knurt.fam.core.util.booking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import de.knurt.fam.core.model.config.BookingRule;
import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * receive a request and return free booking possibilities equals to the request.
 * to find equal possibilities, search nearest units and reduce (or expand) only
 * one parameter of the request to get another request with positive answer.
 * @author Daniel Oltmanns
 * @since 0.20091002 (10/02/2009)
 */
public class BookingFinder {

    private static TimeBookingRequest getSameLowerTimeUnits(TimeBookingRequest tbr) {
        TimeBookingRequest result = null;
        if (tbr.getRequestedTimeUnits() > tbr.getBookingRule().getMinBookableTimeUnits(tbr.getUser())) {
            int units2try = tbr.getRequestedTimeUnits();
            int before = units2try;
            while (units2try > tbr.getBookingRule().getMinBookableTimeUnits(tbr.getUser())) {
                units2try--;
                tbr.setRequestedTimeUnits(units2try);
                if (tbr.isAvailable()) {
                    result = (TimeBookingRequest) tbr.clone();
                    break;
                }
            }
            tbr.setRequestedTimeUnits(before);
        }
        return result;
    }

    private static TimeBookingRequest getSameLowerCapacityUnits(TimeBookingRequest tbr) {
        TimeBookingRequest result = null;
        if (tbr.getRequestedCapacityUnits() > tbr.getBookingRule().getMinBookableCapacityUnits(tbr.getUser())) {
            int units2try = tbr.getRequestedCapacityUnits();
            int before = units2try;
            while (units2try > tbr.getBookingRule().getMinBookableCapacityUnits(tbr.getUser())) {
                units2try--;
                tbr.setRequestedCapacityUnits(units2try);
                if (tbr.isAvailable()) {
                    result = (TimeBookingRequest) tbr.clone();
                    break;
                }
            }
            tbr.setRequestedCapacityUnits(before);
        }
        return result;
    }

    /**
     * return a request containing the given parameters as it is perfectly.
     * "perfectly" means, it comes as near as the user want it and it is allowed to book.
     * e.g. if the user wants to book 3 units from 2:01am to 3:00am, but booking
     * must start at 2:00am and there are only 2 units left at this time, return
     * booking request with 2 units from 2:00am to 3am.
     * @param br rules to apply for the request
     * @param user requesting it
     * @param tf time frame user has requested
     * @param capacityUnitsInterestedIn units of capacity, user is interensted in
     * @return a request containing the given parameters as it is perfectly.
     */
    public static TimeBookingRequest getValidFrom(BookingRule br, User user, TimeFrame tf, Integer capacityUnitsInterestedIn) {
        int smallestMinutesBookable = br.getSmallestMinutesBookable();
        // set capacity units
        int capacityUnitsUsed = capacityUnitsInterestedIn == null ? br.getMinBookableCapacityUnits(user) : capacityUnitsInterestedIn.intValue();
        if (capacityUnitsUsed < br.getMinBookableCapacityUnits(user)) {
            capacityUnitsUsed = br.getMinBookableCapacityUnits(user);
        }
        if (capacityUnitsUsed > br.getMaxBookableCapacityUnits(user)) {
            capacityUnitsUsed = br.getMaxBookableCapacityUnits(user);
        }

        // get the nearest start and end time conform with the configuration of the facility
        Calendar startTimeInterestedIn = tf.getCalendarStart();
        startTimeInterestedIn.set(Calendar.SECOND, 0);
        startTimeInterestedIn.set(Calendar.MILLISECOND, 0);

        Calendar endTimeInterestedIn = tf.getCalendarEnd();
        endTimeInterestedIn.set(Calendar.SECOND, 0);
        endTimeInterestedIn.set(Calendar.MILLISECOND, 0);

        Calendar nearestStartTimePossible = (Calendar) startTimeInterestedIn.clone();
        nearestStartTimePossible.set(Calendar.SECOND, 0);
        nearestStartTimePossible.set(Calendar.MILLISECOND, 0);
        nearestStartTimePossible.set(Calendar.HOUR_OF_DAY, 0);
        nearestStartTimePossible.set(Calendar.MINUTE, 0);

        if (br.getMustStartAt() != null) {
            nearestStartTimePossible.set(Calendar.MINUTE, br.getMustStartAt().intValue());
        }
        boolean isAfterMustStartTime = false;
        while (startTimeInterestedIn.after(nearestStartTimePossible)) {
            nearestStartTimePossible.add(Calendar.MINUTE, TimeBookingRequest.getMinutesOfOneStep(br));
            isAfterMustStartTime = true;
        }

        // get one step back if it is allowed and nearer to what user wanted
        if (isAfterMustStartTime && !c1IsNearest2c2(startTimeInterestedIn, nearestStartTimePossible, br)) { // it is nearer
            nearestStartTimePossible.add(Calendar.MINUTE, -TimeBookingRequest.getMinutesOfOneStep(br));
        }

        Calendar endTime = (Calendar) nearestStartTimePossible.clone();
        int timeUnitsInterestedIn = 0;
        while (endTimeInterestedIn.after(endTime)) {
            endTime.add(Calendar.MINUTE, smallestMinutesBookable);
            timeUnitsInterestedIn++;
        }
        if (timeUnitsInterestedIn >= br.getMaxBookableTimeUnits(user)) {
            timeUnitsInterestedIn = br.getMaxBookableTimeUnits(user);
        } else if (timeUnitsInterestedIn <= br.getMinBookableTimeUnits(user)) {
            timeUnitsInterestedIn = br.getMinBookableTimeUnits(user);
        } else if (timeUnitsInterestedIn > 1 && !c1IsNearest2c2(endTimeInterestedIn, endTime, br)) { // one step back if it is a better choice
            timeUnitsInterestedIn--;
        }
        TimeBookingRequest result = new TimeBookingRequest(br, user, capacityUnitsUsed, timeUnitsInterestedIn, nearestStartTimePossible);
        return result;
    }

    private static TimeBookingRequest getSameStartingNextToForward(TimeBookingRequest tbr) {
        TimeBookingRequest result = null;
        int minutesOfOneStep = TimeBookingRequest.getMinutesOfOneStep(tbr.getBookingRule());
        int trials = 24 * 7 * 60 / minutesOfOneStep; // one week
        Calendar backup = (Calendar) tbr.getRequestedStartTime().clone();
        Calendar classic = tbr.getRequestedStartTime();
        while (trials-- > 0) {
            classic.add(Calendar.MINUTE, minutesOfOneStep);
            if (tbr.isAvailable()) {
                result = (TimeBookingRequest) tbr.clone();
                break;
            }
        }
        tbr.setRequestedStartTime(backup);
        return result;
    }

    private static TimeBookingRequest getSameStartingNextToBackward(TimeBookingRequest tbr) {
        TimeBookingRequest result = null;
        int trials = 24 * 7 * 60 / TimeBookingRequest.getMinutesOfOneStep(tbr.getBookingRule()); // one week
        int pointer = 0;
        Calendar backup = (Calendar) tbr.getRequestedStartTime().clone();
        Calendar classic = tbr.getRequestedStartTime();
        while (pointer++ < trials) {
            classic.add(Calendar.MINUTE, TimeBookingRequest.getMinutesOfOneStep(tbr.getBookingRule()) * -1);
            if (tbr.isAvailable()) {
                result = (TimeBookingRequest) tbr.clone();
                break;
            }
        }
        tbr.setRequestedStartTime(backup);
        return result;
    }

    /**
     * return booking requests, that comes as near as possible to given time frame.
     * capacityUnits interested in are min capacity units bookable by given booking rule, if null given.
     * if there is no booking available, give back two versions: one with capacity units leaving unchanged
     * and a second where the requested time is reduced.
     * @see BookingRule#getMinBookableCapacityUnits() 
     * @param candidate search for free slots next to this candidate. assume, that the candidate is valid. otherwise an exception is thrown.
     * @return a booking request, that comes as near as possible to given time frame.
     */
    public static List<TimeBookingRequest> getBookingRequestNextTo(TimeBookingRequest candidate) {
    	assert candidate.getUser() != null;
        List<TimeBookingRequest> result = new ArrayList<TimeBookingRequest>();
        // candidate is as near as "perfect" now - but is it available?
        if (!candidate.isAvailable()) { // no! ;-(
            if (candidate.isRequest4Yesterdays()) { // do not bother me with that queries
                candidate = null;
            } else if (!candidate.isValidRequest()) {
                throw new DataIntegrityViolationException("candidate's validity must checked before invoking this [200910061602]");
            } else { // take a sledgehammer to crack a nut
                // add the next possibility on another start time on same day
                TimeBookingRequest tmpTbr1a = getSameStartingNextToForward(candidate);
                if (tmpTbr1a != null) {
                    result.add(tmpTbr1a);
                }
                // add the next possibility on another start time on same day
                TimeBookingRequest tmpTbr1b = getSameStartingNextToBackward(candidate);
                if (tmpTbr1b != null) {
                    result.add(tmpTbr1b);
                }

                // add the next possibility with lower time units on same day
                TimeBookingRequest tmpTbr2 = getSameLowerTimeUnits(candidate);
                if (tmpTbr2 != null) {
                    result.add(tmpTbr2);
                }
                // try to find it with same time but lower capacity units
                TimeBookingRequest tmpTbr3 = getSameLowerCapacityUnits(candidate);
                if (tmpTbr3 != null) {
                    result.add(tmpTbr3);
                }

                // add minimals if nothing found
                if (result.size() == 0) {
                    candidate.setRequestedTimeUnits(candidate.getBookingRule().getMinBookableTimeUnits(candidate.getUser()));
                    candidate.setRequestedCapacityUnits(candidate.getBookingRule().getMinBookableCapacityUnits(candidate.getUser()));
                    if (candidate.isAvailable()) {
                        result.add(candidate);
                    }
                }
            }
        } else {
            result.add(candidate);
        }
        return result;
    }

    private static boolean c1IsNearest2c2(Calendar c1, Calendar c2, BookingRule br) {
        return getDistanceInMillis(c1, c2) < TimeBookingRequest.getMinutesOfOneStep(br) * 30000;
    }

    private static long getDistanceInMillis(Calendar c1, Calendar c2) {
        return Math.abs(c1.getTimeInMillis() - c2.getTimeInMillis());
    }

    private BookingFinder() {
    }
}
