/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.securityconverters.StubCalculator.StubType;

/**
 * 
 */
public class ActualActualICMANormal extends ActualTypeDayCount {

  @Override
  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear) {
    return getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, StubType.NONE);
  }

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear,
      final StubType stubType) {
    testDates(previousCouponDate, date, nextCouponDate);
    Validate.notNull(stubType, "stub type");
    final LocalDate previous = previousCouponDate.toLocalDate();
    final LocalDate next = nextCouponDate.toLocalDate();
    long daysBetween, daysBetweenCoupons;
    final long previousCouponDateJulian = previous.toModifiedJulianDays();
    final long nextCouponDateJulian = next.toModifiedJulianDays();
    final long dateJulian = date.toLocalDate().toModifiedJulianDays();
    final int months = 12 / paymentsPerYear;
    switch (stubType) {
      case NONE:
        daysBetween = dateJulian - previousCouponDateJulian;
        daysBetweenCoupons = next.toModifiedJulianDays() - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      case SHORT_START:
        final LocalDate notionalStart = next.minusMonths(months);
        daysBetweenCoupons = nextCouponDateJulian - notionalStart.toLocalDate().toModifiedJulianDays();
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      case LONG_START:
        long firstNotionalJulian = next.minusMonths(months * 2).toModifiedJulianDays();
        long secondNotionalJulian = next.minusMonths(months).toModifiedJulianDays();
        long daysBetweenStub = secondNotionalJulian - previousCouponDateJulian;
        double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        if (dateJulian > secondNotionalJulian) {
          daysBetween = dateJulian - secondNotionalJulian;
          return coupon * (daysBetweenStub / daysBetweenTwoNotionalCoupons + 1) / paymentsPerYear;
        }
        daysBetween = dateJulian - firstNotionalJulian;
        return coupon * (daysBetween / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      case SHORT_END:
        final LocalDate notionalEnd = previous.plusMonths(months);
        daysBetweenCoupons = notionalEnd.toModifiedJulianDays() - previousCouponDateJulian;
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      case LONG_END:
        firstNotionalJulian = previous.plusMonths(months).toModifiedJulianDays();
        secondNotionalJulian = previous.plusMonths(2 * months).toModifiedJulianDays();
        final long daysBetweenPreviousAndFirstNotional = firstNotionalJulian - previousCouponDateJulian;
        if (dateJulian < firstNotionalJulian) {
          daysBetween = dateJulian - previousCouponDateJulian;
          return coupon * daysBetween / daysBetweenPreviousAndFirstNotional / paymentsPerYear;
        }
        daysBetweenStub = dateJulian - firstNotionalJulian;
        daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        return coupon * (1 + daysBetweenStub / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      default:
        throw new IllegalArgumentException("Cannot handle stub type " + stubType);
    }
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    throw new NotImplementedException("Cannot get daycount fraction; need information about the coupon and payment frequency");
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual ICMA Normal";
  }

}
