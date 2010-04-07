/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Calendar;
import java.util.Date;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.FrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Utility methods for simple conversions.
 * 
 * @author Andrew Griffin
 */
/* package */ abstract class Converters {

  protected static Currency currencyBeanToCurrency(CurrencyBean currencyBean) {
    if (currencyBean == null) return null;
    return Currency.getInstance(currencyBean.getName());
  }
  
  protected static DomainSpecificIdentifier domainSpecificIdentifierBeanToDomainSpecificIdentifier(DomainSpecificIdentifierBean domainSpecificIdentifierBean) {
    if (domainSpecificIdentifierBean == null) return null;
    return new DomainSpecificIdentifier(domainSpecificIdentifierBean.getDomain(), domainSpecificIdentifierBean.getIdentifier());
  }
  
  protected static Expiry dateToExpiry(Date date) {
    if (date == null) return null;
    final Calendar c = Calendar.getInstance ();
    c.setTime (date);
    return new Expiry (ZonedDateTime.fromInstant (OffsetDateTime.midnight (c.get (Calendar.YEAR), c.get (Calendar.MONTH) + 1, c.get (Calendar.DAY_OF_MONTH), ZoneOffset.UTC), TimeZone.UTC));
  }
  
  protected static Date expiryToDate (Expiry expiry) {
    if (expiry == null) return null;
    // we're storing just as a date, so assert that the value we're storing isn't a vague month or year
    if (expiry.getAccuracy () != null) {
      if (expiry.getAccuracy () != ExpiryAccuracy.DAY_MONTH_YEAR) throw new OpenGammaRuntimeException ("Expiry is not to DAY_MONTH_YEAR precision");
    }
    return new Date (expiry.toInstant ().toEpochMillisLong ());
  }
  
  protected static LocalDate dateToLocalDate (Date date) {
    if (date == null) return null;
    final Calendar c = Calendar.getInstance ();
    c.setTime (date);
    return LocalDate.of (c.get (Calendar.YEAR), c.get (Calendar.MONTH) + 1, c.get (Calendar.DAY_OF_MONTH));
  }
  
  protected static Date localDateToDate (LocalDate date) {
    if (date == null) return null;
    return new Date (OffsetDateTime.midnightFrom (date, ZoneOffset.UTC).toInstant ().toEpochMillisLong ());
  }
  
  protected static Frequency frequencyBeanToFrequency (final FrequencyBean frequencyBean) {
    if (frequencyBean == null) return null;
    final Frequency f = FrequencyFactory.INSTANCE.getFrequency (frequencyBean.getName ());
    if (f == null) throw new OpenGammaRuntimeException ("Bad value for frequencyBean (" + frequencyBean.getName () + ")");
    return f;
  }
  
  protected static DayCount dayCountBeanToDayCount (final DayCountBean dayCountBean) {
    if (dayCountBean == null) return null;
    final DayCount dc = DayCountFactory.INSTANCE.getDayCount (dayCountBean.getName ());
    if (dc == null) throw new OpenGammaRuntimeException ("Bad value for dayCountBean (" + dayCountBean.getName () + ")");
    return dc;
  }
  
  protected static BusinessDayConvention businessDayConventionBeanToBusinessDayConvention (final BusinessDayConventionBean businessDayConventionBean) {
    if (businessDayConventionBean == null) return null;
    final BusinessDayConvention bdc = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention (businessDayConventionBean.getName ());
    if (bdc == null) throw new OpenGammaRuntimeException ("Bad value for businessDayConventionBean (" + businessDayConventionBean.getName () + ")");
    return bdc;
  }
  
  protected static GICSCode gicsCodeBeanToGICSCode (final GICSCodeBean gicsCodeBean) {
    if (gicsCodeBean == null) return null;
    return GICSCode.getInstance (gicsCodeBean.getName ());
  }
  
  protected static YieldConvention yieldConventionBeanToYieldConvention (final YieldConventionBean yieldConventionBean) {
    if (yieldConventionBean == null) return null;
    final YieldConvention yc = YieldConventionFactory.INSTANCE.getYieldConvention (yieldConventionBean.getName ());
    if (yc == null) throw new OpenGammaRuntimeException ("Bad value for yieldConventionBean (" + yieldConventionBean.getName () + ")");
    return yc;
  }

}