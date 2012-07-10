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
package de.knurt.fam.news;

import java.util.Date;

/**
 * a line of news.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public interface NewsItem extends Comparable<NewsItem> {
	/**
	 * return the start of the event or when the message happens. it is NOT the
	 * time of this notification is generated.
	 * 
	 * @return the start of the event
	 */
	public Date getEventStarts();

	/**
	 * set the start of the event or when the message happens. it is NOT the
	 * time of this notification is generated.
	 */
	public void setEventStarts(Date eventStarts);

	/**
	 * if the notification is about a time frame, this is the end point of time.
	 * if it is just a point of time, return <code>null</code>.
	 * 
	 * @return the end of the event or <code>null</code> on events with a point
	 *         of time
	 */
	public Date getEventEnds();

	/**
	 * set the end of the event. if the notification is about a time frame, this
	 * is the end point of time. if it is just a point of time, return
	 * <code>null</code>.
	 */
	public void setEventEnds(Date eventEnds);

	/**
	 * if a page with further information exist, return the url to it. otherwise
	 * <code>null</code>.
	 * 
	 * @return the url to further information
	 */
	public String getLinkToFurtherInformation();

	/**
	 * set url to further information
	 */
	public void setLinkToFurtherInformation(String linkToFurtherInformation);

	/**
	 * return the description of the notification. in other words: the content.
	 * 
	 * @return notification content
	 */
	public String getDescription();

	/**
	 * set notification content
	 */
	public void setDescription(String description);

}
