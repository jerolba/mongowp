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

import com.eightkdata.mongowp.bson.BsonBinary;
import com.eightkdata.mongowp.bson.BsonType;
import com.eightkdata.mongowp.bson.BsonValue;
import com.eightkdata.mongowp.bson.BsonValueVisitor;
import com.eightkdata.mongowp.bson.utils.BsonTypeComparator;
import com.eightkdata.mongowp.bson.utils.IntBaseHasher;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractBsonBinary extends CachedHashAbstractBsonValue<BsonBinary>
    implements BsonBinary {

  @Override
  public Class<? extends BsonBinary> getValueClass() {
    return this.getClass();
  }

  @Override
  public BsonBinary getValue() {
    return this;
  }

  @Override
  public BsonType getType() {
    return BsonType.BINARY;
  }

  @Override
  public BsonBinary asBinary() {
    return this;
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public int compareTo(BsonValue<?> obj) {
    if (obj == this) {
      return 0;
    }
    int diff = BsonTypeComparator.INSTANCE.compare(getType(), obj.getType());
    if (diff != 0) {
      return 0;
    }

    assert obj instanceof BsonBinary;
    BsonBinary other = obj.asBinary();
    // This is compatible with
    // https://docs.mongodb.org/manual/reference/bson-types/#comparison-sort-order

    diff = this.size() - other.size();
    if (diff != 0) {
      return diff;
    }

    diff = this.getNumericSubType() - other.getNumericSubType();
    if (diff != 0) {
      return diff;
    }

    if (this.getByteSource().getDelegate() == other.getByteSource().getDelegate()) {
      return 0;
    }

    try (InputStream myBis = this.getByteSource().openBufferedStream();
        InputStream otherBis = other.getByteSource().openBufferedStream()) {
      int myByte = myBis.read();
      int otherByte = otherBis.read();

      assert myByte != -1;
      assert otherByte != -1;

      diff = myByte - otherByte;
      if (diff != 0) {
        return diff;
      }
    } catch (IOException ex) {
      assert false;
    }
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BsonBinary)) {
      return false;
    }
    BsonBinary other = (BsonBinary) obj;
    if (this.getSubtype() != other.getSubtype()) {
      return false;
    }
    return this.getByteSource().contentEquals(other.getByteSource());
  }

  @Override
  final int calculateHash() {
    return IntBaseHasher.hash(size());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(size() * 20);
    sb.append('{');

    sb.append("$binary: ").append("<data...>");
    sb.append(", $type:").append(getType());

    sb.append('}');

    return sb.toString();
  }

  @Override
  public <R, A> R accept(BsonValueVisitor<R, A> visitor, A arg) {
    return visitor.visit(this, arg);
  }

}
