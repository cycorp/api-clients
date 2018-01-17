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

package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: KeTextToolImpl.java
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
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.kbtool.KeTextTool;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author nwinant
 */
public class KeTextToolImpl extends AbstractKbTool implements KeTextTool {
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(KeTextToolImpl.class);
  
  //====|    Construction    |====================================================================//
  
  public KeTextToolImpl(CycAccess client) {
    super(client);
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public void loadKeTextFile(Path path, Charset charset)
          throws IOException, CycConnectionException, CycApiException {
    LOG.info("Loading KE text file {}", path);
    final StringBuilder sb = new StringBuilder();
    for (String line : Files.readAllLines(path, charset)) {
      sb.append(line).append(System.lineSeparator());
    }
    final String content = DefaultCycObjectImpl.stringApiValue(sb.toString());
    getConverse().converseVoid("(load-ke-text-string " + content + " :now)");
  }
  
  @Override
  public void loadKeTextFile(Path path)
          throws IOException, CycConnectionException, CycApiException {
    loadKeTextFile(path, StandardCharsets.UTF_8);
  }
  
  @Override
  public void loadKeTextFile(String pathString)
          throws IOException, CycConnectionException, CycApiException {
    loadKeTextFile(Paths.get(pathString));
  }
  
}
