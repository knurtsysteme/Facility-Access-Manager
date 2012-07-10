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
package de.knurt.fam.core.util.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * helper class merging overlapping facility time frames.
 * @author Daniel Oltmanns
 * @since 0.20090518
 */
public class FacilityAvailabilityMerger {

    /**
     * return facility time frames merged by priority of facility keys.
     * if facility key is the same, it is merged by {@link FacilityAvailability#timeStampSet}.
     * this will destroy all {@link FacilityAvailability#timeStampSet}.
     * @see #getMergedByTimeStampSet(java.util.List, de.knurt.heinzelmann.util.time.TimeFrame) 
     * @param tfs {@link FacilityAvailability}s to merge
     * @param fromTo watched time frame (in case of iterations or time frames outside fromTo)
     * @param priority list of facility keys for priority. the higher the index
     *  of the facility key, the higher the priority.
     * @return facility time frames merged by priority of facility keys.
     */
    public static List<FacilityAvailability> getMergedByFacilities(List<? extends FacilityAvailability> tfs, TimeFrame fromTo, List<String> priority) {
        HashMap<String, ArrayList<FacilityAvailability>> tfsOfFacilityKey = new HashMap<String, ArrayList<FacilityAvailability>>();
        for (String facilityKey : priority) {
            tfsOfFacilityKey.put(facilityKey, new ArrayList<FacilityAvailability>());
        }
        for (FacilityAvailability tf : tfs) {
            tfsOfFacilityKey.get(tf.getFacilityKey()).add(tf);
        }
        ArrayList<FacilityAvailability> toMerges = new ArrayList<FacilityAvailability>();
        for (int i = 0; i < priority.size(); i++) {
            List<FacilityAvailability> tmp = getMergedByTimeStampSet(tfsOfFacilityKey.get(priority.get(i)), fromTo);
            for (FacilityAvailability dtf : tmp) {
                dtf.setTimeStampSet((long) i); // set position of priority as new time stamp
            }
            toMerges.addAll(tmp);
        }
        return getMergedByTimeStampSet(toMerges, fromTo);
    }

    private static boolean candidateIsHiddenByOtherTf(FacilityAvailability candidate, FacilityAvailability otherTf) {
        return otherTf.getBasePeriodOfTime().getStart() <= candidate.getBasePeriodOfTime().getStart() && otherTf.getBasePeriodOfTime().getEnd() >= candidate.getBasePeriodOfTime().getEnd();
    }

    private static boolean otherTfIsInCandidate(FacilityAvailability otherTf, FacilityAvailability candidate) {
        return otherTf.getBasePeriodOfTime().getStart() > candidate.getBasePeriodOfTime().getStart() &&
                otherTf.getBasePeriodOfTime().getEnd() < candidate.getBasePeriodOfTime().getEnd();
    }

    /**
     * merge and return the time frames.
     * a newer time frame has priority. facility keys are ignored.
     * all iterations are splitted and set to ONE_TIME.
     * the result is unsorted!
     * @see FacilityAvailability#getTimeStampSet()
     * @see <a href="./doc-files/timeFrameStrategy_beforeMelting.svg">graphic before mergingm</a>
     * @see <a href="./doc-files/timeFrameStrategy_AfterMelting.svg">graphic after merging</a>
     * @param tfs
     * @param fromTo watched time frame (in case of iterations or time frames outside fromTo)
     * @return merged time frames.
     */
    public static List<FacilityAvailability> getMergedByTimeStampSet(List<? extends FacilityAvailability> tfs, TimeFrame fromTo) {
        ArrayList<FacilityAvailability> notIteratedTfs = new ArrayList<FacilityAvailability>();

        // set it all to from to and to ONE TIME FacilityAvailabilitys
        for (FacilityAvailability tf : tfs) {
            if (tf.overlaps(fromTo)) {
                for (FacilityAvailability da : tf.getFacilityAvailabilitiesWithNoIteration(fromTo)) {
                    notIteratedTfs.add(da);
                }
            }
        }
        return getMergedByTimeStampSetIntern(notIteratedTfs, fromTo);
    }

    private static ArrayList<FacilityAvailability> getMergedByTimeStampSetIntern(ArrayList<FacilityAvailability> notIteratedTfs, TimeFrame fromTo) {
        ArrayList<FacilityAvailability> result = new ArrayList<FacilityAvailability>();
        if (notIteratedTfs.size() > 1) { // there is something to merge
            // if a time frame is in another, split and recursion!
            for (FacilityAvailability candidate : notIteratedTfs) {
                for (FacilityAvailability otherTf : notIteratedTfs) {
                    if (otherTf.equals(candidate)) { // other time frame is same time frame
                        continue;
                    }
                    if (otherTf.getTimeStampSet().before(candidate.getTimeStampSet())) { // candidate is newer
                        continue;
                    }
                    if (otherTfIsInCandidate(otherTf, candidate)) {
                        FacilityAvailability dtfBefore = candidate.clone();
                        dtfBefore.getBasePeriodOfTime().setStartEnd(candidate.getBasePeriodOfTime().getStart(), otherTf.getBasePeriodOfTime().getStart());
                        notIteratedTfs.add(dtfBefore);

                        FacilityAvailability dtfAfter = candidate.clone();
                        dtfAfter.getBasePeriodOfTime().setStartEnd(otherTf.getBasePeriodOfTime().getEnd(), candidate.getBasePeriodOfTime().getEnd());
                        notIteratedTfs.add(dtfAfter);

                        notIteratedTfs.remove(candidate);
                        return getMergedByTimeStampSetIntern(notIteratedTfs, fromTo);
                    }
                }
            }
            // compare a candidate to to the rest
            for (FacilityAvailability candidate : notIteratedTfs) {
                Calendar newStart = candidate.getBasePeriodOfTime().getCalendarStart();
                Calendar newEnd = candidate.getBasePeriodOfTime().getCalendarEnd();
                boolean candidateWin = true; // true, if candidate stays in final result (is not hidden by another time frame)
                for (FacilityAvailability otherTf : notIteratedTfs) {
                    if (otherTf.equals(candidate)) { // other time frame is same time frame
                        continue;
                    }
                    if (otherTf.getTimeStampSet().before(candidate.getTimeStampSet())) { // candidate is newer
                        continue;
                    }
                    if (candidateIsHiddenByOtherTf(candidate, otherTf)) { // candidate is hidden by another time frame and fails
                        candidateWin = false;
                        break;
                    }
                    if (candidate.overlaps(otherTf.getBasePeriodOfTime())) {
                        if (otherTf.getBasePeriodOfTime().getCalendarStart().after(newStart)) {
                            newEnd = otherTf.getBasePeriodOfTime().getCalendarStart();
                        }
                        if (otherTf.getBasePeriodOfTime().getCalendarEnd().before(newEnd) ||
                                otherTf.getBasePeriodOfTime().getCalendarEnd().equals(newEnd)) {
                            newStart = otherTf.getBasePeriodOfTime().getCalendarEnd();
                        }
                    }
                    if (newStart.getTimeInMillis() >= newEnd.getTimeInMillis()) { // candidate is hidden completely by many other time frames and fails
                        candidateWin = false;
                        break;
                    }
                }
                if (candidateWin) {
                    // set new start and end of candidate and add it to the result
                    candidate.getBasePeriodOfTime().setStart(newStart);
                    candidate.getBasePeriodOfTime().setEnd(newEnd);
                    result.add(candidate);
                }
            }
        } else { // nothing to merge
            result = notIteratedTfs;
        }
        return result;
    }

    private FacilityAvailabilityMerger() {
    }
}
