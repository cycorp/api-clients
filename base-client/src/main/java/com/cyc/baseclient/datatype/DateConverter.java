package com.cyc.baseclient.datatype;

/*
 * #%L
 * File: DateConverter.java
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
import com.cyc.base.annotation.CycObjectLibrary;
import com.cyc.base.annotation.CycTerm;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.CycApiException;
import static com.cyc.baseclient.CommonConstants.DAY_FN;
import static com.cyc.baseclient.CommonConstants.HOUR_FN;
import static com.cyc.baseclient.CommonConstants.MILLISECOND_FN;
import static com.cyc.baseclient.CommonConstants.MINUTE_FN;
import static com.cyc.baseclient.CommonConstants.MONTH_FN;
import static com.cyc.baseclient.CommonConstants.SECOND_FN;
import static com.cyc.baseclient.CommonConstants.YEAR_FN;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.cycobject.NautImpl;
import static com.cyc.baseclient.datatype.TimeGranularity.*;
import com.cyc.baseclient.exception.CycParseException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * <P>
 * DateConverter is designed to convert java-style dates to their corresponding CycL representations
 * and vice versa.
 *
 * @see java.util.Date
 * @see java.util.Calendar
 * @see com.cyc.base.cycobject.Naut
 *
 * @author baxter
 * @since January 15, 2008, 5:33 PM
 * @version $Id: DateConverter.java 176591 2018-01-09 17:27:27Z nwinant $
 */
@CycObjectLibrary
public class DateConverter extends DataTypeConverter<Date> {

  private static final DateConverter SHARED_INSTANCE = new DateConverter();

  //// Constructors
  /**
   * Creates a new instance of DateConverter.
   */
  private DateConverter() {
  }

  //// Public Area
  /**
   * Returns an instance of <code>DateConverter</code>.
   *
   * If an instance has already been created, the existing one will be returned. Otherwise, a new
   * one will be created.
   *
   * @return The singleton instance of this class.
   */
  public static synchronized DateConverter getInstance() {
    DateConverter dateConverter = SHARED_INSTANCE;
    if (dateConverter == null) {
      dateConverter = new DateConverter();
    }
    return dateConverter;
  }

  /**
   * Try to parse <code>cycList</code> into a java <code>Date</code>
   *
   * If the parse fails, prints a stack trace iff <code>shouldReportFailure</code> is non-null, and
   * returns null.
   *
   * The Cyc date is assumed to be in the default time zone.
   *
   * <p>
   * Deprecated: Use CycNaut version.
   *
   * @param cycList
   * @param shouldReportFailure
   * @return the Date object corresponding to naut, or null if parse fails. The Cyc date is assumed
   * to be in the default time zone.
   * @see TimeZone#getDefault
   */
  @Deprecated
  static public Date parseCycDate(final CycList cycList,
          final boolean shouldReportFailure) {
    final Object naut = NautImpl.convertIfPromising(cycList);
    if (naut instanceof Naut) {
      return parseCycDate((Naut) naut, shouldReportFailure);
    } else if (shouldReportFailure) {
      new IllegalArgumentException(cycList + " cannot be interpreted as a NAUT").printStackTrace();
    }
    return null;
  }

  /**
   * Try to parse <code>naut</code> into a java <code>Date</code>
   *
   * @param naut a date-denoting Cyc NAUT.
   * @param shouldReportFailure If true, and the parse fails, prints a stack trace.
   * @return the Date object corresponding to naut, or null if parse fails. The Cyc date is assumed
   * to be in the default time zone.
   * @see TimeZone#getDefault
   */
  static public Date parseCycDate(final Naut naut,
          final boolean shouldReportFailure) {
    return getInstance().parse(naut, shouldReportFailure);
  }

  /**
   * Try to parse <code>cycList</code> into a java <code>Date</code> in a given time zone.
   *
   * If the parse fails, prints a stack trace iff <code>shouldReportFailure</code> is non-null, and
   * returns null.
   *
   * <p>
   * Deprecated: Use CycNaut version.
   *
   * @param cycList
   * @param timeZone
   * @param shouldReportFailure
   * @return the java.util.Date representation of <tt>cycList</tt>
   */
  @Deprecated
  static public Date parseCycDate(final CycList cycList, final TimeZone timeZone,
          final boolean shouldReportFailure) {
    final Object naut = NautImpl.convertIfPromising(cycList);
    if (naut instanceof Naut) {
      return parseCycDate((Naut) naut, timeZone, shouldReportFailure);
    } else if (shouldReportFailure) {
      new IllegalArgumentException(cycList + " cannot be converted to a NAUT.").printStackTrace();
    }
    return null;
  }

  /**
   * Try to parse <code>naut</code> into a java <code>Date</code> in a given time zone.
   *
   * If the parse fails, prints a stack trace iff <code>shouldReportFailure</code> is non-null, and
   * returns null.
   *
   * @param naut
   * @param timeZone
   * @param shouldReportFailure
   * @return the java.util.Date representation of <tt>naut</tt>
   */
  static public Date parseCycDate(final Naut naut, final TimeZone timeZone,
          final boolean shouldReportFailure) {
    try {
      return naut2Date(naut, timeZone);
    } catch (CycParseException ex) {
      return getInstance().handleParseException(ex, shouldReportFailure);
    }
  }

  /**
   * Try to parse <code>cycList</code> into a java <code>Date</code>
   *
   * Prints stack trace and returns null if the parse fails.
   *
   * The Cyc date is assumed to be in the default time zone.
   *
   * <p>
   * Deprecated: Use CycNaut version.
   *
   * @param cycList
   * @return the corresponding Date object
   * @see TimeZone#getDefault
   */
  @Deprecated
  static public Date parseCycDate(final CycList cycList) {
    return getInstance().parse(cycList);
  }

  /**
   * Try to parse <code>naut</code> into a java <code>Date</code>
   *
   * Prints stack trace and returns null if the parse fails.
   *
   * @param naut
   * @return the corresponding Date object
   *
   * The Cyc date is assumed to be in the default time zone.
   * @see TimeZone#getDefault
   */
  static public Date parseCycDate(final Naut naut) {
    return getInstance().parse(naut);
  }

  /**
   * <p>
   * Deprecated: Use CycNaut version.
   *
   * @param cycDate
   * @return the precision of <tt>cycDate</tt> as a Calendar constant int
   */
  @Deprecated
  public static TimeGranularity getCycDatePrecision(CycList cycDate) {
    return getCycDatePrecision(new NautImpl(cycDate));
  }

  /**
   * @param cycDate a date-denoting Cyc NAUT.
   * @return the precision of <tt>cycDate</tt> as a Calendar constant int.
   */
  public static TimeGranularity getCycDatePrecision(Naut cycDate) {
    final Object fn = cycDate.getOperator();
    if (YEAR_FN.equals(fn)) {
      return YEAR;
    }
    if (MONTH_FN.equals(fn)) {
      return MONTH;
    }
    if (DAY_FN.equals(fn)) {
      return DAY;
    }
    if (HOUR_FN.equals(fn)) {
      return HOUR;
    }
    if (MINUTE_FN.equals(fn)) {
      return MINUTE;
    }
    if (SECOND_FN.equals(fn)) {
      return SECOND;
    }
    if (MILLISECOND_FN.equals(fn)) {
      return MILLISECOND;
    }
    return null;
  }

  /**
   * Convert the date in <code>date</code> to a CycL date term.
   *
   * @param date The date to be converted
   * @param granularity Indicates the desired granularity of the CycL term. Should be an
   * <code>int</code> constant from this class, e.g. <code>DateConverter.YEAR_GRANULARITY</code> or
   * <code>DateConverter.SECOND_GRANULARITY</code>.
   * @return The Cyc term corresponding to date.
   *
   */
  public static Naut toCycDate(final Date date, final TimeGranularity granularity) {
    return date2Naut(date, granularity);
  }

  /**
   * Convert the date in <code>date</code> to a CycL date term.
   *
   * @param date The date to be converted
   * @return The Cyc term corresponding to date.
   *
   */
  public static CycObject toCycDate(Date date) {
    return date2Naut(date, TimeGranularity.guessGranularity(date));
  }

  /**
   * Convert the date in <code>calendar</code> to a CycL date term.
   *
   * @param calendar
   * @param granularity Indicates the desired granularity of the CycL term. Should be an
   * <code>int</code> constant from this class, e.g. <code>DateConverter.YEAR_GRANULARITY</code> or
   * <code>DateConverter.SECOND_GRANULARITY</code>.
   * @return the Naut representation of <tt>calendar</tt>
   * @see com.cyc.base.cycobject.Naut
   *
   */
  public static Naut toCycDate(final Calendar calendar, final TimeGranularity granularity) {
    return calendar2Naut(calendar, granularity);
  }

  //// Protected Area
  @Override
  protected Date fromCycTerm(final CycObject cycObject) throws CycParseException {
    final Naut naut;
    try {
      naut = (Naut) NautImpl.convertIfPromising(cycObject);
    } catch (ClassCastException ex) {
      throw new IllegalArgumentException();
    }
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    updateCalendar(naut, calendar);
    return calendar.getTime();
  }

  @Override
  protected Naut toCycTerm(Date date) {
    return date2Naut(date, TimeGranularity.guessGranularity(date));
  }

  //// Private Area
  static private Naut date2Naut(Date date, final TimeGranularity granularity) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar2Naut(calendar, granularity);
  }

  private static Naut calendar2Naut(final Calendar calendar,
          final TimeGranularity granularity) {
    Naut dateNaut = new NautImpl(YEAR_FN, YEAR.getCalendarValue(calendar));
//      if (granularity == WEEK_GRANULARITY) {
//        dateNaut = new NautImpl(WEEK_FN, WEEK_GRANULARITY.getCalendarValue(calendar), dateNaut);
//      } else
    if (granularity.isGreaterThan(YEAR)) {
      dateNaut = new NautImpl(MONTH_FN, lookupMonth(MONTH.getCalendarValue(calendar)), dateNaut);
      if (granularity.isGreaterThan(MONTH)) {
        dateNaut = new NautImpl(DAY_FN, DAY.getCalendarValue(calendar), dateNaut);
        if (granularity.isGreaterThan(DAY)) {
          dateNaut = new NautImpl(HOUR_FN, HOUR.getCalendarValue(calendar), dateNaut);
          if (granularity.isGreaterThan(Calendar.HOUR)) {
            dateNaut = new NautImpl(MINUTE_FN, MINUTE.getCalendarValue(calendar), dateNaut);
            if (granularity.isGreaterThan(MINUTE)) {
              dateNaut = new NautImpl(SECOND_FN, SECOND.getCalendarValue(calendar), dateNaut);
              if (granularity.isGreaterThan(SECOND)) {
                dateNaut = new NautImpl(MILLISECOND_FN, MILLISECOND.getCalendarValue(calendar),
                        dateNaut);
              }
            }
          }
        }
      }
    }
    return dateNaut;
  }

  private static Date naut2Date(final Naut naut, final TimeZone timeZone) throws CycParseException {
    return naut2Calendar(naut, timeZone).getTime();
  }

  private static Calendar naut2Calendar(final Naut naut,
          final TimeZone timeZone) throws CycParseException {
    final Calendar calendar = Calendar.getInstance();
    calendar.clear();
    updateCalendar(naut, calendar);
    calendar.set(Calendar.ZONE_OFFSET, timeZone.getRawOffset());
    return calendar;
  }

  /**
   * Set the time on <code>calendar</code> based on the CycL date <code>naut</code>
   */
  static private void updateCalendar(final Naut naut, final Calendar calendar) throws CycParseException {
    final int arity = naut.getArity();
    final DenotationalTerm functor = naut.getFunctor();
    if (arity < 1 || arity > 2) {
      throwParseException(naut);
    }
    final Object arg1 = naut.getArg(1);
    if (arity == 1 && YEAR_FN.equals(functor)) {
      final Integer yearNum = parseInteger(arg1, "year number");
      YEAR.setCalendarValue(calendar, yearNum);
    } else if (arity == 1) {
      throwParseException(naut);
    } else {
      final Object arg2 = naut.getArg(2);
      if (!(arg2 instanceof Naut)) {
        throwParseException(arg2);
      }
      if (MONTH_FN.equals(functor)) {
        if (!(arg1 instanceof CycConstantImpl)) {
          throw new CycParseException(arg1 + " is not a valid CycL month.");
        }
        final int monthNum = lookupMonthNum((CycConstantImpl) arg1);
        if (monthNum < Calendar.JANUARY || monthNum > Calendar.DECEMBER) {
          throw new CycParseException(arg1 + " is not a valid CycL month.");
        }
        updateCalendar((Naut) arg2, calendar);
        MONTH.setCalendarValue(calendar, monthNum);
      } else if (DAY_FN.equals(functor)) {
        final Object dayNum = parseInteger(arg1, "day number");
        updateCalendar((Naut) arg2, calendar);
        DAY.setCalendarValue(calendar, (Integer) dayNum);
      } else if (HOUR_FN.equals(functor)) {
        final Object hourNum = parseInteger(arg1, "hour number");
        updateCalendar((Naut) arg2, calendar);
        HOUR.setCalendarValue(calendar, (Integer) hourNum);
      } else if (MINUTE_FN.equals(functor)) {
        final Object minuteNum = parseInteger(arg1, "minute number");
        updateCalendar((Naut) arg2, calendar);
        MINUTE.setCalendarValue(calendar, (Integer) minuteNum);
      } else if (SECOND_FN.equals(functor)) {
        final Object secondNum = Integer.valueOf(arg1.toString());
        if (!(secondNum instanceof Integer && (Integer) secondNum >= 0
                && (Integer) secondNum < TimeGranularity.SECONDS_IN_A_MINUTE)) {
          throw new CycParseException(secondNum + " is not a valid second number.");
        }
        updateCalendar((Naut) arg2, calendar);
        SECOND.setCalendarValue(calendar, (Integer) secondNum);
      } else if (MILLISECOND_FN.equals(functor)) {
        final Object millisecondNum = parseInteger(arg1, "millisecond number");
        updateCalendar((Naut) arg2, calendar);
        MILLISECOND.setCalendarValue(calendar, (Integer) millisecondNum);
      } else {
        throwParseException(naut);
      }
    }
  }

  static public CycConstantImpl lookupSeason(final String season) {
    switch (season) {
      case "SU":
        return SUMMER;
      case "SP":
        return SPRING;
      case "FA":
        return FALL;
      case "WI":
        return WINTER;
      default:
        return null;
    }
  }

  static private CycConstantImpl lookupMonth(final int month) {
    ensureMonthArrayInitialized();
    return CYC_MONTH_TERMS[month];
  }

  static private int lookupMonthNum(CycConstantImpl cycMonth) {
    ensureMonthArrayInitialized();
    for (int monthNum = Calendar.JANUARY; monthNum <= Calendar.DECEMBER; monthNum++) {
      if (cycMonth.equals(CYC_MONTH_TERMS[monthNum])) {
        return monthNum;
      }
    }
    return -1;
  }

  private static void ensureMonthArrayInitialized() {
    if (CYC_MONTH_TERMS == null) {
      initializeCycMonthTerms();
    }
  }

  private static void initializeCycMonthTerms() {
    CYC_MONTH_TERMS = new CycConstantImpl[12];
    CYC_MONTH_TERMS[Calendar.JANUARY] = JANUARY;
    CYC_MONTH_TERMS[Calendar.FEBRUARY] = FEBRUARY;
    CYC_MONTH_TERMS[Calendar.MARCH] = MARCH;
    CYC_MONTH_TERMS[Calendar.APRIL] = APRIL;
    CYC_MONTH_TERMS[Calendar.MAY] = MAY;
    CYC_MONTH_TERMS[Calendar.JUNE] = JUNE;
    CYC_MONTH_TERMS[Calendar.JULY] = JULY;
    CYC_MONTH_TERMS[Calendar.AUGUST] = AUGUST;
    CYC_MONTH_TERMS[Calendar.SEPTEMBER] = SEPTEMBER;
    CYC_MONTH_TERMS[Calendar.OCTOBER] = OCTOBER;
    CYC_MONTH_TERMS[Calendar.NOVEMBER] = NOVEMBER;
    CYC_MONTH_TERMS[Calendar.DECEMBER] = DECEMBER;
  }

  public static boolean isCycDate(Object object) {
    if (object instanceof CycList) {
      return parseCycDate((CycList) object, false) != null;
    } else if (object instanceof Naut) {
      return parseCycDate((Naut) object, false) != null;
    } else {
      return false;
    }
  }

  /**
   * Returns the XML datetime string corresponding to the given CycL date.
   *
   * This function was formerly in com.cyc.baseclient.CycClient#xmlDatetimeString.
   *
   * @param date the date naut
   * @return the XML datetime string corresponding to the given CycL date
   * @throws java.io.IOException
   */
  public static String toXMLDatetimeString(final CycList date) throws IOException, CycApiException {
    try {
      final Naut dateNaut = (Naut) NautImpl.convertIfPromising(date);
      final Date javadate = DateConverter.parseCycDate(dateNaut,
              TimeZone.getDefault(), false);
      final TimeGranularity precision = getCycDatePrecision(dateNaut);
      return toXMLDatetimeString(javadate, precision);
      /*
      if (precision > DateConverter.DAY_GRANULARITY) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(javadate);
      } else {
        return new SimpleDateFormat("yyyy-MM-dd").format(javadate);
      }
       */
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns the XML datetime string corresponding to a given Date
   *
   * @param date the Java Date
   * @param precision
   * @return the XML datetime string corresponding to the given date
   * @throws CycApiException
   */
  public static String toXMLDatetimeString(final Date date, TimeGranularity precision) {
    try {
      //if (precision.intValue() > DateConverter.DAY_GRANULARITY) {
      if (precision.isGreaterThan(DAY)) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
      } else {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
      }
    } catch (Exception e) {
      return null;
    }
  }

  //// Internal Rep
  @CycTerm(cycl = "#$SeasonFn")
  public static final CycConstantImpl SEASON_FN = new CycConstantImpl("SeasonFn",
          new GuidImpl("c0fbe0cd-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$January")
  public static final CycConstantImpl JANUARY = new CycConstantImpl("January",
          new GuidImpl("bd58b833-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$February")
  public static final CycConstantImpl FEBRUARY = new CycConstantImpl("February",
          new GuidImpl("bd58c2f7-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$March")
  public static final CycConstantImpl MARCH = new CycConstantImpl("March",
          new GuidImpl("bd58c2bd-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$April")
  public static final CycConstantImpl APRIL = new CycConstantImpl("April",
          new GuidImpl("bd58c279-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$May")
  public static final CycConstantImpl MAY = new CycConstantImpl("May",
          new GuidImpl("bd58c232-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$June")
  public static final CycConstantImpl JUNE = new CycConstantImpl("June",
          new GuidImpl("bd58c1f0-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$July")
  public static final CycConstantImpl JULY = new CycConstantImpl("July",
          new GuidImpl("bd58c1ad-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$August")
  public static final CycConstantImpl AUGUST = new CycConstantImpl("August",
          new GuidImpl("bd58c170-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$September")
  public static final CycConstantImpl SEPTEMBER = new CycConstantImpl("September",
          new GuidImpl("bd58c131-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$October")
  public static final CycConstantImpl OCTOBER = new CycConstantImpl("October",
          new GuidImpl("bd58c0ef-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$November")
  public static final CycConstantImpl NOVEMBER = new CycConstantImpl("November",
          new GuidImpl("bd58c0a5-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$December")
  public static final CycConstantImpl DECEMBER = new CycConstantImpl("December",
          new GuidImpl("bd58b8ba-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$CalendarSpring")
  public static final CycConstantImpl SPRING = new CycConstantImpl("CalendarSpring",
          new GuidImpl("be011735-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$CalendarSummer")
  public static final CycConstantImpl SUMMER = new CycConstantImpl("CalendarSummer",
          new GuidImpl("be011768-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$CalendarAutumn")
  public static final CycConstantImpl FALL = new CycConstantImpl("CalendarAutumn",
          new GuidImpl("be011790-9c29-11b1-9dad-c379636f7270"));

  @CycTerm(cycl = "#$CalendarWinter")
  public static final CycConstantImpl WINTER = new CycConstantImpl("CalendarWinter",
          new GuidImpl("be0116f3-9c29-11b1-9dad-c379636f7270"));

  // Commented out regarding BASEAPI-63 - nwinant, 2014-08-18
  /*
  @CycTerm(cycl="#$NthSpecifiedDateTypeOfSubsumingDateFn")
  public static final CycConstantImpl NTH_SPECIFIED_DATE_TYPE_OF_SUBSUMING_DATE_FN =
          new CycConstantImpl("NthSpecifiedDateTypeOfSubsumingDateFn",
          new GuidImpl("fa33c621-7b6f-4eeb-9801-3acb990b0c8f"));
   */
  @CycTerm(cycl = "#$CalendarWeek")
  public static final CycConstantImpl CALENDAR_WEEK = new CycConstantImpl("CalendarWeek",
          new GuidImpl("bd58c064-9c29-11b1-9dad-c379636f7270"));

  private static CycConstantImpl[] CYC_MONTH_TERMS = null;
}
