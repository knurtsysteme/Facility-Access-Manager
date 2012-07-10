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
 * The default news item - a simple bean of the attributes.
 * 
 * @author Daniel Oltmanns <info@knurt.de>
 * @since 1.5.0 (07/29/2011)
 * 
 */
public class NewsItemDefault implements NewsItem {
	private Date eventStarts, eventEnds;
	private String linkToFurtherInformation, description;

	/** {@inheritDoc} */
	@Override
	public Date getEventStarts() {
		return eventStarts;
	}

	/** {@inheritDoc} */
	@Override
	public void setEventStarts(Date eventStarts) {
		this.eventStarts = eventStarts;
	}

	/** {@inheritDoc} */
	@Override
	public Date getEventEnds() {
		return eventEnds;
	}

	/** {@inheritDoc} */
	@Override
	public void setEventEnds(Date eventEnds) {
		this.eventEnds = eventEnds;
	}

	/** {@inheritDoc} */
	@Override
	public String getLinkToFurtherInformation() {
		return linkToFurtherInformation;
	}

	/** {@inheritDoc} */
	@Override
	public void setLinkToFurtherInformation(String linkToFurtherInformation) {
		this.linkToFurtherInformation = linkToFurtherInformation;
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription() {
		return description;
	}

	/** {@inheritDoc} */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(NewsItem o) {
		if (this.getEventStarts() == null)
			return 1;
		else
			return this.getEventStarts().before(o.getEventStarts()) ? -1 : 1;
	}
}