/*
 * MongoWP
 * Copyright © 2014 8Kdata Technology (www.8kdata.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.eightkdata.mongowp.bson.abst;

import com.eightkdata.mongowp.bson.BsonDouble;
import com.eightkdata.mongowp.bson.BsonType;
import com.eightkdata.mongowp.bson.BsonValueVisitor;
import com.google.common.primitives.Doubles;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractBsonDouble extends AbstractBsonNumber<Double> implements BsonDouble {

  @Override
  public Class<? extends Double> getValueClass() {
    return Double.class;
  }

  @Override
  public BsonType getType() {
    return BsonType.DOUBLE;
  }

  @Override
  public Double getValue() {
    return doubleValue();
  }

  @Override
  public int intValue() {
    return (int) doubleValue();
  }

  @Override
  public long longValue() {
    return (long) doubleValue();
  }

  @Override
  public float floatValue() {
      return (float) doubleValue();
  }
  
  @Override
  public BsonDouble asDouble() {
    return this;
  }

  @Override
  public boolean isDouble() {
    return true;
  }

  @Override
  public boolean simmilar(BsonDouble other, double delta) {
    return Math.abs(this.doubleValue() - other.doubleValue()) < delta;
  }

  @SuppressFBWarnings("FE_FLOATING_POINT_EQUALITY")
  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BsonDouble)) {
      return false;
    }
    return this.doubleValue() == ((BsonDouble) obj).doubleValue();
  }

  @Override
  public final int hashCode() {
    return Doubles.hashCode(doubleValue());
  }

  @Override
  public <R, A> R accept(BsonValueVisitor<R, A> visitor, A arg) {
    return visitor.visit(this, arg);
  }

}
