/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.money.Currency;

/**
 * Class describing a the sensitivity of some value (present value, par rate, etc) to a family of yield curves. 
 * The currency in which the sensitivity is expressed is indicated through a map.
 */
public class MultipleCurrencyCurveSensitivityMarket {

  /**
   * The backing map for the sensitivities in the different currencies. Not null.
   * The amount in the different currencies are not conversion of each other, they should be understood in an additive way.
   */
  private final TreeMap<Currency, CurveSensitivityMarket> _sensitivity;

  /**
   * Private constructor from an exiting map.
   * @param sensitivity The sensitivity map.
   */
  private MultipleCurrencyCurveSensitivityMarket(final TreeMap<Currency, CurveSensitivityMarket> sensitivity) {
    _sensitivity = sensitivity;
  }

  /**
   * Constructor. A new map is created.
   */
  public MultipleCurrencyCurveSensitivityMarket() {
    _sensitivity = new TreeMap<Currency, CurveSensitivityMarket>();
  }

  /**
   * Create a new multiple currency sensitivity with one currency.
   * @param ccy The currency. Not null.
   * @param sensitivity The sensitivity associated to the currency. Not null.
   * @return The multiple currency sensitivity.
   */
  public static MultipleCurrencyCurveSensitivityMarket of(final Currency ccy, final CurveSensitivityMarket sensitivity) {
    Validate.notNull(ccy, "Currency");
    Validate.notNull(sensitivity, "Sensitivity");
    final TreeMap<Currency, CurveSensitivityMarket> map = new TreeMap<Currency, CurveSensitivityMarket>();
    map.put(ccy, sensitivity);
    return new MultipleCurrencyCurveSensitivityMarket(map);
  }

  /**
   * Returns the (single currency) interest rate sensitivity associated to a given currency.
   * If the currency is not present in the map, an empty InterestRateCurveSensitivity is returned.
   * @param ccy The currency. Not null.
   * @return The (single currency) interest rate sensitivity.
   */
  public CurveSensitivityMarket getSensitivity(final Currency ccy) {
    Validate.notNull(ccy, "Currency");
    if (_sensitivity.containsKey(ccy)) {
      return _sensitivity.get(ccy);
    }
    return new CurveSensitivityMarket();
  }

  /**
   * Create a new multiple currency sensitivity by adding the sensitivity associated to a given currency. 
   * If the currency is not yet present in the existing sensitivity a new map is created with the extra entry.
   * If the currency is already present, the associated sensitivities are added (in the sense of {@link InterestRateCurveSensitivity}) and a new map is created with all the other
   * existing entries and the entry with the currency and the sum sensitivity.
   * @param ccy The currency. Not null.
   * @param sensitivity The sensitivity associated to the currency. Not null.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket plus(final Currency ccy, final CurveSensitivityMarket sensitivity) {
    Validate.notNull(ccy, "Currency");
    Validate.notNull(sensitivity, "Sensitivity");
    final TreeMap<Currency, CurveSensitivityMarket> map = new TreeMap<Currency, CurveSensitivityMarket>();
    if (_sensitivity.containsKey(ccy)) {
      map.put(ccy, sensitivity.plus(_sensitivity.get(ccy)));
      for (final Currency loopccy : _sensitivity.keySet()) {
        if (loopccy != ccy) {
          map.put(loopccy, _sensitivity.get(loopccy));
        }
      }
    } else {
      map.putAll(_sensitivity);
      map.put(ccy, sensitivity);
    }
    return new MultipleCurrencyCurveSensitivityMarket(map);
  }

  /**
   * Create a new multiple currency sensitivity by adding another multiple currency sensitivity. 
   * For each currency in the other multiple currency sensitivity, the currency and its associated sensitivity are added.
   * @param other The multiple currency sensitivity. Not null.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket plus(final MultipleCurrencyCurveSensitivityMarket other) {
    Validate.notNull(other, "Sensitivity");
    final TreeMap<Currency, CurveSensitivityMarket> map = new TreeMap<Currency, CurveSensitivityMarket>();
    map.putAll(_sensitivity);
    MultipleCurrencyCurveSensitivityMarket result = new MultipleCurrencyCurveSensitivityMarket(map);
    for (final Currency loopccy : other._sensitivity.keySet()) {
      result = result.plus(loopccy, other.getSensitivity(loopccy));
    }
    return result;
  }

  /**
   * Create a new multiple currency sensitivity by multiplying all the sensitivities in a multiple currency sensitivity by a common factor. 
   * @param factor The multiplicative factor.
   * @return The new multiple currency sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket multipliedBy(final double factor) {
    final TreeMap<Currency, CurveSensitivityMarket> map = new TreeMap<Currency, CurveSensitivityMarket>();
    for (final Currency loopccy : _sensitivity.keySet()) {
      map.put(loopccy, _sensitivity.get(loopccy).multiply(factor));
    }
    return new MultipleCurrencyCurveSensitivityMarket(map);
  }

  /**
   * Returns a new multiple currency sensitivity by creating clean sensitivity for each currency (see {@link InterestRateCurveSensitivity} clean() method).
   * @return The cleaned sensitivity.
   */
  public MultipleCurrencyCurveSensitivityMarket cleaned() {
    final TreeMap<Currency, CurveSensitivityMarket> map = new TreeMap<Currency, CurveSensitivityMarket>();
    for (final Currency loopccy : _sensitivity.keySet()) {
      map.put(loopccy, _sensitivity.get(loopccy).cleaned());
    }
    final MultipleCurrencyCurveSensitivityMarket result = new MultipleCurrencyCurveSensitivityMarket(map);
    return result;
  }

  /**
   * Compare two sensitivities with a given tolerance. The tolerance is used for both the time and the value. 
   * For each currency, the two sensitivities are suppose to be in the same time order.
   * @param sensi1 The first sensitivity.
   * @param sensi2 The second sensitivity.
   * @param tolerance The tolerance.
   * @return True if the difference is below the tolerance and False if not. If the currencies or the curves are not the same it returns False.
   */
  public static boolean compare(final MultipleCurrencyCurveSensitivityMarket sensi1, final MultipleCurrencyCurveSensitivityMarket sensi2, final double tolerance) {
    final boolean keycmp = sensi1._sensitivity.keySet().equals(sensi2._sensitivity.keySet());
    if (!keycmp) {
      return false;
    }
    for (final Currency loopccy : sensi1._sensitivity.keySet()) {
      if (!CurveSensitivityMarket.compare(sensi1.getSensitivity(loopccy), sensi2.getSensitivity(loopccy), tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the set of currencies in the multiple currency sensitivities.
   * @return The set of currencies.
   */
  public Set<Currency> getCurrencies() {
    return _sensitivity.keySet();
  }

  /**
   * Convert the multiple currency sensitivity to the sensitivity in a given currency.
   * @param ccy The currency in which the sensitivities should be converted.
   * @param fx The matrix with the exchange rates.
   * @return The one currency sensitivity.
   */
  public CurveSensitivityMarket convert(final Currency ccy, final FXMatrix fx) {
    CurveSensitivityMarket sensi = new CurveSensitivityMarket();
    for (Currency c : _sensitivity.keySet()) {
      double rate = fx.getFxRate(c, ccy);
      sensi = sensi.plus(_sensitivity.get(c).multiply(rate));
    }
    return sensi;
  }

  @Override
  public String toString() {
    return _sensitivity.toString();
  }

}