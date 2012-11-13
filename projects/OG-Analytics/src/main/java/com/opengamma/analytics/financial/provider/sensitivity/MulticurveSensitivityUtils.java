/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * Utilities to manipulate present value sensitivities.
 * <p>
 * This is a thread-safe static utility class.
 */
public class MulticurveSensitivityUtils {

  /**
   * Restricted constructor.
   */
  protected MulticurveSensitivityUtils() {
    super();
  }

  /**
   * Clean a map by sorting the times and adding the values at duplicated times.
   * @param map The map.
   * @return The cleaned map.
   */
  public static Map<String, List<DoublesPair>> cleaned(final Map<String, List<DoublesPair>> map) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final Map.Entry<String, List<DoublesPair>> entry : map.entrySet()) {
      final List<DoublesPair> list = entry.getValue();
      final List<DoublesPair> listClean = new ArrayList<DoublesPair>();
      final Set<Double> set = new TreeSet<Double>();
      for (final DoublesPair pair : list) {
        set.add(pair.getFirst());
      }
      for (final Double time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          if (Double.doubleToLongBits(list.get(looplist).getFirst()) == Double.doubleToLongBits(time)) {
            sensi += list.get(looplist).second;
          }
        }
        listClean.add(new DoublesPair(time, sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  public static Map<String, List<ForwardSensitivity>> cleanedFwd(final Map<String, List<ForwardSensitivity>> map) {
    //TODO: improve the sorting algorithm.
    final Map<String, List<ForwardSensitivity>> result = new HashMap<String, List<ForwardSensitivity>>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map.entrySet()) {
      final List<ForwardSensitivity> list = entry.getValue();
      final List<ForwardSensitivity> listClean = new ArrayList<ForwardSensitivity>();
      final Set<Triple<Double, Double, Double>> set = new TreeSet<Triple<Double, Double, Double>>();
      for (final ForwardSensitivity pair : list) {
        set.add(new Triple<Double, Double, Double>(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor()));
      }
      for (final Triple<Double, Double, Double> time : set) {
        double sensi = 0;
        for (int looplist = 0; looplist < list.size(); looplist++) {
          final ForwardSensitivity fwdSensitivity = list.get(looplist);
          final Triple<Double, Double, Double> triple = new Triple<Double, Double, Double>(fwdSensitivity.getStartTime(), fwdSensitivity.getEndTime(), fwdSensitivity.getAccrualFactor());
          if (triple.equals(time)) {
            sensi += list.get(looplist).getValue();
          }
        }
        listClean.add(new ForwardSensitivity(time.getFirst(), time.getSecond(), time.getThird(), sensi));
      }
      result.put(entry.getKey(), listClean);
    }
    return result;
  }

  //  /**
  //   * Takes a list of curve sensitivities (i.e. an unordered list of pairs of times and sensitivities) and returns a list order by ascending
  //   * time, and with sensitivities that occur at the same time netted (zero net sensitivities are removed)
  //   * @param old An unordered list of pairs of times and sensitivities
  //   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed
  //   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed
  //   * @return A time ordered netted list
  //   */
  //  static final List<DoublesPair> cleaned(final List<DoublesPair> old, final double relTol, final double absTol) {
  //
  //    ArgumentChecker.notNull(old, "null list");
  //    ArgumentChecker.isTrue(relTol >= 0.0 && absTol >= 0.0, "Tolerances must be greater than zero");
  //    if (old.size() == 0) {
  //      return new ArrayList<DoublesPair>();
  //    }
  //    final List<DoublesPair> res = new ArrayList<DoublesPair>();
  //    final DoublesPair[] sort = old.toArray(new DoublesPair[] {});
  //    Arrays.sort(sort, FirstThenSecondDoublesPairComparator.INSTANCE);
  //    final DoublesPair pairOld = sort[0];
  //    double tOld = pairOld.first;
  //    double sum = pairOld.getSecond();
  //    double scale = Math.abs(sum);
  //    double t = tOld;
  //    for (int i = 1; i < sort.length; i++) {
  //      final DoublesPair pair = sort[i];
  //      t = pair.first;
  //      if (t > tOld) {
  //        if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
  //          res.add(new DoublesPair(tOld, sum));
  //        }
  //        tOld = t;
  //        sum = pair.getSecondDouble();
  //        scale = Math.abs(sum);
  //      } else {
  //        sum += pair.getSecondDouble();
  //        scale += Math.abs(pair.getSecondDouble());
  //      }
  //    }
  //
  //    if (Math.abs(sum) > absTol && Math.abs(sum) / scale > relTol) {
  //      res.add(new DoublesPair(t, sum));
  //    }
  //
  //    return res;
  //  }
  //
  //  /**
  //   * Takes a map of curve sensitivities (i.e. a map between curve names and a unordered lists of pairs of times and sensitivities)
  //   *  and returns a similar map where the lists order by ascending time, and with sensitivities that occur at the same time netted
  //   *  (zero net sensitivities are removed)
  //   * @param old A map between curve names and unordered lists of pairs of times and sensitivities
  //   * @param relTol Relative tolerance - if the net divided by gross sensitivity is less than this it is ignored/removed
  //   * @param absTol Absolute tolerance  - is the net sensitivity is less than this it is ignored/removed
  //   * @return A map between curve names and time ordered netted lists
  //   */
  //  public static Map<String, List<DoublesPair>> cleaned(final Map<String, List<DoublesPair>> old, final double relTol, final double absTol) {
  //    final Map<String, List<DoublesPair>> res = new HashMap<String, List<DoublesPair>>();
  //    for (final Map.Entry<String, List<DoublesPair>> entry : old.entrySet()) {
  //      List<DoublesPair> cleanList = cleaned(entry.getValue(), relTol, absTol);
  //      if (!cleanList.isEmpty()) {
  //        res.put(entry.getKey(), cleanList);
  //      }
  //    }
  //    return res;
  //  }

  /**
   * Add two list representing sensitivities into one. No attempt is made to net off sensitivities occurring at the same time - Use clean()
   * to do this
   * @param sensi1 First list of sensitivities
   * @param sensi2 Second list of sensitivities
   * @return combined list
   */
  public static List<DoublesPair> plus(final List<DoublesPair> sensi1, final List<DoublesPair> sensi2) {
    final List<DoublesPair> result = new ArrayList<DoublesPair>(sensi1);
    result.addAll(sensi2);
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Add two maps representing sensitivities into one.
   * 
   * @param sensi1  the first sensitivity, not null
   * @param sensi2  the second sensitivity, not null
   * @return the total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> plus(final Map<String, List<DoublesPair>> sensi1, final Map<String, List<DoublesPair>> sensi2) {
    ArgumentChecker.notNull(sensi1, "sensitivity");
    ArgumentChecker.notNull(sensi2, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi1.entrySet()) {
      final String name = entry.getKey();
      if (sensi2.containsKey(name)) {
        result.put(name, plus(entry.getValue(), sensi2.get(name)));
      } else {
        result.put(name, entry.getValue());
      }
    }
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        result.put(name, entry.getValue());
      }
    }
    return result;
  }

  /**
   * Add the list representing the sensitivity to one curve to the map of sensitivities to several curves.
   * @param sensi The multi-curves sensitivity. Not null.
   * @param curveName  The name of the curve the sensitivity of which is added. Not null.
   * @param list The sensitivity as a list. Not null.
   * @return The total sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> plus(final Map<String, List<DoublesPair>> sensi, final String curveName, final List<DoublesPair> list) {
    ArgumentChecker.notNull(sensi, "sensitivity");
    ArgumentChecker.notNull(list, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensi.entrySet()) {
      final String name = entry.getKey();
      if (name.equals(curveName)) {
        result.put(name, plus(entry.getValue(), list));
      } else {
        result.put(name, entry.getValue());
      }
    }
    if (!result.containsKey(curveName)) {
      result.put(curveName, list);
    }
    return result;
  }

  /**
   * Add two maps links to forward curves.
   * @param map1 The first map.
   * @param map2 The second map.
   * @return The sum.
   */
  public static Map<String, List<ForwardSensitivity>> plusFwd(final Map<String, List<ForwardSensitivity>> map1, final Map<String, List<ForwardSensitivity>> map2) {
    final Map<String, List<ForwardSensitivity>> result = new HashMap<String, List<ForwardSensitivity>>();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map1.entrySet()) {
      final List<ForwardSensitivity> temp = new ArrayList<ForwardSensitivity>();
      final String name = entry.getKey();
      for (final ForwardSensitivity pair : entry.getValue()) {
        temp.add(pair);
      }
      if (map2.containsKey(name)) {
        for (final ForwardSensitivity pair : map2.get(name)) {
          temp.add(pair);
        }
      }
      result.put(name, temp);
    }
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : map2.entrySet()) {
      final String name = entry.getKey();
      if (!result.containsKey(name)) {
        final List<ForwardSensitivity> temp = new ArrayList<ForwardSensitivity>();
        for (final ForwardSensitivity pair : entry.getValue()) {
          temp.add(pair);
        }
        result.put(name, temp);
      }
    }
    return result;
  }

  /**
   * Multiply a sensitivity map by a common factor.
   * 
   * @param sensitivity  the original sensitivity, not null
   * @param factor  the multiplicative factor, not null
   * @return the multiplied sensitivity, not null
   */
  public static Map<String, List<DoublesPair>> multipliedBy(final Map<String, List<DoublesPair>> sensitivity, final double factor) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivity.entrySet()) {
      result.put(entry.getKey(), multipliedBy(entry.getValue(), factor));
    }
    return result;
  }

  public static List<DoublesPair> multipliedBy(final List<DoublesPair> sensitivity, final double factor) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    final List<DoublesPair> curveSensi = new ArrayList<DoublesPair>();
    for (final DoublesPair pair : sensitivity) {
      curveSensi.add(new DoublesPair(pair.first, pair.second * factor));
    }
    return curveSensi;
  }

  public static Map<String, List<ForwardSensitivity>> multipliedByFwd(final Map<String, List<ForwardSensitivity>> map, final double factor) {
    final Map<String, List<ForwardSensitivity>> result = new HashMap<String, List<ForwardSensitivity>>();
    for (final String name : map.keySet()) {
      final List<ForwardSensitivity> curveSensi = new ArrayList<ForwardSensitivity>();
      for (final ForwardSensitivity pair : map.get(name)) {
        curveSensi.add(new ForwardSensitivity(pair.getStartTime(), pair.getEndTime(), pair.getAccrualFactor(), pair.getValue() * factor));
      }
      result.put(name, curveSensi);
    }
    return result;
  }

}