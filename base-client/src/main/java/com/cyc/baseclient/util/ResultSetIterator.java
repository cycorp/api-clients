package com.cyc.baseclient.util;

/*
 * #%L
 * File: ResultSetIterator.java
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ResultSetIterator implements Iterator<List<Object>> {
  
  private ResultSet result;
  private int columnCount;
  private boolean hasNext;
  private int currentRowIndex;
  
  public ResultSetIterator(ResultSet result) throws SQLException {
    this.result = result;
    ResultSetMetaData meta = result.getMetaData();
    columnCount = meta.getColumnCount();
    hasNext = result.next();
    currentRowIndex = 1;
  }
  
  @Override
  public boolean hasNext() {
    return hasNext;
  }
  
  @Override
  public List<Object> next() {
    if (!hasNext) {
      throw new NoSuchElementException();
    }
    try {
      List<Object> next = new ArrayList<>();
      
      for (int i = 1; i <= columnCount; i++) {
        Object value = result.getObject(i);
        next.add(value);
      }
      
      hasNext = result.next();
      currentRowIndex++;
      return next;
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Retrieves the current row number.
   * @return 1-based current row number
   */
  public int getCurrentRowIndex() {
    return currentRowIndex;
  }
  
  public int getColumnCount() {
    return columnCount;
  }
  
  public void closeResultSet() throws SQLException {
    result.close();
  }
  
  @Override
  public void remove() {
    throw new UnsupportedOperationException("ResultSetIterator does not support this operation.");
  }
}
