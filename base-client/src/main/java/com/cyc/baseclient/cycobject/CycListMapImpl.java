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
package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycListMapImpl.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
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

import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycListMap;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.baseclient.datatype.CycStringUtils;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author nwinant
 * @param <K>
 * @param <V>
 */
//public class CycListMapImpl<E extends Object, K extends E, V extends E>
//         extends LinkedHashMap<K,V> implements CycListMap<E, K, V> {

public class CycListMapImpl<K, V>
         extends LinkedHashMap<K,V> implements CycListMap<K, V> {

  private static <V> V processListValue(V o) {
    if (o instanceof CycList && ((CycList) o).isPlist()) {
      return (V) from((CycList) o);
    }
    return o;
  }
  
  public static <K,V> CycListMapImpl<K, V> from(CycList<V> l) {
    final CycListMapImpl<K,V> results = new CycListMapImpl<>();
    boolean keyIdentified = false;
    CycSymbol key = null;
    for (Object o : l) {
      if (keyIdentified == false) {
         key = (CycSymbol) o;
        keyIdentified = true;
      } else {
        results.put((K) key, (V) processListValue(o));
        keyIdentified = false;
      }
    }
    return results;
  }
  
  // Construction
  
  public CycListMapImpl(int initialSize) {
    super(initialSize);
  }
  
  public CycListMapImpl() {
    super();
  }
  
  
  // Public
  
  @Override
  public CycList<V> toList() {
    throw new UnsupportedOperationException("Not supported yet."); // TODO: implement! - nwinant, 2017-08-03
  }
  
  @Override
  public List<String> toPrettyStrings(String indent) {
    final String prefix = indent + "";
    return CycStringUtils.toStrings(this, prefix, " = ", "");
  }
  
  @Override
  public String toPrettyString(String indent) {
    return StringUtils.join(toPrettyStrings(indent), StringUtils.LF);
  }
  
}


































































