/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackFunction;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodePnLFunction;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;

/**
 *
 */
public class FXDigitalOptionCallSpreadBlackPnLSeriesCurrencyConversionFunction extends PnlSeriesCurrencyConversionFunction {

  public FXDigitalOptionCallSpreadBlackPnLSeriesCurrencyConversionFunction(final String currencyMatrixName) {
    super(currencyMatrixName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!super.canApplyTo(context, target)) {
      return false;
    }
    final Security security = target.getPositionOrTrade().getSecurity();
    return security instanceof FXDigitalOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, FXDigitalCallSpreadBlackFunction.CALL_SPREAD_BLACK_METHOD)
        .withAny(FXOptionBlackFunction.PUT_CURVE)
        .withAny(FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG)
        .withAny(FXOptionBlackFunction.CALL_CURVE)
        .withAny(FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .withAny(InterpolatedDataProperties.X_INTERPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME)
        .withAny(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME)
        .withAny(FXDigitalCallSpreadBlackFunction.PROPERTY_CALL_SPREAD_VALUE)
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .with(YieldCurveNodePnLFunction.PROPERTY_PNL_CONTRIBUTIONS, ValueRequirementNames.FX_CURRENCY_EXPOSURE).get();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

}
