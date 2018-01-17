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
 * File: SublResourceLoader.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cyc.session.CycAddress;

/**
 * This class is designed to check a Cyc server for a set of SubL source files and load any which
 * are missing from it.
 * 
 * @author nwinant
 */
public class SublResourceLoader {
  
  // Fields
  
  static final private Logger LOGGER = LoggerFactory.getLogger(SublResourceLoader.class);
  final private CycAccess access;

  
  // Constructor
  
  /**
   * Create an instance of SubLResourceLoader for a particular Cyc server.
   * @param access 
   */
  public SublResourceLoader(CycAccess access) {
    this.access = access;
  }
  
  
  // Public
  
  /**
   * Loads a particular SublSourceFile into the Cyc server.
   * 
   * @param resource
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  public void loadResource(SublSourceFile resource) throws CycApiException, CycConnectionException {
    LOGGER.info(access.getCycAddress() + ": loading SubL resource for " + resource);
    final String source = readSourceFile(resource);
    access.converse().converseVoid(source);
  }
  
  /**
   * For a list of SublSourceFiles, load any which are missing from the Cyc server. Note that this 
   * function will evaluate the SublSourceFiles in the order in which they appear in the List
   * argument.
   * 
   * @param resources the ordered list of resources to load, if missing.
   * @return a list of the SublSourceFiles which were loaded by the method.
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  public List<SublSourceFile> loadMissingResources(List<SublSourceFile> resources) throws CycApiException, CycConnectionException {
    final List<SublSourceFile> missing = findMissingResources(resources);
    final List<SublSourceFile> loaded = new ArrayList();
    final CycAddress server = access.getCycAddress();
    if (missing.isEmpty()) {
      LOGGER.info("All expected resources are present on " + server);
    } else {
      LOGGER.info("The following " + missing.size() + " resources are missing from " + server + ":");
      for (SublSourceFile resource : missing) {
        LOGGER.info("  " + server + " is missing " + resource);
      }
      LOGGER.warn("Attempting to load missing resources into " + server + "...");
      for (SublSourceFile resource : missing) {
        loadResource(resource);
        loaded.add(resource);
      }
    }
    return loaded;
  }
  
  /**
   * For a list of SublSourceFiles, determine which are missing from the Cyc server. This method 
   * will <em>not</em> modify the Cyc server.
   * 
   * @param resources
   * @return a list of all missing source files
   * @throws CycApiException
   * @throws CycConnectionException 
   */
  public List<SublSourceFile> findMissingResources(List<SublSourceFile> resources) throws CycApiException, CycConnectionException {
    final List<SublSourceFile> missing = new ArrayList();
    for (SublSourceFile resource : resources) {
      if (resource.isMissing(access)) {
        missing.add(resource);
      }
    }
    return missing;
  }
  
  public List<SublSourceFile> findMissingRequiredResources(List<SublSourceFile> resources) throws CycApiException, CycConnectionException {
    final List<SublSourceFile> missing = new ArrayList();
    for (SublSourceFile resource : findMissingResources(resources)) {
      if (resource.isRequired(access)) {
        missing.add(resource);
      }
    }
    return missing;
  }
  
  
  // Static
  
  /**
   * Reads the contents of the source file described by {@link SublSourceFile#getSourceFilePath() }.
   * 
   * @param resource
   * @return the contents of the source file
   */
  public static String readSourceFile(SublSourceFile resource) {
    final StringBuilder sb = new StringBuilder();
    IOException tmpIOException = null;
    InputStream inStream = null;
    InputStreamReader inReader = null;
    BufferedReader buffReader = null;
    try {
      inStream = SublResourceLoader.class.getClassLoader().getResourceAsStream(resource.getSourceFilePath());
      if (inStream == null) {
        handleResourceNotFound(resource);
      }
      inReader = new InputStreamReader(inStream, StandardCharsets.UTF_8);
      buffReader = new BufferedReader(inReader);
      String line;
      while ((line = buffReader.readLine()) != null) {
        sb.append(line).append(System.lineSeparator());
      }
    } catch (IOException io) {
      tmpIOException = io;
      io.printStackTrace(System.err);
    } finally {
      try {
        if (buffReader != null) {
          buffReader.close();
        }
        if (inReader != null) {
          inReader.close();
        }
        if (inStream != null) {
          inStream.close();
        }
      } catch (IOException io) {
        io.printStackTrace(System.err);
        tmpIOException = io;
      }
      if (tmpIOException != null) {
        handleResourceNotFound(resource, tmpIOException);
      }
    }
    return sb.toString();
  }
  
  /**
   * Handles a missing resource by throwing an exception.
   * 
   * @param resource
   * @param ex 
   */
  private static void handleResourceNotFound(SublSourceFile resource, Exception ex) {
    String msg = "Could not find source file for function " + resource + " at path: " + resource.getSourceFilePath();
    if (ex != null) {
      throw new BaseClientRuntimeException(msg, ex);
    }
    throw new BaseClientRuntimeException(msg);
  }
  
  /**
   * Handles a missing resource by throwing an exception.
   * 
   * @param resource 
   */
  private static void handleResourceNotFound(SublSourceFile resource) {
    handleResourceNotFound(resource, null);
  }
}
