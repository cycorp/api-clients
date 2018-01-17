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
 * File: Interfaces.java
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author nwinant
 */
public class Interfaces {
  
  public static interface ObjectConverter<E, T> {
    T convert(E object);
  }
  
  public static interface ListProcessor<E, T> {
    List<T> process(Collection<E> collection);
  }
  
  public static interface ListProcessorWithArg<E, T, A> {
    List<T> process(Collection<E> collection, A arg);
  }
  
  public static interface ListProcessorWith2Args<E, T, A1, A2> {
    List<T> process(Collection<E> collection, A1 arg1, A2 arg2);
  }
  
  public static interface ListProcessorWith3Arg<E, T, A1, A2, A3> {
    List<T> process(Collection<E> collection, A1 arg1, A2 arg2, A3 arg3);
  }
  
  public static interface SetProcessor<E, T> {
    Set<T> process(Collection<E> collection);
  }
  
  public static interface SetProcessorWithArg<E, T, A> {
    Set<T> process(Collection<E> collection, A arg);
  }
  
  public static interface SetProcessorWith2Args<E, T, A1, A2> {
    Set<T> process(Collection<E> collection, A1 arg1, A2 arg2);
  }
  
  public static interface SetProcessorWith3Args<E, T, A1, A2, A3> {
    Set<T> process(Collection<E> collection, A1 arg1, A2 arg2, A3 arg3);
  }
  
  public static interface CollectionSorter<E> extends Comparator<E> {
    List<E> sort(Collection<E> collection);
  }
  
}
