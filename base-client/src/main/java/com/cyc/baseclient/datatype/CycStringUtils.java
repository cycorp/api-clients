package com.cyc.baseclient.datatype;

/*
 * #%L
 * File: CycStringUtils.java
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
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.Formula;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.kb.KbObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.lang3.text.WordUtils;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.stripStart;

/**
 * Provides <tt>String</tt> utilities not otherwise provided by Violin Strings.
 * All methods are static. There is no need to instantiate this class.<p>
 *
 * @version $Id: CycStringUtils.java 175790 2017-11-08 02:44:55Z nwinant $
 * @author Stephen L. Reed
 */
public class CycStringUtils {
  
  
  // Public fields
  
  public static final String INDENT = "  ";
  public static final String DEFAULT_NULL_STRING = "null";
  
  private static final String UNICODE_STRING_FN_STR = CommonConstants.UNICODE_STRING_FN.cyclify();
  
  public static Comparator<Object> STRING_VALUE_COMPARATOR = (Object o1, Object o2) -> {
    return Objects.toString(o1, "").compareTo(Objects.toString(o2, ""));
  };
  
  
  // Methods

  /**
   * Returns true iff all characters in the given string are numeric. A prefix
   * character of "+" or "-" is accepted as numeric. Decimal points are not
   * accepted as numeric by this method.
   *
   * @param string the string to be tested
   * @return <tt>true</tt> iff all characters are numeric
   */
  public static boolean isNumeric(String string) {
    String testString = string;
    if ((string.charAt(0) == '+')
            || (string.charAt(0) == '-')) {
      if (string.length() > 1) {
        testString = string.substring(1);
      } else {
        return false;
      }
    }
    for (int i = 0; i < testString.length(); i++) {
      if (!Character.isDigit(testString.charAt(i))) {
        return false;
      }
    }
    return true;
  }
  static Pattern dumbNumericPattern = Pattern.compile(".*?[0-9]*.*");

  public static boolean hasNumericChar(String str) {
    return dumbNumericPattern.matcher(str).matches();
  }

  /**
   * Returns true iff all characters in the given string are whitespace.
   *
   * @param string the string to be tested
   * @return <tt>true</tt> iff all characters are whitespace
   */
  public static boolean isWhitespace(final String string) {
    final int string_length = string.length();
    if (string_length == 0) {
      return false;
    }
    for (int i = 0; i < string_length; i++) {
      if (!Character.isWhitespace(string.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Strips the given leading character from the given string.
   *
   * @param string the string to be stripped
   * @param ch the character to be stripped from the leading part of the string
   * @return the stripped string
   */
  public static String stripLeading(final String string, final char ch) {
    //// Preconditons
    if (string == null) {
      throw new InvalidParameterException("string cannot be null");
    }

    int index = 0;
    final int string_length = string.length();
    if (string_length == 0) {
      return string;
    }
    while (true) {
      if (string.charAt(index) != ch) {
        break;
      } else if (++index >= string_length) {
        break;
      }
    }
    return string.substring(index);
  }

  /**
   * Strips the given trailing character from the given string.
   *
   * @param string the string to be stripped
   * @param ch the character to be stripped from the trailing part of the string
   * @return the stripped string
   */
  public static String stripTrailing(final String string, final char ch) {
    //// Preconditons
    if (string == null) {
      throw new InvalidParameterException("string cannot be null");
    }

    int index = string.length();
    if (index == 0) {
      return string;
    }
    while (true) {
      if (index == 0) {
        break;
      }
      if (string.charAt(--index) != ch) {
        break;
      }
    }
    return string.substring(0, index + 1);
  }

  /**
   * Strips leading blank characters from the given string.
   *
   * @param string the string to be stripped
   * @return the stripped string
   */
  public static String stripLeadingBlanks(final String string) {
    //// Preconditons
    if (string == null) {
      throw new InvalidParameterException("string cannot be null");
    }

    return stripLeading(string, ' ');
  }

  /**
   * Strips trailing blank characters from the given string.
   *
   * @param string the string to be stripped
   * @return the stripped string
   */
  public static String stripTrailingBlanks(final String string) {
    //// Preconditons
    if (string == null) {
      throw new InvalidParameterException("string cannot be null");
    }

    return stripTrailing(string, ' ');
  }

  /**
   * Strips leading and trailing blank characters from the given string.
   *
   * @param string the string to be stripped
   * @return the stripped string
   */
  public static String stripBlanks(final String string) {
    //// Preconditons
    if (string == null) {
      throw new InvalidParameterException("string cannot be null");
    }

    return stripTrailing(stripLeading(string, ' '), ' ');
  }

  /**
   * Returns the string resulting from the replacement of toString for all
   * fromString instances in the given string.
   *
   * @param string the given string
   * @param fromString the string to be replaced
   * @param toString the replacement string
   */
  public static String change(final String string, final String fromString, final String toString) {
    //// Preconditons
    if (string == null) {
      throw new InvalidParameterException("string cannot be null");
    }
    if (fromString == null) {
      throw new InvalidParameterException("fromString cannot be null");
    }
    if (toString == null) {
      throw new InvalidParameterException("toString cannot be null");
    }

    final int string_length = string.length();
    final int fromString_length = fromString.length();
    if (string_length == 0 || fromString_length == 0) {
      return string;
    }
    final StringBuffer stringBuffer = new StringBuffer(string_length * 2);
    int fromIndex = 0;
    int index;
    while (true) {
      index = string.indexOf(fromString, fromIndex);
      if (index == -1) {
        stringBuffer.append(string.substring(fromIndex));
        break;
      } else {
        stringBuffer.append(string.substring(fromIndex, index));
        stringBuffer.append(toString);
        fromIndex = index + fromString_length;
        index = -1;
        if (fromIndex == string_length) {
          break;
        }
      }
    }
    return stringBuffer.toString();
  }

  /**
   * Returns true iff the given object is a string delimited by double quotes.
   *
   * @param object the object to be tested
   * @return <tt>true</tt> iff the given object is a string delimited by double
   * quotes
   */
  public static boolean isDelimitedString(Object object) {
    if (!(object instanceof String)) {
      return false;
    }
    String string = (String) object;
    if (string.length() < 2) {
      return false;
    }
    if (!string.startsWith("\"")) {
      return false;
    }
    return string.endsWith("\"");
  }

  /**
   * Removes delimiter characters from the given string.
   *
   * @param string the string which has delimiters to be removed
   * @return a input string without its delimiters
   */
  public static String removeDelimiters(String string) {
    int length = string.length();
    if (length < 3) {
      throw new BaseClientRuntimeException("Cannot remove delimters from " + string);
    }
    return string.substring(1, length - 1);
  }

  /**
   * Returns the phrase formed from the given list of words
   *
   * @param words the phrase words
   * @return the phrase formed from the given list of words
   */
  public static String wordsToPhrase(List words) {
    StringBuilder stringBuffer = new StringBuilder();
    for (int i = 0; i < words.size(); i++) {
      if (i > 0) {
        stringBuffer.append(" ");
      }
      stringBuffer.append(words.get(i));
    }
    return stringBuffer.toString();
  }

  /**
   * Escapes embedded double quote and backslash characters in the given string.
   *
   * @param string the given string
   * @return the given string with embeded double quote characters preceded by a
   * backslash character, and with embedded backslash characters preceded by
   * another (escaping) backslash character
   */
  public static String escapeDoubleQuotes(String string) {
    if (string == null) {
      return null;
    }
    String result = string.replaceAll("\\\\", "\\\\\\\\");
    return result.replaceAll("\\\"", "\\\\\\\"");
  }

  /**
   * Un-escapes embedded double quote and backslash characters in the given
   * string.
   *
   * @param string the given string
   * @return the given string with un-escaped backslash and double quote
   * characters
   */
  public static String unescapeDoubleQuotes(String string) {
    if (string == null) {
      return null;
    }
    String result = string.replaceAll("\\\\\\\"", "\\\"");
    return result.replaceAll("\\\\\\\\", "\\\\");
  }

  /**
   * Inserts an escape character before each quote character in the given
   * string.
   *
   * @param string the given string
   * @return the string with an escape character before each quote character
   */
  public static String escapeQuoteChars(String string) {
    if (string == null) {
      return null;
    }
    return string.replaceAll("\\\"", "\\\\\\\"");
  }

  /**
   * Returns the String representation of an Throwable, including short message
   * and stack trace.
   *
   * @param e The throwable item to get the String rep.
   * @return The String representation of the Throwable passed.
   */
  public static String getStringForException(Throwable e) {
    if (e == null) {
      return "<no exception>";
    }
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(new BufferedWriter(writer));
    e.printStackTrace(out);
    out.flush();
    return writer.getBuffer().toString();
  }

  /**
   * Returns true if all characters in the string have a representation below
   * 0x80 and returns false otherwise.
   *
   * @param str The string to be tested.
   * @return true if the string is 7 bit ASCII and false otherwise.
   */
  public static boolean is7BitASCII(String str) {

    int i;
    for (i = 0; i < str.length(); i++) {
      if (str.charAt(i) >= 0x80) {
        break; // note: change to codePointAt after we switch to java 1.5
      }
    }
    return (i == str.length());
  }

  /**
   * Converts a string-denoting CycL term to a Java string
   *
   * @param cyclString
   * @return Java string version of cyclString
   * @throws IllegalArgumentException if cyclString cannot be interpreted as a
   * string.
   */
  public static String cyclStringToJavaString(final Object cyclString) {
    if (cyclString instanceof String) {
      return (String) cyclString;
    } else {
      Exception exception = null;
      try {
        final Naut naut = (Naut) NautImpl.convertIfPromising(cyclString);
        if (CommonConstants.UNICODE_STRING_FN.equals(naut.getFunctor())) {
          final String encoded = (String) naut.getArg(1);
          return unescapeUnicode(encoded);
        }
      } catch (Exception ex) {
        exception = ex;
      }
      throw new StringConversionException("Can't convert " + cyclString + " to a string.", exception);
    }
  }

  private static class StringConversionException extends IllegalArgumentException {

    StringConversionException(final String msg, final Throwable cause) {
      super(msg, cause);
    }
  }

  /**
   * Converts string-denoting CycL terms anywhere in tree to Java strings.
   *
   * @param tree
   * @return Java version of tree with CycL strings converted to Java strings
   */
  public static Object cyclStringsToJavaStrings(Object tree) {
    return cyclStringsToJavaStringsRecursive(tree);

  }

  private static Object cyclStringsToJavaStringsRecursive(Object tree) {
    if (tree instanceof String) {
      return tree;
    } else if (tree instanceof List) {
      try {
        final String canonical = cyclStringToJavaString(tree);
        return canonical;
      } catch (StringConversionException ex) {
        final List newItems = (List) tree;
        for (int i = 0; i < newItems.size(); i++) {
          newItems.set(i, cyclStringsToJavaStringsRecursive(newItems.get(i)));
        }
        // Canonicalize (:anything UnicodeStringFn :encoded) to (:anything . :string)
        if (newItems instanceof CycArrayList && newItems.size() == 3
                && CommonConstants.UNICODE_STRING_FN.equals(newItems.get(1))) {
          final CycArrayList asCycList = (CycArrayList) newItems;
          if (asCycList.isProperList()) {
            final Object oldRest = asCycList.rest();
            try {
              final Object newCdr = cyclStringToJavaString(oldRest);
              asCycList.remove(2);
              asCycList.remove(1);
              asCycList.setDottedElement(newCdr);
            } catch (StringConversionException ex2) {
              // Just means we couldn't convert it, so leave it as is.
            }
          }
        }
        return newItems;
      }
    } else {
      return tree;
    }
  }

  private static String unescapeUnicode(String encoded) {
    int length = encoded.length();
    final StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      final char currChar = encoded.charAt(i);
      if ('&' == currChar) {
        final StringBuilder codeBuilder = new StringBuilder(6);
        if ('u' != encoded.charAt(++i)) {
          throw new IllegalArgumentException("& not followed by u at position " + i + " in " + encoded);
        }
        char nextChar = encoded.charAt(++i);
        while (!(';' == nextChar || i >= length)) {
          codeBuilder.append(nextChar);
          nextChar = encoded.charAt(++i);
        }
        sb.append(((char) Integer.parseInt(codeBuilder.toString(), 16)));
      } else {
        sb.append(currChar);
      }
    }
    return sb.toString();
  }

  public static String unicodeEscaped(final String str) {
    return unicodeEscaped(str, true);
  }

  /**
   * Returns the Subl escaped string representing the input string Each
   * character that is not 7 bit ASCII and ASCII control characters are escaped
   * in the following form where xxxx is the hex representation of the character
   * (it may be from 2 to 6 hex digits as needed). The escaped representation is
   * then &uxxxx; The semicolon at the end is required. Also '&' is escaped to
   * &u26;
   *
   * @param str The string to be converted.
   * @param isApi
   * @return the unicode escaped string.
   */
  public static String unicodeEscaped(final String str, final boolean isApi) {
    //System.out.println("UnicodeEscaped Entry: |"+str+"|");
    String estr = escapeDoubleQuotes(str);
    //System.out.println("UnicodeEscaped esc  |"+estr+"|");
    // now we have to change "true unicode chars( value>=0x80 ) to sublisp escaped values
    StringBuilder sb = new StringBuilder(str.length());
    char c; // note: change to int after we switch to java 1.5
    int i;
    for (i = 0; i < estr.length(); i++) {
      c = estr.charAt(i);// note: change to codePointAt after we switch to java 1.5
      if (c >= 0x20 && c < 0x80) {
        if (c == '&') {
          sb.append("&u26;");
        } else {
          sb.append(c);
        }
      } else {
        sb.append("&u");
        String hex = Integer.toHexString((int) c).toUpperCase();
        if (hex.length() < 2) {
          sb.append('0');
        }
        sb.append(hex);
        sb.append(';');
      }

    }
    //System.out.println("UnicodeEscaped exit |"+sb.toString()+"|");
    if (isApi) {
      return "(list " + UNICODE_STRING_FN_STR + " \"" + sb.toString() + "\")";
    } else {
      return "(" + UNICODE_STRING_FN_STR + " \"" + sb.toString() + "\")";
    }
  }
  
  //
  //------------------------------------------------------------
  //
  // Code to de-escaped HTML escaped strings,
  // NOTE: we use a static initializer to build the static html treeMap
  //
  //------------------------------------------------------------
  //
  public static String deEscapeHTMLescapedString(String s) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    char c;
    int old;
    while (i < s.length()) {
      c = s.charAt(i);
      //System.out.println("de: "+i+" char: |"+c+"|");
      if ('&' == c) {
        old = i;
        // we assume all entities are of the form &xxxx;
        // where xxxx is either an entity or of the form
        // &#xxxx; where xxxx is a hex number 
        // if its not, just pass it through
        for (i++; i < s.length() && s.charAt(i) != ';'; i++);
        if (i >= s.length()) {// no terminating semicolon
          sb.append(c); // append the &
          i = old;
        } else {// we have a terminating semicolon
          if (old + 1 == i) { // we have just &;
            sb.append("&;");
          } else {// we have a entity of at least length 1
            String ent = s.substring(old + 1, i);
            if (ent.charAt(0) == '#') {// it might be hex
              String hex = ent.substring(1);
              try {
                int intv = Integer.parseInt(hex, 16); // parse it if it's hex
                sb.append((char) intv);
              } catch (NumberFormatException e) { // not a valid hex entity
                sb.append(c); // append the &
                i = old;
              }
            } else { // maybe an html entity?
              char entchar = lookupEntity(ent);
              if (entchar != (char) 0) {// we found the entity
                sb.append(entchar);
              } else { // not a valid entities
                if (ent.charAt(0) == 'u' || ent.charAt(0) == 'U') {
                  // check for ascii denoting string escape &uhhhh;
                  String hex = ent.substring(1);
                  try {
                    int intv = Integer.parseInt(hex, 16); // parse it if it's hex
                    sb.append((char) intv);
                  } catch (NumberFormatException e) { // not a valid hex entity
                    sb.append(c); // append the &
                    i = old;
                  }
                } else {
                  sb.append(c);// just append the &
                  i = old;
                }
              }
            }
          }
        }
      } else {
        sb.append(c);
      }
      i++;
    }
    return sb.toString();

  }

  private static char lookupEntity(String val) {
    final Character v = (Character) HTML_ENTITIES.get(val);
    if (v == null) {
      return (char) 0;
    }
    return v;
  }

  private static class HtmlEntity {

    public String entity;
    public char value;

    HtmlEntity(String entity, int intvalue) {
      this.entity = entity;
      this.value = (char) intvalue;
    }
  }
  private static final HtmlEntity HTML_ENTITIES_LIST[] = {
    new HtmlEntity("AElig", 198),
    new HtmlEntity("Aacute", 193),
    new HtmlEntity("Acirc", 194),
    new HtmlEntity("Agrave", 192),
    new HtmlEntity("Alpha", 913),
    new HtmlEntity("Aring", 197),
    new HtmlEntity("Atilde", 195),
    new HtmlEntity("Auml", 196),
    new HtmlEntity("Beta", 914),
    new HtmlEntity("Ccedil", 199),
    new HtmlEntity("Chi", 935),
    new HtmlEntity("Dagger", 8225),
    new HtmlEntity("Delta", 916),
    new HtmlEntity("ETH", 208),
    new HtmlEntity("Eacute", 201),
    new HtmlEntity("Ecirc", 202),
    new HtmlEntity("Egrave", 200),
    new HtmlEntity("Epsilon", 917),
    new HtmlEntity("Eta", 919),
    new HtmlEntity("Euml", 203),
    new HtmlEntity("Gamma", 915),
    new HtmlEntity("Iacute", 205),
    new HtmlEntity("Icirc", 206),
    new HtmlEntity("Igrave", 204),
    new HtmlEntity("Iota", 921),
    new HtmlEntity("Iuml", 207),
    new HtmlEntity("Kappa", 922),
    new HtmlEntity("Lambda", 923),
    new HtmlEntity("Mu", 924),
    new HtmlEntity("Ntilde", 209),
    new HtmlEntity("Nu", 925),
    new HtmlEntity("OElig", 338),
    new HtmlEntity("Oacute", 211),
    new HtmlEntity("Ocirc", 212),
    new HtmlEntity("Ograve", 210),
    new HtmlEntity("Omega", 937),
    new HtmlEntity("Omicron", 927),
    new HtmlEntity("Oslash", 216),
    new HtmlEntity("Otilde", 213),
    new HtmlEntity("Ouml", 214),
    new HtmlEntity("Phi", 934),
    new HtmlEntity("Pi", 928),
    new HtmlEntity("Prime", 8243),
    new HtmlEntity("Psi", 936),
    new HtmlEntity("Rho", 929),
    new HtmlEntity("Scaron", 352),
    new HtmlEntity("Sigma", 931),
    new HtmlEntity("THORN", 222),
    new HtmlEntity("Tau", 932),
    new HtmlEntity("Theta", 920),
    new HtmlEntity("Uacute", 218),
    new HtmlEntity("Ucirc", 219),
    new HtmlEntity("Ugrave", 217),
    new HtmlEntity("Upsilon", 933),
    new HtmlEntity("Uuml", 220),
    new HtmlEntity("Xi", 926),
    new HtmlEntity("Yacute", 221),
    new HtmlEntity("Yuml", 376),
    new HtmlEntity("Zeta", 918),
    new HtmlEntity("aacute", 225),
    new HtmlEntity("acirc", 226),
    new HtmlEntity("acute", 180),
    new HtmlEntity("aelig", 230),
    new HtmlEntity("agrave", 224),
    new HtmlEntity("alefsym", 8501),
    new HtmlEntity("alpha", 945),
    new HtmlEntity("amp", 38),
    new HtmlEntity("and", 8743),
    new HtmlEntity("ang", 8736),
    new HtmlEntity("aring", 229),
    new HtmlEntity("asymp", 8776),
    new HtmlEntity("atilde", 227),
    new HtmlEntity("auml", 228),
    new HtmlEntity("bdquo", 8222),
    new HtmlEntity("beta", 946),
    new HtmlEntity("brvbar", 166),
    new HtmlEntity("bull", 8226),
    new HtmlEntity("cap", 8745),
    new HtmlEntity("ccedil", 231),
    new HtmlEntity("cedil", 184),
    new HtmlEntity("cent", 162),
    new HtmlEntity("chi", 967),
    new HtmlEntity("circ", 710),
    new HtmlEntity("clubs", 9827),
    new HtmlEntity("cong", 8773),
    new HtmlEntity("copy", 169),
    new HtmlEntity("crarr", 8629),
    new HtmlEntity("cup", 8746),
    new HtmlEntity("curren", 164),
    new HtmlEntity("dArr", 8659),
    new HtmlEntity("dagger", 8224),
    new HtmlEntity("darr", 8595),
    new HtmlEntity("deg", 176),
    new HtmlEntity("delta", 948),
    new HtmlEntity("diams", 9830),
    new HtmlEntity("divide", 247),
    new HtmlEntity("eacute", 233),
    new HtmlEntity("ecirc", 234),
    new HtmlEntity("egrave", 232),
    new HtmlEntity("empty", 8709),
    new HtmlEntity("emsp", 8195),
    new HtmlEntity("ensp", 8194),
    new HtmlEntity("epsilon", 949),
    new HtmlEntity("equiv", 8801),
    new HtmlEntity("eta", 951),
    new HtmlEntity("eth", 240),
    new HtmlEntity("euml", 235),
    new HtmlEntity("euro", 8364),
    new HtmlEntity("exist", 8707),
    new HtmlEntity("fnof", 402),
    new HtmlEntity("forall", 8704),
    new HtmlEntity("frac12", 189),
    new HtmlEntity("frac14", 188),
    new HtmlEntity("frac34", 190),
    new HtmlEntity("frasl", 8260),
    new HtmlEntity("gamma", 947),
    new HtmlEntity("ge", 8805),
    new HtmlEntity("gt", 62),
    new HtmlEntity("hArr", 8660),
    new HtmlEntity("harr", 8596),
    new HtmlEntity("hearts", 9829),
    new HtmlEntity("hellip", 8230),
    new HtmlEntity("iacute", 237),
    new HtmlEntity("icirc", 238),
    new HtmlEntity("iexcl", 161),
    new HtmlEntity("igrave", 236),
    new HtmlEntity("image", 8465),
    new HtmlEntity("infin", 8734),
    new HtmlEntity("int", 8747),
    new HtmlEntity("iota", 953),
    new HtmlEntity("iquest", 191),
    new HtmlEntity("isin", 8712),
    new HtmlEntity("iuml", 239),
    new HtmlEntity("kappa", 954),
    new HtmlEntity("lArr", 8656),
    new HtmlEntity("lambda", 955),
    new HtmlEntity("lang", 9001),
    new HtmlEntity("laquo", 171),
    new HtmlEntity("larr", 8592),
    new HtmlEntity("lceil", 8968),
    new HtmlEntity("ldquo", 8220),
    new HtmlEntity("le", 8804),
    new HtmlEntity("lfloor", 8970),
    new HtmlEntity("lowast", 8727),
    new HtmlEntity("loz", 9674),
    new HtmlEntity("lrm", 8206),
    new HtmlEntity("lsaquo", 8249),
    new HtmlEntity("lsquo", 8216),
    new HtmlEntity("lt", 60),
    new HtmlEntity("macr", 175),
    new HtmlEntity("mdash", 8212),
    new HtmlEntity("micro", 181),
    new HtmlEntity("middot", 183),
    new HtmlEntity("minus", 8722),
    new HtmlEntity("mu", 956),
    new HtmlEntity("nabla", 8711),
    new HtmlEntity("nbsp", 160),
    new HtmlEntity("ndash", 8211),
    new HtmlEntity("ne", 8800),
    new HtmlEntity("ni", 8715),
    new HtmlEntity("not", 172),
    new HtmlEntity("notin", 8713),
    new HtmlEntity("nsub", 8836),
    new HtmlEntity("ntilde", 241),
    new HtmlEntity("nu", 957),
    new HtmlEntity("oacute", 243),
    new HtmlEntity("ocirc", 244),
    new HtmlEntity("oelig", 339),
    new HtmlEntity("ograve", 242),
    new HtmlEntity("oline", 8254),
    new HtmlEntity("omega", 969),
    new HtmlEntity("omicron", 959),
    new HtmlEntity("oplus", 8853),
    new HtmlEntity("or", 8744),
    new HtmlEntity("ordf", 170),
    new HtmlEntity("ordm", 186),
    new HtmlEntity("oslash", 248),
    new HtmlEntity("otilde", 245),
    new HtmlEntity("otimes", 8855),
    new HtmlEntity("ouml", 246),
    new HtmlEntity("para", 182),
    new HtmlEntity("part", 8706),
    new HtmlEntity("permil", 8240),
    new HtmlEntity("perp", 8869),
    new HtmlEntity("phi", 966),
    new HtmlEntity("pi", 960),
    new HtmlEntity("piv", 982),
    new HtmlEntity("plusmn", 177),
    new HtmlEntity("pound", 163),
    new HtmlEntity("prime", 8242),
    new HtmlEntity("prod", 8719),
    new HtmlEntity("prop", 8733),
    new HtmlEntity("psi", 968),
    new HtmlEntity("quot", 34),
    new HtmlEntity("rArr", 8658),
    new HtmlEntity("radic", 8730),
    new HtmlEntity("rang", 9002),
    new HtmlEntity("raquo", 187),
    new HtmlEntity("rarr", 8594),
    new HtmlEntity("rceil", 8969),
    new HtmlEntity("rdquo", 8221),
    new HtmlEntity("real", 8476),
    new HtmlEntity("reg", 174),
    new HtmlEntity("rfloor", 8971),
    new HtmlEntity("rho", 961),
    new HtmlEntity("rlm", 8207),
    new HtmlEntity("rsaquo", 8250),
    new HtmlEntity("rsquo", 8217),
    new HtmlEntity("sbquo", 8218),
    new HtmlEntity("scaron", 353),
    new HtmlEntity("sdot", 8901),
    new HtmlEntity("sect", 167),
    new HtmlEntity("shy", 173),
    new HtmlEntity("sigma", 963),
    new HtmlEntity("sigmaf", 962),
    new HtmlEntity("sim", 8764),
    new HtmlEntity("spades", 9824),
    new HtmlEntity("sub", 8834),
    new HtmlEntity("sube", 8838),
    new HtmlEntity("sum", 8721),
    new HtmlEntity("sup", 8835),
    new HtmlEntity("sup1", 185),
    new HtmlEntity("sup2", 178),
    new HtmlEntity("sup3", 179),
    new HtmlEntity("supe", 8839),
    new HtmlEntity("szlig", 223),
    new HtmlEntity("tau", 964),
    new HtmlEntity("there4", 8756),
    new HtmlEntity("theta", 952),
    new HtmlEntity("thetasym", 977),
    new HtmlEntity("thinsp", 8201),
    new HtmlEntity("thorn", 254),
    new HtmlEntity("tilde", 732),
    new HtmlEntity("times", 215),
    new HtmlEntity("trade", 8482),
    new HtmlEntity("uArr", 8657),
    new HtmlEntity("uacute", 250),
    new HtmlEntity("uarr", 8593),
    new HtmlEntity("ucirc", 251),
    new HtmlEntity("ugrave", 249),
    new HtmlEntity("uml", 168),
    new HtmlEntity("upsih", 978),
    new HtmlEntity("upsilon", 965),
    new HtmlEntity("uuml", 252),
    new HtmlEntity("weierp", 8472),
    new HtmlEntity("xi", 958),
    new HtmlEntity("yacute", 253),
    new HtmlEntity("yen", 165),
    new HtmlEntity("yuml", 255),
    new HtmlEntity("zeta", 950),
    new HtmlEntity("zwj", 8205),
    new HtmlEntity("zwnj", 8204)};
  private static final TreeMap HTML_ENTITIES;
  
  static { // static initializer to make treemap for html entities
    HTML_ENTITIES = new TreeMap();
    for (HtmlEntity htmlEntity : HTML_ENTITIES_LIST) {
      HTML_ENTITIES.put(htmlEntity.entity, new Character(htmlEntity.value));
    }
  }
  
  
  /* ====  String formatting  =================================================================== */

  public static String repeat(String str, Object objectToRepeatToLength) {
    return org.apache.commons.lang3.StringUtils
            .repeat(str, Objects.toString(objectToRepeatToLength, DEFAULT_NULL_STRING).length());
  }

  
  /* ====  Collections  ========================================================================= */
  
  private static final String MAP_DEFAULT_NULL_KEY_STRING = DEFAULT_NULL_STRING;
  private static final String MAP_DEFAULT_NULL_VALUE_STRING = DEFAULT_NULL_STRING;
  private static final boolean MAP_DEFAULT_PAD_KEYS = true;
  private static final boolean MAP_DEFAULT_PAD_VALUES = false;
  private static final boolean MAP_DEFAULT_RIGHT_PAD_KEYS = true;
  private static final boolean MAP_DEFAULT_RIGHT_PAD_VALUES = true;
  
  public static int findGreatestStringLength(final Collection objs, final String nullValue) {
    final String nullString = defaultIfNull(nullValue, "");
    int result = 0;
    for (Object obj : objs) {
      final String str = Objects.toString(obj, nullString);
      if (str.length() > result) {
        result = str.length();
      }
    }
    return result;
  }
  
  public static int findGreatestStringLength(final Collection objs) {
    return findGreatestStringLength(objs, "");
  }
    
  public static LinkedHashMap sort(final Map map, final Comparator comparator) {
    final LinkedHashMap results = new LinkedHashMap();
    final List keys = new ArrayList(map.keySet());
    Collections.sort(keys, comparator);
    for (Object key : keys) {
      results.put(key, map.get(key));
    }
    return results;
  }
  
  public static List<String> toStrings(final Collection objs, final String nullValue) {
    final List<String> results = new ArrayList();
    for (Object obj : objs) {
      results.add(Objects.toString(obj, nullValue));
    }
    return results;
  }
  
  public static List<String> toStrings(final Collection objs) {
    return toStrings(objs, DEFAULT_NULL_STRING);
  }
  
  private static List<String> toStrings(
          final Map map, final String separator,
          final boolean padKeys,   final boolean rightPadKeys,
          final boolean padValues, final boolean rightPadValues,
          final String nullKeyString, final String nullValueString) {
    final List<String> results = new ArrayList();
    final List keys = new ArrayList(map.keySet());
    final int keyPadding   = padKeys   ? findGreatestStringLength(keys,         nullKeyString)   : 0;
    final int valuePadding = padValues ? findGreatestStringLength(map.values(), nullValueString) : 0;
    for (Object key : keys) {
      final String name  = Objects.toString(key, nullKeyString);
      final String value = Objects.toString(map.get(key), nullValueString);
      results.add(""
              + (rightPadKeys   ? rightPad(name, keyPadding)    : leftPad(name, keyPadding))
              + separator
              + (rightPadValues ? rightPad(value, valuePadding) : leftPad(value, valuePadding)));
    }
    return results;
  }
  
  public static List<String> toStrings(
          final Map map, final String separator,
          final boolean padKeys,   final boolean rightPadKeys,
          final boolean padValues, final boolean rightPadValues,
          final String nullValueString) {
    return toStrings(
            map, separator, 
            padKeys, rightPadKeys, padValues, rightPadValues,
            MAP_DEFAULT_NULL_KEY_STRING, nullValueString);
  }
  
  public static List<String> toStrings(
          final Map map, final String separator,
          final boolean padKeys,   final boolean rightPadKeys,
          final boolean padValues, final boolean rightPadValues) {
    return toStrings(
            map, separator, 
            padKeys, rightPadKeys, 
            padValues, rightPadValues,
            MAP_DEFAULT_NULL_VALUE_STRING);
  }
  
  public static List<String> toStrings(
          final Map map, final String separator,
          final boolean padKeys, final boolean padValues, final String nullValueString) {
    return toStrings(
            map, separator, 
            padKeys, MAP_DEFAULT_RIGHT_PAD_KEYS, padValues, MAP_DEFAULT_RIGHT_PAD_VALUES,
            nullValueString);
  }
  
  public static List<String> toStrings(
          final Map map, final String separator,
          final boolean padKeys, final boolean padValues) {
    return toStrings(
            map, separator, 
            padKeys, MAP_DEFAULT_RIGHT_PAD_KEYS, padValues, MAP_DEFAULT_RIGHT_PAD_VALUES);
  }
  
  public static List<String> toStrings(final Map map, final String separator) {
    return toStrings(
          map, separator,
          MAP_DEFAULT_PAD_KEYS, MAP_DEFAULT_PAD_VALUES,
          MAP_DEFAULT_NULL_VALUE_STRING);
  }
  
  public static List<String> toStrings(
          final Map map,
          final String prefix, final String separator, final String suffix, 
          boolean padKeys, boolean padValues) {
    final List<String> results = new ArrayList();
    for (String line : toStrings(map, separator, padKeys, padValues)) {
      results.add(prefix + line + suffix);
    }
    return results;
  }
  
  public static List<String> toStrings(
          final Map map,
          final String prefix, final String separator, final String suffix, boolean padding) {
    return toStrings(map, prefix, separator, suffix, padding, padding && isEmpty(suffix));
  }
  
  public static List<String> toStrings(
          final Map map,
          final String prefix, final String separator, final String suffix) {
    return toStrings(map, prefix, separator, suffix, true);
  }
  
  public static List<String> prependLines(
          String prefix, List objs, boolean prefixNulls, final String nullValueString) {
    final List<String> results = new ArrayList();
    for (Object obj : objs) {
      results.add(((obj != null) || prefixNulls) 
              ? "" + prefix + Objects.toString(obj, nullValueString)
              : nullValueString);
    }
    return results;
  }
  
  public static List<String> prependLines(final String prefix, final List objs) {
    return prependLines(prefix, objs, true, DEFAULT_NULL_STRING);
  }
  
  public static List<String> prependFirstLine(
          final String firstPrefix, 
          final String restPrefix,
          final Collection objs, 
          final boolean prefixNulls,
          final String nullValueString) {
    final List<String> results = new ArrayList();
    final String realRestPrefix = (restPrefix != null)
            ? restPrefix 
            : leftPad(" ", firstPrefix.length());
    boolean firstLine = true;
    for (Object obj : objs) {
      results.add(((obj != null) || prefixNulls) 
              ? "" + (firstLine ? firstPrefix : realRestPrefix) + Objects.toString(obj, nullValueString)
              : nullValueString);
      firstLine = false;
    }
    return results;
  }
  
  public static List<String> prependFirstLine(
          final String firstPrefix, final String restPrefix, final Collection objs) {
    return prependFirstLine(firstPrefix, restPrefix, objs, true, DEFAULT_NULL_STRING);
  }
  
  public static List<String> prependFirstLine(
          final String firstPrefix, final String restPrefix, final Object[] objs) {
    return prependFirstLine(firstPrefix, restPrefix, Arrays.asList(objs));
  }
  
  public static List<String> prependFirstLine(final String firstPrefix, final Collection objs) {
    return prependFirstLine(firstPrefix, leftPad(" ", firstPrefix.length()), objs);
  }
  
  public static List<String> prependFirstLine(final String firstPrefix, final Object[] objs) {
    return prependFirstLine(firstPrefix, Arrays.asList(objs));
  }
  
  
  /* ====  Throwables  ========================================================================== */
  
  public static final String DEFAULT_THROWN_FIRST_INDENT = "> ";
  public static final boolean DEFAULT_THROWN_INCLUDE_THROWABLE_CLASS = true;
  public static final boolean DEFAULT_THROWN_INCLUDE_STACK_ELEMENT = true;
  
  /**
   * Pretty-prints the top-most error message from a Throwable with word-wrap and indenting. 
   * For example:
   * <pre><code>
   * > CycApiException: com.cyc.baseclient.datatype.CycStringUtilsTest.getMockException(StringUtilsTest.java:254)
   *   Important CycApiException: Here's a Cyc API exception...
   *   which spans multiple lines
   * </code></pre>
   * 
   * Or:
   * <pre><code>
   * +-Important CycApiException: Here's a Cyc API exception...
   * |-which spans multiple lines
   * </code></pre>
   * 
   * <p>To recursively print the error message from a Throwable <em>and all of its causes</em>, see
   * the {@link #toPrettyMessages(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) } 
   * method.
   * 
   * <p>The result is returned as a list of strings, with each string representing one line, making 
   * it easy to feed to a logger. The list can also be fed to a PrintStream or a Writer via 
   * {@link #printlns(java.util.Collection, java.io.PrintStream)} or 
   * {@link #write(java.util.Collection, java.io.Writer, boolean)},
   * respectively, and of course can easily be concatenated back into a single String by using 
   * something like <code>org.apache.commons.lang3.StringUtils.join(list, "\n")</code>
   * 
   * @see CycStringUtils#DISABLE_WORD_WRAP
   * @see CycStringUtils#toPrettyMessages(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * @see CycStringUtils#printlns(java.util.Collection, java.io.PrintStream)
   * @see CycStringUtils#write(java.util.Collection, java.io.Writer, boolean)
   * 
   * @param t the Throwable to pretty-print
   * @param firstIndent the string which is prepended to the first line
   * @param restIndent the string which is prepended to all subsequent lines
   * @param wrapLength the column to wrap the words at, less than 1 disables word wrap
   * @param includeThrowableClass whether to include the classname of the Throwable
   * @param includeStackElement whether to include the Throwable's first stack element
   * @return a list of strings, one string per line
   */
  public static List<String> toPrettyMessage(
          final Throwable t,
          final String firstIndent,
          final String restIndent,
          final int wrapLength,
          final boolean includeThrowableClass,
          final boolean includeStackElement) {
    final List<String> results = new ArrayList();
    if (t == null) {
      return null;
    }
    final String realFirstIndent = Objects.toString(firstIndent, DEFAULT_THROWN_FIRST_INDENT);
    final String realRestIndent  = Objects.toString(restIndent, 
            leftPad("", realFirstIndent.length()));
    final int indentLength = (realRestIndent.length() > realFirstIndent.length())
            ? realRestIndent.length()
            : realFirstIndent.length();
    final int adjustedWrapLength = (wrapLength > indentLength) ? wrapLength - indentLength : 1;
    final StackTraceElement stackElem = t.getStackTrace()[0];
    final String leadingLine
            = (includeThrowableClass ? t.getClass().getSimpleName() + ":" : "")
            + (includeThrowableClass && includeStackElement ? " " : "")
            + (includeStackElement
                    ? stackElem.getClassName() + "." + stackElem.getMethodName()
                    + "(" + stackElem.getFileName() + ":" + stackElem.getLineNumber() + ")"
                    : "")
            + (includeThrowableClass || includeStackElement ? "\n" : "");
    final String msgLine = leadingLine + t.getMessage();
    final List<String> lines = prependFirstLine(
            realFirstIndent,
            realRestIndent,
            wordWrapIndented(
                    split(msgLine, "\n"),
                    (wrapLength > DISABLE_WORD_WRAP) ? adjustedWrapLength : wrapLength));
    results.addAll(lines);
    return results;
  }
  
  /**
   * Pretty-prints the top-most error message from a Throwable with word-wrap and indenting. 
   * 
   * @param t
   * @param wrapLength
   * @param includeThrowableClass
   * @param includeStackElement
   * @see CycStringUtils#toPrettyMessage(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessage(
          final Throwable t, 
          final int wrapLength, 
          final boolean includeThrowableClass,
          final boolean includeStackElement) {
    return toPrettyMessage(t, null, null, wrapLength, includeThrowableClass, includeStackElement);
  }
  
  /**
   * Pretty-prints the top-most error message from a Throwable with word-wrap and indenting. 
   * 
   * @see CycStringUtils#toPrettyMessage(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @param firstIndent
   * @param wrapLength
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessage(
          final Throwable t, final String firstIndent, final int wrapLength) {
    return toPrettyMessage(t, firstIndent, null, wrapLength, 
            DEFAULT_THROWN_INCLUDE_THROWABLE_CLASS, DEFAULT_THROWN_INCLUDE_STACK_ELEMENT);
  }
  
  /**
   * Pretty-prints the top-most error message from a Throwable with word-wrap and indenting. 
   * 
   * @see CycStringUtils#toPrettyMessage(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @param wrapLength
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessage(final Throwable t, final int wrapLength) {
    return toPrettyMessage(t, null, wrapLength);
  }
  
  /**
   * Pretty-prints the top-most error message from a Throwable with word-wrap and indenting. 
   * 
   * @see CycStringUtils#toPrettyMessage(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessage(final Throwable t) {
    return toPrettyMessage(t, DISABLE_WORD_WRAP);
  }
  
  /**
   * Recursively pretty-prints the error messages from a Throwable and all of its causes, with
   * word-wrap and indenting. For example:
   * <pre><code>
   * > CycApiException: com.cyc.baseclient.datatype.CycStringUtilsTest.getMockException(StringUtilsTest.java:254)
   *   Important CycApiException: Here's a Cyc API exception...
   *   which spans multiple lines
   *   > UnsupportedOperationException: com.cyc.baseclient.datatype.CycStringUtilsTest.getMockException(StringUtilsTest.java:252)
   *     Which wraps an UnsupportedOperationException
   *     > SQLException: com.cyc.baseclient.datatype.CycStringUtilsTest.getMockException(StringUtilsTest.java:246)
   *       And here's a random SQL exception...
   *       ... whose message spans...
   *       (some really very extremely and deliberately excessively over-verbose and ridiculously long lines which just keep going
   *       and going and going for...)
   *       ... multiple lines!
   *       > NullPointerException: com.cyc.baseclient.datatype.CycStringUtilsTest.getMockException(StringUtilsTest.java:245)
   *         And finally, some random NullPointerException.
   * </code></pre>
   * 
   * Or:
   * <pre><code>
   * +-Important CycApiException: Here's a Cyc API exception...
   * |---which spans multiple lines
   *   +-Which wraps an UnsupportedOperationException
   *     +-And here's a random SQL exception...
   *     |---... whose message spans...
   *     |---(some really very extremely and deliberately
   *     |---excessively over-verbose and ridiculously long lines
   *     |---which just keep going and going and going for...)
   *     |---... multiple lines!
   *       +-And finally, some random NullPointerException.
   * </code></pre>
   * 
   * <p>To pretty-print <em>only the top-most</em> error message from a Throwable, see the
   * {@link #toPrettyMessage(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) }
   * method.
   * 
   * <p>The result is returned as a list of strings, with each string representing one line, making 
   * it easy to feed to a logger. The list can also be fed to a PrintStream or a Writer via 
   * {@link #printlns(java.util.Collection, java.io.PrintStream)} or 
   * {@link #write(java.util.Collection, java.io.Writer, boolean)},
   * respectively, and of course can easily be concatenated back into a single String by using 
   * something like <code>org.apache.commons.lang3.StringUtils.join(list, "\n")</code>
   * 
   * @see CycStringUtils#DISABLE_WORD_WRAP
   * @see CycStringUtils#toPrettyMessage(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * @see CycStringUtils#printlns(java.util.Collection, java.io.PrintStream)
   * @see CycStringUtils#write(java.util.Collection, java.io.Writer, boolean)
   * 
   * @param t the Throwable to pretty-print
   * @param firstIndent the string which is prepended to the first line
   * @param restIndent the string which is prepended to each subsequent line
   * @param wrapLength the column to wrap the words at, less than 1 disables word wrap
   * @param includeThrowableClass whether to include the classname of the Throwable
   * @param includeStackElement whether to include the Throwable's first stack element
   * @return a list of strings, one string per line
   */
  public static List<String> toPrettyMessages(
          final Throwable t, 
          final String firstIndent,
          final String restIndent, 
          final int wrapLength,
          final boolean includeThrowableClass,
          final boolean includeStackElement) {
    final List<String> results = new ArrayList();
    if (t == null) {
      return null;
    }
    final String realFirstIndent = Objects.toString(firstIndent, DEFAULT_THROWN_FIRST_INDENT);
    final String realRestIndent  = Objects.toString(restIndent, 
            Objects.toString(restIndent, leftPad("", realFirstIndent.length())));
    String currFirstIndent = "";
    String currRestIndent;
    Throwable currCause = t;
    while (currCause != null) {
      final String padding = leftPad("", currFirstIndent.length());
      currFirstIndent = padding + realFirstIndent;
      currRestIndent  = padding + realRestIndent;
      results.addAll(
              toPrettyMessage(
                      currCause, currFirstIndent, currRestIndent, wrapLength,
                      includeThrowableClass, includeStackElement));
      currCause = currCause.getCause();
    }
    return results;
  }
  
  /**
   * Recursively pretty-prints the error messages from a Throwable and all of its causes, with
   * word-wrap and indenting.
   * 
   * @see CycStringUtils#toPrettyMessages(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @param wrapLength
   * @param includeThrowableClass
   * @param includeStackElement
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessages(
          final Throwable t,
          final int wrapLength, 
          final boolean includeThrowableClass,
          final boolean includeStackElement) {
    return toPrettyMessages(t, null, null, wrapLength, includeThrowableClass, includeStackElement);
  }
  
  /**
   * Recursively pretty-prints the error messages from a Throwable and all of its causes, with
   * word-wrap and indenting.
   * 
   * @see CycStringUtils#toPrettyMessages(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @param firstIndent
   * @param wrapLength
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessages(
          final Throwable t, final String firstIndent, final int wrapLength) {
    return toPrettyMessages(t, firstIndent, null, wrapLength,
            DEFAULT_THROWN_INCLUDE_THROWABLE_CLASS, DEFAULT_THROWN_INCLUDE_STACK_ELEMENT);
  }
  
  /**
   * Recursively pretty-prints the error messages from a Throwable and all of its causes, with
   * word-wrap and indenting.
   * 
   * @see CycStringUtils#toPrettyMessages(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @param wrapLength
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessages(final Throwable t, final int wrapLength) {
    return toPrettyMessages(t, null, wrapLength);
  }
  
  /**
   * Recursively pretty-prints the error messages from a Throwable and all of its causes, with
   * word-wrap and indenting.
   * 
   * @see CycStringUtils#toPrettyMessages(java.lang.Throwable, java.lang.String, java.lang.String, int, boolean, boolean) 
   * 
   * @param t
   * @return a list of pretty-printed error messages
   */
  public static List<String> toPrettyMessages(final Throwable t) {
    return toPrettyMessages(t, DISABLE_WORD_WRAP);
  }
  
  
  /* ====  CycObject formatting  ================================================================ */
  
  public static boolean CYCLIFY_DEFAULT = true;
  public static int DISABLE_WORD_WRAP = 0;
  
  public static Object possiblyConvertToCycObject(final Object obj) {
    return (obj instanceof KbObject) ? ((KbObject) obj).getCore() : obj;
  }
  
  public static CycList possiblyConvertToCycList(final Object srcObj) {
    final Object obj = possiblyConvertToCycObject(srcObj);
    if (obj instanceof CycList) {
      return (CycList) obj;
    }
    if (obj instanceof Formula) {
      return ((Formula) obj).toCycList();
    }
    if (obj instanceof NonAtomicTerm) {
      return ((NonAtomicTerm) obj).toCycList();
    }
    return null;
  }
  
  public static String possiblyCyclify(final Object srcObj) {
    final Object obj = possiblyConvertToCycObject(srcObj);
    if (obj instanceof CycObject) {
      return ((CycObject) obj).cyclify();
    }
    return (obj != null) ? obj.toString() : null;
  }
  
  public static String[] cyclistToStrings(final CycList list, final boolean cyclify) {
    final String str = (cyclify)
            ? list.toPrettyCyclifiedString("")
            : list.toPrettyString("");
    return str.split("\n");
  }
  
  public static String[] cyclistToStrings(final CycList list) {
    return cyclistToStrings(list, CYCLIFY_DEFAULT);
  }
  
  public static String[] wordWrapOrPrettyPrintCycL(
          final Object obj, final int wrapLength, final boolean cyclify) {
    final String str = "" + ((cyclify) ? possiblyCyclify(obj) : obj);
    if ((str == null) || (str.length() <= wrapLength)) {
      return new String[] {str};
    }
    final CycList cyclist = possiblyConvertToCycList(obj);
    return (cyclist != null) 
            ? cyclistToStrings(cyclist, cyclify)
            : WordUtils.wrap(str, wrapLength).split("\n");
  }
  
  public static String[] wordWrapOrPrettyPrintCycL(final Object obj, final int wrapLength) {
    return wordWrapOrPrettyPrintCycL(obj, wrapLength, CYCLIFY_DEFAULT);
  }
  
  /**
   * Wraps a string, maintaining the indentation from the first line across all subsequent lines
   * 
   * @param str
   * @param wrapLength
   * @param restPrefix
   * @return a string with lines wrapped at wrapLength
   */
  public static List<String> wordWrapIndented(final String str, final int wrapLength, final String restPrefix) {
    final String content = stripStart(str, null);
    final String indent  = removeEnd(str, content);
    final String wrappedStr = (wrapLength > DISABLE_WORD_WRAP) ? WordUtils.wrap(str, wrapLength) : str;
    return prependFirstLine(indent, ((restPrefix != null) ? restPrefix : indent), split(wrappedStr, "\n"));
  }
  
  public static List<String> wordWrapIndented(final String str, final int wrapLength) {
    return wordWrapIndented(str, wrapLength, null);
  }
  
  public static List<String> wordWrapIndented(final String[] strs, final int wrapLength, final String restPrefix) {
    final List<String> results = new ArrayList();
    for (String str : strs) {
      results.addAll(wordWrapIndented(str, wrapLength, restPrefix));
    }
    return results;
  }
  
  public static List<String> wordWrapIndented(final String[] strs, final int wrapLength) {
    return wordWrapIndented(strs, wrapLength, null);
  }
  
  
  /* ====  Printing / Output  =================================================================== */
  
  public static void printlns(
          final Collection objs, final PrintStream out, final String nullValueString) {
    for (Object obj : objs) {
      out.println(Objects.toString(obj, nullValueString));
    }
  }
  
  public static void printlns(final Collection objs) {
    printlns(objs, System.out);
  }
  
  public static void printlns(final Collection objs, final String nullValueString) {
    printlns(objs, System.out, nullValueString);
  }
  
  public static void printlns(final Collection objs, final PrintStream out) {
    printlns(objs, out, DEFAULT_NULL_STRING);
  }
  
  public static void printlns(final Map map, final PrintStream out) {
    printlns(toStrings(map, "[", "]:[", "]", false), out);
  }
  
  public static void write(
          final Collection objs, final Writer writer,
          final boolean addLineBreaks, final String nullValueString) 
          throws IOException {
    final String lineEnd = addLineBreaks ? "\n" : "";
    for (Object obj : objs) {
      writer.write(Objects.toString(obj, nullValueString) + lineEnd);
    }
  }
  
  public static void write(
          final Collection objs, final Writer writer, final boolean addLineBreaks) 
          throws IOException {
    write(objs, writer, addLineBreaks, DEFAULT_NULL_STRING);
  }
  
}
