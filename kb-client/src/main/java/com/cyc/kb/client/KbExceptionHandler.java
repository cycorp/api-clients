package com.cyc.kb.client;

import com.cyc.base.exception.CycConnectionException;
import com.cyc.kb.exception.KbRuntimeException;
import java.io.IOException;
import java.net.UnknownHostException;

/*
 * #%L
 * File: KbExceptionHandler.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc
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
 * Root class for runtime exceptions thrown by the KB API.
 *
 * For now, all {@link UnknownHostException}s and {@link IOException}s from
 * OpenCyc API will be wrapped with KBApiRuntimeException. This is because the
 * KB API user is not expected to handle such exceptions.
 *
 * @author Vijay Raj
 * @version $Id: KBApiRuntimeException.java 151668 2014-06-03 21:46:52Z jmoszko
 $
 */
public class KbExceptionHandler {
  
  
  // Static methods
  
  /**
   * Check if a Throwable is caused by CycConnectionException; if so, re-throw it.
   * 
   * There's really no point in swallowing exceptions caused by CycConnectionExceptions;
   * such situations are really outside of the purview of the KB API. This method
   * makes it easy to ensure that they bubble up: just call it on a Throwable, and
   * if a CycConnectionException is the underlying cause; the Throwable will be
   * rethrown as a KBApiRuntimeException.
   *
   * If the Throwable is a KBApiRuntimeException, it will simply be re-thrown. If
   * not, it will be wrapped in a new KBApiRuntimeException.
   * 
   * @param thrown
   * @throws KbRuntimeException 
   */
  public static void rethrowIfCycConnectionException(final Throwable thrown) throws KbRuntimeException {
    if (CycConnectionException.isUnderlyingCause(thrown)) {
      if (thrown instanceof KbRuntimeException) {
        throw (KbRuntimeException) thrown;
      } else {
        throw KbRuntimeException.fromThrowable(thrown);
      }
    }
  }
  
}
