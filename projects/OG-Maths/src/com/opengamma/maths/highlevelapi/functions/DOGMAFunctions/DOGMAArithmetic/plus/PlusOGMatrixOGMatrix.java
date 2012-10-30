/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.functions.DOGMAFunctions.DOGMAArithmetic.plus;

import com.opengamma.maths.highlevelapi.datatypes.primitive.OGMatrix;
import com.opengamma.maths.lowlevelapi.exposedapi.BLAS;
import com.opengamma.maths.lowlevelapi.functions.checkers.Catchers;

/**
 * 
 */
public final class PlusOGMatrixOGMatrix implements PlusMinusAbstract<OGMatrix, OGMatrix> {
  private static PlusOGMatrixOGMatrix s_instance = new PlusOGMatrixOGMatrix();

  public static PlusOGMatrixOGMatrix getInstance() {
    return s_instance;
  }

  private PlusOGMatrixOGMatrix() {
  }

  private BLAS _localblas = new BLAS();

  @Override
  public OGMatrix plusminus(OGMatrix array1, OGMatrix array2, final int op) {
    int rowsArray1 = array1.getNumberOfRows();
    int columnsArray1 = array1.getNumberOfColumns();
    int rowsArray2 = array2.getNumberOfRows();
    int columnsArray2 = array2.getNumberOfColumns();
    int retRows = 0, retCols = 0;

    int n = array1.getData().length;
    double[] tmp = new double[n];
    System.arraycopy(array1.getData(), 0, tmp, 0, n);
    final double signval = Math.copySign(1, op);
    // Actually adding arrays
    if (rowsArray1 == 1 && columnsArray1 == 1) {
      n = array2.getData().length;
      tmp = new double[n];
      System.arraycopy(array2.getData(), 0, tmp, 0, n);
      final double singleDouble = array1.getData()[0] * signval;
      for (int i = 0; i < n; i++) {
        tmp[i] += singleDouble;
      }
      retRows = rowsArray2;
      retCols = columnsArray2;
    } else if (rowsArray2 == 1 && columnsArray2 == 1) {

      n = array1.getData().length;
      tmp = new double[n];
      System.arraycopy(array1.getData(), 0, tmp, 0, n);
      final double singleDouble = array2.getData()[0] * signval;
      for (int i = 0; i < n; i++) {
        tmp[i] += singleDouble;
      }
      retRows = rowsArray1;
      retCols = columnsArray1;
    } else {
      Catchers.catchBadCommute(rowsArray1, "rows in first array", rowsArray2, "rows in second array");
      Catchers.catchBadCommute(columnsArray1, "columns in first array", columnsArray2, "columns in second array");
      _localblas.daxpy(n, Math.copySign(1, op), array2.getData(), 1, tmp, 1);
      retRows = rowsArray1;
      retCols = columnsArray1;
    }
    return new OGMatrix(tmp, retRows, retCols);
  }
}