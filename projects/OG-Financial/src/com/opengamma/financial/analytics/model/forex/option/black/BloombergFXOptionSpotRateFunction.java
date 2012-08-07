/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class BloombergFXOptionSpotRateFunction extends AbstractFunction.NonCompiledInvoker {
  private static final String PROPERTY_DATA_TYPE = "DataType";
  private static final String LIVE = "Live";
  private static final String LAST_CLOSE = "LastClose";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String dataType = desiredValue.getConstraint(PROPERTY_DATA_TYPE);
    final FXOptionSecurity security = (FXOptionSecurity) target.getSecurity();
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(security.getPutCurrency(), security.getCallCurrency());
    if (dataType.equals(LIVE)) {
      final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
      if (spotObject == null) {
        throw new OpenGammaRuntimeException("Could not get live market data for " + currencyPair);
      }
      final double spot = (Double) spotObject;
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
          createValueProperties().with(PROPERTY_DATA_TYPE, LIVE).get()), spot));
    } else if (dataType.equals(LAST_CLOSE)) {
      final Object spotObject = inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST);
      if (spotObject == null) {
        throw new OpenGammaRuntimeException("Could not get last close market data for " + currencyPair);
      }
      final double spot = (Double) spotObject;
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
          createValueProperties().with(PROPERTY_DATA_TYPE, LIVE).get()), spot));
    }
    throw new OpenGammaRuntimeException("Did not recognise property type " + dataType);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.SPOT_RATE_FOR_SECURITY, target.toSpecification(),
        createValueProperties().withAny(PROPERTY_DATA_TYPE).get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> dataTypes = desiredValue.getConstraints().getValues(PROPERTY_DATA_TYPE);
    if (dataTypes == null || dataTypes.size() != 1) {
      return null;
    }
    final String dataType = Iterables.getOnlyElement(dataTypes);
    final FXOptionSecurity security = (FXOptionSecurity) target.getSecurity();
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(security.getPutCurrency(), security.getCallCurrency());
    if (dataType.equals(LIVE)) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.SPOT_RATE, ComputationTargetType.PRIMITIVE, currencyPair.getUniqueId()));
    } else if (dataType.equals(LAST_CLOSE)) {
      return Collections.singleton(new ValueRequirement(ValueRequirementNames.HISTORICAL_TIME_SERIES_LATEST, ComputationTargetType.PRIMITIVE, currencyPair.getUniqueId()));
    }
    return null;
  }

  //  private UniqueId getBBGId(final UnorderedCurrencyPair currencyPair) {
  //    // Implementation note: the currency pair order in FX is given by FXUtils.
  //    if (FXUtils.isInBaseQuoteOrder(currencyPair.getFirstCurrency(), currencyPair.getSecondCurrency())) {
  //      return ExternalSchemes.bloombergTickerSecurityId(currencyPair.getFirstCurrency().getCode() + currencyPair.getSecondCurrency().getCode() + " Curncy");
  //    }
  //    return ExternalSchemes.bloombergTickerSecurityId(currencyPair.getSecondCurrency().getCode() + currencyPair.getFirstCurrency().getCode() + " Curncy");
  //  }
}