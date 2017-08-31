package com.cyc.baseclient.util.query;

/*
 * #%L
 * File: CycQuerySpecification.java
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

import com.cyc.baseclient.cycobject.CycArrayList;

/**
 * @version $Id: CycQuerySpecification.java 169909 2017-01-11 23:21:20Z nwinant $
 * @author  mreimers
 */
public interface CycQuerySpecification extends QuerySpecification {
  
  public CycArrayList getQueryFormula();
}
