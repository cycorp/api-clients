package com.cyc.baseclient.datatype;

/*
 * #%L
 * File: DataTypeUnitTest.java
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

import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Naut;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.baseclient.util.Base64;
import com.cyc.baseclient.util.Log;
import com.cyc.baseclient.util.MyStreamTokenizer;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import org.junit.Test;

import static com.cyc.baseclient.CommonConstants.DAY_FN;
import static com.cyc.baseclient.CommonConstants.HOUR_FN;
import static com.cyc.baseclient.CommonConstants.MINUTE_FN;
import static com.cyc.baseclient.CommonConstants.MONTH_FN;
import static com.cyc.baseclient.CommonConstants.YEAR_FN;
import static org.junit.Assert.*;

/**
 * Provides a suite of JUnit test cases for the <tt>com.cyc.baseclient.constraintsolver</tt> package.<p>
 *
 * @version $Id: UnitTest.java 131054 2010-05-26 18:59:41Z baxter $
 * @author Stephen L. Reed
 */
public class DataTypeUnitTest {

  /**
   * Tests the Base64 methods.
   */
  @Test
  public void testBase64() {
    System.out.println("** testBase64 **");
    CycArrayList request = new CycArrayList();
    request.add(CycObjectFactory.makeCycSymbol("list"));
    request.add(":none");
    request.add(CycObjectFactory.makeCycSymbol(":none"));
    String encodedRequest = null;
    Object response = null;
    Base64 base64 = new Base64();
    try {
      encodedRequest = base64.encodeCycObject(request, 0);
      response = base64.decodeCycObject(encodedRequest, 0);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    assertNotNull(response);
    assertTrue(response instanceof CycArrayList);
    assertEquals(request, (CycArrayList) response);

    request = new CycArrayList();
    request.add(CycObjectFactory.makeCycSymbol("A"));
    request.setDottedElement(CycObjectFactory.makeCycSymbol("B"));
    encodedRequest = null;
    response = null;
    try {
      encodedRequest = base64.encodeCycObject(request, 0);
      response = base64.decodeCycObject(encodedRequest, 0);
    } catch (IOException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    assertNotNull(response);
    assertTrue(response instanceof CycArrayList);
    assertEquals(request, (CycArrayList) response);
    System.out.println("** testBase64 OK **");
  }

  /**
   * Tests the MyStreamTokenizer methods.
   */
  @Test
  public void testMyStreamTokenizer() {
    System.out.println("** testMyStreamTokenizer **");

    final String testString1 = "xyz\n;abc\ndef";

    final StringReader stringReader = new StringReader(testString1);
    final MyStreamTokenizer st = new MyStreamTokenizer(stringReader);
    st.resetSyntax();
    st.ordinaryChar('(');
    st.ordinaryChar(')');
    st.ordinaryChar('\'');
    st.ordinaryChar('`');
    st.ordinaryChar('.');
    st.whitespaceChars(0, ' ');
    st.quoteChar('"');
    st.wordChars('0', '9');
    st.wordChars('a', 'z');
    st.wordChars('A', 'Z');
    st.wordChars(128 + 32, 255);
    st.wordChars('=', '=');
    st.wordChars('+', '+');
    st.wordChars('-', '-');
    st.wordChars('_', '_');
    st.wordChars('<', '<');
    st.wordChars('>', '>');
    st.wordChars('*', '*');
    st.wordChars('/', '/');
    st.wordChars('#', '#');
    st.wordChars(':', ':');
    st.wordChars('!', '!');
    st.wordChars('$', '$');
    st.wordChars('?', '?');
    st.wordChars('%', '%');
    st.wordChars('&', '&');
    st.wordChars('.', '.');
    st.slashSlashComments(false);
    st.slashStarComments(false);
    st.commentChar(';');
    st.wordChars('?', '?');
    st.wordChars('%', '%');
    st.wordChars('&', '&');
    st.eolIsSignificant(false);

    try {
      st.nextToken();
      assertEquals("xyz", st.sval);
      st.nextToken();
      assertEquals("def", st.sval);
    } catch (IOException e) {
      fail(e.getMessage());
    }
    System.out.println("** testMyStreamTokenizer OK **");
  }
  
  /** Test Log println. */
  @Test
  public void testLogPrintln() {
    System.out.println("** testLogPrintln **");
    Log.current.println("test log line");
    System.out.println("** testLogPrintln OK **");
  }
  
  /** Test MoneyConverter class */
  @Test
  public void testMoneyConverter() {
    System.out.println("** testMoneyConverter **");
    final DenotationalTerm currUSD = MoneyConverter.lookupCycCurrencyTerm(Currency.getInstance(
            "USD"));
    final BigDecimal amount = BigDecimal.valueOf(50.25);
    final Naut cycMoney = new NautImpl(currUSD, amount);
    final Money javaMoney = new Money(amount, Currency.getInstance("USD"));
    try {
      assertTrue(MoneyConverter.isCycMoney(cycMoney));
      assertEquals(javaMoney, MoneyConverter.parseCycMoney(cycMoney));
      assertEquals(cycMoney, MoneyConverter.toCycMoney(javaMoney));
      assertEquals(cycMoney, DataType.MONEY.convertJavaToCyc(javaMoney));
      assertEquals(javaMoney, DataType.MONEY.convertCycToJava(cycMoney));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    System.out.println("** testMoneyConverter OK **");
  }

  /** Test DateConverter class */
  @Test
  public void testDateConverter() {
    System.out.println("** testDateConverter **");
    final int year = 2008;
    final int dayOfMonth = 22;
    final int hour = 7;
    final int minute = 3;
    final Naut cycDate = new NautImpl(MINUTE_FN, minute,
            new NautImpl(HOUR_FN, hour,
            new NautImpl(DAY_FN, dayOfMonth,
            new NautImpl(MONTH_FN, DateConverter.APRIL,
            new NautImpl(YEAR_FN, year)))));
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(year, Calendar.APRIL, dayOfMonth, hour, minute);
    final Date javaDate = calendar.getTime();
    try {
      assertTrue(DateConverter.isCycDate(cycDate));
      assertEquals(javaDate, DateConverter.parseCycDate(cycDate));
      assertEquals(cycDate, DateConverter.toCycDate(javaDate));
      assertEquals(cycDate, DataType.DATE.convertJavaToCyc(javaDate));
      assertEquals(javaDate, DataType.DATE.convertCycToJava(cycDate));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    System.out.println("** testDateConverter OK **");
  }
  
}
