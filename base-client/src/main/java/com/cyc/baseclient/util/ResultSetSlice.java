package com.cyc.baseclient.util;

/*
 * #%L
 * File: ResultSetSlice.java
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

//  IMPORT_NON_CYCORP_PACKAGES

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*****************************************************************************
 * A Cfaslable slice of a java.sql.ResultSet, used to send SubL portions of
 * a result set.
 *
 * @version $Id: ResultSetSlice.java 176591 2018-01-09 17:27:27Z nwinant $
 * @author
 *
 *      Bjorn Aldag<BR>
 *      Antons Rebguns
 *      Copyright &copy; 2003 - 20017 Cycorp, Inc.  All rights reserved.
 *****************************************************************************/
public class ResultSetSlice {
  
  //// Instance Fields ////////////////////////////////////////////////////////
  
  /**
   * The java.sql.ResultSet underlying this ResultSetSlice.
   */
  private ResultSetIterator rsi;
  
  /**
   * The row id of the first row of this result slice.
   */
  private int startRow;
  
  /**
   * The row id of the last row of this result slice.
   */
  private int lastRow;
  
  private int offset;
  private List<List<Object>> precachedRows;
  
  //// Constructors ///////////////////////////////////////////////////////////
  
  /**
   * Creates a new ResultSetSlice that contains all rows from <code>lo</code> to
   * <code>hi</code> (both inclusive) from the specified ResultSet. The
   * current row of the underlying ResultSet is set to the row immediately
   * before <code>lo</code>.
   *
   * @param rsi      the ResultSet from which a slice is to be retrieved
   * @param startRow the first row to be retrieved
   * @param lastRow  the last row to be retrieved
   * @throws SQLException if an SQL error occurs.
   */
  public ResultSetSlice(ResultSetIterator rsi, int startRow, int lastRow) throws IndexOutOfBoundsException {
    this.rsi = rsi;
    this.startRow = startRow;
    this.lastRow = lastRow;
    
    do {
      precacheNextBatch();
//      System.out.println("hasNext " + rsi.hasNext() + " startRow " + startRow + " offset " + offset + " lastRow " + lastRow);
    } while (rsi.hasNext() && (startRow >= offset + 1 + lastRow));
    
    
    if (rsi.getCurrentRowIndex() < startRow) {
      throw new IndexOutOfBoundsException("Start row is invalid.");
    }
  }
  
  private int precacheNextBatch() {
    int batchSize = lastRow - rsi.getCurrentRowIndex() + 1;
    precachedRows = new ArrayList<>(batchSize);
    offset = rsi.getCurrentRowIndex() - 1;
//    System.out.println("offset: " + offset + ", lastRow: " + lastRow + ", current: " + rsi.getCurrentRowIndex() + ", batch: " + batchSize);
    
    for (int i = 0; i < batchSize && rsi.hasNext(); i++) {
      List<Object> row = rsi.next();
      precachedRows.add(row);
    }

//    System.out.println("# precached: " + precachedRows.size());
    
    int currIndex = rsi.getCurrentRowIndex() - 1;
    return currIndex - offset;
  }
  
  public List<List<Object>> getSliceRows() {
    return Collections.unmodifiableList(precachedRows);
  }
  
  /**
   * Returns the number of rows in this ResultSetSlice, not the total number
   * of rows in the underlying ResultSet.
   *
   * @return the number of rows in this ResultSetSlice, not the total number
   * of rows in the underlying ResultSet.
   */
  public int sliceRowCount() {
    return precachedRows.size();
  }
  
  /**
   * Returns the index of the first row of this ResultSetSlice, with respect
   * to the underlying ResultSet.
   *
   * @return the index of the first row of this ResultSetSlice, with respect
   * to the underlying ResultSet.
   */
  public int first() {
    return offset + 1;
  }
  
  public int getColumnCount() {
    return rsi.getColumnCount();
  }
  
  public boolean hasNext() {
    return rsi.hasNext();
  }
  
  public void closeResultSet() throws SQLException {
    rsi.closeResultSet();
  }
  
  /**
   * Returns the printed representation of this ResultSetSlice.
   *
   * @return the printed representation of this ResultSetSlice.
   */
  public String toString() {
    StringBuilder string = new StringBuilder("(" + first() + " (");
    for (List<Object> row : precachedRows) {
      string.append("(");
      
      for (Object columnValue : row) {
        if (columnValue == null) {
          string.append("NULL");
        } else {
          string.append(columnValue);
        }
        
        string.append(" ");
      }
      
      string.deleteCharAt(string.length() - 1);
      string.append(")");
    }
    
    string.append(") ");
    string.append(precachedRows.size());
    string.append(" \"").append(rsi.hasNext() ? "NOT " : "").append("EXHAUSTED\"");
    string.append(")");
    return string.toString();
  }
}
