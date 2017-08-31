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
package com.cyc.baseclient.inference.params;

/*
 * #%L
 * File: InferenceParametersMap.java
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

import com.cyc.base.cycobject.CycSymbol;
import com.cyc.query.parameters.InferenceParameters;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author nwinant
 */
abstract class InferenceParametersMap implements InferenceParameters {
  
  // Static methods
  
  private static <T extends Enum> T coerceToEnum(final Object value, final Class<T> type) {
    if (value == null) {
      return null;
    } else if (type.isInstance(value)) {
      return type.cast(value);
    } else if (value instanceof CycSymbol) {
      return type.cast(Enum.valueOf((Class<T>) type, ((CycSymbol) value).getSymbolName().replace('-', '_')));
    } else {
      throw new IllegalStateException("Bad " + type.getSimpleName() + " value " + value);
    }
  }
  
  
  // Fields
  
  private final Map<String, Object> map = new HashMap<String, Object>();
  
  
  // Methods
  
  @Override
  public void clear() {
    map.clear();
  }
  
  @Override
  public boolean containsKey(String key) {
    return map.containsKey(key.toUpperCase());
  }
  
  @Override
  public Set<Map.Entry<String, Object>> entrySet() {
    return Collections.unmodifiableSet(map.entrySet());
  }
  
  @Override
  public Object get(String parameterName) {
    return map.get(parameterName.toUpperCase());
  }
  
  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }
  
  @Override
  public Set<String> keySet() {
    return Collections.unmodifiableSet(map.keySet());
  }
  
  @Override
  public Object put(String parameterName, Object value) {
    return map.put(parameterName.toUpperCase(), value);
  }
  
  @Override
  public void putAll(InferenceParameters properties) {
    for (final String key : properties.keySet()) {
      put(key, properties.get(key));
    }
  }
  
  @Override
  public void remove(String property) {
    map.remove(property.toUpperCase());
  }
  
  @Override
  public int size() {
    return map.size();
  }
  
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public void updateFromPlist(final List plist) {
    for (int i = 0; i < plist.size(); i++) {
      final String paramKey = ((CycSymbol) plist.get(i++)).cyclify();
      final Object paramValue = plist.get(i);
      put(paramKey, paramValue);
    }
  }
  
  @Override
  public void makeAtLeastAsLooseAs(final InferenceParameters newParams) {
    if (newParams.getMaxTransformationDepth() == null 
            || newParams.getMaxTransformationDepth() > this.getMaxTransformationDepth()) {
      setMaxTransformationDepth(newParams.getMaxTransformationDepth());
    }
    if (newParams.getMaxTime() == null || newParams.getMaxTime() > this.getMaxTime()) {
      setMaxTime(newParams.getMaxTime());
    }
    if (newParams.getMaxAnswerCount() == null
            || newParams.getMaxAnswerCount() > this.getMaxAnswerCount()) {
      setMaxAnswerCount(newParams.getMaxAnswerCount());
    }
    //@TODO -- Add more as needed.
  }
  
  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InferenceParametersMap other = (InferenceParametersMap) obj;
    if (this.map != other.map && (this.map == null || !this.map.equals(other.map))) {
      return false;
    }
    return true;
  }
  
  public boolean equalsByValue(InferenceParameters other) {
    if (other == null) {
      return false;
    }
    return getMap().entrySet().equals(other.entrySet());
    //return map.equals(((SpecifiedInferenceParameters) other).getMap());
  }
  
  
  // Protected
  
  protected Map<String, Object> getMap() {
    return Collections.unmodifiableMap(map);
  }
  
  protected <T extends Enum> T getAs(String key, Class<T> type) {
    return coerceToEnum(get(key), type);
  }
  
}
