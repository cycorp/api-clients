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

package com.cyc.baseclient.subl.subtypes;

/*
 * #%L
 * File: BasicSublFunction.java
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
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.subl.SublFunction;
import com.cyc.baseclient.subl.SublSourceFile;
import static com.cyc.baseclient.subl.functions.SublFunctions.FBOUNDP;
import java.io.File;


/**
 * Provides a basic, common implementation of SublFunction. As with SublFunction, 
 * BasicSublFunction does not provide a method for <em>evaluating</em> the SubL function, but there
 * are classes which inherit from this one to provide such methods.
 * 
 * <p>Note that this class also provides all of the methods necessary to satisfy the 
 * {@link com.cyc.baseclient.subl.SublSourceFile} interface. A class which extends this class and
 * implements SublSourceFile can have its sources located and loaded by 
 * {@link com.cyc.baseclient.subl.SublResourceLoader} with no additional work, provided that the
 * source file adheres to the conventions described in {@link #getSourceFilePath() }.
 * 
 * @author nwinant
 */
 public class BasicSublFunction extends DefaultCycObjectImpl implements SublFunction {
  
  // Fields
  
  public static final String BASE_DEFAULT_SOURCE_PATH = "subl" + File.separatorChar + "api-patches";
  private final CycSymbol symbol;
  
  // Constructors
  
  /**
   * Creates an instance of BasicSublFunction bound to a particular symbol, via a string 
   * representation of that symbol.
   * 
   * @param name
   */
  public BasicSublFunction(String name) {
    this.symbol = makeCycSymbol(name);
  }
  
  
  // Public
  
  
  /**
   * The CycSymbol which denotes the function.
   * 
   * @return the symbol denoting the function.
   */
  @Override
  public CycSymbol getSymbol() {
    return this.symbol;
  }
  
  /**
   * Checks whether a function is bound to the symbol on a particular Cyc server.
   * 
   * @param access the Cyc server to check
   * @return whether a function is bound to the symbol
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  @Override
  public boolean isBound(CycAccess access) throws CycApiException, CycConnectionException {
    return FBOUNDP.eval(access, this);
  }
  
  /**
   * Determines whether the function is required to be in present in a particular Cyc server. This
   * implementation simply assumes that the function is required. Inheriting classes may wish to 
   * override this method with more specific behavior.
   * 
   * @param access the Cyc server to check
   * @return whether the function is required to be present
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  @Override
  public boolean isRequired(CycAccess access) throws CycApiException, CycConnectionException {
    return true;
  }
  
  
  // Methods supporting SublSourceFile
  
  /**
   * Provides an implementation of {@link SublSourceFile#getSourceFilePath() }. Returns the path of
   * the source file. 
   * 
   * <p><strong>Source file conventions:</strong> The filename is assumed to be the function's
   * {@link #toString()} value, lower-cased, with a ".lisp" suffix. The file is assumed to be 
   * located within a directory named "subl" in the root of the classpath. E.g., for a function with
   * the symbol CATEGORIZE-TERM-WRT-API, the file would be expected at
   * <code>subl/categorize-term-wrt-api.lisp</code>.
   * 
   * <p>Note that this method will only provide a non-null result if the inheriting class implements 
   * {@link com.cyc.baseclient.subl.SublSourceFile}.
   * 
   * @return the path of the source file.
   */
  public String getSourceFilePath() {
    if (this instanceof SublSourceFile) {
      return BASE_DEFAULT_SOURCE_PATH + File.separatorChar + toString().toLowerCase() + ".lisp";
    }
    return null;
  }
  
  /**
   * Provides an implementation of {@link SublSourceFile#isMissing(com.cyc.base.CycAccess) }. Checks
   * whether a particular Cyc server already has the contents of this source file. Be
   * aware that this is currently implemented as a simple spot-check: it only checks
   * {@link #isBound(com.cyc.base.CycAccess) }.
   * 
   * @param access the Cyc server to check
   * @return whether the Cyc server already has the contents of this source file
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  public boolean isMissing(CycAccess access) throws CycApiException, CycConnectionException {
    return !this.isBound(access);
  }
  
  @Override
  public String toString() {
    return this.getSymbol().toString();
  }
  
  @Override
  public int compareTo(Object o) {
    return (o != null) ? toString().compareTo(o.toString()) : 1;
  }

}
