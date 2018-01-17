/*
 * Copyright 2015 Cycorp, Inc..
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

package com.cyc.baseclient.subl;

/*
 * #%L
 * File: SublFunction.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;

/**
 * A Java representation of a Subl function. 
 * 
 * <p>Note that this interface does not currently define a method for <em>evaluating</em> the 
 * function. Because there can be a lot of variability to how particular Subl functions may be 
 * evaluated, this is left to the implementation. However, there are a number of classes which 
 * extend {@link com.cyc.baseclient.subl.subtypes.BasicSublFunction} to provide evaluation
 * methods.
 * 
 * @author nwinant
 */
public interface SublFunction extends CycObject {
  
  /**
   * The CycSymbol which denotes the function.
   * 
   * @return the symbol denoting the function.
   */
  CycSymbol getSymbol();
  
  /**
   * Checks whether a function is bound to the symbol returned by {@link #getSymbol() } for a 
   * particular Cyc server.
   * 
   * @param cyc the Cyc server to check
   * @return whether a function is bound to the symbol
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  boolean isBound(CycAccess cyc) throws CycApiException, CycConnectionException;
  
  /**
   * Determines whether the function is required to be in present in a particular Cyc server.
   * 
   * @param cyc the Cyc server to check
   * @return whether the function is required to be present
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  boolean isRequired(CycAccess cyc) throws CycApiException, CycConnectionException;
  
  
  // TODO (maybe): If it becomes necessary, we could add SublFunction sub-types to describe their  
  // expected return values; instances of SublFunction which can have multiple return types could
  // implement multiple interfaces. A general approach might look something like the following...
  // - nwinant, 2015-05-22
  /*
  public interface SublBooleanFunction<T extends Object> extends SublFunction {
    boolean evalBoolean(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublCycObjectFunction<T extends Object> extends SublFunction {
    CycObject evalCycObject(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublIntFunction<T extends Object> extends SublFunction {
    int evalInt(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublListFunction<T extends Object> extends SublFunction {
    CycList evalList(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublObjectFunction<T extends Object> extends SublFunction {
    Object evalObject(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublSentenceFunction<T extends Object> extends SublFunction {
    FormulaSentence evalSentence(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublStringFunction<T extends Object> extends SublFunction {
    String evalString(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  
  public interface SublVoidFunction<T extends Object> extends SublFunction {
    void evalVoid(CycAccess access, T... args) throws CycConnectionException, CycApiException;
  }
  */
}
