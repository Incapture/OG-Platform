/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ForwardPDESplineDefaults extends ForwardPDEDefaults {
  private final String _yInterpolator;
  private final String _yLeftExtrapolator;
  private final String _yRightExtrapolator;
  private final String _splineExtrapolatorFailBehaviour;

  public ForwardPDESplineDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator, //CSIGNORE
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String forwardCurveName, final String forwardCurveCalculationMethod, final String surfaceName,
      final String eps, final String theta, final String nTimeSteps, final String nSpaceSteps, final String timeStepBunching, final String spaceStepBunching,
      final String maxProxyDelta, final String centreMoneyness, final String spaceDirectionInterpolator, final String discountingCurveName,
      final String yInterpolator, final String yLeftExtrapolator, final String yRightExtrapolator, final String splineExtrapolatorFailBehaviour) {
    super(timeAxis, yAxis, volatilityTransform, timeInterpolator, timeLeftExtrapolator, timeRightExtrapolator, forwardCurveName, forwardCurveCalculationMethod,
        surfaceName, eps, theta, nTimeSteps, nSpaceSteps, timeStepBunching, spaceStepBunching, maxProxyDelta, centreMoneyness, spaceDirectionInterpolator,
        discountingCurveName);
    ArgumentChecker.notNull(yInterpolator, "y interpolator");
    ArgumentChecker.notNull(yLeftExtrapolator, "y left extrapolator");
    ArgumentChecker.notNull(yRightExtrapolator, "y right extrapolator");
    ArgumentChecker.notNull(splineExtrapolatorFailBehaviour, "spline extrapolator failure behaviour not set");
    _yInterpolator = yInterpolator;
    _yLeftExtrapolator = yLeftExtrapolator;
    _yRightExtrapolator = yRightExtrapolator;
    _splineExtrapolatorFailBehaviour = splineExtrapolatorFailBehaviour;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (final String valueRequirement : getValueRequirementNames()) {
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Set<String> surfaceDefaults = super.getDefaultValue(context, target, desiredValue, propertyName);
    if (surfaceDefaults != null) {
      return surfaceDefaults;
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_yInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_yLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_yRightExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SPLINE_EXTRAPOLATOR_FAILURE.equals(propertyName)) {
      return Collections.singleton(_splineExtrapolatorFailBehaviour);
    }
    return null;
  }

}
