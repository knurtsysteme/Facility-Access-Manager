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
 * a default solution for the "justBeen"-fields simply using an attribute to store the information in (means "just been" is defined as the lifetime of
 * the instance).
 * 
 * @author Daniel Oltmanns
 * @since 03/24/2014
 */
public abstract class StoreableAbstract implements Storeable {
  private boolean justBeenInserted = false;
  private boolean justBeenUpdated = false;

  @Override
  public boolean hasJustBeenInserted() {

    return this.justBeenInserted;
  }

  @Override
  public void setJustBeenInserted() {
    this.justBeenInserted = true;
  }

  @Override
  public boolean hasJustBeenUpdated() {
    return this.justBeenUpdated;
  }

  @Override
  public void setJustBeenUpdated() {
    this.justBeenUpdated = true;
  }
}
