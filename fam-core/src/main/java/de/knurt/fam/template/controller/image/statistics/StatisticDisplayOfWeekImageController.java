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
package de.knurt.fam.template.controller.image.statistics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.knurt.fam.core.aspects.security.auth.SessionAuth;
import de.knurt.fam.core.config.FamCalendarConfiguration;
import de.knurt.fam.core.config.FamRequestContainer;
import de.knurt.fam.core.config.style.FamColors;
import de.knurt.fam.core.config.style.FamFonts;
import de.knurt.fam.core.content.text.FamDateFormat;
import de.knurt.fam.core.control.persistence.dao.FamDaoProxy;
import de.knurt.fam.core.model.config.Facility;
import de.knurt.fam.core.model.config.FacilityBookable;
import de.knurt.fam.core.model.persist.FacilityAvailability;
import de.knurt.fam.core.model.persist.booking.Booking;
import de.knurt.fam.core.util.graphics.TextArea;
import de.knurt.fam.core.util.graphics.TextCol;
import de.knurt.fam.core.util.mvc.RedirectResolver;
import de.knurt.fam.core.util.mvc.RequestInterpreter;
import de.knurt.heinzelmann.util.graphics.text.StringMetricsGraphics;
import de.knurt.heinzelmann.util.graphics.text.TextSplitter;
import de.knurt.heinzelmann.util.graphics.text.TextSplitterOnWidth;
import de.knurt.heinzelmann.util.time.SimpleTimeFrame;
import de.knurt.heinzelmann.util.time.TimeFrame;

/**
 * generate the overview of bookings for a specific week requested.
 * 
 * @author Daniel Oltmanns
 * @since 0.20091011 (10/11/2009)
 */
public class StatisticDisplayOfWeekImageController extends SimplePngImageController {

	@Override
	protected Color getBackgroundColor() {
		return Color.WHITE;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		boolean containsDirectAccess = RequestInterpreter.containsDirectAccess("statistics", request);
		if (SessionAuth.user(request) == null && !containsDirectAccess) {
			return RedirectResolver.me().login(request, response);
		} else {
			this.setImageWidthAndHeight(request);
			this.calendarStart = Calendar.getInstance(FamRequestContainer.locale());
			try {
				long timestamp = Long.parseLong(RequestInterpreter.getPureDay(request));
				if (timestamp > 0) {
					calendarStart.setTimeInMillis(timestamp);
				}
			} catch (NumberFormatException e) { // stay on today silently
			} catch (NullPointerException e) { // stay on today silently
			}
			this.facility = RequestInterpreter.getFacility(request);
			if (this.calendarStart == null || this.facility == null) {
				return RedirectResolver.me().home(request, response);
			} else {
				if (this.calendarStart.get(Calendar.DAY_OF_WEEK) != this.calendarStart.getFirstDayOfWeek()) {
					while (this.calendarStart.get(Calendar.DAY_OF_WEEK) != this.calendarStart.getFirstDayOfWeek()) {
						this.calendarStart.add(Calendar.DAY_OF_YEAR, -1);
					}
				}
				Integer tmp = RequestInterpreter.getFrom(request);
				if (tmp != null && tmp >= 0 && tmp <= 23) {
					this.hourStart = tmp.intValue();
				}
				tmp = RequestInterpreter.getTo(request);
				if (tmp != null && tmp >= 1 && tmp <= 24) {
					this.hourStop = tmp.intValue();
				}
				if (this.hourStart == this.hourStop) {
					this.hourStop++;
				} else if (this.hourStart > this.hourStop) {
					tmp = this.hourStop;
					this.hourStop = this.hourStart;
					this.hourStart = tmp;
				}
				this.setOptions();
				return super.handleRequest(request, response);
			}
		}
	}

	private int imageWidth = 1200;
	private int imageHeight = 750;
	private Calendar calendarStart;
	private Facility facility;
	/**
	 * bookinglabelsOfWeek: Map<dayOfWeek, [map of labels]> map of labels:
	 * Map<positionInDay, list of bookings at this position>
	 */
	private Map<Integer, Map<Integer, List<Booking>>> bookinglabelsOfWeek;
	/**
	 * bookinglabelsOfWeek: Map<dayOfWeek, [map of facility availability]> map
	 * of facility availability: Map<positionInDay, facility availability>
	 */
	private Map<Integer, Map<Integer, FacilityAvailability>> dasOfWeek;

	@Override
	protected int getImageWidth() {
		return imageWidth;
	}

	@Override
	protected int getImageHeight() {
		return imageHeight;
	}

	private Font getFontForText() {
		int fontSize = 10 * this.getImageWidth() / 800;
		if (fontSize < 10) {
			fontSize = 10;
		}
		return FamFonts.getFont(Font.PLAIN, fontSize);
	}

	@Override
	protected void createImage(Graphics2D g2d) {
		g2d.setFont(this.getFontForText());
		this.printTimeLine(g2d);
		this.printWeekLine(g2d);
		this.printDaysPanel(g2d);
		this.printHourLinesHorizontal(g2d);
		this.printDaysStripe(g2d);
		this.printDaysText(g2d);
		this.printLinesVertical(g2d);
		this.printTopBlackLine(g2d);
		this.printInfo(g2d);
	}

	private void setImageWidthAndHeight(HttpServletRequest request) {
		Integer imageWidthInRq = null;
		try {
			imageWidthInRq = Integer.parseInt(request.getParameter("width"));
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		if (imageWidthInRq != null) {
			this.imageWidth = Math.abs(imageWidthInRq.intValue());
			if (this.imageWidth > 2500) { // do not fire up the server
				this.imageWidth = 2500;
			}
		}
		Integer imageHeightInRq = null;
		try {
			imageHeightInRq = Integer.parseInt(request.getParameter("height"));
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		if (imageHeightInRq != null) {
			this.imageHeight = Math.abs(imageHeightInRq.intValue());
			if (this.imageHeight > 2500) { // do not fire up the server
				this.imageHeight = 2500;
			}
		}
	}

	private int getTimeLineWidth() {
		return 40;
	}

	private int getTimeLineHeight() {
		return this.getImageHeight() - this.getWeekLineHeight();
	}

	private int hourStop = FamCalendarConfiguration.hourStop();
	private int hourStart = FamCalendarConfiguration.hourStart();

	private int getWeekLineHeight() {
		int ground = 20;
		int hShown = this.hourStop - this.hourStart;
		int isPpH = Math.round((this.getImageHeight() - ground) / hShown);
		return this.getImageHeight() - (isPpH * hShown);
	}

	private int getTimeLineX() {
		return PADDING_LEFT;
	}

	private int getTimeLineY() {
		return this.getWeekLineHeight();
	}

	private int getWeekLineWidth() {
		return this.getImageWidth() - this.getTimeLineWidth();
	}

	private int getPixelPerHour() {
		float pure = this.getDayHeight() / (this.hourStop - this.hourStart);
		return Math.round(pure);
	}

	private void printTimeLine(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		int hour = this.hourStart + 1;
		int offset = 5 + this.getTimeLineY();
		float yPointer = offset + this.getPixelPerHour();
		g2d.setColor(FamColors.FONT);
		while (hour < this.hourStop) {
			// draw hour (in 0-24 h format)
			g2d.drawString(FamDateFormat.getShortTimeFormatted(hour) + "", this.getTimeLineX(), Math.round(yPointer));

			// increment
			hour++;
			yPointer += this.getPixelPerHour();
		}
	}

	private void printWeekLine(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		Calendar cPointer = (Calendar) this.calendarStart.clone();
		int dayPointer = 0;
		int y = Math.round(this.getWeekLineHeight() * .8f);
		g2d.setColor(FamColors.FONT);
		while (dayPointer < 7) {
			String date = cPointer.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, FamRequestContainer.locale()) + " " + DateFormat.getDateInstance(DateFormat.SHORT, FamRequestContainer.locale()).format(cPointer.getTime());
			g2d.drawString(date, this.getDayX(dayPointer) + PADDING_LEFT, y);
			cPointer.add(Calendar.DAY_OF_YEAR, 1);
			dayPointer++;
		}
	}

	private List<List<TimeFrame>> pTimeFramesOfWeek;
	private List<List<FacilityAvailability>> generalAvailabilitiesOfWeek;

	private void printDaysPanel(Graphics2D g2d) {
		int dayPointer = 0;
		while (dayPointer < 7) {
			List<TimeFrame> pTimeFrames = pTimeFramesOfWeek.get(dayPointer);
			Color[] colors = null;
			int dayX = this.getDayX(dayPointer);
			for (TimeFrame pTimeFrame : pTimeFrames) {
				colors = this.getColorsFor(pTimeFrame, this.bookingsOfWeek.get(dayPointer), generalAvailabilitiesOfWeek.get(dayPointer));
				g2d.setColor(colors[1]);
				g2d.fillRect(dayX + stripeWidth, this.getY(pTimeFrame.getCalendarStart()), panelWidth, pTimeFrameHeight + 2);
			}
			dayPointer++;
		}
	}

	private int getDayHeight() {
		return this.getTimeLineHeight();
	}

	private int getDayWidth() {
		return this.getWeekLineWidth() / 7;
	}

	private int getDayY() {
		return this.getWeekLineHeight();
	}

	private int getDayX(int no) {
		return (no * this.getDayWidth()) + this.getTimeLineWidth();
	}

	private int getY(Booking booking) {
		return Math.round(this.getY(booking.getSessionTimeFrame().getCalendarStart()));
	}

	private int getHeightOf(TimeFrame timeFrame) {
		return Math.round(this.getPixelPerHour() * (timeFrame.getDuration() / 3600000f)); // pixel
		// per
		// hour
		// *
		// duration
		// in
		// hours
	}

	private int getY(Calendar calendar) {
		Calendar dayStart = SimpleTimeFrame.getDay(calendar).getCalendarStart();
		dayStart.add(Calendar.HOUR_OF_DAY, this.hourStart);
		int offset = this.getDayY();
		if (calendar.before(dayStart)) {
			return offset;
		} else {
			return Math.round(offset + this.getHeightOf(new SimpleTimeFrame(dayStart, calendar)));
		}
	}

	private Color[] getColorsFor(TimeFrame pTimeFrame, List<Booking> bookingsOfDay, List<FacilityAvailability> generalAvailabilitiesOfDay) {
		Color stripe = null;
		Color panel = null;
		boolean colorFound = false;
		for (FacilityAvailability da : generalAvailabilitiesOfDay) {
			if (da.overlaps(pTimeFrame) && !da.isCompletelyAvailable()) {
				if (da.isNotAvailableBecauseOfSuddenFailure()) {
					stripe = FamColors.BLUE;
					panel = FamColors.BLUE_BRIGHT;
				} else if (da.isNotAvailableBecauseOfMaintenance()) {
					stripe = FamColors.GRAY;
					panel = FamColors.GRAY_BRIGHT;
				} else if (da.mustNotStartHere()) {
					stripe = FamColors.MUST_NOT_START_HERE;
					panel = FamColors.MUST_NOT_START_HERE_BRIGHT;
				} else { // opening hours
					stripe = FamColors.GRAY_BRIGHT;
					panel = FamColors.GRAY_BRIGHTER;
				}
				colorFound = true;
			}
		}
		if (!colorFound && bookingsOfDay != null) { // bookable facility and no
			// maintenance
			// count statuses
			int capacitySum = 0;
			for (Booking b : bookingsOfDay) {
				if (b.overlaps(pTimeFrame)) {
					capacitySum += b.getCapacityUnits();
				}
			}
			if (capacitySum > 0) {
				if (((FacilityBookable) this.facility).getCapacityUnits() > capacitySum) { // NOT
					// completely
					// booked
					// up
					stripe = FamColors.PARTLY;
					panel = FamColors.PARTLY_BRIGHT;
				} else { // mainteneance or opening hours
					stripe = FamColors.FULL;
					panel = FamColors.FULL_BRIGHT;
				}
				colorFound = true;
			}
		}

		Color[] result = new Color[2];
		result[0] = colorFound ? stripe : FamColors.FREE;
		result[1] = colorFound ? panel : FamColors.FREE_BRIGHT;
		return result;

	}

	private void printTopBlackLine(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		int y1y2 = this.getDayY();
		int x2 = this.getDayWidth() * 7 + this.getTimeLineWidth();
		g2d.drawLine(this.getTimeLineX(), y1y2, x2, y1y2);
	}

	private void printHourLinesHorizontal(Graphics2D g2d) {
		int y1y2 = this.getDayY();
		int x2 = this.getDayWidth() * 7 + this.getTimeLineWidth();
		float opacity = 0.8f;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g2d.setColor(Color.WHITE);
		int x1 = this.getDayX(0);
		int stop = this.hourStop;
		int pointer = this.hourStart + 1;
		y1y2 += this.getPixelPerHour();
		while (pointer < stop) {
			g2d.drawLine(x1, y1y2, x2, y1y2);
			pointer++;
			y1y2 += this.getPixelPerHour();
		}
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
	}

	private void printInfo(Graphics2D g2d) {
		float opacity = 0.3f;
		Font f = FamFonts.getFont(Font.BOLD, 20);
		int x = this.getImageWidth() - 5;
		int y = this.getImageHeight() - 10;
		AffineTransform trans90 = new AffineTransform();
		trans90.setToRotation(-Math.PI / 2, x, y);
		g2d.setTransform(trans90);
		g2d.setFont(f);
		g2d.setColor(FamColors.FONT);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g2d.drawString(this.facility.getLabel(), x, y);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
	}

	private int stripeWidth = -1;
	private int panelWidth = -1;
	private int pTimeFrameHeight = -1;

	/**
	 * this is invoked by the constructor before any other method
	 */
	private void setOptions() {
		Calendar cPointer = (Calendar) this.calendarStart.clone();

		stripeWidth = (int) (this.getDayWidth() * .1);
		if (stripeWidth > 30) {
			stripeWidth = 30;
		} else if (stripeWidth < 5) {
			stripeWidth = 5;
		}
		panelWidth = this.getDayWidth() - stripeWidth;

		// create set bookable facility
		FacilityBookable bd = null;
		if (this.facility.isBookable()) {
			bd = (FacilityBookable) this.facility;
		}

		// set bookings
		this.bookingsOfWeek = new HashMap<Integer, List<Booking>>();
		int dayPointer = 0;
		while (dayPointer < 7) {
			if (this.facility.isBookable()) {
				this.bookingsOfWeek.put(dayPointer, FamDaoProxy.bookingDao().getUncanceledBookingsWithoutApplicationsIn(bd, SimpleTimeFrame.getDay(cPointer)));
			} else {
				this.bookingsOfWeek.put(dayPointer, new ArrayList<Booking>());
			}
			cPointer.add(Calendar.DAY_OF_YEAR, 1);
			dayPointer++;
		}
		cPointer.add(Calendar.DAY_OF_YEAR, -7);

		// set generalavailability of week
		this.generalAvailabilitiesOfWeek = new ArrayList<List<FacilityAvailability>>();
		dayPointer = 0;
		while (dayPointer < 7) {
			TimeFrame entireDay = SimpleTimeFrame.getDay(cPointer);
			List<FacilityAvailability> tmpDas = FamDaoProxy.facilityDao().getFacilityAvailabilitiesMergedByFacilities(entireDay, this.facility.getKey());
			if (tmpDas == null) {
				this.generalAvailabilitiesOfWeek.add(new ArrayList<FacilityAvailability>());
			} else {
				this.generalAvailabilitiesOfWeek.add(tmpDas);
			}
			cPointer.add(Calendar.DAY_OF_YEAR, 1);
			dayPointer++;
		}
		cPointer.add(Calendar.DAY_OF_YEAR, -7);

		// set p time frames
		dayPointer = 0;
		TimeFrame pTimeFrame = SimpleTimeFrame.getDay(cPointer);
		Calendar pCalStart = pTimeFrame.getCalendarStart();
		pCalStart.set(Calendar.HOUR_OF_DAY, this.hourStart);
		Calendar pCalEnd = (Calendar) pCalStart.clone();
		pCalEnd.add(Calendar.MINUTE, FamCalendarConfiguration.smallestMinuteStep());
		pTimeFrame = new SimpleTimeFrame(pCalStart, pCalEnd);
		pTimeFrameHeight = this.getHeightOf(pTimeFrame);
		this.pTimeFramesOfWeek = new ArrayList<List<TimeFrame>>();
		while (dayPointer < 7) {
			List<TimeFrame> tmp = new ArrayList<TimeFrame>();
			while (pTimeFrame.getCalendarStart().get(Calendar.DAY_OF_YEAR) == cPointer.get(Calendar.DAY_OF_YEAR)) {
				tmp.add(pTimeFrame.clone());
				pTimeFrame.add(Calendar.MINUTE, FamCalendarConfiguration.smallestMinuteStep());
			}
			this.pTimeFramesOfWeek.add(tmp);
			pTimeFrame.add(Calendar.HOUR_OF_DAY, this.hourStart);
			cPointer.add(Calendar.DAY_OF_YEAR, 1);
			dayPointer++;
		}
		cPointer.add(Calendar.DAY_OF_YEAR, -7);

		// set bookings for labels
		dayPointer = 0;
		this.bookinglabelsOfWeek = new HashMap<Integer, Map<Integer, List<Booking>>>();
		while (dayPointer < 7) {
			Map<Integer, List<Booking>> labels = new HashMap<Integer, List<Booking>>(); // Map<Y
			// Position
			// in
			// Day,
			// booking>
			// collect labels of booking
			if (this.facility.isBookable()) {
				for (Booking booking : this.bookingsOfWeek.get(dayPointer)) {
					int bookingY = this.getDayY(); // assuming starting on
					// beginning of day here!
					if (booking.getSessionTimeFrame().getCalendarStart().get(Calendar.DAY_OF_YEAR) == cPointer.get(Calendar.DAY_OF_YEAR)) { // booking
						// starts
						// on
						// this
						// day
						bookingY = this.getY(booking);
					}
					if (!labels.containsKey(bookingY)) {
						labels.put(bookingY, new ArrayList<Booking>());
					}
					labels.get(bookingY).add(booking);
				}
			}
			this.bookinglabelsOfWeek.put(dayPointer, labels);
			cPointer.add(Calendar.DAY_OF_YEAR, 1);
			dayPointer++;
		}
		cPointer.add(Calendar.DAY_OF_YEAR, -7);

		// set maintencane for labels
		dayPointer = 0;
		this.dasOfWeek = new HashMap<Integer, Map<Integer, FacilityAvailability>>();
		while (dayPointer < 7) {
			// collect labels of availablities
			Map<Integer, FacilityAvailability> dasOfDay = new HashMap<Integer, FacilityAvailability>();
			for (FacilityAvailability da : this.generalAvailabilitiesOfWeek.get(dayPointer)) {
				if (!da.isCompletelyAvailable() && da.getBasePeriodOfTime().getCalendarStart().get(Calendar.DAY_OF_YEAR) == cPointer.get(Calendar.DAY_OF_YEAR)) { // booking
					// starts
					// on
					// this
					// day
					int daY = this.getY(da);
					dasOfDay.put(daY, da);
				}
			}
			this.dasOfWeek.put(dayPointer, dasOfDay);
			cPointer.add(Calendar.DAY_OF_YEAR, 1);
			dayPointer++;
		}
		cPointer.add(Calendar.DAY_OF_YEAR, -7);

		this.textSplitter = new TextSplitterOnWidth(this.panelWidth - (2 * PADDING_LEFT), ' ', new StringMetricsGraphics(this.getFontForText()));
	}

	private Map<Integer, List<Booking>> bookingsOfWeek;

	private TextCol getTextCol(Font fontToUse) {
		TextCol result = new TextCol();

		// create text areas
		int dayPointer = 0;
		int lineHeight = FamFonts.getLineHeight(fontToUse);
		while (dayPointer < 7) {

			int posX = this.getPrintOutPosX(dayPointer);

			// add text area for facility availability
			Map<Integer, FacilityAvailability> dasOfDay = this.dasOfWeek.get(dayPointer);
			for (Integer posY : dasOfDay.keySet()) {
				String tmp = "";
				FacilityAvailability da = dasOfDay.get(posY);
				if (dasOfDay.get(posY).isNotAvailableInGeneral()) {
					tmp = "Not opened"; // INTLANG
				} else if (dasOfDay.get(posY).isNotAvailableBecauseOfSuddenFailure()) {
					tmp = "Failure"; // INTLANG
				} else if (dasOfDay.get(posY).mustNotStartHere()) {
					tmp = "Access denial"; // INTLANG
				} else { // maintenance
					tmp = "Maintenance"; // INTLANG
				}

				List<String> notices = this.getNotice(da.getNotice());
				if (notices != null) {
					for (String notice : notices) {
						tmp += ": " + notice;
					}
				}

				TextArea ta = new TextArea(posX, posY, this.textSplitter, lineHeight);
				ta.addText(tmp);
				result.add(ta);
			}

			// add text areas for bookings
			Map<Integer, List<Booking>> bookings = this.bookinglabelsOfWeek.get(dayPointer);
			List<Integer> posYs = new ArrayList<Integer>(bookings.keySet());
			for (Integer posY : posYs) {
				List<Booking> bookingsOnPos = bookings.get(posY);
				TextArea ta = new TextArea(posX, posY, this.textSplitter, lineHeight);
				// create the text
				if (bookingsOnPos.size() == 1) { // one booking on pos
					// print name
					String tmp = bookingsOnPos.get(0).getUser().getFullName();
					if (!this.matchesPanelWidth(tmp)) {
						tmp = tmp.split(" ")[0] + " " + tmp.split(" ")[2];
						if (!this.matchesPanelWidth(tmp)) {
							tmp = bookingsOnPos.get(0).getUser().getUsername();
							if (!this.matchesPanelWidth(tmp)) {
								tmp = bookingsOnPos.get(0).getUser().getInitialing();
							}
						}
					}
					ta.addText(tmp);
				} else { // more than one booking on pos
					// print how many people booked home many units
					int units = 0;
					for (Booking tmp : bookingsOnPos) {
						units += tmp.getCapacityUnits();
					}
					ta.addText(String.format("%s bookings / %s units", bookingsOnPos.size(), units)); // INTLANG
				}
				result.add(ta);
			}
			// increment
			dayPointer++;
		}
		return result;
	}

	private void printDaysText(Graphics2D g2d) {
		TextCol textCol = this.getTextCol(g2d.getFont());
		g2d.setColor(FamColors.FONT);
		int dayPointer = 0;
		int lineHeight = FamFonts.getLineHeight(g2d.getFont());
		while (dayPointer < 7) {
			int posX = this.getPrintOutPosX(dayPointer);
			TextCol textAreasOfDay = textCol.getPosX(posX).doNotOverlap_moveDown();
			for (TextArea ta : textAreasOfDay) {
				int posY = ta.getPosY() + lineHeight;
				for (String line : ta.getTextSplitted()) {
					g2d.drawString(line, ta.getPosX(), posY);
					posY += lineHeight;
				}
			}
			dayPointer++;
		}
	}

	private int PADDING_LEFT = 3;
	private TextSplitter textSplitter = null;

	/**
	 * return true, if given string is smaller then panel width
	 * 
	 * @param raw
	 * @return
	 */
	private boolean matchesPanelWidth(String raw) {
		return FamFonts.getStringMetrics().getWidth(raw) <= this.panelWidth;
	}

	private List<String> getNotice(String raw) {
		if (raw == null) {
			return null;
		} else {
			return this.textSplitter.split(raw.replaceAll("\n", " "));
		}
	}

	private void printDaysStripe(Graphics2D g2d) {
		int dayPointer = 0;
		while (dayPointer < 7) {
			List<TimeFrame> pTimeFrames = pTimeFramesOfWeek.get(dayPointer);
			Color[] colors = null;
			int dayX = this.getDayX(dayPointer);
			for (TimeFrame pTimeFrame : pTimeFrames) {
				int posY = this.getY(pTimeFrame.getCalendarStart());
				colors = this.getColorsFor(pTimeFrame, this.bookingsOfWeek.get(dayPointer), generalAvailabilitiesOfWeek.get(dayPointer));
				g2d.setColor(colors[0]);
				g2d.fillRect(dayX, posY, stripeWidth, pTimeFrameHeight + 2);
			}

			dayPointer++;
		}

	}

	private void printLinesVertical(Graphics2D g2d) {
		int dayPointer = 0;
		g2d.setColor(FamColors.FONT);
		while (dayPointer < 7) {
			g2d.drawLine(this.getDayX(dayPointer), 0, this.getDayX(dayPointer), this.getImageHeight());
			dayPointer++;

		}

	}

	private int getY(FacilityAvailability da) {
		return Math.round(this.getY(da.getBasePeriodOfTime().getCalendarStart()));
	}

	private int getPrintOutPosX(int dayPointer) {
		return this.getDayX(dayPointer) + stripeWidth + PADDING_LEFT;
	}
}
