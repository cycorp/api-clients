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
 * File: AbstractReversibleList.java
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

import com.google.common.collect.ForwardingList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An abstract List implementation which provides methods for accessing elements from the end of the
 * list. Extending classes must implement {@link #delegate() }; see {@link ForwardingList} for
 * more details. This class is not thread-safe.
 *
 * @author nwinant
 * @param <E>
 */
public abstract class AbstractReversibleList<E> extends ForwardingList<E> implements List<E> {
  
  //====|    Fields    |==========================================================================//
  
  private static final int FIRST_ELEMENT = 0;
  
  //====|    Construction    |====================================================================//
  
  protected AbstractReversibleList() {
  }
  
  //====|    Public methods    |==================================================================//
  
  public void addToEnd(E element) {
    addToEnd(FIRST_ELEMENT, element);
  }
  
  public void addToEnd(int reverseIndex, E element) {
    add(reverse(reverseIndex), element);
  }
  
  public boolean addAllToEnd(int reverseIndex, Collection<? extends E> elements) {
    return addAll(reverse(reverseIndex), elements);
  }
  
  public boolean addAllToEndReversed(int reverseIndex, Collection<? extends E> elements) {
    return addAllToEnd(reverseIndex, reverse(elements));
  }
  
  public E getFirst() {
    return get(FIRST_ELEMENT);
  }
  
  public E getLast() {
    return getLast(FIRST_ELEMENT);
  }
  
  public E getLast(int reverseIndex) {
    return get(reverse(reverseIndex));
  }
  
  public List<E> subListFromEnd(int fromReverseIndex, int toReverseIndex) {
    return subList(reverse(toReverseIndex), reverse(fromReverseIndex));
  }
  
  public List<E> subListFromEndReversed(int fromReverseIndex, int toReverseIndex) {
    return reverse(subListFromEnd(fromReverseIndex, toReverseIndex));
  }
  
  public E removeFromEnd(int reverseIndex) {
    return remove(reverse(reverseIndex));
  }
  
  public E removeFromEnd(Object o) {
    return remove(lastIndexOf(o));
  }
  
  public List<E> toReversedList() {
    return reverse(this);
  }
  
  public E setFromEnd(int reverseIndex, E element) {
    return set(reverse(reverseIndex), element);
  }
  
  //====|    Internal methods    |================================================================//
  
  /**
   * Reverses an index wrt the elements in this list; e.g., index 0 becomes the index of the last 
   * element in the list.
   * 
   * @param index
   * @return 
   */
  protected int reverse(int index) {
    return size() - 1 - index;
  }
  
  /**
   * Returns a new List containing the elements from the original collection in reversed order.
   * 
   * @param elements
   * @return 
   */
  protected List<E> reverse(Collection<? extends E> elements) {
    final List<E> result = new ArrayList(elements);
    Collections.reverse(result);
    return result;
  }
  
}
