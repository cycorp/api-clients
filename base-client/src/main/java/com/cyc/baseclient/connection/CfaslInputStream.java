package com.cyc.baseclient.connection;

/*
 * #%L
 * File: CfaslInputStream.java
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

import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.ByteArray;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycAssertionImpl;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.CycVariableImpl;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.cycobject.NartImpl;
import com.cyc.baseclient.exception.CfaslInputStreamClosedException;
import com.cyc.baseclient.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CFASL translating input stream.  All Java-native types which have logical sublisp equivalents
 * are translated automatically by this stream.  Classes implementing the CfaslTranslatingObject
 * interface are created using their readObject(false) method.  Other CYC objects, such as
 * binding-lists and formulas, must be explicitly coerced using their static constructors.
 * 
 * @version $Id: CfaslInputStream.java 176591 2018-01-09 17:27:27Z nwinant $
 * @author Stephen L. Reed <p><p><p><p><p>
 */
public class CfaslInputStream extends BufferedInputStream {

  private static final Logger LOGGER = LoggerFactory.getLogger(CfaslInputStream.class);
  
  /** No api trace. */
  public static final int API_TRACE_NONE = 0;
  
  /* * Message-level api trace. * /
  public static final int API_TRACE_MESSAGES = 1;
  
  /* * Detailed api trace. * /
  public static final int API_TRACE_DETAILED = 2;
  */
  
  /** Parameter that, when true, causes a trace of the messages to and from the server. */
  public int trace = API_TRACE_NONE;
  
  /**
   * Parameter that when set true, causes CFASL object errors to be reported back as strings the
   * caller.
   */
  public boolean reportCfaslErrors = false;
  
  /** CFASL code */
  protected static final int CFASL_IMMEDIATE_FIXNUM_CUTOFF = 128;
  /** CFASL code */
  protected static final int CFASL_IMMEDIATE_FIXNUM_OFFSET = 256 - CFASL_IMMEDIATE_FIXNUM_CUTOFF;
  /** CFASL code */
  protected static final int CFASL_P_8BIT_INT = 0;
  /** CFASL code */
  protected static final int CFASL_N_8BIT_INT = 1;
  /** CFASL code */
  protected static final int CFASL_P_16BIT_INT = 2;
  /** CFASL code */
  protected static final int CFASL_N_16BIT_INT = 3;
  /** CFASL code */
  protected static final int CFASL_P_24BIT_INT = 4;
  /** CFASL code */
  protected static final int CFASL_N_24BIT_INT = 5;
  /** CFASL code */
  protected static final int CFASL_P_32BIT_INT = 6;
  /** DCFASL code */
  protected static final int CFASL_N_32BIT_INT = 7;
  /** DCFASL code */
  protected static final int CFASL_P_FLOAT = 8;
  /** CFASL code */
  protected static final int CFASL_N_FLOAT = 9;
  /** CFASL code */
  protected static final int CFASL_KEYWORD = 10;
  /** CFASL code */
  protected static final int CFASL_SYMBOL = 11;
  /** CFASL code */
  protected static final int CFASL_NIL = 12;
  /** CFASL code */
  protected static final int CFASL_LIST = 13;
  /** CFASL code */
  protected static final int CFASL_VECTOR = 14;
  /** CFASL code */
  protected static final int CFASL_STRING = 15;
  /** CFASL code */
  protected static final int CFASL_CHARACTER = 16;
  /** CFASL code */
  protected static final int CFASL_DOTTED = 17;
  /** CFASL code */
  protected static final int CFASL_HASHTABLE = 18;
  /** CFASL code */
  protected static final int CFASL_BTREE_LOW_HIGH = 19;
  /** CFASL code */
  protected static final int CFASL_BTREE_LOW = 20;
  /** CFASL code */
  protected static final int CFASL_BTREE_HIGH = 21;
  /** CFASL code */
  protected static final int CFASL_BTREE_LEAF = 22;
  /** CFASL code */
  protected static final int CFASL_P_BIGNUM = 23;
  /** CFASL code */
  protected static final int CFASL_N_BIGNUM = 24;
  /** CFASL code */
  protected static final int CFASL_LEGACY_GUID = 25;
  /** CFASL code */
  protected static final int CFASL_GUID = 43;
  /** CFASL code */
  protected static final int CFASL_BYTE_VECTOR = 26;
  /** CFASL code */
  protected static final int CFASL_CONSTANT = 30;
  /** CFASL code */
  protected static final int CFASL_NART = 31;
  /** CFASL code */
  protected static final int CFASL_COMPLETE_CONSTANT = 32;
  /** CFASL code */
  protected static final int CFASL_ASSERTION = 33;
  /** CFASL code */
  protected static final int CFASL_ASSERTION_SHELL = 34;
  /** CFASL code */
  protected static final int CFASL_ASSERTION_DEF = 35;
  /** CFASL code */
  protected static final int CFASL_SOURCE = 36;
  /** CFASL code */
  protected static final int CFASL_SOURCE_DEF = 37;
  /** CFASL code */
  protected static final int CFASL_AXIOM = 38;
  /** CFASL code */
  protected static final int CFASL_AXIOM_DEF = 39;
  /** CFASL code */
  protected static final int CFASL_VARIABLE = 40;
  /** CFASL code */
  protected static final int CFASL_INDEX = 41;
  /** CFASL code */
  protected static final int CFASL_COMPLETE_VARIABLE = 42;
  /** CFASL code */
  protected static final int CFASL_SPECIAL_OBJECT = 50;
  /** CFASL code */
  protected static final int CFASL_EXTERNALIZATION = 51;
  /** CFASL code */
  protected static final int CFASL_UNICODE_CHAR = 52;
  /** CFASL code */
  protected static final int CFASL_UNICODE_STRING = 53;
  /** CFASL code */
  protected static final int CFASL_DICTIONARY = 64;
  /** CFASL code */
  protected static final int CFASL_SERVER_DEATH = -1;
  /** CFASL code */
  protected static final int DEFAULT_READ_LIMIT = 8192;
  
  static private final Map<Integer, String> CFASL_OPCODE_DESCRIPTIONS
          = new HashMap<Integer, String>();
  
  /** indicator that the input contains something invalid, for example an invalid constant */
  protected boolean isInvalidObject = false;

  /**
   * Initializes the opcode descriptions used in trace output.
   */
  static {
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_IMMEDIATE_FIXNUM_CUTOFF, "CFASL_IMMEDIATE_FIXNUM_CUTOFF");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_IMMEDIATE_FIXNUM_OFFSET, "CFASL_IMMEDIATE_FIXNUM_OFFSET");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_P_8BIT_INT, "CFASL_P_8BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_N_8BIT_INT, "CFASL_N_8BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_P_16BIT_INT, "CFASL_P_16BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_N_16BIT_INT, "CFASL_N_16BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_P_24BIT_INT, "CFASL_P_24BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_N_24BIT_INT, "CFASL_N_24BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_P_32BIT_INT, "CFASL_P_32BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_N_32BIT_INT, "CFASL_N_32BIT_INT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_P_FLOAT, "CFASL_P_FLOAT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_N_FLOAT, "CFASL_N_FLOAT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_KEYWORD, "CFASL_KEYWORD");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_SYMBOL, "CFASL_SYMBOL");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_NIL, "CFASL_NIL");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_LIST, "CFASL_LIST");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_VECTOR, "CFASL_VECTOR");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_STRING, "CFASL_STRING");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_CHARACTER, "CFASL_CHARACTER");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_DOTTED, "CFASL_DOTTED");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_HASHTABLE, "CFASL_HASHTABLE");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_BTREE_LOW_HIGH, "CFASL_BTREE_LOW_HIGH");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_BTREE_LOW, "CFASL_BTREE_LOW");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_BTREE_HIGH, "CFASL_BTREE_HIGH");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_BTREE_LEAF, "CFASL_BTREE_LEAF");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_P_BIGNUM, "CFASL_P_BIGNUM");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_N_BIGNUM, "CFASL_N_BIGNUM");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_LEGACY_GUID, "CFASL_LEGACY_GUID");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_GUID, "CFASL_GUID");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_BYTE_VECTOR, "CFASL_BYTE_VECTOR");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_CONSTANT, "CFASL_CONSTANT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_NART, "CFASL_NART");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_COMPLETE_CONSTANT, "CFASL_COMPLETE_CONSTANT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_ASSERTION, "CFASL_ASSERTION");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_ASSERTION_SHELL, "CFASL_ASSERTION_SHELL");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_ASSERTION_DEF, "CFASL_ASSERTION_DEF");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_SOURCE, "CFASL_SOURCE");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_SOURCE_DEF, "CFASL_SOURCE_DEF");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_AXIOM, "CFASL_AXIOM");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_AXIOM_DEF, "CFASL_AXIOM_DEF");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_VARIABLE, "CFASL_VARIABLE");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_INDEX, "CFASL_INDEX");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_COMPLETE_VARIABLE, "CFASL_COMPLETE_VARIABLE");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_SPECIAL_OBJECT, "CFASL_SPECIAL_OBJECT");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_EXTERNALIZATION, "CFASL_EXTERNALIZATION");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_UNICODE_CHAR, "CFASL_UNICODE_CHAR");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_UNICODE_STRING, "CFASL_UNICODE_STRING");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_DICTIONARY, "CFASL_DICTIONARY");
    CFASL_OPCODE_DESCRIPTIONS.put(CFASL_SERVER_DEATH, "CFASL_SERVER_DEATH");
  }

  /**
   * Creates a new CfaslInputStream to read data from the specified underlying input stream.
   * 
   * @param in the underlying input stream.
   */
  public CfaslInputStream(InputStream in) {
    super(in, DEFAULT_READ_LIMIT);
    if (Log.current == null) {
      Log.makeLog("cfasl.log");
    }
  }

  /** Gets the indicator that the input contains something invalid, for example an invalid constant.
   *
   * @return the indicator that the input contains something invalid, for example an invalid constant
   */
  public boolean isInvalidObject() {
    return isInvalidObject;
  }

  /** Resets the indicator that the input contains something invalid, for example an invalid constant.  */
  public void resetIsInvalidObject() {
    isInvalidObject = false;
  }

  /**
   * Reads an Object from this CfaslInputStream. Basic Java types are wrapped as appropriate (e.g.
   * ints become Integer objects). New constants are missing name and GUID values and will be
   * completed by the caller to avoid recursion within the api call.
   *
   * @return the object read from the binary OpenCyc input stream
   *
   * @throws IOException if a communications error occurs
   */
  public Object readObject() throws IOException {
    return readObject(true);
  }
  
  /**
   * As per {@link #readObject()}, but takes a boolean <tt>logAsEntrypoint</tt> which toggles 
   * whether the logging statements for the immediate call should be emphasized: DEBUG instead of 
   * TRACE level, visual markers, etc. Callers should typically pass in <tt>false</tt> for this 
   * value; it is specifically intended so that calls to {@link #readObject()} can stand out amongst
   * subsequent nested calls.
   *
   * @param logAsEntrypoint
   *
   * @return the object read from the binary OpenCyc input stream
   *
   * @throws IOException if a communications error occurs
   */
  private Object readObject(boolean logAsEntrypoint) throws IOException {
    final int cfaslOpcode = readOpcode();
    Object o = null;
    if (LOGGER.isTraceEnabled()) {
      final String msg = logAsEntrypoint
                                 ? "--> reading opcode = {} ({})"
                                 : "    reading opcode = {} ({})";
      LOGGER.trace(msg, cfaslOpcode, CFASL_OPCODE_DESCRIPTIONS.get(cfaslOpcode));
    }
    if (cfaslOpcode >= CFASL_IMMEDIATE_FIXNUM_OFFSET) {
      o = cfaslOpcode - CFASL_IMMEDIATE_FIXNUM_OFFSET;
      LOGGER.trace("Reading Immediate Fixnum: {}", o);
    }
    if (o == null) {
      o = maybeReadNumber(cfaslOpcode);
    }
    if (o == null) {
      o = maybeReadSymbol(cfaslOpcode);
    }
    if (o == null) {
      o = maybeReadSequence(cfaslOpcode);
    }
    if (o == null) {
      o = maybeReadOther(cfaslOpcode);
    }
    if (LOGGER.isTraceEnabled() || (logAsEntrypoint && LOGGER.isDebugEnabled())) {
      final String oClass = o.getClass().getCanonicalName();
      Object oValue;
      try {
        // If object o understands the safeToString method, then use it.
        oValue = o.getClass().getMethod("safeToString").invoke(o);
      } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        oValue = o;
      }
      if (logAsEntrypoint) {
        final String pfx = LOGGER.isTraceEnabled() ? "==> " : "";
        LOGGER.debug(pfx+ "readObject = {} (opcode {}: {})", oValue, cfaslOpcode, oClass);
      } else {
        LOGGER.trace("    readObject = {} (opcode{}: {})", oValue, cfaslOpcode, oClass);
      }
    }
    return o;
  }
  
  /**
   * Reports the unhandled cfasl opcode or throws an exception.
   * 
   * @param cfaslOpcode the unhandled cfasl opcode
   * 
   * @return the unhandled cfasl opcode
   * 
   * @throws CfaslInputStreamClosedException if the socket connection is closed by the peer
   * @throws BaseClientRuntimeException if the error is not logged and ignored
   */
  protected Object reportUnhandledCfaslOpcode(int cfaslOpcode) {
    final String errorMessage;
    if (cfaslOpcode == -1) {
      throw new CfaslInputStreamClosedException(
              "Cfasl connection closed by peer because of bad opcode: " + cfaslOpcode);
    } else {
      errorMessage = "Unknown cfasl opcode: " + cfaslOpcode;
    }
    LOGGER.error(errorMessage);
    if (reportCfaslErrors) {
      //LOGGER.debug(errorMessage);
      return Integer.toString(cfaslOpcode);
    } else {
      // TODO: create a new exception class for this case.
      throw new BaseClientRuntimeException(errorMessage);
    }
  }

  /**
   * Reads an char from this CfaslInputStream.  If the next item on the stream is not a char, throw
   * an exception, and leave that object on the input stream.
   * 
   * @return the character read
   * 
   * @throws IOException if a communications error occurs
   * @throws BaseClientRuntimeException if an unexpected cfasl opcode occurs
   */
  private char readChar() throws IOException {
    mark(DEFAULT_READ_LIMIT);
    final int cfaslOpcode = read();
    if (cfaslOpcode == CFASL_CHARACTER) {
      return (char) read();
    }
    reset();
    throw new BaseClientRuntimeException("Expected a char but received opCode=" + cfaslOpcode);
  }

  /**
   * Reads a double from this CfaslInputStream.  If the next item on the stream is not a double,
   * throw an exception, and leave that object on the input stream.
   * 
   * @return the double read
   * 
   * @throws IOException if a communications error occurs
   * @throws BaseClientRuntimeException if an unexpected cfasl opcode occurs
   */
  private double readDouble() throws IOException {
    mark(DEFAULT_READ_LIMIT);
    final int cfaslOpcode = read();
    switch (cfaslOpcode) {
      case CFASL_P_FLOAT:
        return readFloatBody(false);
      case CFASL_N_FLOAT:
        return readFloatBody(true);
      default:
        reset();
        throw new BaseClientRuntimeException("Expected a double but received OpCode=" + cfaslOpcode);
    }
  }

  /**
   * Reads an int from this CfaslInputStream.  If the next item on the stream is not an int, throw
   * an exception, and leave that object on the input stream.  Bignum ints are not allowed.
   * 
   * @return the int read
   * 
   * @throws IOException if a communications error occurs
   * @throws BaseClientRuntimeException if an unexpected cfasl opcode occurs
   */
  private int readInt() throws IOException {
    mark(DEFAULT_READ_LIMIT);
    final int cfaslOpcode = read();
    if (cfaslOpcode >= CFASL_IMMEDIATE_FIXNUM_OFFSET) {
      return cfaslOpcode - CFASL_IMMEDIATE_FIXNUM_OFFSET;
    } else {
      switch (cfaslOpcode) {
        case CFASL_P_8BIT_INT:
          return readFixnumBody(1, false);
        case CFASL_N_8BIT_INT:
          return readFixnumBody(1, true);
        case CFASL_P_16BIT_INT:
          return readFixnumBody(2, false);
        case CFASL_N_16BIT_INT:
          return readFixnumBody(2, true);
        case CFASL_P_24BIT_INT:
          return readFixnumBody(3, false);
        case CFASL_N_24BIT_INT:
          return readFixnumBody(3, true);
        case CFASL_P_32BIT_INT:
          return readFixnumBody(4, false);
        case CFASL_N_32BIT_INT:
          return readFixnumBody(4, true);
        default:
          reset();
          throw new BaseClientRuntimeException(
                  "Expected an int but received OpCode=" + cfaslOpcode);
      }
    }
  }

  private int readOpcode() throws IOException {
    LOGGER.trace("Reading CFASL opcode.");
    int cfaslOpcode = read();
    LOGGER.trace("Read CFASL opcode: {}", cfaslOpcode);
    if (cfaslOpcode == CFASL_EXTERNALIZATION) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("reading opcode = {} ({})",
                cfaslOpcode, CFASL_OPCODE_DESCRIPTIONS.get(cfaslOpcode));
      }
      cfaslOpcode = read();
    }
    return cfaslOpcode;
  }

  /**
   * Reads the body of a CFASL Fixnum (everything but the opcode) from this CFASL input stream.
   * 
   * @param nBytes  The number of bytes to read
   * @param shouldBeNegative    true iff the Fixnum is negative
   * 
   * @return an int holding the CFASL Fixnum read in
   * 
   * @throws IOException if a communications error occurs
   * @throws ArithmeticException if nBytes > 4 or if the integer read in does not fit into a signed
   *         32 bit integer (i.e. the sign bit is being used for magnitude).
   */
  private int readFixnumBody(int nBytes, final boolean shouldBeNegative) throws IOException {
    LOGGER.trace("readFixnumBody isNegative={} length={}", shouldBeNegative, nBytes);
    if (nBytes > 4) {
      throw new ArithmeticException("Cannot fit " + nBytes + " bytes into an int");
    }
    int num = 0;
    for (int i = 0; i < nBytes; i++) {
      int j = read();
      LOGGER.trace("\t{}", j);
      num |= (j << (8 * i));
    }
    // num should always be positive here.  Negatives indicate overflows.
    if (num < 0) {
      throw new ArithmeticException(
              "Overflow: " + ((long) num & 0xFFFFFFFFL) + " does not fit into an int");
    }
    return (shouldBeNegative) ? (-num) : num;
  }

  /**
   * Reads the body of a CFASL Bignum (everything but the opcode) off of this CFASL input stream.
   * 
   * @param shouldBeNegative    true iff the Bignum should be negative
   * 
   * @return a BigInteger holding the CFASL Bignum read in
   * 
   * @throws IOException if a communications error occurs
   */
  private BigInteger readBignumBody(final boolean shouldBeNegative) throws IOException {
    final int length = readInt();
    LOGGER.trace("readBignumBody shouldBeNegative={} length={}", shouldBeNegative, length);
    final byte[] b = new byte[length];
    for (int i = length - 1; i >= 0; i--) {
      final int j = readInt();
      LOGGER.trace("\t{}", j);
      b[i] = (byte) j;
    }
    return new BigInteger((shouldBeNegative) ? -1 : 1, b);
  }

  /**
   * Reads the body of a CFASL Float (everything but the opcode) off of this CFASL input stream.
   * 
   * @param shouldBeNegative    true iff the Float should be negative
   * 
   * @return a double holding the CFASL Float read in
   * 
   * @throws IOException if a communications error occurs
   * @throws ArithmeticException if significand cannot fit into a 64 bit signed long int
   */
  private double readFloatBody(final boolean shouldBeNegative) throws IOException {
    final long signif;
    final long exp;
    LOGGER.trace("readFloatBody shouldBeNegative={}", shouldBeNegative);
    final Object obj = readObject(false);
    if (obj instanceof BigInteger) {
      BigInteger bi = (BigInteger) obj;
      if (bi.bitCount() < 64) {
        signif = bi.longValue();
      } else {
        throw new ArithmeticException("Overflow reading significand of float");
      }
    } else {
      signif = ((Number) obj).longValue();
    }
    exp = readInt();
    LOGGER.trace("readFloatBody shouldBeNegative={} signif={} exp={}", 
            shouldBeNegative, signif, exp);
    final Double absoluteValue = (double) signif * Math.pow(2.0, exp);
    return (shouldBeNegative) ? (-absoluteValue) : absoluteValue;
  }

  /**
   * Reads the body of a keyword Symbol from the CfaslInputStream.  The CFASL opcode has already
   * been read in at this point, so we only read in what follows.
   * 
   * @return the keyword <tt>CycSymbolImpl</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private CycSymbolImpl readKeyword() throws IOException {
    String keywordString = (String) readObject(false);
    if (!(keywordString.startsWith(":"))) {
      keywordString = ":" + keywordString;
    }
    return CycObjectFactory.makeCycSymbol(keywordString);
  }

  /**
   * Reads the body of a Symbol or EL variable from the CfaslInputStream.  The CFASL opcode has
   * already been read in at this point, so we only read in what follows.
   * 
   * @return the <tt>CycSymbolImpl</tt> or EL <tt>CycVariableImpl</tt>
   * 
   * @throws IOException if a communications error occurs
   */
  private Object readSymbol() throws IOException {
    LOGGER.trace("About to read symbol name.");
    final Object response = readObject(false);
    if (!(response instanceof String)) {
      throw new CycApiException(
              "Expecting  a String, got: " + response.getClass() + " for object: " + response);
    }
    final String name = (String) response;
    if (name.startsWith("?")) {
      return CycObjectFactory.makeCycVariable(name);
    } else {
      return CycObjectFactory.makeCycSymbol(name);
    }
  }

  /**
   * Reads the body of a GuidImpl from the CfaslInputStream.  The CFASL opcode has already been read in
   * at this point, so we only read in what follows.
   * 
   * @return the <tt>GuidImpl</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private GuidImpl readLegacyGuid() throws IOException {
    final GuidImpl guid = CycObjectFactory.makeGuid((String) readObject(false));
    LOGGER.trace("readLegacyGuid: {}", guid);
    return guid;
  }

  /**
   * Reads the body of a GuidImpl from the CfaslInputStream.  The CFASL opcode has already been read in
   * at this point, so we only read in what follows.
   * 
   * @return the <tt>GuidImpl</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private GuidImpl readGuid() throws IOException {
    final byte[] data = new byte[16]; // TODO: resource this
    for (int i = 0; i < 16; i++) {
      final int currByte = read();
      if (currByte == -1) {
        throw new BaseClientRuntimeException("Illegal end of stream");
      }
      data[i] = (byte) currByte;
    }
    final GuidImpl guid = CycObjectFactory.makeGuid(data);
    LOGGER.trace("readGuid: {}", guid);
    return guid;
  }
  
  /**
   * Reads the body of a Unicode Character from the CfaslInputStream.  
   * The CFASL opcode has already been read in
   * at this point, so we only read in what follows.
   * 
   * @return the <tt>Integer</tt> of the unicode value read
   * 
   * @throws IOException if a communications error occurs
   */
  private Integer readUnicodeChar() throws IOException {
    final int len = readInt();
    final byte[] s = new byte[len];
    int off = 0;
    while (off < len) {
      off += read(s, off, len - off);
    }
    final String charString = new String(s, "UTF-8");
    final int retval = (int) charString.charAt(0);
    // NOTE: When we upgrade to java 1.5 change the above line to 
    //     int retval = charString.codePointAt(0);
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("readUnicodeChar: 0x{}", Integer.toHexString(retval));
    }
    return retval;
  }

  /**
   * Reads the body of a Unicode String from the CfaslInputStream.  
   * The CFASL opcode has already been read in
   * at this point, so we only read in what follows.
   * 
   * @return the <tt>String</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private String readUnicodeString() throws IOException {
    final int len = readInt();
    final byte[] s = new byte[len];
    int off = 0;
    while (off < len) {
      off += read(s, off, len - off);
    }
    final String retval = new String(s, "UTF-8");
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("readUnicodeString: " + retval);
    }
    return retval;
  }

  /**
   * Reads a byte vector from the CfaslInputStream. The CFASL opcode has already been read in at
   * this point, so we only read in what follows.
   * 
   * @return the <tt>ByteArray</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private ByteArray readByteArray() throws IOException {
    final int len = readInt();
    final byte[] bytes = new byte[len];
    int off = 0;
    while (off < len) {
      off += read(bytes, off, len - off);
    }
    return new ByteArray(bytes);
  }

  /**
   * Reads a list from the CfaslInputStream.  The CFASL opcode has already been read in at this
   * point, so we only read in what follows.
   * 
   * @return the <tt>CycArrayList</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private CycArrayList readCycList() throws IOException {
    final int size = readInt();
    LOGGER.trace("readCycList.size: {}", size);
    final CycArrayList cycList = new CycArrayList(size);
    for (int i = 0; i < size; i++) {
      cycList.add(readObject(false));
    }
    LOGGER.trace("readCycList.readObject: {}", cycList);
    return cycList;
  }

  /**
   * Reads a dotted list from the CfaslInputStream.  The CFASL opcode has already been read in at
   * this point, so we only read in what follows.
   * 
   * @return the <tt>CycArrayList</tt> read
   * 
   * @throws IOException if a communications error occurs
   */
  private CycArrayList readCons() throws IOException {
    final int size = readInt();
    LOGGER.trace("readCons.size: {}", size);
    final CycArrayList cycList = new CycArrayList();
    //for (int i = 0; i < (size - 1); i++) {
    for (int i = 0; i < size; i++) {
      final Object consObject = readObject(false);
      if (LOGGER.isTraceEnabled()) {
        if (consObject instanceof Fort) {
          LOGGER.trace("readCons.consObject: " + ((Fort) consObject).toString());
        } else {
          LOGGER.trace("readCons.consObject: " + consObject);
        }
      }
      cycList.add(consObject);
    }
    final Object cdrObject = readObject(false);
    if (LOGGER.isTraceEnabled()) {
      try {
        // If element understands the safeToString method, then use it.
        final Method safeToString = cdrObject.getClass().getMethod("safeToString");
        LOGGER.trace("readCons.cdrObject: {}", safeToString.invoke(cdrObject));
      } catch (Exception e) {
        LOGGER.trace("readCons.cdrObject: {}", cdrObject);
      }
    }
    cycList.setDottedElement(cdrObject);
    LOGGER.trace("readCons.readCons: {}", cycList);
    return cycList;
  }

  /**
   * Reads a complete constant from a CfaslInputStream.
   * 
   * @return an complete <tt>CycConstantImpl</tt> having the input guid and name
   * 
   * @throws IOException if a communications error occurs
   * @throws BaseClientRuntimeException if an unexpected constant id type occurs
   */
  private CycConstant readCompleteConstant() throws IOException {
    final Object idObject = readObject(false);
    CycConstant cycConstant;
    if (idObject instanceof GuidImpl) {
      final GuidImpl guid = (GuidImpl) idObject;
      final String name = (String) readObject(false);
      cycConstant = CycObjectFactory.getCycConstantCacheByGuid(guid);
      if (cycConstant == null) {
        cycConstant = new CycConstantImpl(name, guid);
        CycObjectFactory.addCycConstantCache(cycConstant);
      }
    } else if ((idObject instanceof CycSymbolImpl)
            && (idObject.equals(CycObjectFactory.makeCycSymbol(":FREE")))) {
      cycConstant = CycObjectFactory.FREE_CONSTANT;
    } else {
      // ignore the name, which is expected to be blank
      readObject(false);
      cycConstant = CycObjectFactory.INVALID_CONSTANT;
      isInvalidObject = true;
    }
    LOGGER.trace("readConstant: {}", cycConstant);
    return cycConstant;
  }

  /**
   * Reads a variable from the CfaslInputStream.
   * 
   * @return an complete <tt>CycVariableImpl</tt> having the name
   * 
   * @throws IOException if a communications error occurs
   */
  private CycVariableImpl readCompleteVariable() throws IOException {
    final Integer hlVariableId = (Integer) readObject(false);
    final String name = (String) readObject(false);
    final CycVariableImpl cycVariable = new CycVariableImpl(name, hlVariableId);
    LOGGER.trace("readVariable: {}", cycVariable);
    return cycVariable;
  }

  /**
   * Reads a NART from a CfaslInputStream.
   * 
   * @return a the Nart having the input HL Formula or having the input id, or NIL if the nart is invalid
   * 
   * @throws IOException if a communications error occurs
   */
  private CycObject readNart() throws IOException {
    final int cfaslOpcode = read();
    Nart cycNart = null;
    if (cfaslOpcode == CFASL_NIL) {
      cycNart = CycObjectFactory.INVALID_NART;
      isInvalidObject = true;
    } else if (cfaslOpcode != CFASL_LIST) {
      if (cfaslOpcode == CFASL_SYMBOL) {
        String name = (String) readObject(false);
        System.err.println("readNart, symbol=" + name);
      }
      throw new BaseClientRuntimeException("reading nart, expected a list, found " + cfaslOpcode);
    } else {
      cycNart = new NartImpl(readCycList());
    }
    LOGGER.trace("readNart: {}", cycNart);
    return cycNart;
  }

  /**
   * Reads an assertion from a CfaslInputStream.
   * 
   * @return an incomplete <tt>CycAssertionImpl</tt> having the input id
   * 
   * @throws IOException if a communications error occurs
   */
  private CycAssertion readAssertion() throws IOException {
    final Object formulaObject = readObject(false);
    CycAssertion cycAssertion = null;
    if (formulaObject.toString().equals("NIL")) {
      // bypass invalid assertion mt
      readObject(false);
      cycAssertion = CycObjectFactory.INVALID_ASSERTION;
      isInvalidObject = true;
    } else {
      try {
        final CycArrayList formula = (CycArrayList) formulaObject;
        final CycObject mt = (CycObject) readObject(false);
        cycAssertion = new CycAssertionImpl(formula, mt);
      } catch (ClassCastException e) {
        System.err.println("formulaObject " + formulaObject.toString()
                                   + "(" + formulaObject.getClass().getName() + ")");
      }
    }
    LOGGER.trace("readAssertion: {}", cycAssertion);
    return cycAssertion;
  }

  private Object maybeReadNumber(int cfaslOpcode) throws IOException {
    switch (cfaslOpcode) {
      case CFASL_P_8BIT_INT:
        return readFixnumBody(1, false);
      case CFASL_N_8BIT_INT:
        return readFixnumBody(1, true);
      case CFASL_P_16BIT_INT:
        return readFixnumBody(2, false);
      case CFASL_N_16BIT_INT:
        return readFixnumBody(2, true);
      case CFASL_P_24BIT_INT:
        return readFixnumBody(3, false);
      case CFASL_N_24BIT_INT:
        return readFixnumBody(3, true);
      case CFASL_P_32BIT_INT:
        return readFixnumBody(4, false);
      case CFASL_N_32BIT_INT:
        return readFixnumBody(4, true);
      case CFASL_P_FLOAT:
        return readFloatBody(false);
      case CFASL_N_FLOAT:
        return readFloatBody(true);
      case CFASL_P_BIGNUM:
        return readBignumBody(false);
      case CFASL_N_BIGNUM:
        return readBignumBody(true);
      default:
        return null;
    }
  }

  private Object maybeReadSymbol(int cfaslOpcode) throws IOException {
    switch (cfaslOpcode) {
      case CFASL_KEYWORD:
        // Keywords can be distinguished from Symbols by internal evidence
        return readKeyword();
      case CFASL_SYMBOL:
        return readSymbol();
      case CFASL_NIL:
        return CycObjectFactory.nil;
      default:
        return null;
    }
  }

  private Object maybeReadSequence(int cfaslOpcode) throws IOException {
    switch (cfaslOpcode) {
      case CFASL_LIST:
        return readCycList();
      case CFASL_DOTTED:
        return readCons();
      case CFASL_VECTOR:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_STRING:
        int off = 0;
        int len = readInt();
        byte[] s = new byte[len];
        while (off < len) {
          off += read(s, off, len - off);
        }
        return new String(s, "UTF-8");
      default:
        return null;
    }
  }

  private Object maybeReadOther(int cfaslOpcode) throws IOException {
    switch (cfaslOpcode) {
      case CFASL_CHARACTER:
        return (char) read();
      case CFASL_HASHTABLE:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_BTREE_LOW_HIGH:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_BTREE_LOW:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_BTREE_HIGH:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_BTREE_LEAF:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_LEGACY_GUID:
        return readLegacyGuid();
      case CFASL_GUID:
        return readGuid();
      case CFASL_UNICODE_STRING:
        return readUnicodeString();
      case CFASL_UNICODE_CHAR:
        return readUnicodeChar();
      case CFASL_BYTE_VECTOR:
        return readByteArray();
      case CFASL_CONSTANT:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_NART:
        return readNart();
      case CFASL_COMPLETE_CONSTANT:
        return readCompleteConstant();
      case CFASL_ASSERTION:
        return readAssertion();
      case CFASL_ASSERTION_SHELL:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_ASSERTION_DEF:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_SOURCE:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_SOURCE_DEF:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_AXIOM:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_AXIOM_DEF:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_VARIABLE:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_INDEX:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_COMPLETE_VARIABLE:
        return readCompleteVariable();
      case CFASL_SPECIAL_OBJECT:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_DICTIONARY:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      case CFASL_SERVER_DEATH:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
      default:
        return reportUnhandledCfaslOpcode(cfaslOpcode);
    }
  }
  
}
