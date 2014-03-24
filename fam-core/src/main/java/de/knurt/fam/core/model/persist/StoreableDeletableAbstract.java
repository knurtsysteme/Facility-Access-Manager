/*
 * Copyright 2009-2014 by KNURT Systeme (http://www.knurt.de)
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
package de.knurt.fam.core.model.persist;

/**
 * a default solution for the justBeenInserted fields
 * 
 * @author Daniel Oltmanns
 * @since 03/24/2014
 */
public abstract class StoreableDeletableAbstract extends StoreableAbstract implements Deletable {
  private boolean justBeenDeleted = false;

  @Override
  public boolean hasJustBeenDeleted() {

    return this.justBeenDeleted;
  }

  @Override
  public void setJustBeenDeleted() {
    this.justBeenDeleted = true;
  }
}
