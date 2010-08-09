/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class ModifiedDurationCalculator {

  private final BondYieldCalculator _byc = new BondYieldCalculator();
  private final MacaulayDurationCalculator _mdc = new MacaulayDurationCalculator();

  public double calculate(final Bond bond, final double dirtyPrice, int compoundingFrquency) {
    Validate.notNull(bond, "bond");
    if (dirtyPrice <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    if (compoundingFrquency <= 0) {
      throw new IllegalArgumentException("Compounding Frquency must be positive");
    }

    double duration = _mdc.calculate(bond, dirtyPrice);
    double yield = _byc.calculate(bond, dirtyPrice);
    // NOTE this yield is continuously compounded - convert to compoundingFrquency
    yield = compoundingFrquency * (Math.exp(yield / compoundingFrquency) - 1.0);

    return duration / (1 + yield / compoundingFrquency);
  }
}
