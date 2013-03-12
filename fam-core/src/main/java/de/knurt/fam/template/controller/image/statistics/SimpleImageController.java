/*
 * Copyright 2005 - 2012 by KNURT Systeme (http://www.knurt.de)
 *
 * Licensed under the Creative Commons License Attribution-NonCommercial 3.0 Unported;
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * response image created with {@link Graphics2D}
 * 
 * @author Daniel Oltmanns
 * @since 0.20090423 (04/23/2009)
 */
public abstract class SimpleImageController implements Controller {

	private HttpServletRequest request;

	@Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.setRequest(request);
		response.setContentType(this.getContentType());
		BufferedImage messageImage = new BufferedImage(this.getImageWidth(), this.getImageHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) messageImage.getGraphics();
		this.setBackgroundColor(g2d);
		this.createImage(g2d);
		ImageIO.write(messageImage, this.getFormatName(), response.getOutputStream());
		return null;
	}

	/**
	 * return the content type of the image. because of, it is an image, the
	 * content type is commonly one of:
	 * <ul>
	 * <li>image/jpeg</li>
	 * <li>image/png</li>
	 * <li>image/gif</li>
	 * </ul>
	 * note to return the right format name matching the content type
	 * 
	 * @see #getFormatName()
	 * @return the content type of the image.
	 */
	protected abstract String getContentType();

	/**
	 * return the color of the canvas. this is printed on every image before the
	 * image is created.
	 * 
	 * @return the color of the canvas.
	 */
	protected abstract Color getBackgroundColor();

	/**
	 * return the format name of the image. because of it is an image, the
	 * format name is commonly one of:
	 * <ul>
	 * <li>jpg</li>
	 * <li>png</li>
	 * <li>gif</li>
	 * </ul>
	 * note to return the right content type matching the format name.
	 * 
	 * @see #getContentType()
	 * @return the content type of the image.
	 */
	protected abstract String getFormatName();

	/**
	 * return the image width in pixel.
	 * 
	 * @return the image width in pixel.
	 */
	protected abstract int getImageWidth();

	/**
	 * return the image height in pixel.
	 * 
	 * @return the image height in pixel.
	 */
	protected abstract int getImageHeight();

	/**
	 * create the image. this is where to paint the content onto the canvas.
	 * 
	 * @param g2d
	 *            used to paint the image.
	 */
	protected abstract void createImage(Graphics2D g2d);

	private void setBackgroundColor(Graphics2D g2d) {
		g2d.setPaint(this.getBackgroundColor());
		g2d.fillRect(0, 0, this.getImageWidth(), this.getImageHeight());
	}

	/**
	 * return the request got for the image. note: this feature may not be
	 * thread safe!
	 * 
	 * @return the request got for the image.
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * set the request got for the image. note: this feature may not be thread
	 * safe!
	 * 
	 * @param request
	 *            got for the image.
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
}