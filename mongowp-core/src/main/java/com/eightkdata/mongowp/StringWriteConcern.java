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

package com.eightkdata.mongowp;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 */
class StringWriteConcern extends WriteConcern {

  private final String w;

  StringWriteConcern(@Nonnull SyncMode syncMode, @Nonnegative int timeout, String w) {
    super(syncMode, timeout);
    this.w = w;
  }

  @Override
  public int getWInt() throws UnsupportedOperationException {
    throw new UnsupportedOperationException("This write concern is using 'w' as a text");
  }

  @Override
  public String getWString() throws UnsupportedOperationException {
    return w;
  }

  @Override
  public WType getWType() {
    return WType.TEXT;
  }

}
