package com.cyc.baseclient.exception;

import com.cyc.base.exception.BaseClientRuntimeException;

/*
 * #%L
 * File: CycKbContentException.java
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

/**
 * Class CycKBContentException indicates that there is some problem with
 * the knowledge currently stored in the KB.
 *
 * @version $Id: CycKbContentException.java 169909 2017-01-11 23:21:20Z nwinant $
 * @author Stephen L. Reed
 */

public class CycKbContentException extends BaseClientRuntimeException {
  
  /**
   * Construct a CycApiException object with no specified message.
   */
  public CycKbContentException() {
    super();
  }
  
  /**
   * Construct a CycApiException object with a specified message.
   * @param s a message describing the exception.
   */
  public CycKbContentException(String s) {
    super(s);
  }
  
  /**
   * Construct a CycApiException object with a specified message
   * and throwable.
   * @param s the message string
   * @param cause the throwable that caused this exception
   */
  public CycKbContentException(String s, Throwable cause) {
    super(s, cause);
  }
  
  /**
   * Construct a CycApiException object with a specified throwable.
   * @param cause the throwable that caused this exception
   */
  public CycKbContentException(Throwable cause) {
    super(cause);
  }
}
