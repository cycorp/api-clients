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
package com.cyc.query.client.graph;

/*
 * #%L
 * File: AbstractGraphNodePathImpl.java
 * Project: Query Client
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

import com.cyc.query.graph.GraphNodeAbsolutePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author nwinant
 */
abstract public class AbstractGraphNodePathImpl<V, T extends GraphNodeAbsolutePath<V, T>> implements GraphNodeAbsolutePath<V, T> {

  // Fields
  
  private final List<V> path;
  private final T parent;
  private final V id;
  
  
  // Construction
  
  public AbstractGraphNodePathImpl(T parent, V id) {
    this.parent = parent;
    this.id = id;

    final List<V> ancestorIds = new ArrayList();
    if (parent != null) {
      ancestorIds.addAll(parent.toList());
    }
    ancestorIds.add(this.id);
    this.path = Collections.unmodifiableList(ancestorIds);
  }
  
  
  // Public

  @Override
  public V getNodeId() {
    return this.id;
  }

  @Override
  public T getParentPath() {
    return this.parent;
  }

  @Override
  public boolean hasParent() {
    return parent != null;
  }

  @Override
  public int compareTo(T obj) {
    if (obj == null) {
      return 1;
    }
    return toPaddedString(10).compareTo(obj.toPaddedString(10));
  }

  @Override
  public List<V> toList() {
    return this.path;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 83 * hash + Objects.hashCode(this.parent);
    hash = 83 * hash + Objects.hashCode(this.id);
    return hash;
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
    return this.toList().equals(((GraphNodeAbsolutePath) obj).toList());
  }
  
  @Override
  public String toPaddedString(int padding) {
    final StringBuilder sb = new StringBuilder();
    final String separator = getPathSeparator();
    if (hasParent()) {
      sb.append(getParentPath().toPaddedString(padding));
    }
    if (sb.length() > 0) {
      sb.append(separator);
    }
    return sb.append(getNodeIdString(padding)).toString();
  }
  
  @Override
  public String toString() {
    return toPaddedString(1);
  }
  
  
  // Protected
  
  protected String getPathSeparator() {
    return ".";
  }
  
  protected String getNodeIdString(int padding) {
    return String.format("%0" + padding + "d", getNodeId());
  }
  
}
