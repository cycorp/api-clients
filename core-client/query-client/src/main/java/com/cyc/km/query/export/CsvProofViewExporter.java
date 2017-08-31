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
package com.cyc.km.query.export;

/*
 * #%L
 * File: CsvProofViewExporter.java
 * Project: Query Client
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

import com.cyc.baseclient.export.PrintWriterExporter;
import com.cyc.query.explanations.ProofViewImpl;
import com.cyc.query.ProofView;
import com.cyc.query.ProofViewNode;
import com.cyc.query.ProofViewNode.ProofViewNodePath;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import net.htmlparser.jericho.Source;
//import net.htmlparser.jericho.TextExtractor;

/**
 * Converts a ProofView into a CSV file.
 * 
 * <p>The specification for the csv format (or rather, documentation of the de facto specification) 
 * can be found here: https://tools.ietf.org/html/rfc4180
 * 
 * @author nwinant
 */
//public class CsvProofViewExporter extends PrintWriterExporter<ProofView> {
public class CsvProofViewExporter {

  // Static fields
  
  private static final CsvProofViewExporter ME = new CsvProofViewExporter();
  private static final Logger LOGGER = LoggerFactory.getLogger(CsvProofViewExporter.class);
  private static final char DEFAULT_SEPARATOR = ',';
  private static final char DEFAULT_QUOTE = '"';
    
  
  // Static methods
  
  public static void toCsv(final ProofView proofView, final PrintStream out) throws Exception {
    ME.printCsv(proofView, out);
  }
  
  
  // Fields
  
  private final char separator;
  private final char quote;
  private final boolean includeHeaderRow;
  
  
  // Construction
  
  public CsvProofViewExporter(boolean includeHeaderRow) {
    this.includeHeaderRow = includeHeaderRow;
    this.separator = DEFAULT_SEPARATOR;
    this.quote = DEFAULT_QUOTE;
  }
  
  public CsvProofViewExporter() {
    this(true);
  }
  
  
  // Public
  
  public void printCsv(ProofView proofView, final PrintStream out) throws Exception {
    final Map<ProofViewNodePath, ProofViewNode> map = ((ProofViewImpl) proofView).toMap();
    final List<ProofViewNodePath> nodeIds = new ArrayList(map.keySet());
    final int padding = String.valueOf(nodeIds.size()).length();
    Collections.sort(nodeIds);
    
    if (includeHeaderRow) {
      out.println(headerLine());
    }
    
    for (ProofViewNodePath key : nodeIds) {
      out.println(nodeToCsvLine(key.toPaddedString(padding), map.get(key)));
    }
  }
  
  public String escapeString(String value) {
    if (value == null) {
      return value;
    }
    if (value.contains("" + quote)) {
      return value.replace("" + quote, quote + "" + quote);
    }
    return value;
  }
  
  public String formatLineWithSeparator(char separator, String... values) {
    final StringBuilder sb = new StringBuilder();
    for (String value : values) {
      if (sb.length() > 0) {
        sb.append(separator);
      }
      sb.append(quote).append(escapeString(value)).append(quote);
    }
    return sb.toString();
  }
  
  public String formatLine(String... values) {
    return formatLineWithSeparator(separator, values);
  }
  
  public String headerLine() {
    return formatLine(
            "Node path",
            "Expanded initially?",
            "Label",
            "HTML",
            "CycL"
    );
  }
  
  public String nodeToCsvLine(String path, ProofViewNode node) {
    final String label = getInheritedLabel(node);
    final String html = node.getHTML();
    final String cycl = node.getCyclString();
    final boolean expandInitially = node.isExpandInitially();
    return formatLine(
            cleanString("node-" + path),
            cleanBoolean(expandInitially),
            cleanString(label),
            cleanHtml(html),
            cleanString(cycl)
    );
  }
  
  public String getInheritedLabel(ProofViewNode node) {
    final String label = node.getLabel();
    if ((label != null) && !label.trim().isEmpty()) {
      return label;
    }
    if (node.getParent() != null) {
      return getInheritedLabel(node.getParent());
    }
    return null;
  }
  
  public String cleanString(String string) {
    if (string == null) {
      return "";
    }
    return string.trim();
  }
  
  public String cleanHtml(String string) {
    //return cleanString(new TextExtractor(new Source(string)).toString());
    return cleanString(string);
  }
  
  public String cleanBoolean(boolean obj) {
    return cleanString(String.valueOf(obj)).toLowerCase();
  }

}
