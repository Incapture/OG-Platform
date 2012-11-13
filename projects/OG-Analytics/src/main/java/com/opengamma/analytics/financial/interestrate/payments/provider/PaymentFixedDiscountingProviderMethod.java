/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.payments.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.util.surface.StringValue;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Methods related to fixed payments.
 */
public final class PaymentFixedDiscountingProviderMethod {

  /**
   * The method unique instance.
   */
  private static final PaymentFixedDiscountingProviderMethod INSTANCE = new PaymentFixedDiscountingProviderMethod();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static PaymentFixedDiscountingProviderMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private PaymentFixedDiscountingProviderMethod() {
  }

  /**
   * Compute the the present value of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param multicurves The multi-curve provider.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValue(PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(payment, "Payment");
    ArgumentChecker.notNull(multicurves, "Multi-curves");
    double pv = payment.getAmount() * multicurves.getDiscountFactor(payment.getCurrency(), payment.getPaymentTime());
    return MultipleCurrencyAmount.of(payment.getCurrency(), pv);
  }

  /**
   * Computes the present value curve sensitivity of a fixed payment by discounting.
   * @param payment The fixed payment.
   * @param multicurves The multi-curve provider.
   * @return The sensitivity.
   */
  public MultipleCurrencyMulticurveSensitivity presentValueCurveSensitivity(PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    final double time = payment.getPaymentTime();
    final DoublesPair s = new DoublesPair(time, -time * payment.getAmount() * multicurves.getDiscountFactor(payment.getCurrency(), time));
    final List<DoublesPair> list = new ArrayList<DoublesPair>();
    list.add(s);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(multicurves.getName(payment.getCurrency()), list);
    return MultipleCurrencyMulticurveSensitivity.of(payment.getCurrency(), MulticurveSensitivity.ofYieldDiscounting(result));
  }

  /**
   * Compute the the present value curve sensitivity of a fixed payment by discounting to a parallel curve movement.
   * @param payment The payment.
   * @param multicurves The multi-curve provider.
   * @return The sensitivity.
   */
  public StringValue presentValueParallelCurveSensitivity(PaymentFixed payment, final MulticurveProviderInterface multicurves) {
    final String curveName = payment.getFundingCurveName();
    final double time = payment.getPaymentTime();
    double sensitivity = -time * payment.getAmount() * multicurves.getDiscountFactor(payment.getCurrency(), time);
    return StringValue.from(curveName, sensitivity);
  }

}