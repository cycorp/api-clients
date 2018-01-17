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
package com.cyc.baseclient.ui;

/*
 * #%L
 * File: SublInteractionResult.java
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

import com.cyc.base.cycobject.CycList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import static com.cyc.baseclient.datatype.CycStringUtils.prependLines;
import static com.cyc.baseclient.datatype.CycStringUtils.toPrettyMessage;
import static org.apache.commons.lang3.StringUtils.LF;
import static org.apache.commons.lang3.StringUtils.leftPad;

public class SublInteractionResult {

  private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
  private static long currId = 0;
  private final long id;
  private final long timestamp;
  private final String command;
  private final List result;
  private final Exception ex;

  private SublInteractionResult(String command, List result, Exception ex) {
    this.id = currId++;
    this.timestamp = System.currentTimeMillis();
    this.command = command;
    this.result = result;
    this.ex = ex;

  }

  public SublInteractionResult(String command, List result) {
    this(command, result, null);
  }

  public SublInteractionResult(String command, Exception ex) {
    this(command, null, ex);
  }

  public long getId() {
    return id;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getCommand() {
    return command;
  }

  public List getResult() {
    return result;
  }

  public Exception getError() {
    return ex;
  }

  public boolean isSuccessful() {
    return ex == null;
  }

  public String toPrettyString(final int wrapLength) {
    final String prefix = DF.format(new Date(timestamp))
                                  + " " + leftPad("#" + id, String.valueOf(currId).length() + 1);
    final String indent = leftPad("", prefix.length());
    final String resultStr;
    final String exStr;
    if (result != null) {
      final String firstIndent = indent + "> ";
      final String restIndent = indent + "  ";
      final boolean wrap = result.toString().length() > wrapLength;
      resultStr
              = (wrap && result instanceof CycList)
                        ? (((CycList) result)
                                   .toPrettyString(restIndent)
                                   .replaceFirst(restIndent, firstIndent))
                        : firstIndent + result;
    } else {
      resultStr = "";
    }
    if (ex != null) {
      exStr = StringUtils.join(prependLines(indent, toPrettyMessage(ex, wrapLength)), "\n");
    } else {
      exStr = "";
    }
    return prefix + ": " + command + LF + resultStr + exStr;
  }

  public String toPrettyString() {
    return toPrettyString(140);
  }

  @Override
  public String toString() {
    return toPrettyString();
  }

}
