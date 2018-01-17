/*
 * Copyright 2017 Cycorp, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyc.base.cycobject;

/*
 * #%L
 * File: CycListMap.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.Map;

/**
 * A Map representation of a CycList ALIST or PLIST. 
 * 
 * @author nwinant
 * @param <K>
 * @param <V>
 * 
 * @see CycList#toMap() 
 */
public interface CycListMap<K,V> extends Map<K,V> {
  
  /**
   * Return the contents of this CycListMap as a CycList. Depending on the contents of the map, the
   * resulting CycList should be either an ALIST or a PLIST.
   * 
   * @return a CycList containing the contents of this map
   * 
   * @see CycList#toMap() 
   */
  CycList<V> toList();
  
  /**
   * Returns a list of pretty-printed strings, one string per entry.
   *
   * @param indent the indent string that is added before the <tt>String</tt> representation this
   *               <tt>CycListMap</tt>
   *
   * @return a list of pretty-printed strings
   */
  List<String> toPrettyStrings(String indent);
  
  /**
   * Returns a `pretty-printed' <tt>String</tt> representation of this <tt>CycListMap</tt>.
   *
   * @param indent the indent string that is added before the String representation this CycListMap
   *
   * @return a pretty-printed String representation of this CycListMap
   */
  String toPrettyString(String indent);

}
