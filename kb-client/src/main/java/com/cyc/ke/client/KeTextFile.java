/*
 * Copyright 2017 Cycorp, Inc.
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

package com.cyc.ke.client;

/*
 * #%L
 * File: KeTextFile.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc
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

import com.cyc.Cyc;
import com.cyc.base.CycAccess;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbStatus;
import com.cyc.kb.KbTerm;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author nwinant
 */
public class KeTextFile {
  
  //====|    Static methods    |==================================================================//
  
  public static final Charset DEFAULT_KE_TEXT_CHARSET = StandardCharsets.UTF_8;
  
  public static KeTextFile get(Path path, Charset charset) throws IOException {
    return new KeTextFile(path, charset);
  }
  
  public static KeTextFile get(Path path) throws IOException {
    return get(path, DEFAULT_KE_TEXT_CHARSET);
  }
  
  public static KeTextFile get(String pathString, Charset charset) throws IOException {
    return get(Paths.get(pathString), charset);
  }
  
  public static KeTextFile get(String pathString) throws IOException {
    return get(Paths.get(pathString));
  }
  
  /**
   * Loads a single string of KE text into the KB.
   * 
   * @param str KE text string
   * @throws KbException 
   * 
   * @see #loadToKb() 
   * @see #loadToKbUnlessKbContentExists(java.lang.String...) 
   */
  public static void loadKeTextString(String str) throws KbException {
    loadKeTextApiString(DefaultCycObjectImpl.stringApiValue(str));
  }
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(KeTextFile.class);
  private static final String LF = System.lineSeparator();
  private final Path path;
  private final Charset charset;
  private final List<String> lines;
  
  //====|    Construction    |====================================================================//
  
  public KeTextFile(Path path, Charset charset) throws IOException {
    LOG.debug("Reading KE text file from path: {}", path);
    this.path = path;
    this.charset = charset;
    this.lines = Collections.unmodifiableList(Files.readAllLines(this.path, this.charset));
  }
  
  //====|    Public methods    |==================================================================//
  
  public List<String> getLines() {
    return this.lines;
  }
  
  public String getLinesAsString() {
    final StringBuilder sb = new StringBuilder();
    lines.stream().forEachOrdered(line -> {
      sb.append(line).append(LF);
    });
    return sb.toString();
  }
  
  /**
   * Loads the contents of the KE text file into the KB.
   * 
   * @throws KbException 
   * 
   * @see #loadToKbUnlessKbContentExists(java.lang.String...) 
   * @see #loadKeTextString(java.lang.String) 
   */
  public void loadToKb() throws KbException {
    LOG.info("Loading KE text file {}", path);
    try {
      loadKeTextApiString(DefaultCycObjectImpl.stringApiValue(getLinesAsString()));
    } catch (KbException ex) {
      throw KbException.fromThrowable("Error attempting to load contents of " + path, ex.getCause());
    } 
  }
  
  /**
   * Loads the contents of the KE text file into the KB, but only if all terms and/or assertions in
   * {@code namesOrIds} do not already exist in the KB. This method returns all of the names/IDs 
   * from that list which it has found in the KB; if empty, it will have attempted to load the KE 
   * text file.
   *
   * @param namesOrIds names or IDs of all terms/assertions which must not already exist in the KB
   *
   * @return list of all terms/assertions that already exist in the KB
   *
   * @throws KbException
   * 
   * @see #loadToKb() 
   * @see #loadKeTextString(java.lang.String) 
   */
  public List<String> loadToKbUnlessKbContentExists(String... namesOrIds) throws KbException {
    final List<String> preexistingTerms = new ArrayList();
    for (String nameOrId : namesOrIds) {
      if (Cyc.existsInKb(nameOrId)) {
        preexistingTerms.add(nameOrId);
      } else if (KbStatus.EXISTS_WITH_TYPE_CONFLICT.equals(KbTerm.getStatus(nameOrId))) {
        final String kbObjType = KbObject.get(nameOrId).getClass().getSimpleName();
        throw new KbTypeException(nameOrId + " is a " + kbObjType + ", not a term");
      }
    }
    if (preexistingTerms.isEmpty()) {
      loadToKb();
    } else {
      LOG.info("Will not load {}; found {} of {} pre-existing term(s) in KB: {}",
              path, preexistingTerms.size(), namesOrIds.length, preexistingTerms);
    }
    return preexistingTerms;
  }
  
  //====|    Internal methods    |================================================================//
  
  /**
   * Loads a single string of KE text into the KB, which must already have been converted to its
   * string API value.
   * 
   * @param apiString
   * 
   * @throws KbException 
   * 
   * @see #loadKeTextString(java.lang.String) 
   * @see #loadToKb() 
   */
  private static void loadKeTextApiString(String apiString) throws KbException {
    try {
      CycAccess.getCurrent().converse()
              .converseVoid("(load-ke-text-string " + apiString + " :now)");
    } catch (CycConnectionException | CycApiException ex) {
      throw KbException.fromThrowable("Error attempting to load KE text string: " + apiString, ex);
    }
  }
  
}
