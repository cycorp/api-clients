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

package com.cyc.base.kbtool;

/*
 * #%L
 * File: KeTextTool.java
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

import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * 
 * 
 * @author nwinant
 */
public interface KeTextTool {

  void loadKeTextFile(Path path, Charset charset)
          throws IOException, CycConnectionException, CycApiException;
  
  void loadKeTextFile(Path path)
          throws IOException, CycConnectionException, CycApiException;
  
  void loadKeTextFile(String pathString)
          throws IOException, CycConnectionException, CycApiException;
  
}
