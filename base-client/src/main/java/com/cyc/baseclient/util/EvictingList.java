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
package com.cyc.baseclient.util;

/*
 * #%L
 * File: EvictingList.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A List of a fixed size whose salient property is that, once full, it will automatically evict the
 * first element when a new element is added. This class also provides methods for accessing 
 * elements from the end of the list. This class is not thread-safe.
 *
 * @author nwinant
 * @param <E>
 */
public class EvictingList<E> extends AbstractReversibleList<E> implements List<E> {
  
  //====|    InternalArrayList    |===============================================================//
  
  private static class InternalArrayList<E> extends ArrayList<E> {
    
    private final int capacity;
    
    public InternalArrayList(int capacity) {
      super(capacity);
      this.capacity = capacity;
    }
    
    public boolean ensureFreeSpace(int requiredSpace) {
      if (capacity == EMPTY_LIST) {
        return false;
      }
      if (size() + requiredSpace >= capacity) {
        removeRange(FIRST_ELEMENT, size() - capacity + requiredSpace);
      }
      return true;
    }
    
    public int getCapacity() {
      return this.capacity;
    }
    
    @Override
    public void ensureCapacity(int minCapacity) {
      throw new UnsupportedOperationException(
              "The size of this list is fixed and cannot be changed.");
    }
    
  }
  
  //====|    Fields    |==========================================================================//
  
  private static final int EMPTY_LIST = 0;
  private static final int FIRST_ELEMENT = 0;
  private static final int SINGLE_ELEMENT = 1;
  
  private final InternalArrayList<E> list;
  
  //====|    Construction    |====================================================================//
  
  public EvictingList(int fixedCapacity) {
    this.list = new InternalArrayList<>(fixedCapacity);
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public boolean add(E element) {
    if (!ensureFreeSpace(SINGLE_ELEMENT)) {
      return false;
    }
    return super.add(element);
  }
  
  @Override
  public void add(int index, E element) {
    if (!ensureFreeSpace(SINGLE_ELEMENT)) {
      return;
    }
    super.add(index, element);
  }
  
  @Override
  public boolean addAll(Collection<? extends E> elements) {
    if (!ensureFreeSpace(elements.size())) {
      return false;
    }
    return super.addAll(elements);
  }
  
  @Override
  public boolean addAll(int index, Collection<? extends E> elements) {
    if (!ensureFreeSpace(elements.size())) {
      return false;
    }
    return super.addAll(index, elements);
  }
  
  public int getCapacity() {
    return list.getCapacity();
  }
  
  //====|    Internal methods    |================================================================//
  
  protected boolean ensureFreeSpace(int requiredSpace) {
    return list.ensureFreeSpace(requiredSpace);
  }
  
  @Override
  protected List<E> delegate() {
    return list;
  }
  
}
