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

import com.eightkdata.mongowp.bson.BsonNull;
import com.eightkdata.mongowp.bson.BsonType;
import com.eightkdata.mongowp.bson.BsonValue;
import com.eightkdata.mongowp.bson.BsonValueVisitor;
import com.eightkdata.mongowp.bson.utils.BsonTypeComparator;

public abstract class AbstractBsonNull extends AbstractBsonValue<BsonNull> implements BsonNull {

  @Override
  public Class<? extends BsonNull> getValueClass() {
    return this.getClass();
  }

  @Override
  public BsonNull getValue() {
    return this;
  }

  @Override
  public BsonType getType() {
    return BsonType.NULL;
  }

  @Override
  public BsonNull asNull() {
    return this;
  }

  @Override
  public boolean isNull() {
    return true;
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

    if (obj.isUndefined() || obj.isDeprecated()) {
      return -1;
    }

    assert obj.isNull();
    return 0;
  }

  @Override
  public final boolean equals(Object obj) {
    return this == obj || obj != null && obj instanceof BsonNull;
  }

  @Override
  public final int hashCode() {
    return HASH;
  }

  @Override
  public <R, A> R accept(BsonValueVisitor<R, A> visitor, A arg) {
    return visitor.visit(this, arg);
  }

}
