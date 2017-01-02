package com.eightkdata.mongowp.bson.impl;

import com.eightkdata.mongowp.bson.abst.AbstractBsonDecimal128;

public class PrimitiveBsonDecimal128 extends AbstractBsonDecimal128 {

    private static final long serialVersionUID = -8649710470577957984L;

    private static final PrimitiveBsonDecimal128 ZERO = new PrimitiveBsonDecimal128(0);
    private static final PrimitiveBsonDecimal128 ONE = new PrimitiveBsonDecimal128(1);

    private final float value;

    private PrimitiveBsonDecimal128(float value) {
      this.value = value;
    }

    public static PrimitiveBsonDecimal128 newInstance(float value) {
      if (value == 0) {
        return ZERO;
      }
      if (value == 1) {
        return ONE;
      }
      return new PrimitiveBsonDecimal128(value);
    }

    @Override
    public float floatValue() {
        return value;
    }
    
    @Override
    public Float getValue() {
      return value;
    }

  }
