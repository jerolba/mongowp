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

import com.eightkdata.mongowp.bson.BsonDecimal128;
import com.eightkdata.mongowp.bson.BsonDouble;
import com.eightkdata.mongowp.bson.BsonInt32;
import com.eightkdata.mongowp.bson.BsonInt64;
import com.eightkdata.mongowp.bson.BsonNumber;
import com.eightkdata.mongowp.bson.BsonValue;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonDecimal128;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonDouble;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonInt32;
import com.eightkdata.mongowp.bson.impl.PrimitiveBsonInt64;
import com.eightkdata.mongowp.bson.utils.BsonTypeComparator;
import com.google.common.primitives.Doubles;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS",
    justification = "Sub-classes must implement equals and hash code properly")
public abstract class AbstractBsonNumber<V extends Number> extends AbstractBsonValue<V>
    implements BsonNumber<V> {

  @Override
  public abstract boolean equals(Object other);

  @Override
  public abstract int hashCode();

  @Override
  public BsonInt64 asInt64() {
    return PrimitiveBsonInt64.newInstance(longValue());
  }

  @Override
  public BsonInt32 asInt32() {
    return PrimitiveBsonInt32.newInstance(intValue());
  }

  @Override
  public BsonDouble asDouble() {
    return PrimitiveBsonDouble.newInstance(doubleValue());
  }
  
  @Override
  public BsonDecimal128 asDecimal128() {
    return PrimitiveBsonDecimal128.newInstance(floatValue());
  }

  @Override
  public int compareTo(BsonValue<?> obj) {
    if (obj == this) {
      return 0;
    }
    int diff = BsonTypeComparator.INSTANCE.compare(getType(), obj.getType());
    if (diff != 0) {
      return diff;
    }

    assert obj instanceof BsonNumber;
    BsonNumber<?> other = obj.asNumber();
    return Doubles.compare(this.doubleValue(), other.doubleValue());
  }

  @Override
  public boolean isNumber() {
    return true;
  }

  @Override
  public BsonNumber asNumber() throws UnsupportedOperationException {
    return this;
  }

  @Override
  public String toString() {
    return getValue().toString();
  }
}
