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
package de.knurt.fam.core.view.adapter.html;

import java.util.ArrayList;
import java.util.List;

import de.knurt.fam.core.model.persist.User;
import de.knurt.heinzelmann.util.adapter.ViewableObject;

/**
 * map every given model object (<code>T</code>) to a view object.
 * 
 * @param <T>
 *            the object type that is adapted here
 * @author Daniel Oltmanns
 * @since 0.20090710 (07/10/2009)
 */
@Deprecated
public abstract class DasHtmlAdapterAbstractFactory<T extends ViewableObject> {

	/**
	 * return instances of adapters for the given list of objects.
	 * 
	 * @param current
	 *            user logged in into the system
	 * @param viewableObjects
	 *            list of objects being adapted.
	 * @return instances of adapters for the given list of objects.
	 */
	public List<FamHtmlAdapter<T>> getInstances(User current, List<T> viewableObjects) {
		List<FamHtmlAdapter<T>> result = new ArrayList<FamHtmlAdapter<T>>(viewableObjects.size());
		for (T mappedObject : viewableObjects) {
			result.add(this.getInstance(current, mappedObject));
		}
		return result;
	}

	/**
	 * return an instance of the adapter.
	 * 
	 * @param current
	 *            user logged in into the system
	 * @param viewableObject
	 *            that is adapted
	 * @return an instance of the adapter.
	 */
	protected abstract FamHtmlAdapter<T> getInstance(User current, T viewableObject);
}
